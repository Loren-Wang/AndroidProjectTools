package com.adnroidprojecttools.blueToothOptions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by LorenWang on 2018/4/11.
 * 创建时间：2018/4/11 12:02
 * 创建人：王亮（Loren wang）
 * 功能作用：
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */


public class BlueToothOptionUtils {
    private final String TAG = getClass().getName();
    private static BlueToothOptionUtils blueToothOptionUtils;
    private BluetoothAdapter mBluetoothAdapter;
    private BlueToothDeviceReceiver blueToothDeviceReceiver;//蓝牙设备扫描到的广播接收器
    //蓝牙操作回调
    private BlueToothOptionsCallback blueToothOptionsCallback = new BlueToothOptionsCallback() {
        @Override
        protected boolean scanResultJudge(BluetoothDevice bluetoothDevice) {
            return super.scanResultJudge(bluetoothDevice);
        }

        @Override
        protected void connectBTDeviceSuccess(BluetoothGatt bluetoothGatt) {

        }

        @Override
        protected void connectBTDeviceFail(BluetoothGatt bluetoothGatt) {

        }

        @Override
        protected void reConnectBTDevice() {

        }

        @Override
        protected void connectBTClose(BluetoothGatt bluetoothGatt) {

        }

        @Override
        protected void onBTDeviceReadCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {

        }

        @Override
        protected void onBTDeviceWriteCallback(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {

        }

        @Override
        protected void onBTDeviceDescriptorWriteCallback(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {

        }

        @Override
        protected void onBTDeviceDescriptorReadCallback(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor) {

        }

        @Override
        protected void allowSenOrderToBTDevice(BluetoothGatt bluetoothGatt) {

        }
    };
    //蓝牙设备状态回调,私有，仅在单例类中使用
    private BlueToothDeviceStateCallback blueToothDeviceStateCallback = new BlueToothDeviceStateCallback(){};
    private boolean isSupportBT = false;//设备是否支持蓝牙
    private boolean isEnableBT = false;//蓝牙是否开启
    private boolean isScan = false;//是否正在扫描蓝牙设备
    private Map<String,BluetoothDevice> deviceMap = new HashMap<>();//蓝牙设备记录


    private final int STOP_SCAN_DEVICE_AND_CIRCULATION = 0;//停止设备扫描并在指定时间之后再次开启扫描
    private final int START_SCAN_DEVICE = 1;//开启设备扫描
    private final int STOP_SCAN_DEVICE = 2;//停止设备扫描
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STOP_SCAN_DEVICE_AND_CIRCULATION:
                    //指定时间之后再次开启扫描
                    sendEmptyMessageDelayed(START_SCAN_DEVICE, 10000);
                case STOP_SCAN_DEVICE:
                    stopScan();
                    break;
                case START_SCAN_DEVICE://开启设备扫描
                    startScan();
                    break;
                default:
                    break;
            }
        }


        /**
         * 开启蓝牙设备扫描
         * @return
         */
        private synchronized void startScan(){
            if(isSupportBT && isEnableBT && !isScan){
                LogUtils.logD(TAG,"蓝牙设备扫描开启准备，先获取已连接绑定设备检测");
                //清空就数据
                deviceMap.clear();
                //获取已绑定设备
                Iterator<BluetoothDevice> iterator = mBluetoothAdapter.getBondedDevices().iterator();
                BluetoothDevice bluetoothDevice;
                while (iterator.hasNext()){
                    bluetoothDevice = iterator.next();
                    deviceMap.put(bluetoothDevice.getAddress(),bluetoothDevice);
                    boolean state = blueToothOptionsCallback.scanResultJudge(bluetoothDevice);
                    if(state){
                        //是要找的扫描结果，找到了直接返回，不进行另外扫描
                        deviceMap.clear();
                        LogUtils.logD(TAG,"在已绑定设备中查找到了指定设备，停止扫描");
                        stopScan();
                        return;
                    }
                }
                //没有在已绑定设备中找到，开启设备扫描
                mBluetoothAdapter.startDiscovery();
                LogUtils.logD(TAG,"正式开启蓝牙设备扫描");
                isScan = true;

                //开启指定时间之后自动关闭
                sendEmptyMessageDelayed(STOP_SCAN_DEVICE_AND_CIRCULATION,5000);
            }
        }

        /**
         * 停止蓝牙设备扫描
         * @return
         */
        private synchronized void stopScan(){
            if(isSupportBT && isEnableBT && isScan){
                LogUtils.logD(TAG,"蓝牙设备停止扫描准备");
                blueToothDeviceReceiver = null;
                mBluetoothAdapter.cancelDiscovery();
                isScan = false;
            }
        }
    };

    @SuppressLint("MissingPermission")
    public static BlueToothOptionUtils getInstance(){
        if(blueToothOptionUtils == null){
            blueToothOptionUtils = new BlueToothOptionUtils();
        }
        return blueToothOptionUtils;
    }

    /**
     * 实例化蓝牙设备
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_COARSE_LOCATION})
    private BlueToothOptionUtils() {
        if (ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LogUtils.logD(TAG, "设备拥有蓝牙权限");
            // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
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

            //注册广播接收器
            blueToothDeviceReceiver = new BlueToothDeviceReceiver(this,deviceMap);
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
            Setting.APPLICATION_CONTEXT.registerReceiver(blueToothDeviceReceiver, intent);


        }else {
            LogUtils.logD(TAG,"设备未拥有蓝牙权限以及定位，需要申请蓝牙权限以及定位权限");
        }
    }

    /**
     * 设置蓝牙操作回调
     * @param blueToothOptionsCallback
     * @return
     */
    public BlueToothOptionUtils setBlueToothOptionsCallback(BlueToothOptionsCallback blueToothOptionsCallback) {
        if(blueToothOptionsCallback != null) {
            this.blueToothOptionsCallback = blueToothOptionsCallback;
            blueToothDeviceStateCallback.setBlueToothOptionsCallback(blueToothOptionsCallback);
        }
        return this;
    }
    public BlueToothOptionsCallback getBlueToothOptionsCallback() {
        return blueToothOptionsCallback;
    }

    public BlueToothDeviceStateCallback getBlueToothDeviceStateCallback() {
        return blueToothDeviceStateCallback;
    }

    /**
     * 开启蓝牙
     */
    public synchronized BlueToothOptionUtils enableBlueTooth(){
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
        return this;
    }
    /**
     * 关闭蓝牙
     */
    public synchronized BlueToothOptionUtils disableBlueTooth(){
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
        return this;
    }



    /**
     * 开启蓝牙设备扫描
     * @return
     */
    public BlueToothOptionUtils startScan(){
        handler.sendEmptyMessage(START_SCAN_DEVICE);
        return this;
    }
    /**
     * 停止蓝牙设备扫描
     * @return
     */
    public BlueToothOptionUtils stopScan(){
        handler.removeMessages(START_SCAN_DEVICE);
        handler.removeMessages(STOP_SCAN_DEVICE_AND_CIRCULATION);
        handler.sendEmptyMessage(STOP_SCAN_DEVICE);
        return this;
    }



    /**
     * 连接蓝牙设备
     * @param bluetoothDevice
     * @return
     */
    public BlueToothOptionUtils connectBTDevice(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice == null || TextUtils.isEmpty(bluetoothDevice.getAddress())){
            LogUtils.logD(TAG,"要连接的设备参数异常，无法进行连接");
            return this;
        }
        if(!isSupportBT){
            LogUtils.logD(TAG,"设备不支持蓝牙，无法连接蓝牙设备");
            return this;
        }
        if(!isEnableBT){
            LogUtils.logD(TAG,"设备未开启蓝牙，先开启蓝牙再进行连接");
            return this;
        }
        if(blueToothDeviceStateCallback.getBluetoothDevice() != null && blueToothDeviceStateCallback.isConnectSuccess()){
            LogUtils.logD(TAG,"已经有蓝牙设备正在连接，请先关闭连接后再进行操作！");
            return this;
        }

        //直接连接设备
        blueToothDeviceStateCallback.connectBlueToothDevice(bluetoothDevice,true);

//        if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
//            LogUtils.logD(TAG,"该蓝牙设备未绑定，需要先绑定设备");
//
//            createBond(bluetoothDevice.getClass(), bluetoothDevice);
//        }else {
//            blueToothDeviceStateCallback.connectBlueToothDevice(bluetoothDevice,true);
//        }

        return this;
    }
    /**
     * 关闭连接
     * @param bluetoothDevice
     * @return
     */
    public BlueToothOptionUtils connectBTClose(BluetoothDevice bluetoothDevice){
        if(bluetoothDevice == null || TextUtils.isEmpty(bluetoothDevice.getAddress())){
            LogUtils.logD(TAG,"要连接的设备参数异常，无法进行连接");
            return this;
        }
        if(!isSupportBT){
            LogUtils.logD(TAG,"设备不支持蓝牙，无法连接蓝牙设备");
            return this;
        }
        if(!isEnableBT){
            LogUtils.logD(TAG,"设备未开启蓝牙，先开启蓝牙再进行连接");
            return this;
        }
        blueToothDeviceStateCallback.connectBTClose(bluetoothDevice);
        return this;
    }





    /**
     * 向蓝牙设备发送指令并让蓝牙返回相应数据
     * @param bluetoothGatt
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     */
    public void sendOrderToBTDeviceRead(BluetoothGatt bluetoothGatt,UUID serviceUUid, @NonNull UUID characteristicUUid, byte[] optValue){
        LogUtils.logD(TAG,"准备向蓝牙设备发送读命令");
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(bluetoothGatt, serviceUUid,characteristicUUid);
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
     * @param bluetoothGatt
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     * @param optValue 要发送的特殊数据指令
     */
    public void sendOrderToBTDeviceWrite(BluetoothGatt bluetoothGatt, UUID serviceUUid, @NonNull UUID characteristicUUid, byte[] optValue){
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(bluetoothGatt, serviceUUid,characteristicUUid);
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
     * @param bluetoothGatt
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     */
    public void sendOrderToBTDeviceNotify(BluetoothGatt bluetoothGatt, UUID serviceUUid, @NonNull UUID characteristicUUid){
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(bluetoothGatt, serviceUUid,characteristicUUid);
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
     * @param bluetoothGatt
     * @param serviceUUid 服务UUID
     * @param characteristicUUid 特征UUID
     * @return
     */
    private BluetoothGattCharacteristic getBTGattCharForUUid(BluetoothGatt bluetoothGatt,UUID serviceUUid, @NonNull UUID characteristicUUid){
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







    /**
     * 连接设备
     * @param btDevice
     * @return
     * @throws Exception
     */
    public boolean createBond(Class btClass, BluetoothDevice btDevice){
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btClass.getMethod("createBond");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logD(TAG, "向蓝牙设备发起配对请求成功");
                }else {
                    LogUtils.logD(TAG, "向蓝牙设备发起配对请求失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "向蓝牙设备发起配对请求失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "向蓝牙设备发起配对请求失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue.booleanValue();
    }
    /**
     * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
     * \Settings\src\com\android\settings\bluetooth\CachedBluetoothDevice.java
     */
    private boolean removeBond(Class btClass,BluetoothDevice btDevice) throws Exception {
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btClass.getMethod("removeBond");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logD(TAG, "蓝牙设备解绑成功");
                }else {
                    LogUtils.logD(TAG, "蓝牙设备解绑失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备解绑失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备解绑失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue;
    }
    /**
     * 取消配对
     * @param btDevice
     * @return
     */
    public boolean cancelBondProcess(Class btClass,BluetoothDevice btDevice){
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btClass.getMethod("cancelBondProcess");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logE(TAG, "蓝牙设备取消配对成功");
                }else {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue.booleanValue();
    }
    /**
     * 发送密码
     * @param btDevice
     * @param str
     * @return
     */
    public boolean setPin(Class btClass, BluetoothDevice btDevice, String str){
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btClass.getDeclaredMethod("setPin",new Class[]{byte[].class});
                returnValue = (Boolean) createBondMethod.invoke(btDevice,new Object[]{str.getBytes()});
                if(returnValue){
                    LogUtils.logD(TAG, "蓝牙设备发送密码成功");
                }else {
                    LogUtils.logD(TAG, "蓝牙设备发送密码失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备发送密码失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备发送密码失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue.booleanValue();
    }
    // 取消用户输入
    public boolean cancelPairingUserInput(Class btClass, BluetoothDevice btDevice) throws Exception {
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logE(TAG, "蓝牙设备取消配对成功");
                }else {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备取消配对失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue.booleanValue();
    }
    /**
     * 确认配对
     * @param btClass
     * @param device
     * @param isConfirm
     * @return
     */
    public boolean setPairingConfirmation(Class<?> btClass,BluetoothDevice device,boolean isConfirm){
        Boolean returnValue = false;
        if(device != null) {
            try {
                Method createBondMethod = btClass.getDeclaredMethod("setPairingConfirmation",boolean.class);
                returnValue = (Boolean) createBondMethod.invoke(device,isConfirm);
                if(returnValue){
                    LogUtils.logD(TAG, "蓝牙设备确认配对成功");
                }else {
                    LogUtils.logD(TAG, "蓝牙设备确认配对失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备确认配对失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备确认配对失败");
                }
            }
        }else{
            LogUtils.logE(TAG,"传入的蓝牙设备参数为空");
        }
        return returnValue.booleanValue();

    }

}
