package com.vst.itv52.v1.player;

import java.text.DecimalFormat;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class VodCtrTop extends PopupWindow {

	private Context mContetx;
	private Handler mHandler;
	private TextView txtName, txtSpeed;
	private ImageView source, sharp, scale;
	private int autoHideDelay;

	public VodCtrTop(Context context, Handler handler, int delay) {
		super(context);
		mContetx = context;
		mHandler = handler;
		autoHideDelay = delay;
		init();
	}

	public VodCtrTop(Context context, Handler handler) {
		this(context, handler, 10000);
	}

	public void init() {
		setBackgroundDrawable(mContetx.getResources().getDrawable(
				android.R.color.transparent));
		setFocusable(true);
		setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LayoutInflater inflater = (LayoutInflater) mContetx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = inflater.inflate(R.layout.vod_play_top, null);
		txtName = (TextView) root.findViewById(R.id.vod_play_video_name);
		txtSpeed = (TextView) root.findViewById(R.id.vod_play_speed);
		source = (ImageView) root.findViewById(R.id.vod_play_source);
		scale = (ImageView) root.findViewById(R.id.vod_play_scale);
		sharp = (ImageView) root.findViewById(R.id.vod_play_sharp);
		setContentView(root);
	}

	public void setVideoName(String name) {
		txtName.setText(name);
	}

	public void setSourceTag(int resId) {
		if (source != null)
			source.setImageResource(resId);
	}

	public void setScaleTag(int flag) {
		if (scale != null) {
			switch (flag) {
			case VideoView.A_16X9:
				scale.setImageResource(R.drawable.osd_16_9);
				break;
			case VideoView.A_4X3:
				scale.setImageResource(R.drawable.osd_4_3);
				break;
			case VideoView.A_DEFALT:
				scale.setImageResource(R.drawable.osd_default);
				break;
			case VideoView.A_RAW:
				scale.setImageResource(R.drawable.osd_raw);
				break;
			default:
				break;
			}
		}
	}

	public void setHdSdTag(int height) {
		if (height >= 1080) {
			sharp.setImageResource(R.drawable.osd_1080p);
		} else if (1080 > height && height >= 720) {
			sharp.setImageResource(R.drawable.osd_720p);
		} else {
			sharp.setImageResource(R.drawable.osd_sdp);
		}
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
					txtSpeed.setText(speed);
				}
				rxByte = tempByte;
				currentTime = tempTime;
			}
			mHandler.postDelayed(speed, 1000);
		}
	};

	private void startTestSpeeed() {
		rxByte = TrafficStats.getTotalRxBytes();
		currentTime = System.currentTimeMillis();
		mHandler.postDelayed(speed, 1000);
	}

	Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			dismiss();
		}
	};

	@Override
	public void dismiss() {
		mHandler.removeCallbacks(autoHide);
		mHandler.removeCallbacks(speed);
		super.dismiss();
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		mHandler.removeCallbacks(autoHide);
		mHandler.postDelayed(autoHide, autoHideDelay);
		startTestSpeeed();
		super.showAtLocation(parent, gravity, x, y);
	}

}
