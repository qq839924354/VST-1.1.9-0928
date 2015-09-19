package com.vst.itv52.v1.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class UserSetPwdChange extends BaseActivity implements
		OnFocusChangeListener, OnClickListener {
	private TextView stepName;
	private TextView intruduce;
	private SharedPreferences share;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_set_main);
		((ViewStub) findViewById(R.id.user_set_step2_info_pwd)).inflate();
		share = getSharedPreferences("settingSPF", MODE_PRIVATE);
		initPwdChangeView();
	}

	private void initPwdChangeView() {
		stepName = (TextView) findViewById(R.id.user_set_stepname);
		intruduce = (TextView) findViewById(R.id.user_set_main_step0_tv);
		stepName.setText("修改密码");

		Button lastPwd, newPwd1, newPwd2;
		lastPwd = (Button) findViewById(R.id.user_set_info_pwd_last);
		newPwd1 = (Button) findViewById(R.id.user_set_info_pwd_new1);
		newPwd2 = (Button) findViewById(R.id.user_set_info_pwd_new2);

		lastPwd.setOnFocusChangeListener(this);
		newPwd1.setOnFocusChangeListener(this);
		newPwd2.setOnFocusChangeListener(this);

		lastPwd.setOnClickListener(this);
		newPwd1.setOnClickListener(this);
		newPwd2.setOnClickListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.user_set_info_pwd_last:
			if (hasFocus) {
				String last = share.getString("PWD_last", "");
				if (last.equals("")) {
					intruduce
							.setText("您还未输入原始密码，修改密码需要原始密码来确认用户身份，防止他人恶意操作，点击可输入。");
				} else {
					intruduce.setText("您已输入原始密码：" + last
							+ "，若与当前账号密码不一致可点击重新输入。");
				}
			}
			break;
		case R.id.user_set_info_pwd_new1:
			if (hasFocus) {
				String new1 = share.getString("PWD_new1", "");
				if (new1.equals("")) {
					intruduce.setText("您还未输入新密码，点击可输入您想要修改的密码。");
				} else {
					intruduce.setText("您已输入新密码：" + new1
							+ "，可点击“确认密码”再次输入本密码，请保持这两次输入的密码一致。");
				}
			}
			break;
		case R.id.user_set_info_pwd_new2:
			if (hasFocus) {
				String new2 = share.getString("PWD_new2", "");
				if (new2.equals("")) {
					intruduce.setText("您还未确认新密码，点击可输入您刚才设置的新密码。");
				} else {
					if (new2.equals(share.getString("PWD_new1", ""))) {
						intruduce.setText("您已输入确认新密码：" + new2
								+ "，两次输入的新密码一致，可以提交修改。");
					} else {
						intruduce.setText("您已输入确认新密码：" + new2
								+ "，两次输入的新密码不一致，不能提交修改，请确认并重新输入。");
					}
				}
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_set_info_pwd_last:

			break;
		case R.id.user_set_info_pwd_new1:

			break;
		case R.id.user_set_info_pwd_new2:

			break;
		}
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
