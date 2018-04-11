package com.adnroidprojecttools.blueToothOptions;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by LorenWang on 2018/4/11.
 * 创建时间：2018/4/11 13:27
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙操作类回调
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public abstract class BlueToothOptionsCallback {
    /**
     * 返回扫描结果并判断是否是要找的扫描数据
     * @param bluetoothDevice
     * @return
     */
    protected boolean scanResultJudge(BluetoothDevice bluetoothDevice){
        return false;
    }
    protected abstract void connectBTDeviceSuccess(BluetoothGatt bluetoothGatt);//蓝牙设备连接成功
    protected abstract void connectBTDeviceFail(BluetoothGatt bluetoothGatt);//蓝牙设备连接失败
    protected abstract void reConnectBTDevice(BluetoothGatt bluetoothGatt);//蓝牙设备重连
    protected abstract void connectBTClose(BluetoothGatt bluetoothGatt);//蓝牙设备连接关闭
    protected abstract void allowSenOrderToBTDevice(BluetoothGatt bluetoothGatt);//允许向蓝牙设备发送指令，只有执行了这个方法后才会允许指令发送
    protected abstract void onBTDeviceReadCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic);//蓝牙设备读数据回调
    protected abstract void onBTDeviceWriteCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic);//蓝牙设备写数据回调

}
