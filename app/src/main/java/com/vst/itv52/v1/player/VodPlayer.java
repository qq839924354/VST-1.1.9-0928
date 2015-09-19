package com.vst.itv52.v1.player;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.BaseActivity;
import com.vst.itv52.v1.activity.BaseActivity.NumKeyClickListener;
import com.vst.itv52.v1.adapters.PlayerChooseArtAdapter;
import com.vst.itv52.v1.adapters.PlayerChooseTvAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.VideoDetailBiz;
import com.vst.itv52.v1.biz.VodBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.custom.LoadingDialog;
import com.vst.itv52.v1.db.VodDataHelper;
import com.vst.itv52.v1.model.SharpnessEnum;
import com.vst.itv52.v1.model.ShooterSRTBean;
import com.vst.itv52.v1.model.VideoDetailInfo;
import com.vst.itv52.v1.model.VideoPlayUrl;
import com.vst.itv52.v1.model.VideoSet;
import com.vst.itv52.v1.model.VideoSource;
import com.vst.itv52.v1.model.VodRecode;
import com.vst.itv52.v1.srt.SRTBean;
import com.vst.itv52.v1.srt.SRTbiz;
import com.vst.itv52.v1.srt.SRTsetPop;
import com.vst.itv52.v1.srt.SearchSrtPop;
import com.vst.itv52.v1.srt.ShooterSRTGetter;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.HttpWorkTask;
import com.vst.itv52.v1.util.HttpWorkTask.ParseCallBack;
import com.vst.itv52.v1.util.HttpWorkTask.PostCallBack;
import com.vst.itv52.v1.util.StringUtil;

public class VodPlayer extends BaseActivity implements OnCompletionListener,
		OnErrorListener, OnPreparedListener, OnInfoListener,
		OnSeekCompleteListener, NumKeyClickListener {
	public static final String TAG = "VodPlayer";
	private VideoView mVideoView;
	private VideoDetailInfo media;
	private VodRecode playRecode;
	private SharpnessEnum sharpness;
	private VodDataHelper dbHelper;
	private TextView scalText;
	private Dialog errorDialog = null;
	private int scaleMod;
	private View viewTop;
	private WindowManager.LayoutParams p;
	private VodBiz vodBiz = null;
	private int hdHeadValue = 3;
	private int jumpStart;
	private int jumpEnd;
	private PlayerMenuContrl menuContrl;
	private Timer timer;
	private ImageView hintPopImg;
	private PopupWindow hintPop;
	private TextView netSpeed, speedDanwei, shishi, srtTv;
	private String baseplayurl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_player_layout);
		baseplayurl = MyApp.baseServer + "v_xml/";
		scaleMod = MyApp.scaleMod;
		sharpness = SharpnessEnum.getSharp(MyApp.sharpness);
		dbHelper = VodDataHelper.getInstance(this);
		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		p = new LayoutParams();
		p.gravity = Gravity.TOP | Gravity.RIGHT;
		p.x = 50;
		p.y = 80;
		p.format = PixelFormat.TRANSPARENT;
		p.width = WindowManager.LayoutParams.WRAP_CONTENT;
		p.height = WindowManager.LayoutParams.WRAP_CONTENT;
		p.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_NOT_TOUCHABLE;
		vodBiz = new VodBiz(this);
		jumpStart = MyApp.getJumpStart();
		jumpEnd = MyApp.getJumpEnd();
		initDialog();
		initView();
		initControl();
		initIntent();
		// 检查跳过片尾
		// TimerStart();
	}

	public VodRecode getPlayRecode() {
		return playRecode;
	}


	/**
	 * 检查跳过片尾
	 */
	public void TimerStart() {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (mVideoView != null && mVideoView.isPlaying()) {
					if (MyApp.getVodJump()) {
						int position = mVideoView.getCurrentPosition();
						int duration = mVideoView.getDuration();
						if (position + jumpEnd >= duration) {
							mVideoView.seekTo(duration);
						}
					}
				}
			}
		}, 10000, 10000);
	}

	Animation topFadeIn, topFadeOut, botFadeIn, botFadeOut;
	AnimationDrawable gif;
	private long rxByte, currentTime;
	private static final int MSG_SPEED = 54454;
	private Runnable speed = new Runnable() {

		@Override
		public void run() {
			if (rxByte != 0 && currentTime != 0) {
				long tempTime = System.currentTimeMillis();
				long tempByte = TrafficStats.getTotalRxBytes();
				// String KbToMb = "KB/S";
				String speed = null;
				String danwei = null;
				if ((tempByte - rxByte) != 0 && (tempTime - currentTime) != 0) {
					long DownloadByte = ((tempByte - rxByte)
							/ (tempTime - currentTime) * 1000 / 1024);
					if (DownloadByte < 1000) {
						speed = DownloadByte + "";
						danwei = " KB/S";

					} else {
						double DownloadByte2 = (double) DownloadByte / 1024D;
						speed = new DecimalFormat("#.##").format(DownloadByte2);
						danwei = " MB/S";
					}
					shishi.setVisibility(View.VISIBLE);
					netSpeed.setText(speed);
					speedDanwei.setText(danwei);
				}
				rxByte = tempByte;
				currentTime = tempTime;
			}
			mHandler.postDelayed(speed, 200);
		}
	};

	/**
	 * 开始获取及时网络
	 */
	private void startTestSpeeed() {
		rxByte = TrafficStats.getTotalRxBytes();
		currentTime = System.currentTimeMillis();
		mHandler.postDelayed(speed, 1000);
	}

	private void initView() {
		mVideoView = (VideoView) findViewById(R.id.vod_player_videoView);
		mVideoView.setOnPreparedListener(this);
		mVideoView.setOnCompletionListener(this);
		
		transformationWebView =(TransformationWebView) findViewById(R.id.transformation_webview);
		// mVideoView.setOnInfoListener(this);
		mVideoView.setOnErrorListener(this);
		netSpeed = (TextView) findViewById(R.id.vod_player_real_speed);
		speedDanwei = (TextView) findViewById(R.id.vod_player_speed_danwei);
		shishi = (TextView) findViewById(R.id.vod_player_speed_wenzi);
		srtTv = (TextView) findViewById(R.id.vod_player_srt_tv);
		// setSRTTextColor(MyApp.getSRTTextColor());
		// setSRTTextSize(MyApp.getSRTTextSize());
		// setSRTTextLoaction(MyApp.getSSRTLocation());
		updateSRTShow();
		viewTop = findViewById(R.id.vod_player_top_view);
		viewTop.setVisibility(View.VISIBLE);

		scalText = new TextView(this);
		scalText.setTextColor(0xffffffff);
		scalText.setTextSize(30.0f);
		scalText.setGravity(Gravity.CENTER);
		initOperatHintPop();
	}

	private void initOperatHintPop() {
		hintPop = new PopupWindow();
		hintPopImg = new ImageView(this);
		hintPop.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		hintPop.setBackgroundDrawable(new BitmapDrawable());
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		hintPopImg.setLayoutParams(lp);
		// hintPop.setBackground(Color.)
		hintPop.setContentView(hintPopImg);
	}

	private void initData() {
		if (media != null) {
			if (playRecode == null) {
				playRecode = new VodRecode();
				playRecode.id = media.id;
				playRecode.title = media.title;
				playRecode.banben = media.banben;
				playRecode.imgUrl = media.img;
				playRecode.type = VodDataHelper.RECODE;
				playRecode.sourceIndex = 0;
				playRecode.setIndex = 0;
				playRecode.positon = 0;
			}

			VideoSource source = media.playlist.get(playRecode.sourceIndex);
			ctrtop.setSourceTag(StringUtil.getSourceTag(source.sourceName));
			ctrtop.setScaleTag(scaleMod);
			ctrBotSetVideoName(source);
			ctrtop.setHdSdTag(0);
		}
	}

	private void ctrBotSetVideoName(VideoSource source) {
		if (media.type.contains("电影")) {
			ctrtop.setVideoName(media.title);
		} else {
			// 阻止下标越界
			if (playRecode.setIndex < source.sets.size()) {
				ctrtop.setVideoName(media.title + "（"
						+ source.sets.get(playRecode.setIndex).setName + "）");
			}
		}
	}

	public int getCurrentPosition() {
		return mVideoView.getCurrentPosition();
	}

	private void initIntent() {
		Intent intent = getIntent();
		Bundle data = intent.getBundleExtra(ConstantUtil.VODEXTRA);
		playRecode = (VodRecode) data.get("playinfo");
		media = (VideoDetailInfo) data.get("media");

		if (media == null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						media = VideoDetailBiz.parseDetailInfo(
								"",
								playRecode.id,
								getPackageManager().getPackageInfo(
										VodPlayer.this.getPackageName(), 0).versionCode);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					if (media != null) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								initData();
								playVideo();
							}
						});
					} else {
						mHandler.sendEmptyMessage(MSG_ERROR);
					}
				}
			}).start();
		} else {
			initData();
			playVideo();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart>>>>");
	}

	public void onResume() {
		mVideoView.start();
		super.onResume();
		Log.i(TAG, "onResume");
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		Log.i(TAG, "onPause");
		if (mVideoView.isPlaying()) {
			mVideoView.pause();
			playRecode.positon = mVideoView.getCurrentPosition();
			// System.out.println("playInfo.positon" + playRecode.positon);
			dbHelper.insertRecode(playRecode);
		}
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy");
		if (frushTimeTask != null) {
			frushTimeTask.cancel();
		}
		isFrush = false;

		// mHandler.removeMessages(MSG_SPEED);
		shishi.setVisibility(View.INVISIBLE);
		// 关闭字幕
		MyApp.setCanShowSRT(false);
		if (scalText.getParent() != null)
			wm.removeView(scalText);
		if (errorDialog.isShowing()) {
			errorDialog.dismiss();
		}
		searchCount = 0;
		/**
		 * 将所有的popwindow全部隐藏
		 */
		if (hintPop != null && hintPop.isShowing()) {
			hintPop.dismiss();
		}
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (ctrtop != null && ctrtop.isShowing()) {
			ctrtop.dismiss();
		}
		if (ctrbot != null && ctrbot.isShowing()) {
			ctrbot.dismiss();
		}
		if (searchSrtPop != null && searchSrtPop.isShowing()) {
			searchSrtPop.dismiss();
		}
		if (srtPop != null && srtPop.isShowing()) {
			srtPop.dismiss();
		}
		release();
		super.onDestroy();
	}

	/**
	 * 释放资源
	 */
	public void release() {
		mHandler.removeMessages(MSG_DISMISS_SRT);
		mHandler.removeMessages(MSG_HIDE_SCAL);
		mHandler.removeMessages(MSG_ERROR);
		mHandler.removeMessages(MSG_PLAY);
		mHandler.removeMessages(SRT);
		mHandler.removeMessages(MSG_SELECTSET);
		mHandler.removeMessages(MSG_PLAY_NEXT);
		// mHandler.removeCallbacks(frushSrt);
		mHandler.removeCallbacks(parseNetSrt);
		mHandler.removeCallbacks(runnable);
		mHandler.removeCallbacks(speed);
		frushSRTTimer = null;
		frushTimeTask = null;
		currentStr = null;
		srtNetPath = null;
		srtMap = null;// 字幕
	}

	private VodCtrBot ctrbot;
	private VodCtrTop ctrtop;

	public void initControl() {
		ctrbot = new VodCtrBot(this, mVideoView, mHandler, true);
		ctrtop = new VodCtrTop(this, mHandler);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
				// && keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isKeyCodeSupported) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
					|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER
					|| keyCode == KeyEvent.KEYCODE_ENTER) {
				// ctrbot.show();
				if (mVideoView.isPlaying()) {
					mVideoView.pause();
					hintPopImg.setImageResource(R.drawable.osd_pause);
					hintPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
				} else {
					mVideoView.start();
					try {
						hintPop.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				// ctrbot.doPauseResume();
				// ctrbot.playOrPauseRequesFocus();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!mVideoView.isPlaying()) {
					mVideoView.start();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {

				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// if (media.playlist.get(playRecode.sourceIndex).sets.size() <
				// 2) {
				// }
				ctrtop.showAtLocation(mVideoView, Gravity.TOP, 0, 0);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (media.playlist.get(playRecode.sourceIndex).sets.size() > playRecode.setIndex + 1) { // 有下一集
					toNextSet();
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				showHintPop(ConstantUtil.OPERATE_LEFT);
				ctrbot.show();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				showHintPop(ConstantUtil.OPERATE_RIGHT);
				ctrbot.show();
				return true;
			} else if (keyCode == 185) { // 185 是比例 A11
				changScale(ConstantUtil.OPERATE_RIGHT);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				if (menuContrl == null) {
					menuContrl = new PlayerMenuContrl(this, mHandler,
							ConstantUtil.MENU_VOD);
				}
				if (menuContrl.isShowing()) {
					menuContrl.dismiss();
				} else {
					menuContrl.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
					menuContrl.setScalor(scaleMod);
					menuContrl.showVoiceLevel(menuContrl.getVoice());
					menuContrl.setVideoSources(media.playlist,
							playRecode.sourceIndex);
					ArrayList<VideoPlayUrl> playUrls = media.playlist.get(playRecode.sourceIndex).sets.get(playRecode.setIndex).playUrls;
					//重新匹配一次清晰度
					
					
					
					
					
					
					
					
	/////////////////////////////////////////////////0629				
					
//					SuitSharp(playUrls);
//					menuContrl.setSharpness(playUrls, sharpness);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void showHintPop(int derection) {
		mHandler.removeCallbacks(dismissHintPop);
		if (derection == ConstantUtil.OPERATE_LEFT) {
			hintPopImg.setImageResource(R.drawable.osd_backward);
		} else if (derection == ConstantUtil.OPERATE_RIGHT) {
			hintPopImg.setImageResource(R.drawable.osd_forward);
		}
		hintPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
		mHandler.postDelayed(dismissHintPop, 2000);
	}

	public String changScale(int derection) {
		String text = null;
		if (mVideoView.isPlaying()) {
			if (derection == ConstantUtil.OPERATE_RIGHT) {
				scaleMod += 1;
			} else if (derection == ConstantUtil.OPERATE_LEFT) {
				if (scaleMod > 0) {
					scaleMod--;
				} else {
					scaleMod = 3;
				}
			}
			switch (scaleMod % 3) {
			case VideoView.A_DEFALT:
				text = "原 始比例";
				break;
			case VideoView.A_4X3:
				text = " 4 : 3 ";
				break;
			case VideoView.A_16X9:
				text = " 16 : 9 ";
				break;
			}
			scalText.setText(text);
			mVideoView.selectScales(scaleMod % 3);
			if (isRunning()) {
				if (scalText.getParent() == null) {
					wm.addView(scalText, p);
				} else {
					p.height = scalText.getHeight() + 1;
					wm.updateViewLayout(scalText, p);
				}
				mHandler.removeMessages(MSG_HIDE_SCAL);
				mHandler.sendEmptyMessageDelayed(MSG_HIDE_SCAL, 3000);
			}
		}
		return text;
	}

	/**
	 * 播放完成 自动播放下一集 或者 做出其他提示
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		MyApp.setApkRunTime(MyApp.enjoyStart - System.currentTimeMillis());
		nextSet();
	}

	private void toNextSet() {
		if (waitNext) {
			waitNext = false;
			ItvToast toast = new ItvToast(this);
			toast.setText(R.string.toast_nextset_hint);
			toast.setIcon(R.drawable.toast_shut);
			toast.show();
			mHandler.postDelayed(cancleNext, 2000);
		} else {
			nextSet();
		}
	}

	private boolean waitNext = true;
	Runnable cancleNext = new Runnable() {
		@Override
		public void run() {
			waitNext = true;
		}
	};

	private void nextSet() {
		if (media.playlist.get(playRecode.sourceIndex).sets.size() > playRecode.setIndex + 1) { // 有下一集
			// 刷新遮罩上面的集数显示
			playRecode.setIndex = playRecode.setIndex + 1;
			mHandler.sendMessage(mHandler.obtainMessage(MSG_SELECTSET,
					playRecode.sourceIndex, playRecode.setIndex));
			playRecode.positon = 0;
			// txtFilmSet.setText(media.playlist.get(playRecode.sourceIndex).sets
			// .get(playRecode.setIndex).setName);
		} else {
			/**
			 * 播放完成 没有下一集 并给用户 相应提示
			 */
			finish();
		}
		/* 做播放完成的 清理 */
		mVideoView.stopPlayback();
		playRecode.positon = 0;
		dbHelper.insertRecode(playRecode);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// 发生错误的话，需要中断播放判断线程
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		/**
		 * 源失效或其他错误 给用户相应提示
		 */
		Log.i(TAG, "onError : what = " + what + " , extra = " + extra);
		if (mp != null) {
			playRecode.positon = mp.getCurrentPosition();
		}
		if (playRecode != null) {
			dbHelper.insertRecode(playRecode);
			mHandler.sendEmptyMessage(MSG_ERROR);
		}
		return true;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {

		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mp != null && playRecode != null) {
			Log.i(TAG, "onPrepared>>> sourceindex   =  "
					+ playRecode.sourceIndex + " ,   setIndex = "
					+ playRecode.setIndex);
			// playRecode.positon = 0;
			mHandler.sendEmptyMessage(MSG_HIDEBUFF);
			mHandler.removeMessages(MSG_SHOWBUFF);
			mVideoView.selectScales(MyApp.scaleMod % 3); // height 有可能为空
			int height = mp.getVideoHeight();
			System.out.println("video height:" + height);
			ctrtop.setHdSdTag(height);
			/* 根据设置跳过片头，电影不跳，记录进度大于片头不跳 */
			System.out.println("跳片头：" + jumpStart);
			if (MyApp.getVodJump() && jumpStart > playRecode.positon) {
				mVideoView.seekTo(jumpStart);
			} else {
				mVideoView.seekTo(playRecode.positon);
			}
			TimerStart();
		}
	}

	private int seekPositon = 0;

	/**
	 * 通过服务器 解析出 视频的真正地址 网络操作 在工作 线程中处理
	 * 
	 * @param url
	 * @return String 视频实际的地址
	 */
	// private ExecutorService pool = Executors.newFixedThreadPool(1);
	private ArrayList<VideoPlayUrl> urls;
	private void playVideo() {
		 if (urls != null) {
			urls.clear();
			urls = null;
		}
		if (playRecode.sourceIndex < media.playlist.size()) {
			ctrBotSetVideoName(media.playlist.get(playRecode.sourceIndex));
			hdHeadValue = 3;
			urls = media.playlist.get(playRecode.sourceIndex).sets
					.get(playRecode.setIndex).playUrls;
		} else {
			playRecode.sourceIndex = 0;
			ctrBotSetVideoName(media.playlist.get(playRecode.sourceIndex));
			hdHeadValue = 3;
			urls = media.playlist.get(playRecode.sourceIndex).sets
					.get(playRecode.setIndex).playUrls;
		}
		if (urls != null && urls.size() > 0) { // 已经从网上解析过了
//			if (menuContrl !=null) {
//				menuContrl.setUrls(urls);
//			}
			// 设置 控制的清晰度
			SuitSharp(urls);
			matchSetandPlay(urls);
		} else {
		
			// 没有解析过的
			// urlIndex = 0 ;
			MyApp.pool.execute(runnable);
		}
	}

	private Runnable dismissHintPop = new Runnable() {
		@Override
		public void run() {
			if (hintPop != null) {
				hintPop.dismiss();
			}
		}
	};

	/**
	 * 匹配 合适的 清晰度
	 * 
	 * @param urls
	 */
	private void SuitSharp(ArrayList<VideoPlayUrl> urls) {
		
		
		
		
		
/////////////////////////////////////////0629		
		
		
//		List<SharpnessEnum> sharpList = new ArrayList<SharpnessEnum>();
//		for (int i = 0; i < urls.size(); i++) {
//			Log.i("info", "qxds="+urls.get(i).toString());
//			sharpList.add(urls.get(i).sharp);
//		}
//		Log.d("info", "sharpness="+sharpness);
//		sharpness = SharpnessEnum.getSuitSharp(sharpness, sharpList);
		
		
		
		
		
		
		
//		if (menuContrl!=null && urls!=null && sharpness!=null) {
//			//重新设置一次清晰度
//			menuContrl.setSharpness(urls, sharpness);
//		}
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
//			hdHeadValue--;
//			// 这里内存溢出 先置于空 在赋值
//			urls = null;
//			urls = vodBiz.getPlayUris(baseplayurl,
//					media.playlist.get(playRecode.sourceIndex).sets
//							.get(playRecode.setIndex).link, new BasicHeader(
//							"Player-HD", hdHeadValue + ""));
//			if (urls == null || urls.size() == 0) {
//				mHandler.sendEmptyMessage(MSG_ERROR);
//				return;
//			}
//			SuitSharp(urls);
//			matchSetandPlay(urls);
//			media.playlist.get(playRecode.sourceIndex).sets
//					.get(playRecode.setIndex).playUrls = urls;
			  
			
			
			
			   transformationWebView.setOnLoadUrlListener(loadUrlListener);
			
			 transformationWebView
             .loadUrl(media.playlist.get(playRecode.sourceIndex).sets
						.get(playRecode.setIndex).link);
			
         // .loadUrl("http://v.youku.com/v_show/id_XMTI1ODc5MjU2NA==.html");
			
			
		}
	};
	
	
    TransformationWebView transformationWebView;
    
    TransformationWebView.OnLoadUrlListener loadUrlListener =
            new TransformationWebView.OnLoadUrlListener() {
                @Override
                public void onFinish(final String url) {
                	urls =null;
                	ArrayList<VideoPlayUrl> urls = new ArrayList<VideoPlayUrl>();
                	
                	VideoPlayUrl u =  new VideoPlayUrl();
                	u.playurl = url;
                	urls.add(u);
                	matchSetandPlay(urls);
        			media.playlist.get(playRecode.sourceIndex).sets
					.get(playRecode.setIndex).playUrls = urls;
                }
            };

    
	
	

	/**
	 * 匹配设置中的清晰度并播放
	 */
	private void matchSetandPlay(ArrayList<VideoPlayUrl> urls) {
		
		///////////////////////////0629
		
		
//		String playUrl = null;
//		// 取与清晰度设置相同的播放链接
//		for (int i = 0; i < urls.size(); i++) {
//			if (urls.get(i).sharp.equals(sharpness)) {
//				playUrl = urls.get(i).playurl;
//				break;
//			}
//		}
//		// 如果取不到就播放第一个
//		if (playUrl == null) {
//			VideoPlayUrl videoPlayUrl = urls.get(urls.size() - 1);
//			System.out.println(videoPlayUrl.toString());
//			playUrl = videoPlayUrl.playurl;
//			sharpness = videoPlayUrl.sharp;
//		}
	mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY,urls.get(0).playurl));
	}

	public final static int MSG_SHOWBUFF = 5;
	public final static int MSG_HIDEBUFF = 6;
	public final static int MSG_SELECTSET = 7;
	public final static int MSG_SELECTSOURCE = 10;
	public final static int MSG_PLAY = 12;
	public final static int MSG_ERROR = 13;
	public final static int MSG_HIDE_SCAL = 14;
	public final static int MSG_RETRY = 15;
	private static final int MSG_DISMISS_CHOOSE = 16;
	public final static int MSG_PLAY_NEXT = 17;
	public static final int SRT_SELECTED = 233;
	public static final int SRT_LIST = 244;
	public static final int SRT = 255;
	public static final int SRT_ALLOW = 256;
	private int srtIndex = 0, netOrxlFlag = 2;
	private String srtNetPath;
	private Map<Integer, SRTBean> srtMap;// 字幕
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_RETRY:
				break;
			case MSG_HIDE_SCAL:
				if (scalText.getParent() != null)
					wm.removeView(scalText);
				break;
			case MSG_ERROR:
				if (hdHeadValue >= 1) {
					MyApp.pool.execute(runnable);
					return;
				}
				if (isRunning()) {
					// 退出对话框为空，或者没有显示才显示错误对话框
					// if (exitDialog == null || !exitDialog.isShowing()) {
					errorDialog.show();
					// }
					mVideoView.stopPlayback();
				}
				break;
			// case MSG_SPEED://显示下载速度
			// break;

			case MSG_SHOWBUFF:
				viewTop.setVisibility(View.VISIBLE);
				try {
					progressShow();
				} catch (Exception e) {
					e.printStackTrace();
				}
				startTestSpeeed();
				break;
			case MSG_HIDEBUFF:
				viewTop.setVisibility(View.GONE);
				mHandler.removeCallbacks(speed);
				progressDismiss();
				break;
			case MSG_SELECTSET:
				playRecode.sourceIndex = msg.arg1;
				playRecode.setIndex = msg.arg2;
				playVideo();
				break;
			case MSG_SELECTSOURCE:
				playRecode.sourceIndex = msg.arg1;
				playRecode.positon = mVideoView.getCurrentPosition();
				Log.i(TAG, "MSG_SELECTSOURCE =" + playRecode.sourceIndex);
				VideoSource source = media.playlist.get(playRecode.sourceIndex);
				//设置视屏源
				menuContrl.setVideoSources(playRecode.sourceIndex);
				ctrtop.setSourceTag(StringUtil.getSourceTag(source.sourceName));

				ArrayList<VideoSet> sets = media.playlist
						.get(playRecode.sourceIndex).sets;
//				menuContrl.setUrls(sets.get(playRecode.setIndex).playUrls);
				Log.i("info", "还原之后的urls="+sets.get(playRecode.setIndex).playUrls);
				if (playRecode.setIndex > sets.size() - 1) { // 2个源 集数 不一样
					ItvToast toast = new ItvToast(VodPlayer.this);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setText("当前源未更新到 " + playRecode.setIndex
							+ "集，自动选择当前源最后一集！");
					toast.show();
					playRecode.setIndex = sets.size() - 1;
				}
//				sharpness=SharpnessEnum.getSharp(MyApp.sharpness);
				playVideo();
				showChooseSetDialog(false);
				break;
			case MSG_PLAY:
				playUrl= (String) msg.obj;
//				if (urls !=null && !urls.isEmpty() && urls.size()>0) {
//					sharpness = urls.get(msg.arg2).sharp;
//					MyApp.setSharpness(msg.arg2);
//				}
				sharpness=SharpnessEnum.getSharp(MyApp.sharpness);
				Log.d("info", "更改后的sharpness="+sharpness);
				System.out.println("paly url = " +  msg.obj);
				if (playUrl == null) {
					return;
				}
				if (msg.arg1 > 0) {
					playRecode.positon = msg.arg1;
				}
				
				mVideoView.setVideoURI(Uri.parse(playUrl));
				mVideoView.start();
				scaleMod=2;
				mHandler.sendEmptyMessage(MSG_SHOWBUFF);
				break;
			case MSG_DISMISS_CHOOSE:
				if (chooseDialog != null && chooseDialog.isShowing()) {
					chooseDialog.dismiss();
				}
				break;
			case MSG_PLAY_NEXT:
				nextSet();
				break;
			case SRT_SELECTED:
				srtIndex = msg.arg1;
				netOrxlFlag = msg.arg2;
				if (netOrxlFlag == 2) {
					srtNetPath = (String) msg.obj;
					Log.d("info", "msg.obj=" + srtNetPath);
					// 从本地加载字幕
					MyApp.pool.execute(parseNetSrt);
				}
				break;
			case SRT:// 显示字幕
				// 允许显示悬挂字幕，且字幕不为空
				if (MyApp.getCanShowSRT() && srtMap != null) {
					// 刷新字幕
					isFrush = true;
					if (frushTimeTask != null) {
						frushTimeTask.cancel(); // 将原任务从队列中移除
					}
					mHandler.removeMessages(MSG_DISMISS_SRT);
					frushTimeTask = new MyTimerTask();
					frushSRTTimer.schedule(frushTimeTask, 200, 500);
				} else {
					srtTv.setText("");

				}
				break;
			case SRT_ALLOW:
				// allowShowSRT();
				break;
			case MSG_FRUSH_SRT:
				try {
					// Log.i("info", "当前字幕="+msg.obj.toString());
					String str = (String) msg.obj;
					if (msg.obj != null) {
						if (!currentStr.equals(str)) {
							srtTv.setText(Html.fromHtml(str));
							currentStr = str;
							mHandler.removeMessages(MSG_DISMISS_SRT);
							mHandler.sendEmptyMessageDelayed(MSG_DISMISS_SRT,
									2000);
						}
					} else {
						srtTv.setText("");
					}
				} catch (Exception e) {
					srtTv.setText("");
					e.printStackTrace();
				}
				break;
			case MSG_DISMISS_SRT:
				srtTv.setText("");
				break;
			default:
				break;
			}
		}
	};
	private static final int MSG_DISMISS_SRT = 787878;
	private int timerError; // 字幕错位时间调整值 单位是s;

	public void setTimerError(int timerError) {
		this.timerError = timerError;
	}

	/**
	 * 解析射手网字幕的线程
	 */
	Runnable parseNetSrt = new Runnable() {
		@Override
		public void run() {
			// 判断是否开启悬挂字幕
			if (MyApp.getCanShowSRT()) {
				Log.d("info", "srtNetPath=" + srtNetPath);
				if (srtNetPath != null) {
					// 压缩文件的包
					srtMap = SRTbiz
							.parseSrt(new File(srtNetPath.replace("\\", "/")),
									timerError);
					if (srtMap != null) {
						Log.d("info", srtMap.toString());
						Log.d("info", "错位调整的时间=" + timerError);
						// 更新字幕
						mHandler.sendEmptyMessage(SRT);
					}
				}
			}
		}
	};
	// //定时刷新任务
	private Timer frushSRTTimer = new Timer();
	public static boolean isFrush;
	private static final int MSG_FRUSH_SRT = 12313;
	private MyTimerTask frushTimeTask;
	private String currentStr = "";

	class MyTimerTask extends TimerTask {
		//
		@Override
		public void run() {
			if (isFrush) {
				try {
					int currentPosition = mVideoView.getCurrentPosition();
					Iterator<Integer> keys = srtMap.keySet().iterator();
					// 通过while循环遍历比较
					while (keys.hasNext()) {
						Integer key = keys.next();
						SRTBean srtbean = srtMap.get(key);
						// // 判断当前播放时间是否在字幕的开始时间和结束时间之内
						Message msg = Message.obtain();
						msg.what = MSG_FRUSH_SRT;
						if (currentPosition > srtbean.getBeginTime()
								&& currentPosition < srtbean.getEndTime()) {
							if (!srtbean.getSrtBody().equals(currentStr)) {
								msg.obj = srtbean.getSrtBody();
								mHandler.sendMessage(msg);
							}
							break;
							// }
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private String playUrl;

	private void initDialog() {
		errorDialog = new Dialog(this, R.style.MyDialog);
		View view = LayoutInflater.from(this).inflate(
				R.layout.vod_error_dialog, null);
		errorDialog.setContentView(view);
		Button btn = (Button) view.findViewById(R.id.vod_error_dialog_btn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				errorDialog.dismiss();
				// url 获取为空的时候 有bug
				if (playUrl != null) {
					mHandler.sendMessage(mHandler.obtainMessage(MSG_PLAY,
							playUrl));
				} else { // 继续 获取 url
					MyApp.pool.execute(runnable);
				}
			}
		});

		btn.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					VodPlayer.this.finish();
					return true;
				}
				return false;
			}
		});
		errorDialog.setContentView(view);
	}

	Dialog chooseDialog = null;

	public void showChooseSetDialog(boolean show) {
		if (!show) {
			chooseDialog = new ChooseSetDiaLog(this,
					media.playlist.get(playRecode.sourceIndex).sets,
					media.type.contains("综艺") || media.type.contains("电影"),
					playRecode.setIndex);
		} else {
			if (chooseDialog == null) {
				chooseDialog = new ChooseSetDiaLog(this,
						media.playlist.get(playRecode.sourceIndex).sets,
						media.type.contains("综艺") || media.type.contains("电影"),
						playRecode.setIndex);
			}
			chooseDialog.show();
			mHandler.sendEmptyMessageDelayed(MSG_DISMISS_CHOOSE, 10000);
		}
	}

	public int responseSetSize() {
		return media.playlist.get(playRecode.sourceIndex).sets.size();
	}

	public String responseType() {
		return media.type;
	}

	@Override
	public void onBackPressed() {
		exit();
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
			hintPop.dismiss();
			// Intent intent=new Intent();
			setResult(ConstantUtil.ACTIVITY_RESULT_OK);
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

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		playRecode.positon = 0;
		seekPositon = 0;
	}

	@Override
	public void singleKeyDown(int num) {
		// p.width = 300;
		// p.height = 100;
		// if (num == 0) {
		// if (tvback_num > 0) {
		// Back_LiveNum();
		// } else {
		// TipTextShow("", 60f);
		// }
		// } else if (num > 9999) {
		// TipTextShow("", 60f);
		// } else {
		// TipTextShow(num + "", 60f);
		// }
	}

	@Override
	public void multeKeyDown(int num) {
		// if (num < 9999) {
		// LiveChannelInfo channel = dbHelper.getChannelInfoByNum(num);
		// if (channel != null) {
		// Message msg = mHandler.obtainMessage();
		// msg.what = LivePlayer.MSG_CHANGECHANNEL;
		// msg.obj = channel;
		// mHandler.sendMessage(msg);
		// }
		// }
	}

	/**
	 * 显示搜索字幕的popwindow
	 */
	private SearchSrtPop searchSrtPop;
	private static int searchCount = 0;

	public void showSearchSrtPop() {
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (searchCount < 1) {
			getSrtList();
		} else {
			if (searchSrtPop != null) {
				searchSrtPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
			} else {
				ItvToast toast = ItvToast.makeText(this, "没有该影片的字幕...", 3000);
				toast.show();
			}
		}
	}

	/**
	 * 获取字幕列表
	 */
	public void getSrtList() {
		searchCount = 1;
		try {
			final LoadingDialog progressDialog1 = new LoadingDialog(this);
			progressDialog1.setLoadingMsg("加载中...");
			progressDialog1.show();
			new HttpWorkTask<ArrayList<ShooterSRTBean>>(
					new ParseCallBack<ArrayList<ShooterSRTBean>>() {

						@Override
						public ArrayList<ShooterSRTBean> onParse() {

							try {
								return ShooterSRTGetter.getShooterSrts(media.title);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					}, new PostCallBack<ArrayList<ShooterSRTBean>>() {

						@Override
						public void onPost(ArrayList<ShooterSRTBean> reult) {
							progressDialog1.dismiss();
							if (reult != null && reult.size() > 0) {
								searchSrtPop = new SearchSrtPop(VodPlayer.this,
										reult, mHandler, media.title);
								// 显示在屏幕中间位置
								searchSrtPop.showAtLocation(mVideoView,
										Gravity.CENTER, 0, 0);
							} else {
								ItvToast toast = ItvToast.makeText(
										VodPlayer.this, "暂无该视频的字幕", 3000);
								toast.show();
							}
						}
					}).execute();
		} catch (Exception e) {
			ItvToast toast = ItvToast.makeText(this, "网络加载出错，请重试！", 3000);
			toast.show();
			e.printStackTrace();
		}

	}

	private SRTsetPop srtPop;

	/**
	 * 下载的字幕文件
	 */
	private ArrayList<String> files;

	public ArrayList<String> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}

	public void showSRTPop(ArrayList<String> fileNames) {
		// 先置于空
		srtPop = null;
		srtPop = new SRTsetPop(this, mHandler);
		// 设置字幕列表
		srtPop.setNetSrts(fileNames);
		// 显示在屏幕中间位置
		srtPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
	}

	/**
	 * 设置自定义悬挂字幕大小
	 * 
	 * @param size
	 */
	public void setSRTTextSize(int size) {
		if (srtTv != null)
			srtTv.setTextSize(size);
	}

	/**
	 * 设置悬挂字幕颜色
	 * 
	 * @param colors
	 */
	public void setSRTTextColor(int[] colors) {
		if (srtTv != null) {
			srtTv.setTextColor(colors[0]);
			srtTv.setShadowLayer(3, 2, -2, colors[1]);
		}
	}

	/**
	 * 设置悬挂字幕位置
	 * 
	 * @param level
	 */
	public void setSRTTextLoaction(int level) {
		if (srtTv != null) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
			int buttom = 40 + 20 * level;
			lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			lp.setMargins(0, 0, 0, buttom);
			srtTv.setLayoutParams(lp);
		}
	}

	/**
	 * 更新悬挂字幕属性
	 */
	private void updateSRTShow() {
		int size = MyApp.getSRTTextSize();
		int[] color = MyApp.getSRTTextColor();
		int location = MyApp.getSSRTLocation();
		setSRTTextSize(size);
		setSRTTextColor(color);
		setSRTTextLoaction(location);
	}

	private class ChooseSetDiaLog extends Dialog implements
			OnFocusChangeListener {
		private Context context;
		private int pageContain;// 分组单位，电视剧30，综艺20
		private ArrayList<List<VideoSet>> pagesSets;
		private ArrayList<String> groupTitles;// 组标签上显示的内容
		private RadioGroup groupRg;
		private GridView tvSetGrid;
		private ListView artSetList;
		private boolean isArt = false;
		private int currentSet;
		private PlayerChooseTvAdapter tvAdapter;
		private PlayerChooseArtAdapter artAdapter;
		private int currentGroupIndex;

		public ChooseSetDiaLog(Context context, List<VideoSet> sets,
				boolean isArt, int currentSet) {
			super(context, R.style.MyDialog);
			this.context = context;
			this.isArt = isArt;
			this.currentSet = currentSet;
			LinearLayout view = (LinearLayout) LayoutInflater.from(context)
					.inflate(R.layout.player_choose_set, null);
			view.setMinimumWidth(620);
			view.setMinimumHeight(420);
			setContentView(view);
			groupRg = (RadioGroup) view.findViewById(R.id.player_choose_table);
			if (isArt) {
				ViewStub stub = (ViewStub) view
						.findViewById(R.id.player_choose_art);
				stub.inflate();
				artSetList = (ListView) view
						.findViewById(R.id.player_choose_art);
				pageContain = 20;
			} else {
				ViewStub stub = (ViewStub) view
						.findViewById(R.id.player_choose_tv);
				stub.inflate();
				tvSetGrid = (GridView) view.findViewById(R.id.player_choose_tv);
				pageContain = 30;
			}
			tvSetGrid = (GridView) view
					.findViewById(R.id.player_choose_tv_gridView);
			artSetList = (ListView) view
					.findViewById(R.id.player_choose_art_list);
			spliteSets(sets);
			createGroupTitle();
			initSetChooseAndListener();
		}

		private void createGroupTitle() {
			if (groupTitles == null || groupTitles.isEmpty()) {
				return;
			}
			int size = groupTitles.size();
			for (int i = 0; i < size; i++) {
				RadioButton rb = (RadioButton) LayoutInflater.from(context)
						.inflate(R.layout.vediodetail_rb, null);
				rb.setBackgroundResource(R.drawable.video_details_btn10_selector);
				rb.setFocusable(true);
				rb.setGravity(Gravity.CENTER);
				rb.setRight(10);
				// rb.setButtonDrawable(android.R.color.transparent);
				rb.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						android.R.color.transparent, 0);
				rb.setText(groupTitles.get(i));
				rb.setTextSize(24);
				groupRg.addView(rb);
			}
			currentGroupIndex = currentSet / pageContain;
			groupRg.check(groupRg.getChildAt(currentGroupIndex).getId());
			if (isArt) {
				artSetList.setSelection(currentSet % pageContain);
			} else {
				tvSetGrid.setSelection(currentSet % pageContain);
			}
		}

		private void initSetChooseAndListener() {
			if (tvSetGrid != null) {
				tvSetGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
				tvAdapter = new PlayerChooseTvAdapter(context,
						pagesSets.get(currentGroupIndex), currentGroupIndex);
				tvSetGrid.setAdapter(tvAdapter);

				tvSetGrid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 刷新遮罩上面的集数显示
						playRecode.setIndex = currentGroupIndex * pageContain
								+ position;
						mHandler.sendMessage(mHandler.obtainMessage(
								MSG_SELECTSET, playRecode.sourceIndex,
								playRecode.setIndex));
						playRecode.positon = 0;
						// txtFilmSet.setText(media.playlist
						// .get(playRecode.sourceIndex).sets
						// .get(playRecode.setIndex).setName);
						dismiss();
					}
				});

				tvSetGrid
						.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {
								mHandler.removeMessages(MSG_DISMISS_CHOOSE);
								mHandler.sendEmptyMessageDelayed(
										MSG_DISMISS_CHOOSE, 30000);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});
				tvSetGrid.setOnFocusChangeListener(this);
			} else {
				artAdapter = new PlayerChooseArtAdapter(context,
						pagesSets.get(currentGroupIndex), currentGroupIndex);
				artSetList.setAdapter(artAdapter);

				artSetList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 刷新遮罩上面的集数显示
						playRecode.setIndex = currentGroupIndex * pageContain
								+ position;
						mHandler.sendMessage(mHandler.obtainMessage(
								MSG_SELECTSET, playRecode.sourceIndex,
								playRecode.setIndex));
						playRecode.positon = 0;
						// txtFilmSet.setText(media.playlist
						// .get(playRecode.sourceIndex).sets
						// .get(playRecode.setIndex).setName);
						dismiss();
					}
				});

				artSetList
						.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> parent,
									View view, int position, long id) {
								mHandler.removeMessages(MSG_DISMISS_CHOOSE);
								mHandler.sendEmptyMessageDelayed(
										MSG_DISMISS_CHOOSE, 30000);
							}

							@Override
							public void onNothingSelected(AdapterView<?> parent) {
							}
						});
				artSetList.setOnFocusChangeListener(this);
			}
			groupRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					currentGroupIndex = groupRg.indexOfChild(groupRg
							.findViewById(checkedId));
					if (tvSetGrid != null) {
						tvAdapter.setDataChanged(
								pagesSets.get(currentGroupIndex),
								currentGroupIndex);
					} else {
						artAdapter.setDataChanged(pagesSets
								.get(currentGroupIndex));
					}
				}
			});
			groupRg.setOnFocusChangeListener(this);
		}

		private void spliteSets(List<VideoSet> allSets) {
			int tempItems = allSets.size() % pageContain;
			int tempPages = allSets.size() / pageContain;
			int pages = tempItems == 0 ? tempPages : tempPages + 1;// 总组数
			int lastItems = tempItems;// 最后一组的集数
			pagesSets = new ArrayList<List<VideoSet>>();
			groupTitles = new ArrayList<String>();
			for (int page = 0; page < pages; page++) {
				if (page < pages - 1 || lastItems == 0) {
					List<VideoSet> sets = allSets.subList(page * pageContain,
							(page + 1) * pageContain);
					groupTitles.add((page * pageContain + 1) + " - "
							+ (page + 1) * pageContain);
					pagesSets.add(sets);
				} else {
					List<VideoSet> sets = allSets.subList(page * pageContain,
							page * pageContain + lastItems);
					groupTitles.add((page * pageContain + 1) + " - "
							+ (page * pageContain + lastItems));
					pagesSets.add(sets);
				}
			}
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			mHandler.removeMessages(MSG_DISMISS_CHOOSE);
			mHandler.sendEmptyMessageDelayed(MSG_DISMISS_CHOOSE, 30000);
		}
	}

}