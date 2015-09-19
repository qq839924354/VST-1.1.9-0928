package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.ApkUpdateInfo;
import com.vst.itv52.v1.util.MD5Util;

/**
 * 检测,下载，安装APK更新
 * 
 * @author mygica-hsj
 * 
 */
public class ApkUpdatebiz {

	public static final String ApkUpdatebiz_ACTION = "com.vst.itv52.ACTION.ApkUpdate";
	public static final String CHECK_UPDATE_ACTION = "com.vst.itv52.ACRION.ApkUpdate_dia";

	public static String downLoadFile(Context context, String url, String md5) {
		File apk = new File(context.getCacheDir(), "vst.apk");
		try {
			if (apk.exists()
					&& MD5Util.getFileMD5String(apk).equalsIgnoreCase(md5)) {
				return apk.getAbsolutePath();
			} else {
				byte[] data = HttpUtils.getBinary(url, null, null);
				if (data != null) {
					ByteArrayInputStream is = new ByteArrayInputStream(data);
					FileOutputStream fos = new FileOutputStream(apk);
					byte[] buf = new byte[30720]; // 30k
					int count = -1;
					while ((count = is.read(buf)) != -1) {
						fos.write(buf, 0, count);
					}
					is.close();
					fos.close();
					return apk.getAbsolutePath();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析服务器上APK版本信息
	 * 
	 * @param url
	 * @return
	 */
	// public static final String APPNAME = "appname";
	// public static final String APKNAME = "apkname";
	// public static final String VERNAME = "verName";
	// public static final String VERCODE = "verCode";
	// public static final String APKURL = "apkurl";
	// public static final String INSTRUCTION = "instruction";
	// public static final String APKMD5 = "apkmd5";
	//
	// public static final String APKPATH = "apkpath";
	// public static final String UPDATEINFO = "updateinfo";

	public static ApkUpdateInfo parseUpdataInfo(Context context, String url) {
		url = "http://update.myvst.net/62425151E239DC.json";
		if (!HttpUtils.isNetworkAvailable(context)) {
			return null;
		}
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
		try {
			System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, ApkUpdateInfo.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
