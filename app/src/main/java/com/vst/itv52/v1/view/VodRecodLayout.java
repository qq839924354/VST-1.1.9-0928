package com.vst.itv52.v1.view;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.FavVideoActivity;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.db.VodDataHelper;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.VodRecode;
import com.vst.itv52.v1.util.BitmapWorkerTask;
import com.vst.itv52.v1.util.ConstantUtil;

public class VodRecodLayout extends LinearLayout implements IVstHomeView,
		OnFocusChangeListener, OnClickListener, Observer {
	private Context mContext;
	private FrameLayout[] fls = new FrameLayout[3];
	private ImageView[] backGrounds = new ImageView[3];
	private ImageView[] posters = new ImageView[3];
	private ImageView[] infos = new ImageView[3];
	private ImageView refImageView;// 放置阴影的控件
	private ImageView whiteBorder;
	private TextView zhuiCount, zhuiName, colectionCount;
	private View colectionLayout;// 收藏记录布局，不包含阴影

	private VodDataHelper helper;
	private VodRecode favBean;// 收藏
	private VodRecode zhuiBean;// 追剧
	private VodRecode playrecode;// 追剧
	private ScaleAnimEffect effect;

	public VodRecodLayout(Context context) {
		super(context);
		this.mContext = context;
		helper = VodDataHelper.getInstance(context);
		helper.addObserver(this);
		effect = new ScaleAnimEffect();
	}

	@Override
	public void initView() {
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER_HORIZONTAL);
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.play_colection, null);
		addView(view);

		fls[0] = (FrameLayout) findViewById(R.id.play_collect_fl_0);
		fls[1] = (FrameLayout) findViewById(R.id.play_collect_fl_1);
		fls[2] = (FrameLayout) findViewById(R.id.play_collect_fl_2);

		posters[0] = (ImageView) findViewById(R.id.play_collect_post_0);
		posters[1] = (ImageView) findViewById(R.id.play_collect_post_1);
		posters[2] = (ImageView) findViewById(R.id.play_collect_post_2);

		backGrounds[0] = (ImageView) findViewById(R.id.play_collect_bg_0);
		backGrounds[1] = (ImageView) findViewById(R.id.play_collect_bg_1);
		backGrounds[2] = (ImageView) findViewById(R.id.play_collect_bg_2);

		infos[0] = (ImageView) findViewById(R.id.play_collect_info_0);
		infos[1] = (ImageView) findViewById(R.id.play_collect_info_1);
		infos[2] = (ImageView) findViewById(R.id.play_collect_info_2);

		colectionLayout = findViewById(R.id.play_colection_layout);
		refImageView = (ImageView) findViewById(R.id.play_collect_reflected_img);

		zhuiCount = (TextView) findViewById(R.id.play_collect_tv_text_count);
		zhuiName = (TextView) findViewById(R.id.play_collect_tv_text_tvname);
		colectionCount = (TextView) findViewById(R.id.play_collect_collected_count);

		for (int i = 0; i < 3; i++) {
			posters[i].setOnFocusChangeListener(this);
			posters[i].setOnClickListener(this);
			backGrounds[i].setVisibility(View.GONE);
		}
		Bitmap rebm = ImageReflect.createCutReflectedImage(
				ImageReflect.convertViewToBitmap(colectionLayout), 80);
		refImageView.setImageBitmap(rebm);

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(292, 445);
		lp.leftMargin = 100;
		lp.topMargin = 0;
		whiteBorder.setLayoutParams(lp);
	}

	@Override
	public void initListener() {

	}

	@Override
	public void updateData() {
		favBean = helper.queryLastRecode(VodDataHelper.FAV);
		zhuiBean = helper.queryLastRecode(VodDataHelper.ZHUI);
		playrecode = helper.queryLastRecode(VodDataHelper.RECODE);

		if (favBean != null) {
			new BitmapWorkerTask(mContext, posters[2], false, false)
					.execute(favBean.imgUrl);
			colectionCount.setText(helper.queryRecodeCount(VodDataHelper.FAV)
					+ "");
		} else {
			posters[2].setImageResource(R.drawable.play_collect_empty);
			colectionCount.setText("0");
		}

		if (playrecode != null) {
			new BitmapWorkerTask(mContext, posters[1], false, false)
					.execute(playrecode.imgUrl);
		} else {
			posters[1].setImageResource(R.drawable.play_history_empty);
		}

		if (zhuiBean != null) {
			new BitmapWorkerTask(mContext, posters[0], false, false)
					.execute(zhuiBean.imgUrl);
			zhuiCount.setText(helper.queryRecodeCount(VodDataHelper.ZHUI) + "");
			zhuiName.setText(zhuiBean.title);
		} else {
			posters[0].setImageResource(R.drawable.play_zhuiju_empty);
			zhuiCount.setText("0");
			zhuiName.setText("暂无剧集");
		}
		Bitmap rebm = ImageReflect.createCutReflectedImage(
				ImageReflect.convertViewToBitmap(colectionLayout), 80);
		refImageView.setImageBitmap(rebm);
	}

	private void zhuijuHint() {
		if (helper.queryRecodeCount(VodDataHelper.ZHUI) > 0) {
			ArrayList<VodRecode> zhuisArrayList = helper
					.queryRecodes(VodDataHelper.ZHUI);
			for(int i=0;i<zhuisArrayList.size();i++){
				
			}
		}
	}

	@Override
	public void destroy() {
		helper.deleteObserver(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.TOP_FLOAT);
		if (this.hasFocus()) {
			whiteBorder.setVisibility(View.VISIBLE);
		}
		switch (v.getId()) {
		case R.id.play_collect_post_0:
			if (hasFocus) {
				showOnFocusAnimation(0);
				flyWhiteBorder(292, 445, 100f, 30f);
			} else {
				showLooseFocusAinimation(0);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.play_collect_post_1:
			if (hasFocus) {
				showOnFocusAnimation(1);
				flyWhiteBorder(304, 445, 375f, 30f);
			} else {
				showLooseFocusAinimation(1);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.play_collect_post_2:
			if (hasFocus) {
				showOnFocusAnimation(2);
				flyWhiteBorder(304, 445, 656f, 30f);
			} else {
				showLooseFocusAinimation(2);
				whiteBorder.setVisibility(View.INVISIBLE);
			}
			break;
		}
	}

	private void flyWhiteBorder(int toWidth, int toHeight, float toX, float toY) {
		if (whiteBorder != null && MyApp.flyWhiteBorder) {
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
		effect.setAttributs(1.0f, 1.10f, 1.0f, 1.10f, 100);
		Animation anim = effect.createAnimation();
		posters[position].startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				backGrounds[position].setVisibility(View.VISIBLE);
				backGrounds[position].startAnimation(effect.alphaAnimation(0,
						1, 120, 30));

			}
		});

		infos[position].startAnimation(anim);

	}

	private void showLooseFocusAinimation(int position) {
		effect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		posters[position].startAnimation(effect.createAnimation());
		infos[position].startAnimation(effect.createAnimation());
		backGrounds[position].setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		MyApp.playSound(ConstantUtil.COMFIRE);
		switch (v.getId()) {
		case R.id.play_collect_post_0:
			intent = new Intent(mContext, FavVideoActivity.class);
			intent.putExtra("favType", VodDataHelper.ZHUI);
			break;
		case R.id.play_collect_post_2:
			intent = new Intent(mContext, FavVideoActivity.class);
			intent.putExtra("favType", VodDataHelper.FAV);
			break;
		case R.id.play_collect_post_1:
			intent = new Intent(mContext, FavVideoActivity.class);
			intent.putExtra("favType", VodDataHelper.RECODE);
			break;
		}
		mContext.startActivity(intent);
		((Activity) mContext).overridePendingTransition(R.anim.zoout,
				R.anim.zoin);

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_RECORD)
				updateData();
		}
	};
	private static final int UPDATE_RECORD = 1;

	@Override
	public void update(Observable observable, Object data) {
		Message mes = Message.obtain();
		mes.what = UPDATE_RECORD;
		handler.sendMessage(mes);// 注意：此处要用handler来更新页面内容
	}

}
