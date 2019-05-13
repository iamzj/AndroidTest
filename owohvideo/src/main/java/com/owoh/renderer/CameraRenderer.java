package com.owoh.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.owoh.interfaces.CameraApiInterface;
import com.owoh.natives.BaseGLNative;
import com.owoh.view.CameraSGLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/10/11 13:06
 * Des    :
 */

public class CameraRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener , Camera.PreviewCallback{
    GLSurfaceView mGLSurfaceView;
    private Context mContext;
    private CameraApiInterface mCamera;
    private SurfaceTexture mSurfaceTexture;
    private byte[] currentData;
    private byte[]  callbackBuffer;
    public CameraRenderer(CameraSGLSurfaceView cameraSGLSurfaceView) {
        mGLSurfaceView = cameraSGLSurfaceView;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        mSurfaceTexture = CameraSGLNative.getSurfaceTexture();
        mSurfaceTexture = new SurfaceTexture(1);
        if(mSurfaceTexture!=null){
            Log.e("sen_","mSurfaceTexture is not null");
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }
        mCamera.setSurfaceTexture(mSurfaceTexture);
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        callbackBuffer = new byte[mCamera.getPreviewSize ().getWidth()*mCamera.getPreviewSize().getHeight()*3/2];
        BaseGLNative.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        GLES20.glEnable(GL10.GL_POINT_SMOOTH);
//        GLES20.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);
//        GLES20.glEnable(GL10.GL_LINE_SMOOTH);
//        GLES20.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
//        GLES20.glEnable(GL10.GL_MULTISAMPLE);
//        gl.glMatrixMode(GL10.GL_PROJECTION);
//        gl.glLoadIdentity();
//        float ritio = (float)width/(float)height;
//        gl.glFrustumf(-ritio, ritio,-1f,1f,3f,7f);
        BaseGLNative.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null &&!BaseGLNative.isStop) {
            synchronized (CameraRenderer.class) {
                //  mSurfaceTexture.updateTexImage();
                BaseGLNative.onDrawFrame(currentData,
                        mCamera.getPreviewSize().getWidth(),
                        mCamera.getPreviewSize().getHeight());
            }
        }else {
            mCamera.stopPreview();
            Log.e("sen_","java stop");
        }

    }

    public void init(CameraApiInterface camera) {
        mCamera = camera;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(!BaseGLNative.isStop) {
            mGLSurfaceView.requestRender();
        }

    }




    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        synchronized (CameraRenderer.class){
            currentData  = bytes;
        }
        mGLSurfaceView.requestRender();

    }
}
