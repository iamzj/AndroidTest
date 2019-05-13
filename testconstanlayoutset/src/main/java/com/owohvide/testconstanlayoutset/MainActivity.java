package com.owohvide.testconstanlayoutset;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;
    int progress = 0;
    Handler handler =new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                progress+= 200;
            //    shutterButton.setProgress(progress);
                handler.sendEmptyMessageDelayed(1,2000);
            }
        }
    };
    private ShutterView shutterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        constraintLayout = findViewById(R.id.rootView);

        shutterButton = findViewById(R.id.btShutterView);
//      shutterButton.setOnShutterListener(new ShutterButton.OnShutterListener() {
//          @Override
//          public void onStartRecord() {
//              Log.e("Harrison","onStartRecord");
//              // 编码器已经进入录制状态，则快门按钮可用
//              shutterButton.setEnableEncoder(true);
//          }
//
//          @Override
//          public void onStopRecord() {
//              Log.e("Harrison","onStopRecord");
//          }
//
//          @Override
//          public void onProgressOver() {
//              Log.e("Harrison","onProgressOver");
//          }
//      });
//      handler.postDelayed(new Runnable() {
//          @Override
//          public void run() {
//              shutterButton.deleteSplitView();
//              shutterButton.setProgressMax(10*1000);
//              handler.sendEmptyMessageDelayed(1,2000);
//          }
//      }, 5000);

//        findViewById(R.id.test2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone(constraintLayout);
//                constraintSet.setMargin(R.id.layout_aspect, 1, 60);
//                constraintSet.setMargin(R.id.layout_aspect, 2, 60);
//                constraintSet.setMargin(R.id.layout_aspect, 3, 60);
//                constraintSet.setDimensionRatio(R.id.layout_aspect, "h,1:1.3");
//                constraintSet.applyTo(constraintLayout);
//            }
//        });
//
//        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConstraintSet constraintSet = new ConstraintSet();
//                constraintSet.clone(constraintLayout);
//                constraintSet.setMargin(R.id.layout_aspect, 1, 60);
//                constraintSet.setMargin(R.id.layout_aspect, 2, 60);
//                constraintSet.setMargin(R.id.layout_aspect, 3, 60);
//                constraintSet.setDimensionRatio(R.id.layout_aspect, "h,1:1.3");
//                constraintSet.applyTo(constraintLayout);
//            }
//        });

    }
}
