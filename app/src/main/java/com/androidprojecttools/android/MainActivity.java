package com.androidprojecttools.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lorenwang.customviews.android.FancyCoverFlow.FancyCoverFlow;
import com.lorenwang.tools.android.mobile.MobilePhoneBrandUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FancyCoverFlow mFancyCoverFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
//                    , Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
//        }
//
//        ((SudokuSwipeGesturesView)findViewById(R.id.test)).setInputStateChangeCallback(new SudokuSwipeGesturesView.InputStateChangeCallback() {
//            @Override
//            public void finishInput(int[] posis) {
////                ((SudokuSwipeGesturesView)findViewById(R.id.test)).resetAll();
////                ((SudokuSwipeGesturesView)findViewById(R.id.test)).setAllowDraw(false);
//            }
//        });
//
//        mFancyCoverFlow = (FancyCoverFlow) findViewById(R.id.facyCoverFlow);

        MobilePhoneBrandUtils.getInstance().isMeiZuMobile();
        MobilePhoneBrandUtils.getInstance().isXiaoMiMobile();
        MobilePhoneBrandUtils.getInstance().isHuaWeiMobile();
//        sample();
    }
//    private void sample() {
//        //设置数据
//        this.mFancyCoverFlow.setAdapter(new FancyCoverFlowSampleAdapter());
//        //设置未选中的图的透明度
//        this.mFancyCoverFlow.setUnselectedAlpha(1.0f);
//        //设置未选中的图的色彩饱和度
//        this.mFancyCoverFlow.setUnselectedSaturation(0.0f);
//        //未被选中的图像的缩放比例
//        this.mFancyCoverFlow.setUnselectedScale(0.5f);
//        //设置两个图之间的比例
//        this.mFancyCoverFlow.setSpacing(50);
//        //设置未选中图像的最大旋转角度
//        this.mFancyCoverFlow.setMaxRotation(0);
//        //设置未被 选中图像的下沉度
//        this.mFancyCoverFlow.setScaleDownGravity(0.2f);
//        this.mFancyCoverFlow
//                .setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);
//    }
//
//    public class FancyCoverFlowSampleAdapter extends FancyCoverFlowAdapter {
//
//        //加载显示图片的资源
//        private int[] images = {R.drawable.ic_launcher_background, R.mipmap.ic_launcher, R.mipmap.ic_launcher_round
//                , R.drawable.ic_launcher_background, R.mipmap.ic_launcher, R.mipmap.ic_launcher_round};
//
//        @Override
//        public int getCount() {
//            return images.length;
//        }
//
//        @Override
//        public Integer getItem(int i) {
//            return images[i];
//        }
//
//        @Override
//        public long getItemId(int i) {
//            return i;
//        }
//
//        @Override
//        public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
//            ImageView imageView = null;
//
//            if (reuseableView != null) {
//                imageView = (ImageView) reuseableView;
//            } else {
//                imageView = new ImageView(viewGroup.getContext());
//                //设置图像在中心显示
//                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//                //设置图像的显示尺寸
//                imageView.setLayoutParams(new FancyCoverFlow.LayoutParams(300, 400));
//
//            }
//            //显示图像
//            imageView.setImageResource(this.getItem(i));
//            return imageView;
//        }
//    }
//
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        // If request is cancelled, the result arrays are empty.
//        List<String> successPermissionList = new ArrayList<>();
//        List<String> failPermissionList = new ArrayList<>();
//
//        if (grantResults.length > 0 && grantResults.length == permissions.length) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    successPermissionList.add(permissions[i]);
//                    LogUtils.logI("用户同意权限", "user granted the permission!" + permissions[i]);
//                } else {
//                    LogUtils.logI("用户不同意权限", "user denied the permission!" + permissions[i]);
//                    failPermissionList.add(permissions[i]);
//                }
//            }
//        } else {
//            for (int i = 0; i < permissions.length; i++) {
//                failPermissionList.add(permissions[i]);
//            }
//        }
//        if (failPermissionList.size() == 0) {
//            final UUID serviceUUid = CarAirBlueToothInfo.getInstance().paramUUid("0001");
//            final UUID characteristicUUid02 = CarAirBlueToothInfo.getInstance().paramUUid("0002");
//            final UUID characteristicUUid03 = CarAirBlueToothInfo.getInstance().paramUUid("0003");
//
//            final int SEND_ORDER_FOR_WRITE_GET_DATA = 0;
//            final Handler handler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    switch (msg.what) {
//                        case SEND_ORDER_FOR_WRITE_GET_DATA:
//                            BlueToothOptionsUtils.getInstance(getApplicationContext()).sendOrderToBTDeviceWrite(serviceUUid, characteristicUUid02
//                                    , new byte[]{0x55, 0x01, 0x01, 0x02});
//                            sendEmptyMessageDelayed(SEND_ORDER_FOR_WRITE_GET_DATA, 2000);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            };
//
//
//            BlueToothOptionsUtils.getInstance(getApplicationContext()).setBlueToothOptionsCallback(new BlueToothOptionsCallback() {
//                @Override
//                public void systemBTDeviceStateChange(boolean isOpen) {
//                    if(!isOpen){
//                        BlueToothOptionsUtils.getInstance(getApplicationContext()).enableBlueTooth();
//                    }else {
//                        BlueToothOptionsUtils.getInstance(getApplicationContext()).startScanBTDevice(null,null);
//                    }
//                }
//
//                @Override
//                public void scanBTDevice(boolean isHaveError, boolean isScan) {
//
//                }
//
//                @Override
//                public void scanFoundBlueToothDevice(BluetoothDevice bluetoothDevice) {
//                    if (TextUtils.equals(bluetoothDevice.getAddress(),"C6:AC:81:D3:2E:49")) {
//                        BlueToothOptionsUtils.getInstance(getApplicationContext()).stopScanBTDevice();
//                        BlueToothOptionsUtils.getInstance(getApplicationContext()).connectBlueToothDevice(bluetoothDevice,true);
//                    }
//                }
//
//                @Override
//                public void disconnectBtDevice() {
//
//                }
//
//                @Override
//                public void connectBtDevice() {
//
//                }
//
//                @Override
//                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                    BlueToothOptionsUtils.getInstance(getApplicationContext()).sendOrderToBTDeviceNotify(serviceUUid, characteristicUUid03);
//                }
//
//                @Override
//                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, Integer status) {
//
//                }
//
//                @Override
//                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, Integer status) {
//
//                }
//
//                @Override
//                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//                    String hexStr = null;
//                    if(characteristic.getValue() != null){
//                        hexStr = DigitalTransUtils.getInstance().byte2HexStr(characteristic.getValue());
//                        LogUtils.logD(TAG,"接收到接收同志的特征改变返回数据:::" + hexStr);
//                    }
//                }
//
//                @Override
//                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, Integer status) {
//
//                }
//
//                @Override
//                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, Integer status) {
//                    handler.sendEmptyMessage(SEND_ORDER_FOR_WRITE_GET_DATA);
//                }
//            }).startScanBTDevice(null,null);
//
//
////
////
////
////            BlueToothUtils.getInstance().setBlueToothCallback(new BlueToothCallback() {
////                @Override
////                public void enableBT() {
////
////                }
////
////                @Override
////                public void disableBT() {
////
////                }
////
////                @Override
////                public void scaningBTDevice(boolean isBondScan) {
////
////                }
////
////                @Override
////                public void stopScanBtDeveice(boolean isBondScan) {
////
////                }
////
////                @Override
////                public void scanBTDeviceResult(BluetoothDevice bluetoothDevice) {
////                    if (!TextUtils.isEmpty(bluetoothDevice.getAddress()) &&
////                            bluetoothDevice.getAddress().substring(0, 8).equals("F9:CA:06")) {
////                        BlueToothUtils.getInstance().connectBTClose(bluetoothDevice);
////                        BlueToothUtils.getInstance().connectBTDevice(bluetoothDevice);
////                        BlueToothUtils.getInstance().stopScan();
////                    }
////                }
////
////                @Override
////                public void connectBTDeviceSuccess() {
////
////                }
////
////                @Override
////                public void connectBTDeviceRecon() {
////
////                }
////
////                @Override
////                public void connectBTDeviceFail() {
////
////                }
////
////                @Override
////                public void connectBTDeviceClose() {
////
////                }
////
////                @Override
////                public void onCharacteristicForWriteOrderReceiveData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String hexStr) {
////
////                }
////
////                @Override
////                public void onCharacteristicForReadOrderReceiveData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String hexStr) {
////
////                }
////
////                @Override
////                public void onCharacteristicForNotifyWriteOrderReceiveData(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
////                    handler.sendEmptyMessage(SEND_ORDER_FOR_WRITE_GET_DATA);
////                }
////
////                @Override
////                public void onCharacteristicForNotifyReadOrderReceiveData(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
////
////                }
////
////                @Override
////                public void onCharacteristicChangeForNotifyOrderReceiveData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String hexStr) {
////
////                }
////
////                @Override
////                public void allowSendOrderToBTDevice() {
////                    BlueToothUtils.getInstance().sendOrderToBTDeviceNotify(serviceUUid, characteristicUUid03);
////                }
////            });
////            BlueToothUtils.getInstance().enableBlueTooth();
////            try {
////                BlueToothUtils.getInstance().startScanBTDevice(null, null, false);
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//
//        }
//    }
}
