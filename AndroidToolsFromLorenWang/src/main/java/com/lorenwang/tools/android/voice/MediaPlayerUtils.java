package com.lorenwang.tools.android.voice;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;

import com.lorenwang.tools.android.LogUtils;

import java.io.File;
import java.io.FileInputStream;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_VOICE_CALL;

/**
 * 创建时间： 0019/2018/4/19 下午 1:38
 * 创建人：王亮（Loren wang）
 * 功能作用：音频播放单例类
 * 功能方法：1、传感器注册
 *         2、反注册传感器
 *         3、销毁电源锁
 *         4、初始化电源锁
 *         5、播放音频
 *         6、停止播放音频
 *         7、切换麦克风和话筒
 * 思路：在单例创建的时候需要初始化各个管理器、传感器，但是需要把电源锁单独拿出来单独做，因为后续会牵扯到电源锁的释放以及重新初始化的问题
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class MediaPlayerUtils {
    private final String TAG = getClass().getName() + hashCode();
    private static MediaPlayerUtils mediaPlayerUtils;
    private Activity activity;

    public static MediaPlayerUtils getInstance(Context context){
        if(mediaPlayerUtils == null && context != null){
            mediaPlayerUtils = new MediaPlayerUtils(context);
        }
        return mediaPlayerUtils;
    }

//    private MediaPlayer _mediaPlayer = null; // 音乐播放管理器
    private AudioManager audioManager = null; // 声音管理器
    private SensorManager _sensorManager = null; // 传感器管理器
    private Sensor mProximiny = null; // 传感器实例
    //屏幕开关
    private PowerManager localPowerManager = null;//电源管理对象
    private PowerManager.WakeLock localWakeLock = null;//电源锁
//    private boolean isFirstSetSpeaker = true;
    private String nowPlayVoicePath;//当前正在播放的地址
    private boolean isRegistSensorManager = false;//是否注册了传感器
//    private boolean isPlayer = false;
//    private boolean isChangeOut = false;//是否切换输入
    private int nowPlayStream = STREAM_MUSIC;//当前的设备播放模式


    private MediaPlayerUtils(Context context){
        context = context.getApplicationContext();
//        _mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        _sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProximiny = _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //获取系统服务POWER_SERVICE，返回一个PowerManager对象
        localPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        initWakeLock();
    }






    /**
     * 初始化电源锁
     */
    private void initWakeLock(){
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        localWakeLock = localPowerManager.newWakeLock(32, "MyPower");//第一个参数为电源锁级别，第二个是日志tag
    }



    /**
     * 切换麦克风和话筒
     * @param on
     */
    private synchronized void setSpeakerphoneOn(boolean on) {
        if(on) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            activity.setVolumeControlStream(STREAM_VOICE_CALL);
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
            //       把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }


    /**
     * 传感器监听
     */
    private SensorEventListener sensorEventListener = new SensorEventListener(){
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            int type = event.sensor.getType();
            if(type == Sensor.TYPE_PROXIMITY){
                if (values[0] == 0.0) {
                    if (isPlayer && localWakeLock != null) {
                        LogUtils.logD(TAG,"申请电源设备锁");
                        localWakeLock.acquire();// 申请设备电源锁
                    }
                }else {
                    if (localWakeLock != null) {
                        LogUtils.logD(TAG,"释放设备电源锁");
                        localWakeLock.setReferenceCounted(false);
                        localWakeLock.release(); // 释放设备电源锁
                    }
                }


                if(isPlayer){
                    if (values[0] == 0.0) {
                        if (nowPlayStream == STREAM_MUSIC) {
                            LogUtils.logD(TAG,"贴近手机，切换到听筒播放");
                            nowPlayStream = STREAM_VOICE_CALL;
                            setSpeakerphoneOn(false);
//                            isChangeOut = true;
                            startPlayRecord(nowPlayVoicePath, AudioOutputType.EARPIECE, lastPlayRecordStateListener);
                        }
                    }else {
                        if(nowPlayStream == STREAM_VOICE_CALL) {
                            LogUtils.logD(TAG,"远离手机，切换到扬声器播放");
                            nowPlayStream = STREAM_MUSIC;
                            setSpeakerphoneOn(true);
//                            isChangeOut = true;
                            startPlayRecord(nowPlayVoicePath, AudioOutputType.SPEAKER,lastPlayRecordStateListener);
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


//    /*********************************************私有方法******************************************/
//    /**
//     * 播放准备
//     * @param playPath
//     * @param type
//     * @param playRecordStateListener
//     */
//    private void start(final String playPath, AudioOutputType type, final PlayRecordStateListener playRecordStateListener){
//        try {
//            //重置播放器
//            _mediaPlayer.reset();
//            playFileInputStream = new FileInputStream(playPath);
//            _mediaPlayer.setDataSource(playFileInputStream.getFD());//直接使用地址会抛异常
//            //这里是重点注意的地方，必须要设置，并且要在_mediaPlayer.prepare()之前设置
//            if(type == AudioOutputType.EARPIECE){
//                nowPlayStream = STREAM_VOICE_CALL;
//                _mediaPlayer.setAudioStreamType(STREAM_VOICE_CALL);
//            }else{
//                nowPlayStream = STREAM_MUSIC;
//                _mediaPlayer.setAudioStreamType(STREAM_MUSIC);
//            }
//            //设置播放完成回调，当reset的时候也会调用
//            _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    if(isChangeOut){
//                        isChangeOut = false;
//                    }else {
//                        stop(playRecordStateListener, true);
//                    }
//                }
//            });
//            _mediaPlayer.prepare();
//            _mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.start();
//                    Message message = Message.obtain();
//                    message.what = PLAY_RECORD_START_SUCCESS;
//                    message.obj = playRecordStateListener;
//                    handler.sendMessage(message);
//                }
//            });
//        } catch (Exception e) {
//            Message message = Message.obtain();
//            message.what = PLAY_RECORD_START_FAIL;
//            message.obj = playRecordStateListener;
//            handler.sendMessage(message);
//        }
//    }
//
//    /**
//     * 停止播放
//     * @param playRecordStateListener
//     * @param isComple 是否是自动完成的
//     */
//    private void stop(final PlayRecordStateListener playRecordStateListener,boolean isComple){
//        this.lastPlayRecordStateListener = playRecordStateListener;
//        if(!_mediaPlayer.isPlaying()){
//            Message message = Message.obtain();
//            message.what = PLAY_RECORD_STOP_FAIL;
//            message.obj = playRecordStateListener;
//            handler.sendMessage(message);
//            return;
//        }
////        isPlayer = false;
////        nowPlayVoicePath = null;
////        lastPlayRecordStateListener = null;
//        // 停止播放
//        if (_mediaPlayer.isPlaying() && !isComple) {
//            _mediaPlayer.stop();
//        }
//        Message message = Message.obtain();
//        message.what = PLAY_RECORD_STOP_SUCCESS;
//        message.obj = playRecordStateListener;
//        handler.sendMessage(message);
//        LogUtils.logD(TAG,"已停止播放");
//    }
//




    /**********************************************公开方法*****************************************/

    /**
     * 注册传感器
     */
    public void registSensorManager(){
        if(!isRegistSensorManager){
            isRegistSensorManager = true;
            // 注册传感器
            _sensorManager.registerListener(sensorEventListener, mProximiny,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 反注册传感器
     */
    public void unRegistSensorManager(){
        if(isRegistSensorManager) {
            isRegistSensorManager = false;
            // 取消注册传感器
            _sensorManager.unregisterListener(sensorEventListener);
        }
    }

    /**
     * 重新获得焦点的监听
     */
    public void onResume(Activity activity){
        this.activity = activity;
        //电源锁是否为空
        if(localWakeLock == null){
            initWakeLock();
        }
    }

    /**
     * 反注册传感器
     */
    public void onPause(){
        activity = null;
        unRegistSensorManager();
    }

    /**
     * 销毁电源锁
     */
    public void onDestory(){
        activity = null;
        if(localWakeLock != null){
            localWakeLock.setReferenceCounted(false);
            localWakeLock.release();//释放电源锁，如果不释放finish这个acitivity后仍然会有自动锁屏的效果，不信可以试一试
            localWakeLock = null;
        }
        unRegistSensorManager();
        stopPlayRecord(null);
    }

//    /**
//     * 开始播放
//     * @param playPath
//     * @param type
//     * @param playRecordStateListener
//     */
//    public void startPlayVoice(final String playPath, AudioOutputType type, final PlayRecordStateListener playRecordStateListener){
//        //检测文件是否存在
//        if(TextUtils.isEmpty(playPath) || !new File(playPath).exists()){
//            Message message = Message.obtain();
//            message.what = PLAY_RECORD_START_FAIL;
//            message.obj = playRecordStateListener;
//            handler.sendMessage(message);
//        }
//        //开始播放了
//        nowPlayVoicePath = playPath;
//        lastPlayRecordStateListener = playRecordStateListener;
//        //播放准备
//        start(playPath,type,playRecordStateListener);
//    }
//    /**
//     * 停止播放
//     * @param playRecordStateListener
//     */
//    public void stopPlayVoice(PlayRecordStateListener playRecordStateListener){
//        stop(playRecordStateListener,false);
//    }



    /***********************************************接口以及枚举类***********************************/

    public enum AudioOutputType{
        EARPIECE,
        SPEAKER,
    }
    public interface PlayRecordStateListener{
        void playRecordStart(boolean isSuccess);
        void playRecordStop(boolean isSuccess);
    }









    private MediaPlayer mPlayer = null;
    private boolean isPlayer = false;//是否正在播放音频
    private static final int PLAY_RECORD_START_SUCCESS = 1;
    private static final int PLAY_RECORD_START_FAIL = 2;
    private static final int PLAY_RECORD_STOP_SUCCESS = 3;
    private static final int PLAY_RECORD_STOP_FAIL = 4;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PLAY_RECORD_START_SUCCESS:
                    if(msg.obj != null){
                        ((PlayRecordStateListener)msg.obj).playRecordStart(true);
                    }
                    break;
                case PLAY_RECORD_START_FAIL:
                    if(msg.obj != null){
                        ((PlayRecordStateListener)msg.obj).playRecordStart(false);
                    }
                    break;
                case PLAY_RECORD_STOP_SUCCESS:
                    if(msg.obj != null){
                        ((PlayRecordStateListener)msg.obj).playRecordStop(true);
                    }
                    break;
                case PLAY_RECORD_STOP_FAIL:
                    if(msg.obj != null){
                        ((PlayRecordStateListener)msg.obj).playRecordStop(false);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    //上一次音频播放回调
    private PlayRecordStateListener lastPlayRecordStateListener;
    private FileInputStream playFileInputStream;
    //开启音频播放
    public void startPlayRecord(final String playPath, AudioOutputType type, final PlayRecordStateListener playRecordStateListener){
        if(isPlayer){
            stopPlayRecord(lastPlayRecordStateListener);
        }
        if(!new File(playPath).exists()){
            Message message = Message.obtain();
            message.what = PLAY_RECORD_START_FAIL;
            message.obj = playRecordStateListener;
            handler.sendMessage(message);
        }

        try {
            // TODO Auto-generated method stub
            mPlayer = new MediaPlayer();
            if(type == AudioOutputType.EARPIECE){
                nowPlayStream = STREAM_VOICE_CALL;
                mPlayer.setAudioStreamType(STREAM_VOICE_CALL);
            }else{
                nowPlayStream = STREAM_MUSIC;
                mPlayer.setAudioStreamType(STREAM_MUSIC);
            }
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayRecord(playRecordStateListener);
                }
            });
            playFileInputStream = new FileInputStream(playPath);
            mPlayer.setDataSource(playFileInputStream.getFD());//直接使用地址会抛异常
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    isPlayer = true;
                    nowPlayVoicePath = playPath;
                    lastPlayRecordStateListener = playRecordStateListener;
                    Message message = Message.obtain();
                    message.what = PLAY_RECORD_START_SUCCESS;
                    message.obj = playRecordStateListener;
                    handler.sendMessage(message);
                }
            });
        } catch (Exception e) {
            Message message = Message.obtain();
            message.what = PLAY_RECORD_START_FAIL;
            message.obj = playRecordStateListener;
            handler.sendMessage(message);
        }

    }
    //停止播放录音,停止时需要使用reset方法重置录音器，不要释放以及置空，释放或置空不一定会成立
    public void stopPlayRecord(PlayRecordStateListener playRecordStateListener){
        if(mPlayer == null || !isPlayer){
            Message message = Message.obtain();
            message.what = PLAY_RECORD_STOP_FAIL;
            message.obj = playRecordStateListener;
            handler.sendMessage(message);

            return;
        }
        // TODO Auto-generated method stub
        if(mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        isPlayer = false;
        Message message = Message.obtain();
        message.what = PLAY_RECORD_STOP_SUCCESS;
        message.obj = playRecordStateListener;
        handler.sendMessage(message);
    }


    public boolean isPlayer() {
        return isPlayer;
    }
}
