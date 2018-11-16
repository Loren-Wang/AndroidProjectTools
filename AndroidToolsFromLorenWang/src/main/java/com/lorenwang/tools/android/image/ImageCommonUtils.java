package com.lorenwang.tools.android.image;

import android.content.Context;
import android.util.Base64;

import com.lorenwang.tools.android.LogUtils;
import com.lorenwang.tools.android.base.CheckUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * 创建时间：2018-11-16 上午 10:15:2
 * 创建人：王亮（Loren wang）
 * 功能作用：图片处理通用类
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class ImageCommonUtils {
    private final String TAG = getClass().getName();
    private static ImageCommonUtils imageCommonUtils;
    public static ImageCommonUtils getInstance(){
        if(imageCommonUtils == null){
            imageCommonUtils = new ImageCommonUtils();
        }
        return imageCommonUtils;
    }


    /**
     * 将图片文件转换为base64字符串
     * @param filePath
     * @return
     */
    public String imageFileToBase64String(Context context,String filePath){
        if(CheckUtils.getInstance().checkFileOptionsPermisstion(context)
                && CheckUtils.getInstance().checkFileIsExit(filePath)
                && CheckUtils.getInstance().checkFileIsImage(filePath)){
            LogUtils.logI(TAG,"图片文件地址有效性检测成功");
            FileInputStream fileInputStream = null;
            String base64Str = null;//base64字符串
            try {
                fileInputStream = new FileInputStream(filePath);
                byte[] bytes = new byte[fileInputStream.read()];
                fileInputStream.read(bytes);
                base64Str = Base64.encodeToString(bytes, Base64.DEFAULT);
                LogUtils.logI(TAG,"图片转换成功，转换后数据：" + base64Str);
            }catch (Exception e){
                LogUtils.logE(TAG,"图片转换发生异常，异常信息：" + e != null ? e.getMessage() : "");
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
            return base64Str;
        }else {
            LogUtils.logI(TAG,"图片文件转换失败，失败原因可能是以下几点：1、未拥有文件权限；2、文件不存在；3、传输的地址非图片地址");
            return null;
        }
    }
}
