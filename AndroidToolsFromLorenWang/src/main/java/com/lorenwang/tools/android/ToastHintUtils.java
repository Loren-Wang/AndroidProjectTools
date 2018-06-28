package com.lorenwang.tools.android;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * 创建时间： 0001/2018/3/1 下午 2:03
 * 创建人：王亮（Loren wang）
 * 功能作用：提示弹窗单例类
 * 功能方法：
 * 思路：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class ToastHintUtils {
    private static ToastHintUtils toastHintUtils;
    private Toast allToast;//吐司提示弹窗，如果有下一个要弹出则隐藏上一个
    private Context context;

    public ToastHintUtils(Context context) {
        this.context = context;
    }

    public static ToastHintUtils getInstance(Context context){
        if(toastHintUtils == null){
            toastHintUtils = new ToastHintUtils(context);
        }
        return toastHintUtils;
    }

    /**
     * 显示提示信息
     * @param msg 提示文字
     * @param toastTime 提示时间,为空则使用默认短时间
     */
    public void toastMsg(String msg,Integer toastTime){
        if(checkMsg(msg)){
            if(allToast != null){
                allToast.cancel();
            }
            allToast = Toast.makeText(context,msg,toastTime != null ? toastTime : Toast.LENGTH_SHORT);
            allToast.show();
        }
    }

    /**
     * 显示吐司提示信息
     * @param msgResId 提示文字资源id
     * @param toastTime 提示时间，为空则使用默认短时间
     */
    public void toastMsg(@StringRes int msgResId, Integer toastTime){
        if(allToast != null){
            allToast.cancel();
        }
        allToast = Toast.makeText(context,msgResId,toastTime != null ? toastTime : Toast.LENGTH_SHORT);
        allToast.show();
    }

    /**
     * 检测msg消息是否为空
     * @param msg
     * @return 不为空则返回true
     */
    private boolean checkMsg(String msg){
        return msg != null && !msg.isEmpty();
    }


}
