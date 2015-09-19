package com.vst.itv52.v1.util;

public class parseString {

	public static String getwjm(String link) {
		return link.substring(link.lastIndexOf("/") + 1).split(".")[0];
	}

	/**
	 * 截取你需要的字符串
	 * 
	 * @param str
	 *            源字符串
	 * @param start
	 *            开始字符串
	 * @param end
	 *            结束字符串
	 * @return
	 */
	public static String jiequ(String str, String start, String end) {
		if (isInString(str, start) && isInString(str, end)) {
			return str.split(start, 2)[1].split(end, 2)[0];
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param str
	 *            源字符串
	 * @param txt
	 *            需要匹配的字符串
	 * @return
	 */
	private static boolean isInString(String str, String txt) {
		String[] array = str.split(txt);
		if (array.length > 1) {
			return true;
		}
		return false;
	}
}
