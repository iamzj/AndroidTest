package com.owohvide.testseekbar;

import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SeekBar seekBarBGM = findViewById(R.id.seekBar);
        seekBarBGM.setMax(100);
        seekBarBGM.setProgress(30);
        seekBarBGM.setEnabled(false);
        ShapeDrawable thumb = new ShapeDrawable(new CircleShap());
        thumb.getPaint().setColor(ContextCompat.getColor(this,android.R.color.white));
        thumb.setIntrinsicHeight(80);
        thumb.setIntrinsicWidth(40);
        seekBarBGM.setThumb(thumb);

    }
}
