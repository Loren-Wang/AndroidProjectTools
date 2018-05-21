package com.lorenwang.customviews.android;

import android.content.Context;
import android.util.AttributeSet;

public class CustomDrawableButton extends android.support.v7.widget.AppCompatButton {
    public CustomDrawableButton(Context context) {
        super(context);
        init(context,null,-1);
    }

    public CustomDrawableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public CustomDrawableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){

    }
}
