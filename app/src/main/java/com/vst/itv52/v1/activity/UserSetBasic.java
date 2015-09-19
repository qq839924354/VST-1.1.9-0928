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

public class UserSetBasic extends BaseActivity implements
		OnFocusChangeListener, OnClickListener {
	private TextView stepName;
	private TextView intruduce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_set_main);
		((ViewStub) findViewById(R.id.user_set_step1_info)).inflate();
		initBasicView();
	}

	private void initBasicView() {
		stepName = (TextView) findViewById(R.id.user_set_stepname);
		intruduce = (TextView) findViewById(R.id.user_set_main_step0_tv);
		stepName.setText("基本设置");

		Button personInfo, datumChange, pwdChange;
		personInfo = (Button) findViewById(R.id.user_set_info_personal);
		datumChange = (Button) findViewById(R.id.user_set_info_datum);
		pwdChange = (Button) findViewById(R.id.user_set_info_pwd);

		personInfo.setOnFocusChangeListener(this);
		datumChange.setOnFocusChangeListener(this);
		pwdChange.setOnFocusChangeListener(this);

		personInfo.setOnClickListener(this);
		datumChange.setOnClickListener(this);
		pwdChange.setOnClickListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.user_set_info_personal:
			if (hasFocus)
				intruduce.setText(R.string.user_info_hint);
			break;
		case R.id.user_set_info_datum:
			if (hasFocus)
				intruduce.setText(R.string.user_datum_hint);
			break;
		case R.id.user_set_info_pwd:
			if (hasFocus)
				intruduce.setText(R.string.user_pwd_hint);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		MyApp.playSound(ConstantUtil.COMFIRE);
		switch (v.getId()) {
		case R.id.user_set_info_personal:
			intent = new Intent(UserSetBasic.this, UserSetPersonalInfo.class);
			break;
		case R.id.user_set_info_datum:
			intent = new Intent(UserSetBasic.this, UserSetDatumChange.class);
			break;
		case R.id.user_set_info_pwd:
			intent = new Intent(UserSetBasic.this, UserSetPwdChange.class);
			break;
		}
		startActivity(intent);
		overridePendingTransition(R.anim.fade1, R.anim.fade2);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
		return super.onKeyDown(keyCode, event);
	}

}
