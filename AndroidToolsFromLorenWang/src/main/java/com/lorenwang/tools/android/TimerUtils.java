package com.lorenwang.tools.android;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LorenWang on 2018/8/2 0002.
 * 创建时间：2018/8/2 0002 下午 05:42
 * 创建人：王亮（Loren wang）
 * 功能作用：定时器单例类
 * 思路：
 * 方法：
 * 注意：
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class TimerUtils {
    private final String TAG = getClass().getName();
    private static TimerUtils timerUtils;
    public static TimerUtils getInstance(){
        if(timerUtils == null){
            timerUtils = new TimerUtils();
        }
        return timerUtils;
    }

    private Timer timer = new Timer();

    /**
     * 开启一个定时器，在制定时间之后执行runnable
     * @param runnable
     * @param delay 等待时间
     * @return
     */
    public TimerTask schedule(final Runnable runnable, long delay){
        if(runnable == null){
            return null;
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timer.schedule(timerTask,delay);
        return timerTask;
    }

    /**
     * 开启一个定时器，在等待delay后执行第一次任务，第二次（含）之后间隔period时间后再次执行
     * @param runnable
     * @param delay 等待时间
     * @param period 间隔时间
     * @return
     */
    public TimerTask schedule(final Runnable runnable, long delay, long period){
        if(runnable == null){
            return null;
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timer.schedule(timerTask,delay,period);
        return timerTask;
    }
}
