package com.vst.itv52.v1.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class SettingOther extends BaseActivity implements
		OnCheckedChangeListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private SharedPreferences share;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_channel)).inflate();
		share = getSharedPreferences("settingSPF", MODE_PRIVATE);
		initView();

	}

	private void initView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("其他设置");
		setName2.setText("其他设置");
		setItemLog.setImageResource(R.drawable.other_setup);

		CheckBox playSound = (CheckBox) findViewById(R.id.setting_channel_play_sound);
		playSound.setChecked(share.getBoolean("play_sound", true));
		playSound.setOnCheckedChangeListener(this);

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Editor editor = share.edit();
		switch (buttonView.getId()) {
		case R.id.setting_channel_play_sound:
			editor.putBoolean("play_sound", isChecked);
			break;
		}
		editor.commit();
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
