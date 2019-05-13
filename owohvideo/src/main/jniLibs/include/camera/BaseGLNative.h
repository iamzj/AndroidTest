/**
 * 
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/11/13 14:10
 * Des    : 
 */
//


#include "sggl.h"


/* Header for class sen_com_openglcamera_natives_BaseGLNative */

#ifndef _Included_sen_com_openglcamera_natives_BaseGLNative
#define _Included_sen_com_openglcamera_natives_BaseGLNative
#ifdef __cplusplus

class BaseGLNative {

public:

    BaseGLNative();
    virtual ~BaseGLNative();

    AAssetManager *aAssetManager;

//    //增加一个方法，这个方法在onSurfaceCreated 之前调用
//    virtual void onBeforeSurfaceCreated(JNIEnv *env, jobject bitmapObj);
//
//    virtual void onSurfaceCreated();
//
//    virtual void onSurfaceChanged(int width, int height);
//
//    virtual void onDrawFrame();
//
//    virtual void releaseNative(JNIEnv *env);
//
//    virtual void changeFilter(jint r, jint g, jint b, jint a);
//
//    virtual void changeVSFS(const char *vs, const char *fs);
//
//    virtual void changeShape(int cameraShape, int count);
//
//    virtual void changeBgColor(glm::vec4 bgcolor);
//
//    virtual void changeShapeSize(float size);
//
//    virtual void changeShapeDrawCount(int count);

//    virtual void changeFileterZoom(float temp);
};

#endif
#endif
