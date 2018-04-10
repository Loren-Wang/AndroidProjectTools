package com.androidprojecttools.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.adnroidprojecttools.bluetooth.BlueToothStateListener;
import com.adnroidprojecttools.bluetooth.BlueToothUtils;
import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Setting.APPLICATION_CONTEXT = getApplicationContext();
        Setting.IS_DEBUGGABLE = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If request is cancelled, the result arrays are empty.
        List<String> successPermissionList = new ArrayList<>();
        List<String> failPermissionList = new ArrayList<>();

        if(grantResults.length > 0 && grantResults.length == permissions.length) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    successPermissionList.add(permissions[i]);
                    LogUtils.logI("用户同意权限", "user granted the permission!" + permissions[i]);
                } else {
                    LogUtils.logI("用户不同意权限", "user denied the permission!" + permissions[i]);
                    failPermissionList.add(permissions[i]);
                }
            }
        }else {
            for(int i = 0 ; i < permissions.length ; i++){
                failPermissionList.add(permissions[i]);
            }
        }
        try {//只要有一个权限不通过则都失败
            if(failPermissionList.size() == 0){
                @SuppressLint("MissingPermission") final BlueToothUtils blueToothUtils = new BlueToothUtils();
                blueToothUtils.enableBlueTooth().scanDevice(false)
                        .setBlueToothStateListener(new BlueToothStateListener() {
                    @Override
                    protected void startScan() {

                    }

                    @Override
                    protected void stopScan(List<BluetoothDevice> deviceList) {
                        Iterator<BluetoothDevice> iterator = deviceList.iterator();
                        BluetoothDevice bluetoothDevice;
                        while (iterator.hasNext()){
                            bluetoothDevice = iterator.next();
                            if(!TextUtils.isEmpty(bluetoothDevice.getName()) && bluetoothDevice.getName().contains("MI1S")){
                                blueToothUtils.connectDevice(bluetoothDevice);
                                blueToothUtils.scanDevice(true);
                            }
                        }
                    }

                    @Override
                    protected void connectSuccess(BluetoothGatt gatt) {
                    }

                    @Override
                    protected void connectFail(BluetoothGatt gatt) {

                    }

                            @Override
                            protected void allowCommunication(BluetoothGatt gatt) {
                                blueToothUtils.sendOrderToBTDevice(gatt,"00002a06-0000-1000-8000-00805f9b34fb","01");
                            }
                        });
            }
        }catch (Exception e){
            LogUtils.logE(TAG,e.getMessage());
        }finally {
            successPermissionList.clear();
            failPermissionList.clear();
            successPermissionList = null;
            failPermissionList = null;
        }

        return;
    }

}
