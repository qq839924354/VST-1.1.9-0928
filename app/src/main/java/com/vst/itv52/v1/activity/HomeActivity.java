package com.vst.itv52.v1.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.AllPagesAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.ApkUpdatebiz;
import com.vst.itv52.v1.biz.VideoCateBiz;
import com.vst.itv52.v1.biz.VideoTJBiz;
import com.vst.itv52.v1.broadcast.WeatherReceiver;
import com.vst.itv52.v1.broadcast.WeatherReceiver.WeatherUpdateListener;
import com.vst.itv52.v1.custom.ExitDialog;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.custom.UpdateDialog;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.effect.AnimationSetUtils;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.CityWeatherInfoBean;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoTypeInfo;
import com.vst.itv52.v1.player.LivePlayer;
import com.vst.itv52.v1.service.MainService;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.StringUtil;
import com.vst.itv52.v1.view.ApplicationLayout;
import com.vst.itv52.v1.view.LatestHotLayout;
import com.vst.itv52.v1.view.SettingsLayout;
import com.vst.itv52.v1.view.TVShowLayout;
import com.vst.itv52.v1.view.VideoTypeLayout;
import com.vst.itv52.v1.view.VodRecodLayout;

/**
 * 
 * @author shenhui
 * 
 */
public class HomeActivity extends Activity implements WeatherUpdateListener {

	private static final String TAG = "HomeActivity";
	private WeatherReceiver weatherReceiver;
	private TimeTickReciver timeTickReceiver;
	private NetStatReceiver netStatReceiver;
	private ApkUpdateReciver apkUpdateReciver;
	// private int usefulServer = 0;// 可用的服务器在服务器列表中的位置，默认选择的服务器失效的时候启用
	private ViewPager centerPager;
	private RadioGroup titleGroup;// 标签组
	private Button search;
	// private boolean tosearch = false;
	private ArrayList<View> pages = new ArrayList<View>();
	private ArrayList<VideoTypeInfo> typeInfos;
	private ArrayList<VideoInfo> tjInfos;
	private String baseUrl = MyApp.baseServer;
	private static final int MSG_DISMISS_DIALOG = 0;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DISMISS_DIALOG:
				dialog.dismiss();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout_52itv);
		if (MyApp.getAutoLive() == SettingPlay.AUTOLIVE_APPBOOT) {
			LiveChannelInfo lastChannel = LiveDataHelper.getInstance(this)
					.getChannelByVid(MyApp.getLastChannel());
			if (lastChannel != null) {
				Intent intent = new Intent(this, LivePlayer.class);
				intent.putExtra(ConstantUtil.LIVE_VID_EXTRA, lastChannel.vid);
				intent.putExtra(ConstantUtil.LIVE_TID_EXTRA, lastChannel.tid[0]);
				startActivity(intent);
			} else {
				ItvToast toast = new ItvToast(this);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setIcon(R.drawable.toast_err);
				toast.setText(R.string.toast_live_list_unexsit_auto);
				toast.show();
			}
		}
		
		Intent i = getIntent();
		typeInfos = (ArrayList<VideoTypeInfo>) i
				.getSerializableExtra("typelist");
		tjInfos = (ArrayList<VideoInfo>) i.getSerializableExtra("tjlist");

		hot = new LatestHotLayout(this);
		hot.setTopRecommends(tjInfos);
		hot.requestFocus();

		live = new TVShowLayout(this);
		cate = new VideoTypeLayout(this);
		cate.setTypePageData(typeInfos);

		fav = new VodRecodLayout(this);
		appMnanger = new ApplicationLayout(this);
		setting = new SettingsLayout(this);
		pages.add(hot);
		pages.add(live);
		pages.add(cate);
		pages.add(fav);
		pages.add(appMnanger);
		pages.add(setting);
		registerReceiver();
		initView();
		initData();
		initListener();
		startService(new Intent(this, MainService.class));
		sendBroadcast(new Intent(WeatherReceiver.RESPONSE_WEATHER));
	}

	public void initData() {
		fav.updateData();
		appMnanger.updateData();
		hot.updateData();
		live.updateData();
		cate.updateData();
	}

	private TextView systemTime;
	private TextView weatherCity;
	private ImageView weatherLog1;
	private ImageView weatherLog2;
	private ImageView topNetType;
	private TextView weatherInfo;

	private LatestHotLayout hot;
	private TVShowLayout live;
	private VideoTypeLayout cate;
	private VodRecodLayout fav;
	private ApplicationLayout appMnanger;
	private SettingsLayout setting;

	private void initView() {
		titleGroup = (RadioGroup) findViewById(R.id.title_group);
		titleGroup.check(R.id.latest_recommend);
		centerPager = (ViewPager) findViewById(R.id.main_layout_pager);
		centerPager.setAdapter(new AllPagesAdapter(pages));
		search = (Button) findViewById(R.id.main_search);
		// 当前显示第一页
		centerPager.setCurrentItem(0);
		systemTime = (TextView) findViewById(R.id.top_system_time);
		systemTime.setText(new SimpleDateFormat("HH:mm").format(new Date()));
		weatherCity = (TextView) findViewById(R.id.top_weather_city);
		weatherLog1 = (ImageView) findViewById(R.id.top_weather_log1);
		weatherLog2 = (ImageView) findViewById(R.id.top_weather_log2);
		weatherInfo = (TextView) findViewById(R.id.top_weather_info);
		topNetType = (ImageView) findViewById(R.id.top_net_type);

		if (HttpUtils.isNetworkAvailable(this)) {
			if (HttpUtils.isEthernetDataEnable(this)) {
				topNetType.setImageResource(R.drawable.et_connect_normal);
			} else if (HttpUtils.isWifiDataEnable(this)) {
				topNetType.setImageResource(R.drawable.wifi0401);
			}
		} else {
			topNetType.setImageResource(R.drawable.et_disconnected);
		}

		hot.initView();
		live.initView();
		cate.initView();
		fav.initView();
		appMnanger.initView();
		setting.initView();
	}

	private void registerReceiver() {

		weatherReceiver = new WeatherReceiver();
		registerReceiver(weatherReceiver, new IntentFilter(
				WeatherReceiver.RESPONSE_WEATHER));

		timeTickReceiver = new TimeTickReciver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		registerReceiver(timeTickReceiver, filter);

		netStatReceiver = new NetStatReceiver();
		registerReceiver(netStatReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		apkUpdateReciver = new ApkUpdateReciver();
		registerReceiver(apkUpdateReciver, new IntentFilter(
				ApkUpdatebiz.ApkUpdatebiz_ACTION));
	}

	@Override
	protected void onStart() {
		Log.i(TAG, "onStart()-----------");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop()-----------");
		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume()-----------");
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, MainService.class));
		unregisterReceiver(weatherReceiver);
		unregisterReceiver(timeTickReceiver);
		unregisterReceiver(apkUpdateReciver);
		unregisterReceiver(netStatReceiver);
		fav.destroy();
		appMnanger.destroy();
		hot.destroy();
		live.destroy();
		cate.destroy();
		super.onDestroy();
		Process.killProcess(Process.myPid());
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed>>>>>>");
		if (dialog == null) {
			dialog = new ExitDialog(this);
			dialog.setMsgLineVisible();
		}
		dialog.setTitle("现在时间："
				+ new SimpleDateFormat("HH:mm").format(new Date())
				+ "，软件使用时间："
				+ StringUtil.longTimeToString(System.currentTimeMillis()
						- MyApp.enjoyStart));
		dialog.setActivityType(2);
		dialog.updateDuration(System.currentTimeMillis() - MyApp.enjoyStart);
		dialog.matchTimePeriodSentence();
		dialog.setButtonConfirm("退出，真的不看了");
		dialog.setButtonCancle("返回，还想再看会儿");
		dialog.show();
		handler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 30000);
	}

	private ExitDialog dialog = null;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == 186) {
			Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	private class TimeTickReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println(action);
			if (action.equals(Intent.ACTION_TIME_TICK)) {
				if (systemTime == null) {
					return;
				}
				if (MyApp.enjoyStart < 10000) {
					MyApp.enjoyStart = System.currentTimeMillis();
				}
				systemTime.setText(new SimpleDateFormat("HH:mm")
						.format(new Date()));
			} else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
				MyApp.enjoyStart = System.currentTimeMillis();
			} else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
				sendBroadcast(new Intent(ConstantUtil.SERVICE_SYNCHRONIZE_DATA));
			}
		}
	}

	private class ApkUpdateReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println(action);
			if (action.equals(ApkUpdatebiz.ApkUpdatebiz_ACTION)) {
				String msg = intent.getStringExtra("updatemsg");
				String path = intent.getStringExtra("filepath");
				int verCode = intent.getIntExtra("version", 0);
				Log.d(TAG, "ApkUpdateReciver   " + " msg =" + msg + " ,path ="
						+ path + "，Version=" + verCode);
				new UpdateDialog(HomeActivity.this, UpdateDialog.UPDATE, msg,
						path, verCode).show();
				// 接受到驻留广播后移除
				removeStickyBroadcast(intent);
			}
		}
	}

	private class NetStatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				boolean isBreak = intent.getBooleanExtra(
						ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
				MyApp.isOnline = !isBreak;
				if (!isBreak) { // 有网络
					if (dialog != null && dialog.isNet() && dialog.isShowing()) {
						dialog.dismiss();
					}
					ConnectivityManager mConnMgr = (ConnectivityManager) context
							.getSystemService(Context.CONNECTIVITY_SERVICE);
					if (mConnMgr != null) {
						NetworkInfo aActiveInfo = mConnMgr
								.getActiveNetworkInfo(); // 获取活动网络连接信息
						if(aActiveInfo!=null){
						int type = aActiveInfo.getType();
						if (type == ConnectivityManager.TYPE_ETHERNET) {
							topNetType
									.setImageResource(R.drawable.et_connect_normal);
						} else if (type == ConnectivityManager.TYPE_WIFI) {
							topNetType.setImageResource(R.drawable.wifi0401);
						}
						}
						synchronizData();
					}
				} else { // 无网络
					topNetType.setImageResource(R.drawable.et_disconnected);
					showNetDialog();
				}
			}
		}
	}

	private void showNetDialog() {
		if (dialog == null) {
			dialog = new ExitDialog(this);
			dialog.setMsgLineVisible();
		}
		dialog.setIsNet(true);
		dialog.setTitle("网络未连接");
		dialog.setMessage("当前网络未连接，海量电影、电视剧等无法观看哦，现在设置网络？");
		dialog.setButtonConfirm("好，现在设置");
		dialog.setButtonCancle("算了，现在不管");
		dialog.show();
		// handler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 30000);
	}

	private void synchronizData() {
		if (tjInfos == null || typeInfos == null) {
			sendBroadcast(new Intent(WeatherReceiver.RESPONSE_WEATHER));
			tjInfos = VideoTJBiz.parseTJ(this, baseUrl, true);
			typeInfos = VideoCateBiz.parseTopCate(this, baseUrl, true);

			hot.setTopRecommends(tjInfos);
			hot.updateData();
			cate.setTypePageData(typeInfos);
			cate.updateData();
		}
	}

	public void initListener() {
		OnFocusChangeListener focusListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					int position = (Integer) v.getTag();
					centerPager.setCurrentItem(position, true);
				}
			}
		};
		for (int i = 0; i < titleGroup.getChildCount(); i++) {
			View view = titleGroup.getChildAt(i);
			view.setTag(i);
			view.setOnFocusChangeListener(focusListener);
		}

		centerPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				MyApp.playSound(ConstantUtil.PAGE_CHANGE);
				if (arg0 < titleGroup.getChildCount()) {
					((RadioButton) titleGroup.getChildAt(arg0))
							.setChecked(true);
					// titleGroup.check(id);
				}
				if (arg0 >= 4) {
					search.setVisibility(View.INVISIBLE);
				} else {
					search.setVisibility(View.VISIBLE);
				}
				if (arg0 != 1) {
					live.stopTv();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
				startActivity(intent);
			}
		});
		hot.initListener();
		live.initListener();
		cate.initListener();
		fav.initListener();
		appMnanger.initListener();
		setting.initListener();
	}

	@Override
	public void updateWeather(CityWeatherInfoBean bean) {
		if (bean == null) {
			return;
		}
		if (weatherCity != null)
			weatherCity.setText(bean.getCityName());
		if (weatherInfo != null)
			weatherInfo.setText(bean.getWeatherInfo());
		int[] ids = StringUtil.getWeaResByWeather(bean.getWeatherInfo());
		if (ids[0] != 0) {
			if (weatherLog1 != null)
				weatherLog1.setVisibility(View.VISIBLE);
			weatherLog1.setImageResource(ids[0]);
		} else {
			if (weatherLog1 != null)
				weatherLog1.setVisibility(View.GONE);
		}
		if (ids[1] != 0) {
			if (weatherLog2 != null)
				weatherLog2.setVisibility(View.VISIBLE);
			weatherLog2.setImageResource(ids[1]);
		} else {
			if (weatherLog2 != null)
				weatherLog2.setVisibility(View.GONE);
		}
		String temp = bean.getfTemp() + "~" + bean.gettTemp();
		AnimationSetUtils.SetFlickerAnimation(weatherInfo,
				bean.getWeatherInfo(), temp);
	}
}
