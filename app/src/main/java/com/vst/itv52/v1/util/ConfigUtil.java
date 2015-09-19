package com.vst.itv52.v1.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 配置文件工具类
 * 
 * @author shenhui
 * 
 */
public class ConfigUtil {

	public static final String VER = "VER";

	private static Properties props = new Properties();
	static {
		try {
			props.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取配置文件的值
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		return props.getProperty(key);
	}

	/**
	 * 更新配置文件
	 * 
	 * @param key
	 * @param value
	 */
	public static void updateProperties(String key, String value) {
		props.setProperty(key, value);
	}
}
