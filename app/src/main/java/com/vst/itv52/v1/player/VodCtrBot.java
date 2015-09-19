package com.vst.itv52.v1.player;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.StringUtil;

public class VodCtrBot extends PopupWindow {
	private Context mContetx;
	private VideoView mVideoView;
	private Handler mHandler;
	private SeekBar seekBar;
	private View contentView;
	private TextView mEndTime, mCurrentTime, tipTime;
	private boolean mDragging = false;
	private int duration, draggingDura;
	private boolean isVod = false;

	protected final static int FFWD_FLAG = 100; //
	protected final static int REW_FLAG = 101; //
	private static final int TIMEOUT = 10000;
	private final static String TAG = "vodbot"; //
	private static final int INCREMENTMS = 30000;

	public VodCtrBot(Context context, VideoView video, Handler handler,
			boolean isVod) {
		super(context);
		mContetx = context;
		mVideoView = video;
		mHandler = handler;
		this.isVod = isVod;
		init();
	}

	public void init() {
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		LayoutInflater inflater = (LayoutInflater) mContetx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(R.layout.vod_seek_layout, null);
		seekBar = (SeekBar) contentView.findViewById(R.id.vod_seek_new_seekbar);

		seekBar.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				mHandler.removeCallbacks(hide);
				mHandler.postDelayed(hide, TIMEOUT);
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
					setDraggingProgress(REW_FLAG);
					if (isVod) {
						((VodPlayer) mContetx)
								.showHintPop(ConstantUtil.OPERATE_LEFT);
					}
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					setDraggingProgress(FFWD_FLAG);
					if (isVod) {
						((VodPlayer) mContetx)
								.showHintPop(ConstantUtil.OPERATE_RIGHT);
					}
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

		mEndTime = (TextView) contentView.findViewById(R.id.vod_seek_new_dura);
		mCurrentTime = (TextView) contentView
				.findViewById(R.id.vod_seek_new_curr);
		tipTime = (TextView) contentView
				.findViewById(R.id.play_bottom_tip_time);

		setTipTimeLocation(1);
		setContentView(contentView);
	}

	private Runnable hide = new Runnable() {
		@Override
		public void run() {
			if (isShowing())
				dismiss();
		}
	};

	private Runnable hideTip = new Runnable() {
		@Override
		public void run() {
			if (tipTime.getVisibility() == View.VISIBLE) {
				tipTime.setVisibility(View.INVISIBLE);
			}
		}
	};

	protected void setDraggingProgress(int Flag) {
		tipTime.setVisibility(View.VISIBLE);
		mHandler.removeCallbacks(hideTip);
		mHandler.postDelayed(hideTip, 2000);
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
			// tipTime.setVisibility(View.INVISIBLE);
		}
	}

	private void setTipTimeLocation(long pos) {
		int[] location = new int[2];
		contentView.getLocationOnScreen(location);
		seekBar.getLocationOnScreen(location);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
		int left = (int) (pos * (seekBar.getWidth()) / 1000) + location[0]
				- tipTime.getWidth() / 2;
		lp.setMargins(left, 0, 0, 0);
		tipTime.setLayoutParams(lp);
	}

	public void show() {
		// updatePausePlay();
		System.out.println("Seek Show===========>>>");
		showAtLocation(mVideoView, Gravity.CENTER, 0, 200);
		setProgress();
		mHandler.post(updateProgress);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, TIMEOUT);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		setProgress();
		mHandler.post(updateProgress);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, TIMEOUT);
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

			setTipTimeLocation(pos);

			int percent = mVideoView.getBufferPercentage();
			seekBar.setSecondaryProgress(percent * 10);

			mEndTime.setText(StringUtil.stringForTime(duration));
			mCurrentTime.setText(StringUtil.stringForTime(position));
			tipTime.setText(StringUtil.stringForTime(position));
			return position;
		}
		return 0;
	}

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
}
