package nbkj.cht.bletest;
import android.os.Message;
import java.util.concurrent.*;


public class TSBComm  extends Thread{

    public static ArrayBlockingQueue<byte[]> rx_buf_queue = new ArrayBlockingQueue<byte[]>(256);

    private int com_id = 0x100;

    byte[] tx_buf = new byte[2048];


    private int[] CRC16CcittTable =
    {
        0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
        0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
        0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
        0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
        0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
        0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
        0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
        0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
        0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
        0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
        0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
        0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
        0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
        0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
        0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
        0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
        0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
        0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
        0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
        0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
        0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
        0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
        0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
        0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
        0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
        0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
        0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
        0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
        0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
        0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
        0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
        0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
    };

    private int CRC16(byte[] buf, int len)
    {
        int crc,i;

        crc = 0xffff;//暂存校验字节
        for(i = 0; i < len; i++)
        {
            crc = CRC16CcittTable[ ((crc >> 8 & 0xff) ^ (buf[i])) & 0xff ] ^ (crc << 8 & 0xff00);
        }
        return crc;
    }



    private byte rx_state = 0;
    private boolean rx_escape = false;
    private static byte[] rx_msg_buf = new byte[1050];
    private short rx_len = 0;
    private int rx_msg_len = 0;
    private int rx_com_tool_cmd_id = 0;

    private boolean escape_rx(byte dat)
    {
        if(rx_state == 0)
        {
            if (dat == (byte)(0xFB))
            {
                rx_state = 1;
                rx_len = 1;
                rx_escape = false;
            }
        }
        else
        {
            if (dat == (byte)(0xFB))
            {
                rx_state = 1;
                rx_len = 1;
                rx_escape = false;
            }
            else
            {
                if(dat == (byte)(0xFE))//帧结束
                {
                    rx_state = 2;
                    rx_msg_len = ((rx_msg_buf[1] & 0xff) << 8) + (rx_msg_buf[2] & 0xff);
                    if(rx_msg_len+5 == rx_len)//包中长度信息正确
                    {
                        byte[] rx_buf_content = new byte[rx_msg_len];
                        System.arraycopy(rx_msg_buf, 3, rx_buf_content, 0, rx_msg_len);
                        int crc = CRC16(rx_buf_content, rx_msg_len);

                        int rx_crc = ((rx_msg_buf[rx_len - 2] & 0xff) << 8) + (rx_msg_buf[rx_len - 1] & 0xff);
                        if(crc == rx_crc) {
                            rx_com_tool_cmd_id =  ((rx_msg_buf[5] & 0xff) << 16)
                                                + ((rx_msg_buf[6] & 0xff) << 8)
                                                +  (rx_msg_buf[7] & 0xff);
                            return true;
                        }
                    }

                }
                else if(dat == (byte)(0xF0))//转义标志
                {
                    rx_escape = true;
                }
                else
                {
                    if(rx_escape)//转义字符
                    {
                        if((dat == 0x00) || (dat == 0x0B) || (dat == 0x0E))
                        {
                            rx_escape = false;
                            rx_msg_buf[rx_len++] = (byte)(dat | 0xF0);
                        }
                        else
                            rx_state = 0;
                    }
                    else//正常接收
                        rx_msg_buf[rx_len++] = dat;

                    //超长判断
                    if(rx_len > 1050)
                        rx_state = 0;
                }

            }
        }

        return false;
    }

    @Override
    public void run(){
        byte[] rx_buf = null;

        while(true)
        {
            try {
                rx_buf = rx_buf_queue.take();

                for(int i=0; i<rx_buf.length; i++)
                {
                    if (escape_rx(rx_buf[i]))//成功接收到一帧
                    {
                        Message msg = MainActivity.handler.obtainMessage(0xFBFE);
                        msg.obj = rx_msg_buf;
                        msg.arg1 = rx_com_tool_cmd_id;
                        msg.arg2 = rx_msg_buf[4];
                        MainActivity.handler.sendMessage(msg);
                    }
                }
                //Thread.sleep(100);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void rx_buf_put(byte[] dat_buf) {
        rx_buf_queue.offer(dat_buf);
    }





    public int send_cmd(int tool_id, int cmd_id, byte[] para_buf)
    {
        byte[] tx_msg_buf = new byte[1050];

        int tx_msg_len;

        if(para_buf != null)
            tx_msg_len = para_buf.length + 5;
        else
            tx_msg_len = 5;

        tx_msg_buf[0] = (byte)((tx_msg_len >> 8) & 0xff);
        tx_msg_buf[1] = (byte)( tx_msg_len & 0xff);
        com_id = 0;
        tx_msg_buf[2] = (byte)((com_id >> 8) & 0xff);
        tx_msg_buf[3] = (byte)( com_id++ & 0xff);
        tx_msg_buf[4] = (byte)( tool_id & 0xff);
        tx_msg_buf[5] = (byte)( cmd_id & 0xff);
        tx_msg_buf[6] = 0;

        //拷贝参数
        if(para_buf != null) {
            System.arraycopy(para_buf, 0, tx_msg_buf, 7, para_buf.length);
        }

        //计算CRC
        byte[] tx_buf_content = new byte[tx_msg_len];
        System.arraycopy(tx_msg_buf, 2, tx_buf_content, 0, tx_msg_len);
        int tx_crc = CRC16(tx_buf_content, tx_msg_len);
        tx_msg_buf[tx_msg_len + 2] = (byte)( (tx_crc >> 8) & 0xff);
        tx_msg_buf[tx_msg_len + 3] = (byte)(  tx_crc & 0xff);

        //数据转义
        tx_buf[0] = (byte)(0xFB);
        int pos = 1;
        for(int i=0; i<(tx_msg_len+4); i++)
        {
            if((tx_msg_buf[i] == 0xF0) || (tx_msg_buf[i] == 0xFB) || (tx_msg_buf[i] == 0xFE)) {
                tx_buf[pos++] = (byte)(0xF0);
                tx_buf[pos++] = (byte)(tx_msg_buf[i] & 0x0F);
            }
            else
                tx_buf[pos++] = tx_msg_buf[i];
        }
        tx_buf[pos++] = (byte)(0xFE);

        //数据发送
        byte[] tx_buf_real = new byte[pos];
        System.arraycopy(tx_buf, 0, tx_buf_real, 0, pos);
        MainActivity.get_ble_op().write_cmd(tx_buf_real);

        int val = ((com_id & 0xffff) << 16) + ((tool_id & 0xff) << 8) + (cmd_id & 0xff);
        return val;
    }

}
