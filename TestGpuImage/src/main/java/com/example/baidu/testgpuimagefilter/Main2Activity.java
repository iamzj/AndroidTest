package com.example.baidu.testgpuimagefilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;


/**
 *
 * 给图片打个洞（样例中打了两个半圆洞）
 *
 * 另外，如果控件浮在其他控件上面，这个洞是透明的，看到的是底层控件的内容。
 *
 * cited: http://www.techrepublic.com/article/punch-a-hole-in-a-bitmap-by-using-androids-porter-duff-xfer/
 *
 * related: http://blog.csdn.net/t12x3456/article/details/10432935
 *
 */
public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ImageView iv = (ImageView) findViewById(R.id.img);
//        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.mug_shot);
        Bitmap foreground = BitmapFactory.decodeResource(getResources(), R.drawable.cat_wanted);
        foreground = punchAHoleInABitmap(foreground);
        iv.setImageBitmap(foreground);

    }

    private Bitmap punchAHoleInABitmap(Bitmap foreground) {
        Bitmap bitmap = Bitmap.createBitmap(foreground.getWidth(), foreground.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(foreground, 0, 0, paint);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        int width = foreground.getWidth();
        int height = foreground.getHeight();


        float radius = (float)(width * .1);
        float x = (float) (0);
        float y = (float)  ((height*.75));
        canvas.drawCircle(x, y, radius, paint);

        // draw another half circle
        x = width;

        canvas.drawCircle(x, y, radius, paint);
        return bitmap;
    }

//    private Point getScreenSize() {
//        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        Display display = window.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        return size;
//    }

}
