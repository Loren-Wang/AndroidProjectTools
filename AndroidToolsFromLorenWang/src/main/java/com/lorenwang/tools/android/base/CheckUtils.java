package com.lorenwang.tools.android.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.lorenwang.tools.android.LogUtils;

import java.io.File;

/**
 * 创建时间：2018-11-16 上午 10:19:49
 * 创建人：王亮（Loren wang）
 * 功能作用：检查工具类，用来检查各种，属于基础工具类
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class CheckUtils {
    private final String TAG = getClass().getName();
    private static CheckUtils checkUtils;
    public static CheckUtils getInstance() {
        if (checkUtils == null) {
            checkUtils = new CheckUtils();
        }
        return checkUtils;
    }


    /**
     * 检查是否拥有文件操作权限
     *
     * @param context
     * @return 有权限返回true，无权限返回false
     */
    public boolean checkFileOptionsPermisstion(Context context) {
        if (context == null) {
            return false;
        }
        context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(context
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context
                    , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath
     * @return
     */
    public boolean checkFileIsExit(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            LogUtils.logI(TAG, "被检查文件地址为空，不通过检测");
            return false;
        }
        File file = new File(filePath);
        boolean isExit = false;//文件是否存在记录
        if (file == null || file.isDirectory()) {
            LogUtils.logI(TAG, "被检查文件为空或被检测的地址为文件夹，不通过检测");
            return false;
        }
        if (file.exists()) {
            isExit = true;
            LogUtils.logI(TAG, "被检查文件存在");
        } else {
            LogUtils.logI(TAG, "被检查文件不存在");
        }
        file = null;
        return isExit;
    }

    /**
     * 检测文件是否是图片
     *
     * @param filePath
     * @return
     */
    public boolean checkFileIsImage(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            if (filePath.length() > 4 &&
                    (filePath.toLowerCase().substring(filePath.length() - 4).contains(".jpg")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".png")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".bmp")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".gif")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".psd")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".swf")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".svg")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".pcx")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".dxf")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".wmf")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".emf")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".lic")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".eps")
                            || filePath.toLowerCase().substring(filePath.length() - 4).contains(".tga"))) {
                LogUtils.logI(TAG, "被检测地址为图片地址，图片地址后缀：" + filePath.toLowerCase().substring(filePath.length() - 4));
                return true;
            } else if (filePath.length() > 5 &&
                    (filePath.toLowerCase().substring(filePath.length() - 5).contains(".jpeg")
                            || filePath.toLowerCase().substring(filePath.length() - 5).contains(".tiff"))) {
                LogUtils.logI(TAG, "被检测地址为图片地址，图片地址后缀：" + filePath.toLowerCase().substring(filePath.length() - 5));
                return true;
            }else {
                LogUtils.logI(TAG, "被检测地址为空或文件为非图片");
                return false;
            }
        } else {
            LogUtils.logI(TAG, "被检测地址为空或文件为非图片");
            return false;
        }
    }
}
