package com.adnroidprojecttools.blueToothOptions;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

/**
 * 蓝牙广播接收器，用来接收扫描到的蓝牙设备
 */
class BlueToothDeviceReceiver extends BroadcastReceiver {
    private BlueToothOptionUtils blueToothOptionUtils;
    private  Map<String, BluetoothDevice> deviceMap;
    private String TAG = getClass().getName();

    public BlueToothDeviceReceiver(BlueToothOptionUtils blueToothOptionUtils, Map<String, BluetoothDevice> deviceMap) {
        this.blueToothOptionUtils = blueToothOptionUtils;
        this.deviceMap = deviceMap;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Log.d(TAG, "开始扫描...");
        }
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                // 添加到ListView的Adapter。
                deviceMap.put(device.getAddress(),device);
                boolean state = blueToothOptionUtils.getBlueToothOptionsCallback().scanResultJudge(device);
                if(state){
                    blueToothOptionUtils.stopScan();
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "扫描结束.");
            }
        }
    }

}
