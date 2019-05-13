/**
 * 
 * Author : 唐家森
 * Version: 1.0
 * On     : 2017/11/13 14:10
 * Des    : 从新整理一下c++架构
 */
//
#include <camera/BaseSences.h>
#include <camera/utils.h>
#include <camera/sggl.h>
#include "camera/sggl.h"
#include "camera/CameraSence.h"
#include "camera/BaseGLNative.h"
#include "camera/NativeSencesType.h"

BaseSences *mBaseSences;
AAssetManager *aAssetManager;
std::map<NativeSencesType, BaseSences *> mSencesManager;

BaseGLNative::BaseGLNative() {
    aAssetManager = nullptr;
}

BaseGLNative::~BaseGLNative() {
    if (aAssetManager != nullptr) {
        free(aAssetManager);
    }
}

/**
 * 初始化AssetManager
 * @param env
 * @param jclazz
 * @param assetManager
 * @param senceType
 */
void initAssetManager(JNIEnv *env, jclass jclazz, jobject assetManager, jint senceType) {
    aAssetManager = AAssetManager_fromJava(env, assetManager);
    LOGE("initAssetManager ok1");
    if (senceType == CAMERA) {
        mBaseSences = new CameraSence;
        mBaseSences->onBeforeSurfaceCreated(env, nullptr);
        mSencesManager.insert(std::pair<NativeSencesType, BaseSences *>(CAMERA, mBaseSences));
    }
    LOGE("initAssetManager ok");
};


void onSurfaceCreated(JNIEnv *env, jclass clzss) {
    if(mBaseSences){
        mBaseSences->onSurfaceCreated();
    }else{

    }
    LOGE("onSurfaceCreated ok");

};


void onBeforeSurfaceCreated(JNIEnv *env, jclass clzss, jobject jobj) {
    if(mBaseSences){
        mBaseSences->onBeforeSurfaceCreated(env, jobj);
    }

}


unsigned char *loadFile(const char *path, int &fileSize) {
    unsigned char *file = nullptr;
    if (path == nullptr) {
        return nullptr;
    }
    fileSize = 0;
    //android 读取内部资源的方法
    AAsset *asset = AAssetManager_open(aAssetManager, path, AASSET_MODE_UNKNOWN);
    if (asset == nullptr)
        return nullptr;
    //读取成功
    fileSize = AAsset_getLength(asset);
    //开辟内存 +1 为了file[fileSize] = '\0';
    file = new unsigned char[fileSize + 1];
    AAsset_read(asset, file, fileSize);
    //为了程序的健壮性
    file[fileSize] = '\0';
    //关闭
    AAsset_close(asset);
    return file;
}

void writeFileToSdcard(const char *path, const char *outPath) {
    unsigned char *file = nullptr;
    if (path == nullptr) {
        return;
    }
    int fileSize = 0;
    //android 读取内部资源的方法
    AAsset *asset = AAssetManager_open(aAssetManager, path, AASSET_MODE_UNKNOWN);
    if (asset == nullptr)
        return;
    //读取成功
    fileSize = AAsset_getLength(asset);
    //开辟内存 +1 为了file[fileSize] = '\0';
    file = new unsigned char[fileSize + 1];
    AAsset_read(asset, file, fileSize);
    //为了程序的健壮性
    file[fileSize] = '\0';
    FILE *outFile = fopen(outPath, "w");
    fwrite(file, 1, fileSize, outFile);
    fclose(outFile);
    free(file);
    //关闭
    AAsset_close(asset);
    LOGE("out File ok");
}


void onSurfaceChanged(JNIEnv *env, jclass clzss, jint width, jint height) {
    mBaseSences->onSurfaceChanged(width, height);
};

void onDrawFrame(JNIEnv *env, jclass clzss, jbyteArray data, jint width, jint height) {
    if (data) {
        jbyte *cameraData = env->GetByteArrayElements(data, NULL);
        mBaseSences->onDrawFrame(data, width, height);
        env->ReleaseByteArrayElements(data, cameraData, 0);
    } else {
        mBaseSences->onDrawFrame(nullptr, width, height);
    }
};

void releaseNative(JNIEnv *env, jclass type, jint senceType) {
    if (aAssetManager) {
        free(aAssetManager);
        aAssetManager = nullptr;
    }
    if (!mSencesManager.empty()) {
        if (senceType == CAMERA) {
            auto iterators = mSencesManager.find(CAMERA);
            if (iterators != mSencesManager.end()) {
                iterators->second->releaseNative(env);
                delete iterators->second;
                iterators->second = nullptr;
                mSencesManager.erase(CAMERA);
            }
        }
    }

}


void onChangeFileter(JNIEnv *env, jclass type,
                     jint r, jint g, jint b, jint a,
                     jint max) {
    float rc = (float) r / (float) max;
    float gc = (float) g / (float) max;
    float bc = (float) b / (float) max;
    float ac = (float) a / (float) max;
    LOGE("rc:%f**gc:%f**bc:%f**ac:%f", rc, gc, bc, ac);
    mBaseSences->changeFilter(rc, gc, bc, ac);
}

void onChangeVSFS(JNIEnv *env, jclass type,
                  jstring vs_, jstring fs_) {

    const char *vs = env->GetStringUTFChars(vs_, 0);
    const char *fs = env->GetStringUTFChars(fs_, 0);
    if (vs != nullptr && fs != nullptr) {
        mBaseSences->changeVSFS(vs, fs);
    }
    env->ReleaseStringUTFChars(vs_, vs);
    env->ReleaseStringUTFChars(fs_, fs);
}


//修改形状
void onChangeShape(JNIEnv *env, jclass type,
                   jint cameraShape, jint count) {
    if (count < 3)
        count = 3;
    mBaseSences->changeShape(cameraShape, count);
}

//修改背景颜色
void onChangeBgColor(JNIEnv *env, jclass type,
                     jfloat r, jfloat g, jfloat b,
                     jfloat a) {
    //检验数据
    r = checkData(r);
    g = checkData(g);
    b = checkData(b);
    a = checkData(a);
    mBaseSences->changeBgColor(glm::vec4(r, g, b, a));
}

void onChangeShapeSize(JNIEnv *env, jclass type,
                       jint size, jint max) {

    if (size > 0) {
        //先减少 ，后变大
        mBaseSences->changeShapeSize(1.0f - float(size) / float(max));
    }
}

void onChangeShapeCount(JNIEnv *env, jclass type,
                        jint count) {
    //组成一个面至少3个顶点
    if (count >= 4) {
        mBaseSences->changeShapeDrawCount(count);
    }

}


void onChangeFileterZoom(JNIEnv *env, jclass type,
                         jint current, jint max) {

    if (current >= 0 && max > 0) {
        //将中间设置为0
        if (current == max / 2) {
            current = 0;
        } else {
            current -= max;
        }
        float temp = float(current) / float(max);

        mBaseSences->changeFileterZoom(temp);
    }
}

jobject getSurfaceTexture(JNIEnv *env, jclass jcla) {
    return mBaseSences->getSurfaceTexture();
}

void addTextEffect(JNIEnv *env, jclass type, jobject bitmap) {
    if (bitmap) {
        LOGE("sen______0");
        void *piexl;
        float width, height;
        AndroidBitmap_lockPixels(env, bitmap, &piexl);
        AndroidBitmapInfo info;
        AndroidBitmap_getInfo(env, bitmap, &info);
        width = info.width;
        height = info.height;
        mBaseSences->addTextEffect(piexl, width, height);
        AndroidBitmap_unlockPixels(env, bitmap);
    }


}




/**
 * 动态注册相关的
 */

#define JNIREG_CLASS "com/owoh/natives/BaseGLNative"


static int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *gMethods,
                                 int numMethods) {
    jclass clazz = nullptr;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

//BaseGLNative 的所有方法
static JNINativeMethod mMethods[] = {
        {"initAssetManager",       "(Landroid/content/res/AssetManager;I)V", (void *) initAssetManager},
        {"onSurfaceCreated",       "()V",    (void *) onSurfaceCreated},
        {"onSurfaceChanged",       "(II)V",    (void *) onSurfaceChanged},
        {"onBeforeSurfaceCreated", "(Ljava/lang/Object;)V",    (void *) onBeforeSurfaceCreated},
        {"onDrawFrame",            "([BII)V",    (void *) onDrawFrame},
        {"releaseNative",          "(I)V",    (void *) releaseNative},
        {"onChangeFileter",        "(IIIII)V",    (void *) onChangeFileter},
        {"onChangeVSFS",           "(Ljava/lang/String;Ljava/lang/String;)V",    (void *) onChangeVSFS},
        {"onChangeShape",          "(II)V",    (void *) onChangeShape},
        {"onChangeBgColor",        "(FFFF)V",    (void *) onChangeBgColor},
        {"onChangeShapeSize",      "(II)V",    (void *) onChangeShapeSize},
        {"onChangeShapeCount",     "(I)V",    (void *) onChangeShapeCount},
        {"onChangeFileterZoom",    "(II)V",    (void *) onChangeFileterZoom},
        {"getSurfaceTexture",      "()Landroid/graphics/SurfaceTexture;",    (void *) getSurfaceTexture},
        {"addTextEffect",          "(Landroid/graphics/Bitmap;)V",    (void *) addTextEffect},
};

//BaseGLNative 的所有方法
//static JNINativeMethod mMethods[] = {
//        {"initAssetManager",       "(Landroid/content/res/AssetManager;I)V", (void *) initAssetManager},
//        {"onSurfaceCreated",       "()V",    (void *) onSurfaceCreated},
//        {"onSurfaceChanged",       "(II)V",    (void *) onSurfaceChanged},
//        {"onBeforeSurfaceCreated", "(Ljava/lang/Object;)V",    (void *) onBeforeSurfaceCreated},
//        {"onDrawFrame",            "(Ljava/lang/Object;)V",    (void *) onDrawFrame},
//        {"releaseNative",          "(I)V",    (void *) releaseNative},
//        {"onChangeFileter",        "(II)V",    (void *) onChangeFileter},
//        {"onChangeVSFS",           "(Ljava/lang/String;Ljava/lang/String;)V",    (void *) onChangeVSFS},
//        {"onChangeShape",          "(I)V",    (void *) onChangeShape},
//        {"onChangeBgColor",        "(FFFF)V",    (void *) onChangeBgColor},
//        {"onChangeShapeSize",      "(II)V",    (void *) onChangeShapeSize},
//        {"onChangeShapeCount",     "(I)V",    (void *) onChangeShapeCount},
//        {"onChangeFileterZoom",    "(II)V",    (void *) onChangeFileterZoom},
//        {"getSurfaceTexture",      "()Landroid/graphics/SurfaceTexture;",    (void *) getSurfaceTexture},
//        {"addTextEffect",          "(Landroid/graphics/Bitmap;)V",    (void *) addTextEffect},
//};

/*
* 为所有该类注册本地方法
*/
static int registerNatives(JNIEnv *env) {
    int re = registerNativeMethods(env, JNIREG_CLASS, mMethods,
                                   sizeof(mMethods) / sizeof(mMethods[0]));
    return re;
}

/*
* System.loadLibrary("lib")时会调用
* 如果成功返回JNI版本, 失败返回-1
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);
    if (!registerNatives(env)) {//注册
        return -1;
    }
    //成功
    result = JNI_VERSION_1_6;
    return result;
}
/**动态注册结束*/