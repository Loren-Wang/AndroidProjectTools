package com.lorenwang.tools.android;

import android.content.Context;
import android.text.TextUtils;

import com.lorenwang.tools.android.base.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 创建时间： 0002/2018/4/2 上午 11:35
 * 创建人：王亮（Loren wang）
 * 功能作用：用于安卓热修复使用
// * 思路：1、首先要读取热修复记录，读取热修复文件名，判断该程序的系统文件夹路径下是否有相应的文件，如果有相应的文件则直接合并dex（在程序启动页的oncreate中就要加载的）
// *      2、不管有没有热修复记录倒要在程序启动的时候从网络拉取热修复记录如果网络拉取和本地不同的话那么就要将修复文件下载下来同时
// *         将修复文件复制到该程序对应的系统文件目录下，复制完成后进行合并dex

 * 新思路：1、必须进行网络请求，请求成功按新数据修复或不修复；
 *        2、请求失败则按已存储数据进行修复
 * 方法：1、合并修复的dex文件
 *      2、修复结果状态返回
 *      3、初始化（同时也是思路1要做的）
 *      4、
 * 修改人：
 * 修改时间：
 * 备注：
 */
public class AndroidHotFixUtils {
    private final String TAG = getClass().getName();
    private static AndroidHotFixUtils androidHotFixUtils;
    private Context context;
    private final String DEX_OPT_DIR = "optimize_dex";//dex的优化路径
    private final String DEX_BASECLASSLOADER_CLASS_NAME = "dalvik.system.BaseDexClassLoader";
    private final String DEX_FILE_E = "dex";//扩展名
    private final String DEX_ELEMENTS_FIELD = "dexElements";//pathList中的dexElements字段
    private final String DEX_PATHLIST_FIELD = "pathList";//BaseClassLoader中的pathList字段
    private final String FIX_DEX_PATH = "fix_dex";//fixDex存储的路径
    private File dexDirFilePath;
    private boolean isFinishFix = true;//是否修复完成
    private String fixData = "";//修复数据

    private AndroidHotFixUtils(Context context){
        if (this.context != null){
            return;
        }
        if(context != null) {
            this.context = context.getApplicationContext();
            dexDirFilePath = context.getDir(FIX_DEX_PATH, Context.MODE_PRIVATE);
        }else {
            throw new NullPointerException("context is null");
        }
    }
    public static AndroidHotFixUtils getInstance(Context context){
        if(androidHotFixUtils == null){
            androidHotFixUtils = new AndroidHotFixUtils(context);
        }
        return androidHotFixUtils;
    }


    /**
     * 合并修复的dex文件
     * @param dexFile
     */
    private void mergeFixDex(File dexFile){
        try {
            //创建dex的optimize路径
            File optimizeDir = new File(dexDirFilePath, DEX_OPT_DIR);
            if (!optimizeDir.exists()) {
                optimizeDir.mkdirs();
            }
            //加载自身Apk的dex，通过PathClassLoader
            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            //找到dex并通过DexClassLoader去加载
            //dex文件路径，优化输出路径，null,父加载器
            DexClassLoader dexClassLoader = new DexClassLoader(dexFile.getAbsolutePath(), optimizeDir.getAbsolutePath(), null, pathClassLoader);
            //获取app自身的BaseDexClassLoader中的pathList字段
            Object appDexPathList = getDexPathListField(pathClassLoader);
            //获取补丁的BaseDexClassLoader中的pathList字段
            Object fixDexPathList = getDexPathListField(dexClassLoader);

            Object appDexElements = getDexElements(appDexPathList);
            Object fixDexElements = getDexElements(fixDexPathList);
            //合并两个elements的数据，将修复的dex插入到数组最前面
            Object finalElements = combineArray(fixDexElements, appDexElements);
            //给app 中的dex pathList 中的dexElements 重新赋值
            setFiledValue(appDexPathList, appDexPathList.getClass(), DEX_ELEMENTS_FIELD, finalElements);
            fixResultState(true);
        }catch (Exception e){
            fixResultState(false);
        }
    }
    /**
     * 获取指定classloader 中的pathList字段的值（DexPathList）
     *
     * @param classLoader
     * @return
     */
    private Object getDexPathListField(Object classLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(classLoader, Class.forName(DEX_BASECLASSLOADER_CLASS_NAME), DEX_PATHLIST_FIELD);
    }
    /**
     * 获取一个字段的值
     *
     * @return
     */
    private Object getField(Object obj, Class<?> clz, String fieldName) throws NoSuchFieldException, IllegalAccessException {

        Field field = clz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);

    }
    /**
     * 为指定对象中的字段重新赋值
     *
     * @param obj
     * @param claz
     * @param filed
     * @param value
     */
    private void setFiledValue(Object obj, Class<?> claz, String filed, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = claz.getDeclaredField(filed);
        field.setAccessible(true);
        field.set(obj, value);
//        field.setAccessible(false);
    }
    /**
     * 获得pathList中的dexElements
     *
     * @param obj
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), DEX_ELEMENTS_FIELD);
    }
    /**
     * 两个数组合并
     *
     * @param arrayLhs
     * @param arrayRhs
     * @return
     */
    private Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }
    /**
     * 复制修复文件到APP程序目录，同时指定复制的文件名
     * @param fromFileDir
     * @param fromFileName
     * @param toFileName
     */
    private boolean copyDexFileToApp(String fromFileDir, String fromFileName, String toFileName){
        File path = new File(fromFileDir,fromFileName);
        if (!path.exists()) {
            LogUtils.logD(TAG,"没有找到补丁文件");
            fixResultState(false);
            return false;
        }
        if (!path.getAbsolutePath().endsWith(DEX_FILE_E)){
            LogUtils.logD(TAG,"补丁文件格式不正确");
            fixResultState(false);
            return false;
        }
        File dexFile = new File(dexDirFilePath, toFileName);
        if (dexFile.exists()) {
            boolean delete = dexFile.delete();
            if(!delete) {
                context.deleteFile(dexFile.getAbsolutePath());
            }
        }
        if(!dexFile.getParentFile().exists()){
            dexFile.getParentFile().mkdirs();
        }

        //copy
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(path);
            os = new FileOutputStream(dexFile);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            path = null;
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            fixResultState(false);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }





    /****************************************格式化json获取相应数值***********************************/
    /**
     * 要修复的补丁包下载地址
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private String getDexUrl(JSONObject jsonObject) throws JSONException {
        return jsonObject.getString("dexUrl");
    }
    /**
     * 要修复的APP的versionCode
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private int getFixAppVersionCode(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt("fixAppVersionCode");
    }
    /**
     * 修复内容
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private String getFixContent(JSONObject jsonObject) throws JSONException {
        return jsonObject.getString("fixContent");
    }
    /**
     * 修复补丁的版本
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private String getFixVersion(JSONObject jsonObject) throws JSONException {
        return jsonObject.getString("fixVersion");
    }
    /**
     * 补丁包名称
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private String getDexName(JSONObject jsonObject) throws JSONException {
        return jsonObject.getString("dexName");
    }
    /**
     * 修复类型，0-不修复，1-热启动修复，2-需重启修复
     * @param jsonObject
     * @return
     * @throws JSONException
     */
    private int getFixType(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt("fixType");
    }



    /**
     * 以本地记录的信息进行修复
     */
    private void fixForLocalRecord(String recordFixInfo){
        if(!TextUtils.isEmpty(recordFixInfo) && dexDirFilePath != null && dexDirFilePath.exists()){
            isFinishFix = false;
            try {
                JSONObject jsonObject = new JSONObject(recordFixInfo);
                String dexName = getDexName(jsonObject);
                int fixAppVersionCode = getFixAppVersionCode(jsonObject);
                if(Integer.compare(fixAppVersionCode,BuildConfig.VERSION_CODE) == 0) {
                    File[] fileList = dexDirFilePath.listFiles();
                    for (File file : fileList) {
                        if (TextUtils.equals(file.getName(),dexName)) {
                            mergeFixDex(file);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                fixResultState(false);
            }
        }
    }



    /**
     * 修复结果状态
     * @param isSuccess
     */
    private void fixResultState(boolean isSuccess){
        isFinishFix = true;
        if(isSuccess){
            LogUtils.logD(TAG,"修复成功");
        }else {
            LogUtils.logD(TAG,"修复失败");
        }
        androidHotFixUtils = null;
    }

}
