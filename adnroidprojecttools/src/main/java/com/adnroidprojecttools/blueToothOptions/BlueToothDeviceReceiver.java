package com.adnroidprojecttools.blueToothOptions;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Map;

/**
 * 蓝牙广播接收器，用来接收扫描到的蓝牙设备
 */
class BlueToothDeviceReceiver extends BroadcastReceiver {

    public BlueToothDeviceReceiver(BlueToothOptionUtils blueToothOptionUtils, Map<String, BluetoothDevice> deviceMap) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}
