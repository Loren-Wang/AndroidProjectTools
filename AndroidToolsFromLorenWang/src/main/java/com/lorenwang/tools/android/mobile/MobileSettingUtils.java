package com.lorenwang.tools.android.mobile;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.lorenwang.tools.android.base.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 创建时间：2018-11-28 下午 21:02:13
 * 创建人：王亮（Loren wang）
 * 功能作用：${DESCRIPTION}
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class MobileSettingUtils {
    private final String TAG = getClass().getName();
    private static MobileSettingUtils mobileSettingUtils;

    private MobileSettingUtils() {
    }

    public static MobileSettingUtils getInstance() {
        if (mobileSettingUtils == null) {
            mobileSettingUtils = new MobileSettingUtils();
        }
        return mobileSettingUtils;
    }

    public void jumpToAppPermisstionSettingPage(Context context, String packageName) {
        LogUtils.logI(TAG, "跳转到APP权限设置页面：" + packageName);
        if (MobilePhoneBrandUtils.getInstance().isMeiZuMobile()) {
            jumpToMeizuAppPermisstionSettingPage(context, packageName);
        } else if (MobilePhoneBrandUtils.getInstance().isXiaoMiMobile()) {
            jumpToXiaoMiAppPermisstionSettingPage(context, packageName);
        } else {
            jumpToDefaultAppPermisstionSettingPage(context, packageName);
        }
    }


    /**
     * 获取小米手机的MIUI版本号
     *
     * @return
     */
    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    /**
     * 跳转到小米App权限设置
     *
     * @param context
     * @param packageName
     */
    private void jumpToXiaoMiAppPermisstionSettingPage(Context context, String packageName) {
        String rom = getMiuiVersion();
        LogUtils.logI(TAG, "jumpToMiaoMiAppPermisstionSettingPage --- rom : " + rom);
        Intent intent = new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else if ("V8".equals(rom) || "V9".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
        } else {
            jumpToDefaultAppPermisstionSettingPage(context, packageName);
        }
        context.startActivity(intent);
    }

    /**
     * 跳转到魅族App权限设置
     *
     * @param context
     * @param packageName
     */
    private void jumpToMeizuAppPermisstionSettingPage(Context context, String packageName) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", packageName);
            context.startActivity(intent);
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            jumpToDefaultAppPermisstionSettingPage(context, packageName);
        }
    }

    /**
     * 跳转到默认App权限设置页面
     *
     * @param context
     * @param packageName
     */
    private void jumpToDefaultAppPermisstionSettingPage(Context context, String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
