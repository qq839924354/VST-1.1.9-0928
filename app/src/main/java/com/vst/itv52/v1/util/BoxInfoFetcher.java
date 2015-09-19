package com.vst.itv52.v1.util;

import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

/**
 * 盒子硬件信息获取工具类
 * 
 * @author mygica-hsj
 * 
 */
public class BoxInfoFetcher {

	/**
	 * 操作系统版本,linux内核
	 * 
	 * @return
	 */

	public String fetchBoxInfo() {
		String result = fetch_mac_eth() + "\n" + fetch_mac_wlan() + "|"
				+ fetchVersionAndroid() + "|" + fetchBoxModel() + "|"
				+ fetch_cpu_info() + "\n" + fetchVersionCore();
		// System.out.println(result);
		return result;
	}

	private String fetchVersionCore() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/proc/version" };
			result = cmdexe.run(args, "system/bin/");
			result = result.substring(0, result.indexOf("\n"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "Linux内核：" + result;
	}

	/**
	 * 获取盒子的型号
	 * 
	 * @return
	 */
	public static String fetchBoxModel() {
		return Build.MODEL;
	}

	/**
	 * 安卓版本
	 * 
	 * @return
	 */
	public static String fetchVersionAndroid() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * mac地址,无线
	 * 
	 * @return
	 */
	public static String fetch_mac_wlan() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/sys/class/net/wlan0/address" };
			result = cmdexe.run(args, "system/bin/");
			result = result.substring(0, result.indexOf("\n"));
			// 防止获取不到返回乱码
			if (result.length() > 28) {
				result = result.substring(0, 28);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	
	public static String get_user_mac() {
		String u_mac = fetch_mac_eth().trim();
		if (u_mac == null || u_mac.length() != 17){
			u_mac = fetch_mac_wlan().trim();
		}
		return u_mac;
	}
	
	/**
	 * mac地址,有线
	 * 
	 * @return
	 */
	public static String fetch_mac_eth() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/sys/class/net/eth0/address" };
			result = cmdexe.run(args, "system/bin/");
			result = result.substring(0, result.indexOf("\n"));
			// 防止获取不到返回乱码
			if (result.length() > 27) {
				result = result.substring(0, 27);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * CPU信息
	 * 
	 * @return
	 */
	private String fetch_cpu_info() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/proc/cpuinfo" };
			result = cmdexe.run(args, "/system/bin/");
			result = result.substring(0, result.lastIndexOf("\n"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "CPU信息：" + result;
	}

	/**
	 * 当前APK版本
	 * 
	 * @return
	 */
	private String fetch_apk_version(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(),
					0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String version = packInfo.versionName;
		String apkName = packInfo.applicationInfo.loadLabel(packageManager)
				.toString();
		return apkName + " 版本：" + version;
	}

}
