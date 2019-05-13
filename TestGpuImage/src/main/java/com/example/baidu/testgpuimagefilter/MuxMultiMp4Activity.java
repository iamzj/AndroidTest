package com.example.baidu.testgpuimagefilter;

import android.os.Bundle;
import android.app.Activity;
import android.widget.Toast;

public class MuxMultiMp4Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mux_multi_mp4);

        MediaMuxerTest mmt = new MediaMuxerTest(this);
        try {
            Toast.makeText(this, "Concat Start!", Toast.LENGTH_SHORT).show();
            mmt.testVideoAudio();
            Toast.makeText(this, "Concat Over!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
