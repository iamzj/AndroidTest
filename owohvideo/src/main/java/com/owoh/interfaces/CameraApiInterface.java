package com.owoh.interfaces;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.owoh.camera.CameraSize;

/**
 * @author Harrison 唐广森
 * @description: 对camera 新旧版本 的一些行为规范
 * @date :2019/3/16 11:09
 */
public interface CameraApiInterface {
    /**
     * 打开摄像头
     *
     * @param cameraId
     * @return
     */
    boolean openCamera(int cameraId);

    /**
     * 关闭,并释放资源
     */
    void releaseCamera();

    /**
     * 开启预览
     */
    boolean startPreview();

    /**
     * 停止预览
     */
    void stopPreview();


    /**
     * 设置预览画面
     *
     * @param surfaceTexture
     */
    void setSurfaceTexture(SurfaceTexture surfaceTexture);

    /**
     * 获取预览画面的大小
     * @return
     */
    CameraSize getPreviewSize();

    /**
     * 获取图片的大小
     * @return
     */
    CameraSize getPictureSize();


    void setPreviewCallback( Camera.PreviewCallback previewCallback);
}
