package com.vst.itv52.v1.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.db.AppDB;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.db.VSTDBHelper;
import com.vst.itv52.v1.db.VodDataHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.ImageFileCache;

public class SettingClear extends BaseActivity implements OnClickListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private TextView picHint;
	private TextView colHint;
	private TextView recodHint;
	private TextView channelHint;
	private TextView appHint;
	private ImageFileCache imageCache;
	private VodDataHelper vodHelper;
	private LiveDataHelper liveHelper;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x123:
				picHint.setText("正在删除图片，请稍候……");
				break;
			case 0x234:
				picHint.setText("图片删除完成！");
				break;
			}
		}
	};

	/* 频道设置 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_clearcache)).inflate();

		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/* 主体视图 */
	private void initView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);

		setName1.setText("清除记录");
		setName2.setText("清除记录");
		setItemLog.setImageResource(R.drawable.claer_setup);

		initClearView();
	}

	/* 清理缓存视图 */
	private void initClearView() {
		imageCache = new ImageFileCache();
		vodHelper = VodDataHelper.getInstance(this);
		liveHelper = LiveDataHelper.getInstance(this);

		picHint = (TextView) findViewById(R.id.setting_clear_pic_hint);
		picHint.setText("图片占用 "
				+ imageCache.getDirSize(imageCache.getDirectory()) / 1024
				/ 1024 + " M/SD卡可用空间 " + imageCache.getFreeSDcard() + " M");
		colHint = (TextView) findViewById(R.id.setting_clear_colection_hint);
		colHint.setText("共 "
				+ (vodHelper.queryRecodeCount(VodDataHelper.FAV) + vodHelper
						.queryRecodeCount(VodDataHelper.ZHUI)) + " 部");
		recodHint = (TextView) findViewById(R.id.setting_clear_cache_hint);
		recodHint.setText("共 "
				+ vodHelper.queryRecodeCount(VodDataHelper.RECODE) + " 条记录");
		channelHint = (TextView) findViewById(R.id.setting_clear_channel_tv);
		ArrayList<LiveChannelInfo> infos = liveHelper
				.getChannelListByTid(VSTDBHelper.CUSTOM_TID);
		int customCount = infos == null ? 0 : infos.size();
		channelHint.setText("共 " + customCount + " 个频道");
		appHint = (TextView) findViewById(R.id.setting_clear_app_tv);

		Button clearPic = (Button) findViewById(R.id.setting_clear_pic_btn);
		Button clearCol = (Button) findViewById(R.id.setting_clear_colection_btn);
		Button clearRec = (Button) findViewById(R.id.setting_clear_cache_btn);
		Button clearAll = (Button) findViewById(R.id.setting_clear_all_btn);
		Button clearChan = (Button) findViewById(R.id.setting_clear_channed_btn);
		Button clearApp = (Button) findViewById(R.id.setting_clear_topapp_btn);

		clearPic.setOnClickListener(this);
		clearCol.setOnClickListener(this);
		clearRec.setOnClickListener(this);
		clearAll.setOnClickListener(this);
		clearChan.setOnClickListener(this);
		clearApp.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_clear_pic_btn:
			deletImageCache();
			break;
		case R.id.setting_clear_colection_btn:
			// helper.deletRecodeFav();
			vodHelper.deleteRecodes(VodDataHelper.FAV);
			vodHelper.deleteRecodes(VodDataHelper.ZHUI);
			colHint.setText("收藏记录已清除！");
			break;
		case R.id.setting_clear_cache_btn:
			vodHelper.deleteRecodes(VodDataHelper.RECODE);
			recodHint.setText("播放记录已清除！");
			break;
		case R.id.setting_clear_channed_btn:
			liveHelper.deleteChannels(VSTDBHelper.CUSTOM_TID);
			channelHint.setText("自定义频道已清除！");
			break;
		case R.id.setting_clear_topapp_btn:
			new AppDB(this).deletAppinfoss();
			sendBroadcast(new Intent(ConstantUtil.APP_DB_CHANGE));
			appHint.setText("首页应用已清除！");
			break;
		case R.id.setting_clear_all_btn:
			Builder builder = new Builder(this);
			builder.setTitle("删除所有记录数据？")
					.setMessage("此操作将一次性删除以上所有数据(不包括首页应用)！")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									clearAll();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
			break;
		}
	}

	private void clearAll() {
		deletImageCache();
		vodHelper.deleteRecodes(VodDataHelper.FAV);
		vodHelper.deleteRecodes(VodDataHelper.ZHUI);
		colHint.setText("收藏记录已清除！");
		vodHelper.deleteRecodes(VodDataHelper.RECODE);
		recodHint.setText("播放记录已清除！");
		liveHelper.deleteChannels(VSTDBHelper.CUSTOM_TID);
		channelHint.setText("自定义频道已清除！");
	}

	/**
	 * 清楚图片缓存
	 */
	private void deletImageCache() {
		File dir = new File(imageCache.getDirectory());
		final File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return;
		}
		new Thread() {
			public void run() {
				int i = 0;
				handler.sendEmptyMessage(0x123);
				while (i < files.length) {
					Log.i("info", "删除图片" + files[i].getName());
					files[i].delete();
					i++;
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(0x234);
			};
		}.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

}
