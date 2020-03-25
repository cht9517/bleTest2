package nbkj.cht.bletest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewDebug;
import android.widget.TextView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import nbkj.cht.bletest.AppContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleOperate{


    public int ble_status = 0;
    public BleDevice mbleDevice;
    private BluetoothGatt mgatt;
    private Activity this_activity = null;


    private String uuid_str = "";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static final String STR_UUID_SERVICE =
            new UUID(0x0000fff000001000L, 0x800000805F9B34FBL).toString();
    private static final String STR_UUID_CHARA_READ =
            new UUID(0x0000fff100001000L, 0x800000805F9B34FBL).toString();
    private static final String STR_UUID_CHARA_WRITE =
            new UUID(0x0000fff200001000L, 0x800000805F9B34FBL).toString();


    public void Init(Activity activity) {
        this_activity = activity;
        //setScanRule();
    }

    public void setScanRule() {

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                //.setDeviceName(false, "E104-BT0")   // 只扫描指定广播名的设备，可选  扫描后人工判断
                //.setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(true)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                //mDeviceAdapter.clearScanDevice();
                //mDeviceAdapter.notifyDataSetChanged();
                //img_loading.startAnimation(operatingAnim);
                //img_loading.setVisibility(View.VISIBLE);
                //btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                String dev_name = "";
                if(bleDevice.getName() != null)
                    dev_name = bleDevice.getName();

                if(dev_name.contains("E104-BT0")) {
                    mbleDevice = bleDevice;
                    ble_status = 1;

                    MainActivity.f0.textViewConnectSet("连接状态：1(" + dev_name.subSequence(0, 9) + ")");

                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                    //mDeviceAdapter.addDevice(bleDevice);
                    //mDeviceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                //img_loading.clearAnimation();
                //img_loading.setVisibility(View.INVISIBLE);
                if(ble_status == 0)
                {
                    MainActivity.f0.textViewConnectSet("连接失败，请重新连接！");
                    MainActivity.f0.btnReconnectShow(View.VISIBLE);
                }

                //btn_scan.setText(getString(R.string.start_scan));
            }
        });
    }


    public void write_cmd(byte[] buf)
    {
        BleManager.getInstance().write(
                mbleDevice,
                STR_UUID_SERVICE,
                STR_UUID_CHARA_WRITE,
                buf,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                    }
                });
    }

    public void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mgatt = gatt;
                ble_status = 2;
                MainActivity.f0.textViewConnectSet("连接状态：2");

                BleManager.getInstance().notify(
                        mbleDevice,
                        STR_UUID_SERVICE,
                        STR_UUID_CHARA_READ,
                        new BleNotifyCallback() {
                            @Override
                            public void onNotifySuccess() {
                                ble_status = 3;// 打开通知操作成功
                                MainActivity.f0.textViewConnectSet("连接状态：完成");
                                MainActivity.hide_f0();
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                ble_status = 0xFe;
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                // 打开通知后，设备发过来的数据将在这里出现
                                TSBComm.rx_buf_put(data);
                            }
                        });
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }
        });
    }


    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this_activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onPermissionGranted(String permission) {
        switch (permission) {

            case Manifest.permission.ACCESS_FINE_LOCATION:

                if (checkGPSIsOpen()) {
                    //setScanRule();这里加上后蓝牙连接状态迟迟不能进入2
                    startScan();
                }
                break;
        }
    }

    public void checkPermissions() {

        //打开蓝牙
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this_activity, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this_activity, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }



}
