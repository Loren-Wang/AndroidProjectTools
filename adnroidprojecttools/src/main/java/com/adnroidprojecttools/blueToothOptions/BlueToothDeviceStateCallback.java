package com.adnroidprojecttools.blueToothOptions;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.adnroidprojecttools.common.DigitalTransUtils;
import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

/**
 * Created by LorenWang on 2018/4/11.
 * 创建时间：2018/4/11 14:09
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙设备状态改变回调
 * 思路：当连接成功同时获取到了所有的设备服务后通过操作回调
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：蓝牙设备通信流程，先开启通道进行通知的接收，当通知通道开启后才能开启读写操作
 */

public class BlueToothDeviceStateCallback  extends BluetoothGattCallback {
    private final String TAG = getClass().getName();
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;//正在连接的设备
    private boolean isConnecting = false;//是否正在发起连接
    private boolean isConnectSuccess = false;//是否连接成功
    private BlueToothOptionsCallback blueToothOptionsCallback;

    private final int CONNECT_ERROR_RECONNECTION = 0;//蓝牙连接异常重新连接
    private final int CONNECT_ERROR_RECONNECTION_MAX_NUM = 5;//蓝牙连接重连次数
    private boolean isRecon = false;//是否正在重连
    private int reconNum = 0;//重连次数
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CONNECT_ERROR_RECONNECTION:
                    blueToothOptionsCallback.reConnectBTDevice();
                    LogUtils.logD(TAG,"蓝牙设备重连中");
                    reConBTDevice(bluetoothDevice);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 设置蓝牙操作回调
     * @param blueToothOptionsCallback
     */
    public void setBlueToothOptionsCallback(BlueToothOptionsCallback blueToothOptionsCallback) {
        this.blueToothOptionsCallback = blueToothOptionsCallback;
    }
    /**
     * 获取当前正在连接的设备
     * @return
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
    /**
     * 连接是否成功
     * @return
     */
    public boolean isConnectSuccess() {
        return isConnectSuccess;
    }


    /****************************************连接操作***********************************************/

    /**
     * 连接蓝牙设备
     * @param bluetoothDevice
     * @param stopReCon 是否结束上一次重连
     */
    public void connectBlueToothDevice(BluetoothDevice bluetoothDevice,boolean stopReCon){
        if(isRecon && stopReCon){
            //当正在重连同时需要停止重连的时候执行以下代码
            reconNum = -1;
            handler.removeMessages(CONNECT_ERROR_RECONNECTION);
            reConBTDevice(bluetoothDevice);
        }else if(!isRecon){
            reConBTDevice(bluetoothDevice);
        }
    }
    /**
     * 内部重连方法
     * @param bluetoothDevice
     */
    private void reConBTDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress()) && !isConnecting){
            this.bluetoothDevice = bluetoothDevice;
            bluetoothDevice.connectGatt(Setting.APPLICATION_CONTEXT,false,this);
            isConnecting = true;
            LogUtils.logD(TAG,"开始连接蓝牙设备");
        }
    }
    /**
     * 关闭蓝牙连接
     * @param bluetoothDevice
     */
    public void connectBTClose(BluetoothDevice bluetoothDevice){
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            isConnectSuccess = false;
        }
    }


    /********************************************数据操作*******************************************/

    /**
     * 蓝牙连接状态改变
     * @param gatt
     * @param status 连接状态
     * @param newState 新连接状态
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        isConnecting = false;
        this.bluetoothGatt = gatt;
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            LogUtils.logD(TAG,"连接蓝牙设备成功");
            isConnectSuccess = true;
            blueToothOptionsCallback.connectBTDeviceSuccess(gatt);
            //获取蓝牙设备信息
            gatt.discoverServices();
        } else if (status == BluetoothGatt.GATT_FAILURE && newState == BluetoothProfile.STATE_DISCONNECTED) {
            isConnectSuccess = false;
            if(newState == BluetoothProfile.STATE_DISCONNECTED){
                LogUtils.logD(TAG,"连接蓝牙设备断开连接");
                bluetoothGatt.close();
                blueToothOptionsCallback.connectBTClose(gatt);
            }else {
                LogUtils.logD(TAG,"连接蓝牙设备失败");
                blueToothOptionsCallback.connectBTDeviceFail(gatt);
            }

        }else {
            //防止出现133错误
            if(gatt != null && status == 133) {
                gatt.disconnect();
                gatt.close();
                this.bluetoothGatt = gatt = null;

                //开启重连，并做判断，超过一定重连次数则不再进行重连
                if(reconNum % CONNECT_ERROR_RECONNECTION_MAX_NUM > 0) {
                    isRecon = true;
                    handler.sendEmptyMessage(CONNECT_ERROR_RECONNECTION);
                }else {
                    isRecon = false;
                }
            }
        }
    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        this.bluetoothGatt = gatt;
        if(status == BluetoothGatt.GATT_SUCCESS) {
            LogUtils.logD(TAG,"蓝牙设备准备就绪");
            blueToothOptionsCallback.allowSenOrderToBTDevice(bluetoothGatt);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        this.bluetoothGatt = gatt;
        if(characteristic.getValue() != null){
            LogUtils.logD(TAG,"接收到读命令返回数据:::" + DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue()));
        }
        if(status == BluetoothGatt.GATT_SUCCESS) {
            blueToothOptionsCallback.onBTDeviceReadCallback(bluetoothGatt,characteristic);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        this.bluetoothGatt = gatt;
        if(characteristic.getValue() != null){
            LogUtils.logD(TAG,"接收到写命令返回数据:::" + DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue()));
        }
        if(status == BluetoothGatt.GATT_SUCCESS) {
            blueToothOptionsCallback.onBTDeviceWriteCallback(bluetoothGatt,characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if(characteristic.getValue() != null){
            LogUtils.logD(TAG,"接收到特征值改变数据:::" + DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue()));
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            blueToothOptionsCallback.onBTDeviceDescriptorReadCallback(bluetoothGatt,descriptor);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            blueToothOptionsCallback.onBTDeviceDescriptorWriteCallback(bluetoothGatt,descriptor);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }


}
