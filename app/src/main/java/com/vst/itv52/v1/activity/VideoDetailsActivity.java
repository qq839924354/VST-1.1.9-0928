package com.vst.itv52.v1.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.AllPagesAdapter;
import com.vst.itv52.v1.adapters.DetailsKeyGridAdapter;
import com.vst.itv52.v1.adapters.DetailsKeyListAdapter;
import com.vst.itv52.v1.adapters.DetailsKeyTabAdapter;
import com.vst.itv52.v1.adapters.HotVideoAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.VideoDetailBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.VodDataHelper;
import com.vst.itv52.v1.effect.Reflect3DImage;
import com.vst.itv52.v1.model.VideoDetailInfo;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoSet;
import com.vst.itv52.v1.model.VideoSource;
import com.vst.itv52.v1.model.VodRecode;
import com.vst.itv52.v1.player.VodPlayer;
import com.vst.itv52.v1.util.BitmapWorkerTask;
import com.vst.itv52.v1.util.BitmapWorkerTask.PostCallBack;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.StringUtil;

//import android.view.View.OnClickListener;

public class VideoDetailsActivity extends BaseActivity implements OnKeyListener {
	private Button replay, play, choose, colection;// 4个主操作按钮
	private TextView videoName, point, editors, actors; // keyName;
	private TextView introduce, area, type, year;
	private ImageView poster, sharpness, shadow;
	private RadioGroup sourcesLin;// 源标签组
	private VodRecode playRecode;//
	private VideoDetailInfo media;//
	// 以下三个子布局互斥，默认显示第一个
	private GridView hotGrid;// 推荐观看布局
	private LinearLayout keyLayoutT;// 电视剧选集软键布局
	private LinearLayout keyLayoutA;// 综艺选集软键布局

	private int selectSource; // 选择的 源
	private int index = 0;
	private int id;
	private VodDataHelper dbHelper;
	private ItvToast toast;

	private AlertDialog dialog;

	private final static int MSG_INIT = 11;
	private final static int MSG_ERROR = 12;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ERROR:
				// 有报错的可能性 提示为 activity 未运行
				if (getWindow() != null) {
					// dialog.setMessage((String) msg.obj);
					if (dialog!=null&& !dialog.isShowing()) {
						try {
							//这里有些机型会出现显示错误的异常 暂未解决
							dialog.show();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				break;
			case MSG_INIT:
				// long time1 = System.currentTimeMillis();
				media = (VideoDetailInfo) msg.obj;
				showDetailInfo();
				// long time2 = System.currentTimeMillis();
				// System.out.println("显示界面：" + (time2 - time1));
				break;
			}
		}
	};

	private void showDetailInfo() {
		/**
		 * 创建 源的 布局 创建 推荐布局 创建 3D图 动画 初始化 填充界面text
		 */

		if (media == null) {
			return;
		}
		if (media.banben.contains("更新")) {
			if (dbHelper.queryHasRecode(media.id, VodDataHelper.ZHUI)) {
				colection
						.setBackgroundResource(R.drawable.video_details_zhuiju_selector);
				colection.setText("取消");
			} else {
				colection
						.setBackgroundResource(R.drawable.video_details_zhuiju_n_selector);
				colection.setText("追剧");
			}
		} else {
			if (dbHelper.queryHasRecode(media.id, VodDataHelper.FAV)) {
				colection
						.setBackgroundResource(R.drawable.video_details_zhuiju_selector);
				colection.setText("取消");
			} else {
				colection
						.setBackgroundResource(R.drawable.video_details_zhuiju_n_selector);
				colection.setText("收藏");
			}
		}

		updateView();

		videoName.setText(media.title);
		editors.setText("导演：" + media.director);
		actors.setText("演员：" + media.actor);
		introduce.setText(media.info);
		area.setText("地区：" + media.area);
		type.setText("类别：" + media.cate);
		year.setText(media.year);
		setSharpNessLog(media.banben);
		progressDismiss();
		play.requestFocus();
	}

	private void updateView() {
		if(media!=null){
			playRecode = dbHelper.queryRecode(media.id, VodDataHelper.RECODE);
		}
		if (playRecode == null) {
			play.setText("播放");
			createSourceLayout(media.playlist, 0);
		} else {
			replay.setVisibility(View.VISIBLE);
			play.setText("续播");
			play.setBackgroundResource(R.drawable.details_replay_sel);
			createSourceLayout(media.playlist, playRecode.sourceIndex);
		}
		create3DPost(poster, media.img);
		createHotLayout(media.recommends);
		if (!media.type.equals("电影")) {
			choose.setVisibility(View.VISIBLE);
		}
		play.setVisibility(View.VISIBLE);
		colection.setVisibility(View.VISIBLE);
	}

	private void initDialog() {
		dialog = new AlertDialog.Builder(VideoDetailsActivity.this)
				.setTitle("温馨提示").setMessage("加载失败，稍后重试！").create();
		android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case AlertDialog.BUTTON_POSITIVE:
					VideoDetailsActivity.this.finish();
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					break;
				default:
					break;
				}
			}
		};
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确认", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "重试", listener);
	}

	LayoutInflater inflater;
	String hostUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// long time1 = System.currentTimeMillis();
		setContentView(R.layout.video_details);
		Process.setThreadPriority(Process.myTid(), -3);// UI线程-4，比UI低一级
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dbHelper = VodDataHelper.getInstance(this);
		// hostUrl = ServerBiz.getServer(this, ServerBiz.HOSTURL);
		toast = new ItvToast(this);
		toast.setDuration(Toast.LENGTH_LONG);
		hostUrl = MyApp.baseServer + "v_info/";
		initDialog();
		initView();
		// long time2 = System.currentTimeMillis();
		// System.out.println("初始页面：" + (time2 - time1));
		initData();
		initListener();
		progressShow();
	}

	protected void create3DPost(final ImageView imageView, String imgUrl) {
		BitmapWorkerTask task = new BitmapWorkerTask(this, null, false, false);
		task.setCallback(new PostCallBack() {

			@Override
			public void post(Bitmap bitmap) {
				if (bitmap != null) {
					imageView.setImageBitmap(Reflect3DImage.skewImage(bitmap,
							260, 366, 50));
				}
				Animation anim = AnimationUtils.loadAnimation(
						VideoDetailsActivity.this, R.anim.triangleeffect);
				shadow.startAnimation(anim);
				shadow.setVisibility(View.VISIBLE);
			
			}
		});
		task.execute(imgUrl);
	}

	/**
	 * 创建 推荐布局
	 */
	HotVideoAdapter adapter;

	protected void createHotLayout(ArrayList<VideoInfo> hot) {
		hotGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new HotVideoAdapter(VideoDetailsActivity.this, hot);
		hotGrid.setAdapter(adapter);
	}

	/**
	 * 创建 源布局
	 */
	protected void createSourceLayout(ArrayList<VideoSource> source,
			int sourceIndex) {
		if (source == null || source.isEmpty()) {
			play.setEnabled(false);
			return;
		}
		if (sourcesLin.getChildCount() > 0) {
			sourcesLin.removeAllViews();
		}
		for (int i = 0; i < source.size(); i++) {
			RadioButton rb = (RadioButton) inflater.inflate(
					R.layout.vediodetail_rb, null);
			rb.setCompoundDrawablesWithIntrinsicBounds(0, 0, StringUtil
					.sourceStringToResourceID(source.get(i).sourceName), 0);
			rb.setBackgroundResource(R.drawable.detailsource_bg_s);
			// rb.setFocusable(true);
			sourcesLin.addView(rb, i, new LayoutParams(-2, -2));
		}

		if (playRecode != null)
			sourceIndex = playRecode.sourceIndex;
		if (sourceIndex < 0) {
			sourceIndex = 0;
		} else if (sourceIndex >= sourcesLin.getChildCount()) {

		}

		sourcesLin.check(sourcesLin.getChildAt(sourceIndex).getId());
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		// 侧阴影
		shadow = (ImageView) findViewById(R.id.details_poster_shadow);
		shadow.setVisibility(View.GONE);
		// 海报
		poster = (ImageView) findViewById(R.id.details_poster);
		poster.setImageBitmap(Reflect3DImage.skewImage(BitmapFactory
				.decodeResource(getResources(), R.drawable.hao260x366), 260,
				366, 50));
		// 清晰度
		sharpness = (ImageView) findViewById(R.id.details_sharpness);

		// 影视名称
		videoName = (TextView) findViewById(R.id.details_name);
		// 评分
		point = (TextView) findViewById(R.id.details_rate);
		// 导演
		editors = (TextView) findViewById(R.id.details_director);
		// 所属地区
		area = (TextView) findViewById(R.id.details_playTimes);
		// 演员
		actors = (TextView) findViewById(R.id.details_actors);
		// 所属类别
		type = (TextView) findViewById(R.id.details_update);
		// 年份
		year = (TextView) findViewById(R.id.details_year);
		// 介绍
		introduce = (TextView) findViewById(R.id.details_video_introduce);
		// 视频源
		sourcesLin = (RadioGroup) findViewById(R.id.video_details_resources);
		// 重头播放
		replay = (Button) findViewById(R.id.details_replay);
		// 播放
		play = (Button) findViewById(R.id.details_play);
		play.setVisibility(View.GONE);
		// 选集
		choose = (Button) findViewById(R.id.details_choose);
		choose.setVisibility(View.GONE);
		// 收藏
		colection = (Button) findViewById(R.id.details_colection);
		colection.setVisibility(View.GONE);
		// 推荐观看
		hotGrid = (GridView) findViewById(R.id.details_recommend);
		// 电视剧选集布局
		keyLayoutT = (LinearLayout) findViewById(R.id.details_key_tv);
		// 综艺选集软键布局
		keyLayoutA = (LinearLayout) findViewById(R.id.details_key_arts);
		// 名称
		// keyName = (TextView) findViewById(R.id.details_key_name);
	}

	private void initData() {
		Intent intent = getIntent();
		id = intent.getIntExtra(ConstantUtil.VIDEODEAIL, 0);
		new Thread() {
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
				VideoDetailInfo info = null;
				try {
					info = VideoDetailBiz.parseDetailInfo(
							hostUrl,
							id,
							getPackageManager().getPackageInfo(
									VideoDetailsActivity.this.getPackageName(),
									0).versionCode);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				if (info != null) {
					handler.sendMessage(handler.obtainMessage(MSG_INIT, info));
				} else {
					handler.sendEmptyMessage(MSG_ERROR);
				}
			};
		}.start();
	}

	private void initListener() {
		// 重头播放
		replay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (media != null) {
					// myApp.playSound(ConstantUtil.COMFIRE);
					Intent intent = new Intent(VideoDetailsActivity.this,
							VodPlayer.class);
					Bundle data = new Bundle();
					data.putSerializable("media", media);
					// 此键出现，播放记录必不为空,只将集数和进度归0；
					playRecode.setIndex = 0;
					playRecode.positon = 0;
					data.putSerializable("playinfo", playRecode);
					Log.i("info", playRecode.toString());
					intent.putExtra(ConstantUtil.VODEXTRA, data);
					VideoDetailsActivity.this.startActivityForResult(intent, 0);
				}
			}
		});
		replay.setOnKeyListener(this);

		/* 播放按键的 监听 跳转 */
		play.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (media != null) {
					// myApp.playSound(ConstantUtil.COMFIRE);
					Intent intent = new Intent(VideoDetailsActivity.this,
							VodPlayer.class);
					Bundle data = new Bundle();
					data.putSerializable("media", media);
					if (playRecode != null) {
						if (playRecode.sourceIndex != selectSource) {
							playRecode.sourceIndex = selectSource;
							playRecode.setIndex = 0;
							playRecode.positon = 0;
						}
						// data.putSerializable("playinfo", playRecode);
					} else {
						playRecode = new VodRecode();
						playRecode.sourceIndex = selectSource;
						playRecode.id = media.id;
						playRecode.title = media.title;
						playRecode.banben = media.banben;
						playRecode.imgUrl = media.img;
						playRecode.type = VodDataHelper.RECODE;
						playRecode.setIndex = 0;
						playRecode.positon = 0;
						// playRecode.qxd=media.
					}
					data.putSerializable("playinfo", playRecode);
					Log.i("info", playRecode.toString());
					intent.putExtra(ConstantUtil.VODEXTRA, data);
					VideoDetailsActivity.this.startActivityForResult(intent, 0);
				}
			}
		});
		play.setOnKeyListener(this);

		/* sourcesLin */
		sourcesLin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				selectSource = sourcesLin.indexOfChild(sourcesLin
						.findViewById(checkedId));
				MyApp.playSound(ConstantUtil.COMFIRE);
				if (keyLayoutT.getVisibility() == View.VISIBLE
						|| keyLayoutA.getVisibility() == View.VISIBLE) {
					ArrayList<VideoSet> sets = media.playlist.get(selectSource).sets;
					String type = media.type;
					if (!type.contains("综艺")) {
						CreateTvLayout(sets);
						hotGrid.setVisibility(View.GONE);
						keyLayoutT.setVisibility(View.VISIBLE);
					} else {
						// 设置选集参数
						CreateArtLayout(sets);
						hotGrid.setVisibility(View.GONE);
						keyLayoutA.setVisibility(View.VISIBLE);
					}
				}
				if (playRecode != null
						&& selectSource == playRecode.sourceIndex) {
					play.setText("续播");
					play.setBackgroundResource(R.drawable.details_replay_sel);
					replay.setVisibility(View.VISIBLE);
				} else {
					play.setText("播放");
					play.setBackgroundResource(R.drawable.video_details_play_selector);
					replay.setVisibility(View.GONE);
				}
			}
		});
		sourcesLin.setOnKeyListener(this);

		/* 收藏 按键 监听 */
		colection.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (media.banben.contains("更新")) { // 追剧
					VodRecode recode = dbHelper.queryRecode(media.id,
							VodDataHelper.ZHUI);
					if (recode == null) {
						colection.setText("取消");
						recode = new VodRecode();
						recode.id = media.id;
						recode.title = media.title;
						recode.banben = media.banben;
						recode.imgUrl = media.img;
						recode.type = VodDataHelper.ZHUI;
						dbHelper.insertRecode(recode);
						colection
								.setBackgroundResource(R.drawable.video_details_zhuiju_selector);
						toast.setIcon(R.drawable.toast_smile);
						toast.setText("新增追剧成功！");
					} else {
						dbHelper.deleteRecodes(media.id, VodDataHelper.ZHUI);
						colection.setText("追剧");
						colection
								.setBackgroundResource(R.drawable.video_details_zhuiju_n_selector);
						toast.setIcon(R.drawable.toast_smile);
						toast.setText("取消追剧成功！");
					}
				} else { // 收藏
					VodRecode recode = dbHelper.queryRecode(media.id,
							VodDataHelper.FAV);
					if (recode == null) {
						colection.setText("取消");
						recode = new VodRecode();
						recode.id = media.id;
						recode.title = media.title;
						recode.banben = media.banben;
						recode.imgUrl = media.img;
						recode.type = VodDataHelper.FAV;
						dbHelper.insertRecode(recode);
						colection
								.setBackgroundResource(R.drawable.video_details_zhuiju_selector);
						toast.setIcon(R.drawable.toast_smile);
						toast.setText("新增收藏成功！");
					} else {
						dbHelper.deleteRecodes(media.id, VodDataHelper.FAV);
						colection.setText("收藏");
						colection
								.setBackgroundResource(R.drawable.video_details_zhuiju_n_selector);
						toast.setIcon(R.drawable.toast_smile);
						toast.setText("取消收藏成功！");
					}
				}
				toast.show();
			}
		});
		colection.setOnKeyListener(this);

		choose.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String type = media.type;
				// myApp.playSound(ConstantUtil.COMFIRE);
				if (keyLayoutT.getVisibility() == View.VISIBLE
						|| keyLayoutA.getVisibility() == View.VISIBLE) {
					keyLayoutA.setVisibility(View.GONE);
					keyLayoutT.setVisibility(View.GONE);
					hotGrid.setVisibility(View.VISIBLE);
				} else {
					ArrayList<VideoSet> sets = null;
					try {
						sets = media.playlist.get(selectSource).sets;
						//此处可能出现下标越界异常
					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
					}
					/**
					 * [VideoSet [setName=第1集, link=http://www.hunantv.com/v/3/104466/f/1067322.html, playUrls=null]]
					 */
					if (sets != null && !sets.isEmpty()) {
						if (type.contains("综艺")) {
							// 设置选集参数
							CreateArtLayout(sets);
							hotGrid.setVisibility(View.GONE);
							keyLayoutA.setVisibility(View.VISIBLE);
						} else {
							CreateTvLayout(sets);
							hotGrid.setVisibility(View.GONE);
							keyLayoutT.setVisibility(View.VISIBLE);
						}
					} else {
						toast.setIcon(R.drawable.toast_shut);
						toast.setText("Sorry,未找到可播放的剧集……");
						toast.show();
						toast.removeIcon();
						// Toast.makeText(VideoDetailsActivity.this,
						// "Sorry,未找到可播放的剧集……", Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		choose.setOnKeyListener(this);

		hotGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, adapter + "");
				VideoInfo info = (VideoInfo) adapter.getItem(position);
				Intent intent = new Intent(VideoDetailsActivity.this,
						VideoDetailsActivity.class);
				intent.putExtra(ConstantUtil.VIDEODEAIL, info.id);
				VideoDetailsActivity.this.startActivity(intent);
				overridePendingTransition(R.anim.slid2, R.anim.slid1);
				VideoDetailsActivity.this.finish();

			}
		});
		hotGrid.setOnKeyListener(this);

	}

	/**
	 * 电视剧、动漫选集视图
	 * 
	 * @param sets
	 *            集信息
	 */
	protected void CreateTvLayout(ArrayList<VideoSet> sets) {
		// 每10集一个标签
		final Gallery keyGallery = (Gallery) findViewById(R.id.details_key_gallery);
		// 集数编号
		final ViewPager keyPager = (ViewPager) findViewById(R.id.details_key_pager);

		DetailsKeyTabAdapter textAdapter = new DetailsKeyTabAdapter(this,
				countStrArr(sets, media.banben.contains("更新")));
		keyGallery.setAdapter(textAdapter);

		keyPager.setAdapter(new AllPagesAdapter(addViewToPager(sets,
				media.banben.contains("更新"))));
		keyGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				keyPager.setCurrentItem(position);
				MyApp.playSound(ConstantUtil.PAGE_CHANGE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		keyPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				keyGallery.setSelection(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		keyPager.setOnKeyListener(this);
	}

	private void CreateArtLayout(final ArrayList<VideoSet> sets) {
		System.out.println("集数" + sets.size());
		ListView keyList = (ListView) findViewById(R.id.details_key_list);
		GridView keyGrid = (GridView) findViewById(R.id.details_key_grid);
		// 设置item默认选择背景全透明
		keyGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		keyList.setSelector(new ColorDrawable(Color.TRANSPARENT));

		final DetailsKeyListAdapter listAdapter;
		DetailsKeyGridAdapter adapter = new DetailsKeyGridAdapter(this,
				countStrArr(sets, false));
		keyGrid.setAdapter(adapter);
		listAdapter = new DetailsKeyListAdapter(this, pagingSets(sets, index));
		keyList.setAdapter(listAdapter);

		keyGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				index = position;
				Log.i("info", "Click=" + position);
				listAdapter.setDataChanged(pagingSets(sets, index));
			}
		});
		keyGrid.setOnKeyListener(this);

		keyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				playRecode = new VodRecode();
				playRecode.id = media.id;
				playRecode.title = media.title;
				playRecode.banben = media.banben;
				playRecode.imgUrl = media.img;
				playRecode.type = VodDataHelper.RECODE;
				playRecode.sourceIndex = selectSource;
				playRecode.setIndex = index * 10 + position;
				playRecode.positon = 0;

				if (media != null) {
					Intent intent = new Intent(VideoDetailsActivity.this,
							VodPlayer.class);
					Bundle data = new Bundle();
					data.putSerializable("playinfo", playRecode);
					data.putSerializable("media", media);
					intent.putExtra(ConstantUtil.VODEXTRA, data);
					VideoDetailsActivity.this.startActivityForResult(intent, 0);
					// VideoDetailsActivity.this.finish();
				}
			}
		});
		keyList.setOnKeyListener(this);
	}

	private ArrayList<View> addViewToPager(ArrayList<VideoSet> sets,
			boolean containUodate) {
		int num = sets.size();
		System.out.println("集数" + num);
		LinearLayout line = new LinearLayout(this);
		ArrayList<View> pages = new ArrayList<View>();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 50);
		line.setLeft(40);
		line.setLayoutParams(params);
		// 不包含更新，顺序排列
		if (!containUodate) {
			int index = 1;
			while (index <= num) {
				line.addView(createSetBTN(index));
				if (index % 10 == 0) {
					pages.add(line);
					line = new LinearLayout(this);
					line.setLayoutParams(params);
					line.setLeft(40);
				} else {
					if (index == num) {
						pages.add(line);
					}
				}
				index++;
			}
		} else {
			int index = num;
			int count = 1;
			while (index >= 1) {
				line.addView(createSetBTN(index));
				if (count % 10 == 0) {
					pages.add(line);
					line = new LinearLayout(this);
					line.setLayoutParams(params);
					line.setLeft(40);
				} else {
					if (index == 1) {
						pages.add(line);
					}
				}
				index--;
				count++;
			}
		}
		return pages;
	}

	private Button createSetBTN(int index) {
		final Button btn = new Button(this);
		btn.setWidth(120);
		btn.setHeight(55);
		btn.setText("第" + index + "集");
		btn.setTextSize(18);
		btn.setTag(index - 1);
		btn.setBackgroundResource(R.drawable.video_details_btn_selector);
		// 跳转到播放器
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playRecode = new VodRecode();
				playRecode.id = media.id;
				playRecode.title = media.title;
				playRecode.banben = media.banben;
				playRecode.imgUrl = media.img;
				playRecode.type = VodDataHelper.RECODE;
				playRecode.sourceIndex = selectSource;
				playRecode.setIndex = (Integer) btn.getTag();
				playRecode.positon = 0;
				if (media != null) {
					Intent intent = new Intent(VideoDetailsActivity.this,
							VodPlayer.class);
					Bundle data = new Bundle();
					data.putSerializable("playinfo", playRecode);
					data.putSerializable("media", media);
					intent.putExtra(ConstantUtil.VODEXTRA, data);
					VideoDetailsActivity.this.startActivityForResult(intent, 0);
					// VideoDetailsActivity.this.finish();
				}
			}
		});
		btn.setOnKeyListener(this);
		btn.setTextColor(Color.LTGRAY);
		return btn;
	}

	/**
	 * 根据集数产生(整十级)标签字符串数组 连续剧、综艺通用
	 * 
	 * @param sets
	 *            集信息集合，亦可修改成sets.size()传入
	 * @return
	 */
	private List<String> countStrArr(ArrayList<VideoSet> sets,
			boolean containUpdate) {
		int num = sets.size();
		List<String> name = new ArrayList<String>();
		// 不包含更新，顺序排列
		if (!containUpdate) {
			int index = 1;
			StringBuilder sb = new StringBuilder();
			while (index <= num) {
				if (index % 10 == 1) {
					sb.append(index);
					sb.append('-');
				} else if (index % 10 == 0) {
					sb.append(index);
					name.add(sb.toString());
					sb = new StringBuilder();
				}
				if (index == num && index % 10 != 0) {
					sb.append(index);
					name.add(sb.toString());
				}
				index++;
			}
		} else {
			int index = num;
			int count = 1;
			StringBuilder sb = new StringBuilder();
			while (index >= 1) {
				if (count % 10 == 1) {
					sb.append(index);
					sb.append('-');
				} else if (count % 10 == 0) {
					sb.append(index);
					name.add(sb.toString());
					sb = new StringBuilder();
				}
				if (index == 1) {
					sb.append(index);
					name.add(sb.toString());
				}
				index--;
				count++;
			}
		}
		return name;
	}

	/**
	 * 综艺列表分页工具
	 * 
	 * @param setsA
	 *            综艺集信息（所有）
	 * @param pageIndex
	 *            页码编号，0开始
	 * @return listView需要显示的部分综艺集信息
	 */
	private List<VideoSet> pagingSets(ArrayList<VideoSet> setsA, int pageIndex) {
		Log.i("info", "Current Page=" + pageIndex);
		List<VideoSet> sets = new ArrayList<VideoSet>();
		boolean fullPage = (pageIndex + 1) * 10 <= setsA.size() ? true : false;
		if (fullPage) {
			sets = setsA.subList(pageIndex * 10, (pageIndex + 1) * 10);
		} else {
			sets = setsA.subList(pageIndex * 10, setsA.size());
		}
		for (VideoSet set : sets) {
			System.out.println(set.toString());
		}
		return sets;
	}

	private static final String TAG = "VideoDetailsActivity";

	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// this.finish();
	// overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
	// return super.onKeyDown(keyCode, event);
	// }
	// return false;
	// }

	/**
	 * 清晰度标记
	 * 
	 * @param sharp
	 */
	private void setSharpNessLog(String sharp) {
		if (sharp == null || sharp.equals("")) {
			return;
		} else if (sharp.contains("超清") || sharp.contains("SD")) {
			sharpness.setImageResource(R.drawable.video_details_superhd);
		} else if (sharp.contains("高清") || sharp.contains("HD")) {
			sharpness.setImageResource(R.drawable.video_details_hd);
		} else if (sharp.contains("DVD") || sharp.contains("流畅")
				|| sharp.contains("标清")) {
			sharpness.setImageResource(R.drawable.video_details_dvd);
		} else {
			point.setText(sharp);
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				this.finish();
				overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			}
			return super.onKeyDown(keyCode, event);
		} else {
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ConstantUtil.ACTIVITY_RESULT_OK) {
			updateView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}