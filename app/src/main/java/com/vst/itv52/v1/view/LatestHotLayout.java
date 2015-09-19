package com.vst.itv52.v1.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.SubjectActivity;
import com.vst.itv52.v1.activity.VideoDetailsActivity;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.util.BitmapWorkerTask;
import com.vst.itv52.v1.util.ConstantUtil;

public class LatestHotLayout extends LinearLayout implements IVstHomeView,
		OnClickListener, OnFocusChangeListener {
	private Context mContext;
	// private Handler mHandler;
	// 当前页数据集合
	private ArrayList<VideoInfo> topRecommends;
	// private boolean onLine = true;
	private FrameLayout[] fls = new FrameLayout[10];// 块
	private ImageView[] posts = new ImageView[10];// 海报控件
	private ImageView[] backGrounds = new ImageView[10];// 背景图片层
	private Bitmap[] bitmaps = new Bitmap[10];// 海报上的图片
	private TextView[] tvs = new TextView[10];// 文字控件

	private ImageView refImageView[] = new ImageView[6];// 放置阴影的控件
	private HorizontalScrollView hsScrollView;
	private ViewGroup latestLayout;
	private ImageView whiteBorder;

	public LatestHotLayout(Context context) {
		super(context);
		mContext = context;
	}

	public void setTopRecommends(ArrayList<VideoInfo> topRecommends) {
		this.topRecommends = topRecommends;
	}

	@Override
	public void initView() {
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(VERTICAL);
		hsScrollView = (HorizontalScrollView) LayoutInflater.from(mContext)
				.inflate(R.layout.latest_recommend, null);
		addView(hsScrollView);
		latestLayout = (ViewGroup) findViewById(R.id.latest_layout);
		refImageView[0] = (ImageView) findViewById(R.id.hot_reflected_img_0);
		refImageView[1] = (ImageView) findViewById(R.id.hot_reflected_img_1);
		refImageView[2] = (ImageView) findViewById(R.id.hot_reflected_img_2);
		refImageView[3] = (ImageView) findViewById(R.id.hot_reflected_img_3);
		refImageView[4] = (ImageView) findViewById(R.id.hot_reflected_img_4);
		refImageView[5] = (ImageView) findViewById(R.id.hot_reflected_img_5);

		fls[0] = (FrameLayout) findViewById(R.id.latest_recommend_fl_0);
		fls[1] = (FrameLayout) findViewById(R.id.latest_recommend_fl_1);
		fls[2] = (FrameLayout) findViewById(R.id.latest_recommend_fl_2);
		fls[3] = (FrameLayout) findViewById(R.id.latest_recommend_fl_3);
		fls[4] = (FrameLayout) findViewById(R.id.latest_recommend_fl_4);
		fls[5] = (FrameLayout) findViewById(R.id.latest_recommend_fl_5);
		fls[6] = (FrameLayout) findViewById(R.id.latest_recommend_fl_6);
		fls[7] = (FrameLayout) findViewById(R.id.latest_recommend_fl_7);
		fls[8] = (FrameLayout) findViewById(R.id.latest_recommend_fl_8);
		fls[9] = (FrameLayout) findViewById(R.id.latest_recommend_fl_9);

		tvs[0] = (TextView) findViewById(R.id.latest_recommend_text_0);
		tvs[1] = (TextView) findViewById(R.id.latest_recommend_text_1);
		tvs[2] = (TextView) findViewById(R.id.latest_recommend_text_2);
		tvs[3] = (TextView) findViewById(R.id.latest_recommend_text_3);
		tvs[4] = (TextView) findViewById(R.id.latest_recommend_text_4);
		tvs[5] = (TextView) findViewById(R.id.latest_recommend_text_5);
		tvs[6] = (TextView) findViewById(R.id.latest_recommend_text_6);
		tvs[7] = (TextView) findViewById(R.id.latest_recommend_text_7);
		tvs[8] = (TextView) findViewById(R.id.latest_recommend_text_8);
		tvs[9] = (TextView) findViewById(R.id.latest_recommend_text_9);

		posts[0] = (ImageView) findViewById(R.id.latest_recommend_poster_0);
		posts[1] = (ImageView) findViewById(R.id.latest_recommend_poster_1);
		posts[2] = (ImageView) findViewById(R.id.latest_recommend_poster_2);
		posts[3] = (ImageView) findViewById(R.id.latest_recommend_poster_3);
		posts[4] = (ImageView) findViewById(R.id.latest_recommend_poster_4);
		posts[5] = (ImageView) findViewById(R.id.latest_recommend_poster_5);
		posts[6] = (ImageView) findViewById(R.id.latest_recommend_poster_6);
		posts[7] = (ImageView) findViewById(R.id.latest_recommend_poster_7);
		posts[8] = (ImageView) findViewById(R.id.latest_recommend_poster_8);
		posts[9] = (ImageView) findViewById(R.id.latest_recommend_poster_9);

		backGrounds[0] = (ImageView) findViewById(R.id.latest_recommend_bg_0);
		backGrounds[1] = (ImageView) findViewById(R.id.latest_recommend_bg_1);
		backGrounds[2] = (ImageView) findViewById(R.id.latest_recommend_bg_2);
		backGrounds[3] = (ImageView) findViewById(R.id.latest_recommend_bg_3);
		backGrounds[4] = (ImageView) findViewById(R.id.latest_recommend_bg_4);
		backGrounds[5] = (ImageView) findViewById(R.id.latest_recommend_bg_5);
		backGrounds[6] = (ImageView) findViewById(R.id.latest_recommend_bg_6);
		backGrounds[7] = (ImageView) findViewById(R.id.latest_recommend_bg_7);
		backGrounds[8] = (ImageView) findViewById(R.id.latest_recommend_bg_8);
		backGrounds[9] = (ImageView) findViewById(R.id.latest_recommend_bg_9);

		for (int i = 0; i < 10; i++) {
			posts[i].setOnClickListener(this);
			posts[i].setOnFocusChangeListener(this);
			backGrounds[i].setVisibility(View.GONE);
			tvs[i].setVisibility(View.GONE);
		}

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(292, 445);
		lp.leftMargin = 100;
		lp.topMargin = 0;
		whiteBorder.setLayoutParams(lp);
	}

	@Override
	public void initListener() {
	}

	int refIndex = 0;

	@Override
	public void updateData() {
		if (topRecommends != null) {
			for (int i = 0; i < topRecommends.size(); i++) {
				VideoInfo info = topRecommends.get(i);
				tvs[i].setText(info.title);
				final int index = i;
				new BitmapWorkerTask(mContext, posts[i], true, true)
						.setCallback(new BitmapWorkerTask.PostCallBack() {
							@Override
							public void post(Bitmap bitmap) {
								if (index == 0 || index == 2 || index == 3
										|| index == 4 || index == 7
										|| index == 9) {
									Bitmap rebm = ImageReflect.createCutReflectedImage(
											ImageReflect
													.convertViewToBitmap(fls[index]),
											0);
									refImageView[refIndex].setImageBitmap(rebm);
									refIndex++;
								}
							}
						}).execute(info.img);
			}
		} else {

		}
	}

	@Override
	public void destroy() {
		for (int i = 0; i < 10; i++) {
			if (bitmaps[i] != null && !bitmaps[i].isRecycled()) {
				bitmaps[i].recycle();
				bitmaps[i] = null;
			}
			fls[i] = null;
			tvs[i] = null;
			backGrounds[i] = null;
			posts[i] = null;
			System.gc();
		}
	}

	ScaleAnimEffect animEffect = new ScaleAnimEffect();

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.TOP_FLOAT);
		// if (latestLayout.hasFocus()) {
		// whiteBorder.setVisibility(View.VISIBLE);
		// }
		switch (v.getId()) {
		case R.id.latest_recommend_poster_0:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(0, 0);
				showOnFocusAnimation(0);
				flyWhiteBorder(292, 445, 100f, 0f);
			} else {
				showLooseFocusAinimation(0);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_1:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(0, 0);
				showOnFocusAnimation(1);
				flyWhiteBorder(445, 220, 441f, -103f);
			} else {
				showLooseFocusAinimation(1);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_2:
			if (hasFocus) {
				showOnFocusAnimation(2);
				flyWhiteBorder(220, 220, 339f, 103f);
			} else {
				showLooseFocusAinimation(2);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_3:
			if (hasFocus) {
				showOnFocusAnimation(3);
				flyWhiteBorder(220, 220, 544f, 103f);
			} else {
				showLooseFocusAinimation(3);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_4:
			if (hasFocus) {
				showOnFocusAnimation(4);
				flyWhiteBorder(300, 445, 784f, 0f);
			} else {
				showLooseFocusAinimation(4);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_5:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1436, 0);
				showOnFocusAnimation(5);
				flyWhiteBorder(220, 220, 1025f, -103f);
			} else {
				showLooseFocusAinimation(5);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_6:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1647, 0);
				showOnFocusAnimation(6);
				flyWhiteBorder(220, 220, 1230f, -103f);
			} else {
				showLooseFocusAinimation(6);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_7:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1679, 0);
				showOnFocusAnimation(7);
				flyWhiteBorder(445, 220, 1128f, 103f);
			} else {
				showLooseFocusAinimation(7);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_8:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1837, 0);
				showOnFocusAnimation(8);
				flyWhiteBorder(220, 220, 1435f, -103f);
			} else {
				showLooseFocusAinimation(8);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.latest_recommend_poster_9:
			if (hasFocus) {
				hsScrollView.smoothScrollTo(1837, 0);
				showOnFocusAnimation(9);
				flyWhiteBorder(220, 220, 1435f, 103f);
			} else {
				showLooseFocusAinimation(9);
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
				backGrounds[position].startAnimation(animEffect.alphaAnimation(
						0, 1, 150, 0));
				tvs[position].startAnimation(animEffect.alphaAnimation(0, 1,
						150, 0));
				tvs[position].setVisibility(View.VISIBLE);
				backGrounds[position].setVisibility(View.VISIBLE);
			}
		});
		posts[position].startAnimation(anim1);
	}

	private void showLooseFocusAinimation(final int position) {
		animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		posts[position].startAnimation(animEffect.createAnimation());
		tvs[position].setVisibility(View.GONE);
		backGrounds[position].setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		MyApp.playSound(ConstantUtil.COMFIRE);
		v.setVisibility(View.VISIBLE);
		if (MyApp.isOnline && topRecommends != null) {
			Intent intent = null;
			VideoInfo info = null;
			switch (v.getId()) {
			case R.id.latest_recommend_poster_0:
				info = topRecommends.get(0);
				break;
			case R.id.latest_recommend_poster_1:
				info = topRecommends.get(1);
				break;
			case R.id.latest_recommend_poster_2:
				info = topRecommends.get(2);
				break;
			case R.id.latest_recommend_poster_3:
				info = topRecommends.get(3);
				break;
			case R.id.latest_recommend_poster_4:
				info = topRecommends.get(4);
				break;
			case R.id.latest_recommend_poster_5:
				info = topRecommends.get(5);
				break;
			case R.id.latest_recommend_poster_6:
				info = topRecommends.get(6);
				break;
			case R.id.latest_recommend_poster_7:
				info = topRecommends.get(7);
				break;
			case R.id.latest_recommend_poster_8:
				info = topRecommends.get(8);
				break;
			case R.id.latest_recommend_poster_9:
				info = topRecommends.get(9);
				break;
			}
			Log.i(VIEW_LOG_TAG, info.toString());
			if (info.zid != null) {
				intent = new Intent(mContext, SubjectActivity.class);
				intent.putExtra(ConstantUtil.VIDEOSUB, info);
			} else {
				intent = new Intent(mContext, VideoDetailsActivity.class);
				intent.putExtra(ConstantUtil.VIDEODEAIL, info.id);
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
