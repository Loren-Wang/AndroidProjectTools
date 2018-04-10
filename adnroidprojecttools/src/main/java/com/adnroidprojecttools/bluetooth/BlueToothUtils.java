package com.adnroidprojecttools.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.adnroidprojecttools.common.LogUtils;
import com.adnroidprojecttools.common.Setting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by LorenWang on 2018/4/9.
 * 创建时间：2018/4/9 13:55
 * 创建人：王亮（Loren wang）
 * 功能作用：蓝牙操作工具类
 * 方法：1、检测设备是否支持蓝牙
 *      2、检测蓝牙设备是否开启
 *      3、开启蓝牙
 *      4、关闭蓝牙
 *      5、开启或者关闭扫描
 *      6、连接指定的设备
 *      7、绑定蓝牙设备
 *      8、解除绑定蓝牙设备
 *      9、向蓝牙设备发送指令并让蓝牙返回相应数据
 *      10、向蓝牙设备写入数据或者单纯的发送指令
 * 思路：首先在创建实例的时候检测蓝牙权限并获取蓝牙适配器，之后通过公共方法开启扫描，获取到的结果存储在工具类中，
 *      通过get方法可以读取，或者指定读取某一类型的设备，同时通过某一个类型的设备的某一个通道进行数据信息的发送以及接受
 *      在关闭蓝牙的时候回将蓝牙适配器以及蓝牙的单例销毁
 *
 * 注意：扫描蓝牙必须要拥有蓝牙权限以及定位权限
 * 修改人：
 * 修改时间：
 * 备注：
 */

public class BlueToothUtils {
    private final String TAG = getClass().getName();
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
//    private BluetoothAdapter.LeScanCallback mLeScanCallback;//蓝牙扫描回调
//    private ScanCallback scanCallback;//蓝牙扫描回调(大于21安卓版本使用）
    private boolean isScan = false;//是否正在扫描
    private Map<String,BluetoothDevice> deviceMap = new HashMap<>();//扫描到的设备记录
    private BluetoothDevice connectedDevice;//记录已经连接的设备
    private BlueToothStateListener blueToothStateListener;

    private final int STOP_SCAN_DEVICE_AND_CIRCULATION = 0;//停止设备扫描并在指定时间之后再次开启扫描
    private final int START_SCAN_DEVICE = 1;//开启设备扫描
    private final int STOP_SCAN_DEVICE = 2;//停止设备扫描
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case STOP_SCAN_DEVICE_AND_CIRCULATION:
                    //指定时间之后再次开启扫描
                    sendEmptyMessageDelayed(START_SCAN_DEVICE,10000);
                case STOP_SCAN_DEVICE:
                    scanDevice(true);//停止设备扫描
                    break;
                case START_SCAN_DEVICE://开启设备扫描
                    scanDevice(false);
                    break;
                default:
                    break;
            }
        }
    };

    /********************************************蓝牙创建以及初始化***********************************/

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_COARSE_LOCATION})
    public BlueToothUtils(){
        if(ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(Setting.APPLICATION_CONTEXT, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LogUtils.logD(TAG,"设备拥有蓝牙权限");
            // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
            final BluetoothManager bluetoothManager = (BluetoothManager) Setting.APPLICATION_CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // 注册广播接收器。
            // 接收蓝牙发现
            BlueToothDeviceReceiver blueToothDeviceReceiver = new BlueToothDeviceReceiver(deviceMap);
            Setting.APPLICATION_CONTEXT.registerReceiver(blueToothDeviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            Setting.APPLICATION_CONTEXT.registerReceiver(blueToothDeviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            Setting.APPLICATION_CONTEXT.registerReceiver(blueToothDeviceReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

            blueToothStateListener = new BlueToothStateListener() {
                @Override
                protected void startScan() {

                }

                @Override
                protected void stopScan(List<BluetoothDevice> deviceList) {

                }

                @Override
                protected void connectSuccess(BluetoothGatt gatt) {

                }

                @Override
                protected void connectFail(BluetoothGatt gatt) {

                }

                @Override
                protected void allowCommunication(BluetoothGatt gatt) {

                }
            };
        }else {
            LogUtils.logD(TAG,"设备未拥有蓝牙权限以及定位，需要申请蓝牙权限以及定位权限");
        }
    }

    /**
     * 设置蓝牙监听
     * @param blueToothStateListener
     * @return
     */
    public BlueToothUtils setBlueToothStateListener(BlueToothStateListener blueToothStateListener) {
        if(blueToothStateListener != null) {
            this.blueToothStateListener = blueToothStateListener;
        }
        return this;
    }


    /***************************************蓝牙开关以及属性检测、扫描*********************************/

    /**
     * 检测设备是否支持蓝牙
     * @return
     */
    private boolean checkIsSupportBlueTooth(){
        if(mBluetoothAdapter == null){
            LogUtils.logD(TAG,"设备不支持蓝牙");
            return false;
        }else {
            LogUtils.logD(TAG,"设备支持蓝牙");
            return true;
        }
    }
    /**
     * 检测蓝牙是否开启
     * @return
     */
    private boolean checkIsBlueToothEnabled(){
        if(checkIsSupportBlueTooth()){
            if(mBluetoothAdapter.isEnabled()){
                LogUtils.logD(TAG,"蓝牙设备是已开启状态");
                return true;
            }else {
                LogUtils.logD(TAG,"蓝牙设备未开启");
                return false;
            }
        }else {
            return false;
        }
    }
    /**
     * 开启蓝牙
     */
    public synchronized BlueToothUtils enableBlueTooth(){
        if(!checkIsBlueToothEnabled()){
            LogUtils.logD(TAG,"准备开启蓝牙");
            try {
                //首先直接开启蓝牙
                mBluetoothAdapter.enable();
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
    public synchronized BlueToothUtils disableBlueTooth(){
        if(checkIsBlueToothEnabled()){
            LogUtils.logD(TAG,"准备关闭蓝牙");
            try {
                //如果正在扫描的话则要停止扫描设备
                if(isScan){
                    scanDevice(true);
                }
                //直接关闭蓝牙
                mBluetoothAdapter.disable();
                //清空设备记录
                deviceMap.clear();
                //删除已连接设备
                connectedDevice = null;
            }catch (Exception e){
                LogUtils.logE(TAG,"蓝牙关闭失败，跳转到设置中关闭蓝牙");
                Setting.APPLICATION_CONTEXT.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        }
        return this;
    }
    /**
     * 扫描设备
     * @param isStop 是否停止扫描
     * @return
     */
    public BlueToothUtils scanDevice(boolean isStop){
        if(checkIsBlueToothEnabled()) {
            if (isScan && isStop) {
                //如果正在扫描同时要停止扫描的话
                LogUtils.logD(TAG, "当前正在扫描，准备关闭设备扫描");
                //关闭扫描
                mBluetoothAdapter.cancelDiscovery();
                isScan = false;
                blueToothStateListener.stopScan(new ArrayList<BluetoothDevice>(deviceMap.values()));


                /****************************************以下是旧版扫描******************************/
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
//                    scanCallback = null;
//                }else {
//                    mBluetoothAdapter.startLeScan(mLeScanCallback);
//                    mLeScanCallback = null;
//                }
                /****************************************以上是旧版扫描******************************/
            } else if (!isScan && !isStop) {
                LogUtils.logD(TAG, "当前没有进行扫描，准备开启设备扫描");
                //开启扫描
                mBluetoothAdapter.startDiscovery();
                isScan = true;
                deviceMap.clear();//清空旧表
                //添加已绑定设备
                Iterator<BluetoothDevice> iterator = mBluetoothAdapter.getBondedDevices().iterator();
                BluetoothDevice bluetoothDevice;
                while (iterator.hasNext()){
                    bluetoothDevice = iterator.next();
                    if(bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.getAddress())){
                        deviceMap.put(bluetoothDevice.getAddress(),bluetoothDevice);
                    }
                }
                blueToothStateListener.startScan();
                /****************************************以下是旧版扫描******************************/
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    scanCallback = new ScanCallback() {
//                        @Override
//                        public void onScanResult(int callbackType, ScanResult result) {
//                            super.onScanResult(callbackType, result);
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                if(result != null && result.getDevice() != null && !TextUtils.isEmpty(result.getDevice().getAddress())){
//                                    if(result.getDevice().getName() != null) {
//                                        LogUtils.logD(TAG, "扫描到设备：" + result.getDevice().getName());
//                                    }
//                                    deviceMap.put(result.getDevice().getAddress(),result.getDevice());
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onBatchScanResults(List<ScanResult> results) {
//                            super.onBatchScanResults(results);
//                            Iterator<ScanResult> iterator = results.iterator();
//                            BluetoothDevice bluetoothDevice;
//                            while (iterator.hasNext()){
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                    bluetoothDevice = iterator.next().getDevice();
//                                    if(bluetoothDevice.getName() != null) {
//                                        LogUtils.logD(TAG, "扫描到设备：：" + bluetoothDevice.getName());
//                                    }
//                                    deviceMap.put(bluetoothDevice.getAddress(),bluetoothDevice);
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onScanFailed(int errorCode) {
//                            super.onScanFailed(errorCode);
//                        }
//                    };
//                    mBluetoothAdapter.startDiscovery();
//                    ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
//                    mBluetoothAdapter.getBluetoothLeScanner().startScan(null, scanSettings,scanCallback);
//                }else {
//                    mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//                        @Override
//                        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//                            if(device != null && !TextUtils.isEmpty(device.getAddress())) {
//                                deviceMap.put(device.getAddress(), device);
//                            }
//                        }
//                    };
//                    mBluetoothAdapter.startLeScan(mLeScanCallback);
//                }

                //指定时间之后停止扫描
                /****************************************以上是旧版扫描******************************/
                handler.sendEmptyMessageDelayed(STOP_SCAN_DEVICE, 10000);
            }
        }
        return this;
    }


    /*****************************************蓝牙连接以及绑定解绑************************************/

    /**
     * 连接设备
     * 逻辑：先判断蓝牙是否开启或者是否支持蓝牙
     *      判断传入的蓝牙设备参数是否为空
     *      判断传入的蓝牙设备是否是已经被当前手机app连接
     *      开始连接
     * @param bluetoothDevice
     */
    public synchronized void connectDevice(BluetoothDevice bluetoothDevice){
        if(!checkIsBlueToothEnabled()){
            LogUtils.logE(TAG,"蓝牙设备未开启或不支持蓝牙设备，无法连接");
        }
        if(bluetoothDevice == null || TextUtils.isEmpty(bluetoothDevice.getAddress())){
            LogUtils.logE(TAG,"传入的设备数据为空，或设备地址为空，无法连接");
        }
        if(connectedDevice != null && TextUtils.equals(connectedDevice.getAddress(),bluetoothDevice.getAddress())){
            LogUtils.logD(TAG,"要连接的设备已经被连接了，直接使用即可");
        }
        if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE){
            //设备没有绑定，需要先绑定设备
            boolean bond = createBond(bluetoothDevice);
            if(bond){
                //绑定成功
                blueToothStateListener.connectBlueToothDevice(bluetoothDevice, true);
            }
        }else  if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            //设备已经绑定，开始连接设备
            blueToothStateListener.connectBlueToothDevice(bluetoothDevice, true);
        }

    }
    /**
     * 连接设备
     * @param btDevice
     * @return
     * @throws Exception
     */
    private boolean createBond(BluetoothDevice btDevice){
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btDevice.getClass().getMethod("createBond");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logE(TAG, "蓝牙设备绑定成功");
                }else {
                    LogUtils.logE(TAG, "蓝牙设备绑定失败");
                }
            } catch (Exception e) {
                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                    LogUtils.logE(TAG, "蓝牙设备绑定失败:::" + e.getMessage());
                } else {
                    LogUtils.logE(TAG, "蓝牙设备绑定失败");
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
    private boolean removeBond(BluetoothDevice btDevice) throws Exception {
        Boolean returnValue = false;
        if(btDevice != null) {
            try {
                Method createBondMethod = btDevice.getClass().getMethod("removeBond");
                returnValue = (Boolean) createBondMethod.invoke(btDevice);
                if(returnValue){
                    LogUtils.logE(TAG, "蓝牙设备解绑成功");
                }else {
                    LogUtils.logE(TAG, "蓝牙设备解绑失败");
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
     * 向蓝牙设备发送指令并让蓝牙返回相应数据
     * @param bluetoothGatt
     * @param uuidStr
     */
    public void sendOrderToBTDeviceAndResultData(BluetoothGatt bluetoothGatt,String uuidStr){
        BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(bluetoothGatt, uuidStr);
        if(characteristic != null){
            bluetoothGatt.readCharacteristic(characteristic);
        }
    }

    /**
     * 向蓝牙设备写入数据或者单纯的发送指令
     * @param bluetoothGatt
     * @param uuidStr
     * @param optValue 要发送的特殊数据指令
     */
    public void sendOrderToBTDevice(BluetoothGatt bluetoothGatt,String uuidStr,String optValue){
        if(!TextUtils.isEmpty(optValue)) {
            BluetoothGattCharacteristic characteristic = getBTGattCharForUUid(bluetoothGatt, uuidStr);
            if (characteristic != null) {
                characteristic.setValue(hexStr2Bytes(optValue));
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        }
    }

    /**
     * 根据指定的UUID获取蓝牙设备的通道
     * @param bluetoothGatt
     * @param uuidStr
     * @return
     */
    private BluetoothGattCharacteristic getBTGattCharForUUid(BluetoothGatt bluetoothGatt,String uuidStr){
        if(bluetoothGatt != null && !TextUtils.isEmpty(uuidStr)){
            UUID uuid = null;
            try {
                uuid = UUID.fromString(uuidStr);
            }catch (Exception e){
                LogUtils.logE(TAG,"UUID 格式化失败");
                return null;
            }
            Iterator<BluetoothGattService> iterator = bluetoothGatt.getServices().iterator();
            BluetoothGattService gattService;
            BluetoothGattCharacteristic characteristic = null;
            while (iterator.hasNext()) {
                gattService = iterator.next();
                characteristic = gattService.getCharacteristic(uuid);
                if (characteristic != null) {
                    gattService = null;
                    iterator = null;
                    uuid = null;
                    return characteristic;
                }
            }
            return characteristic;
        }else {
            return null;
        }
    }

    /**
     * bytes字符串转换为Byte值
     */
    private byte[] hexStr2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
