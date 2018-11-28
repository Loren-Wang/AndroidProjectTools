package com.lorenwang.tools.android.mobile;

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
    private MobileSettingUtils(){}
    public static MobileSettingUtils getInstance(){
        if(mobileSettingUtils == null){
            mobileSettingUtils = new MobileSettingUtils();
        }
        return mobileSettingUtils;
    }
}
