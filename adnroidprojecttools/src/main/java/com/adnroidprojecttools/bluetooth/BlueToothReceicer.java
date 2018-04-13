package com.adnroidprojecttools.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.adnroidprojecttools.common.LogUtils;

/**
 * Created by LorenWang on 2018/4/13.
 * 创建时间：2018/4/13 10:16
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙设备扫描以及蓝牙设备绑定状态改变的广播监听
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public class BlueToothReceicer extends BroadcastReceiver {
    private final String TAG = getClass().getName() + hashCode();
    private Handler handler;//回调handler
    private int SCAN_TO_DEVICE;//扫描到设备的回调msg.what
    private int SCAN_UN_BOND_START;//开始蓝牙扫描
    private int SCAN_UN_BOND_STOP;//停止蓝牙扫描
    private int BT_DEVICE_BOND_BONDING;//正在配对
    private int BT_DEVICE_BOND_BONDED;//完成配对
    private int BT_DEVICE_BOND_NONE;//取消配对

    public BlueToothReceicer(Handler handler, int SCAN_TO_DEVICE, int SCAN_UN_BOND_START, int SCAN_UN_BOND_STOP
            , int BT_DEVICE_BOND_BONDING, int BT_DEVICE_BOND_BONDED, int BT_DEVICE_BOND_NONE) {
        if(handler == null){
            throw new NullPointerException("蓝牙广播接收器的回调handler不能为空");
        }
        this.handler = handler;
        this.SCAN_TO_DEVICE = SCAN_TO_DEVICE;
        this.SCAN_UN_BOND_START = SCAN_UN_BOND_START;
        this.SCAN_UN_BOND_STOP = SCAN_UN_BOND_STOP;
        this.BT_DEVICE_BOND_BONDING = BT_DEVICE_BOND_BONDING;
        this.BT_DEVICE_BOND_BONDED = BT_DEVICE_BOND_BONDED;
        this.BT_DEVICE_BOND_NONE = BT_DEVICE_BOND_NONE;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device;
        Message message;
        switch (action){
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                LogUtils.logD(TAG,"未绑定设备蓝牙扫描已开始");
                handler.sendEmptyMessage(SCAN_UN_BOND_START);
                break;
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                LogUtils.logD(TAG,"蓝牙扫描已结束");
                handler.sendEmptyMessage(SCAN_UN_BOND_STOP);
                break;
            case BluetoothDevice.ACTION_FOUND:
                LogUtils.logD(TAG,"已扫描到未绑定设备，准备进行设备地址检测");
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                message = Message.obtain();
                message.what = SCAN_TO_DEVICE;
                message.obj = device;
                handler.sendMessage(message);
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)) {
                    case BluetoothDevice.BOND_BONDING:
                        message = Message.obtain();
                        message.what = BT_DEVICE_BOND_BONDING;
                        if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                            Log.d(TAG, "接收到蓝牙设备MAC地址为：" + device.getAddress() + "的正在配对状态");
                            message.obj = device;
                        }else {
                            Log.d(TAG, "接收到蓝牙设备正在配对状态");
                        }
                        handler.sendMessage(message);
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        message = Message.obtain();
                        message.what = BT_DEVICE_BOND_BONDED;
                        if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                            Log.d(TAG, "接收到蓝牙设备MAC地址为：" + device.getAddress() + "的完成配对状态");
                            message.obj = device;
                        }else {
                            Log.d(TAG, "接收到蓝牙设备完成配对状态");
                        }
                        handler.sendMessage(message);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        message = Message.obtain();
                        message.what = BT_DEVICE_BOND_NONE;
                        if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                            Log.d(TAG, "接收到蓝牙设备MAC地址为：" + device.getAddress() + "的取消配对状态");
                            message.obj = device;
                        }else {
                            Log.d(TAG, "接收到蓝牙设备取消配对状态");
                        }
                        handler.sendMessage(message);
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
