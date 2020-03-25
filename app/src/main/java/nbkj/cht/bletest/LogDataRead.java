package nbkj.cht.bletest;

import android.graphics.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.*;


public class LogDataRead {
    private File file = null;
    private int num = 0;
    private double[] Distance_Vx = null;
    private double[] Distance_Vy = null;
    private double[] Distance_Hx = null;
    private double[] Distance_Hy = null;
    private double  Angle_V = 0;
    private double  Angle_H = 0;
    private int  Angle_H_base = 0;

    private int step = 1;

    public LogDataRead(File log_file) {
        file = log_file;

        num = 0;

        if(file.exists()) {

            num = (int) file.length() / 32;

            if(num > 0) {
                int[] dft_buf = new int[num];
                Distance_Vx = new double[num+1];
                Distance_Vy = new double[num+1];
                Distance_Vx[0] = 0;
                Distance_Vy[0] = 0;

                int[] azim_buf = new int[num];
                Distance_Hx = new double[num+1];
                Distance_Hy = new double[num+1];
                Distance_Hx[0] = 0;
                Distance_Hy[0] = 0;

                try {

                    FileInputStream fis = new FileInputStream(file);


                    byte[] buf = new byte[num * 32];
                    fis.read(buf, 0, num * 32);

                    for (int i = 0; i < num; i++) {

                        int dft = ((buf[i * 32 + 27] & 0xff) << 8) + (buf[i * 32 + 26] & 0xff);
                        int azim = ((buf[i * 32 + 29] & 0xff) << 8) + (buf[i * 32 + 28] & 0xff);
                        //azim = 2500;

                        dft_buf[i] = dft;
                        azim_buf[i] = azim;

                        double dft_rad = Math.toRadians(dft/10.0);
                        double azim_rad = Math.toRadians(azim/10.0);

                        Distance_Vx[i+1] = Distance_Vx[i] + Math.sin(dft_rad)*step;
                        Distance_Vy[i+1] = Distance_Vy[i] - Math.cos(dft_rad)*step;//绘图时正数在坐标轴的上面



                        Distance_Hx[i+1] = Distance_Hx[i] + Math.sin(dft_rad) * Math.cos(azim_rad) * step;
                        Distance_Hy[i+1] = Distance_Hy[i] + Math.sin(dft_rad) * Math.sin(azim_rad) * step;

                    }

                    Angle_V = Math.toDegrees(Math.atan2(Distance_Vy[num], Distance_Vx[num]));

                    Angle_H = Math.toDegrees(Math.atan2(Distance_Hy[num], Distance_Hx[num]));

                    Angle_H_base = (int)(Math.round(Angle_H/90.0) * 90);

                    //转换到显示坐标系
                    //西
                    //   北
                    //东
                    //绘图时正数在坐标轴的上面
                    for (int i = 0; i < num; i++) {

                        double dft_rad = Math.toRadians(dft_buf[i]/10.0);
                        double azim_rad = Math.toRadians(azim_buf[i]/10.0 - Angle_H_base);

                        Distance_Hx[i+1] = Distance_Hx[i] + Math.sin(dft_rad) * Math.cos(azim_rad) * step;
                        Distance_Hy[i+1] = Distance_Hy[i] - Math.sin(dft_rad) * Math.sin(azim_rad) * step;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public int get_num()
    {
        return num;
    }
    public double[] get_Vx()
    {
        return Distance_Vx;
    }
    public double[] get_Vy()
    {
        return Distance_Vy;
    }
    public double[] get_Hx()
    {
        return Distance_Hx;
    }

    public double[] get_Hy()
    {
        return Distance_Hy;
    }

    public double get_AngleV()
    {
        return Angle_V;
    }
    public double get_AngleH()
    {
        return Angle_H;
    }

    public int get_AngleHBase()
    {
        if(Angle_H_base < 0)
            return Angle_H_base + 360;
        else
            return Angle_H_base;
    }
}
