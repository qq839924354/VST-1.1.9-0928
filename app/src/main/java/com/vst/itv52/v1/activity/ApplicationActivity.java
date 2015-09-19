package com.vst.itv52.v1.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.AppShowTopAdapter;
import com.vst.itv52.v1.adapters.ApplicationAdapter;
import com.vst.itv52.v1.biz.LancherBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.AppDB;
import com.vst.itv52.v1.model.AppBean;
import com.vst.itv52.v1.util.ConstantUtil;

public class ApplicationActivity extends Activity implements OnClickListener {
	private GridView appGrid;
	private ArrayList<AppBean> appBeans;
	private ApplicationAdapter adapter;
	private AppShowTopAdapter showTopAdapter;
	private ImageView whiteBorder;
	private int selectionPosition = 0;
	private Dialog contentDia, positionDia;
	private AppDB appDb;
	private HashMap<Integer, AppBean> topApps;
	private AppReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list);
		appDb = new AppDB(this);
		receiver = new AppReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(receiver, filter);
		initView();
		initListener();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void initView() {
		appGrid = (GridView) findViewById(R.id.app_grid_new);
		appGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));

		whiteBorder = (ImageView) findViewById(R.id.white_boder);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(160, 160);
		lp.leftMargin = 35;
		lp.topMargin = 100;
		whiteBorder.setLayoutParams(lp);
		appBeans = new LancherBiz(this).getLauncherApps();
		adapter = new ApplicationAdapter(this, appBeans);
		appGrid.setAdapter(adapter);
	}

	private void initListener() {
		appGrid.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				whiteBorder.setVisibility(View.VISIBLE);
				selectionPosition = position;
				flyWhiteBorder(160, 160, view.getX() + 35, view.getY() + 100);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				whiteBorder.setVisibility(View.INVISIBLE);
			}
		});

		appGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectionPosition = position;
				bootAnApp(position);
			}
		});

		appGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectionPosition = position;
				showContentDia();
				return false;
			}
		});
	}

	private void refrushView() {
		appBeans = new LancherBiz(this).getLauncherApps();
		adapter.changeData(appBeans);
	}

	private void flyWhiteBorder(int toWidth, int toHeight, float toX, float toY) {
		if (whiteBorder != null) {
			int width = whiteBorder.getWidth();
			int height = whiteBorder.getHeight();
			ViewPropertyAnimator animator = whiteBorder.animate();
			animator.setDuration(200);
			animator.scaleX((float) toWidth / (float) width);
			animator.scaleY((float) toHeight / (float) height);
			animator.x(toX);
			animator.y(toY);
			animator.start();
		}
	}

	private void showContentDia() {
		appGrid.clearFocus();
		if (contentDia == null) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.app_dia_contentmenu, null);
			Button open = (Button) view.findViewById(R.id.app_boot);
			open.setOnClickListener(this);
			Button top = (Button) view.findViewById(R.id.app_top);
			top.setOnClickListener(this);
			Button detail = (Button) view.findViewById(R.id.app_detail);
			detail.setOnClickListener(this);
			Button delete = (Button) view.findViewById(R.id.app_delete);
			delete.setOnClickListener(this);

			contentDia = new Dialog(this);
			contentDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
			contentDia.setContentView(view);
			Window dialogWindow = contentDia.getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			dialogWindow.setGravity(Gravity.CENTER);
			lp.width = 250;
			lp.height = 180;
			dialogWindow.setAttributes(lp);
		}
		contentDia.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.app_boot:
			bootAnApp(selectionPosition);
			break;
		case R.id.app_top:
			showOnTop();
			break;
		case R.id.app_detail:
			showAppDetail(selectionPosition);
			break;
		case R.id.app_delete:
			unInstallApp(selectionPosition);
			break;
		}
		contentDia.dismiss();
		appGrid.requestFocus();
	}

	private void unInstallApp(int position) {
		Uri packageURI = Uri.parse("package:"
				+ appBeans.get(position).getPackageName());
		Intent unIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		startActivity(unIntent);

	}

	private void bootAnApp(int position) {
		if (appBeans.get(position) != null) {
			PackageManager pm = this.getPackageManager();
			String packageName = appBeans.get(position).getPackageName();
			Intent intent = pm.getLaunchIntentForPackage(packageName);
			try {
				this.startActivity(intent);
				overridePendingTransition(R.anim.zoout, R.anim.zoin);
			} catch (Exception e) {
				e.printStackTrace();
				ItvToast toast = new ItvToast(this);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setText(R.string.toast_app_boot_err);
				toast.show();
				appBeans.remove(position);
				adapter.changeData(appBeans);
			}
		}
	}

	private void showAppDetail(int position) {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", appBeans.get(position)
				.getPackageName(), null);
		intent.setData(uri);
		startActivity(intent);
	}

	private void showOnTop() {
		if (positionDia == null) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.app_dia_totop, null);
			GridView positionGrid = (GridView) view
					.findViewById(R.id.top_app_position_grid);
			positionGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
			topApps = appDb.queryTopApps();
			showTopAdapter = new AppShowTopAdapter(topApps, this);
			positionGrid.setAdapter(showTopAdapter);

			positionGrid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position2, long id) {
					AppBean appBean = appBeans.get(selectionPosition);
					if (topApps != null && topApps.get(position2) != null) {
						appDb.removeRecode(topApps.remove(position2));
					}
					appDb.recodeApp(appBean, position2);
					topApps.put(position2, appBean);
					showTopAdapter.changData(topApps);
					positionDia.dismiss();
					sendBroadcast(new Intent(ConstantUtil.APP_DB_CHANGE));
				}
			});
			positionDia = new Dialog(this);
			positionDia.requestWindowFeature(Window.FEATURE_NO_TITLE);
			positionDia.setContentView(view);
			Window dialogWindow = positionDia.getWindow();
			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
			dialogWindow.setGravity(Gravity.CENTER);
			lp.width = 350;
			lp.height = 250;
			dialogWindow.setAttributes(lp);
		}
		positionDia.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "临时过滤系统应用");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			appBeans = new LancherBiz(this).getUserApps();
			adapter.changeData(appBeans);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class AppReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("info", action);
			String packageName = intent.getDataString().substring(8);
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				refrushView();
			} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
				appDb.deleteApp(packageName);
				refrushView();
				sendBroadcast(new Intent(ConstantUtil.APP_DB_CHANGE));
			}
		}

	}
}
