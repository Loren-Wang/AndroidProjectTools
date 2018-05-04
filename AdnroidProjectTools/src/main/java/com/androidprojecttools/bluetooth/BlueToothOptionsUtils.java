package com.androidprojecttools.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.androidprojecttools.R;
import com.androidprojecttools.bluetooth.callback.BlueToothOptionsCallback;
import com.androidprojecttools.bluetooth.callback.BlueToothReceiverCallback;
import com.androidprojecttools.common.LogUtils;
import com.androidprojecttools.common.Setting;

import java.util.Iterator;
import java.util.Set;

/**
 * 创建时间： 0004/2018/5/4 下午 3:31
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙工具类
 * 功能方法：1、注册广播接收器
 *         2、取消注册广播接收器（一般只有销毁单例的时候才会取消注册）
 * 思路：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class BlueToothOptionsUtils {
    private final String TAG = "BlueToothOptions";
    private static BlueToothOptionsUtils blueToothOptionsUtils;
    private Resources resources;
    public static BlueToothOptionsUtils getInstance(){
        if(blueToothOptionsUtils == null){
            blueToothOptionsUtils = new BlueToothOptionsUtils();
        }
        return blueToothOptionsUtils;
    }
    private BlueToothOptionsUtils(){
        blueToothStateReceiver = new BlueToothStateReceiver(blueToothReceiverCallback);
        //注册广播接收器
        registReceiver();
        resources = Setting.APPLICATION_CONTEXT.getResources();
    }

    private BlueToothStateReceiver blueToothStateReceiver;//蓝牙状态广播接收器
    private BlueToothOptionsCallback blueToothOptionsCallback;//蓝牙回调
    private BluetoothGatt bluetoothGatt;//当前正在连接的蓝牙设备的控制器
    private BluetoothDevice bluetoothDevice;//当前正在连接的设备



    /*****************************************变量**************************************************/
    private boolean isScan = false;//是否正在扫描
    private String scanBTAddress;//扫描的蓝牙设备地址
    private String scanBTName;//扫描的蓝牙设备名称




    /*******************************************私有方法********************************************/
    /**
     * 注册蓝牙广播接收器
     */
    private void registReceiver(){
        // 注册Receiver来获取蓝牙设备相关的结果
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";//配对请求
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索蓝压设备，每搜到一个设备发送一条广播
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//远程蓝牙设备状态改变的时候发出这个广播, 例如设备被匹配, 或者解除配对
        intent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//配对时，发起连接
        intent.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);//一个远程设备的绑定状态发生改变时发出广播
        intent.addAction(BluetoothDevice.ACTION_UUID);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//与远程设备建立了ACL连接发出的广播
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);//ACL连接即将断开
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//与远程设备断开ACL连接后发出的广播
        intent.addAction(ACTION_PAIRING_REQUEST);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //搜索结束。重新搜索时，会先终止搜索
        intent.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//连接蓝牙，断开蓝牙
        intent.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);//更改蓝牙名称，打开蓝牙时，可能会调用多次
        intent.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//搜索模式改变
        Setting.APPLICATION_CONTEXT.registerReceiver(blueToothStateReceiver, intent);
    }
    /**
     * 取消注册广播接收器
     */
    private void unRegistReceiver(){
        Setting.APPLICATION_CONTEXT.unregisterReceiver(blueToothStateReceiver);
    }
    /**
     *检测权限
     * @return
     */
    private boolean checkPermisstion(){
        if (ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else {
            return false;
        }
    }
    /**
     * 获取蓝牙适配器
     * @return
     */
    private BluetoothAdapter getBluetoothAdapter(){
        if(checkPermisstion()){
            BluetoothManager bluetoothManager = (BluetoothManager) Setting.APPLICATION_CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            bluetoothManager = null;
            return adapter;
        }else {
            return null;
        }
    }
    /**
     * 重置蓝牙
     */
    private void reset(){
        connectBTClose();
        stopScanBTDevice();
    }
    /**
     * 扫描未绑定远程设备
     */
    private void scanUnBoundBTDevice(){
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            //开始扫描未绑定设备
            boolean state = bluetoothAdapter.startDiscovery();
            //开启设备扫描回调
            if(blueToothOptionsCallback != null){
                blueToothOptionsCallback.scanBTDevice(!state,isScan);
            }
            handler.sendEmptyMessageDelayed(PAUSE_SCAN_DEVICE, 5000);
        }
    }
    /**
     * 暂停扫描
     */
    private void pauseScanUnBoundBTDevice(){
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            //开始扫描未绑定设备
            bluetoothAdapter.cancelDiscovery();
            handler.sendEmptyMessageDelayed(RESET_SCAN_DEVICE, 5000);
        }
    }






    /*******************************************公有方法********************************************/
    /**
     * 设置操作回调
     * @param blueToothOptionsCallback
     * @return
     */
    public BlueToothOptionsUtils setBlueToothOptionsCallback(BlueToothOptionsCallback blueToothOptionsCallback) {
        this.blueToothOptionsCallback = blueToothOptionsCallback;
        return this;
    }

    /**
     * 开启蓝牙
     */
    public synchronized void enableBlueTooth(){
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()){
            LogUtils.logD(TAG,"准备开启蓝牙");
            try {
                //首先直接开启蓝牙
                bluetoothAdapter.enable();
            }catch (Exception e){
                LogUtils.logE(TAG,"直接开启蓝牙失败，需要跳转到设置中开启蓝牙");
                Setting.APPLICATION_CONTEXT.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }
    }
    /**
     * 关闭蓝牙
     */
    public synchronized void disableBlueTooth(){
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
            LogUtils.logD(TAG,"准备关闭蓝牙");
            try {
                //直接关闭蓝牙,关闭蓝牙后续操作由广播接收到系统蓝牙关闭了再进行操作
                bluetoothAdapter.disable();
            }catch (Exception e){
                LogUtils.logE(TAG,"蓝牙关闭失败，跳转到设置中关闭蓝牙");
                Setting.APPLICATION_CONTEXT.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }
    }

    /**
     * 开启设备扫描
     * @param deviceName 要查找的指定设备的名称
     * @param deviceAddress 要查找的指定设备的MAC地址
     */
    public synchronized void startScanBTDevice(String deviceName,String deviceAddress){
        if(isScan){
            return;
        }
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null){
            if(bluetoothAdapter.isEnabled()){
                isScan = true;
                this.scanBTName = deviceName;
                this.scanBTAddress = deviceAddress;

                //开启设备扫描回调
                if(blueToothOptionsCallback != null){
                    blueToothOptionsCallback.scanBTDevice(false,isScan);
                }

                //开始扫描已绑定设备
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                if(bondedDevices != null){
                    Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
                    BluetoothDevice bluetoothDevice;
                    while (iterator.hasNext()){
                        if(!isScan){
                            break;
                        }
                        bluetoothDevice = iterator.next();
                        if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress())){
                            blueToothReceiverCallback.scanFoundBlueToothDevice(bluetoothDevice);
                        }
                    }
                }
                //扫描未绑定设备
                scanUnBoundBTDevice();
                LogUtils.logD(TAG,resources.getString(R.string.scan_bt_device_start));
            }else {
                //开启设备扫描回调
                if(blueToothOptionsCallback != null){
                    blueToothOptionsCallback.scanBTDevice(true,isScan);
                }
            }
        }else {
            //开启设备扫描回调
            if(blueToothOptionsCallback != null){
                blueToothOptionsCallback.scanBTDevice(true,isScan);
            }
        }
    }
    /**
     * 停止扫描
     */
    public synchronized void stopScanBTDevice(){
        isScan = false;
        handler.removeMessages(PAUSE_SCAN_DEVICE);
        handler.removeMessages(RESET_SCAN_DEVICE);
        this.scanBTAddress = null;
        this.scanBTName = null;
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
            boolean state = bluetoothAdapter.cancelDiscovery();
            //开启设备扫描回调
            if(blueToothOptionsCallback != null){
                blueToothOptionsCallback.scanBTDevice(!state,isScan);
            }
        }else {
            //开启设备扫描回调
            if(blueToothOptionsCallback != null){
                blueToothOptionsCallback.scanBTDevice(true,isScan);
            }
        }
        LogUtils.logD(TAG,resources.getString(R.string.scan_bt_device_stop));
    }
    /**
     * 重置扫描
     */
    public synchronized void resetScan(){
        stopScanBTDevice();
        startScanBTDevice(this.scanBTName,this.scanBTAddress);
    }

    /**
     * 连接蓝牙设备
     * @param bluetoothDevice
     * @param stopReCon 是否结束上一次重连
     */
    public void connectBlueToothDevice(BluetoothDevice bluetoothDevice){
        reConBTDevice(bluetoothDevice);
    }
    /**
     * 内部重连方法
     * @param bluetoothDevice
     */
    private void reConBTDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress()) ){
            this.bluetoothDevice = bluetoothDevice;
            LogUtils.logD(TAG,"准备开始连接蓝牙设备");
            bluetoothDevice.connectGatt(Setting.APPLICATION_CONTEXT,false,null);
        }
    }

    /**
     * 关闭蓝牙连接
     */
    public void connectBTClose(){
        bluetoothDevice = null;
        bluetoothGatt = null;
    }






    /*******************************************回调方法********************************************/
    private final int PAUSE_SCAN_DEVICE = 0;//暂停设备扫描
    private final int RESET_SCAN_DEVICE = 1;//重启设备扫描
    private Handler handler = new Handler(){
        private String scanDeviceName = "";//扫描设备的名称
        private String scanDeviceAdress = "";//扫描设备的地址
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PAUSE_SCAN_DEVICE://停止设备扫描并在指定时间之后再次开启扫描
                    if(isScan) {
                        LogUtils.logD(TAG,resources.getString(R.string.scan_bt_device_pause));
                        pauseScanUnBoundBTDevice();
                    }
                    break;
                case RESET_SCAN_DEVICE://开始设备扫描并在指定时间之后再次关闭扫描
                    if(isScan) {
                        LogUtils.logD(TAG,resources.getString(R.string.scan_bt_device_reset));
                        scanUnBoundBTDevice();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    //蓝牙广播接收器接收到的数据
    private BlueToothReceiverCallback blueToothReceiverCallback = new BlueToothReceiverCallback() {
        @Override
        public void systemBlueToothStateChange(Integer state) {
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    LogUtils.logD(TAG, resources.getString(R.string.system_bluetooth_off));
                    if(blueToothOptionsCallback != null){
                        blueToothOptionsCallback.systemBTDeviceStateChange(false);
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    LogUtils.logD(TAG,resources.getString(R.string.system_bluetooth_on));
                    if(blueToothOptionsCallback != null){
                        blueToothOptionsCallback.systemBTDeviceStateChange(true);
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    LogUtils.logD(TAG,resources.getString(R.string.system_bluetooth_turning_off));
                    connectBTClose();
                    stopScanBTDevice();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    LogUtils.logD(TAG,resources.getString(R.string.system_bluetooth_turning_on));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void scanFoundBlueToothDevice(BluetoothDevice bluetoothDevice) {
            if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress())){
                LogUtils.logD(TAG,resources.getString(R.string.scan_bt_device_address) + bluetoothDevice.getAddress());
                if(blueToothOptionsCallback != null){
                    blueToothOptionsCallback.scanFoundBlueToothDevice(bluetoothDevice);
                }
            }
        }

        @Override
        public void disconnectBtDevice() {
            connectBTClose();
            if(blueToothOptionsCallback != null){
                blueToothOptionsCallback.disconnectBtDevice();
            }
        }
    };
}
