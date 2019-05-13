package com.owoh.camera;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.owoh.camera.camera.CameraOldVersion;
import com.owoh.camera.natives.BaseGLNative;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/10/11 13:06
 * Des    :
 */

public class CameraRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    GLSurfaceView mGLSurfaceView;
    private CameraOldVersion mCamera;
    private SurfaceTexture mSurfaceTexture;

    public CameraRenderer(CameraSGLSurfaceView cameraSGLSurfaceView) {
        mGLSurfaceView = cameraSGLSurfaceView;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurfaceTexture = BaseGLNative.getSurfaceTexture();
        if(mSurfaceTexture!=null){
            Log.e("sen_","mSurfaceTexture is not null");
            mSurfaceTexture.setOnFrameAvailableListener(this);
        }
//        mCamera.setSurfaceTexture(mSurfaceTexture);
//        mCamera.onResume();
    //    BaseGLNative.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
     //   BaseGLNative.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null &&!BaseGLNative.isStop) {
            mSurfaceTexture.updateTexImage();
           // BaseGLNative.onDrawFrame(mCamera.getCurrentData());
        }

    }

    public void init(CameraSGLSurfaceView cameraSGLSurfaceView, CameraOldVersion camera) {
        mGLSurfaceView = cameraSGLSurfaceView;
        mCamera = camera;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(!BaseGLNative.isStop) {
            mGLSurfaceView.requestRender();
        }

    }





}
