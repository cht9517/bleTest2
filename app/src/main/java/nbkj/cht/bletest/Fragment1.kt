package nbkj.cht.bletest;

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.content.DialogInterface
//import javax.swing.text.StyleConstants.setIcon
import android.app.AlertDialog;
import android.os.Environment
import android.widget.CheckBox
import android.os.Environment.getExternalStorageDirectory
import android.widget.Toast
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and


class Fragment1 : Fragment() {

    var view_msg: TextView? = null;
    var dld: DownLoadData? = null;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_fragment1, container, false)

        view_msg = view.findViewById<TextView>(R.id.view_CommMsg);



        var btn = view.findViewById<Button>(R.id.btn_Verify)
        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                MainActivity.tsb_comm.send_cmd(0x0f, 0x36, null);
            }
        })

        var btn_Diag = view.findViewById<Button>(R.id.btn_Diagnose)
        btn_Diag.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                MainActivity.tsb_comm.send_cmd(0x0f, 0x5b, null);
            }
        })

        dld = DownLoadData(activity);
        var btn_Download = view.findViewById<Button>(R.id.btn_Download)
        btn_Download.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dld!!.timer_download_start();
            }
        })

        var checkbox = view.findViewById<CheckBox>(R.id.checkBox1);

        var btn_ReRun = view.findViewById<Button>(R.id.btn_ReRun)
        btn_ReRun.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val time = ((System.currentTimeMillis()) / 1000).toInt();
                //view_msg.setText(time.toString())
                var buf = ByteArray(16, {it->0});
                buf[0] = ((time shr 24) and 0xff).toByte();
                buf[1] = ((time shr 16) and 0xff).toByte();
                buf[2] = ((time shr 8 ) and 0xff).toByte();
                buf[3] = ((time       ) and 0xff).toByte();
                if(checkbox.isChecked)
                    buf[8] = 1;

                MainActivity.tsb_comm.send_cmd(0x0f, 0xab, buf);
            }
        })

        // Inflate the layout for this fragment
        return view
    }


    fun rec_download_data(byteArray: ByteArray)
    {

        var rec_block_id = (byteArray[9].toInt() and 0xff shl 8) + (byteArray[10].toInt() and 0xff);
        var rec_block_total = (byteArray[11].toInt() and 0xff shl 8) + (byteArray[12].toInt() and 0xff);
        var rec_block_len = (byteArray[13].toInt() and 0xff shl 8) + (byteArray[14].toInt() and 0xff);


        view_msg!!.setText("block:(" + rec_block_id.toString() + ")接收到" + rec_block_len.toString() + "字节数据！");

        //写入文件
        if(rec_block_len > 0)
        {
            dld!!.write_log_data(byteArray, 15,  rec_block_len);
            dld!!.block_id_rec = rec_block_id;
        }

        //正确接收到数据帧主动判断并结束下载过程
        if((rec_block_len < 1024) || ((rec_block_id + 1 == rec_block_total) && (rec_block_len == 1024)) )
        {
            dld!!.timer_download_end();

            //view_msg!!.setText("数据下载完成，共收到" + rec_block_total.toString() + "块数据！");
            var str = "数据下载完成，共收到" + rec_block_total.toString() + "块数据！";
            Toast.makeText(activity, str, Toast.LENGTH_SHORT).show();
        }

    }

    fun rec_verify_OK(byteArray: ByteArray) {
        view_msg!!.setText("验证通信成功！");
    }

    fun rec_diag_info(byteArray: ByteArray)
    {
        var tool_num = CharArray(4);

        tool_num[0] = byteArray[40].toChar();
        tool_num[1] = byteArray[41].toChar();
        tool_num[2] = byteArray[42].toChar();
        tool_num[3] = byteArray[43].toChar();

        var str1 = "仪器编号：" + String(tool_num) + "\n";


        var cali_info = byteArray[44].toString() + byteArray[45].toString() + "-" + byteArray[46].toString() + "-" + byteArray[47].toString();

        var str2 = "刻度日期：" + cali_info + "\n" + "\n";


        //错误！！！koltlin不用分号，换行产生逻辑错误！
        /*var time_sec:Long = (byteArray[72].toLong().shl(24))
                        + (byteArray[73].toLong().shl(16))
                        + (byteArray[74].toLong().shl(8))
                        + (byteArray[75].toInt()) ;*/
        var time_sec = (byteArray[72].toInt() and 0xff shl 24)+ (byteArray[73].toInt() and 0xff shl 16)+ (byteArray[74].toInt() and 0xff shl 8) + (byteArray[75].toInt() and 0xff);

        var ts = Timestamp(time_sec * 1000L);

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var str3 = "仪器时间：" + sdf.format(ts) + "\n";

        var log_num = ((byteArray[76].toLong() and 0xff shl 24) + (byteArray[77].toLong() and 0xff shl 16) + (byteArray[78].toLong() and 0xff shl 8) + (byteArray[79].toLong() and 0xff)) / 32;

        var str4 = "存储记录：" + log_num.toString() + "条" + "\n";


        var Batt_volt = ((byteArray[84].toInt() and 0xff shl 8) + (byteArray[85].toInt() and 0xff)) / 100.0;

        var batt_level = 0;
        if(Batt_volt > 3.2)
        {
            if(Batt_volt > 4.1)
                batt_level = 100;
            else
                batt_level = ((Batt_volt - 3.2)/(4.1 - 3.2) * 100.0).toInt();
        }
        var str5 = "电池电量：" + batt_level.toString() + "%";

        view_msg!!.setText(str1 + str2 + str3 + "\n"+ str4 + str5);


    }
}
