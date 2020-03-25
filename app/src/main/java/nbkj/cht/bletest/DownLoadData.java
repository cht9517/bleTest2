package nbkj.cht.bletest;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;


import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;


public class DownLoadData {

    private boolean DownLoad_Start = false;
    private Timer timer_dl = null;
    private TimerTask timer_task = null;
    private int block_id = 0;
    public  int block_id_rec = -1;
    private int comm_err_cnt = 0;

    File file_Dir = null;
    File file_log = null;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;


    private Activity this_activity = null;

    DownLoadData(Activity act)
    {
        this_activity = act;


        //文件存储目录
        String dir_str = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BLE-MWD";
        file_Dir = new File(dir_str);
        if (!file_Dir.exists()) {
            file_Dir.mkdirs();
        }
    }


    private void send_dl_cmd()
    {
        byte[] buf = new byte[3];
        buf[0] = 0;
        buf[1] = (byte) ((block_id >> 8) & 0xff);
        buf[2] = (byte) (block_id & 0xff);

        MainActivity.tsb_comm.send_cmd(0x0f, 0xcd, buf);
    }

    public void timer_download_end()
    {
        DownLoad_Start = false;

        if(timer_dl != null)
            timer_dl.cancel();
        if(timer_task != null)
            timer_task.cancel();

        timer_dl = null;
        timer_task = null;

        //关闭文件流
        try {
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void timer_download_start() {
        DownLoad_Start = true;

        block_id = -1;
        block_id_rec = -1;

        //文件名
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String filestr = now.format(formatter) + ".dld";

        File file_log = new File(file_Dir, filestr);

        //创建文件、流
        try {
            file_log.createNewFile();

            fos = new FileOutputStream(file_log);
            bos = new BufferedOutputStream(fos);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        //创建定时任务
        if (timer_dl == null)
            timer_dl = new Timer();

        if (timer_task == null) {
            timer_task = new TimerTask() {
                @Override
                public void run() {
                    if(block_id == block_id_rec) {

                        comm_err_cnt = 0;
                        block_id++;

                        send_dl_cmd();
                    }
                    else
                    {
                        comm_err_cnt++;
                        if(comm_err_cnt >= 3)
                        {
                            timer_download_end();
                        }
                        else
                        {
                            send_dl_cmd();
                        }
                    }
                }
            };
        }
        timer_dl.schedule(timer_task, 100, 1500);
    }

    public void write_log_data(byte[] buf, int offset, int len)
    {
        try
        {
            if((len + offset)<= buf.length)
                bos.write(buf, offset, len);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        //Log.e(TAG, "isExternalStorageWritable: " + state);
        return false;
    }


    /*
    public String writeStringToFile(String str) {
        if (!isExternalStorageWritable()) {
            return "ExternalStorageWritable Fault!";
        }
        ActivityCompat.requestPermissions(this_activity, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);



        try {
            file.createNewFile();

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            bos.write(str.getBytes());

            bos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dirstr + filestr;
    }*/

}
