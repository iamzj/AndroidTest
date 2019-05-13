/**
 * 
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/11/13 13:59
 * Des    : 
 */
//
#include <camera/camera.h>

#include "camera/CameraSence.h"
#include "camera/Model3D.h"
#include "camera/FrameBuffer.h"
#include "camera/FullScreenQuad.h"
glm::vec3 carmeaPoss(0.0f, 0.0f, 6.0f);
Camera *mCamera ;

FrameBuffer *frameBuffer;
FullScreenQuad *normalSQ;
CameraSence::CameraSence() {
}

CameraSence::~CameraSence() {}

void CameraSence::onBeforeSurfaceCreated(JNIEnv *env, jobject bitmapObj) {
    mCamera = new Camera;
    mCamera->createSurfaceTextureObject(env);

}

void CameraSence::onSurfaceCreated() {
    mCamera->initVertex(0.0f,0.0f,0.0f,4);
    //全屏四方形
    normalSQ = new FullScreenQuad;
    normalSQ->init();
//    mCamera->mShader->init("resource/camera/camera_normal.vs","resource/effects/fullsrceenfbo.fs");
    //mCamera->mShader->init("resource/camera/camera_normal.vs","resource/effects/fullsrceenfbo.fs");
    normalSQ->mShader->init("resource/camera/camera_normal.vs","resource/camera/camera_normal.fs");
}



void CameraSence::onSurfaceChanged(float width, float height) {
    glViewport(0,0,width,height);
    mCamera->initMVP(width,height,carmeaPoss);
    frameBuffer = new FrameBuffer;
    frameBuffer->attachColorBuffer("color",GL_COLOR_ATTACHMENT0,(int)width,(int)height);
    frameBuffer->attachDepthBuffer("depth",(int)width,(int)height);
    frameBuffer->finish();

    mCamera->mShader->setTexture("U_Texture",frameBuffer->getBufferByName("color"));
}

//绘制场景
void CameraSence::onDrawFrame( void * data,int width,int height) {
    if(mCamera== nullptr){
        LOGE("camera is nullptr");
        return;
    }
    glClearColor(mCamera->mBgColor.r,mCamera->mBgColor.g,mCamera->mBgColor.b,mCamera->mBgColor.a);
    glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    float time = getTime();
    frameBuffer->bind();
    normalSQ->draw();
    frameBuffer->unBind();

    LOGE(" CameraSence::onDrawFrame");
//    //良好习惯，当绘制完毕后，将程序置为0 号程序
    glUseProgram(0);
//    LOGE("draw usetime %f",time);
}

//增加一个方法，这个方法在onSurfaceCreated 之前调用

void CameraSence::releaseNative(JNIEnv *env) {

}

void CameraSence::changeFilter(jint r, jint g, jint b, jint a) {
    if (mCamera){
        mCamera->changeFilter(r, g, b, a);
    }

}

void CameraSence::changeVSFS(const char *vs, const char *fs) {
    if (mCamera)
        mCamera->changeVSFS(vs, fs);

}

void CameraSence::changeShape(int cameraShape, int count) {
    if (mCamera) {
        mCamera->changeShape(cameraShape, count);
    }

}

void CameraSence::changeBgColor(glm::vec4 bgcolor) {
    if (mCamera) {
        mCamera->changeBgColor(bgcolor);
    }
}

void CameraSence::changeShapeSize(float size) {
    if (mCamera) {
        mCamera->changeShapeSize(size);
    }
    //
}

void CameraSence::changeShapeDrawCount(int count) {
    if (mCamera) {
        mCamera->changeShapeDrawCount(count);
    }
}

void CameraSence::changeFileterZoom(float temp) {
    if (mCamera) {
        mCamera->changeFileterZoom(temp);
    }
}

jobject CameraSence::getSurfaceTexture(){
    if (mCamera) {
        LOGE("getSurfaceTexturegetSurfaceTexturegetSurfaceTexture");
        return mCamera->javaSurfaceTextureObj;
    }
    return nullptr;
}


