package com.vst.itv52.v1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.util.ConstantUtil;

public class UserSetMain extends BaseActivity implements OnFocusChangeListener,
		OnClickListener {
	private TextView stepName;
	private TextView intruduce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_set_main);
		((ViewStub) findViewById(R.id.user_set_main_step0)).inflate();
		initMainSetView();
	}

	private void initMainSetView() {
		stepName = (TextView) findViewById(R.id.user_set_stepname);
		intruduce = (TextView) findViewById(R.id.user_set_main_step0_tv);
		stepName.setText("管理主界面");

		Button info, useTips, exit;
		info = (Button) findViewById(R.id.user_info);
//		tvManger = (Button) findViewById(R.id.user_tvmg);
//		shareResource = (Button) findViewById(R.id.user_share);
		useTips = (Button) findViewById(R.id.user_inturduce);
		exit = (Button) findViewById(R.id.user_exit);

		info.setOnFocusChangeListener(this);
//		tvManger.setOnFocusChangeListener(this);
//		shareResource.setOnFocusChangeListener(this);
		useTips.setOnFocusChangeListener(this);
		exit.setOnFocusChangeListener(this);

		info.setOnClickListener(this);
//		tvManger.setOnClickListener(this);
//		shareResource.setOnClickListener(this);
		useTips.setOnClickListener(this);
		exit.setOnClickListener(this);
	}

	// private void initData() {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// userInfoData = parseUserData(
	// ConfigUtil.getValue("USER_INFO_LINK"),
	// share.getString("login_key", ""));
	// if (userInfoData != null) {
	// Log.i("info", userInfoData.toString());
	// handler.sendEmptyMessage(USERINFOGETED);
	// }
	// }
	// }).start();
	// }

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.user_info:
			if (hasFocus)
				intruduce.setText(R.string.user_basic_hint);
			break;
//		case R.id.user_tvmg:
//			if (hasFocus)
//				intruduce.setText(R.string.user_tvmg_hint);
//			break;
//		case R.id.user_share:
//			if (hasFocus)
//				intruduce.setText(R.string.user_share_hint);
//			break;
		case R.id.user_inturduce:
			if (hasFocus)
				intruduce.setText(R.string.user_usetips_hint);
			break;
		case R.id.user_exit:
			if (hasFocus)
				intruduce.setText(R.string.user_exit_hint);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		MyApp.playSound(ConstantUtil.COMFIRE);
		switch (v.getId()) {
		case R.id.user_info:
			intent = new Intent(UserSetMain.this, UserSetBasic.class);
			startActivity(intent);
			overridePendingTransition(R.anim.fade1, R.anim.fade2);
			break;
//		case R.id.user_tvmg:
//			// intent = new Intent(UserSetMain.this, UserTvManenger.class);
//			// startActivity(intent);
//			// overridePendingTransition(R.anim.fade1, R.anim.fade2);
//			break;
//		case R.id.user_share:
//
//			break;
		case R.id.user_inturduce:

			break;
		case R.id.user_exit:
			MyApp.setLoginKey(null);
			finish();
			overridePendingTransition(R.anim.zoout, R.anim.zoin);
			break;
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
}
