package com.adnroidprojecttools.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

import java.util.List;

/**
 * Created by LorenWang on 2018/4/9.
 * 创建时间：2018/4/9 17:18
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙状态监听
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public abstract class BlueToothStateListener extends BluetoothGattCallback {
    private final String TAG = getClass().getName();
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

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
                    reConBTDevice(bluetoothDevice);
                    break;
                default:
                    break;
            }
        }
    };

    protected abstract void startScan();//开启蓝牙扫描
    protected abstract void stopScan(List<BluetoothDevice> deviceList);//停止蓝牙扫描,并返回扫描结果
    protected abstract void connectSuccess(BluetoothGatt gatt);//蓝牙连接成功
    protected abstract void connectFail(BluetoothGatt gatt);//蓝牙连接失败
    protected abstract void allowCommunication(BluetoothGatt gatt);//允许和蓝牙设备进行通信


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
        if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress())){
            this.bluetoothDevice = bluetoothDevice;
            bluetoothDevice.connectGatt(Setting.APPLICATION_CONTEXT,true,this);
            LogUtils.logD(TAG,"开始连接蓝牙设备");
        }
    }

    /**
     * 蓝牙连接状态改变
     * @param gatt
     * @param status 连接状态
     * @param newState 新连接状态
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        this.bluetoothGatt = gatt;
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            LogUtils.logD(TAG,"连接蓝牙设备成功");
            //获取蓝牙设备信息
            gatt.discoverServices();
            connectSuccess(gatt);
        } else if (status == BluetoothGatt.GATT_FAILURE && newState == BluetoothProfile.STATE_DISCONNECTED) {
            LogUtils.logD(TAG,"连接蓝牙设备失败");
            connectFail(gatt);
        }else {
            //防止出现133错误
            if(gatt != null && status == 133) {
                gatt.disconnect();
                gatt.close();
                this.bluetoothGatt = gatt = null;

                //开启重连，并做判断，超过一定重连次数则不再进行重连
                if(++reconNum % CONNECT_ERROR_RECONNECTION_MAX_NUM > 0) {
                    isRecon = true;
                    handler.sendEmptyMessage(CONNECT_ERROR_RECONNECTION);
                }else {
                    isRecon = false;
                }
            }
        }
    }
//
//    int uuidPosi = 0;
//    String[] uuids = new String[]{"00002a01-0000-1000-8000-00805f9b34fb"
//            ,"00002a02-0000-1000-8000-00805f9b34fb"
//            ,"00002a04-0000-1000-8000-00805f9b34fb"
//            ,"00002a05-0000-1000-8000-00805f9b34fb"
//            ,"0000ff01-0000-1000-8000-00805f9b34fb"
//            ,"0000ff02-0000-1000-8000-00805f9b34fb"
//            ,"0000ff03-0000-1000-8000-00805f9b34fb"
//            ,"0000ff04-0000-1000-8000-00805f9b34fb"
//            ,"0000ff05-0000-1000-8000-00805f9b34fb"
//            ,"0000ff06-0000-1000-8000-00805f9b34fb"
//            ,"0000ff07-0000-1000-8000-00805f9b34fb"
//            ,"0000ff08-0000-1000-8000-00805f9b34fb"
//            ,"0000ff09-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0a-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0b-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0c-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0d-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0e-0000-1000-8000-00805f9b34fb"
//            ,"0000ff0f-0000-1000-8000-00805f9b34fb"
//            ,"0000ff10-0000-1000-8000-00805f9b34fb"
//            ,"0000fec9-0000-1000-8000-00805f9b34fb"
//            ,"0000fedd-0000-1000-8000-00805f9b34fb"
//            ,"0000fede-0000-1000-8000-00805f9b34fb"
//            ,"0000fedf-0000-1000-8000-00805f9b34fb"
//            ,"0000fed0-0000-1000-8000-00805f9b34fb"
//            ,"0000fed1-0000-1000-8000-00805f9b34fb"
//            ,"0000fed2-0000-1000-8000-00805f9b34fb"
//            ,"0000fed3-0000-1000-8000-00805f9b34fb"
//            ,"00002a37-0000-1000-8000-00805f9b34fb"
//            ,"00002a39-0000-1000-8000-00805f9b34fb"
//            ,"00002a06-0000-1000-8000-00805f9b34fb"
//    };
//    private void testGetBTInfo(BluetoothGatt gatt){
//        if(uuidPosi < uuids.length) {
//            boolean isHave = false;
//            List<BluetoothGattService> services = gatt.getServices();
//            for (BluetoothGattService service : services) {
//                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(uuids[uuidPosi]));
//                if (characteristic != null) {
//                    characteristic.setValue(hexStr2Bytes("02"));
//                    gatt.writeCharacteristic(characteristic);
//                    isHave = true;
//                }
//            }
//            uuidPosi++;
//
////            if (!isHave) {
////                testGetBTInfo(gatt);
////            }
//        }
//    }

    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        this.bluetoothGatt = gatt;
        if(status == BluetoothGatt.GATT_SUCCESS) {
            allowCommunication(gatt);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        this.bluetoothGatt = gatt;
        LogUtils.logD(TAG, String.valueOf(status));
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        this.bluetoothGatt = gatt;
        LogUtils.logD(TAG, String.valueOf(status));
    }
}
