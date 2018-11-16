package com.lorenwang.tools.android.file;

import android.content.Context;

import com.lorenwang.tools.android.LogUtils;
import com.lorenwang.tools.android.base.CheckUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 创建时间：2018-11-16 下午 14:10:33
 * 创建人：王亮（Loren wang）
 * 功能作用：${DESCRIPTION}
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class FileOptionsUtils {
    private final String TAG = getClass().getName();
    private static FileOptionsUtils fileOptionsUtils;

    public static FileOptionsUtils getInstance() {
        if (fileOptionsUtils == null) {
            fileOptionsUtils = new FileOptionsUtils();
        }
        return fileOptionsUtils;
    }


    /**
     * 读取图片文件并获取字节
     * @param context
     * @param isCheckPermisstion 是否检查权限
     * @param isCheckFile 是否检查文件
     * @param filePath 文件地址
     * @return
     */
    public byte[] readImageFileGetBytes(Context context, boolean isCheckPermisstion
            , boolean isCheckFile, String filePath) {
        if (isCheckPermisstion && !CheckUtils.getInstance().checkFileOptionsPermisstion(context)) {
            return null;
        }
        if (isCheckFile && CheckUtils.getInstance().checkFileIsExit(filePath)
                && CheckUtils.getInstance().checkFileIsImage(filePath)) {
            return null;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            return bytes;
        }catch (Exception e){
            LogUtils.logE(TAG,"图片读取异常，异常信息：" + e != null ? e.getMessage() : "");
            return null;
        }finally {
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileInputStream = null;
            }
        }
    }
}
