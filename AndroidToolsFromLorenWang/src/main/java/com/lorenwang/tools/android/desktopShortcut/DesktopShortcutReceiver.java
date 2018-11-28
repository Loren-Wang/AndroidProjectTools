package com.lorenwang.tools.android.desktopShortcut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lorenwang.tools.android.messageTransmit.FlyMessageUtils;

import static com.lorenwang.tools.android.messageTransmit.FlyMessageMsgTypes.DESKTOP_SHORTCUT_CREATE_SUCCESS;

public class DesktopShortcutReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            FlyMessageUtils.getInstance().sendMsg(DESKTOP_SHORTCUT_CREATE_SUCCESS,true);
        }
    }
}
