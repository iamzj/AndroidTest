package com.owohvide.gpuimagedemo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GPUImageView gpuImageView = findViewById(R.id.gpuimageview);
        Resources res = MainActivity.this.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.test);
        gpuImageView.setImage(bmp); // this loads image on the current thread, should be run in a thread
        gpuImageView.setFilter(new GPUImageThresholdEdgeDetectionFilter());
        Log.e("Harrison","onCreate");



    }
}
