package com.android.guoheng.decodeencodemp4;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String inputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/test.mp4";
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/out.mp4";

        EncodeDecodeSurface test = new EncodeDecodeSurface();
        test.setVideoPath(inputPath, outputPath);
        try {
            test.testEncodeDecodeSurface();
        } catch (Throwable a) {
            a.printStackTrace();
        }

    }
}
