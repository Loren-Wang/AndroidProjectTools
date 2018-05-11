package com.lorenwang.tools.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.text.NumberFormat;
import java.util.List;

/**
 * 数据检查工具
 * @author yynie
 * 
 */
public class CheckUtils {
    public static final String EXP_a_z = "[a-z]*";//匹配所有的小写字母
    public static final String EXP_A_Z = "[A-Z]*";//匹配所有的大写字母
    public static final String EXP_a_z_A_Z = "[a-zA-Z]*";//匹配所有的字母
    public static final String EXP_0_9 = "[0-9]*";//匹配所有的数字
    public static final String EXP_ALL_INTEGET_NOT_AND_ZERO = "[1-9]{1}[0-9]*";//匹配所有的数字(不包含0)
    public static final String EXP_0_9_a_z = "[0-9a-z]*";
    public static final String EXP_0_9_a_z_A_Z = "[0-9a-zA-Z]*";
    public static final String EXP_0_9_a_z__ = "[0-9a-z_]*";
    public static final String EXP_EMAIL = "^([a-z0-9A-Z_]+[_|\\-|\\.]?)+[a-z0-9A-Z_]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";//EMAIL
    public static final String EXP_PRICE = "^([1-9]\\d+|[1-9])(\\.\\d\\d?)*$";//金额，2位小数
    public static final String EXP_MOBILE = "[1]{1}[0-9]{10}";//11位数的手机号码
    public static final String EXP_POSTALCODE = "[0-9]{6}";//6位数的邮编
    public static final String EXP_TEL = "[0-9]{3,4}[-]{1}[0-9]{7,8}";//电话号码：( 如021-12345678 or 0516-12345678 )
    public static final String EXP_IP = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";//匹配IP地址
    public static final String EXP_CHINESE = "[\u4e00-\u9fa5]*";//匹配中文
    public static final String EXP_0_9_a_z_A_Z_CHINESE = "[0-9a-zA-Z\u4e00-\u9fa5]*";//匹配中文,数字,小写字母,大写字母
    public static final String EXP_0_9_a_z_A_Z_CHINESE_DOT = "[.·0-9a-zA-Z\u4e00-\u9fa5]*";//匹配中文,数字,小写字母,大写字母
	public static final String EXP_ALL_INTEGET_AND_ZERO = "^-?[0-9]\\d*$";//所有的整数包括0
	public static final String EXP_CAR_LICENSE_NUM = "[a-zA-Z]{1}[0-9a-zA-Z]{5,6}";//匹配车牌号
	public static final String EXP_OBD_SERIAL_NUM = "[0-9a-zA-Z]{4,6}";//匹配obd的序列号
	public static final String EXP_OBD_ACTIVATION_NUM = "[0-9a-zA-Z]{6}";//匹配obd的邀请码
    public static final String EXP_URL = "^[a-zA-z]+://[^><\"' ]+";
    public static final String EXP_MAC = "[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}";
    public static final String EXP_DATE = "[0-9]{4}[-]{1}[0-9]{1,2}[-]{1}[0-9]{1,2}";
    public static final String EXP_DATETIME = "[0-9]{4}[-]{1}[0-9]{1,2}[-]{1}[0-9]{1,2}[ ]{1}[0-9]{1,2}[:]{1}[0-9]{1,2}";
    public static final String EXP_DATETIMESECOND = "[0-9]{4}[-]{1}[0-9]{1,2}[-]{1}[0-9]{1,2}[ ]{1}[0-9]{1,2}[:]{1}[0-9]{1,2}[:]{1}[0-9]{1,2}";
    public static final String DATESTRING_TAIL = "000000000";
    public static final String LOGIN_OR_REG_PWD = "[0-9]{4}";//登录或者注册的时候密码或验证码正则
    public static final String CHAT_MSG_DEFAULT_EMOJI = "[[\u4e00-\u9fa5]]";//聊天中表情文件名正则（不包含文件路径）


	//扫描正则，二维码扫描用户资料的正则
	public static final String SCAN_RESULT_MATCH_FOR_PEOPLE = "((http|https)://)[^\\s]*&&&\\d*";
	//二维码扫描ve_box正则，具体如下：[公司缩写][日期编码][产品序列号]；公司缩写为YL；日期编码为4位数字，值固定为0176；产品序列号由6位数字组成，从000001至999999；
	public static final String SCAN_RESULT_MATCH_FOR_VE_BOX = "[a-zA-Z]{1}[0-9a-zA-Z]{11}#[0-9a-zA-Z]{4,}";
	//二维码扫描ve_air正则,具体如下：[产品编号][产品序列号];产品编码由3位数字组成，101表示VE-AIR，102表示VE-SEE后续产品依此类推（VE-BOX产品例外）;产品序列号由9位数字组成，从000000001至999999999
	public static final String SCAN_RESULT_MATCH_FOR_VE_AIR = "101[0-9]{9}#[0-9a-zA-Z]{4,}";
	//ve_box序列号正则
	public static final String IMEI_FOR_VE_BOX = "[a-zA-Z]{1}[0-9a-zA-Z]{11}";
	//ve_air序列号正则
	public static final String IMEI_FOR_VE_AIR = "101[0-9]{9}";




    /*--------------------------------------------------------------------------
	| 检查字符串
	--------------------------------------------------------------------------*/
	/**
	 * 判断字符串是否为空
	 * @param str String
	 * @return boolean
	 */
	public static boolean isEmpty(String str) {
		return (str == null || "".equals(str)) ? true : false;
	}

	/**
	 * 判断是否符合指定的正则表达式 eg: [^0-9A-Za-z]
	 * @param str String
	 * @param patternStr String
	 * @return boolean
	 */
	public static boolean matches(String str, String patternStr) {
		if (isEmpty(str)) {
			return false;
		}
		return str.matches(patternStr);
	}

	/**
	 * 判断字符串是否是日期 2002-02-02
	 * @param str String
	 * @return boolean
	 */
	public static boolean isDate(String str) {
		if (isEmpty(str)) {
			return false;
		}
		return str.matches(EXP_DATE);
	}

	/**
	 * 判断字符串是否是日期+时间 2002-02-02 10：30
	 * @param str String
	 * @return boolean
	 */
	public static boolean isDateTime(String str) {
		if (isEmpty(str)) {
			return false;
		}
		return str.matches(EXP_DATETIME);
	}

	/**
	 * 判断字符串是否是金额 eg: 12.00
	 * @param str String
	 * @return boolean
	 */
	public static boolean isMoney(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Double.valueOf(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断字符串是否是整型
	 * @param str String
	 * @return boolean
	 */
	public static boolean isInteger(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Integer.valueOf(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断字符串是否是长整型
	 * @param str String
	 * @return boolean
	 */
	public static boolean isLong(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Long.valueOf(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断字符串是否是浮点数
	 * @param str String
	 * @return boolean
	 */
	public static boolean isDouble(String str) {
		if (isEmpty(str)) {
			return false;
		}
		try {
			Double.valueOf(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*--------------------------------------------------------------------------
	| 检查超长
	--------------------------------------------------------------------------*/
	/**
	 * 字符串是否超长
	 * @param str String
	 * @param len int
	 * @return boolean
	 */
	public static boolean isOverLength(String str, int len) {
		if (isEmpty(str)) {
			return false;
		}
		return str.length() > len ? true : false;
	}

	/**
	 * Double类型是否超长
	 * @param d Double
	 * @param len int
	 * @return boolean
	 */
	public static boolean isOverLength(Double d, int len) {
		if (d == null) {
			return false;
		}
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setGroupingUsed(false); // 是否对结果分组（即使用","分组）
		formatter.setMaximumFractionDigits(0); // 小数位数最大值
		formatter.setMinimumFractionDigits(0); // 小数位数最小值
		if (formatter.format(d.doubleValue()).length() > len) {
			return true;
		}
		return false;
	}

	/**
	 * 判断对象是否基本类型
	 * @author wfc
	 * @param object
	 * @return
	 */
	public static boolean isBaseType(Object object) {
		if (object instanceof Integer ||
				object instanceof Double ||
				object instanceof String ||
				object instanceof Character ||
				object instanceof Byte ||
				object instanceof Long ||
				object instanceof Float ||
				object instanceof Boolean ||
				object instanceof Short) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否在列表中
	 * @param item
	 * @param list
	 * @return
	 */
	public static boolean isInList(String item, List<String> list) {
		for (String listItem : list) {
			if (item.equals(listItem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断对象是否在列表中
	 * @param <T>
	 * @param item
	 * @param list
	 * @return
	 */
	public static <T> boolean isInList(T item, List<T> list) {
		for (T listItem : list) {
			if (item.equals(listItem)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断对象是否在数组中
	 * @param <T>
	 * @param item
	 * @param list
	 * @return
	 */
	public static <T> boolean isInArray(T item, T[] list) {
		for (T listItem : list) {
			if (item.equals(listItem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查传入的路径是否是图片
	 * @param path
	 * @return
	 */
	public static boolean checkIsImage(String path){
		if(path != null) {
			if(path.length() > 4){
				if(path.toLowerCase().substring(path.length() - 4).contains(".jpg")
						|| path.toLowerCase().substring(path.length() - 4).contains(".png")
						|| path.toLowerCase().substring(path.length() - 4).contains(".bmp")
						|| path.toLowerCase().substring(path.length() - 4).contains(".gif")
						|| path.toLowerCase().substring(path.length() - 4).contains(".psd")
						|| path.toLowerCase().substring(path.length() - 4).contains(".swf")
						|| path.toLowerCase().substring(path.length() - 4).contains(".svg")
						|| path.toLowerCase().substring(path.length() - 4).contains(".pcx")
						|| path.toLowerCase().substring(path.length() - 4).contains(".dxf")
						|| path.toLowerCase().substring(path.length() - 4).contains(".wmf")
						|| path.toLowerCase().substring(path.length() - 4).contains(".emf")
						|| path.toLowerCase().substring(path.length() - 4).contains(".lic")
						|| path.toLowerCase().substring(path.length() - 4).contains(".eps")
						|| path.toLowerCase().substring(path.length() - 4).contains(".tga")){
					return true;
				}else if(path.length() > 5){
					if(path.toLowerCase().substring(path.length() - 5).contains(".jpeg")
							|| path.toLowerCase().substring(path.length() - 5).contains(".tiff")){
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * 检查传入的路径是否是图片
	 * @param path
	 * @return
	 */
	public static boolean checkIsVideo(String path){
		if(path != null) {
			if(path.length() > 4){
				if(path.toLowerCase().substring(path.length() - 4).contains(".mp4")){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检查io操作工具类权限以及传入参数
	 * @param context 一定要传入applicationcontext
	 * @param objects
	 * @return
	 */
	public static boolean checkIOUtilsOptionsPermissionAndObjects(Context context,Object... objects){
		if(context == null){
			return false;
		}
		context = context.getApplicationContext();
		for(Object object : objects){
			if(object == null){
				return false;
			}
		}

		if (Build.VERSION.SDK_INT >= 23) {
			if (ActivityCompat.checkSelfPermission(context
					, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					|| ActivityCompat.checkSelfPermission(context
					, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				return false;
			} else {
				return true;
			}
		}else {
			return true;
		}

	}

	/**
	 * 判断传入参数是否为空是否为空
	 * @param object object
	 * @return boolean
	 */
	public static boolean isNotEmptyOrNull(Object object) {
		if(object == null){
			return false;
		}else if(object instanceof String && ("".equals(object) || ((String)object).isEmpty())){
			return false;
		}else {
			return true;
		}
	}

}
