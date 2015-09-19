package com.vst.itv52.v1.player;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.SharpnessEnum;
import com.vst.itv52.v1.model.VideoPlayUrl;
import com.vst.itv52.v1.util.StringUtil;

public class XLLXCtr extends PopupWindow {

	private Context mContetx;
	private VideoView mVideoView;
	private Handler mHandler;
	private static final int TIMEOUT = 5000;
	private boolean mDragging = false;
	private int duration, draggingDura;
	private TextView name, mEndTime, mCurrentTime, tipTime;
	private SeekBar seekBar;
	private ImageView playOrPause, forward, sharpness;
	private RadioGroup sharpnessRg;
	private ArrayList<VideoPlayUrl> urls;

	public XLLXCtr(Context context, VideoView video, Handler handler) {
		super(context);
		mContetx = context;
		mVideoView = video;
		mHandler = handler;
		init();
	}

	private int setProgress() {
		if (mVideoView == null || mDragging) {
			return 0;
		}
		if (mVideoView.isPlaying()) {
			int position = mVideoView.getCurrentPosition();
			duration = mVideoView.getDuration();

			long pos = 1000L * position / duration;
			seekBar.setProgress((int) pos);

			int percent = mVideoView.getBufferPercentage();
			seekBar.setSecondaryProgress(percent * 10);

			mEndTime.setText(StringUtil.stringForTime(duration));
			mCurrentTime.setText(StringUtil.stringForTime(position));
			tipTime.setText(StringUtil.stringForTime(position));
			return position;
		}
		return 0;
	}

	public void setVideoName(String text) {
		name.setText(text);
	}

	View contentView;

	public void init() {
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		LayoutInflater inflater = (LayoutInflater) mContetx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.lixian_ctrbot_layout, null);
		name = (TextView) contentView.findViewById(R.id.xllx_ctr_videoName);
		tipTime = (TextView) contentView
				.findViewById(R.id.xllx_ctr_tiptime_txt);
		seekBar = (SeekBar) contentView.findViewById(R.id.xllx_ctr_seekbar);
		seekBar.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mHandler.removeCallbacks(hide);
					mHandler.postDelayed(hide, TIMEOUT);

					tipTime.setVisibility(View.VISIBLE);
				} else {
					tipTime.setVisibility(View.INVISIBLE);
				}
			}
		});

		seekBar.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				mHandler.removeCallbacks(hide);
				mHandler.postDelayed(hide, TIMEOUT);
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					setDraggingProgress(REW_FLAG);
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					setDraggingProgress(FFWD_FLAG);
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_UP
						&& (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)) {
					if (mDragging && mVideoView.isPlaying()) {
						mVideoView.seekTo(draggingDura);
						mDragging = false;
					}
					return true;
				}
				return false;
			}
		});

		mEndTime = (TextView) contentView.findViewById(R.id.xllx_ctr_duration);
		mCurrentTime = (TextView) contentView
				.findViewById(R.id.xllx_ctr_current_time);

		playOrPause = (ImageView) contentView
				.findViewById(R.id.xllx_ctr_play_pause);
		forward = (ImageView) contentView.findViewById(R.id.xllx_ctr_forward);
		sharpness = (ImageView) contentView
				.findViewById(R.id.xllx_ctr_sharpness_iv);
		sharpnessRg = (RadioGroup) contentView
				.findViewById(R.id.xllx_ctr_sharpness_choose);

		playOrPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.removeCallbacks(hide);
				mHandler.postDelayed(hide, TIMEOUT);
				doPauseResume();
			}
		});

		sharpness.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if (hasFocus) {
					mHandler.removeCallbacks(hide);
					mHandler.postDelayed(hide, TIMEOUT);
					sharpnessRg.setVisibility(View.VISIBLE);
				} else if (!hasFocus && !sharpnessRg.hasFocus()) {
					sharpnessRg.setVisibility(View.INVISIBLE);
				}

			}
		});

		sharpnessRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mHandler.removeCallbacks(hide);
				mHandler.postDelayed(hide, TIMEOUT);
				RadioButton rb = (RadioButton) group.findViewById(checkedId);
				VideoPlayUrl url = (VideoPlayUrl) rb.getTag();
				int index = urls.indexOf(url);
				VideoPlayUrl currentSharp = urls.get(index);
				sharpness.setImageResource(StringUtil
						.getSharpByName(currentSharp.sharp.getName()));
				sharpnessRg.removeAllViews();
				for (int i = 0; i < urls.size(); i++) {
					if (i != index) {
						RadioButton nrb = new RadioButton(mContetx);
						nrb.setButtonDrawable(new BitmapDrawable());
						nrb.setBackgroundResource(StringUtil
								.getSharpByName(urls.get(i).sharp.getName()));
						nrb.setTag(urls.get(i));
						rb.setOnFocusChangeListener(rbFocus);
						sharpnessRg.addView(nrb);
						LinearLayout.LayoutParams params = new LayoutParams(-2,
								-2);
						params.leftMargin = 10;
						nrb.setLayoutParams(params);
					}
				}
				sharpnessRg.setVisibility(View.INVISIBLE);
				// 更新完Ui 换源
				mHandler.sendMessage(mHandler.obtainMessage(VodPlayer.MSG_PLAY,
						mVideoView.getCurrentPosition(), 0,
						currentSharp.playurl));
				mVideoView.setVideoURI(Uri.parse(currentSharp.playurl));
			}
		});

		setTipTimeLocation(1);
		setContentView(contentView);
	}

	private final static int FFWD_FLAG = 100; //
	private final static int REW_FLAG = 101; //
	private final static String TAG = "vodbot"; //
	private static final int INCREMENTMS = 30000;

	protected void setDraggingProgress(int Flag) {
		if (!mDragging && mVideoView.isPlaying()) {
			draggingDura = mVideoView.getCurrentPosition();
		}
		mDragging = true;
		if (duration > 0) {
			switch (Flag) {
			case FFWD_FLAG:
				draggingDura += INCREMENTMS;
				break;
			case REW_FLAG:
				draggingDura -= INCREMENTMS;
				break;
			default:
				break;
			}
			if (draggingDura < 0) {
				draggingDura = 0;
			} else if (draggingDura > duration) {
				draggingDura = duration;
			}
			long pos = 1000L * draggingDura / duration;
			seekBar.setProgress((int) pos);

			setTipTimeLocation(pos);
			tipTime.setText(StringUtil.stringForTime(draggingDura));

		}
	}

	private void setTipTimeLocation(long pos) {
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
		int left = (int) (pos * (seekBar.getWidth() - 20) / 1000) + 90;
		int top = 20;
		lp.setMargins(left, top, 0, 0);
		tipTime.setLayoutParams(lp);
	}

	public static final int SEEK = 1;
	public static final int PAUSE = 2;

	public void show(int flag) {
		updatePausePlay();
		if (flag == SEEK) {
			seekBar.requestFocus();
		} else if (flag == PAUSE) {
			playOrPause.requestFocus();
		}
		showAtLocation(mVideoView, Gravity.BOTTOM, 0, 0);
		mHandler.post(updateProgress);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, TIMEOUT);
	}

	private Runnable hide = new Runnable() {
		@Override
		public void run() {
			if (isShowing())
				dismiss();
		}
	};
	private Runnable updateProgress = new Runnable() {
		@Override
		public void run() {
			int pos = 0;
			if (isShowing() && mVideoView.isPlaying()) {
				pos = mVideoView.getCurrentPosition();
				if (!mDragging) {
					setProgress();
				} else {
					mCurrentTime.setText(StringUtil.stringForTime(pos));
				}
			}
			mHandler.postDelayed(updateProgress, 1000 - (pos % 1000));
		}
	};

	@Override
	public void dismiss() {
		mDragging = false;
		mHandler.removeCallbacks(hide);
		mHandler.removeCallbacks(updateProgress);
		super.dismiss();
	}

	private void doPauseResume() {
		if (this != null && this.isShowing()) {
			mHandler.removeCallbacks(hide);
			mHandler.postDelayed(hide, TIMEOUT);

			if (mVideoView.isPlaying()) {
				mVideoView.pause();
			} else {
				mVideoView.start();
			}
			updatePausePlay();
		}
	}

	private void updatePausePlay() {
		if (mVideoView.isPlaying()) {
			playOrPause.setImageResource(R.drawable.osd_pause_selector);
		} else {
			playOrPause.setImageResource(R.drawable.osd_play_selector);
		}
	}

	private OnFocusChangeListener rbFocus = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				boolean b = false;
				for (int i = 0; i < sharpnessRg.getChildCount(); i++) {
					b = b | sharpnessRg.getChildAt(i).hasFocus();
				}
				if (!b)
					sharpnessRg.setVisibility(View.INVISIBLE);
			}
		}
	};

	public void setSharpness(ArrayList<VideoPlayUrl> urls, SharpnessEnum sharp) {
		this.urls = urls;
		sharpness.setImageResource(StringUtil.getSharpByName(sharp.getName()));
		sharpnessRg.removeAllViews();
		for (int i = 0; i < urls.size(); i++) {
			if (urls.get(i).sharp != sharp) {
				RadioButton rb = new RadioButton(mContetx);
				rb.setButtonDrawable(new BitmapDrawable());
				rb.setBackgroundResource(StringUtil.getSharpByName(urls.get(i).sharp
						.getName()));
				rb.setTag(urls.get(i));
				rb.setOnFocusChangeListener(rbFocus);
				sharpnessRg.addView(rb);
				LinearLayout.LayoutParams params = new LayoutParams(-2, -2);
				params.leftMargin = 10;
				rb.setLayoutParams(params);
			}
		}
	}

}
