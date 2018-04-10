package com.adnroidprojecttools.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class BlueToothDeviceReceiver extends BroadcastReceiver {

    private String TAG = getClass().getName();
    private Map<String, BluetoothDevice> deviceMap = new HashMap<>();

    public BlueToothDeviceReceiver(Map<String, BluetoothDevice> deviceMap) {
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
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "扫描结束.");
            }
        }
    }
}
