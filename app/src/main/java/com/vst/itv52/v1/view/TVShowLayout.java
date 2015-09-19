package com.vst.itv52.v1.view;

import java.util.ArrayList;
import java.util.Random;

import net.tsz.afinal.FinalBitmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.db.VSTDBHelper;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.player.LivePlayer;
import com.vst.itv52.v1.player.TVBackActivity;
import com.vst.itv52.v1.player.VideoView;
import com.vst.itv52.v1.util.BitmapWorkerTaskNew;
import com.vst.itv52.v1.util.ConstantUtil;

/**
 * 直播电视页面
 * 
 * @author mygica-hsj
 * 
 */
public class TVShowLayout extends LinearLayout implements IVstHomeView,
		OnFocusChangeListener, OnClickListener {
	private final static String TAG = "TVShowLayout";

	private Context mContext;
	private ImageView refImageView;// 放置阴影的控件
	private RelativeLayout tvLayout;// 需要产生阴影的布局

	private FrameLayout tvScreen;// 预览电视窗口
	private VideoView videoView;

	private FrameLayout[] fls = new FrameLayout[4];
	private ImageView[] backgrouds = new ImageView[4];
	private ImageView[] poster = new ImageView[4];
	private ImageView[] tvLog = new ImageView[4];
	private ImageView whiteBorder;

	private int randomTemp = -1;

	private Handler mHandler = new Handler();

	public TVShowLayout(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public void initView() {
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(VERTICAL);
		View root = LayoutInflater.from(mContext).inflate(R.layout.tv_show,
				null);
		addView(root);

		tvScreen = (FrameLayout) findViewById(R.id.tv_show_tv);
		videoView = (VideoView) findViewById(R.id.tv_show_video);
		videoView.setZOrderOnTop(true);
		videoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				/* 错误处理 */
				return true;
			}
		});

		refImageView = (ImageView) findViewById(R.id.tv_show_reflected_img);
		tvLayout = (RelativeLayout) findViewById(R.id.tv_show_layout);

		fls[0] = (FrameLayout) findViewById(R.id.tv_show_fl_0);
		fls[1] = (FrameLayout) findViewById(R.id.tv_show_fl_1);
		fls[2] = (FrameLayout) findViewById(R.id.tv_show_fl_2);
		fls[3] = (FrameLayout) findViewById(R.id.tv_show_fl_3);

		tvLog[0] = (ImageView) findViewById(R.id.tv_show_log_0);
		tvLog[1] = (ImageView) findViewById(R.id.tv_show_log_1);
		tvLog[2] = (ImageView) findViewById(R.id.tv_show_log_2);
		tvLog[3] = (ImageView) findViewById(R.id.tv_show_log_3);

		poster[0] = (ImageView) findViewById(R.id.tv_show_post_0);
		poster[1] = (ImageView) findViewById(R.id.tv_show_post_1);
		poster[2] = (ImageView) findViewById(R.id.tv_show_post_2);
		poster[3] = (ImageView) findViewById(R.id.tv_show_post_3);

		backgrouds[0] = (ImageView) findViewById(R.id.tv_show_bg_0);
		backgrouds[1] = (ImageView) findViewById(R.id.tv_show_bg_1);
		backgrouds[2] = (ImageView) findViewById(R.id.tv_show_bg_2);
		backgrouds[3] = (ImageView) findViewById(R.id.tv_show_bg_3);

		int[] bgSelector = { R.drawable.blue_no_shadow,
				R.drawable.dark_no_shadow, R.drawable.green_no_shadow,
				R.drawable.orange_no_shadow, R.drawable.pink_no_shadow,
				R.drawable.red_no_shadow, R.drawable.yellow_no_shadow };
		int bgSize = bgSelector.length;
		for (int i = 0; i < 4; i++) {
			poster[i].setOnClickListener(this);
			poster[i].setOnFocusChangeListener(this);
			poster[i].setImageResource(bgSelector[createRandom(bgSize)]);
			backgrouds[i].setVisibility(View.GONE);
		}

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(220, 220);
		lp.leftMargin = 625;
		lp.topMargin = 10;
		whiteBorder.setLayoutParams(lp);
		String tvPicUrl = MyApp.getTvRecommend();
		if(tvPicUrl!=null && tvPicUrl.contains("http://")){
			new BitmapWorkerTaskNew(mContext, videoView, false, false).execute(tvPicUrl);
		}
	}

	private int createRandom(int size) {
		Random random = new Random();
		int randomIndex = random.nextInt(size);
		// 如果本次随机与上次一样，重新随机
		while (randomIndex == randomTemp) {
			randomIndex = random.nextInt(size);
		}
		randomTemp = randomIndex;
		return randomIndex;
	}

	@Override
	public void initListener() {
		tvScreen.setOnFocusChangeListener(this);
		tvScreen.setOnClickListener(this);
	}

	@Override
	public void updateData() {

		Bitmap rebm = ImageReflect.createCutReflectedImage(
				ImageReflect.convertViewToBitmap(tvLayout), 74);
		refImageView.setImageBitmap(rebm);
	}

	ScaleAnimEffect animEffect = new ScaleAnimEffect();
	Runnable play = new Runnable() {
		@Override
		public void run() {
			if (tvScreen.isFocused() && !videoView.isPlaying()) {
				LiveChannelInfo lastChannel = LiveDataHelper.getInstance(
						mContext).getChannelByVid(MyApp.getLastChannel());
				if (lastChannel != null) {
					String url = lastChannel.getSourceUrl(0);
					videoView.setVideoURI(Uri.parse(url));
					videoView.start();
				}
			} else { /* 没有常看记录的 */

			}
		}
	};

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.TOP_FLOAT);
		// if (tvLayout.hasFocus()) {
		// whiteBorder.setVisibility(View.VISIBLE);
		// }
		switch (v.getId()) {
		case R.id.tv_show_tv:
			if (hasFocus) {
				tvScreen.setBackgroundResource(R.drawable.tv_bg_selected);
				tvScreen.bringToFront();
				mHandler.postDelayed(play, 3000);
				tvScreen.setNextFocusUpId(R.id.tv_show);
			} else {
				mHandler.removeCallbacks(play);
				tvScreen.setBackgroundResource(R.drawable.tv_bg_default);
			}
			break;
		case R.id.tv_show_post_0:
			if (hasFocus) {
				showOnFocusAnimation(0);
				flyWhiteBorder(220, 220, 725f, 40f);
			} else {
				showLooseFocusAinimation(0);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.tv_show_post_1:
			if (hasFocus) {
				showOnFocusAnimation(1);
				flyWhiteBorder(220, 220, 930f, 40f);
			} else {
				showLooseFocusAinimation(1);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.tv_show_post_2:
			if (hasFocus) {
				showOnFocusAnimation(2);
				flyWhiteBorder(220, 220, 725, 245f);
			} else {
				showLooseFocusAinimation(2);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.tv_show_post_3:
			if (hasFocus) {
				showOnFocusAnimation(3);
				flyWhiteBorder(220, 220, 930f, 245f);
			} else {
				showLooseFocusAinimation(3);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		}
	}

	private void flyWhiteBorder(int toWidth, int toHeight, float toX, float toY) {
		if (whiteBorder != null && MyApp.flyWhiteBorder) {
			whiteBorder.setVisibility(View.VISIBLE);
			int width = whiteBorder.getWidth();
			int height = whiteBorder.getHeight();
			ViewPropertyAnimator animator = whiteBorder.animate();
			animator.setDuration(150);
			animator.scaleX((float) toWidth / (float) width);
			animator.scaleY((float) toHeight / (float) height);
			animator.x(toX);
			animator.y(toY);
			animator.start();
		}
	}

	@Override
	public void onClick(View v) {
		if (MyApp.isOnline) {
			Intent intent = new Intent(mContext, LivePlayer.class);
			MyApp.playSound(ConstantUtil.COMFIRE);
			switch (v.getId()) {
			case R.id.tv_show_tv:
			case R.id.tv_show_post_0: // 直播
				LiveChannelInfo lastChannel = LiveDataHelper.getInstance(
						mContext).getChannelByVid(MyApp.getLastChannel());
				if (lastChannel != null) {
					System.out.println(lastChannel.toString());
					intent.putExtra(ConstantUtil.LIVE_VID_EXTRA,
							lastChannel.vid);
					intent.putExtra(ConstantUtil.LIVE_TID_EXTRA,
							lastChannel.tid[0]);
				} else {
					ItvToast toast = new ItvToast(mContext);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setText(R.string.toast_live_list_unexsit);
					toast.show();
				}
				break;
			case R.id.tv_show_post_1: // 收藏 没有记录
				String favTid = VSTDBHelper.FAVORITE_TID;

				ArrayList<LiveChannelInfo> favchannels = LiveDataHelper
						.getInstance(mContext).getChannelListByTid(favTid);
				if (favchannels == null || favchannels.isEmpty()) {
					ItvToast toast = new ItvToast(mContext);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setIcon(R.drawable.toast_err);
					toast.setText(R.string.toast_live_no_fav);
					toast.show();
					return;
				}
				intent.putExtra(ConstantUtil.LIVE_TID_EXTRA, favTid);
				intent.putExtra(ConstantUtil.LIVE_VID_EXTRA,
						favchannels.get(0).vid);
				break;
			case R.id.tv_show_post_2: // 回看
				intent = new Intent(mContext, TVBackActivity.class);
				break;
			case R.id.tv_show_post_3:// 自定义没有记录
				String customTid = VSTDBHelper.CUSTOM_TID;
				ArrayList<LiveChannelInfo> cuschannels = LiveDataHelper
						.getInstance(mContext).getChannelListByTid(customTid);
				if (cuschannels == null || cuschannels.isEmpty()
						|| MyApp.getChanState() != 1) {
					ItvToast toast = new ItvToast(mContext);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setIcon(R.drawable.toast_err);
					toast.setText(R.string.toast_live_no_define);
					toast.show();
					return;
				}
				intent.putExtra(ConstantUtil.LIVE_TID_EXTRA, customTid);
				intent.putExtra(ConstantUtil.LIVE_VID_EXTRA,
						cuschannels.get(0).vid);
				break;
			}
			mContext.startActivity(intent);
			((Activity) mContext).overridePendingTransition(R.anim.zoout,
					R.anim.zoin);
		} else {
			ItvToast toast = new ItvToast(mContext);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setIcon(R.drawable.toast_err);
			toast.setText(R.string.toast_net_disconnect_hint);
			toast.show();
		}
	}

	private void showOnFocusAnimation(final int position) {
		tvLayout.bringToFront();
		whiteBorder.bringToFront();
		fls[position].bringToFront();
		animEffect.setAttributs(1.0f, 1.10f, 1.0f, 1.10f, 100);
		Animation anim = animEffect.createAnimation();
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				backgrouds[position].startAnimation(animEffect.alphaAnimation(
						0, 1, 150, 0));
				backgrouds[position].setVisibility(View.VISIBLE);
			}
		});
		tvLog[position].startAnimation(anim);
		poster[position].startAnimation(anim);
	}

	private void showLooseFocusAinimation(int position) {
		animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		poster[position].startAnimation(animEffect.createAnimation());
		tvLog[position].startAnimation(animEffect.createAnimation());
		backgrouds[position].setVisibility(View.GONE);
	}

	public void stopTv() {
		if (videoView.isPlaying()) {
			videoView.stopPlayback();
		}
	}

	@Override
	public void destroy() {

	}
}
