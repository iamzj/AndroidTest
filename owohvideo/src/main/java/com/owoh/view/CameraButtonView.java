package com.owoh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.owoh.R;

/**
 * Created by Administrator on 2017/7/24.
 * 将Camera 所有操作的功能，就集在这里
 * VideoRecoder 在子线程 ，还有点问题
 * VideoRecoder 就是没在子线程
 */

public class CameraButtonView extends View {
    private Paint mPaint;
    private Path bigCirclePath;
    private Path smollPath; //它可以说圆形，也可以是正方形，看状态
    //当拍完照，或者
    private final static int STATE_IDLE = 0; //空闲状态
    private final static int STATE_TAKE_PICTURE = 1; //拍照片状态，完毕之后都会变为STATE_IDLE
    private final static int STATE_RECODING_VIDEO = 2; //正在拍摄状态，完毕之后都会变为STATE_IDLE
    private int cameraState = STATE_IDLE;
    private int centerX;
    private int centerY;
    private int changeOffset = 0;
    private int ruduis;
    public final static int MODE_PICTRUE = 0;
    public final static int MODE_VIDEO = 1;
    private int mCurrentMode = MODE_PICTRUE;

    public void handleClick() {
        //先分camera 的状态
        switch (cameraState) {
            case STATE_IDLE:

                break;
            case STATE_TAKE_PICTURE:
                cameraState = STATE_IDLE;
                break;
            case STATE_RECODING_VIDEO:
                cameraState = STATE_IDLE;
                Toast.makeText(getContext(), "停止录制", Toast.LENGTH_SHORT).show();
                break;
        }
        postInvalidate();
    }


    public CameraButtonView(Context context) {
        this(context, null);
    }

    public CameraButtonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bigCirclePath = new Path();
        centerX = w / 2;
        centerY = h / 2;
        ruduis = w / 2 - 16;

        smollPath = new Path();


        bigCirclePath.reset();
        smollPath.reset();
        bigCirclePath.addCircle(centerX, centerY, ruduis + changeOffset, Path.Direction.CW);
        smollPath.addCircle(centerX, centerY, ruduis - 12 + changeOffset, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //先分camera 的状态
        switch (cameraState) {
            case STATE_IDLE:
                //然后判断是那种模式
                if (mCurrentMode == MODE_PICTRUE) {
                    drawBigCircle(canvas, Color.WHITE);
                    //中间的画圆，白色
                    drawSmallPath(canvas, Color.WHITE, true);
                } else {
                    drawBigCircle(canvas, Color.WHITE);
                    //中间的画圆，紫色
                    drawSmallPath(canvas, ContextCompat.getColor(getContext(), R.color.colorPrimary), true);
                }
                break;
            case STATE_TAKE_PICTURE:
                break;
            case STATE_RECODING_VIDEO:
                //当前必定是video 模式
                drawBigCircle(canvas, Color.WHITE);
                //中间的画正方形，紫色
                drawSmallPath(canvas, ContextCompat.getColor(getContext(), R.color.colorPrimary), false);
                break;
        }

    }

    private void drawBigCircle(Canvas canvas, int color) {
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        canvas.drawPath(bigCirclePath, mPaint);
    }

    private void drawSmallPath(Canvas canvas, int color, boolean isCircle) {
//        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mPaint.setColor(color);
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        smollPath.reset();
        if (isCircle) {
            smollPath.addCircle(centerX, centerY, ruduis - 12 + changeOffset, Path.Direction.CW);
        } else {
            //这些数据应该放在自定义属性了做，我这里就不做了，随便先来点数
            smollPath.addRect(centerX - 30, centerY - 30, centerX + 30, centerY + 30, Path.Direction.CW);
        }
        canvas.drawPath(smollPath, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //currentState = DOWN_STATE;
                changeOffset = -4;
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //  currentState = DEF_STATE;
                changeOffset = 0;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }


    public void setChangeStuats(int mode) {
        if (mode != mCurrentMode) {
            //切换状态了，就先停止录制
            if (cameraState == STATE_RECODING_VIDEO) {
                Toast.makeText(getContext(), "停止录制", Toast.LENGTH_SHORT).show();
            }
            if (mode == MODE_PICTRUE || mode == MODE_VIDEO) {
                mCurrentMode = mode;
                postInvalidate();
            }

        }
    }

    //提供给外部获取button 模式对应点击
    public int getCameraMode() {
        return mCurrentMode;
    }


}
