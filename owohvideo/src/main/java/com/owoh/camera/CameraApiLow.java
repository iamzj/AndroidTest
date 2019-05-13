package com.owoh.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.owoh.interfaces.CameraApiInterface;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Harrison 唐广森
 * @description: 旧版本的Camera 实现
 * @date :2019/3/16 11:22
 */
public class CameraApiLow implements CameraApiInterface {
    //想要的尺寸。
    private int mDesiredHeight = 1280;
    private int mDesiredWidth = 720;
    private boolean mAutoFocus;
    public CameraSize mPreviewSize;
    public CameraSize mPicSize;

    private int mCameraId;

    private Camera mCamera;

    private Camera.Parameters mCameraParameters;
    /*
     * 当前相机的高宽比
     */
    private AspectRatio mDesiredAspectRatio;

    @Override
    public boolean openCamera(int cameraId) {
       /*
            预览的尺寸和照片的尺寸
        */
        final CameraSize.ISizeMap mPreviewSizes = new CameraSize.ISizeMap();
        final CameraSize.ISizeMap mPictureSizes = new CameraSize.ISizeMap();
        if (mCamera != null) {
            releaseCamera();
        }
        mCameraId = cameraId;
        mCamera = Camera.open(mCameraId);
        if (mCamera != null) {
            mCameraParameters = mCamera.getParameters();

            mPreviewSizes.clear();
            //先收集参数.因为每个手机能够得到的摄像头参数都不一致。所以将可能的尺寸都得到。
            for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
                mPreviewSizes.add(new CameraSize(size.width, size.height));
            }

            mPictureSizes.clear();
            for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
                mPictureSizes.add(new CameraSize(size.width, size.height));
            }
            //挑选出最需要的参数
            adJustParametersByAspectRatio2(mPreviewSizes, mPictureSizes);
            Log.e("Harrison","摄像头打开成功");
            return true;
        }
        Log.e("Harrison","摄像头打开失败");
        return false;
    }

    @Override
    public void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            return true;
        }
        return false;
    }

    @Override
    public void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public CameraApiLow() {
        mDesiredHeight = 1280;
        mDesiredWidth = 720;
        //创建默认的比例.因为后置摄像头的比例，默认的情况下，都是旋转了270
        mDesiredAspectRatio = AspectRatio.of(mDesiredWidth, mDesiredHeight).inverse();
    }


    private void adJustParametersByAspectRatio2(CameraSize.ISizeMap previewSizes, CameraSize.ISizeMap pictureSizes) {
        //得到当前预期比例的size
        SortedSet<CameraSize> sizes = previewSizes.sizes(mDesiredAspectRatio);
        if (sizes == null) {  //表示不支持.
            return;
        }
        for (CameraSize next : sizes) {
            if (next.getWidth() >= 720 && next.getHeight() >= 720) {
                mPreviewSize = next;
                break;
            }
        }
        if (mPreviewSize == null) {
            mPreviewSize = sizes.last();
        }

//
        for (CameraSize next : pictureSizes.sizes(mDesiredAspectRatio)) {
            if (next.getWidth() >= 720 && next.getHeight() >= 720) {
                mPicSize = next;
                break;
            }
        }
        if (mPicSize == null) {
            mPicSize = pictureSizes.sizes(mDesiredAspectRatio).last();
        }

        mCameraParameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mCameraParameters.setPictureSize(mPicSize.getWidth(), mPicSize.getHeight());

        mPreviewSize = mPreviewSize.inverse();
        mPicSize = mPicSize.inverse();

//        Log.d(TAG, "preview=" + mPreviewSize);
//        Log.d(TAG, "mPicSize=" + mPicSize);
        //设置对角和闪光灯
        setAutoFocusInternal(mAutoFocus);
        //先不设置闪光灯
//        mCameraParameters.setFlashMode("FLASH_MODE_OFF");

        //设置到camera中
//        mCameraParameters.setRotation(90);
        mCamera.setParameters(mCameraParameters);
//        mCamera.setDisplayOrientation(90);
//        setCameraDisplayOrientation();
    }

    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
//        if (isCameraOpened()) {
        final List<String> modes = mCameraParameters.getSupportedFocusModes();
        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mCameraParameters.setFocusMode(modes.get(0));
        }
        return true;
//        } else {
//            return false;
//        }
    }


    @Override
    public CameraSize getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public CameraSize getPictureSize() {
        return mPicSize;
    }

    @Override
    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        if(mCamera!=null){
            mCamera.setPreviewCallback(previewCallback);
        }
    }


    //判断是否自动对焦
    private boolean getAutoFocus() {
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }
}
