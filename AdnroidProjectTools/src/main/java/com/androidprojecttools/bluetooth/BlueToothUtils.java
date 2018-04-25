package com.androidprojecttools.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.androidprojecttools.common.DigitalTransUtils;
import com.androidprojecttools.common.LogUtils;
import com.androidprojecttools.common.Setting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by LorenWang on 2018/4/13.
 * 创建时间：2018/4/13 10:40
 * 创建人：王亮（Loren wang）
 * 功能作用：
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public class BlueToothUtils {
    private static BlueToothUtils blueToothUtils;

    public static BlueToothUtils getInstance(){
        if(blueToothUtils == null){
            blueToothUtils = new BlueToothUtils();
        }
        return blueToothUtils;
    }

    private final String TAG = getClass().getName() + hashCode();
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private BluetoothDevice bluetoothDevice;//当前正在连接的设备
    private BlueToothReceicer blueToothReceicer;//蓝牙广播接收器
    private BluetoothGatt bluetoothGatt;//当前正在连接的蓝牙设备的控制器
    private BlueToothCallback blueToothCallback;//蓝牙回调
    private boolean isSupportBT = false;//设备是否支持蓝牙
    private boolean isEnableBT = false;//蓝牙是否开启
    private boolean isScan = false;//是否正在扫描蓝牙设备
    private boolean isConnecting = false;//是否正在发起连接
    private boolean isConnectSuccess = false;//是否连接成功
    private boolean isRecon = false;//是否正在重连
    private Map<String,BluetoothDevice> deviceMap = new HashMap<>();//蓝牙设备记录

    private BlueToothUtils(){
        LogUtils.logD(TAG,"蓝牙工具类单例实例化，进行权限检测");
        if (ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LogUtils.logD(TAG, "设备拥有蓝牙相关权限");
            final BluetoothManager bluetoothManager = (BluetoothManager) Setting.APPLICATION_CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            //初始化是否支持蓝牙
            if(mBluetoothAdapter == null){
                LogUtils.logD(TAG,"设备不支持蓝牙");
                isSupportBT = false;
                return;
            }else {
                LogUtils.logD(TAG,"设备支持蓝牙");
                isSupportBT = true;
            }
            //初始化蓝牙设备是否开启
            if(mBluetoothAdapter.isEnabled()){
                LogUtils.logD(TAG,"蓝牙设备是已开启状态");
                isEnableBT = true;
            }else {
                LogUtils.logD(TAG,"蓝牙设备未开启");
                isEnableBT = false;
            }

            //实例化蓝牙广播接收器
            blueToothReceicer = new BlueToothReceicer(handler, SCAN_TO_DEVICE, SCAN_UN_BOND_START, SCAN_UN_BOND_STOP
                    , BT_DEVICE_BOND_BONDING, BT_DEVICE_BOND_BONDED, BT_DEVICE_BOND_NONE);
            // 注册Receiver来获取蓝牙设备相关的结果
            String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
            IntentFilter intent = new IntentFilter();
            intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
            intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            intent.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
            intent.addAction(BluetoothDevice.ACTION_UUID);
            intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intent.addAction(ACTION_PAIRING_REQUEST);
            intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            Setting.APPLICATION_CONTEXT.registerReceiver(blueToothReceicer, intent);


        }else {
            LogUtils.logD(TAG,"设备未拥有蓝牙相关权限");
        }
    }

    /**
     * 设置回调
     * @param blueToothCallback
     * @return
     */
    public BlueToothUtils setBlueToothCallback(BlueToothCallback blueToothCallback) {
        this.blueToothCallback = blueToothCallback;
        return this;
    }

    /***************************************蓝牙状态获取*********************************************/
    /**
     * 蓝牙是否开启
     * @return
     */
    public boolean isEnableBT() {
        isEnableBT = mBluetoothAdapter != null ? mBluetoothAdapter.isEnabled() : false;
        return isEnableBT;
    }

    /**
     * 获取正在连接的蓝牙设备
     * @return
     */
    public BluetoothDevice getNowConnectBluetoothDevice() {
        return isConnectSuccess ? bluetoothDevice : null;
    }












    /****************************************蓝牙的开启与关闭****************************************/

    /**
     * 开启蓝牙
     */
    public synchronized void enableBlueTooth(){
        if(isSupportBT && !isEnableBT){
            LogUtils.logD(TAG,"准备开启蓝牙");
            try {
                //首先直接开启蓝牙
                mBluetoothAdapter.enable();
                LogUtils.logD(TAG,"蓝牙开启成功");
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
        if(isSupportBT && isEnableBT){
            LogUtils.logD(TAG,"准备关闭蓝牙");
            try {
                //如果正在扫描的话则要停止扫描设备
                if(isScan){
                    stopScan();
                }
                //直接关闭蓝牙
                mBluetoothAdapter.disable();
                //清空设备记录
                deviceMap.clear();
            }catch (Exception e){
                LogUtils.logE(TAG,"蓝牙关闭失败，跳转到设置中关闭蓝牙");
                Setting.APPLICATION_CONTEXT.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }
    }



    /****************************************开启蓝牙扫描以及关闭蓝牙扫描******************************/

    /**
     * 开启设备扫描
     * @param deviceName 要查找的指定设备的名称
     * @param deviceAddress 要查找的指定设备的MAC地址
     * @param isOnlyScanBond 是否仅仅扫描已绑定设备
     */
    public synchronized void startScanBTDevice(String deviceName,String deviceAddress,boolean isOnlyScanBond) throws Exception {
        if(blueToothCallback == null){
            throw new Exception("请先设置接收回调，否则会导致无法接收扫描状态改变");
        }

        if(isSupportBT && isEnableBT && !isScan){
            LogUtils.logD(TAG,"准备开蓝牙扫描第一步，对已绑定设备进行扫描");
            //传递设备扫描的条件
            Message message = Message.obtain();
            message.what = SCAN_DEVICE_FILTER;
            if(deviceName == null){
                deviceName = "";
            }
            if(deviceAddress == null){
                deviceAddress = "";
            }
            Bundle bundle = new Bundle();
            bundle.putString("deviceName",deviceName);
            bundle.putString("deviceAddress",deviceAddress);
            message.setData(bundle);
            handler.sendMessage(message);

            //回传已绑定设备扫描开启
            handler.sendEmptyMessage(SCAN_BOND_START);

            //开始扫描已绑定设备
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            if(bondedDevices != null){
                Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
                BluetoothDevice bluetoothDevice;
                while (iterator.hasNext()){
                    bluetoothDevice = iterator.next();
                    if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress())){
                        message = Message.obtain();
                        message.what = SCAN_TO_DEVICE;
                        message.obj = bluetoothDevice;
                        handler.sendMessage(message);
                    }
                }
            }
            //完成已绑定设备扫描
            handler.sendEmptyMessage(SCAN_BOND_FINISH);
            LogUtils.logD(TAG,"已完成对于绑定设备的扫描");
            if(!isOnlyScanBond) {
                LogUtils.logD(TAG, "准备开蓝牙扫描第二步，开启广播扫描");
                startScanUnBond();
            }
        }
    }
    /**
     * 开启未绑定设备扫描
     */
    private synchronized void startScanUnBond(){
        mBluetoothAdapter.startDiscovery();
        LogUtils.logD(TAG, "开启指定时间之后暂停扫描");
        handler.sendEmptyMessageDelayed(STOP_SCAN_DEVICE_AND_CIRCULATION, 5000);
    }
    /**
     * 彻底停止扫描
     */
    public synchronized void stopScan(){
        stopScan(false);
    }
    /**
     * 停止扫描
     * @param isCirculation 是否循环开启扫描
     */
    private synchronized void stopScan(boolean isCirculation){
        if(isSupportBT && isEnableBT && isScan) {
            mBluetoothAdapter.cancelDiscovery();
            handler.removeMessages(STOP_SCAN_DEVICE_AND_CIRCULATION);
            handler.removeMessages(START_SCAN_DEVICE_AND_CIRCULATION);
            if (isCirculation) {
                LogUtils.logD(TAG,"暂停设备扫描,指定时间之后再次开启扫描");
                handler.sendEmptyMessageDelayed(START_SCAN_DEVICE_AND_CIRCULATION,5000);
            }else {
                LogUtils.logD(TAG,"彻底停止设备扫描");
            }
        }
    }





    /**************************************连接设备与关闭连接设备*************************************/

    /**
     * 连接蓝牙设备
     * @param bluetoothDevice
     * @return
     */
    public void connectBTDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice == null || TextUtils.isEmpty(bluetoothDevice.getAddress())){
            LogUtils.logD(TAG,"要连接的设备参数异常，无法进行连接");
            return;
        }
        if(!isSupportBT){
            LogUtils.logD(TAG,"设备不支持蓝牙，无法连接蓝牙设备");
            return;
        }
        if(!isEnableBT){
            LogUtils.logD(TAG,"设备未开启蓝牙，先开启蓝牙再进行连接");
            return;
        }
        if(isConnecting){
            LogUtils.logD(TAG,"已经有蓝牙设备正在发起连接，请先停止后再进行操作！");
            return;
        }
        if(isConnectSuccess){
            LogUtils.logD(TAG,"已经有蓝牙设备正在连接成功，请先断开后再进行操作！");
            return;
        }
        //直接连接设备
        this.bluetoothDevice = bluetoothDevice;
        connectBlueToothDevice(bluetoothDevice,true);
        return;
    }
    /**
     * 连接蓝牙设备
     * @param bluetoothDevice
     * @param stopReCon 是否结束上一次重连
     */
    private void connectBlueToothDevice(BluetoothDevice bluetoothDevice,boolean stopReCon){
        if(isRecon && stopReCon){
            //当正在重连同时需要停止重连的时候执行以下代码
            reconNum = -1;
            handler.removeMessages(CONNECT_BT_DEVICE_RECON);
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
            isConnecting = true;
            LogUtils.logD(TAG,"准备开始连接蓝牙设备");
            bluetoothDevice.connectGatt(Setting.APPLICATION_CONTEXT,false,bluetoothGattCallback);
        }
    }
    /**
     * 关闭蓝牙连接
     * @param bluetoothDevice
     */
    public void connectBTClose(BluetoothDevice bluetoothDevice){
        if(isConnectSuccess && bluetoothGatt != null){
            bluetoothGatt.disconnect();
            handler.sendEmptyMessage(CONNECT_BT_DEVICE_CLOSE);
        }
        this.bluetoothDevice = null;
        bluetoothGatt = null;
    }


    /*******************************************发送命令********************************************/

    /**
     * 向蓝牙设备发送指令并让蓝牙返回相应数据
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     */
    public void sendOrderToBTDeviceRead(UUID serviceUUid, @NonNull UUID characteristicUUid, byte[] optValue){
        LogUtils.logD(TAG,"准备向蓝牙设备发送读命令");
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(serviceUUid,characteristicUUid);
        if(characteristic != null){
            LogUtils.logD(TAG,"准备向蓝牙设备发送读命令");
            characteristic.setValue(optValue);
            boolean state = bluetoothGatt.readCharacteristic(characteristic);
            if(state){
                LogUtils.logD(TAG,"读命令发送成功");
            }else {
                LogUtils.logD(TAG,"读命令发送失败");
            }
        }
    }
    /**
     * 向蓝牙设备写入数据或者单纯的发送指令
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     * @param optValue 要发送的特殊数据指令
     */
    public void sendOrderToBTDeviceWrite(UUID serviceUUid, @NonNull UUID characteristicUUid, byte[] optValue){
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(serviceUUid,characteristicUUid);
        if (characteristic != null) {
            characteristic.setValue(optValue);
            boolean state = bluetoothGatt.writeCharacteristic(characteristic);
            if(state){
                LogUtils.logD(TAG,"写命令发送成功");
            }else {
                LogUtils.logD(TAG,"写命令发送失败");
            }
        }
    }
    /**
     * 向蓝牙设备发送通知命令
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     */
    public void sendOrderToBTDeviceNotify(UUID serviceUUid, @NonNull UUID characteristicUUid){
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(serviceUUid,characteristicUUid);
        if (characteristic != null) {
            boolean state = bluetoothGatt.setCharacteristicNotification(characteristic, true);
            if(state){
                LogUtils.logD(TAG,"通知命令发送成功");
            }else {
                LogUtils.logD(TAG,"通知命令发送失败");
            }

            List<BluetoothGattDescriptor> descriptors=characteristic.getDescriptors();
            for(BluetoothGattDescriptor dp:descriptors){
                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(dp);
            }
        }
    }
    /**
     * 根据指定的UUID获取蓝牙设备的通道
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     * @return
     */
    private BluetoothGattCharacteristic getBTGattCharForUUid(UUID serviceUUid, @NonNull UUID characteristicUUid){
        BluetoothGattCharacteristic characteristics = null;
        if(bluetoothGatt != null && characteristicUUid != null){
            if(serviceUUid != null){
                BluetoothGattService service = bluetoothGatt.getService(serviceUUid);
                if(service != null){
                    LogUtils.logD(TAG,"查找到对应服务值:::" + service.toString());
                    characteristics = service.getCharacteristic(characteristicUUid);
                    if (characteristics != null) {
                        LogUtils.logD(TAG, "查找到对应特征:::" + characteristics.toString() + "，对应特征属性:::" + characteristics.getProperties());
                    }
                    characteristicUUid = null;
                    serviceUUid = null;
                    service = null;
                }
            }else {
                Iterator<BluetoothGattService> iterator = bluetoothGatt.getServices().iterator();
                BluetoothGattService gattService;
                BluetoothGattCharacteristic characteristic = null;
                while (iterator.hasNext()) {
                    gattService = iterator.next();
                    characteristic = gattService.getCharacteristic(characteristicUUid);
                    if (characteristic != null) {
                        LogUtils.logD(TAG, "查找到对应特征:::" + characteristics.toString() + "，对应特征属性:::" + characteristics.getProperties());
                        gattService = null;
                        iterator = null;
                        characteristicUUid = null;
                        return characteristic;
                    }
                }
            }
        }
        return characteristics;
    }











    /*******************************************handler回调****************************************/
    private final int SCAN_TO_DEVICE = 0;//扫描到设备的回调msg.what
    private final int SCAN_DEVICE_FILTER = 1;//扫描设备的筛选条件，用，号区分
    private final int SCAN_BOND_START = 2;//已绑定设备开始蓝牙扫描
    private final int SCAN_BOND_FINISH = 3;//已绑定设备停止蓝牙扫描
    private final int SCAN_UN_BOND_START = 4;//未绑定设备开始蓝牙扫描
    private final int SCAN_UN_BOND_STOP = 5;//未绑定设备停止蓝牙扫描
    private final int STOP_SCAN_DEVICE_AND_CIRCULATION = 6;//停止设备扫描并在指定时间之后再次开启扫描
    private final int START_SCAN_DEVICE_AND_CIRCULATION = 7;//停止设备扫描并在指定时间之后再次开启扫描
    private final int BT_DEVICE_BOND_BONDING = 8;//正在配对
    private final int BT_DEVICE_BOND_BONDED = 9;//完成配对
    private final int BT_DEVICE_BOND_NONE = 10;//取消配对
    private final int CONNECT_BT_DEVICE_SUCCESS = 11;//连接设备成功
    private final int CONNECT_BT_DEVICE_FAIL = 13;//连接设备失败
    private final int CONNECT_BT_DEVICE_CLOSE = 14;//关闭设备连接
    private final int CONNECT_BT_DEVICE_RECON = 16;//蓝牙连接异常重新连接
    private final int ALLOW_SEND_ORDER_TO_BT_DEVICE = 15;//允许发送指令到设备
    private final int CONNECT_BT_DEVICE_RECON_MAX_NUM = 17;//蓝牙连接重连次数

    private int reconNum = 0;//重连次数
    private Handler handler = new Handler(){
        private String scanDeviceName = "";//扫描设备的名称
        private String scanDeviceAdress = "";//扫描设备的地址
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SCAN_DEVICE_FILTER://扫描设备的筛选条件
                    Bundle data = msg.getData();
                    if(data != null){
                        scanDeviceName = data.getString("scanDeviceName","");
                        scanDeviceAdress = data.getString("scanDeviceName","");
                    }
                    break;
                case SCAN_BOND_START://开始扫描绑定设备
                    if(blueToothCallback != null){
                        blueToothCallback.scaningBTDevice(true);
                    }
                    isScan = true;
                    break;
                case SCAN_BOND_FINISH://完成已绑定设备扫描
                    if(blueToothCallback != null){
                        blueToothCallback.stopScanBtDeveice(true);
                    }
                    break;
                case SCAN_UN_BOND_START://未绑定扫描开启
                    if(blueToothCallback != null){
                        blueToothCallback.scaningBTDevice(false);
                    }
                    break;
                case SCAN_UN_BOND_STOP://未绑定设备扫描停止
                    //判断是否是暂停扫描，暂停扫描不回传
                    if(!(hasMessages(STOP_SCAN_DEVICE_AND_CIRCULATION) || hasMessages(START_SCAN_DEVICE_AND_CIRCULATION))){
                        if(blueToothCallback != null){
                            blueToothCallback.scaningBTDevice(false);
                        }
                        isScan = false;
                    }
                    break;
                case STOP_SCAN_DEVICE_AND_CIRCULATION://停止设备扫描并在指定时间之后再次开启扫描
                    LogUtils.logD(TAG,"接收到暂停扫描请求，暂停扫描");
                    stopScan(true);
                    break;
                case START_SCAN_DEVICE_AND_CIRCULATION://开始设备扫描并在指定时间之后再次关闭扫描
                    LogUtils.logD(TAG,"从暂停扫描中接收到开始扫描请求，开始扫描");
                    startScanUnBond();
                    break;
                case SCAN_TO_DEVICE://扫描到设备
                    LogUtils.logD(TAG,"已扫描到设备，准备进行设备地址检测");
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                        LogUtils.logD(TAG,"已扫描到的设备有效，设备MAC地址：" + device.getAddress());

                        //先判断是否要筛选设备，如果要筛选设备的话在返回设备后就要停止扫描
                        if(!TextUtils.isEmpty(scanDeviceName) || !TextUtils.isEmpty(scanDeviceAdress)){
                            //判断地址是否筛选条件是否满足，满足就返回
                            if(TextUtils.equals(scanDeviceName,device.getName()) || TextUtils.equals(scanDeviceAdress,device.getAddress())){
                                stopScan();
                                if(blueToothCallback != null){
                                    blueToothCallback.scanBTDeviceResult(device);
                                }
                            }
                        }else {
                            if(blueToothCallback != null){
                                blueToothCallback.scanBTDeviceResult(device);
                            }
                        }
                    }else {
                        LogUtils.logD(TAG,"已扫描到的设备无效");
                    }
                    break;
                case CONNECT_BT_DEVICE_RECON:
                    LogUtils.logD(TAG,"蓝牙设备重连中");
                    reConBTDevice(bluetoothDevice);
                    if(blueToothCallback != null){
                        blueToothCallback.connectBTDeviceRecon();
                    }
                    break;
                case CONNECT_BT_DEVICE_SUCCESS:
                    LogUtils.logD(TAG,"蓝牙设备连接成功");
                    isConnectSuccess = true;
                    isConnecting = false;
                    if(blueToothCallback != null){
                        blueToothCallback.connectBTDeviceSuccess();
                    }
                    break;
                case CONNECT_BT_DEVICE_FAIL:
                    LogUtils.logD(TAG,"蓝牙设备连接失败");
                    isConnectSuccess = false;
                    isConnecting = false;
                    bluetoothDevice = null;
                    bluetoothGatt = null;
                    if(blueToothCallback != null){
                        blueToothCallback.connectBTDeviceFail();
                    }
                    break;
                case CONNECT_BT_DEVICE_CLOSE:
                    LogUtils.logD(TAG,"蓝牙设备连接关闭");
                    isConnectSuccess = false;
                    isConnecting = false;
                    bluetoothDevice = null;
                    bluetoothGatt = null;
                    if(blueToothCallback != null){
                        blueToothCallback.connectBTDeviceClose();
                    }
                    break;
                case ALLOW_SEND_ORDER_TO_BT_DEVICE:
                    LogUtils.logD(TAG,"蓝牙设备准备就绪");
                    if(blueToothCallback != null){
                        blueToothCallback.allowSendOrderToBTDevice();
                    }
                    break;
                default:
                    break;
            }
        }
    };



    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            bluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                handler.sendEmptyMessage(CONNECT_BT_DEVICE_SUCCESS);
                //获取蓝牙设备信息
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_FAILURE && newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnectSuccess = false;
                if(newState == BluetoothProfile.STATE_DISCONNECTED){
                    connectBTClose(bluetoothDevice);
                }else {
                    handler.sendEmptyMessage(CONNECT_BT_DEVICE_FAIL);
                }
            }else {
                //防止出现133错误
                if(gatt != null && status == 133) {
                    gatt.disconnect();
                    gatt.close();
                    bluetoothGatt = gatt = null;

                    //开启重连，并做判断，超过一定重连次数则不再进行重连
                    if(reconNum % CONNECT_BT_DEVICE_RECON_MAX_NUM > 0) {
                        isRecon = true;
                        handler.sendEmptyMessage(CONNECT_BT_DEVICE_RECON);
                    }else {
                        isRecon = false;
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            bluetoothGatt = gatt;
            if(status == BluetoothGatt.GATT_SUCCESS) {
                handler.sendEmptyMessage(ALLOW_SEND_ORDER_TO_BT_DEVICE);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            bluetoothGatt = gatt;
            if(status == BluetoothGatt.GATT_SUCCESS) {
                String hexStr = null;
                if(characteristic.getValue() != null){
                    hexStr = DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue());
                    LogUtils.logD(TAG,"接收到读命令返回数据:::" + hexStr);
                }
                if (blueToothCallback != null) {
                    blueToothCallback.onCharacteristicForReadOrderReceiveData(gatt,characteristic,hexStr);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            bluetoothGatt = gatt;
            if(status == BluetoothGatt.GATT_SUCCESS) {
                String hexStr = null;
                if(characteristic.getValue() != null){
                    hexStr = DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue());
                    LogUtils.logD(TAG,"接收到写命令返回数据:::" + hexStr);
                }
                if (blueToothCallback != null) {
                    blueToothCallback.onCharacteristicForWriteOrderReceiveData(gatt,characteristic,hexStr);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            bluetoothGatt = gatt;
            String hexStr = null;
            if(characteristic.getValue() != null){
                hexStr = DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue());
                LogUtils.logD(TAG,"接收到接收同志的特征改变返回数据:::" + hexStr);
            }
            if (blueToothCallback != null) {
                blueToothCallback.onCharacteristicChangeForNotifyOrderReceiveData(gatt,characteristic,hexStr);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            bluetoothGatt = gatt;
            if(status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.logD(TAG,"设置指定特征通知写命令返回");
                if (blueToothCallback != null) {
                    blueToothCallback.onCharacteristicForNotifyReadOrderReceiveData(gatt,descriptor);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            bluetoothGatt = gatt;
            if(status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.logD(TAG,"设置指定特征通知写命令返回");
                if (blueToothCallback != null) {
                    blueToothCallback.onCharacteristicForNotifyWriteOrderReceiveData(gatt,descriptor);
                }
            }
        }
    };
}
