package com.lorenwang.tools.android.mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * Created by LorenWang on 2018/8/16 0016.
 * 创建时间：2018/8/16 0016 下午 04:32
 * 创建人：王亮（Loren wang）
 * 功能作用：手机系统信息工具类
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class MobileSystemInfoUtils {
    private final String TAG = getClass().getName();
    private static MobileSystemInfoUtils mobileSystemInfoUtils;
    public static MobileSystemInfoUtils getInstance(){
        if(mobileSystemInfoUtils == null){
            mobileSystemInfoUtils = new MobileSystemInfoUtils();
        }
        return mobileSystemInfoUtils;
    }

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机系统sdk版本号
     * @return
     */
    public int getSystemSdkVersion(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机品牌信息
     * @return
     */
    public String getMobileBrand(){
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    @SuppressLint({"MissingPermission"})
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }


}
