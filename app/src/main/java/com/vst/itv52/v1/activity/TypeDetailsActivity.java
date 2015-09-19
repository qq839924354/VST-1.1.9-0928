package com.vst.itv52.v1.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.TypeDetailsSubMenuAdapter;
import com.vst.itv52.v1.adapters.VideoInfoAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.VideoCateBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.effect.AnimationSetUtils;
import com.vst.itv52.v1.model.VideoList;
import com.vst.itv52.v1.model.VideoTypeInfo;
import com.vst.itv52.v1.util.ConstantUtil;

public class TypeDetailsActivity extends BaseActivity implements OnKeyListener,
		OnItemClickListener {
	private static final String TAG = "TypeDetailsActivity";
	private TextView typeName;// 类型名称
	private TextView sum;// 总共影视数量
	private GridView grid;// 详情列表
	private TextView fliterType;// 子分类过滤提示
	private View menuLayout;// 菜单层
	private ListView rankLv;// 排行列表
	private ListView sharpLv;// 清晰度列表
	private ListView typeLv;// 子分类列表
	private ListView areaLv;// 地区列表
	private ListView timeLv;// 上映时间列表
	private ListView sortLv;// 排序列表
	private ImageView detailMenuKey;
	private int currentPage = 1;// 当前页面数据页码编号
	private int sumPages;// 总共的页码数
	private VideoTypeInfo info;
	private String baseUrl;
	private VideoList videoList;// 页面数据类
	private VideoInfoAdapter gridAdapter;
	private HashMap<String, String> param = new HashMap<String, String>();
	// 时间间隔，用于handler延迟发送命令执行下载任务，优化连翻体验
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final int BOTTOM = 2;
	private ImageView whiteBorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.type_details);
		baseUrl = MyApp.baseServer + "list";
		initIntent();
		initView();
		initData();
		initMenuData();
		initListener();
		progressShow();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initIntent() {
		Intent intent = getIntent();
		info = (VideoTypeInfo) intent
				.getSerializableExtra(ConstantUtil.VIDEOTYPE);
		// 默认参数
		param.put("num", "30");
		param.put("tid", info.tid);
	}

	private void initView() {
		typeName = (TextView) findViewById(R.id.type_details_type);
		sum = (TextView) findViewById(R.id.type_details_sum);
		detailMenuKey = (ImageView) findViewById(R.id.detail_menu_key);
		AnimationSetUtils.SetMenuAnimation(detailMenuKey, R.drawable.menu_key,
				R.drawable.menu_key_blue);
		grid = (GridView) findViewById(R.id.type_details_grid);
		grid.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridAdapter = new VideoInfoAdapter(this, null);
		grid.setAdapter(gridAdapter);
		grid.setFocusableInTouchMode(true);

		whiteBorder = (ImageView) findViewById(R.id.white_border);
		LayoutParams lp = new LayoutParams(162, 238);
		lp.leftMargin = 55;
		lp.topMargin = 112;
		whiteBorder.setLayoutParams(lp);

		fliterType = (TextView) findViewById(R.id.type_details_fliter_type);

		menuLayout = findViewById(R.id.type_details_menulayout);
		menuLayout.setVisibility(View.GONE);

		typeLv = (ListView) menuLayout.findViewById(R.id.filter_list_type);
		typeLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		areaLv = (ListView) menuLayout.findViewById(R.id.filter_list_area);
		areaLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		timeLv = (ListView) menuLayout.findViewById(R.id.filter_list_year);
		timeLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		rankLv = (ListView) menuLayout.findViewById(R.id.filter_list_rank);
		rankLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		sharpLv = (ListView) menuLayout.findViewById(R.id.filter_list_sharp);
		sharpLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		sortLv = (ListView) menuLayout.findViewById(R.id.filter_list_sort);
		sortLv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	private void initData() {
		new Thread() {
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
				if (videoList == null) {
					videoList = VideoCateBiz.parseVideoList(baseUrl, param);
					handler.sendEmptyMessage(MSG_VIDEOINFO_LOADED);
				} else {
					VideoList pageList = VideoCateBiz.parseVideoList(baseUrl,
							param);
					if (pageList != null && videoList!=null) {
						videoList.video.addAll(pageList.video);
						handler.sendEmptyMessage(MSG_VIDEOINFO_LOADED);
					} else {
						// handler.sendEmptyMessage(MSG_LOAD_ERR);
					}
				}
			};
		}.start();
	}

	private void loadData() {
		if (videoList != null) {
			sumPages = videoList.maxpage;
			typeName.setText(info.name);
			sum.setText("共 " + videoList.video_count + " 部");
			gridAdapter.changData(videoList.video);
		}
		progressDismiss();
	}

	private void initListener() {
		typeLv.setOnKeyListener(this);
		typeLv.setOnItemClickListener(this);
		areaLv.setOnKeyListener(this);
		areaLv.setOnItemClickListener(this);
		timeLv.setOnKeyListener(this);
		timeLv.setOnItemClickListener(this);
		rankLv.setOnKeyListener(this);
		rankLv.setOnItemClickListener(this);
		sharpLv.setOnKeyListener(this);
		sharpLv.setOnItemClickListener(this);
		sortLv.setOnKeyListener(this);
		sortLv.setOnItemClickListener(this);

		grid.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				MyApp.playSound(ConstantUtil.TOP_FLOAT);
				whiteBorder.setVisibility(View.VISIBLE);
				flyWhiteBorder(162, 238, arg1.getX() + 55, arg1.getY() + 112);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				whiteBorder.setVisibility(View.INVISIBLE);
			}
		});

		detailMenuKey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (menuLayout.getVisibility() == View.VISIBLE) {
					menuLayout.setVisibility(View.INVISIBLE);
				} else {
					menuLayout.setVisibility(View.VISIBLE);
				}
			}
		});

		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MyApp.playSound(ConstantUtil.COMFIRE);
				Intent intent = new Intent(TypeDetailsActivity.this,
						VideoDetailsActivity.class);
				int videoid=0;
				if(videoList!=null){
					videoid = videoList.video.get(position).id;
				}
				intent.putExtra(ConstantUtil.VIDEODEAIL, videoid);
				startActivity(intent);
				overridePendingTransition(R.anim.zoout, R.anim.zoin);
			}
		});

		grid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				menuLayout.setVisibility(View.VISIBLE);
				grid.clearFocus();
				grid.setFocusable(false);
				menuLayout.requestFocus();
				return true;
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
					pageDown();
				}
			}
		});
	}

	// 由夏禹谟在5月8日修正，如再有改动，请先相互讨论下
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if (menuLayout.getVisibility() == View.GONE) {
				MyApp.playSound(ConstantUtil.COMFIRE);
				menuLayout.setVisibility(View.VISIBLE);
				grid.clearFocus();
				grid.setFocusable(false);
				menuLayout.requestFocus();
				return true;
			} else if (menuLayout.getVisibility() == View.VISIBLE) {
				MyApp.playSound(ConstantUtil.BACK);
				menuLayout.setVisibility(View.GONE);
				menuLayout.clearFocus();
				grid.setFocusable(true);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (menuLayout.getVisibility() == View.VISIBLE) {
				menuLayout.clearFocus();
				menuLayout.setVisibility(View.GONE);
				grid.setFocusable(true);
				return true;
			} else {
				this.finish();
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (menuLayout.getVisibility() == View.GONE) {
				if (getGridSelectionState() == BOTTOM) {
					pageDown();
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (menuLayout.getVisibility() == View.GONE) {
				if (getGridSelectionState() == RIGHT) {
					int selection = grid.getSelectedItemPosition() + 1;
					grid.setSelection(selection);
				} else if (getGridSelectionState() == BOTTOM) {
					pageDown();
				}
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (menuLayout.getVisibility() == View.GONE) {
				if (getGridSelectionState() == LEFT) {
					int selection = grid.getSelectedItemPosition() - 1;
					grid.setSelection(selection);
				}
			}
			break;
		}
		return false;
	}

	private void pageDown() {
		if (currentPage < sumPages) {
			currentPage++;
			System.out.println("页码：" + currentPage);
			param.put("page", currentPage + "");
			initData();
		}
	}

	/**
	 * 判断当前选中的位置在屏幕中的相对位置
	 * 
	 * @param selection
	 * @return
	 */
	private int getGridSelectionState() {
		int selection = grid.getSelectedItemPosition();
		int total = grid.getChildCount();
		if (selection + 12 >= total) {
			return BOTTOM;
		}
		if (selection % 6 == 0) {
			return LEFT;
		} else if (selection % 6 == 5) {
			return RIGHT;
		} else {
			return -1;
		}
	}

	private void pageUp() {
		if (currentPage > 1) {
			currentPage--;
			param.put("page", currentPage + "");
			initData();
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				int position = -1;
				switch (v.getId()) {
				case R.id.filter_list_rank:
					position = rankLv.getSelectedItemPosition();
					if (position == 1 || position == 2) {
						rankLv.setItemChecked(position, true);
						((TypeDetailsSubMenuAdapter) rankLv.getAdapter())
								.setSelctItem(position);
					} else {
						rankLv.setItemChecked(-1, true);
						((TypeDetailsSubMenuAdapter) rankLv.getAdapter())
								.setSelctItem(-1);
					}
					break;
				case R.id.filter_list_area:
					position = areaLv.getSelectedItemPosition();
					areaLv.setItemChecked(position, true);
					((TypeDetailsSubMenuAdapter) areaLv.getAdapter())
							.setSelctItem(position);
					break;
				case R.id.filter_list_sharp:
					position = sharpLv.getSelectedItemPosition();
					sharpLv.setItemChecked(position, true);
					((TypeDetailsSubMenuAdapter) sharpLv.getAdapter())
							.setSelctItem(position);
					break;
				case R.id.filter_list_type:
					position = typeLv.getSelectedItemPosition();
					typeLv.setItemChecked(position, true);
					((TypeDetailsSubMenuAdapter) typeLv.getAdapter())
							.setSelctItem(position);
					break;
				case R.id.filter_list_year:
					position = timeLv.getSelectedItemPosition();
					timeLv.setItemChecked(position, true);
					((TypeDetailsSubMenuAdapter) timeLv.getAdapter())
							.setSelctItem(position);
					break;
				case R.id.filter_list_sort:
					position = sortLv.getSelectedItemPosition();
					sortLv.setItemChecked(position, true);
					((TypeDetailsSubMenuAdapter) sortLv.getAdapter())
							.setSelctItem(position);
					break;
				default:
					break;
				}
			}
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.equals(rankLv)) {
			if (position == 0) {
				Intent intent = new Intent(TypeDetailsActivity.this,
						SearchActivity.class);
				intent.putExtra("tid", Integer.valueOf(info.tid));
				startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				menuLayout.setVisibility(View.GONE);
				menuLayout.clearFocus();
				grid.setFocusable(true);
			} else if (position == 3) {
				clearFilter();
			} else {
				rankLv.setItemChecked(position, true);
				((TypeDetailsSubMenuAdapter) rankLv.getAdapter())
						.setSelctItem(position);
				getFlitedData();
			}
		} else if (parent.equals(areaLv)) {
			areaLv.setItemChecked(position, true);
			((TypeDetailsSubMenuAdapter) areaLv.getAdapter())
					.setSelctItem(position);
			getFlitedData();
		} else if (parent.equals(sharpLv)) {
			sharpLv.setItemChecked(position, true);
			((TypeDetailsSubMenuAdapter) sharpLv.getAdapter())
					.setSelctItem(position);
			getFlitedData();
		} else if (parent.equals(typeLv)) {
			typeLv.setItemChecked(position, true);
			((TypeDetailsSubMenuAdapter) typeLv.getAdapter())
					.setSelctItem(position);
			getFlitedData();
		} else if (parent.equals(timeLv)) {
			timeLv.setItemChecked(position, true);
			((TypeDetailsSubMenuAdapter) timeLv.getAdapter())
					.setSelctItem(position);
			getFlitedData();
		} else if (parent.equals(sortLv)) {
			sortLv.setItemChecked(position, true);
			((TypeDetailsSubMenuAdapter) sortLv.getAdapter())
					.setSelctItem(position);
			getFlitedData();
		}
	}

	private void getFlitedData() {
		splitJointString();
		videoList = null;
		initData();
		menuLayout.setVisibility(View.GONE);
		menuLayout.clearFocus();
		grid.setFocusable(true);
	}

	/**
	 * 将选中的过滤项目拼接进链接中
	 */
	private void splitJointString() {
		currentPage = 1;
		param.put("page", currentPage + "");
		StringBuilder sb = new StringBuilder();
		int checkPosition = -1;

		checkPosition = rankLv.getCheckedItemPosition();
		if (checkPosition > 0 && checkPosition < 3) {
			String rank = (String) rankLv.getAdapter().getItem(checkPosition);
			if (rank.equals("最新上线")) {
				param.put("top", "1");
			} else if (rank.equals("最热门")) {
				param.put("top", "2");
			}
			sb.append(rank + " ");
			checkPosition = -1;
		}

		checkPosition = areaLv.getCheckedItemPosition();
		if (checkPosition >= 0) {
			String area = (String) areaLv.getAdapter().getItem(checkPosition);
			param.put("area", area);
			sb.append(area + " ");
			checkPosition = -1;
		}

		checkPosition = sharpLv.getCheckedItemPosition();
		if (checkPosition >= 0) {
			String sharp = (String) sharpLv.getAdapter().getItem(checkPosition);
			if (sharp.equals("超清")) {
				param.put("qxd", "3");
			} else if (sharp.equals("高清")) {
				param.put("qxd", "2");
			} else {
				param.put("qxd", "");
			}
			sb.append(sharp + " ");
			checkPosition = -1;
		}

		checkPosition = typeLv.getCheckedItemPosition();
		if (checkPosition >= 0) {
			String type = (String) typeLv.getAdapter().getItem(checkPosition);
			param.put("item", type);
			sb.append(type + " ");
			checkPosition = -1;
		}

		checkPosition = timeLv.getCheckedItemPosition();
		if (checkPosition >= 0) {
			String year = (String) timeLv.getAdapter().getItem(checkPosition);
			param.put("year", year);
			sb.append(year + " ");
			checkPosition = -1;
		}

		checkPosition = sortLv.getCheckedItemPosition();
		if (checkPosition >= 0) {
			String sort = (String) sortLv.getAdapter().getItem(checkPosition);
			if (sort.equals("从新到旧")) {
				param.put("sort", "1");
			} else if (sort.equals("从旧到新")) {
				param.put("sort", "2");
			} else if (sort.equals("评分从高到低")) {
				param.put("sort", "3");
			} else if (sort.equals("评分从低到高")) {
				param.put("sort", "4");
			} else {
				param.put("sort", "0");
			}
			sb.append(sort);
			checkPosition = -1;
		}
		fliterType.setText(sb.toString());
	}

	private void clearFilter() {
		((TypeDetailsSubMenuAdapter) rankLv.getAdapter()).setSelctItem(-1);
		rankLv.setItemChecked(-1, true);
		((TypeDetailsSubMenuAdapter) areaLv.getAdapter()).setSelctItem(-1);
		areaLv.setItemChecked(-1, true);
		((TypeDetailsSubMenuAdapter) sharpLv.getAdapter()).setSelctItem(-1);
		sharpLv.setItemChecked(-1, true);
		((TypeDetailsSubMenuAdapter) typeLv.getAdapter()).setSelctItem(-1);
		typeLv.setItemChecked(-1, true);
		((TypeDetailsSubMenuAdapter) timeLv.getAdapter()).setSelctItem(-1);
		timeLv.setItemChecked(-1, true);
		((TypeDetailsSubMenuAdapter) sortLv.getAdapter()).setSelctItem(-1);
		sortLv.setItemChecked(-1, true);
		param.clear();
		// 默认参数
		param.put("num", "30");
		param.put("tid", info.tid);
		fliterType.setText(null);
		videoList = null;
		initData();
	}

	private void initMenuData() {
		new Thread() {
			public void run() {

				final Map<String, ArrayList<String>> map = VideoCateBiz
						.parseCateList(baseUrl, info.tid);
				if (map == null) {

				} else {
					handler.post(new Runnable() {
						@Override
						public void run() {
							TypeDetailsSubMenuAdapter adapter = null;
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, map.get("item"));
							typeLv.setAdapter(adapter);
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, map.get("area"));
							areaLv.setAdapter(adapter);
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, map.get("year"));
							timeLv.setAdapter(adapter);

							ArrayList<String> rank = new ArrayList<String>();
							rank.add("转到搜索");
							rank.add("最新上线");
							rank.add("最热门");
							rank.add("清空筛选");
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, rank);
							rankLv.setAdapter(adapter);

							ArrayList<String> sharp = new ArrayList<String>();
							sharp.add("全部");
							sharp.add("超清");
							sharp.add("高清");
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, sharp);
							sharpLv.setAdapter(adapter);

							ArrayList<String> sort = new ArrayList<String>();
							sort.add("默认");
							sort.add("从新到旧");
							sort.add("从旧到新");
							sort.add("评分从高到低");
							sort.add("评分从低到高");
							adapter = new TypeDetailsSubMenuAdapter(
									TypeDetailsActivity.this, sort);
							sortLv.setAdapter(adapter);
						}
					});
				}
			}
		}.start();
	}

	private static final int MSG_VIDEOINFO_LOADED = 0;
	// private static final int MSG_LOAD_VIDEOINFO = 1;
	private static final int MSG_INIT_MENU = 2;
	private static final int MSG_FAILED = 3;
	private static final int MSG_LOAD_ERR = 4;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_VIDEOINFO_LOADED:
				loadData();
				break;
			case MSG_LOAD_ERR:
				ItvToast toast = new ItvToast(TypeDetailsActivity.this);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setText(R.string.toast_request_page_data_err);
				toast.show();
				break;
			case MSG_FAILED:
				AlertDialog dialog2 = new AlertDialog.Builder(
						TypeDetailsActivity.this).setTitle("网络异常")
						.setMessage("网络异常，请稍后重试！").create();
				DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case AlertDialog.BUTTON_POSITIVE:
							TypeDetailsActivity.this.finish();
							break;
						case AlertDialog.BUTTON_NEUTRAL:
							initMenuData();
							initData();
							break;
						default:
							break;
						}
					}
				};
				dialog2.setButton(AlertDialog.BUTTON_POSITIVE, "确认", listener2);
				dialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "重试", listener2);
				dialog2.show();
				break;
			}
		}
	};

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
}
