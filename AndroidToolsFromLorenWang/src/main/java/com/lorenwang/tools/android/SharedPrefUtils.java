package com.lorenwang.tools.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Android prefence文件读写操作工具类
 * 
 * @author yynie
 * 
 */
public final class SharedPrefUtils {
	private static SharedPrefUtils sharedPrefUtils;
	private SharedPreferences mPref;
	public SharedPrefUtils(Context context) {
		mPref = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());
	}

	public static SharedPrefUtils getInstance(Context context){
		if(sharedPrefUtils == null && context != null){
			sharedPrefUtils = new SharedPrefUtils(context);
		}
		return sharedPrefUtils;
	}


	public SharedPreferences getSharedPreferences(Context context,String name) {
		if(context == null){
			return null;
		}
		return context.getApplicationContext().getSharedPreferences(name,
				Context.MODE_PRIVATE);
	}

	public SharedPreferences getSharedPreferences(Context context,String name, int mode) {
		if(context == null){
			return null;
		}
		return context.getApplicationContext().getSharedPreferences(name, mode);
	}

	public boolean clear() {
		return clear(mPref);
	}

	public boolean clear(Context context) {
		return clear(mPref);
	}

	public boolean clear(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.clear();
		return editor.commit();
	}

	public boolean remove(String key) {
		return remove(mPref, key);
	}


	public boolean remove(SharedPreferences pref, String key) {
		SharedPreferences.Editor editor = pref.edit();
		editor.remove(key);
		return editor.commit();
	}

	public boolean contains(String key) {
		return contains(mPref, key);
	}

	public boolean contains(SharedPreferences pref, String key) {
		return pref.contains(key);
	}

	/*--------------------------------------------------------------------------
	| 读数据
	--------------------------------------------------------------------------*/
	public int getInt(String key, int defValue) {
		return getInt(mPref, key, defValue);
	}


	public int getInt(SharedPreferences pref, String key, int defValue) {
		return pref.getInt(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return getLong(mPref, key, defValue);
	}


	public long getLong(SharedPreferences pref, String key, long defValue) {
		return pref.getLong(key, defValue);
	}

	public String getString(String key, String defValue) {
		return getString(mPref, key, defValue);
	}


	public String getString(SharedPreferences pref, String key,
                                   String defValue) {
		return pref.getString(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return getBoolean(mPref, key, defValue);
	}


	public boolean getBoolean(SharedPreferences pref, String key,
                                     boolean defValue) {
		return pref.getBoolean(key, defValue);
	}

	/*--------------------------------------------------------------------------
	| 写数据
	--------------------------------------------------------------------------*/
	public boolean putInt(String key, int value) {
		return putInt(mPref, key, value);
	}


	public boolean putInt(SharedPreferences pref, String key, int value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	public boolean putLong(String key, long value) {
		return putLong(mPref, key, value);
	}


	public boolean putLong(SharedPreferences pref, String key, long value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	public boolean putString(String key, String value) {
		return putString(mPref, key, value);
	}

	public boolean putString(SharedPreferences pref, String key,
                                    String value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	public boolean putBoolean(String key, boolean value) {
		return putBoolean(mPref, key, value);
	}

	public boolean putBoolean(SharedPreferences pref, String key,
                                     boolean value) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}
}
