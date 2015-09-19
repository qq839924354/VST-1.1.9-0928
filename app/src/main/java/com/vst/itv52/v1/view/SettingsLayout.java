package com.vst.itv52.v1.view;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.SettingAboutUs;
import com.vst.itv52.v1.activity.SettingClear;
import com.vst.itv52.v1.activity.SettingLogin;
import com.vst.itv52.v1.activity.SettingOther;
import com.vst.itv52.v1.activity.SettingPlay;
import com.vst.itv52.v1.activity.SettingServer;
import com.vst.itv52.v1.activity.SettingSpeedTest;
import com.vst.itv52.v1.activity.SettingWeather;
import com.vst.itv52.v1.activity.UserSetMain;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.util.ConstantUtil;

public class SettingsLayout extends LinearLayout implements IVstHomeView,
		OnClickListener, OnFocusChangeListener {
	private Context context;
	private ScaleAnimEffect animEffect;
	/**
	 * 数组中的顺序依次为：服务器选择、速度测试、点播设置、天气设置、用户管理、记录清除、其他设置、关于我们
	 */
	private FrameLayout[] fls = new FrameLayout[8];
	private ImageView[] bgs = new ImageView[8];
	private ImageView[] setLog = new ImageView[8];
	private ImageView whiteBorder;
	private int randomTemp = -1;// 随机数缓存，用于避免连续产生2个相同的随机数

	public SettingsLayout(Context context) {
		super(context);
		this.context = context;
		animEffect = new ScaleAnimEffect();
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		this.setGravity(Gravity.CENTER_HORIZONTAL);
		this.setOrientation(VERTICAL);
		View vInflaterRoot = LayoutInflater.from(this.context).inflate(
				R.layout.settings, null);
		this.addView(vInflaterRoot);
		initView();// 实例化视图组件
		initListener();// 添加事件监听
	}

	@Override
	public void initView() {
		int[] bgSelector = { R.drawable.blue_no_shadow,
				R.drawable.dark_no_shadow, R.drawable.green_no_shadow,
				R.drawable.orange_no_shadow, R.drawable.pink_no_shadow,
				R.drawable.red_no_shadow, R.drawable.yellow_no_shadow };
		ImageView[] refImg = new ImageView[4];

		int bgSize = bgSelector.length;
		fls[0] = (FrameLayout) findViewById(R.id.set_server_fl);
		fls[1] = (FrameLayout) findViewById(R.id.set_speed_fl);
		fls[2] = (FrameLayout) findViewById(R.id.set_vod_fl);
		fls[3] = (FrameLayout) findViewById(R.id.set_weather_fl);
		fls[4] = (FrameLayout) findViewById(R.id.set_user_fl);
		fls[5] = (FrameLayout) findViewById(R.id.set_clear_fl);
		fls[6] = (FrameLayout) findViewById(R.id.set_other_fl);
		fls[7] = (FrameLayout) findViewById(R.id.set_about_fl);

		bgs[0] = (ImageView) findViewById(R.id.set_server_bg);
		bgs[1] = (ImageView) findViewById(R.id.set_speed_bg);
		bgs[2] = (ImageView) findViewById(R.id.set_vod_bg);
		bgs[3] = (ImageView) findViewById(R.id.set_weather_bg);
		bgs[4] = (ImageView) findViewById(R.id.set_user_bg);
		bgs[5] = (ImageView) findViewById(R.id.set_clear_bg);
		bgs[6] = (ImageView) findViewById(R.id.set_other_bg);
		bgs[7] = (ImageView) findViewById(R.id.set_about_bg);

		setLog[0] = (ImageView) findViewById(R.id.set_server_iv);
		setLog[1] = (ImageView) findViewById(R.id.set_speed_iv);
		setLog[2] = (ImageView) findViewById(R.id.set_vod_iv);
		setLog[3] = (ImageView) findViewById(R.id.set_weather_iv);
		setLog[4] = (ImageView) findViewById(R.id.set_user_iv);
		setLog[5] = (ImageView) findViewById(R.id.set_clear_iv);
		setLog[6] = (ImageView) findViewById(R.id.set_other_iv);
		setLog[7] = (ImageView) findViewById(R.id.set_about_iv);

		refImg[0] = (ImageView) findViewById(R.id.set_refimg_1);
		refImg[1] = (ImageView) findViewById(R.id.set_refimg_2);
		refImg[2] = (ImageView) findViewById(R.id.set_refimg_3);
		refImg[3] = (ImageView) findViewById(R.id.set_refimg_4);

		for (int i = 0; i < 8; i++) {
			bgs[i].setVisibility(View.GONE);
			setLog[i].setBackgroundResource(bgSelector[createRandom(bgSize)]);
			setLog[i].setOnFocusChangeListener(this);
			setLog[i].setOnClickListener(this);
			if (i - 4 >= 0) {
				refImg[i - 4].setImageBitmap(ImageReflect
						.createCutReflectedImage(
								ImageReflect.convertViewToBitmap(fls[i]), 0));
			}
		}
		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(220, 220);
		lp.leftMargin = 168;
		lp.topMargin = 20;
		whiteBorder.setLayoutParams(lp);
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

	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		MyApp.playSound(ConstantUtil.COMFIRE);
		switch (v.getId()) {
		case R.id.set_server_iv:
			intent = new Intent(context, SettingServer.class);
			break;
		case R.id.set_user_iv:
			String loginKey = MyApp.getLoginKey();
			if (loginKey != null) {
				intent = new Intent(context, UserSetMain.class);
			} else {
				ItvToast toast = new ItvToast(context);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setText(R.string.toast_set_login_hint);
				toast.show();
				intent = new Intent(context, SettingLogin.class);
			}
			break;
		case R.id.set_other_iv:
			intent = new Intent(context, SettingOther.class);
			break;
		case R.id.set_vod_iv:
			intent = new Intent(context, SettingPlay.class);
			break;
		case R.id.set_weather_iv:
			intent = new Intent(context, SettingWeather.class);
			break;
		case R.id.set_speed_iv:
			intent = new Intent(context, SettingSpeedTest.class);
			break;
		case R.id.set_clear_iv:
			intent = new Intent(context, SettingClear.class);
			break;
		case R.id.set_about_iv:
			intent = new Intent(context, SettingAboutUs.class);
			break;
		}
		context.startActivity(intent);
		((Activity) context).overridePendingTransition(R.anim.zoout,
				R.anim.zoin);
	}

	@Override
	public void updateData() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.TOP_FLOAT);
		int position = -1;
		float toX = 0;
		float toY = 0;
		switch (v.getId()) {
		case R.id.set_server_iv:
			position = 0;
			toX = 218f;
			toY = 40f;
			break;
		case R.id.set_speed_iv:
			position = 1;
			toX = 423f;
			toY = 40f;
			break;
		case R.id.set_vod_iv:
			position = 2;
			toX = 628;
			toY = 40f;
			break;
		case R.id.set_weather_iv:
			position = 3;
			toX = 833f;
			toY = 40f;
			break;
		case R.id.set_user_iv:
			position = 4;
			toX = 218f;
			toY = 245f;
			break;
		case R.id.set_clear_iv:
			position = 5;
			toX = 423f;
			toY = 245f;
			break;
		case R.id.set_other_iv:
			position = 6;
			toX = 628;
			toY = 245f;
			break;
		case R.id.set_about_iv:
			position = 7;
			toX = 833f;
			toY = 245f;
			break;
		}
		if (hasFocus) {
			// whiteBorder.setVisibility(View.VISIBLE);
			showOnFocusAnimation(position);
			flyWhiteBorder(220, 220, toX, toY);
		} else {
			showLooseFocusAinimation(position);
			whiteBorder.setVisibility(View.INVISIBLE);
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

	private void showOnFocusAnimation(final int position) {
		fls[position].bringToFront();
		animEffect.setAttributs(1.0f, 1.10f, 1.0f, 1.10f, 100);
		Animation anim1 = animEffect.createAnimation();
		anim1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				bgs[position].startAnimation(animEffect.alphaAnimation(0, 1,
						150, 0));
				bgs[position].setVisibility(View.VISIBLE);
			}
		});
		setLog[position].startAnimation(anim1);

	}

	private void showLooseFocusAinimation(final int position) {
		animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		setLog[position].startAnimation(animEffect.createAnimation());
		bgs[position].setVisibility(View.GONE);
	}

}
