package nbkj.cht.bletest;


import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class MyGraph extends View {

    private Paint paint;
    private Rect mRect;
    private Random mRand = new Random();
    public Point[] points = new Point[500];


    public MyGraph(Context context) {
        super(context);

        init();
    }

    public MyGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);// 必须设置抗锯齿，这样绘制出来的画面才会是平滑的
        paint.setColor(Color.RED);// 设置画笔的颜色为红色
        paint.setStyle(Paint.Style.STROKE);// 设置画笔的样式
        paint.setStrokeWidth(2);// 设置画笔的粗细
    }

    public void clear()
    {
        for(int i=0; i<500; i++) {
            points[i] = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getMeasuredWidth();
        float height = getMeasuredHeight();

        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(2);
        canvas.drawRect(0, 0, width, height, paint);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        paint.setStrokeWidth(1);
        paint.setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));

        //画横格线
        canvas.drawLine(0, height/2, width, height/2, paint);
        for(int i=100; i<height/2; i+=100) {
            canvas.drawLine(0, height / 2 - i, width, height / 2 - i, paint);
            canvas.drawLine(0, height / 2 + i, width, height / 2 + i, paint);
        }

        //画竖格线
        for(int i=200; i<width; i += 200) {
            canvas.drawLine(i, 0, i, height, paint);
        }


        paint.setPathEffect(null);
        paint.setTextSize(24);

        //纵坐标
        for(int i=100; i<height/2; i+=100) {
            canvas.drawText(String.valueOf(-i/10), 0, height/2 + i, paint);
            canvas.drawText(String.valueOf(i/10), 0, height/2 - i, paint);
        }

        //横坐标
        for(int i=200; i<width; i+=200) {
            canvas.drawText(String.valueOf(i/10), i, height/2, paint);
        }

        //实际数据描点连线
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);

        paint.setStyle(Paint.Style.STROKE);// 设置画笔的样式
        for(int i=0; i<points.length-1; i++)
        {
            if((points[i] != null)&&(points[i+1] != null))
                canvas.drawLine(points[i].x, height/2-points[i].y, points[i+1].x, height/2-points[i+1].y, paint);
        }
    }

}