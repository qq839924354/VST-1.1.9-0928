package com.vst.itv52.v1.player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.BaseActivity;
import com.vst.itv52.v1.adapters.TVBackColumnsAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.TvBackBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.model.ChannelColumns;
import com.vst.itv52.v1.model.TVBackChannelJavabean;
import com.vst.itv52.v1.util.ConstantUtil;

public class TVBackActivity extends BaseActivity implements OnErrorListener,
		OnPreparedListener {

	private VideoView videoView;
	private ListView channels;// 电视台列表
	private RadioGroup weekdays;// 星期几标签
	private ListView videos;// 最右边节目列表
	private TextView currentTV;
	private TextView nextTV;
	private TextView currentChannel;

	private RadioButton sunday;
	private RadioButton monday;
	private RadioButton tuesday;
	private RadioButton wednesday;
	private RadioButton thursday;
	private RadioButton friday;
	private RadioButton saturday;

	private WindowManager wm;
	private TextView scalText;
	private ImageView blackBg;
	private PlayerMenuContrl menuContrl;

	private TVBackChannelJavabean backChannels;// 频道
	private ChannelColumns columns;// 栏目
	private ChannelsAdapter channelsAdapter;// 频道适配器
	private TVBackColumnsAdapter columnsAdapter;// 栏目适配器
	private ArrayList<String> vids;// 频道的ID
	private int channelIndex = 0;// 頻道位置记录
	private int columnsIndex = -1;// 栏目位置记录
	private int weekIndex = -1;// 正在播放的星期数位置记录
	private String urlForMp4;// 用于获取MP4列表的链接
	// private long enjoyStart=System.currentTimeMillis();

	private PopupWindow pop;
	private ListView lv;
	private String baseUrl = "http://livecdn.91vst.com/" + "tvback.php";
	private VodCtrBot ctrbot;

	// private VodSeek seek;

	private static final int MSG_HIDE_SCAL = 1;
	private static final int MSG_PLAY_DATA = 2;
	private static final int MSG_DATA = 3;
	private static final int MSG_0x345 = 4;
	private static final int MSG_MP4URL_PREPARED = 5;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DATA:
				updateData();
				break;
			case MSG_PLAY_DATA:
				if (!videoView.isPlaying()) {
					handler.postDelayed(play, 500);
					currentChannel.setText(backChannels.getTvMap().get(
							vids.get(0)));
					currentTV.setText("正在直播");
					nextTV.setText("以实际播放为准");
				}
				break;
			case MSG_0x345:
				loadData();
				break;
			case MSG_HIDE_SCAL:
				if (scalText.getParent() != null)
					wm.removeView(scalText);
				break;
			case MSG_MP4URL_PREPARED:
				// 正在播放，先释放回收
				if (videoView.isPlaying()) {
					videoView.stopPlayback();
				}
				// 进一步解析获得MP4格式播放地址
				ArrayList mp4Urls = (ArrayList) msg.obj;
				try {
					videoView.setVideoURI((Uri[]) mp4Urls.get(0), null,
							(int[]) mp4Urls.get(1));
					// columnsAdapter.dataChanged(position);
				} catch (Exception e) {
					e.printStackTrace();
					if(columns !=null){
						videoView.setVideoURI(Uri.parse(columns.getLiveurl()));
					}
				}
				videoView.start();
				break;
			}
		};
	};

	private Runnable play = new Runnable() {
		@Override
		public void run() {
			if (columns != null) {
				videoView.setVideoURI(Uri.parse(columns.getLiveurl()));
				videoView.start();
			}
		}
	};

	private Runnable getMp4Url = new Runnable() {

		@Override
		public void run() {
			ArrayList mp4Urls = TvBackBiz.parseMP4url(urlForMp4);
			Message msg = new Message();
			msg.what = MSG_MP4URL_PREPARED;
			msg.obj = mp4Urls;
			handler.sendMessage(msg);
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_back_view);
		menuContrl = new PlayerMenuContrl(this, handler, ConstantUtil.MENU_BACK);
		lv = new ListView(this);
		lv.setBackgroundResource(R.drawable.hao366x180);
		pop = new PopupWindow(this);
		pop.setBackgroundDrawable(new BitmapDrawable());
		pop.setFocusable(true);
		pop.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setContentView(lv);

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		scalText = new TextView(this);
		scalText.setTextColor(0xffffffff);
		scalText.setTextSize(25.0f);
		scalText.setGravity(Gravity.CENTER);

		progressShow();

		initData();// 初始化资源数据
		initView();// 实例化视图组件
		initListener();// 添加事件监听
	}

	@Override
	protected void onResume() {
		videoView.start();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		videoView.pause();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//释放资源
		releaseSource();
		if (scalText.getParent() != null)
			wm.removeView(scalText);
		videoView.stopPlayback();
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
		if (ctrbot != null && ctrbot.isShowing()) {
			ctrbot.dismiss();
		}
	}

	private void releaseSource() {
		menuContrl = null;
		backChannels = null;// 频道
		columns = null;// 栏目
		channelsAdapter = null;// 频道适配器
		columnsAdapter = null;// 栏目适配器
		vids = null;// 频道的ID
		urlForMp4 = null;// 用于获取MP4列表的链接
		pop = null;
		lv = null;
		ctrbot = null;
	}

	int rawHeight = 0;
	int rawWidth = 0;
	int[] rawLocation = new int[2];

	public void initView() {
		videoView = (VideoView) findViewById(R.id.tv_back_video);
		videoView.setFocusable(false);
		videoView.setClickable(false);
		videoView.setOnErrorListener(this);
		videoView.setOnPreparedListener(this);

		// seek = new VodSeek(this, videoView, handler);
		ctrbot = new VodCtrBot(this, videoView, handler, false);

		channels = (ListView) findViewById(R.id.tv_back_channles);
		weekdays = (RadioGroup) findViewById(R.id.tv_back_weekdays);
		weekdays.check(weekdays.getChildAt(getSystemWeekday()).getId());
		videos = (ListView) findViewById(R.id.tv_back_videos);

		currentChannel = (TextView) findViewById(R.id.tv_back_current_channel);
		currentTV = (TextView) findViewById(R.id.tv_back_current_tv);
		nextTV = (TextView) findViewById(R.id.tv_back_next_tv);
		blackBg = (ImageView) findViewById(R.id.back_video_blck);

		sunday = (RadioButton) weekdays.findViewById(R.id.tv_back_rd_Sunday);
		monday = (RadioButton) weekdays.findViewById(R.id.tv_back_rd_Monday);
		tuesday = (RadioButton) weekdays.findViewById(R.id.tv_back_rd_Tuesday);
		wednesday = (RadioButton) weekdays
				.findViewById(R.id.tv_back_rd_Wednesday);
		thursday = (RadioButton) weekdays
				.findViewById(R.id.tv_back_rd_Thursday);
		friday = (RadioButton) weekdays.findViewById(R.id.tv_back_rd_Friday);
		saturday = (RadioButton) weekdays
				.findViewById(R.id.tv_back_rd_Saturday);
		columnsAdapter = new TVBackColumnsAdapter(this, null, null, -1);
	}

	public void initData() {
		getSystemWeekday();

		MyApp.pool.execute(new Runnable() {
			@Override
			public void run() {
				backChannels = TvBackBiz.praseChannelList(baseUrl);
				if (backChannels != null)
					handler.sendEmptyMessage(MSG_0x345);
			}
		});
	}

	private void loadData() {
		vids = new ArrayList<String>();
		for (String str : backChannels.getTvMap().keySet()) {
			vids.add(str);
		}
		channelsAdapter = new ChannelsAdapter();
		channels.setAdapter(channelsAdapter);
		channels.requestFocus();
		matchDateToTables();
		String vid = vids.get(channelIndex);
		String date = (String) weekdays.findViewById(
				weekdays.getCheckedRadioButtonId()).getTag();
		getColumns(date, vid);
		// 数据初始化完成，开始播放第一个频道直播
		handler.sendEmptyMessage(MSG_PLAY_DATA);
	}

	final static int FULL_SCEEN = 0;
	final static int RAW_SCEEN = 1;
	boolean isFull = false;

	private void changeFullScreen(int flag) {
		android.widget.FrameLayout.LayoutParams param;
		switch (flag) {
		case FULL_SCEEN:
			blackBg.setVisibility(View.VISIBLE);
			rawHeight = videoView.getHeight();
			rawWidth = videoView.getWidth();
			videoView.getLocationOnScreen(rawLocation);

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
			videos.requestFocus();
			videos.setSelection(columnsIndex);
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
		} else if (videos.hasFocus()) {
			weekdays.requestFocus();
		} else {
			exit();
		}
	}

	int modIndex;

	public void initListener() {

		videoView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == 185 && event.getAction() == KeyEvent.ACTION_DOWN) {
					changeScalType(ConstantUtil.OPERATE_RIGHT);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					ctrbot.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					// seek.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					ctrbot.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					// seek.showAtLocation(videoView, Gravity.BOTTOM, 0, 100);
					return true;
				}
				return false;
			}
		});

		videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				columnsIndex++;
				if (columnsIndex < columns.getList().size()) {
					readyToBack(columnsIndex);
				} else {
					callLive();
					columnsIndex = -1;
				}
			}
		});

		weekdays.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (vids !=null && weekdays !=null) {
					String vid = vids.get(channelIndex);
					String date = (String) weekdays.findViewById(checkedId)
							.getTag();
					// columnsIndex = -1;
					progressShow();
					getColumns(date, vid);
				}
			}
		});

		channels.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				channelIndex = position;
				String vid = vids.get(channelIndex);
				String date = (String) weekdays.findViewById(
						weekdays.getCheckedRadioButtonId()).getTag();
				progressShow();
				getColumns(date, vid);
				columnsIndex = -1;
				// needScaleScreen = false;
			}
		});

		OnItemClickListener itemClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int weekIndexTemp = weekdays.indexOfChild(weekdays
						.findViewById(weekdays.getCheckedRadioButtonId()));
				if (columnsIndex == position && weekIndex == weekIndexTemp) {// 栏目位置和星期数相同
					if (videoView.isPlaying()) {
						changeFullScreen(FULL_SCEEN);
					}
				} else {
					columnsIndex = position;
					// 记录正在播放的是星期几的列表
					weekIndex = weekIndexTemp;
					if ((Integer) view.getTag() != 1) {
						Toast.makeText(TVBackActivity.this,
								"您选择的栏目还未收录，将为您进行直播！", Toast.LENGTH_LONG)
								.show();
						callLive();
					} else {
						readyToBack(position);
					}
				}
			}
		};

		videos.setOnItemClickListener(itemClick);
		lv.setOnItemClickListener(itemClick);
	}

	private void callLive() {
		// 正在播放，先释放回收
		if (videoView.isPlaying()) {
			videoView.stopPlayback();
		}
		if(columns !=null){
			
			videoView.setVideoURI(Uri.parse(columns.getLiveurl()));
			videoView.start();
		}

		if (backChannels !=null) {
			currentChannel.setText(backChannels.getTvMap().get(
					vids.get(channelIndex)));
			currentTV.setText("正在直播");
			nextTV.setText("以实际播放为准");
			columnsIndex = -1;
			columnsAdapter.dataChanged(-1);
		}
	}

	private void readyToBack(int position) {
		// 正在播放，先释放回收
		if (videoView.isPlaying()) {
			videoView.stopPlayback();
		}
		String url = columns.getList().get(position).getUrl();
		///////////////0613
		urlForMp4 = url;//columns.getNexturl().replace("@link", url);
		// 进一步解析获得MP4格式播放地址
		MyApp.pool.execute(getMp4Url);

		columnsAdapter.dataChanged(position);

		currentChannel.setText(backChannels.getTvMap().get(
				vids.get(channelIndex)));
		currentTV.setText(columns.getList().get(position).getChannelText());
		if (position < columns.getList().size() - 1) {
			nextTV.setText(columns.getList().get(position + 1).getChannelText());
		} else {
			nextTV.setText("没有下一个");
		}
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

		videoView.selectScales(modIndex % 3);
		scalText.setText(text);
		if (scalText.getParent() == null) {
			WindowManager.LayoutParams p = new WindowManager.LayoutParams();
			p.gravity = Gravity.TOP | Gravity.RIGHT;
			p.x = 20;
			p.y = 30;
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

	public void updateData() {
		int weekIndexTemp = weekdays.indexOfChild(weekdays
				.findViewById(weekdays.getCheckedRadioButtonId()));
		if (columns == null) {
			Toast.makeText(TVBackActivity.this, "获取栏目列表失败……",
					Toast.LENGTH_SHORT).show();
			columnsAdapter = new TVBackColumnsAdapter(this, null,
					(String) weekdays.getChildAt(weekIndexTemp).getTag(),
					columnsIndex);
		} else {
			if (columns.getList() == null) {
				Toast.makeText(TVBackActivity.this, "获取栏目列表失败……",
						Toast.LENGTH_SHORT).show();
				columnsAdapter.dataChanged(null);
				return;
			}
			if (weekIndex == weekIndexTemp) {// 选中的星期数为正在播放的星期数
				if (columns !=null && weekdays !=null) {
				columnsAdapter = new TVBackColumnsAdapter(this,
						columns.getList(), (String) weekdays.getChildAt(
								weekIndexTemp).getTag(), columnsIndex);
				videos.setAdapter(columnsAdapter);
				lv.setAdapter(columnsAdapter);
				videos.setSelection(columnsIndex);
				lv.setSelection(columnsIndex);
				}
			} else {
				if (columns !=null && weekdays !=null) {
					columnsAdapter = new TVBackColumnsAdapter(this,
							columns.getList(), (String) weekdays.getChildAt(
									weekIndexTemp).getTag(), -1);
					videos.setAdapter(columnsAdapter);
					lv.setAdapter(columnsAdapter);
				}
			}
		}
	}

	/**
	 * 给标签加上对应的日期
	 */
	private void matchDateToTables() {
		Map<String, String> dateMap = backChannels.getDateMap();
		for (String str : dateMap.keySet()) {
			if (str.contains("星期日")) {
				sunday.setTag(dateMap.get(str));
				sunday.setText(cutString(str));
			} else if (str.contains("星期一")) {
				monday.setTag(dateMap.get(str));
				monday.setText(cutString(str));
			} else if (str.contains("星期二")) {
				tuesday.setTag(dateMap.get(str));
				tuesday.setText(cutString(str));
			} else if (str.contains("星期三")) {
				wednesday.setTag(dateMap.get(str));
				wednesday.setText(cutString(str));
			} else if (str.contains("星期四")) {
				thursday.setTag(dateMap.get(str));
				thursday.setText(cutString(str));
			} else if (str.contains("星期五")) {
				friday.setTag(dateMap.get(str));
				friday.setText(cutString(str));
			} else if (str.contains("星期六")) {
				saturday.setTag(dateMap.get(str));
				saturday.setText(cutString(str));
			}
		}
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	private String cutString(String string) {
		return string.substring(string.indexOf(" "), string.length()).trim();
	}

	/**
	 * 获取系统的星期数
	 * 
	 * @return
	 */
	private int getSystemWeekday() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		// Log.i("info", "今天是星期" + week);
		return week;
	}

	/**
	 * 通过频道ID和日期获取当天的栏目信息
	 * 
	 * @param date
	 *            日期
	 * @param id
	 *            頻道ID
	 * @return
	 */
	// private String url;
	private Map<String, String> columnMap;
	private Runnable getColumns = new Runnable() {
		@Override
		public void run() {
			columns = TvBackBiz.parseColumns(baseUrl, columnMap);
			progressDismiss();
			handler.sendEmptyMessage(MSG_DATA);
		}
	};

	private void getColumns(String date, String vid) {
		columnMap = new HashMap<String, String>();
		columnMap.put("vid", vid);
		columnMap.put("date", date);
		MyApp.pool.execute(getColumns);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (isFull) {
				if (menuContrl.isShowing()) {
					menuContrl.dismiss();
				} else {
					menuContrl.showAtLocation(videoView, Gravity.CENTER, 0, 0);
					menuContrl.showVoiceLevel(menuContrl.getVoice());
					menuContrl.setScalor(modIndex % 3);
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class ChannelsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return backChannels.getTvMap().size();
		}

		@Override
		public Object getItem(int position) {
			return backChannels.getTvMap().get(vids.get(position));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(TVBackActivity.this).inflate(
					R.layout.tv_back_channel_item, null);
			TextView tv = (TextView) convertView
					.findViewById(R.id.tv_back_channel_item_text);
			tv.setText(backChannels.getTvMap().get(vids.get(position)));
			return convertView;
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if(mp!=null){
			System.out.println(mp.getDuration());
		}
	}
}
