package com.vst.itv52.v1.activity;

import java.util.ArrayList;

import net.tsz.afinal.FinalBitmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.VideoTJBiz;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.util.BitmapUtil;
import com.vst.itv52.v1.util.BitmapWorkerTask;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.FirstImageAsyncTaskUtil;
import com.vst.itv52.v1.util.HttpWorkTask;

public class SubjectActivity extends BaseActivity implements OnKeyListener {
	private HorizontalScrollView bgHScrollView, containerHScrollView;
	private ImageView bgImage;
	private LinearLayout container;
	private ArrayList<VideoInfo> subList;
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final String TAG = "SubjectActivity";
	private static final int MSG_INIT_SUBJECTINFO = 0;

	private VideoInfo info;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_INIT_SUBJECTINFO:
				loadData(subList);
				break;
			}
		};
	};

	String hosturl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subject_layout);
		hosturl = MyApp.baseServer + "recommend";
		initIntent();
		initView();
		initData();
		progressShow();
	}

	private void initIntent() {
		Intent intent = getIntent();
		info = (VideoInfo) intent.getSerializableExtra(ConstantUtil.VIDEOSUB);
	}

	private void initView() {
		bgHScrollView = (HorizontalScrollView) findViewById(R.id.subject_pageBG_hs);
		containerHScrollView = (HorizontalScrollView) findViewById(R.id.subject_container_hs);
		bgImage = (ImageView) findViewById(R.id.subject_pageBG);
		container = (LinearLayout) findViewById(R.id.subject_container);
		if (info.logo!=null) {
			Log.i("info", "背景图地址=" + info.logo);
//			new BitmapWorkerTask(this, bgImage, true, true).execute(info.logo);
//			bgImage.setImageBitmap(BitmapUtil.getBitmap(this, info.logo, true));
			FinalBitmap fbBitmap=FinalBitmap.create(this);
			fbBitmap.configLoadingImage(R.drawable.video_details_bg);
			fbBitmap.display(bgImage, info.logo);
		}
	}

	private void initData() {

		new HttpWorkTask<ArrayList<VideoInfo>>(
				new HttpWorkTask.ParseCallBack<ArrayList<VideoInfo>>() {
					@Override
					public ArrayList<VideoInfo> onParse() {
						return VideoTJBiz.parseSubject(hosturl, info.zid);
					}
				}, new HttpWorkTask.PostCallBack<ArrayList<VideoInfo>>() {
					@Override
					public void onPost(ArrayList<VideoInfo> result) {
						if (result != null) {
							subList = result;
							handler.sendEmptyMessage(MSG_INIT_SUBJECTINFO);
						} 
					}
				}).execute();
	}

	private void loadData(ArrayList<VideoInfo> videos) {
		final ScaleAnimEffect effect = new ScaleAnimEffect();
		final ArrayList<View> views = new ArrayList<View>();
		AbsoluteLayout itemView = null;
		Space space = new Space(this);
		space.setMinimumWidth(20);
		space.setMinimumHeight(10);
		container.addView(space);
		for (int i = 0; i < videos.size(); i++) {
			itemView = (AbsoluteLayout) LayoutInflater.from(this).inflate(
					R.layout.subject_item, null);
			final FrameLayout videoFrame = (FrameLayout) itemView
					.findViewById(R.id.subject_frame);
			ImageView poster = (ImageView) itemView
					.findViewById(R.id.subject_poster);
			TextView videoName = (TextView) itemView
					.findViewById(R.id.subject_name);
			ImageView refimg = (ImageView) itemView
					.findViewById(R.id.subject_ref);
			// 取得单个item的数据
			VideoInfo info = videos.get(i);
			// 显示影视名称
			videoName.setText(info.title);
			//设置影片图片
			FirstImageAsyncTaskUtil task = new FirstImageAsyncTaskUtil(this,
					info.img);
			task.setParams(poster, refimg, 50, null);
			task.execute();

			itemView.setTag(i);
			itemView.setFocusable(true);
			itemView.setFocusableInTouchMode(true);
			itemView.setOnKeyListener(this);
			itemView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						MyApp.playSound(ConstantUtil.TOP_FLOAT);
						effect.setAttributs(1.0f, 1.2f, 1.0f, 1.2f, 120);
						Animation anim = effect.createAnimation();
						v.startAnimation(anim);
						videoFrame
								.setBackgroundResource(R.drawable.film_item_selected);
						if (views.size() != 0) {
							View view = views.get(0);
							effect.setAttributs(1.2f, 1.0f, 1.2f, 1.0f, 120);
							anim = effect.createAnimation();
							view.startAnimation(anim);
							view.findViewById(R.id.subject_frame)
									.setBackgroundResource(
											R.drawable.empty_frame_bg);
							views.remove(0);
						}
						views.add(v);
					}
				}
			});
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MyApp.playSound(ConstantUtil.COMFIRE);
					Intent intent = new Intent(SubjectActivity.this,
							VideoDetailsActivity.class);
					int videoid = subList.get((Integer) v.getTag()).id;
					intent.putExtra(ConstantUtil.VIDEODEAIL, videoid);
					startActivity(intent);
					overridePendingTransition(R.anim.zoout, R.anim.zoin);
				}
			});
			container.addView(itemView);
		}
		Space space0 = new Space(this);
		space0.setMinimumWidth(20);
		space0.setMinimumHeight(10);
		container.addView(space0);
		container.requestFocus();
		progressDismiss();
	}

	private void scrollBackGround(int derection) {
		// 总共滚动长度等于背景图片宽度减去屏幕输出宽度；
		int totalScrollWidth = bgImage.getWidth() - 1280;
		if (totalScrollWidth > 0) {
			int itemScrollWidth = totalScrollWidth / subList.size();
			if (derection == RIGHT) {
				bgHScrollView.smoothScrollBy(itemScrollWidth, 0);
			} else if (derection == LEFT) {
				bgHScrollView.smoothScrollBy(itemScrollWidth * (-1), 0);
			}
		}
	}

	private void scrollItemToCenter(View itemView, int deraction) {
		int width = itemView.getWidth();
		System.out.println("item宽度" + width);
		if (deraction == LEFT) {
			containerHScrollView.smoothScrollBy(width * (-1), 0);
		} else if (deraction == RIGHT) {
			containerHScrollView.smoothScrollBy(width, 0);
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				scrollBackGround(LEFT);
				scrollItemToCenter(container.findFocus(), LEFT);
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				scrollBackGround(RIGHT);
				scrollItemToCenter(container.findFocus(), RIGHT);
			} else if (keyCode == KeyEvent.KEYCODE_BACK) {
				this.finish();
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			}
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}

}
