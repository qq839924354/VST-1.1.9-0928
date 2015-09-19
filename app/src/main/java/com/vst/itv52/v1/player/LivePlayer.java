package com.vst.itv52.v1.player;

import java.util.ArrayList;

import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.BaseActivity;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.https.NetWorkHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.util.BoxInfoFetcher;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.StringUtil;

public class LivePlayer extends BaseActivity implements OnErrorListener,
		OnPreparedListener, OnCompletionListener,
		BaseActivity.NumKeyClickListener {
	public static final String TAG = "LivePlayer";
	private VideoView mVideoView;

	private ArrayList<LiveChannelInfo> channels;
	private int channelIndex;
	private int tvback_num = 0;
	private int Error_Player = 0;
	private LiveDataHelper dbHelper;
	private LiveControl control = null;
	private LiveChannelList channellist = null;
	private PlayerMenuContrl menuContrl;
	private TextView tipText;
	// private static final long TIMEOUT = 30000; // 超时时间 ms
	private WindowManager wm;
	private WindowManager.LayoutParams p;
	private int scaleMod;
	// private long enjoyStart = System.currentTimeMillis();
	private long requesTime = 0;// 发起请求的时间

	private long startTime = 0; // 频道播放开始时间
	private long endTime = 0; // 频道播放结束时间

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//progressDismiss();
		scaleMod = MyApp.scaleMod;
		System.out.println("scaleMod = " + scaleMod);
		setOnNumKeyClickListener(this);
		MyApp.setUserMac(BoxInfoFetcher.get_user_mac());
		mVideoView = new VideoView(this);
		mVideoView.setOnPreparedListener(this);
		// mVideoView.setOnInfoListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1,
				Gravity.CENTER);
		setContentView(mVideoView, lp);
		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		tipText = new TextView(this);
		tipText.setTextColor(0xffFCD208);
		tipText.setBackgroundColor(Color.TRANSPARENT);
		tipText.setGravity(Gravity.CENTER);
		p = new LayoutParams();
		p.gravity = Gravity.TOP | Gravity.RIGHT;
		p.type = LayoutParams.TYPE_APPLICATION_PANEL;
		p.format = PixelFormat.TRANSPARENT;
		p.width = 200;
		p.height = 80;
		p.y = 50;
		p.x = 50;
		p.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_NOT_TOUCHABLE;

		dbHelper = LiveDataHelper.getInstance(this);
		//progressShow();
		initDialog();
		initController();
		initData();
	}

	/**
	 * 初始化数据
	 */

	private void initData() {
		Intent i = getIntent();
		String tid = i.getStringExtra(ConstantUtil.LIVE_TID_EXTRA);
		int vid = i.getIntExtra(ConstantUtil.LIVE_VID_EXTRA, -1);
		// System.out.println("tid = " + tid + " , vid = " + vid);
		if (tid == null) {
			tid = "(all)";
		}
		updateData(tid, vid);
	}

	private void updateData(String tid, int vid) {
		// System.out.println("tid = " + tid + " , vid = " + vid);
		channels = dbHelper.getChannelListByTid(tid);
		if (channels == null && channels.isEmpty()) {
			return;
		}
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).vid == vid) {
				channelIndex = i;
				break;
			}
		}
		setControlInfo();
	}

	private void setControlInfo() {
		// 设置 控制器内容
		LiveChannelInfo channelInfo = channels.get(channelIndex);
		control.setChannelName(channelInfo.vname);
		control.setSource("源" + (channelInfo.lastSource + 1) + "/"
				+ channelInfo.liveSources.length);
		control.setEPG(String.valueOf(channelInfo.epgid));
		control.setChannelNum(channelInfo.num + "");
	}

	/**
	 * 初始化 控制界面
	 */
	private void initController() {
		control = new LiveControl(this, mHandler);
		channellist = new LiveChannelList(this, mHandler);
		control.startTestSpeeed();
	}

	@Override
	public void onStart() {
		Log.i(TAG, "------------onStart-------------");
		super.onStart();
		LiveChannelInfo info = channels.get(channelIndex);
		String url = info.getSourceUrl(info.lastSource);
		if (NetWorkHelper.isNetworkAvailable(this)) {
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
		} else {// 没有联网，延迟播放
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MSG_PLAY_LIVE, url), 5000);
		}

	}

	public void onResume() {
		Log.i(TAG, "--------------onResume----------");
		mVideoView.start();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		Log.i(TAG, "-------------onPause------------");
		if (startTime != 0) {
			endTime = System.currentTimeMillis();
		}
		mVideoView.pause();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "----------------onDestroy-----------------");
		MyApp.setLastChannel(channels.get(channelIndex).vid);
		mHandler.removeCallbacks(auto);
		control.dismiss();
		if (tipText.getParent() != null)
			wm.removeView(tipText);
		mVideoView.stopPlayback();
		control.relese();
		channellist.relese();
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (control.isShown()) {
			control.dismiss();
		}
		if (channellist != null && channellist.isShowing()) {
			channellist.dismiss();
		}

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// System.out.println(keyCode);
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
				&& event.getRepeatCount() == 0) {
			/* menu 显示换台 */
			Message msg = mHandler
					.obtainMessage(LivePlayer.MSG_LIVE_CHANNEL_LIST);
			Bundle data = new Bundle();
			data.putString(ConstantUtil.LIVE_TID_EXTRA, null);
			data.putInt(ConstantUtil.LIVE_INDEX_EXTRA, -1);
			msg.setData(data);
			mHandler.sendMessage(msg);
			if (control.isShown()) {
				control.dismiss();
			}
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_MENU) {
			if (menuContrl == null) {
				menuContrl = new PlayerMenuContrl(this, mHandler,
						ConstantUtil.MENU_LIVE);
			}
			if (menuContrl.isShowing()) {
				menuContrl.dismiss();
			} else {
				if (control.isShown()) {
					control.dismiss();
				}
				menuContrl.showVoiceLevel(menuContrl.getVoice());
				menuContrl.setScalor(scaleMod);
				menuContrl.updateLiveMenu(getLiveSourceSize());
				matchMenuFavText();
				menuContrl.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
				count = 1;
			}
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_DPAD_UP
				&& event.getRepeatCount() == 0) {
			if (MyApp.liveUdFunction == 0) {
				nextChannel();
			} else {
				previousChannel();
			}
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				&& event.getRepeatCount() == 0) {
			if (MyApp.liveUdFunction == 0) {
				previousChannel();
			} else {
				nextChannel();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (MyApp.liveLrFunction == 0) {
				call_Audio(INDUCE_VOICE);
			} else {
				// mHandler.removeMessages(MSG_SOURCE_PREVOURES);
				// mHandler.sendEmptyMessageDelayed(MSG_SOURCE_PREVOURES, 500);
				previousSource();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (MyApp.liveLrFunction == 0) {
				call_Audio(ADD_VOICE);
			} else {
				nextSource();
				// mHandler.removeMessages(MSG_SOURCE_NEXT);
				// mHandler.sendEmptyMessageDelayed(MSG_SOURCE_NEXT, 500);
			}
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == 186) { // 搜索键切换回看到上个频道
			Back_LiveNum();
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& (keyCode == 183 || keyCode == 17)) { // 红色按键，收藏，取消收藏
			if (!control.isShown()) {
				favCurrentChannel();
			}
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& (keyCode == 184 || keyCode == 18)) { // 绿色按键，弹出EPG
			if (control.isShown()) {
				control.dismiss();
			} else {
				control.show();
			}
			return true;
		} else if (keyCode == 185) { // 185 是比例 A11
			/**
			 * 其他按键
			 */
			changScale(ConstantUtil.OPERATE_RIGHT);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (control.isShown()) {
				control.dismiss();
			} else if (isRunning()) {
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private static final int ADD_VOICE = 1;
	private static final int INDUCE_VOICE = 0;

	private void call_Audio(int stype) {
		AudioManager audioMa = (AudioManager) LivePlayer.this
				.getSystemService(Context.AUDIO_SERVICE);
		if (stype == INDUCE_VOICE) {
			audioMa.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
		} else if (stype == ADD_VOICE) {
			audioMa.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
		}
	}

	protected int getLiveSourceSize() {
		return channels.get(channelIndex).liveSources.length;
	}

	protected String changScale(int derection) {
		if (derection == ConstantUtil.OPERATE_RIGHT) {
			scaleMod += 1;
		} else if (derection == ConstantUtil.OPERATE_LEFT) {
			if (scaleMod > 0) {
				scaleMod--;
			} else {
				scaleMod = 3;
			}
		}
		scaleMod = scaleMod % 3;
		String text = null;
		if (scaleMod == VideoView.A_DEFALT) {
			text = "原始比例";
		} else if (scaleMod == VideoView.A_4X3) {
			text = "4:3";
		} else if (scaleMod == VideoView.A_16X9) {
			text = "16:9";
		}
		// else if (scaleMod == VideoView.A_RAW) {
		// text = "原 始大小";
		// }
		// int x = getWindow().getDecorView().getWidth();
		// int y = getWindow().getDecorView().getHeight();
		mVideoView.selectScales(scaleMod);
		p.width = 300;
		p.height = 100;
		TipTextShow(text, 25f);
		return text;
	}

	private void TipTextShow(String text, float size) {
		tipText.setText(text);
		tipText.setTextSize(size);
		if (tipText.getParent() == null) {
			if (isRunning()) {
				wm.addView(tipText, p);
			}
		} else {
			// p.width += 1;
			wm.updateViewLayout(tipText, p);
		}
		mHandler.removeMessages(MSG_HIDE_SCAL);
		mHandler.sendEmptyMessageDelayed(MSG_HIDE_SCAL, 3000);
	}

	/**
	 * 上一个台
	 */
	private void previousChannel() {

		if (startTime != 0) {
			endTime = System.currentTimeMillis();
			recodePlayDuration(channels.get(channelIndex).vid);
		}
		if (channelIndex > 0) {
			tvback_num = channels.get(channelIndex).num;
			channelIndex = channelIndex - 1;
			LiveChannelInfo info = channels.get(channelIndex);
			String url = info.getSourceUrl(info.lastSource);
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
		} else {
			/* 最上面的台了 */
			channellist.changeListLeft();// 跟换列表
			channels = channellist.liveInfos;// 取得新的列表
			while (channels == null) {// 如果列表为空，继续换列表，直到不为空
				channellist.changeListLeft();
				channels = channellist.liveInfos;
			}
			channelIndex = channels.size() - 1;// 取得列表的最后位置
			LiveChannelInfo info = channels.get(channelIndex);
			String url = info.getSourceUrl(info.lastSource);
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
		}
		if (menuContrl == null) {
			menuContrl = new PlayerMenuContrl(this, mHandler,
					ConstantUtil.MENU_LIVE);
		}
		TipTextShow(channels.get(channelIndex).num + "", 60f);
		menuContrl
				.updateLiveMenu(channels.get(channelIndex).liveSources.length);
	}

	/**
	 * 下个台
	 */

	private void nextChannel() {
		if (startTime != 0) {
			endTime = System.currentTimeMillis();
			recodePlayDuration(channels.get(channelIndex).vid);
		}
		if (channelIndex < channels.size() - 1) {
			tvback_num = channels.get(channelIndex).num;
			channelIndex = channelIndex + 1;
			LiveChannelInfo info = channels.get(channelIndex);
			String url = info.getSourceUrl(info.lastSource);
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
		} else {
			/* 最下面的台了 */
			channellist.changeListRight();
			channels = channellist.liveInfos;
			while (channels == null) {// 如果列表为空，继续换列表，直到不为空
				channellist.changeListRight();
				channels = channellist.liveInfos;
			}
			channelIndex = 0;
			LiveChannelInfo info = channels.get(channelIndex);
			String url = info.getSourceUrl(info.lastSource);
			mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
		}
		if (menuContrl == null) {
			menuContrl = new PlayerMenuContrl(this, mHandler,
					ConstantUtil.MENU_LIVE);
		}
		TipTextShow(channels.get(channelIndex).num + "", 60f);
		menuContrl
				.updateLiveMenu(channels.get(channelIndex).liveSources.length);
	}

	/**
	 * 下个源
	 */
	protected void nextSource() {

		LiveChannelInfo info = channels.get(channelIndex);
		if (info.liveSources.length > 1) {
			if (startTime != 0) {
				endTime = System.currentTimeMillis();
				// 记录播放时间
				recodePlayDuration(channels.get(channelIndex).vid);
			}
			if (info.lastSource < info.liveSources.length - 1) {
				int i = info.lastSource + 1;
				info.lastSource = i;
				String url = info.getSourceUrl(i);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
			} else {
				info.lastSource = 0;
				String url = info.getSourceUrl(info.lastSource);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
			}
		}
	}

	/**
	 * 上个源
	 */
	protected void previousSource() {
		LiveChannelInfo info = channels.get(channelIndex);
		if (info.liveSources.length > 1 || info.lastSource > 0) {
			if (startTime != 0) {
				endTime = System.currentTimeMillis();
				// 记录播放时间
				recodePlayDuration(channels.get(channelIndex).vid);
			}
			if (info.lastSource > 0) {
				info.lastSource -= 1;
				String url = info.getSourceUrl(info.lastSource);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
			} else {
				info.lastSource = info.liveSources.length - 1;
				String url = info.getSourceUrl(info.lastSource);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
			}
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		requesTime = System.currentTimeMillis();
		LiveChannelInfo channel = channels.get(channelIndex);
		if (channel.lastSource < channel.liveSources.length - 1) {
			mVideoView.stopPlayback();
			nextSource(); // 自动切源
		} else {
			mHandler.sendEmptyMessage(MSG_ERROR);
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(TAG, "onPrepared-----------");
		progressDismiss();
		control.setState(LiveControl.PREPARED);
		startTime = System.currentTimeMillis(); // 播放预处理完成 开始记录开始时间
		dbHelper.updateLastSoure(channels.get(channelIndex).vid,
				channels.get(channelIndex).lastSource);
		control.setCWTime(StringUtil.longToSec(System.currentTimeMillis()
				- requesTime));
	}

	// 播放流 结束 说明 网络缓慢 或者 断开
	@Override
	public void onCompletion(MediaPlayer mp) {
		MyApp.setApkRunTime(MyApp.enjoyStart - System.currentTimeMillis());
		mHandler.sendEmptyMessage(MSG_ERROR);
	}

	public static final int MSG_CHANGECHANNEL = 0; // 换台
	public static final int MSG_CHANGESOURCE = 1; // 换源
	public static final int MSG_LIVE_CHANNEL_LIST = 2; // 频道列表
	public static final int MSG_SOURCELIST = 6; // 超时信息

	public static final int MSG_PLAY_LIVE = 7; // 超时信息
	public static final int MSG_ERROR = 8; // 超时信息
	public static final int MSG_HIDE_SCAL = 10; // 超时信息
	public static final int MSG_SOURCE_NEXT = 11;
	public static final int MSG_SOURCE_PREVOURES = 12;
	private int count = 1;
	private long responseTime;
	private Handler mHandler = new Handler() { // 在activity中 处理所有的操作
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CHANGECHANNEL: // 换台
				LiveChannelInfo channel = (LiveChannelInfo) msg.obj;
				tvback_num = channels.get(channelIndex).num;
				int tempvid = channels.get(channelIndex).vid;
				updateData(channel.tid[0], channel.vid);
				if (channels.get(channelIndex).vid != tempvid) {
					String url = channel.getSourceUrl(channels
							.get(channelIndex).lastSource);
					mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE,
							url));
				}
				break;
			case MSG_LIVE_CHANNEL_LIST:
				Bundle data = msg.getData();
				String tid = data.getString(ConstantUtil.LIVE_TID_EXTRA);
				int index = data.getInt(ConstantUtil.LIVE_INDEX_EXTRA);
				if (tid == null) {
					tid = channels.get(channelIndex).tid[0];
				}
				if (index == -1) {
					index = channelIndex;
				}
				channellist.refreshView(tid, index, ConstantUtil.OPERATE_RIGHT);
				channellist.showAtLocation(mVideoView, Gravity.LEFT, 20, 0);
				break;
			case MSG_PLAY_LIVE:
				setControlInfo();
				requesTime = System.currentTimeMillis();
				if (count == 1) {
					final String url = (String) msg.obj;
					// System.out.println("live url = " + url);
					if (url != null) {
						Error_Player = 0;
						mVideoView.stopPlayback();
						mVideoView.setVideoURI(Uri.parse(url));
						mVideoView.start();
					}
					count = 2;
					responseTime = System.currentTimeMillis();
				} else if (System.currentTimeMillis() - responseTime > 500) {
					final String url = (String) msg.obj;
					// System.out.println("live url = " + url);
					if (url != null) {
						Error_Player = 0;
						mVideoView.stopPlayback();
						mVideoView.setVideoURI(Uri.parse(url));
						mVideoView.start();
					}
				}
				control.setState(LiveControl.PREPARING);
				if (isRunning()) {
					if (errorDialog != null && errorDialog.isShowing()) {
						errorDialog.dismiss();
					}
					control.show();
					responseTime = System.currentTimeMillis();
				}

				control.setState(LiveControl.PREPARING);
				if (isRunning()) {
					if (errorDialog != null && errorDialog.isShowing()) {
						errorDialog.dismiss();
					}
					control.show();
				}
				// count=1;

				break;
			case MSG_ERROR:
				if (isRunning()) {
					if (mVideoView != null) {
						if (mVideoView.isPlaying()) {
							mVideoView.stopPlayback();
						}
						requesTime = System.currentTimeMillis();
						if (msg.obj != null) {
							if (Error_Player > 3 || MyApp.LiveSeek != "0") {
								mVideoView.setVideoURI(Uri
										.parse(MyApp.LiveNextUrl));
							} else {
								mVideoView.setVideoURI(Uri
										.parse((String) msg.obj));
							}
							Error_Player = Error_Player + 1;
							mVideoView.start();
						}
					}
				}
				break;
			case MSG_HIDE_SCAL:
				if (tipText.getParent() != null)
					wm.removeView(tipText);
				break;
			// case MSG_DISMISS_EXIT:
			// exitDialog.dismiss();
			// break;
			case MSG_SOURCE_NEXT:// 下一个源
				nextSource();
				break;
			case MSG_SOURCE_PREVOURES:// 上一个源
				previousSource();
				break;
			default:
				break;
			}
		}
	};

	Runnable auto = new Runnable() {
		@Override
		public void run() {
			nextChannel();
		}
	};

	/**
	 * 记录播放时长
	 */
	private void recodePlayDuration(int vid) {

		if (startTime != 0 && endTime != 0 && startTime < endTime) { // 有起始结束时间
			long duration = endTime - startTime;
			dbHelper.updatePlayDuration(vid, duration);
		}
		// 重置
		startTime = 0;
		endTime = 0;
	}

	private Dialog errorDialog;
	TextView tv;

	private void initDialog() {
		errorDialog = new Dialog(this, R.style.MyDialog);
		View root = LayoutInflater.from(this).inflate(
				R.layout.live_error_dialog, null);
		tv = (TextView) root.findViewById(R.id.live_error_dialog_txt);
		errorDialog.setContentView(root);
	}

	private void exit() {
		if (waitExit) {
			waitExit = false;
			ItvToast toast = new ItvToast(this);
			toast.setText(R.string.toast_exit_hint);
			toast.setIcon(R.drawable.toast_shut);
			toast.show();
			mHandler.postDelayed(cancleExit, 2000);
		} else {
			finish();
		}
	}

	private boolean waitExit = true;
	Runnable cancleExit = new Runnable() {
		@Override
		public void run() {
			waitExit = true;
		}
	};

	public void Back_LiveNum() {
		if (tvback_num > 0) {
			LiveChannelInfo channel = dbHelper.getChannelInfoByNum(tvback_num);
			tvback_num = channels.get(channelIndex).num;
			int tempvid = channels.get(channelIndex).vid;
			updateData(channel.tid[0], channel.vid);
			if (channels.get(channelIndex).vid != tempvid) {
				String url = channel
						.getSourceUrl(channels.get(channelIndex).lastSource);
				mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY_LIVE, url));
			}
		}
	}

	@Override
	public void singleKeyDown(int num) {
		p.width = 300;
		p.height = 100;
		if (num == 0) {
			if (tvback_num > 0) {
				Back_LiveNum();
			} else {
				TipTextShow("", 60f);
			}
		} else if (num > 9999) {
			TipTextShow("", 60f);
		} else {
			TipTextShow(num + "", 60f);
		}
	}

	@Override
	public void multeKeyDown(int num) {
		if (num < 9999) {
			LiveChannelInfo channel = dbHelper.getChannelInfoByNum(num);
			if (channel != null) {
				Message msg = mHandler.obtainMessage();
				msg.what = LivePlayer.MSG_CHANGECHANNEL;
				msg.obj = channel;
				mHandler.sendMessage(msg);
			}
		}
	}

	protected void favCurrentChannel() {
		if (menuContrl == null) {
			menuContrl = new PlayerMenuContrl(this, mHandler,
					ConstantUtil.MENU_LIVE);
		}
		if (channels.get(channelIndex).favorite) {
			dbHelper.updateChannelFav(channels.get(channelIndex).vid, false);
			channels.get(channelIndex).favorite = false;
			menuContrl.setFavText("收藏当前频道");
			ItvToast toast = new ItvToast(this);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setIcon(R.drawable.toast_err);
			toast.setText(channels.get(channelIndex).vname + " -> 已取消收藏！");
			toast.show();
		} else {
			dbHelper.updateChannelFav(channels.get(channelIndex).vid, true);
			channels.get(channelIndex).favorite = true;
			menuContrl.setFavText("取消收藏当前频道");
			ItvToast toast = new ItvToast(this);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setIcon(R.drawable.toast_smile);
			toast.setText(channels.get(channelIndex).vname + " -> 已添加收藏！");
			toast.show();
		}
	}

	private void matchMenuFavText() {
		if (channels.get(channelIndex).favorite) {
			menuContrl.setFavText("取消收藏当前频道");
		} else {
			menuContrl.setFavText("收藏当前频道");
		}
	}
}
