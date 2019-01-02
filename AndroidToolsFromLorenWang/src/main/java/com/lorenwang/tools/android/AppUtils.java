package com.lorenwang.tools.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;

import com.lorenwang.tools.android.base.LogUtils;

import java.io.File;
import java.util.UUID;


/**
 * 应用工具类
 *
 * @author yynie
 * @since 2013-09-23
 */
public final class AppUtils {

	private static final String TAG = "AppUtils";


	/*uuid产生器*/
	public static String generateUuid(){
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid ;
	}






	/**
	 * 安装应用
	 * @param context
	 * @param filePath
	 */
	public static void installApp(Context context,String authority,String filePath){
		Intent intent = getInstallAppIntent(context, authority, filePath);
		if(intent != null){
			context.getApplicationContext().startActivity(intent);
		}
	}

	/**
	 * 获取App安装的intent
	 * @param context s上下文
	 * @param installAppFilePath 安卓文件地址
	 * @param authority The authority of a {@link FileProvider} defined in a
	 *            {@code <provider>} element in your app's manifest.
	 * @return
	 */
	public synchronized static Intent getInstallAppIntent(Context context,String authority,String installAppFilePath){
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(installAppFilePath)), "application/vnd.android.package-archive");
				return intent;
			}else {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File file = (new File(installAppFilePath));
				// 由于没有在Activity环境下启动Activity,设置下面的标签
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
				Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(), authority, file);
				//添加这一句表示对目标应用临时授权该Uri所代表的文件
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
				return intent;
			}
		}catch (Exception e){
			LogUtils.logD(TAG,"安装异常：" + e.getMessage());
			return null;
		}
	}

	/**
	 * 使设备震动
	 */
	@RequiresPermission(Manifest.permission.VIBRATE)
	public static void vibrate(Context context, long milliseconds) {
		try {
			Vibrator vibrator = (Vibrator) context
					.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(milliseconds);
		} catch (Exception e) {
			LogUtils.logE(e);
		}
	}

	/**
	 * 获取设备型号
	 * @param context
	 * @return
	 */
	public static String getPhoneModel(Context context) {
		String device_model = Build.FINGERPRINT; // 设备型号 。
		return device_model;
	}

	public static void makeCall(Context context, String phoneNo) {
		if(phoneNo != null && !"".equals(phoneNo)) {
			String number = "tel:" + phoneNo;
			try {
				Intent callIntent = new Intent(Intent.ACTION_DIAL);
				callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				callIntent.setData(Uri.parse(number));
				context.startActivity(callIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取权限
	 */
	public static void getPermissions(Activity activity, String permission, int requestCode) {
		//具有权限
		if (selfPermissionGranted(activity,permission)) {
		} else {
			//不具有获取权限，需要进行权限申请
			ActivityCompat.requestPermissions(activity, new String[]{permission},requestCode);
		}
	}

	public static boolean selfPermissionGranted(Context context, String permission) {
		// For Android < Android M, self permissions are always granted.
		boolean result = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			if (getTargetVersion(context) >= Build.VERSION_CODES.M) {
				// targetSdkVersion >= Android M, we can
				// use Context#checkSelfPermission
				result = ContextCompat.checkSelfPermission(context,permission)
						== PackageManager.PERMISSION_GRANTED;
			} else {
				// targetSdkVersion < Android M, we have to use PermissionChecker
				result = PermissionChecker.checkSelfPermission(context,permission)
						== PermissionChecker.PERMISSION_GRANTED;
			}
		}

		return result;
	}

	private static int getTargetVersion(Context context){
		int targetSdkVersion = 0;
		try {
			final PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			targetSdkVersion = info.applicationInfo.targetSdkVersion;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return targetSdkVersion;
	}
}
