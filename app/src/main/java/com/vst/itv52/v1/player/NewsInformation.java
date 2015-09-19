package com.vst.itv52.v1.player;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.BaseActivity;
import com.vst.itv52.v1.adapters.NewsListAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.NewsBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.model.NewsBean;
import com.vst.itv52.v1.util.ConstantUtil;

public class NewsInformation extends BaseActivity {
	private VideoView videoView;
	private ListView newsList;
	private ListView poplv;
	private TextView playing;
	private ImageView blackBg;
	private String baseUrl;
	private NewsListAdapter newsAdapter;
	private int clickedPosition = 0;// 记录上次点击的位置，用于第二次点击同一位置播放全屏
	private ArrayList<NewsBean> newsVideos;
	boolean isFull = false;
	private PopupWindow pop;
	// private long enjoyStart = System.currentTimeMillis();

	private VodCtrBot ctrbot;
	private WindowManager wm;
	private TextView scalText;
	private PlayerMenuContrl menuContrl;

	private static final int DATA_DOWN_FINISH = 0;
	private final static int FULL_SCEEN = 0;
	private final static int RAW_SCEEN = 1;
	private static final int MSG_HIDE_SCAL = 2;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DATA_DOWN_FINISH:
				if (newsVideos != null && !newsVideos.isEmpty()
						&& newsVideos.size() > 0) {
					loadData();
					initListener();
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_layout);

		videoView = (VideoView) findViewById(R.id.news_video);
		videoView.setFocusable(false);
		videoView.setFocusableInTouchMode(false);
		newsList = (ListView) findViewById(R.id.news_list);
		playing = (TextView) findViewById(R.id.news_current_play);
		blackBg = (ImageView) findViewById(R.id.news_video_black);

		menuContrl = new PlayerMenuContrl(this, handler, ConstantUtil.MENU_NEWS);

		ctrbot = new VodCtrBot(this, videoView, handler, false);

		poplv = new ListView(this);
		poplv.setBackgroundResource(R.drawable.hao366x180);
		pop = new PopupWindow(this);
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setFocusable(true);
		pop.setWidth(400);
		pop.setHeight(LayoutParams.WRAP_CONTENT);
		// pop.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
		// ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setContentView(poplv);
		progressShow();

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		scalText = new TextView(this);
		scalText.setTextColor(0xffffffff);
		scalText.setTextSize(25.0f);
		scalText.setGravity(Gravity.CENTER);
		initData();
	}

	private void initData() {
		baseUrl = MyApp.baseServer + "news.json";
		new Thread() {
			public void run() {
				newsVideos = NewsBiz.parseNews(baseUrl);
				handler.sendEmptyMessage(DATA_DOWN_FINISH);
			};
		}.start();
	}

	@Override
	protected void onPause() {
		videoView.pause();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		videoView.start();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	private void loadData() {
		newsAdapter = new NewsListAdapter(newsVideos, this);
		newsList.setAdapter(newsAdapter);
		newsList.requestFocus();
		poplv.setAdapter(newsAdapter);
		videoView.setVideoURI(Uri.parse(newsVideos.get(0).playurl.replace(
				"@id", newsVideos.get(0).id)));

		progressDismiss();

		videoView.start();
		newsAdapter.stateChange(0);
		playing.setText(newsVideos.get(0).title);
	}

	int modIndex;

	private void initListener() {
		OnItemClickListener itemListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (clickedPosition == position) {
					changeFullScreen(FULL_SCEEN);
				} else {
					if (videoView.isPlaying()) {
						videoView.stopPlayback();
					}
					videoView
							.setVideoURI(Uri
									.parse(newsVideos.get(position).playurl
											.replace("@id",
													newsVideos.get(position).id)));
					videoView.start();
					newsAdapter.stateChange(position);
					newsList.setSelection(position);
					playing.setText(newsVideos.get(position).title);
					clickedPosition = position;
				}
			}
		};
		newsList.setOnItemClickListener(itemListener);
		poplv.setOnItemClickListener(itemListener);

		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				clickedPosition = clickedPosition + 1;
				//有下一个视频
				if (clickedPosition <newsVideos.size()) {
					videoView.setVideoURI(Uri.parse(newsVideos
							.get(clickedPosition).playurl.replace("@id",
							newsVideos.get(clickedPosition).id)));
					videoView.start();
					newsAdapter.stateChange(clickedPosition);
					newsList.setSelection(clickedPosition);
					playing.setText(newsVideos.get(clickedPosition).title);
				}else{
					//播放完了 直接播放第一个源
					clickedPosition=0;
					videoView.setVideoURI(Uri.parse(newsVideos
							.get(clickedPosition).playurl.replace("@id",
							newsVideos.get(clickedPosition).id)));
					videoView.start();
					newsAdapter.stateChange(clickedPosition);
					newsList.setSelection(clickedPosition);
					playing.setText(newsVideos.get(clickedPosition).title);
				}
			}
		});
		videoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				if (mp!=null) {
					//让播放器直接播放下一个源
					ItvToast toast = new ItvToast(NewsInformation.this);
					toast.setText(R.string.toast_newsError_tishi);
					toast.setIcon(R.drawable.toast_shut);
					toast.show();
					if (videoView.isPlaying()) {
						videoView.stopPlayback();
					}
					clickedPosition = clickedPosition + 1;
					//有下一个视频
					if (clickedPosition < newsVideos.size()) {
						videoView.setVideoURI(Uri.parse(newsVideos
								.get(clickedPosition).playurl.replace("@id",
								newsVideos.get(clickedPosition).id)));
						videoView.start();
						newsAdapter.stateChange(clickedPosition);
						newsList.setSelection(clickedPosition);
						playing.setText(newsVideos.get(clickedPosition).title);
					} else {
						//播放完了 直接播放第一个源
						clickedPosition = 0;
						videoView.setVideoURI(Uri.parse(newsVideos
								.get(clickedPosition).playurl.replace("@id",
								newsVideos.get(clickedPosition).id)));
						videoView.start();
						newsAdapter.stateChange(clickedPosition);
						newsList.setSelection(clickedPosition);
						playing.setText(newsVideos.get(clickedPosition).title);
					}
					return true;
				}
				return false;
			}
		});

		videoView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == 185 && event.getAction() == KeyEvent.ACTION_DOWN) {
					changeScalType(ConstantUtil.OPERATE_RIGHT);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					ctrbot.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					ctrbot.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					return true;
				}
				return false;
			}
		});
	}

	public String changeScalType(int derection) {
		if (derection == ConstantUtil.OPERATE_RIGHT) {
			modIndex += 1;
		} else if (derection == ConstantUtil.OPERATE_LEFT) {
			if (modIndex > 0) {
				modIndex--;
			} else {
				modIndex = 3;
			}
		}
		String text = null;
		if (modIndex % 3 == VideoView.A_DEFALT) {
			text = "原始比例";
		} else if (modIndex % 3 == VideoView.A_4X3) {
			text = "4:3";
		} else if (modIndex % 3 == VideoView.A_16X9) {
			text = "16:9";
		}
		// else if (modIndex % 4 == VideoView.A_RAW) {
		// text = "原始大 小";
		// }

		videoView.selectScales(modIndex % 3);
		scalText.setText(text);
		if (scalText.getParent() == null) {
			WindowManager.LayoutParams p = new WindowManager.LayoutParams();
			p.gravity = Gravity.TOP | Gravity.RIGHT;
			p.x = 20;
			p.y = 30;
			// p.type =
			// WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL
			// ;
			p.width = WindowManager.LayoutParams.WRAP_CONTENT;
			p.height = WindowManager.LayoutParams.WRAP_CONTENT;
			p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
			wm.addView(scalText, p);
		}
		handler.removeMessages(MSG_HIDE_SCAL);
		handler.sendEmptyMessageDelayed(MSG_HIDE_SCAL, 3000);
		return text;
	}

	int rawHeight = 0;
	int rawWidth = 0;
	int[] rawLocation = new int[2];

	private void changeFullScreen(int flag) {
		android.widget.FrameLayout.LayoutParams param;
		switch (flag) {
		case FULL_SCEEN:
			blackBg.setVisibility(View.VISIBLE);
			rawHeight = videoView.getHeight();
			rawWidth = videoView.getWidth();
			videoView.getLocationOnScreen(rawLocation);
			System.out.println(rawWidth + " - " + rawHeight + " - "
					+ rawLocation[0] + " - " + rawLocation[1]);

			param = new android.widget.FrameLayout.LayoutParams(-1, -1,
					Gravity.CENTER);
			param.setMargins(0, 0, 0, 0);
			videoView.setLayoutParams(param);
			isFull = true;
			videoView.setFocusable(true);
			videoView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					pop.showAtLocation(videoView, Gravity.RIGHT, 0, 0);
				}
			});
			videoView.requestFocus();
			break;
		case RAW_SCEEN:
			blackBg.setVisibility(View.INVISIBLE);
			param = new android.widget.FrameLayout.LayoutParams(rawWidth,
					rawHeight);
			param.setMargins(rawLocation[0], rawLocation[1], 0, 0);
			videoView.setLayoutParams(param);
			videoView.setFocusable(false);
			videoView.setClickable(false);
			isFull = false;
			newsList.requestFocus();
			newsList.setSelection(clickedPosition);
			break;
		default:
			break;
		}
	}

	private void exit() {
		if (waitExit) {
			waitExit = false;
			ItvToast toast = new ItvToast(this);
			toast.setText(R.string.toast_exit_hint);
			toast.setIcon(R.drawable.toast_shut);
			toast.show();
			handler.postDelayed(cancleExit, 2000);
		} else {
			finish();
		}
	}

	private boolean waitExit = true;
	Runnable cancleExit = new Runnable() {
		@Override
		public void run() {
			waitExit = true;
		}
	};

	@Override
	public void onBackPressed() {
		if (isFull) {
			changeFullScreen(RAW_SCEEN);
		} else {
			exit();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && isFull) {
			if (menuContrl.isShowing()) {
				menuContrl.dismiss();
			} else {
				menuContrl.showAtLocation(videoView, Gravity.CENTER, 0, 0);
				menuContrl.showVoiceLevel(menuContrl.getVoice());
				menuContrl.setScalor(modIndex % 3);
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
		if (ctrbot != null && ctrbot.isShowing()) {
			ctrbot.dismiss();
		}
		super.onDestroy();
	}
}
