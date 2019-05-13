package com.owoh.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.owoh.interfaces.CameraApiInterface;
import com.owoh.renderer.CameraRenderer;


/**
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/10/11 13:09
 * Des    :
 */

public class CameraSGLSurfaceView extends GLSurfaceView {
    private CameraRenderer sRenderer;

    public CameraSGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void init(CameraApiInterface mCamera) {
        //设置opengl 的渲染环境版本2，如果需要是3.0的话，那么在c++ 层去做
        setEGLContextClientVersion(2);
        sRenderer = new CameraRenderer(this);
        sRenderer.init(mCamera);
        setRenderer(sRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
