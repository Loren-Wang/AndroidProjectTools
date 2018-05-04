package com.androidprojecttools.bluetooth.callback;

import android.bluetooth.BluetoothDevice; /**
 * 创建时间： 0004/2018/5/4 下午 3:35
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙操作回调
 * 功能方法：
 * 思路：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public interface BlueToothOptionsCallback {
    void systemBTDeviceStateChange(boolean isOpen);//蓝牙设备状态改变
    void scanBTDevice(boolean isHaveError, boolean isScan);
    void scanFoundBlueToothDevice(BluetoothDevice bluetoothDevice);//扫描到蓝牙设备
    void disconnectBtDevice();//远程蓝牙设备断开连接
}
