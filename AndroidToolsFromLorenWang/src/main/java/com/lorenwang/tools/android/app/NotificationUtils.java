package com.lorenwang.tools.android.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建时间：2018-12-22 上午 09:36:9
 * 创建人：王亮（Loren wang）
 * 功能作用：通知栏通知工具类，使用它之前必须调用初始化方法
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class NotificationUtils {
    private final String TAG = getClass().getName();
    private static NotificationUtils notificationUtils;
    private Context context;//上下文
    private int notifyIcon;//通知icon
    private int notifySmallIcon;//通知小icon
    private NotificationManager notificationManager;
    private Map<Integer, NotificationCompat.Builder> builderMap = new HashMap<>();//通知build存储

    private NotificationUtils() {
    }

    public static NotificationUtils getInstance() {
        if (notificationUtils == null) {
            notificationUtils = new NotificationUtils();
        }
        return notificationUtils;
    }






    /**
     * 初始化
     *
     * @param context         上下文
     * @param notifyIcon      通知icon
     * @param notifySmallIcon 通知小icon
     */
    public void init(@NonNull Context context, @DrawableRes int notifyIcon, @DrawableRes int notifySmallIcon) {
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        this.context = context.getApplicationContext();
        this.notifyIcon = notifyIcon;
        this.notifySmallIcon = notifySmallIcon;
        notificationManager = (NotificationManager) this.context.getSystemService(Activity.NOTIFICATION_SERVICE);
    }

    /**
     * 文件下载进度通知
     *
     * @param fileDownloadNotifyId 通知id
     * @param maxProgress          最大进度
     * @param nowProgress          当前进度
     * @param installIntent        安装intent，当前小于最大可以为空
     * @param title                标题
     * @param msg                  显示内容，当前进度小于最大是可以显示百分比字符串
     * @param notifyHintType       通知提示类型,为空时代表不提示
     *                             Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
     *                             Notification.DEFAULT_SOUND：系统默认铃声。
     *                             Notification.DEFAULT_VIBRATE：系统默认震动。
     *                             Notification.DEFAULT_LIGHTS：系统默认闪光。
     */
    public void notifyFileDownloadProgress(int fileDownloadNotifyId, int maxProgress, int nowProgress
            , Intent installIntent, String title, String msg, Integer notifyHintType) {
        NotificationCompat.Builder builder = null;
        if (nowProgress >= maxProgress) {
            //销毁下载的build
            clear(fileDownloadNotifyId);
            builder = getCompatBuilder(builder, installIntent, "", title, msg, true
                    , true, notifyHintType, false, 0, 0, false);
        } else if (nowProgress >= 0) {
            builder = getCompatBuilder(builderMap.get(fileDownloadNotifyId), null, "", title, msg, false
                    , false, notifyHintType, true, maxProgress, nowProgress, false);
        }
        sentNotify(fileDownloadNotifyId, builder);
    }




















    /********************************************私有属性*******************************************/

    /**
     * 发送通知
     *
     * @param notificationId
     * @param builder
     */
    private void sentNotify(int notificationId, NotificationCompat.Builder builder) {
        if (builder != null) {
            //记录通知build
            builderMap.put(notificationId, builder);
            // 发送该通知
            notificationManager.notify(notificationId, builder.build());
        }
    }

    /**
     * 构建通知build
     *
     * @param intent              跳转页面的intent
     * @param ticker              通知提示文本
     * @param title               通知标题
     * @param msg                 通知内容
     * @param isClickNotifyRemove 是否可以点击移除通知
     * @param isSlidNotiyRemove   是否可以滑动移除通知
     * @param notifyHintType      通知提示类型
     *                            Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
     *                            Notification.DEFAULT_SOUND：系统默认铃声。
     *                            Notification.DEFAULT_VIBRATE：系统默认震动。
     *                            Notification.DEFAULT_LIGHTS：系统默认闪光。
     * @return
     */
    private NotificationCompat.Builder getCompatBuilder(NotificationCompat.Builder cBuilder
            , Intent intent, String ticker, String title, String msg
            , boolean isClickNotifyRemove, boolean isSlidNotiyRemove, Integer notifyHintType
            , boolean isShowProgress, int maxProgress, int nowProgress, boolean indeterminate) {
        if (cBuilder == null) {
            cBuilder = new NotificationCompat.Builder(context.getApplicationContext());
        }
        if(intent == null){
            intent = new Intent();
        }
        //判断点击是否要起订应用程序或Activity
        // 如果当前Activity启动在前台，则不开启新的Activity。
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // 当设置下面PendingIntent.FLAG_UPDATE_CURRENT这个参数的时候，常常使得点击通知栏没效果，你需要给notification设置一个独一无二的requestCode
        // 将Intent封装进PendingIntent中，点击通知的消息后，就会启动对应的程序
        @SuppressLint("WrongConstant")
        PendingIntent pIntent = PendingIntent.getActivity(context.getApplicationContext(), (int) SystemClock.uptimeMillis(), intent, Notification.FLAG_INSISTENT);
        cBuilder.setContentIntent(pIntent);// 该通知要启动的Intent

        //设置在通知栏显示内容
        cBuilder.setSmallIcon(notifySmallIcon);// 设置顶部状态栏的小图标
        cBuilder.setTicker(ticker);// 在顶部状态栏中的提示信息
        cBuilder.setContentTitle(title);// 设置通知中心的标题
        cBuilder.setContentText(msg);// 设置通知中心中的内容
        cBuilder.setWhen(System.currentTimeMillis());

        //设置是否点击通知移除通知，将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失,不设置的话点击消息后也不清除，但可以滑动删除
        cBuilder.setAutoCancel(isClickNotifyRemove);
        //设置是否可以滑动删除通知,将Ongoing设为true 那么notification将不能滑动删除
        cBuilder.setOngoing(!isSlidNotiyRemove);
        //从Android4.1开始，可以通过以下方法，设置notification的优先级， 优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
        cBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        /*
         * Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
         * Notification.DEFAULT_SOUND：系统默认铃声。
         * Notification.DEFAULT_VIBRATE：系统默认震动。
         * Notification.DEFAULT_LIGHTS：系统默认闪光。
         * notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
         */
        if(notifyHintType != null) {
            cBuilder.setDefaults(notifyHintType);
        }
        //设置进度,如果显示的话
        if (isShowProgress) {
            cBuilder.setProgress(maxProgress, nowProgress, indeterminate);
        }
        return cBuilder;
    }

























    /********************************************清除通知*******************************************/

    /**
     * 清除所有通知
     */
    public void clear() {
        // 取消通知
        notificationManager.cancelAll();
        //清除所有记录
        builderMap.clear();
    }

    /**
     * 根据指定id清除通知
     *
     * @param id 通知id
     */
    public void clear(Integer id) {
        if (id != null) {
            notificationManager.cancel(id);
            //清除指定记录
            builderMap.remove(id);
        }
    }

    /**
     * 根据指定的id和tag清除通知
     *
     * @param tag 通知tag
     * @param id  通知id
     */
    public void clear(String tag, Integer id) {
        if (!TextUtils.isEmpty(tag) && id != null) {
            notificationManager.cancel(tag, id);
            //清除指定记录
            builderMap.remove(id);
        }
    }
}

