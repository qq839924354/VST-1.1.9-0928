package com.vst.itv52.v1.activity;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.custom.UpdateDialog;
import com.vst.itv52.v1.service.TaskService;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingAboutUs extends BaseActivity implements OnClickListener {
	private TextView version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_about);
		version = (TextView) findViewById(R.id.set_about_version);
		version.setText("版本号：" + getAPKVersionName());

		Button update = (Button) findViewById(R.id.set_check_update);
		update.setOnClickListener(this);
	}

	private String getAPKVersionName() {
		PackageManager packageManager = this.getPackageManager();
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packInfo.versionName;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_check_update:
			Intent service = new Intent(SettingAboutUs.this, TaskService.class);
			service.putExtra(TaskService.PARAM_IN_MSG,
					TaskService.PARAM_UPDATE_APK);
			SettingAboutUs.this.startService(service);
			new UpdateDialog(this).show();
			break;
		}
	}

}
