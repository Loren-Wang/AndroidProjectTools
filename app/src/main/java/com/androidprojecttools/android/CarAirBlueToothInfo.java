package com.androidprojecttools.android;

import android.text.TextUtils;

import java.util.UUID;

/**
 * Created by LorenWang on 2018/4/11.
 * 创建时间：2018/4/11 16:09
 * 创建人：王亮（Loren wang）
 * 功能作用：
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */

public class CarAirBlueToothInfo {
    private static CarAirBlueToothInfo carAirBlueToothInfo;
    public static CarAirBlueToothInfo getInstance(){
        if(carAirBlueToothInfo == null){
            carAirBlueToothInfo = new CarAirBlueToothInfo();
        }
        return carAirBlueToothInfo;
    }

    /**
     * 格式化UUID
     * @param uuidStr
     * @return
     */
    public UUID paramUUid(String uuidStr){
        try {
            if(!TextUtils.isEmpty(uuidStr)){
                if(uuidStr.length() == 4){
                    return UUID.fromString("6e40" + uuidStr + "-b5a3-f393-e0a9-e50e24dcca9e");
                }else if(uuidStr.length() == 8){
                    return UUID.fromString(uuidStr + "-b5a3-f393-e0a9-e50e24dcca9e");
                }
            }
            return UUID.fromString(uuidStr);
        }catch (Exception e){
            return null;
        }
    }
}
