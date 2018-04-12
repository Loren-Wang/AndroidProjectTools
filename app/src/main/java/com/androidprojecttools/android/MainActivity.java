package com.androidprojecttools.android;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.adnroidprojecttools.blueToothOptions.BlueToothOptionUtils;
import com.adnroidprojecttools.blueToothOptions.BlueToothOptionsCallback;
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
                final UUID serviceUUid = CarAirBlueToothInfo.getInstance().paramUUid("0001");
                final UUID characteristicUUid02 = CarAirBlueToothInfo.getInstance().paramUUid("0002");
                final UUID characteristicUUid03 = CarAirBlueToothInfo.getInstance().paramUUid("0003");
                BlueToothOptionUtils.getInstance().setBlueToothOptionsCallback(new BlueToothOptionsCallback() {
                    @Override
                    protected void connectBTDeviceSuccess(BluetoothGatt bluetoothGatt) {

                    }

                    @Override
                    protected void connectBTDeviceFail(BluetoothGatt bluetoothGatt) {

                    }

                    @Override
                    protected void reConnectBTDevice() {

                    }

                    @Override
                    protected void connectBTClose(BluetoothGatt bluetoothGatt) {

                    }

                    @Override
                    protected boolean scanResultJudge(BluetoothDevice bluetoothDevice) {
                        if(!TextUtils.isEmpty(bluetoothDevice.getAddress()) &&
                                bluetoothDevice.getAddress().substring(0,8).equals("F9:CA:06")){
                            BlueToothOptionUtils.getInstance().connectBTClose(bluetoothDevice).connectBTDevice(bluetoothDevice);
                            BlueToothOptionUtils.getInstance().stopScan();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    protected void onBTDeviceReadCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
//                         //小米运动步数
//                        int stepNum = characteristic.getValue()[3] << 24 | (characteristic.getValue()[2] & 0xFF) << 16 | (characteristic.getValue()[1] & 0xFF) << 8 | (characteristic.getValue()[0] & 0xFF);

                        int flag = characteristic.getProperties();
                        int format = -1;
                        if ((flag & 0x01) != 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                            Log.d(TAG, "Heart rate format UINT16.");
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                            Log.d(TAG, "Heart rate format UINT8.");
                        }
                        final int heartRate = characteristic.getIntValue(format, 1);
                    }

                    @Override
                    protected void onBTDeviceWriteCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
//                        //小米运动步数
//                        int stepNum = characteristic.getValue()[3] << 24 | (characteristic.getValue()[2] & 0xFF) << 16 | (characteristic.getValue()[1] & 0xFF) << 8 | (characteristic.getValue()[0] & 0xFF);
                        allowSenOrderToBTDevice(bluetoothGatt);
                    }

                    @Override
                    protected void onBTDeviceDescriptorWriteCallback(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {
                        BlueToothOptionUtils.getInstance().sendOrderToBTDeviceWrite(bluetoothGatt,serviceUUid,characteristicUUid02
                                , new byte[]{0x55,0x01,0x01,0x02});
                    }

                    @Override
                    protected void onBTDeviceDescriptorReadCallback(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {
                        boolean state = bluetoothGatt.readCharacteristic(descriptor.getCharacteristic());
                        if(state){

                        }
                    }

                    @Override
                    protected void allowSenOrderToBTDevice(BluetoothGatt bluetoothGatt) {
                        BlueToothOptionUtils.getInstance().sendOrderToBTDeviceNotify(bluetoothGatt,serviceUUid,characteristicUUid03);
                    }

                }).enableBlueTooth().startScan();
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
