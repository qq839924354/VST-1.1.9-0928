package com.vst.itv52.v1.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.ApplicationActivity;
import com.vst.itv52.v1.adapters.AppChooseListAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.LancherBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.AppDB;
import com.vst.itv52.v1.effect.ImageReflect;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.AppBean;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.FileUtils;

public class ApplicationLayout extends LinearLayout implements IVstHomeView,
		OnFocusChangeListener, OnClickListener, OnKeyListener {
	private Context context;
	private ScaleAnimEffect animEffect;
	private HashMap<Integer, AppBean> topApps;
	private AppDB appDb;
	private Dialog chooseDia;
	private int position = -1;// 焦点、点击的位置
	private int randomTemp = -1;// 随机数缓存，用于避免连续产生2个相同的随机�?
	private RelativeLayout fatherView;
	private AppDbDataChangeReceiver receiver;
	private FrameLayout[] fls;
	private ImageView[] bgs;
	private ImageView[] poster;
	private TextView[] appName;
	private ImageView[] refImg = new ImageView[5];
	private ImageView whiteBorder;
	private TextView popName;
	private ListView popList;

	public ApplicationLayout(Context context) {
		super(context);
		this.context = context;
		animEffect = new ScaleAnimEffect();
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		View view = LayoutInflater.from(context).inflate(
				R.layout.application_layout, null);
		appDb = new AppDB(context);
		receiver = new AppDbDataChangeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConstantUtil.APP_DB_CHANGE);
		context.registerReceiver(receiver, filter);
		topApps = appDb.queryTopApps();
		fatherView = (RelativeLayout) view.findViewById(R.id.fatherview);
		addView(view);
	}

	public void initView() {
		int[] refIds = new int[] { R.id.app_refimg_0, R.id.app_refimg_1,
				R.id.app_refimg_2, R.id.app_refimg_3, R.id.app_refimg_4 };
		for (int i = 0; i < refImg.length; i++) {
			refImg[i] = (ImageView) findViewById(refIds[i]);
		}
		createContentView();

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(220, 220);
		lp.leftMargin = 120;
		lp.topMargin = 40;
		whiteBorder.setLayoutParams(lp);
	}

	private int[] bgSelector = { R.drawable.blue_no_shadow_a,
			R.drawable.dark_no_shadow_a, R.drawable.green_no_shadow_a,
			R.drawable.orange_no_shadow_a, R.drawable.pink_no_shadow_a,
			R.drawable.yellow_no_shadow_a, R.drawable.red_no_shadow_a };
	private int bgSize = bgSelector.length;

	private int[] flIds = new int[] { R.id.app_fl_0, R.id.app_fl_1,
			R.id.app_fl_2, R.id.app_fl_3, R.id.app_fl_4, R.id.app_fl_5,
			R.id.app_fl_6, R.id.app_fl_7, R.id.app_fl_8, R.id.app_fl_9 };
	private int[] bgIds = new int[] { R.id.app_bg_0, R.id.app_bg_1,
			R.id.app_bg_2, R.id.app_bg_3, R.id.app_bg_4, R.id.app_bg_5,
			R.id.app_bg_6, R.id.app_bg_7, R.id.app_bg_8, R.id.app_bg_9 };
	private int[] postIds = new int[] { R.id.app_poster_0, R.id.app_poster_1,
			R.id.app_poster_2, R.id.app_poster_3, R.id.app_poster_4,
			R.id.app_poster_5, R.id.app_poster_6, R.id.app_poster_7,
			R.id.app_poster_8, R.id.app_poster_9 };
	private int[] nameIds = new int[] { R.id.app_name_0, R.id.app_name_1,
			R.id.app_name_2, R.id.app_name_3, R.id.app_name_4, R.id.app_name_5,
			R.id.app_name_6, R.id.app_name_7, R.id.app_name_8, R.id.app_name_9 };

	private void createContentView() {
		RelativeLayout contentView = null;
		fls = new FrameLayout[10];
		bgs = new ImageView[10];
		poster = new ImageView[10];
		appName = new TextView[10];
		contentView = fatherView;
		for (int i = 0; i < 10; i++) {
			fls[i] = (FrameLayout) contentView.findViewById(flIds[i]);
			bgs[i] = (ImageView) contentView.findViewById(bgIds[i]);
			bgs[i].setVisibility(View.GONE);
			poster[i] = (ImageView) contentView.findViewById(postIds[i]);
			poster[i].setTag(i);
			poster[i].setOnFocusChangeListener(this);
			poster[i].setOnClickListener(this);
			poster[i].setOnKeyListener(this);
			poster[i].setBackgroundResource(bgSelector[createRandom(bgSize)]);
			appName[i] = (TextView) contentView.findViewById(nameIds[i]);
			if (topApps != null && topApps.get(i) != null && i > 0 && i < 9) {
				poster[i].setImageDrawable(topApps.get(i).getIcon());
				appName[i].setText(topApps.get(i).getName());
			} else {
				if (i == 0) {
					poster[i].setImageResource(R.drawable.aijia);
					appName[i].setText("爱家市场");
				} else if (i >= 9) {
					poster[i].setImageResource(R.drawable.all_apps);
					appName[i].setText("所有应用");
				} else {
					poster[i].setImageResource(R.drawable.add_apps);
					appName[i].setText("（未添加）");
				}
			}
			if (i - 5 >= 0) {
				refImg[i - 5].setImageBitmap(ImageReflect
						.createCutReflectedImage(
								ImageReflect.convertViewToBitmap(fls[i]), 0));
			}
		}
	}

	private int createRandom(int size) {
		Random random = new Random();
		int randomIndex = random.nextInt(size);
		// 如果本次随机与上次一样，重新随机
		while (randomIndex == randomTemp) {
			randomIndex = random.nextInt(size);
		}
		randomTemp = randomIndex;
		return randomIndex;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (fatherView.findFocus() != null && position != 9) {
				if (topApps == null || topApps.get(position) == null) {
					showAppChoosePop("添加应用");
				} else {
					showAppChoosePop("更换应用");
				}
			} else {
				ItvToast toast = new ItvToast(context);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setIcon(R.drawable.toast_shut);
				toast.setText(R.string.toast_app_choose_err);
				toast.show();
			}
		}
		return false;
	}

	private void showAppChoosePop(String popNameStr) {
		if (chooseDia == null) {
			View view = LayoutInflater.from(context).inflate(
					R.layout.app_dia_choose_list, null);
			popName = (TextView) view.findViewById(R.id.app_choose_pop_name);
			popList = (ListView) view.findViewById(R.id.app_choose_pop_list);
			popList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position2, long id) {
					AppBean appBean = (AppBean) popList.getAdapter().getItem(
							position2);
					if (topApps != null && topApps.get(position) != null) {
						appDb.removeRecode(topApps.remove(position));
					}
					appDb.recodeApp(appBean, position);
					chooseDia.dismiss();
					context.sendBroadcast(new Intent(ConstantUtil.APP_DB_CHANGE));
				}
			});
			chooseDia = new Dialog(context);
			chooseDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
			chooseDia.setContentView(view);
			Window dialogWindow = chooseDia.getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			dialogWindow.setGravity(Gravity.CENTER);
			lp.width = 300;
			lp.height = 450;
			dialogWindow.setAttributes(lp);
		}
		popName.setText(popNameStr);
		popList.setAdapter(new AppChooseListAdapter(context, new LancherBiz(
				context).getLauncherApps()));
		chooseDia.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_poster_0:
			position = 0;
			bootAnApp(position);
			break;
		case R.id.app_poster_1:
			position = 1;
			bootAnApp(position);
			break;
		case R.id.app_poster_2:
			position = 2;
			bootAnApp(position);
			break;
		case R.id.app_poster_3:
			position = 3;
			bootAnApp(position);
			break;
		case R.id.app_poster_4:
			position = 4;
			bootAnApp(position);
			break;
		case R.id.app_poster_5:
			position = 5;
			bootAnApp(position);
			break;
		case R.id.app_poster_6:
			position = 6;
			bootAnApp(position);
			break;
		case R.id.app_poster_7:
			position = 7;
			bootAnApp(position);
			break;
		case R.id.app_poster_8:
			position = 8;
			bootAnApp(position);
			break;
		case R.id.app_poster_9:
			Intent intent = new Intent(context, ApplicationActivity.class);
			context.startActivity(intent);
			((Activity) context).overridePendingTransition(R.anim.zoout,
					R.anim.zoin);
			break;
		}
	}

	private void bootAnApp(int position) {
		PackageManager pm = context.getPackageManager();
		if (position == 0) {
			String packageName = "tv.tv9ikan.app";
			Intent intent = pm.getLaunchIntentForPackage(packageName);
			try {
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(R.anim.zoout,
						R.anim.zoin);
			} catch (Exception e) {
				e.printStackTrace();
				showDownApkDia(position);
			}
		} else if (position > 0 && topApps.get(position) != null) {
			String packageName = topApps.get(position).getPackageName();
			Intent intent = pm.getLaunchIntentForPackage(packageName);
			try {
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(R.anim.zoout,
						R.anim.zoin);
			} catch (Exception e) {
				e.printStackTrace();
				ItvToast toast = new ItvToast(context);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setIcon(R.drawable.toast_err);
				toast.setText(R.string.toast_app_boot_err);
				toast.show();
				appDb.removeRecode(topApps.remove(position));
				createContentView();// 重新生成视图

			}
		} else {
			ItvToast toast = new ItvToast(context);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setText(R.string.toast_app_boot_no);
			toast.show();
		}
	}

	/**
	 * 下载apk
	 * 
	 * @param position
	 */
	private void showDownApkDia(final int position) {
		final ProgressDialog proDia = new ProgressDialog(context);
		proDia.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		proDia.setTitle("未找到指定应用");
		proDia.setMessage("正在重新下载，请稍候……");
		proDia.setIndeterminate(false);
		proDia.setCancelable(true);
		// "http://up.52itv.cn/Installer/apk/"

		final String baseDownUrl = "http://ups.stdvr.com/mygica/vst/TVBOX/";
		final File apk = new File(context.getCacheDir(), "aijiashichang.apk");
		new Thread() {
			@Override
			public void run() {
				try {
					String apkPath = null;
					URLConnection conn = new URL(baseDownUrl
							+ "aijiashichang.apk").openConnection();
					conn.connect();
					InputStream is = conn.getInputStream();
					int apkSize = conn.getContentLength();
					if (is == null || apkSize <= 0) {

					} else {
						FileOutputStream fos = new FileOutputStream(apk);
						byte[] buf = new byte[4096]; // 4k
						int count = -1;
						int loadedLength = 0;
						while ((count = is.read(buf)) != -1) {
							fos.write(buf, 0, count);
							loadedLength += count;
							proDia.setProgress((int) (((float) loadedLength / (float) apkSize) * 100.0));
						}
						is.close();
						fos.close();
						apkPath = apk.getAbsolutePath();
					}
					if (apkPath != null) {
						FileUtils.modifyFile(new File(apkPath));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setDataAndType(Uri.parse("file://" + apkPath),
								"application/vnd.android.package-archive");
						context.getApplicationContext().startActivity(intent);
						proDia.dismiss();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		proDia.setButton("后台下载", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				proDia.dismiss();
			}
		});
		proDia.show();
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.app_poster_0:
			position = 0;
			break;
		case R.id.app_poster_1:
			position = 1;
			break;
		case R.id.app_poster_2:
			position = 2;
			break;
		case R.id.app_poster_3:
			position = 3;
			break;
		case R.id.app_poster_4:
			position = 4;
			break;
		case R.id.app_poster_5:
			position = 5;
			break;
		case R.id.app_poster_6:
			position = 6;
			break;
		case R.id.app_poster_7:
			position = 7;
			break;
		case R.id.app_poster_8:
			position = 8;
			break;
		case R.id.app_poster_9:
			position = 9;
			break;
		}
		if (hasFocus) {
			showOnFocusAnimation(position);
			// whiteBorder.setVisibility(View.VISIBLE);
			flyWhiteBorder(220, 220, 119 + position % 5 * 205,
					40 + position / 5 * 205);
		} else {
			showLooseFocusAinimation(position);
			whiteBorder.setVisibility(View.INVISIBLE);
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
				bgs[position].startAnimation(animEffect.alphaAnimation(0, 1,
						150, 0));
				bgs[position].setVisibility(View.VISIBLE);
			}
		});
		poster[position].startAnimation(anim1);
	}

	private void showLooseFocusAinimation(final int position) {
		animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
		poster[position].startAnimation(animEffect.createAnimation());
		bgs[position].setVisibility(View.GONE);
	}

	@Override
	public void updateData() {

	}

	@Override
	public void destroy() {
		for (int i = 0; i < fls.length; i++) {
			fls[i] = null;
		}
		bgIds = null;
		flIds = null;
		nameIds = null;
		postIds = null;
		context.unregisterReceiver(receiver);
	}

	@Override
	public void initListener() {

	}

	private class AppDbDataChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConstantUtil.APP_DB_CHANGE)) {
				topApps = appDb.queryTopApps();
				createContentView();
			}
		}
	}
}
