package com.lorenwang.tools.android.mobile;

import android.os.Build;
import android.text.TextUtils;

import com.lorenwang.tools.android.base.LogUtils;

import java.lang.reflect.Method;

/**
 * 创建时间： 0026/2018/6/26 下午 3:13
 * 创建人：王亮（Loren wang）
 * 功能作用：手机品牌判断工具类
 * 功能方法：
 * 思路：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class MobilePhoneBrandUtils {
    private final String TAG = getClass().getName();
    private static MobilePhoneBrandUtils mobilePhoneBrandUtils;

    public static MobilePhoneBrandUtils getInstance() {
        if (mobilePhoneBrandUtils == null) {
            mobilePhoneBrandUtils = new MobilePhoneBrandUtils();
        }
        return mobilePhoneBrandUtils;
    }

    private Boolean isXiaoMi;//是否是小米手机
    private Boolean isMeiZu;//判断是否是魅族手机
    private Boolean isHuaWei;//是否是华为手机

    /**
     * 是否是小米手机,参考 https://dev.mi.com/doc/?p=254
     *
     * @return true 是
     */
    public boolean isXiaoMiMobile() {
        if (isXiaoMi == null) {
            if (Build.MANUFACTURER.toLowerCase().contains("xiaomi")) {
                isXiaoMi = true;
            } else {
                try {
                    Class<?> clz = Class.forName("android.os.SystemProperties");
                    Method method = clz.getMethod("get", String.class, String.class);
                    isXiaoMi = !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.miui.ui.version.code", null))
                            || !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.miui.ui.version.name", null))
                            || !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.miui.internal.storage", null));
                } catch (Exception e) {
                    isXiaoMi = false;
                }
            }
            LogUtils.logD(TAG, "is xiaomi mobile:" + isXiaoMi);
        }
        return isXiaoMi;
    }

    /**
     * 判断是否是魅族手机
     *
     * @return
     */
    public boolean isMeiZuMobile() {
        if (isMeiZu == null) {
            if (Build.MANUFACTURER.toLowerCase().contains("meizu")) {
                isMeiZu = true;
            } else {
                try {
                    Class<?> clz = Class.forName("android.os.SystemProperties");
                    Method method = clz.getMethod("get", String.class, String.class);
                    String systemProperty = (String) method.invoke(clz, "ro.build.display.id", "");
                    isMeiZu = !systemProperty.isEmpty() && systemProperty.toLowerCase().contains("flyme");
                } catch (Exception e) {
                    isMeiZu = false;
                }
            }
            LogUtils.logD(TAG, "is meizu mobile:" + isMeiZu);
        }
        return isMeiZu;
    }

    /**
     * 是否是华为手机
     *
     * @return
     */
    public boolean isHuaWeiMobile() {
        if (isHuaWei == null) {
            if (Build.MANUFACTURER.toLowerCase().contains("huaswei")) {
                isHuaWei = true;
            } else {
                try {
                    Class<?> clz = Class.forName("android.os.SystemProperties");
                    Method method = clz.getMethod("get", String.class, String.class);
                    isHuaWei = !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.build.hw_emui_api_level", null))
                            || !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.build.version.emui", null))
                            || !TextUtils.isEmpty((CharSequence) method.invoke(clz, "ro.confg.hw_systemversion", null));
                } catch (Exception e) {
                    isHuaWei = false;
                }
            }
            LogUtils.logD(TAG, "is huawei mobile:" + isHuaWei);
        }
        return isHuaWei;
    }

    /**
     * 是否是vivo手机
     *
     * @return
     */
    public boolean isVivoMobile() {
        return "vivo".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }

    /**
     * 是否是oppo手机
     *
     * @return
     */
    public boolean isOPPOMobile() {
        return "oppo".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }

    /**
     * 是否是Coolpad手机
     *
     * @return
     */
    public boolean isCoolpadMobile() {
        return "coolpad".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }

    /**
     * 是否是samsung手机
     *
     * @return
     */
    public boolean isSamsungMobile() {
        return "samsung".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }

    /**
     * 是否是Sony手机
     *
     * @return
     */
    public boolean isSonyMobile() {
        return "sony".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }

    /**
     * 是否是LG手机
     *
     * @return
     */
    public boolean isLGMobile() {
        return "lg".equals(MobileSystemInfoUtils.getInstance().getMobileBrand().toLowerCase());
    }
}
