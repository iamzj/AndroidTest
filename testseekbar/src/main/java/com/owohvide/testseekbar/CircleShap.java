package com.owohvide.testseekbar;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.shapes.Shape;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/19 16:48
 */
public class CircleShap extends Shape {

    @Override
    public void draw(Canvas canvas, Paint paint) {
        int radius = (int) (Math.min(getWidth(), getHeight()) / 2);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
    }

    @Override
    protected void onResize(float width, float height) {

    }




}
