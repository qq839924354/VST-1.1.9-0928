package com.vst.itv52.v1.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.ApkUpdatebiz;
import com.vst.itv52.v1.biz.LiveBiz;
import com.vst.itv52.v1.biz.VideoCateBiz;
import com.vst.itv52.v1.biz.VideoTJBiz;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.model.ApkUpdateInfo;
import com.vst.itv52.v1.model.LiveDataInfo;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoTypeInfo;
import com.vst.itv52.v1.util.BitmapUtil;

/**
 * 下载直播数据库，检测更新<br>
 * 检测主页数据的更新 并下载 相关的数据 <br>
 * 2个小时在后台请求一次 检测<br>
 * 网络检测<br>
 * 
 * @author shenhui
 * 
 */
public class MainService extends Service implements Runnable {

	private boolean isRuning = false;
	private static final String TAG = "MainService";

	@Override
	public void onCreate() {
		super.onCreate();
		isRuning = true;
		new Thread(this).start();
		Log.d(TAG, "service ................onCreate");
	}

	@Override
	public void onDestroy() {
		isRuning = false;
		super.onDestroy();
		Log.d(TAG, "service ................onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 该线程做以下任务<br>
	 * 1.与activity 有交互的<br>
	 * 1.1 检测软件是否更新 ,并下载，发送驻留广播 ，这个可以看做没有交互的<br>
	 * 2.与activity 没交互的 ，修改数据库或者文件的<br>
	 * 2.1修改直播数据库<br>
	 * 2.2下载推荐的json文件保存到本地，并下载相应的图片保存到本地<br>
	 * 2.3下载分类的json文件保存到本地，并下载相应的图片保存到本地<br>
	 */

	@Override
	public void run() {
		while (isRuning) {
			// 得到需要的地址
			//String liveurl = "http://live.91vst.com/tvlist";
			//String liveurl = "http://live.myvst.net/list.php";//http://mylive.91vst.com/tvlist  http://livecdn.91vst.com/tvlist
			
			String liveurl = "http://livecdn.91vst.com/tvlist";
			String hosturl = MyApp.baseServer;
			// 直播
			final LiveDataInfo info = LiveBiz.parseLiveData(liveurl);
			if (info != null) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					public void run() {
						LiveDataHelper.getInstance(MainService.this)
								.initLiveDB(info);
					}
				});
			}

			// 检查更新
			try {
				ApkUpdateInfo apkinfo = ApkUpdatebiz.parseUpdataInfo(
						MainService.this, hosturl);
				if (apkinfo != null) {
					int localVercode = MainService.this.getPackageManager()
							.getPackageInfo(getApplication().getPackageName(),
									0).versionCode;
					int remoteVercode = apkinfo.verCode;
					if (remoteVercode > localVercode
							&& remoteVercode != MyApp.getForgiveVersion()) {
						String apkpath = ApkUpdatebiz.downLoadFile(
								MainService.this, apkinfo.apkurl,
								apkinfo.apkmd5);
						if (apkpath != null) { // 下载成功
							apkinfo.apkpath = apkpath;
							Intent intent = new Intent(
									ApkUpdatebiz.ApkUpdatebiz_ACTION);
							intent.putExtra("updatemsg", apkinfo.instruction);
							intent.putExtra("filepath", apkinfo.apkpath);
							intent.putExtra("version", apkinfo.verCode);
							sendStickyBroadcast(intent);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 推荐
			ArrayList<VideoInfo> tjInfos = VideoTJBiz.parseTJ(MainService.this,
					hosturl, false);
			if (tjInfos != null) {
				for (int i = 0; i < tjInfos.size(); i++) {
					VideoInfo videoInfo = tjInfos.get(i);
					if (!TextUtils.isEmpty(videoInfo.img)) {
						BitmapUtil.getBitmap(MainService.this, videoInfo.img,
								false);
					}
					if (videoInfo.logo != null&&!videoInfo.logo.isEmpty()) {
						BitmapUtil.getBitmap(MainService.this, videoInfo.logo,
								false);
					}
				}
			}

			// 分类
			ArrayList<VideoTypeInfo> typeInfos = VideoCateBiz.parseTopCate(
					MainService.this, hosturl, true);
			if (typeInfos != null) {
				for (int i = 0; i < typeInfos.size(); i++) {
					VideoTypeInfo videoTypeInfo = typeInfos.get(i);
					if (!TextUtils.isEmpty(videoTypeInfo.logo)) {
						BitmapUtil.getBitmap(MainService.this,
								videoTypeInfo.logo, false);
					}
				}
			}

			// 休息1个小时
			try {
				Thread.sleep(1000 * 60 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
