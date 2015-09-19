package com.vst.itv52.v1.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.VideoInfoAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.SerachBiz;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoList;
import com.vst.itv52.v1.util.ConstantUtil;

public class SearchActivity extends BaseActivity implements OnClickListener {
	private String hostURL;// 初始基础链接，保存固定参数
	private String requestUrl;// 拼接请求连接
	private int page = 1;// 当前页码 默认 为 0
	private GridView grid;
	private EditText input;
	private TextView keybordHint, empteyTv;
	private TextView searHot, searAll, searFilm, searTv, searArt, searCar,
			searDoc;
	// private ImageButton fullClear, fullDel, t9Clear, t9Del;
	private ImageButton fullBord, t9Bord;
	private View fullLayout, t9Layout, topTables;
	private StringBuilder sb;
	private VideoList searchPageData;// 搜索页面数据集合
	private VideoInfoAdapter adapter;
	private String keybordMod;// 按键模式
	private String keyWordTemp = "";
	private int tid = -1;// 分类过滤
	private static final String NUM = "&num=28";
	private static final int MSG_SERCH = 0;
	private static final int MSG_SERCH_RESULT = 1;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SERCH:
				progressShow();
				new Thread(loadThread).start();
				break;
			case MSG_SERCH_RESULT:
				progressDismiss();
				if (searchPageData == null) {
					searchPageData = (VideoList) msg.obj;
				} else {
					VideoList pageList = (VideoList) msg.obj;
					if (pageList != null && pageList.video != null) {
						if (searchPageData.video == null) {
							searchPageData = pageList;
						} else {
							searchPageData.video.addAll(pageList.video);
						}
					}
				}
				if (tid == -1 && sb.length() > 0) {// 还没有进行过搜索，且已有关键字
					tid = 0;
					setCurrentTable(tid + 1);
				} else if (sb.length() <= 0) {// 关键字被全部清除，，还原到热搜
					tid = -1;
					setCurrentTable(tid + 1);
				}
				if (searchPageData != null) {
					adapter.changData(searchPageData.video);
				} else {
					adapter.changData(null);
					grid.setEmptyView(empteyTv);
				}
				break;
			}
		}
	};

	private Runnable loadThread = new Runnable() {
		@Override
		public void run() {
			String keyWord = null;
			if (!TextUtils.isEmpty(sb.toString())) {
				keyWord = keybordMod.equals("t9") ? "T9%23" + sb.toString()
						: sb.toString();
			} else {
				keyWord = sb.toString();
			}
			String url = requestUrl + sb.toString() + "&page=" + page;
			VideoList pageList = SerachBiz.parseSerachResult(requestUrl,
					keyWord, page);
			if (url.equals(requestUrl + sb.toString() + "&page=" + page)) {
				// Log.i(TAG, "开始返回结果");
				Message msg = new Message();
				msg.obj = pageList;
				msg.what = MSG_SERCH_RESULT;
				handler.sendMessage(msg);
			} else {
				// Log.i(TAG, "此结果不返回");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_new);
		hostURL = MyApp.baseServer + "so?data=so" + NUM;
		requestUrl = hostURL;
		keybordMod = MyApp.getSearchKeyBord();
		sb = new StringBuilder();
		// tid = getIntent().getIntExtra("tid", -1);
		initView();
		initListener();
		handler.sendEmptyMessage(MSG_SERCH);
		progressShow();
	}

	public void initView() {
		input = (EditText) findViewById(R.id.search_keybord_input);
		keybordHint = (TextView) findViewById(R.id.search_keybord_hint);
		empteyTv = (TextView) findViewById(R.id.search_empty_text);
		fullLayout = findViewById(R.id.search_keybord_full_layout);
		t9Layout = findViewById(R.id.search_keybord_t9_layout);
		topTables = findViewById(R.id.search_top_tables);
		fullBord = (ImageButton) findViewById(R.id.search_keybord_full);
		t9Bord = (ImageButton) findViewById(R.id.search_keybord_t9);
		searHot = (TextView) findViewById(R.id.search_hot);
		searAll = (TextView) findViewById(R.id.search_all);
		searFilm = (TextView) findViewById(R.id.search_film);
		searTv = (TextView) findViewById(R.id.search_tv);
		searArt = (TextView) findViewById(R.id.search_art);
		searCar = (TextView) findViewById(R.id.search_cartoon);
		searDoc = (TextView) findViewById(R.id.search_doc);
		matchKeybordMod(keybordMod);
		setCurrentTable(tid + 1);
		grid = (GridView) findViewById(R.id.search_result);
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new VideoInfoAdapter(this, null, true);
		grid.setAdapter(adapter);
		grid.setEmptyView(empteyTv);
	}

	private void initListener() {
		fullBord.setOnClickListener(this);
		t9Bord.setOnClickListener(this);
		searHot.setOnClickListener(this);
		searAll.setOnClickListener(this);
		searFilm.setOnClickListener(this);
		searTv.setOnClickListener(this);
		searArt.setOnClickListener(this);
		searCar.setOnClickListener(this);
		searDoc.setOnClickListener(this);

		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(SearchActivity.this,
						VideoDetailsActivity.class);
				intent.putExtra(ConstantUtil.VIDEODEAIL,
						((VideoInfo) adapter.getItem(position)).id);
				startActivity(intent);
				overridePendingTransition(R.anim.zoout, R.anim.zoin);
				/*清除关键字*/
				sb=new StringBuilder();
				input.setText(sb.toString());
			}
		});

		grid.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem >= totalItemCount - visibleItemCount) {
					if (searchPageData != null && page < searchPageData.maxpage) {
						page++;
						handler.sendEmptyMessage(MSG_SERCH);
					}
				}
			}
		});

	}

	public void doClick(View v) {
		int id = v.getId();
		if (id == R.id.search_keybord_full_clear
				|| id == R.id.search_keybord_t9_clear) {
			sb = new StringBuilder();
		} else if (id == R.id.search_keybord_full_del
				|| id == R.id.search_keybord_t9_del) {
			if (sb.length() > 0)
				sb.deleteCharAt(sb.length() - 1);
		} else {
			sb.append(v.getTag());
		}
		readyToSearch();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			sb.append(String.valueOf(0));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_1:
			sb.append(String.valueOf(1));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_2:
			sb.append(String.valueOf(2));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_3:
			sb.append(String.valueOf(3));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_4:
			sb.append(String.valueOf(4));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_5:
			sb.append(String.valueOf(5));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_6:
			sb.append(String.valueOf(6));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_7:
			sb.append(String.valueOf(7));
			break;
		case KeyEvent.KEYCODE_8:
			sb.append(String.valueOf(8));
			readyToSearch();
			break;
		case KeyEvent.KEYCODE_9:
			sb.append(String.valueOf(9));
			readyToSearch();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void readyToSearch() {
		input.setText(sb.toString());
		page = 1;
		handler.removeMessages(MSG_SERCH);
		searchPageData = null;
		handler.sendEmptyMessageDelayed(MSG_SERCH, 1000);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_keybord_full:
			matchKeybordMod("full");
			MyApp.setSearchKeybord("full");
			keybordMod = "full";
			break;
		case R.id.search_keybord_t9:
			matchKeybordMod("t9");
			MyApp.setSearchKeybord("t9");
			keybordMod = "t9";
			break;
		case R.id.search_hot:
			onTopTypesClick(0);
			break;
		case R.id.search_all:
			onTopTypesClick(1);
			break;
		case R.id.search_film:
			onTopTypesClick(2);
			break;
		case R.id.search_tv:
			onTopTypesClick(3);
			break;
		case R.id.search_cartoon:
			onTopTypesClick(4);
			break;
		case R.id.search_art:
			onTopTypesClick(5);
			break;
		case R.id.search_doc:
			onTopTypesClick(6);
			break;
		}
	}

	/**
	 * 选择顶部分类事件处理
	 * 
	 * @param position
	 */
	private void onTopTypesClick(int position) {
		setCurrentTable(position);
		page = 1;
		handler.removeMessages(MSG_SERCH);
		searchPageData = null;
		handler.sendEmptyMessageDelayed(MSG_SERCH, 1000);
	}

	/**
	 * 匹配搜索分类，点亮相应的分类颜色为白色
	 * 
	 * @param position
	 */
	private void setCurrentTable(int position) {
		TextView[] texts = { searHot, searAll, searFilm, searTv, searCar,
				searArt, searDoc };
		for (int i = 0; i < texts.length; i++) {
			if (i == position) {
				texts[i].setSelected(true);
			} else {
				texts[i].setSelected(false);
			}
		}
		tid = position - 1;
		if (tid >= 1) {
			requestUrl = hostURL + "&tid=" + tid;
		} else {
			requestUrl = hostURL;
		}
	}

	/**
	 * 匹配键盘类型
	 * 
	 * @param mod
	 */
	private void matchKeybordMod(String mod) {
		if (mod.equals("full")) {
			fullLayout.setVisibility(View.VISIBLE);
			t9Layout.setVisibility(View.INVISIBLE);
			keybordHint.setText("拼音首字母速查，如《太极》选择TJ");
			fullBord.setBackgroundResource(R.drawable.full_key_broad_h);
			t9Bord.setBackgroundResource(R.drawable.t9_key_broad_sel);
			sb = new StringBuilder();
			input.setText(sb.toString());
		} else {
			fullLayout.setVisibility(View.INVISIBLE);
			t9Layout.setVisibility(View.VISIBLE);
			keybordHint.setText("T9智能匹配，如《太极》选择85");
			fullBord.setBackgroundResource(R.drawable.full_key_broad_sel);
			t9Bord.setBackgroundResource(R.drawable.t9_key_broad_h);
			sb = new StringBuilder();
			input.setText(sb.toString());
		}
	}

	@Override
	public void onBackPressed() {
		if (grid.hasFocus()) {
			topTables.requestFocus();
		} else if (topTables.hasFocus()) {
			if (keybordMod.equals("full")) {
				fullLayout.requestFocus();
			} else {
				t9Layout.requestFocus();
			}
		} else {
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
				input.setText(sb.toString());
				// if (sb.length() <= 0) {// 关键字被全部清除，还原到热搜
				// tid = -1;
				// setCurrentTable(tid + 1);
				// }
				page = 1;
				handler.removeMessages(MSG_SERCH);
				handler.sendEmptyMessageDelayed(MSG_SERCH, 1000);
			} else {
				super.onBackPressed();
				overridePendingTransition(R.anim.fade1, R.anim.fade2);
			}
		}
	}

}
