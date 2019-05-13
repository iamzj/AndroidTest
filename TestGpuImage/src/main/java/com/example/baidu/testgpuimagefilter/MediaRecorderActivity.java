package com.example.baidu.testgpuimagefilter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by baidu on 2017/3/9.
 */

public class MediaRecorderActivity extends Activity implements View.OnClickListener{
    public static final String TAG = "MediaRecorderActivity";
    // 程序中的两个按钮
    Button record , stop;
    // 系统的视频文件
    File videoFile ;
    MediaRecorder mRecorder;

    Camera mCamera;
    // 显示视频预览的SurfaceView
    SurfaceView sView;
    // 记录是否正在进行录制
    private boolean isRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // 去掉标题栏 ,必须放在setContentView之前
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_media_recorder);
        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        // 获取程序界面中的两个按钮
        record = (Button) findViewById(R.id.record);
        stop = (Button) findViewById(R.id.stop);
        // 让stop按钮不可用。
        stop.setEnabled(false);
        // 为两个按钮的单击事件绑定监听器
        record.setOnClickListener(this);
        stop.setOnClickListener(this);
        // 获取程序界面中的SurfaceView
        sView = (SurfaceView) this.findViewById(R.id.sView);
        // 设置分辨率
        sView.getHolder().setFixedSize(1280, 720);
        // 设置该组件让屏幕不会自动关闭
        sView.getHolder().setKeepScreenOn(true);

        getSingInfo();
    }

    @Override
    public void onClick(View source)
    {
        switch (source.getId())
        {
            // 单击录制按钮
            case R.id.record:
                if (!Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED))
                {
                    Toast.makeText(MediaRecorderActivity.this
                            , "SD卡不存在，请插入SD卡！"
                            , Toast.LENGTH_SHORT).show();
                    return;
                }
                try
                {
                    // 创建保存录制视频的视频文件
                    videoFile = new File(Environment
                            .getExternalStorageDirectory()
                            .getCanonicalFile() + "/testrecordevideo" + System.currentTimeMillis() + ".mp4");
                    // 创建MediaPlayer对象
                    int cameraId = 0;
                    mCamera = Camera.open(cameraId);
                    setCameraRotation(cameraId);
//                    mCamera.setDisplayOrientation(90);
                    mCamera.getParameters().setFocusMode(FOCUS_MODE_CONTINUOUS_VIDEO);
                    final List<Camera.Size> mSupportedVideoSizes = getSupportedVideoSizes(mCamera);
                    for (Camera.Size str : mSupportedVideoSizes)
                        Log.e(TAG, "mSupportedVideoSizes "+str.width + ":" + str.height + " ... "
                                + ((float) str.width / str.height));

                    mRecorder = new MediaRecorder();
//                    mCamera.lock();
                    mCamera.unlock();
//                    mRecorder.reset();
//                    mCamera.setDisplayOrientation(90);

                    mRecorder.setCamera(mCamera);
                    // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    // 设置从摄像头采集图像
                    mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mRecorder.setProfile(CamcorderProfile
                            .get(CamcorderProfile.QUALITY_720P));
                    // 设置视频文件的输出格式
                    // 必须在设置声音编码格式、图像编码格式之前设置
//                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 与profile设置冲突
                    // 设置声音编码的格式
//                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // 与profile设置冲突
                    // 设置图像编码的格式
//                    mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // 与profile设置冲突
                    mRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);

                    mRecorder.setVideoSize(1280, 720);
                    // 每秒 4帧
//                    mRecorder.setVideoFrameRate(20); // 对于三星note3等一些手机，
                    mRecorder.setOrientationHint(mRotate);
                    mRecorder.setOutputFile(videoFile.getAbsolutePath());
                    // 指定使用SurfaceView来预览视频
                    mRecorder.setPreviewDisplay(sView.getHolder().getSurface());  //①
                    mRecorder.prepare();
                    // 开始录制
                    mRecorder.start();
                    System.out.println("---recording---");
                    // 让record按钮不可用。
                    record.setEnabled(false);
                    // 让stop按钮可用。
                    stop.setEnabled(true);
                    isRecording = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            // 单击停止按钮
            case R.id.stop:
                // 如果正在进行录制
                if (isRecording)
                {
                    // 停止录制
                    mRecorder.stop();
                    // 释放资源
                    mRecorder.release();
                    mRecorder = null;

                    mCamera.release();
                    mCamera = null;
                    // 让record按钮可用。
                    record.setEnabled(true);
                    // 让stop按钮不可用。
                    stop.setEnabled(false);
                }
                break;
        }
    }

    int mRotate;

    public void setCameraRotation(int cameraId) {
        try {

            Camera.CameraInfo camInfo = new Camera.CameraInfo();

            if (cameraId == 0)
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
            else
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            // ...

            Camera.Parameters parameters = mCamera.getParameters();


            int rotation = ((Activity)this).getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }
            int displayRotation;
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360;
                displayRotation = (360 - displayRotation) % 360; // compensate
                // the
                // mirror
            } else { // back-facing
                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
            }

            mCamera.setDisplayOrientation(displayRotation);

            mRotate = 0;
            if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mRotate = (360 + cameraRotationOffset + degrees) % 360;
            } else {
                mRotate = (360 + cameraRotationOffset - degrees) % 360;
            }

            parameters.set("orientation", "portrait");
            parameters.setRotation(mRotate);
            mCamera.setParameters(parameters);

        } catch (Exception e) {

        }
    }

    public List<Camera.Size> getSupportedVideoSizes(Camera camera) {
        if (camera.getParameters().getSupportedVideoSizes() != null) {
            Log.d(TAG, "has multi getSupportedVideoSizes");
            return camera.getParameters().getSupportedVideoSizes();
        } else {
            Log.d(TAG, "has no multi getSupportedVideoSizes, use getSupportedPreviewSizes");
            // Video sizes may be null, which indicates that all the supported
            // preview sizes are supported for video recording.
            return camera.getParameters().getSupportedPreviewSizes();
        }
    }

    public void getSingInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            parseSignature(sign.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory
                    .getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory
                    .generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
            String signNumber = cert.getSerialNumber().toString();
            Log.d(TAG, "pubKey = " + pubKey + ";signNumber=" + signNumber);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecorder != null) {
            // 停止录制
//            mRecorder.
            mRecorder.reset();
            // 释放资源
            mRecorder.release();
            mRecorder = null;
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        super.onDestroy();
    }
}
