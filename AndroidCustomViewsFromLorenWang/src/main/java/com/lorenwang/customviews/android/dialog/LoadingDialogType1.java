package com.lorenwang.customviews.android.dialog;

import android.content.Context;
import android.view.LayoutInflater;

import com.lorenwang.customviews.android.R;

/**
 * 创建时间：2018-11-16 下午 15:10:8
 * 创建人：王亮（Loren wang）
 * 功能作用：加载中弹窗
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class LoadingDialogType1 extends BaseDialog {
    public LoadingDialogType1(Context context) {
        super(context,R.style.loading_dialog_type1);
        view = LayoutInflater.from(context).inflate(R.layout.loading_dialog_type_1,null);
        new Builder(context,R.style.loading_dialog_type1).create();
        setView(view);
        setCanceledOnTouchOutside(false);
        getWindow().setWindowAnimations(R.style.dialog_anim_for_center);
    }

    @Override
    public void onBackPressed() {

    }
}
