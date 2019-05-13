package com.owoh.natives;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

/**
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/11/13 14:05
 * Des    :
 */

public class BaseGLNative {
    //加载native 的camera 场景
    public static final int NATIVE_SENCE_CAMERA=0;
    //加载的是native 的piture 场景
    public static final int NATIVE_SENCE_PICTURE=1;

    //目前使用动态注册的方式
    static{
        System.loadLibrary("owoh");
    }



    public enum CameraShape{

        Normal(0),Circle(1),Multiple(2);
        private int value;
        CameraShape(int i) {
            this.value=i;
        }

        public int getValue() {
            return value;
        }
    }
    /**
     * 为了让底层去获取android 资源
     * @param assets
     */
    public static native void initAssetManager(AssetManager assets,int senceType);

    /**
     * JNI 创建SurfaceTexture
     * @return
     */
    public static native SurfaceTexture getSurfaceTexture();

    /**
     * 增加一个方法，就是在onSurfaceCreated调用之前作为底层需要参数初始化
     * @param bitmap
     */
    public static native void onBeforeSurfaceCreated(Object bitmap);
    /**
     * 初始化opengl
     */
    public static native void onSurfaceCreated();

    public static native void onSurfaceChanged(int width, int height);

    //更新一帧画面
    public static native void onDrawFrame(byte[] currentData,int width,int height);
    //主要用于在GL线程在不断画的过程中初始化一些东西，比如传递图片到底层
    public static native void onDrawFrameBefore(Object currentData);

    //稀放带有一个参数，是释放谁的场景
    public static native void releaseNative(int senceType);

    //修改滤镜参数
    public static native void onChangeFileter(int r,int g,int b,int a,int max);
    //修改 滤镜文件
    public static native void onChangeVSFS(String vs, String fs) ;

    //修改形状
    public static native void onChangeShape(int  cameraShape,int count) ;


    public static boolean isStop = false;

    //修改背景颜色，请传0-1之间的值
    public static native void onChangeBgColor(float r, float g, float b, float a);
    //修改形状的大小
    public static native void onChangeShapeSize(int size,int max) ;
    //修改及几遍形
    public static native void onChangeShapeCount(int count) ;
    //修改要渲染的区域
    public static native void onChangeFileterZoom(int i, int max) ;

    //特效 添加文字
    public static native void addTextEffect(Bitmap bitmap) ;

}
