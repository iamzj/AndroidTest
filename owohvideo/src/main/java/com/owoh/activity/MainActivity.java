package com.owoh.activity;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.owoh.R;
import com.owoh.camera.CameraApiLow;
import com.owoh.interfaces.CameraApiInterface;
import com.owoh.natives.BaseGLNative;
import com.owoh.view.CameraButtonView;
import com.owoh.view.CameraSGLSurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    CameraSGLSurfaceView mSGlSurfaceView;
    private int mCameraId;
    private CameraApiInterface mCamera;
    private CameraButtonView takePicture;
    private final String LTag = "sen_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(null);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //初始化AssetManager
        BaseGLNative.initAssetManager(getAssets(), BaseGLNative.NATIVE_SENCE_CAMERA);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mSGlSurfaceView = (CameraSGLSurfaceView) findViewById(R.id.camera_glview);
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera = new CameraApiLow();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        //打开摄像头
        mCamera.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        mSGlSurfaceView.init(mCamera);

        takePicture = (CameraButtonView) findViewById(R.id.takePicture);

    }

    //对button需要做多次点击才行，要不会弹出多个
    //对camera setting
    public void settingView(View view) {
//
//        CameraSettingInfo info = mCamera.getCameraSettingInfo();
//        //获取当前的设置
//        CurrentCameInfo currentCameInfo = mCamera.getCurrentSettingInfo();
//        CameraInfoFragmentV2 dialog = new CameraInfoFragmentV2();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("CameraSettingInfo", info);
//        bundle.putSerializable("CurrentCameInfo", currentCameInfo);
//        dialog.setArguments(bundle);
//        dialog.show(getFragmentManager(), "CameraInfoFragment");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera != null) {

            mCamera.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        if (mSGlSurfaceView != null) {
            mSGlSurfaceView.destroyDrawingCache();
        }
        if (mCamera != null) {
            mCamera.releaseCamera();
            mCamera = null;
        }
        BaseGLNative.releaseNative(BaseGLNative.NATIVE_SENCE_PICTURE);
        super.onDestroy();

    }

    //点击屏幕对焦
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mCamera != null) {
//                mCamera.requestCameraFocus();
            }
        }
        return super.onTouchEvent(event);
    }


}
