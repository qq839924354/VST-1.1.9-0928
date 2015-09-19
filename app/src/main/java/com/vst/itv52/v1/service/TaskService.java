package com.vst.itv52.v1.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.ApkUpdatebiz;
import com.vst.itv52.v1.biz.LiveBiz;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.model.ApkUpdateInfo;
import com.vst.itv52.v1.model.LiveDataInfo;

/**
 * 单个任务<br>
 * 1. 更新直播列表<br>
 * 2.检查更新<br>
 * 
 * @author shenhui
 * 
 */

public class TaskService extends IntentService {

	public static final String PARAM_IN_MSG = "imsg";
	public static final String PARAM_UPDATE_LIVE = "updatelive";
	public static final String PARAM_UPDATE_APK = "updateapk";

	public TaskService() {
		super("Task");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		String msg = intent.getStringExtra(PARAM_IN_MSG);
		if (msg.equals(PARAM_UPDATE_LIVE)) {

			final LiveDataInfo info = LiveBiz.parseLiveData("http://livecdn.91vst.com/tvlist");
			if (info != null) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						boolean b = LiveDataHelper
								.getInstance(TaskService.this).initLiveDB(info);
						if (b) {
							Intent i = new Intent(LiveBiz.LIVEUPDAT_ACTION);
							sendBroadcast(i);
						}
					}
				});
			}
		} else if (msg.equals(PARAM_UPDATE_APK)) {

			String hosturl = MyApp.baseServer;
			ApkUpdateInfo apkinfo = ApkUpdatebiz.parseUpdataInfo(
					TaskService.this, hosturl);
			if (apkinfo != null) {
				try {
					int localVercode = TaskService.this.getPackageManager()
							.getPackageInfo(getApplication().getPackageName(),
									0).versionCode;
					int remoteVercode = apkinfo.verCode;

					Intent bintent = new Intent(
							ApkUpdatebiz.CHECK_UPDATE_ACTION);
					if (remoteVercode > localVercode) {
						String apkpath = ApkUpdatebiz.downLoadFile(
								TaskService.this, apkinfo.apkurl,
								apkinfo.apkmd5);
						if (apkpath != null) { // 下载成功
							apkinfo.apkpath = apkpath;
							bintent.putExtra("updatemsg", apkinfo.instruction);
							bintent.putExtra("filepath", apkinfo.apkpath);
							bintent.putExtra("version", apkinfo.verCode);
						}
					}
					sendStickyBroadcast(bintent);
				} catch (Exception e) {

				}
			}

		}
	}

}
