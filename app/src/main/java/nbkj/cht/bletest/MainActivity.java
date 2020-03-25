package nbkj.cht.bletest;



import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.graphics.Color;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.Toast;

import com.clj.fastble.BleManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static Fragment0 f0 = new Fragment0();
    public static Fragment1 f1 = new Fragment1();
    public static Fragment2 f2 = new Fragment2();
    public static Fragment3 f3 = new Fragment3();
    public static Fragment4 f4 = new Fragment4();

    private int ble_status = 0;
    private String uuid_str = "";

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 3;
    public  TextView mtextViewLog;
    /*
    public  TextView mTextMessage2;
    private Button mbtn;
    private Button mbtn2;*/
    private static int cnt = 0;
    public static TSBComm tsb_comm = new TSBComm();
    public static BleOperate ble_ops = new BleOperate();
    public static FragmentManager fragmentManager = null;


    public static BleOperate get_ble_op()
    {
        return ble_ops;
    }

    public static void cmd_send()
    {
        tsb_comm.send_cmd(0x0f, 0xdc, null);
    }

    private static void tsb_app_fun(int ID, byte[] buf)
    {
        cnt++;
        if(ID == 0x0FDC04)
            f2.update_mDatas(buf);
        else if(ID == 0x0FCD04)
            f1.rec_download_data(buf);
        else if(ID == 0x0F5B04)
            f1.rec_diag_info(buf);
        else if(ID == 0x0F3604)
            f1.rec_verify_OK(buf);
        //mTextMessage.setText(cnt+":" + ID +":" +buf[0] + ":" + buf[1]);
    }
    public static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what==0xFBFE){
                byte[] data=(byte[])msg.obj;

                //if(msg.arg2 == 4)
                    tsb_app_fun(msg.arg1, data);
            }
        }
    };

    public static void hide_f0()
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(f0);
        transaction.show(f1);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.hide(f3);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.hide(f0);
                    transaction.show(f1);
                    transaction.hide(f2);
                    transaction.hide(f4);
                    transaction.commit();
                    if(f2.getSample_EN())
                        f2.timer_sample_dis();
                    return true;
                case R.id.navigation_dashboard:
                    transaction.hide(f0);
                    transaction.hide(f1);
                    transaction.show(f2);

                    transaction.hide(f4);
                    transaction.commit();

                    Button Sample_Btn = (Button)findViewById(R.id.btn_ADTest);
                    Sample_Btn.setText(R.string.btn_ADTest_on);
                    Sample_Btn.setTextColor(Color.BLACK);
                    return true;
                case R.id.navigation_notifications:
                    transaction.hide(f0);
                    transaction.hide(f1);
                    transaction.hide(f2);
                    f4.update_fileItemList();
                    transaction.show(f4);
                    transaction.commit();
                    if(f2.getSample_EN())
                        f2.timer_sample_dis();
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //this.ConnectView.
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.content, f0).commit();
        fragmentManager.beginTransaction().add(R.id.content, f1).hide(f1).commit();
        fragmentManager.beginTransaction().add(R.id.content, f2).hide(f2).commit();
        fragmentManager.beginTransaction().add(R.id.content, f3).hide(f3).commit();
        fragmentManager.beginTransaction().add(R.id.content, f4).hide(f4).commit();


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        tsb_comm.start();

        ble_ops.Init(this);

        /*
        new Handler().postDelayed(new Runnable(){
            public void run() {
                ble_ops.checkPermissions();
            }
        }, 1000);*/
        ble_ops.checkPermissions();

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }




    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            ble_ops.onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
            case REQUEST_CODE_EXTERNAL_STORAGE:
                if (grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    /*
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        File file = new File(Environment.getExternalStorageDirectory() + "/BLE-MWD/");
                        if (!file.exists()) {
                            Log.d("result", "create result:" + file.mkdirs());
                        }
                    }*/
                }
                else
                    Toast.makeText(this, "没有外部文件读写授权将无法实现数据下载和绘图！",Toast.LENGTH_SHORT).show();
                break;

        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                ble_ops.setScanRule();
                ble_ops.startScan();
            }
        }
    }

    public void checkPermissions() {

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        List<String> permissionDeniedList = new ArrayList<>();

        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionDeniedList.add(permission);
            }
        }

        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);

            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_EXTERNAL_STORAGE);
        }
    }
}
