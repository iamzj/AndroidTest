package com.example.baidu.testgpuimagefilter;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class Main3Activity extends Activity  implements GestureDetector.OnGestureListener {

    /** Called when the activity is first created. */
    private int[] imageID = { R.drawable.cat_mugshot, R.drawable.cat_wanted,R.drawable.cat_mugshot,
            R.drawable.cat_wanted,R.drawable.cat_mugshot,R.drawable.cat_wanted,R.drawable.cat_mugshot,R.drawable.cat_wanted};

    private ViewFlipper viewFlipper = null;
    private GestureDetector gestureDetector = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        // 生成GestureDetector对象，用于检测手势事件
        gestureDetector = new GestureDetector(this);
        // 添加用于切换的图片
        for (int i = 0; i < imageID.length; i++)
        {
            // 定义一个ImageView对象
            ImageView image = new ImageView(this);
            image.setImageResource(imageID[i]);
            // 充满父控件
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            // 添加到viewFlipper中
            viewFlipper.addView(image, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        float dura = motionEvent.getX() - motionEvent1.getX();
        if (dura > 0) {
            // finger: from rigth to left
            ViewGroup.LayoutParams params = viewFlipper.getCurrentView().getLayoutParams();
            params.width = viewFlipper.getCurrentView().getWidth() - (int)dura;
            viewFlipper.getCurrentView().setLayoutParams(params);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float v, float v1) {
        //对手指滑动的距离进行了计算，如果滑动距离大于120像素，就做切换动作，否则不做任何切换动作。

        if (arg0.getX() - arg1.getX() > 120)
        {
            Log.d("test", "手指从右往左");
            // 添加动画
//            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
//                    R.anim.push_left_in));
//            this.viewFlipper.setInAnimation(null); // need align right and change width? (align left when animation is done)

//            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
//                    R.anim.push_left_out));
            this.viewFlipper.showNext();

            return true;
        }// 从右向左滑动
        else if (arg0.getX() - arg1.getX() < -120)
        {
            Log.d("test", "手指从左往右");
//            this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
//                    R.anim.push_right_in));
            this.viewFlipper.setInAnimation(null);
            this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                    R.anim.push_right_out));
            this.viewFlipper.showPrevious();
            return true;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return this.gestureDetector.onTouchEvent(event);
    }
}
