package com.vst.itv52.v1.activity;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.VodRecodeAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.VodDataHelper;
import com.vst.itv52.v1.model.VodRecode;
import com.vst.itv52.v1.util.ConstantUtil;

public class FavVideoActivity extends BaseActivity implements Observer,
		OnClickListener {
	private TextView typeName, empty;// 类型名称
	private GridView grid;// 详情列表
	private ArrayList<VodRecode> favorites;
	private VodDataHelper helper;
	private VodRecodeAdapter adapter;
	private Button chooseMult, chooseAll, chooseDel, chooseExit;
	private View choose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_grid_layout);
		helper = VodDataHelper.getInstance(this);
		helper.addObserver(this);
		initView();
		initData();

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	private void initView() {
		typeName = (TextView) findViewById(R.id.favorite_type);
		empty = (TextView) findViewById(R.id.fav_empty);
		choose = findViewById(R.id.fav_choose);
		chooseAll = (Button) findViewById(R.id.fav_choose_all);
		chooseDel = (Button) findViewById(R.id.fav_choose_del);
		chooseExit = (Button) findViewById(R.id.fav_choose_exit);
		chooseMult = (Button) findViewById(R.id.fav_choose_mult);
		grid = (GridView) findViewById(R.id.favorite_grid);
		// 设置item默认选择背景全透明
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		grid.requestFocus();
		initListener();
	}
	private int flag = 0;
	private void initData() {
		if (flag == 0) {
			Intent intent = getIntent();
			flag = intent.getIntExtra("favType", -1);
		}
		switch (flag) {
		case VodDataHelper.ZHUI:
			typeName.setText("我的追剧");
			empty.setText("您还没有追剧哦，快去看看有最近更新的电视剧有没有喜欢的吧！");
			break;
		case VodDataHelper.FAV:
			typeName.setText("我的收藏");
			empty.setText("您还没有收藏哦，“影视分类”里有海量的电影、电视剧，快去收藏吧！");
			break;
		case VodDataHelper.RECODE:
			typeName.setText("播放历史");
			empty.setText("您还没有播放记录啊，海量的电影、电视剧每天更新，更有大片抢先看，快去欣赏吧！");
			break;
		default:
			break;
		}
		favorites = helper.queryRecodes(flag);
		if (adapter == null) {
			adapter = new VodRecodeAdapter(this, favorites);
		} else {
			adapter.setSlectPositions(null);
			adapter.changeData(favorites);
		}
		if (favorites == null || favorites.isEmpty()) {
			empty.setVisibility(View.VISIBLE);
			grid.setVisibility(View.GONE);
			chooseMult.setVisibility(View.GONE);
		} else {
			grid.setAdapter(adapter);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initListener() {
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!adapter.getChoiceMode()) {
					if (MyApp.isOnline) {
						MyApp.playSound(ConstantUtil.COMFIRE);
						Intent intent = new Intent(FavVideoActivity.this,
								VideoDetailsActivity.class);
						int videoid = favorites.get(position).id;
						intent.putExtra(ConstantUtil.VIDEODEAIL, videoid);
						startActivity(intent);
					} else {
						ItvToast toast = new ItvToast(FavVideoActivity.this);
						toast.setDuration(Toast.LENGTH_LONG);
						toast.setIcon(R.drawable.toast_err);
						toast.setText(R.string.toast_net_disconnect_hint);
						toast.show();
					}
				} else {
					adapter.changData(position);
				}
			}
		});
		grid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.setChoiceMode(true);
				chooseMult.setVisibility(View.GONE);
				choose.setVisibility(View.VISIBLE);
				return false;
			}
		});
		chooseAll.setOnClickListener(this);
		chooseDel.setOnClickListener(this);
		chooseExit.setOnClickListener(this);
		chooseMult.setOnClickListener(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		// favorites = helper.queryRecodes(flag);
		// if (favorites == null || favorites.isEmpty()) {
		// empty.setVisibility(View.VISIBLE);
		// grid.setVisibility(View.GONE);
		// chooseMult.setVisibility(View.GONE);
		// } else {
		// empty.setVisibility(View.GONE);
		// grid.setVisibility(View.VISIBLE);
		// chooseMult.setVisibility(View.VISIBLE);
		// adapter.changeData(favorites);
		// }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fav_choose_all:
			adapter.chooseAll();
			break;
		case R.id.fav_choose_del:
			deleteRecode();
			break;
		case R.id.fav_choose_exit:
			adapter.setChoiceMode(false);
			chooseMult.setVisibility(View.VISIBLE);
			choose.setVisibility(View.GONE);
			break;
		case R.id.fav_choose_mult:
			adapter.setChoiceMode(true);
			chooseMult.setVisibility(View.GONE);
			choose.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void deleteRecode() {
		ArrayList<Integer> positions = adapter.getSlectPositions();
		for (int i = 0; i < favorites.size(); i++) {
			if (positions.contains(i)) {
				helper.deleteRecodes(favorites.get(i).id, flag);
				// favorites.remove(i);
				positions.remove((Integer) i);
			}
		}
		initData();
	}
}
