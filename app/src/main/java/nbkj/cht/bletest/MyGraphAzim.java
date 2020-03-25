package nbkj.cht.bletest;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import nbkj.cht.bletest.MyGraph;

public class MyGraphAzim extends MyGraph {

    public int Base_Angle = 0;

    public MyGraphAzim(Context context) {
        super(context);
    }

    public MyGraphAzim(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getMeasuredWidth();
        float height = getMeasuredHeight();

        Paint paint = new Paint();



        double centerX = width - 80;
        double centerY = 80;
        int base = 20;

        double sinV = Math.sin(Math.toRadians(Base_Angle));
        double cosV = Math.cos(Math.toRadians(Base_Angle));
        float NPoleX = (float)(centerX + base*1.732*cosV);
        float NPoleY = (float)(centerY + base*1.732*sinV);
        float TextNX = (float)(centerX + base*2.6*cosV - 12*0.7);
        float TextNY = (float)(centerY + base*2.6*sinV + 12*0.7);

        float EPoleX = (float)(centerX - base*sinV);
        float EPoleY = (float)(centerY + base*cosV);
        float TextEX = (float)(centerX - base*1.9*sinV - 12*0.7);
        float TextEY = (float)(centerY + base*1.9*cosV + 12*0.7);

        float SPoleX = (float)(centerX - base*1.732*cosV);
        float SPoleY = (float)(centerY - base*1.732*sinV);

        float WPoleX = (float)(centerX + base*sinV);
        float WPoleY = (float)(centerY - base*cosV);


        paint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        paint.setStrokeWidth(4);

        //画N极半边
        paint.setColor(Color.RED);

        path.moveTo(NPoleX, NPoleY);
        path.lineTo(EPoleX, EPoleY);
        path.lineTo(WPoleX, WPoleY);
        path.lineTo(NPoleX, NPoleY);
        canvas.drawPath(path, paint);

        //画N极半边
        paint.setColor(Color.GRAY);
        Path path2 = new Path();

        path2.moveTo(SPoleX, SPoleY);
        path2.lineTo(EPoleX, EPoleY);
        path2.lineTo(WPoleX, WPoleY);
        path2.lineTo(SPoleX, SPoleY);
        canvas.drawPath(path2, paint);

        //添加N、E文字
        paint.setStyle(Paint.Style.STROKE);// 设置画笔的样式
        paint.setStrokeWidth(2);
        paint.setTextSize(24);
        canvas.drawText("N", TextNX, TextNY, paint);
        canvas.drawText("E", TextEX, TextEY, paint);
    }
}
