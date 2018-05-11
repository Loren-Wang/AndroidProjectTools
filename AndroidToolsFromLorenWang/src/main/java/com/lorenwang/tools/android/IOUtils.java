package com.lorenwang.tools.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * IO操作工具类
 * 
 * @author yynie
 * 
 */
public final class IOUtils {
	private static final String TAG = IOUtils.class.getName();

	private static final int BUFFER_SIZE = 1024; // 流转换的缓存大小
	private static final int CONNECT_TIMEOUT = 3000; // 从网络下载文件时的连接超时时间


	/**
	 * 从指定路径的文件中读取Bytes
	 */
	public static byte[] readBytes(Context context,String path) {
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,path)){
			return new byte[]{};
		}
		try {
			File file = new File(path);
			return readBytes(context,file);
		} catch (Exception e) {
			LogUtils.logE(e);
			return null;
		}
	}

	/**
	 * 从File中读取Bytes
	 */
	public static byte[] readBytes(Context context,File file) {
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,file)){
			return new byte[]{};
		}
			FileInputStream fis = null;
			try {
				if (!file.exists()) {
					return null;
				}
				fis = new FileInputStream(file);
				return readBytes(context,fis);
			} catch (Exception e) {
				LogUtils.logE(e);
				return null;
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (Exception e) {
					LogUtils.logE(e);
				}
			}
	}

	/**
	 * 从InputStream中读取Bytes
	 */
	public static byte[] readBytes(Context context,InputStream is) {
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,is)){
			return new byte[]{};
		}
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = 0;
			while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				baos.write(buffer, 0, length);
				baos.flush();
			}
			return baos.toByteArray();
		} catch (Exception e) {
			LogUtils.logE(e);
			return null;
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (Exception e) {
				LogUtils.logE(e);
			}
		}
	}


	/**
	 * 将InputStream写入File
	 */
	public static boolean writeToFile(Context context,File file, InputStream is) {
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,file,is)){
			return false;
		}

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = 0;
			while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				fos.write(buffer, 0, length);
				fos.flush();
			}
			return true;
		} catch (Exception e) {
			LogUtils.logE(e);
			return false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				LogUtils.logE(e);
			}
			fos = null;
		}
	}

	public static boolean writeToFile(Context context,File file, String text) {
		return writeToFile(context,file, text, Xml.Encoding.UTF_8.toString(), false);
	}

	public static boolean writeToFile(Context context,File file, String text, boolean append) {
		return writeToFile(context,file, text, Xml.Encoding.UTF_8.toString(), append);
	}

	public static boolean writeToFile(Context context,File file, String text, String encoding) {
		try {
			return writeToFile(context,file, text.getBytes(encoding), false);
		} catch (UnsupportedEncodingException e) {
			LogUtils.logE(e);
			return false;
		}
	}

	public static boolean writeToFile(Context context,File file, String text, String encoding,
									  boolean append) {
		try {
			return writeToFile(context,file, text.getBytes(encoding), append);
		} catch (UnsupportedEncodingException e) {
			LogUtils.logE(e);
			return false;
		}
	}

	public static boolean writeToFile(Context context,File file, byte[] buffer) {
		return writeToFile(context,file, buffer, false);
	}

	public static boolean writeToFile(Context context,File file, byte[] buffer, boolean append) {
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,file,buffer,append)){
			return false;
		}
		FileOutputStream fos = null;
		try {
			if(file.exists()){
				file.delete();
			}
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}

			fos = new FileOutputStream(file, append);
			fos.write(buffer);
			return true;
		} catch (Exception e) {
			LogUtils.logE(e);
			return false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				LogUtils.logE(e);
			}
		}
	}

	/**
	 * 保存图片
	 * @param bitmap
	 * @param localCachePath 缓存地址
	 * @param isLocalCachePathDir 缓存地址是否是文件夹
	 */
	public static boolean saveBitmap(Context context,Bitmap bitmap, String localCachePath,boolean isLocalCachePathDir, Bitmap.CompressFormat format){
		if(!CheckUtils.checkIOUtilsOptionsPermissionAndObjects(context,bitmap,localCachePath,format)){
			return false;
		}
		String savePath = "";
		File file = new File(localCachePath);
		if(isLocalCachePathDir){
			if(file.isDirectory()){
				file.mkdirs();
				if(format.equals(Bitmap.CompressFormat.PNG)){
					savePath = localCachePath + "/" + AppUtils.generateUuid() + ".png";
				}else if(format.equals(Bitmap.CompressFormat.JPEG)){
					savePath = localCachePath + "/" + AppUtils.generateUuid() + ".jpg";
				}
			}else if(file.getAbsoluteFile().isDirectory() && !file.getAbsoluteFile().exists()){//file不是文件夹
				file.getAbsoluteFile().mkdirs();
				savePath = localCachePath;
			}else {
				savePath = localCachePath;
			}
		}else {
			if(file.isDirectory()){
				return false;
			}
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			savePath = localCachePath.intern();
		}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(savePath));
			bitmap.compress(format, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = null;
				return true;
			}
		}
		return true;
	}

	/**
	 * 复制单个文件
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static boolean copyFile(String oldPath, String newPath) {
		FileOutputStream fs = null;
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				return true;
			}else {
				return false;
			}
		}
		catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
			return false;
		}finally {
			if(fs != null){
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fs = null;
			}
		}

	}
}
