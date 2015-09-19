package com.vst.itv52.v1.player;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.LiveBiz;
import com.vst.itv52.v1.util.HttpWorkTask;

public class LiveControl extends LinearLayout {
	private Context context;
	private Handler handler;
	private static final int AUTO_DISMISS = 10000;

	private WindowManager wm;
	private TextView channleName, channleSource, channleEPG1, sysTime;
	private TextView channleEPG2, channleNum, realSpeed, cwTime;
	private ImageView gifBuff;
	private ProgressBar bufImg;
	private AnimationDrawable anim;

	public LiveControl(Context context, Handler handler) {
		super(context);
		this.context = context;
		this.handler = handler;
		init();
	}

	private void init() {
		initFloatingWindowLayout();
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.live_contrl_new, this);
		channleName = (TextView) view
				.findViewById(R.id.live_control_new_channle_name);
		channleSource = (TextView) view
				.findViewById(R.id.live_control_new_channle_source);
		channleEPG1 = (TextView) view
				.findViewById(R.id.live_control_new_epg_current);
		channleEPG2 = (TextView) view
				.findViewById(R.id.live_control_new_epg_next);
		channleNum = (TextView) view
				.findViewById(R.id.live_control_new_channle_index);
		realSpeed = (TextView) view.findViewById(R.id.live_control_new_speed);
		sysTime = (TextView) view.findViewById(R.id.live_control_new_systime);
		cwTime = (TextView) view.findViewById(R.id.live_control_new_cwtime);
		gifBuff = (ImageView) view.findViewById(R.id.live_control_new_bufgif);
		bufImg = (ProgressBar) view.findViewById(R.id.live_control_new_bufimg);
		anim = (AnimationDrawable) gifBuff.getDrawable();
		// anim.start();
	}

	private WindowManager.LayoutParams mDecorLayoutParams;

	private void initFloatingWindowLayout() {
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mDecorLayoutParams = new WindowManager.LayoutParams();
		WindowManager.LayoutParams p = mDecorLayoutParams;
		p.gravity = Gravity.BOTTOM;
		p.height = LayoutParams.WRAP_CONTENT;
		p.format = PixelFormat.TRANSLUCENT;
		// p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
		p.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		p.windowAnimations = android.R.anim.fade_in;
	}

	private long rxByte, currentTime;

	private Runnable speed = new Runnable() {
		@Override
		public void run() {
			if (rxByte != 0 && currentTime != 0) {
				long tempTime = System.currentTimeMillis();
				long tempByte = TrafficStats.getTotalRxBytes();
				String KbToMb = "KB/S";
				String speed = null;
				if ((tempByte - rxByte) != 0 && (tempTime - currentTime) != 0) {
					long DownloadByte = ((tempByte - rxByte)
							/ (tempTime - currentTime) * 1000 / 1024);
					if (DownloadByte < 1000) {
						KbToMb = "KB/S";
						speed = DownloadByte + KbToMb;
					} else {
						double DownloadByte2 = (double) DownloadByte / 1024D;
						KbToMb = "MB/S";
						speed = new DecimalFormat("#.##").format(DownloadByte2)
								+ KbToMb;
					}
					realSpeed.setText(speed);
				}
				rxByte = tempByte;
				currentTime = tempTime;
			}
			handler.postDelayed(speed, 1000);
		}

	};

	public static final int PREPARED = 1;
	public static final int PREPARING = 0;
	private int flag = 0;

	public void setState(int flag) {
		this.flag = flag;
		switch (flag) {
		case PREPARING:
			anim.start();
			gifBuff.setVisibility(View.VISIBLE);
			bufImg.setVisibility(View.GONE);
			break;
		case PREPARED:
			anim.stop();
			gifBuff.setVisibility(View.GONE);
			bufImg.setVisibility(View.VISIBLE);
			break;
		}
	}

	public void setChannelName(String name) {
		if (channleName != null && name != null)
			channleName.setText(name);
	}

	private String EPG = null;

	public void setEPG(String epgid) {
		EPG = epgid;
	}

	public void setSource(String source) {
		if (channleSource != null && source != null)
			channleSource.setText(source);
	}

	public void setChannelNum(String num) {
		if (channleNum != null && num != null)
			channleNum.setText(num);
	}

	public void startTestSpeeed() {
		rxByte = TrafficStats.getTotalRxBytes();
		currentTime = System.currentTimeMillis();
		handler.postDelayed(speed, 1000);
	}

	/**
	 * 系统时间
	 * 
	 * @param time
	 */
	public void setSysTime(String time) {
		if (sysTime != null && time != null) {
			sysTime.setText(time);
		}
	}

	/**
	 * 加载时间
	 * 
	 * @param time
	 */
	public void setCWTime(String time) {
		if (cwTime != null && time != null) {
			cwTime.setText(time);
		}
	}

	private int defaultProgress = 30;

	public void show() {
		handler.removeCallbacks(hide);
		handler.postDelayed(hide, AUTO_DISMISS);
//		if (MyApp.getChanState() != 2) {
			handler.removeCallbacks(getEpg);
			handler.postDelayed(getEpg, 1000);
//		}
		if (this.getParent() == null) {
			try {
				wm.addView(this, mDecorLayoutParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		setSysTime(new SimpleDateFormat("HH:mm").format(new Date()));
	}

	private Runnable getEpg = new Runnable() {
		@Override
		public void run() {
			getEpg();
		}
	};

	private void getEpg() {
		new HttpWorkTask<Bundle>(new HttpWorkTask.ParseCallBack<Bundle>() {

			@Override
			public Bundle onParse() {
				if (TextUtils.isEmpty(EPG)) {
					return null;
				}
				try {
					return LiveBiz.getLiveEPG(context, EPG);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}, new HttpWorkTask.PostCallBack<Bundle>() {
			@Override
			public void onPost(Bundle result) {
				if (result == null) {
					if (MyApp.LiveEpg != "-" && MyApp.LiveNextEpg != "-") {
						channleEPG1.setText(MyApp.LiveEpg);
						channleEPG2.setText(MyApp.LiveNextEpg);
						bufImg.setProgress((int) LiveBiz.getLiveProgress(
								MyApp.LiveEpg, MyApp.LiveNextEpg));
					} else {
						channleEPG1.setText("当前节目：以实际播放为准");
						channleEPG2.setText("下个节目：以实际播放为准");
						bufImg.setProgress(defaultProgress);
					}
				} else {
					channleEPG1.setText(result.getString("dqjm"));
					channleEPG2.setText(result.getString("xgjm"));
					bufImg.setProgress(result.getInt("progress"));
				}
			}
		}).execute();
	}

	private Runnable hide = new Runnable() {
		@Override
		public void run() {
			if (flag == PREPARED) {
				dismiss();
			} else {
				handler.postDelayed(hide, 2000);
			}
		}
	};

	public void dismiss() {
		handler.removeCallbacks(hide);
		handler.removeCallbacks(speed);
		if (this.getParent() != null) {
			wm.removeView(this);
		}
	}

	public void relese() {
		handler.removeCallbacks(hide);
		handler.removeCallbacks(speed);
		anim.stop();
	}

}
