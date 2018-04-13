package com.androidprojecttools.android;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.adnroidprojecttools.bluetooth.BlueToothCallback;
import com.adnroidprojecttools.bluetooth.BlueToothUtils;
import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Setting.APPLICATION_CONTEXT = getApplicationContext();
        Setting.IS_DEBUGGABLE = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        List<String> successPermissionList = new ArrayList<>();
        List<String> failPermissionList = new ArrayList<>();

        if (grantResults.length > 0 && grantResults.length == permissions.length) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    successPermissionList.add(permissions[i]);
                    LogUtils.logI("用户同意权限", "user granted the permission!" + permissions[i]);
                } else {
                    LogUtils.logI("用户不同意权限", "user denied the permission!" + permissions[i]);
                    failPermissionList.add(permissions[i]);
                }
            }
        } else {
            for (int i = 0; i < permissions.length; i++) {
                failPermissionList.add(permissions[i]);
            }
        }
        if (failPermissionList.size() == 0) {
            final UUID serviceUUid = CarAirBlueToothInfo.getInstance().paramUUid("0001");
            final UUID characteristicUUid02 = CarAirBlueToothInfo.getInstance().paramUUid("0002");
            final UUID characteristicUUid03 = CarAirBlueToothInfo.getInstance().paramUUid("0003");

            final int SEND_ORDER_FOR_WRITE_GET_DATA = 0;
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case SEND_ORDER_FOR_WRITE_GET_DATA:
                            BlueToothUtils.getInstance().sendOrderToBTDeviceWrite(serviceUUid, characteristicUUid02
                                    , new byte[]{0x55, 0x01, 0x01, 0x02});
                            sendEmptyMessageDelayed(SEND_ORDER_FOR_WRITE_GET_DATA, 3000);
                            break;
                        default:
                            break;
                    }
                }
            };

            BlueToothUtils.getInstance().setBlueToothCallback(new BlueToothCallback() {
                @Override
                public void enableBT() {

                }

                @Override
                public void disableBT() {

                }

                @Override
                public void scaningBTDevice(boolean isBondScan) {

                }

                @Override
                public void stopScanBtDeveice(boolean isBondScan) {

                }

                @Override
                public void scanBTDeviceResult(BluetoothDevice bluetoothDevice) {
                    if (!TextUtils.isEmpty(bluetoothDevice.getAddress()) &&
                            bluetoothDevice.getAddress().substring(0, 8).equals("F9:CA:06")) {
                        BlueToothUtils.getInstance().connectBTClose(bluetoothDevice);
                        BlueToothUtils.getInstance().connectBTDevice(bluetoothDevice);
                        BlueToothUtils.getInstance().stopScan();
                    }
                }

                @Override
                public void connectBTDeviceSuccess() {

                }

                @Override
                public void connectBTDeviceRecon() {

                }

                @Override
                public void connectBTDeviceFail() {

                }

                @Override
                public void connectBTDeviceClose() {

                }

                @Override
                public void onCharacteristicForWriteOrderReceiveData() {

                }

                @Override
                public void onCharacteristicForReadOrderReceiveData() {

                }

                @Override
                public void onCharacteristicForNotifyWriteOrderReceiveData() {
                    handler.sendEmptyMessage(SEND_ORDER_FOR_WRITE_GET_DATA);
                }

                @Override
                public void onCharacteristicForNotifyReadOrderReceiveData() {

                }

                @Override
                public void onCharacteristicChangeForNotifyOrderReceiveData() {

                }

                @Override
                public void allowSendOrderToBTDevice() {
                    BlueToothUtils.getInstance().sendOrderToBTDeviceNotify(serviceUUid, characteristicUUid03);
                }
            });
            BlueToothUtils.getInstance().enableBlueTooth();
            try {
                BlueToothUtils.getInstance().startScanBTDevice(null, null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
