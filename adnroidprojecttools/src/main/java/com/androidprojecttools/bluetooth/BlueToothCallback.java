package com.androidprojecttools.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by LorenWang on 2018/4/13.
 * 创建时间：2018/4/13 09:58
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙状态回调，包括蓝牙的开关以及扫描状态还有蓝牙设备的连接以及状态改变传值等
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public interface BlueToothCallback {
    /**
     * 开启蓝牙
     */
    void enableBT();//开启蓝牙

    /**
     * 关闭蓝牙
     */
    void disableBT();//关闭蓝牙

    /**
     * 正在扫描蓝牙设备
     */
    void scaningBTDevice(boolean isBondScan);//正在扫描蓝牙

    /**
     * 停止扫描蓝牙设备
     */
    void stopScanBtDeveice(boolean isBondScan);

    /**
     * 扫描到设备后将设备返回
     * @param bluetoothDevice
     */
    void scanBTDeviceResult(BluetoothDevice bluetoothDevice);

    /**
     * 连接蓝牙设备成功
     */
    void connectBTDeviceSuccess();

    /**
     * 重新连接蓝牙设备中
     */
    void connectBTDeviceRecon();

    /**
     * 连接蓝牙设备失败
     */
    void connectBTDeviceFail();

    /**
     * 蓝牙设备连接关闭
     */
    void connectBTDeviceClose();

    /**
     * 当向蓝牙设备发送写命令后蓝牙设备返回的数据
     */
    void onCharacteristicForWriteOrderReceiveData();

    /**
     * 当向蓝牙设备发送读命令后蓝牙设备返回的数据
     */
    void onCharacteristicForReadOrderReceiveData();

    /**
     * 当向蓝牙设备发送要接收某一个特征值得通知命令后蓝牙设备返回的数据，也就是调用这个命令之后蓝牙设备返回是否允许通知返回
     * 一般情况下需要在这个方法被调用后进行读写命令操作，否则一旦出现发送读取数据命令和返回命令的通道不一致的情况就会导致无法接收
     * 到任何数据，当然了如果是同一通道返回的话可以不用管这个回调也不用发送特征通道改变的通知监听命令
     */
    void onCharacteristicForNotifyWriteOrderReceiveData();
    /**
     * 当向蓝牙设备发送要接收某一个特征值得通知命令后蓝牙设备返回的数据，也就是调用这个命令之后蓝牙设备返回是否允许通知返回
     * 一般情况下需要在这个方法被调用后进行读写命令操作，否则一旦出现发送读取数据命令和返回命令的通道不一致的情况就会导致无法接收
     * 到任何数据，当然了如果是同一通道返回的话可以不用管这个回调也不用发送特征通道改变的通知监听命令
     */
    void onCharacteristicForNotifyReadOrderReceiveData();

    /**
     * 发送指定的特征通道通知监听后这个指定的特征通道状态或值改变时的回调
     */
    void onCharacteristicChangeForNotifyOrderReceiveData();

    /**
     * 当有效服务被检测完成后的回调，在执行了这个方法后才允许发送读写或者通知命令
     */
    void allowSendOrderToBTDevice();

}
