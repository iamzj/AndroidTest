package com.owohvide.testconstanlayoutset;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/29 15:22
 */
public class ShutterView extends View {
    // 外圆背景颜色
    private int mOuterOvalBgColor;
    //内圆颜色
    private int mInerOvalBgColor;
    //内圆半径
    private int mInerOvalRadius;
    //外圆半径
    private int mOuterOvalRadius;
    //外圆宽度
    private int mOuterOvalWidth;

    // 控件填充背景
    private Paint mFillPaint;


    // 按钮动画
    private ValueAnimator mButtonAnim;

    //点击模式
    public static final int MODE_CLICK_SINGLE = 0x001;
    //长按模式
    public static final int MODE_CLICK_LONG = 0x002;
    private int mMeasuredWidth;
    private int mMeasuredHeight;


    @IntDef({MODE_CLICK_SINGLE, MODE_CLICK_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutterViewMode {

    }


    //闲置状态
    public static final int STATE_IDLE = 0x001;
    //开始录制状态
    public static final int STATE_START = 0x002;
    //录制结束
    public static final int STATE_END = 0x003;

    private int mCurrentState = STATE_IDLE;

    private int mCurrentMode = MODE_CLICK_SINGLE;


    @IntDef({STATE_IDLE, STATE_START, STATE_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutterViewState {

    }


    public ShutterView(Context context) {
        this(context, null);
    }

    public ShutterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShutterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 初始化属性
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ShutterView);
        mOuterOvalBgColor = array.getColor(R.styleable.ShutterView_outer_oval_color, ContextCompat.getColor(context, R.color.white));
        mInerOvalBgColor = array.getColor(R.styleable.ShutterView_iner_oval_color, ContextCompat.getColor(context, R.color.white));
        mInerOvalRadius = array.getDimensionPixelSize(R.styleable.ShutterView_iner_oval_radius, 10);
        mOuterOvalRadius = array.getDimensionPixelSize(R.styleable.ShutterView_outer_oval_radius, 10);
        mOuterOvalWidth = array.getDimensionPixelSize(R.styleable.ShutterView_outer_oval_width, 10);
        array.recycle();
        // 填充背景的Paint
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            // 按下时缩小
            case MotionEvent.ACTION_DOWN:

                break;
            // 松开手时，先复位按钮初始状态，如果开始录制，则放大，否则复位
            case MotionEvent.ACTION_UP:
                if(mCurrentMode ==MODE_CLICK_SINGLE){
                    //那么开启单击模式

                }else{

                }
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 开始缩放动画
     * @param start 起始值
     * @param end   结束值
     */
    private void startZoomAnim(float start, float end) {
        if (mButtonAnim == null || !mButtonAnim.isRunning()) {
            mButtonAnim = ValueAnimator.ofFloat(start, end).setDuration(0);
            mButtonAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    invalidate();
                }
            });
            mButtonAnim.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mCurrentState) {
            case STATE_IDLE:
                // 绘制外圆背景
                mFillPaint.setColor(mOuterOvalBgColor);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mOuterOvalRadius, mFillPaint);
                // 绘制内圆颜色
                mFillPaint.setColor(mInerOvalBgColor);
                canvas.drawCircle(mMeasuredWidth / 2, mMeasuredHeight / 2, mInerOvalRadius, mFillPaint);
                break;
            case STATE_START:

                break;
            case STATE_END:

                break;
        }

    }
}
