package com.vst.itv52.v1.activity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.VideoCateBiz;
import com.vst.itv52.v1.biz.VideoTJBiz;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoTypeInfo;

/**
 * 启动之前的 界面 <br>
 * 
 * 1.下载首页预备的图片，并显示进度 使用 PreWorkerTask<br>
 * 2.启动一个后台服务 做基本数据的处理,如 直播数据库初始化,检查是否有更新 MainService<br>
 * 
 * @author shenhui
 * 
 */
public class PreLancherActivity extends Activity {

	// private TextView tv;
	private Context context;
	// private ImageView topImg, bottomImg, bufImg;
	// private AnimationDrawable gif;

	private ArrayList<VideoTypeInfo> typeInfos;
	private ArrayList<VideoInfo> tjInfos;
	private String baseUrl = MyApp.baseServer;
	private Thread addData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MobclickAgent.onError(this);
		setContentView(R.layout.wait_for_data);
		context = this;
		addData=new Thread(){
			@Override
			public void run() {
				initData();
				super.run();
			}
		};
		addData.start();
		new PreWokerTask().execute(baseUrl);

	}
	
	/**
	 * 获取服务器的xml 并解析
	 */
	private void initData() {
		try {
			String xml = MyApp.curl("http://live.myvst.net/liveconfig.xml");
			Document document = new SAXReader().read(new ByteArrayInputStream(xml.getBytes("utf-8")));
			Element root = document.getRootElement();
			String S_TvPic = root.element("tvpic").getText().trim();//图片地址
			String S_Range = root.element("range").getText().trim();
			String S_Referer = root.element("referer").getText().trim();
			if (S_TvPic != null && !"".equals(S_TvPic)  && S_TvPic.length() > 7) {
				MyApp.setTvRecommend(S_TvPic);
				Log.i("info", "广告链接="+S_TvPic);
			}
			if (S_Range != null && !"".equals(S_Range ) && S_Range.length() > 1) {
				MyApp.setLive_Range(S_Range);
				Log.i("S_Range", S_Range);
			}
			if (S_Referer != null && !"".equals(S_Referer) && S_Referer.length() > 1) {
				MyApp.setLive_Referer(S_Referer);
				Log.i("S_Referer", S_Referer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// private void initView() {
	// topImg = (ImageView) findViewById(R.id.pre_top);
	// bottomImg = (ImageView) findViewById(R.id.pre_bottom);
	// bufImg = (ImageView) findViewById(R.id.pre_bufgif);
	// gif = (AnimationDrawable) bufImg.getDrawable();
	// gif.start();
	// }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	// /**
	// * 数据同步失败的提示框
	// */
	// private void showFieldDialog() {
	// AlertDialog errorDialog = new AlertDialog.Builder(
	// PreLancherActivity.this).setTitle("Sorry")
	// .setMessage("数据同步失败！").create();
	// DialogInterface.OnClickListener listener = new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// switch (which) {
	// case AlertDialog.BUTTON_POSITIVE:
	// context.startActivity(new Intent(
	// Settings.ACTION_WIRELESS_SETTINGS));
	// PreLancherActivity.this.finish();
	// break;
	// case AlertDialog.BUTTON_NEUTRAL:
	// new PreWokerTask().execute(baseUrl);
	// break;
	// case AlertDialog.BUTTON_NEGATIVE:
	// context.startActivity(new Intent(
	// Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri
	// .fromParts("package",
	// context.getPackageName(), null)));
	// PreLancherActivity.this.finish();
	// break;
	// default:
	// break;
	// }
	// }
	// };
	// errorDialog.setButton(AlertDialog.BUTTON_POSITIVE, "设置网络", listener);
	// errorDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "重试", listener);
	// errorDialog
	// .setButton(AlertDialog.BUTTON_NEGATIVE, "清除默认启动设置", listener);
	// errorDialog.show();
	// }

	/**
	 * 特定用途的AsyncTask<br>
	 * 为首页准备图片<br>
	 * 推荐页面准备显示的图片和专题背景图片，分类界面准备图片 ，并添加到 硬盘缓存和内存缓存<br>
	 * 写入基础的配置 http://v.52itv.cn/vst_cn/<br>
	 * 
	 * @author shenhui
	 * 
	 */
	class PreWokerTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			boolean isconnect = HttpUtils.isNetworkAvailable(context);
			MyApp.isOnline = isconnect;
			if (!isconnect) {
				return false;
			}
			// String baseUrl = params[0];
			boolean result = true;
			// /* 写入服务器基本配置 */
			// publishProgress("同步服务器数据...");
			// ServerInfo info = ServerBiz.parseServer(context, baseUrl);
			// if (info == null) { // 读取服务器配置失败
			// return false;
			// }
			// String hostUrl = ServerBiz.getServer(context, ServerBiz.HOSTURL);
			publishProgress("同步首页推荐数据...");
			tjInfos = VideoTJBiz.parseTJ(context, baseUrl, true);
			result &= (tjInfos != null);
			// if (result) {
			// for (int i = 0; i < tjInfos.size(); i++) {
			// VideoInfo videoInfo = tjInfos.get(i);
			// if (!TextUtils.isEmpty(videoInfo.img)) {
			// publishProgress("下载首页图片..." + (i + 1) + "/"
			// + tjInfos.size());
			// BitmapUtil.getBitmap(context, videoInfo.img,true);
			// }
			// if (videoInfo.logo != null) {
			// BitmapUtil.getBitmap(context, videoInfo.logo,true);
			// }
			// }
			// }
			publishProgress("同步分类数据...");
			typeInfos = VideoCateBiz.parseTopCate(context, baseUrl, true);
			result &= (typeInfos != null);
			// if (result) {
			//
			// for (int i = 0; i < typeInfos.size(); i++) {
			// VideoTypeInfo videoTypeInfo = typeInfos.get(i);
			// if (!TextUtils.isEmpty(videoTypeInfo.logo)) {
			// publishProgress("下载分类图片..." + (i + 1) + "/"
			// + typeInfos.size());
			// BitmapUtil.getBitmap(context, videoTypeInfo.logo,true);
			//
			// }
			// }
			// }
			publishProgress("精彩即将开始...");
			return result;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			// tv.setText(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) { // 数据同步成功
				// topImg.startAnimation(AnimationUtils.loadAnimation(context,
				// R.anim.vod_wait_top_fade_out));
				// bottomImg.startAnimation(AnimationUtils.loadAnimation(context,
				// R.anim.vod_wait_bot_fade_out));
				// topImg.setVisibility(View.INVISIBLE);
				// bottomImg.setVisibility(View.INVISIBLE);
				// bufImg.setVisibility(View.INVISIBLE);
				Intent intetn = new Intent(context, HomeActivity.class);
				intetn.putExtra("tjlist", tjInfos);
				intetn.putExtra("typelist", typeInfos);
				context.startActivity(intetn);
				PreLancherActivity.this.finish();
			} else { // 数据同步失败
				Intent intetn = new Intent(context, HomeActivity.class);
				// intetn.putExtra("tjlist", tjInfos);
				// intetn.putExtra("typelist", typeInfos);
				context.startActivity(intetn);
				PreLancherActivity.this.finish();
				// showFieldDialog();
			}
		}
	}

}
