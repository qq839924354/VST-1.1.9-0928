package com.vst.itv52.v1.view;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.LixianActivity;
import com.vst.itv52.v1.activity.TypeDetailsActivity;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.VideoTypeInfo;
import com.vst.itv52.v1.player.NewsInformation;
import com.vst.itv52.v1.util.BitmapWorkerTask;
import com.vst.itv52.v1.util.ConstantUtil;

public class VideoTypeLayout extends LinearLayout implements IVstHomeView,
		OnClickListener, OnFocusChangeListener {
	private Context mContext;
	private ArrayList<VideoTypeInfo> typePageData;// 影视类型页面数据集合
	private ImageView refImg[] = new ImageView[5];
	private HorizontalScrollView hsScrollView;
	private FrameLayout[] fls = new FrameLayout[7];
	private ImageView[] posts = new ImageView[3];
	private ImageView[] backGrounds = new ImageView[7];// 背景图片层
	private ImageView[] typeLogs = new ImageView[7];// 类型标志
	private ImageView whiteBorder;

	private static int[] bgSelector = { R.drawable.blue_no_shadow,
			R.drawable.dark_no_shadow, R.drawable.green_no_shadow,
			R.drawable.orange_no_shadow, R.drawable.pink_no_shadow,
			R.drawable.red_no_shadow,R.drawable.yellow_no_shadow};

	public VideoTypeLayout(Context context) {
		super(context);
		mContext = context;
	}

	public void setTypePageData(ArrayList<VideoTypeInfo> typePageData) {
		this.typePageData = typePageData;
	}

	@Override
	public void initView() {
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER_HORIZONTAL);
		hsScrollView = (HorizontalScrollView) LayoutInflater.from(mContext)
				.inflate(R.layout.video_type, null);
		addView(hsScrollView);

		refImg[0] = (ImageView) findViewById(R.id.video_type_refimg_0);
		refImg[1] = (ImageView) findViewById(R.id.video_type_refimg_1);
		refImg[2] = (ImageView) findViewById(R.id.video_type_refimg_2);
		refImg[3] = (ImageView) findViewById(R.id.video_type_refimg_3);
		refImg[4] = (ImageView) findViewById(R.id.video_type_refimg_5);

		fls[0] = (FrameLayout) findViewById(R.id.video_type_fl_0);
		fls[1] = (FrameLayout) findViewById(R.id.video_type_fl_1);
		fls[2] = (FrameLayout) findViewById(R.id.video_type_fl_2);
		fls[3] = (FrameLayout) findViewById(R.id.video_type_fl_3);
		fls[4] = (FrameLayout) findViewById(R.id.video_type_fl_4);
		fls[5] = (FrameLayout) findViewById(R.id.video_type_fl_6);
		fls[6] = (FrameLayout) findViewById(R.id.video_type_fl_8);

		posts[0] = (ImageView) findViewById(R.id.video_type_post_0);
		posts[1] = (ImageView) findViewById(R.id.video_type_post_1);
		posts[2] = (ImageView) findViewById(R.id.video_type_post_2);

		typeLogs[0] = (ImageView) findViewById(R.id.video_type_log_0);
		typeLogs[1] = (ImageView) findViewById(R.id.video_type_log_1);
		typeLogs[2] = (ImageView) findViewById(R.id.video_type_log_2);
		typeLogs[3] = (ImageView) findViewById(R.id.video_type_log_3);
		typeLogs[4] = (ImageView) findViewById(R.id.video_type_log_4);
		typeLogs[5] = (ImageView) findViewById(R.id.video_type_log_6);
		typeLogs[6] = (ImageView) findViewById(R.id.video_type_log_8);

		backGrounds[0] = (ImageView) findViewById(R.id.video_type_bg_0);
		backGrounds[1] = (ImageView) findViewById(R.id.video_type_bg_1);
		backGrounds[2] = (ImageView) findViewById(R.id.video_type_bg_2);
		backGrounds[3] = (ImageView) findViewById(R.id.video_type_bg_3);
		backGrounds[4] = (ImageView) findViewById(R.id.video_type_bg_4);
		backGrounds[5] = (ImageView) findViewById(R.id.video_type_bg_6);
		backGrounds[6] = (ImageView) findViewById(R.id.video_type_bg_8);

		for (int i = 0; i < 7; i++) {
			typeLogs[i].setOnClickListener(this);
			typeLogs[i].setOnFocusChangeListener(this);
			backGrounds[i].setVisibility(View.GONE);
			if (i != 0 && i != 1 && i != 4) {
				typeLogs[i].setBackgroundResource(bgSelector[new Random()
						.nextInt(bgSelector.length)]);
			}
		}
		initRef();

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(292, 445);
		lp.leftMargin = 101;
		lp.topMargin = 0;
		whiteBorder.setLayoutParams(lp);
	}

	private void initRef() {
		int refIndex = 0;
		for (int i = 0; i < 7; i++) {
			if (i != 1 && i != 5) {
				Bitmap rebm = ImageReflect.createCutReflectedImage(
						ImageReflect.convertViewToBitmap(fls[i]), 0);
				refImg[refIndex].setImageBitmap(rebm);
				refIndex++;
			}
		}
	}

	@Override
	public void initListener() {

	}

	int refIndex = 0;

	@Override
	public void updateData() {

		if (typePageData != null) {
			// 取出链接地址
			ArrayList<String> imgUrl = new ArrayList<String>();
			for (int i = 0; i < typePageData.size(); i++) {
				String url = typePageData.get(i).logo;
				if (url != null && !url.equals("")) {
					imgUrl.add(url);
				}
			}
			for (int j = 0; j < 3; j++) {
				refIndex = j;
				new BitmapWorkerTask(mContext, posts[j], true, true)
						.setCallback(new BitmapWorkerTask.PostCallBack() {
							@Override
							public void post(Bitmap bitmap) {
								if (refIndex == 0) {
									Bitmap rebm = ImageReflect.createCutReflectedImage(
											ImageReflect
													.convertViewToBitmap(fls[0]),
											0);
									refImg[0].setImageBitmap(rebm);
								} else if (refIndex == 2) {
									Bitmap rebm = ImageReflect.createCutReflectedImage(
											ImageReflect
													.convertViewToBitmap(fls[4]),
											0);
									refImg[3].setImageBitmap(rebm);
								}
							}
						}).execute(imgUrl.get(j));
			}
		} else {

		}
	}

	@Override
	public void destroy() {

	}

	ScaleAnimEffect animEffect = new ScaleAnimEffect();

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.TOP_FLOAT);
		// if (this.hasFocus()) {
		// whiteBorder.setVisibility(View.VISIBLE);
		// }
		switch (v.getId()) {
		case R.id.video_type_log_0:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(0, 0);
				showOnFocusAnimation(0);
				flyWhiteBorder(292, 445, 101, 0f);
			} else {
				whiteBorder.setVisibility(View.INVISIBLE);
				showLooseFocusAinimation(0);
			}
			break;
		case R.id.video_type_log_1:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(0, 0);
				showOnFocusAnimation(1);
				flyWhiteBorder(448, 220, 440f, -103f);
			} else {
				showLooseFocusAinimation(1);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.video_type_log_2:
			if (hasFocus) {
				showOnFocusAnimation(2);
				flyWhiteBorder(220, 220, 338f, 103f);
			} else {
				showLooseFocusAinimation(2);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.video_type_log_3:
			if (hasFocus) {
				showOnFocusAnimation(3);
				flyWhiteBorder(220, 220, 543f, 103f);
			} else {
				showLooseFocusAinimation(3);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.video_type_log_4:
			if (hasFocus) {
				showOnFocusAnimation(4);
				flyWhiteBorder(300, 445, 784f, 0f);
			} else {
				showLooseFocusAinimation(4);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.video_type_log_6:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1432, 0);
				showOnFocusAnimation(5);
				flyWhiteBorder(220, 220, 1028f, -103f);
			} else {
				showLooseFocusAinimation(5);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.video_type_log_8:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1432, 0);
				showOnFocusAnimation(6);
				flyWhiteBorder(220, 220, 1028f, 103f);
			} else {
				showLooseFocusAinimation(6);
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

	private void showOnFocusAnimation(final int position) {
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
				backGrounds[position].startAnimation(animEffect.alphaAnimation(
						0, 1, 150, 0));
				backGrounds[position].setVisibility(View.VISIBLE);

			}
		});
		typeLogs[position].startAnimation(anim);
		if (position == 0) {
			posts[0].startAnimation(animEffect.createAnimation());
		} else if (position == 1) {
			posts[1].startAnimation(animEffect.createAnimation());
		} else if (position == 4) {
			posts[2].startAnimation(animEffect.createAnimation());
		}
	}

	private void showLooseFocusAinimation(int position) {
		animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		if (position == 0) {
			posts[0].startAnimation(animEffect.createAnimation());
		} else if (position == 1) {
			posts[1].startAnimation(animEffect.createAnimation());
		} else if (position == 4) {
			posts[2].startAnimation(animEffect.createAnimation());
		}
		typeLogs[position].startAnimation(animEffect.createAnimation());
		backGrounds[position].setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		v.setVisibility(View.VISIBLE);
		if (MyApp.isOnline && typePageData != null) {
			Intent intent = new Intent(mContext, TypeDetailsActivity.class);
			MyApp.playSound(ConstantUtil.COMFIRE);
			switch (v.getId()) {
			case R.id.video_type_log_0:
				intent = new Intent(mContext, LixianActivity.class);
				break;
			case R.id.video_type_log_1:
				intent.putExtra(ConstantUtil.VIDEOTYPE, typePageData.get(1));
				break;
			case R.id.video_type_log_2:
				intent.putExtra(ConstantUtil.VIDEOTYPE, typePageData.get(2));
				break;
			case R.id.video_type_log_3:
				intent.putExtra(ConstantUtil.VIDEOTYPE, typePageData.get(3));
				break;
			case R.id.video_type_log_4:
				intent.putExtra(ConstantUtil.VIDEOTYPE, typePageData.get(4));
				break;
			case R.id.video_type_log_6:
				intent.putExtra(ConstantUtil.VIDEOTYPE, typePageData.get(5));
				break;
			case R.id.video_type_log_8:
				intent = new Intent(mContext, NewsInformation.class);
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

}
