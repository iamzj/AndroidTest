package com.owohvide.imageblur;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.test);
       long start = System.currentTimeMillis();
        ImageBlur.blurBitmap(bitmap,20);
        Log.e("Harrison","end"+(System.currentTimeMillis()-start));
        findViewById(R.id.rootView).setBackground(new BitmapDrawable(bitmap));
        new Thread(){
            @Override
            public void run() {
                super.run();
//                ImageBlur.blurBitmap(bitmap,25);
            }
        }.start();
    }
}
