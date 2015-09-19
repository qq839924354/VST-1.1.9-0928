package com.vst.itv52.v1.custom;

import java.io.File;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.ApkUpdatebiz;
import com.vst.itv52.v1.util.FileUtils;

public class UpdateDialog extends Dialog implements
		android.view.View.OnClickListener {
	private static final int CHECK = 0;
	public static final int UPDATE = 1;
	private static final int UPDATE_USER = 2;
	public static final int NO_UPDATE = 3;
	private int currentState = CHECK;

	private Context context;

	private TextView title, msgTxt;
	private Button comfire, cancle, forgive;
	private LinearLayout proLine, btnLine;
	private ScrollView msgLine;

	private String msg;
	private String path;
	private int version = 0;

	private ApkUpdateReciver reciver;

	public UpdateDialog(Context context) {
		super(context, R.style.CustomDialog);
		this.context = context;
		init();
	}

	public UpdateDialog(Context context, int state, String updateMsg,
			String apkPath, int verCode) {
		super(context, R.style.CustomDialog);
		this.context = context;
		this.currentState = state;
		this.path = apkPath;
		this.msg = updateMsg;
		this.version = verCode;
		init();
		renewDiaLogView();
	}

	private void init() {
		reciver = new ApkUpdateReciver();
		IntentFilter filter = new IntentFilter(ApkUpdatebiz.CHECK_UPDATE_ACTION);
		context.registerReceiver(reciver, filter);
		View view = LayoutInflater.from(context).inflate(
				R.layout.itv_update_dialog, null);
		title = (TextView) view.findViewById(R.id.update_dialog_title);
		msgTxt = (TextView) view.findViewById(R.id.update_dialog_msg);
		comfire = (Button) view.findViewById(R.id.update_dialog_comfire);
		cancle = (Button) view.findViewById(R.id.update_dialog_cancle);
		forgive = (Button) view.findViewById(R.id.update_dialog_forgive);
		comfire.setOnClickListener(this);
		cancle.setOnClickListener(this);
		forgive.setOnClickListener(this);

		proLine = (LinearLayout) view.findViewById(R.id.update_dialog_proline);
		btnLine = (LinearLayout) view.findViewById(R.id.update_dialog_btnline);
		msgLine = (ScrollView) view.findViewById(R.id.update_dialog_msgline);
		setContentView(view);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.update_dialog_comfire:
			if (currentState == UPDATE_USER || currentState == UPDATE) {
				FileUtils.modifyFile(new File(path));
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.parse("file://" + path),
						"application/vnd.android.package-archive");
				context.startActivity(intent);
			}
			dismiss();
			break;
		case R.id.update_dialog_cancle:
			dismiss();
			break;
		case R.id.update_dialog_forgive:
			MyApp.setForgiveVersion(version);
			dismiss();
			break;
		}
	}

	@Override
	public void dismiss() {
		context.unregisterReceiver(reciver);
		super.dismiss();
	}

	private void renewDiaLogView() {
		switch (currentState) {
		case CHECK:
			proLine.setVisibility(View.VISIBLE);
			btnLine.setVisibility(View.GONE);
			msgLine.setVisibility(View.GONE);
			break;
		case UPDATE:
			proLine.setVisibility(View.GONE);
			btnLine.setVisibility(View.VISIBLE);
			btnLine.requestFocus();
			msgLine.setVisibility(View.VISIBLE);
			title.setText("软件版本更新");
			msgTxt.setText(msg);
			break;
		case UPDATE_USER:
			proLine.setVisibility(View.GONE);
			btnLine.setVisibility(View.VISIBLE);
			comfire.setText("安装更新");
			cancle.setText("暂不更新");
			forgive.setVisibility(View.GONE);
			btnLine.requestFocus();
			msgLine.setVisibility(View.VISIBLE);
			title.setText("软件版本更新");
			msgTxt.setText(msg);
			break;
		case NO_UPDATE:
			proLine.setVisibility(View.GONE);
			btnLine.setVisibility(View.VISIBLE);
			btnLine.requestFocus();
			msgLine.setVisibility(View.VISIBLE);
			cancle.setVisibility(View.GONE);
			forgive.setVisibility(View.GONE);
			title.setText("暂无更新");
			msgTxt.setText("赞哦，使用最新发布版；更新版本努力开发中……");
			comfire.setText("好耶，过段时间再来看");
			break;
		}
	}

	class ApkUpdateReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println(action);
			if (action.equals(ApkUpdatebiz.CHECK_UPDATE_ACTION)) {
				msg = intent.getStringExtra("updatemsg");
				path = intent.getStringExtra("filepath");
				version = intent.getIntExtra("version", 0);
				if (msg != null && path != null) {
					Log.d("UpdateDialog", "ApkUpdateReciver   " + " msg ="
							+ msg + " ,path =" + path + "，Version=" + version);
					currentState = UPDATE_USER;
				} else {
					currentState = NO_UPDATE;
				}
				renewDiaLogView();
				// 接受到驻留广播后移除
				context.removeStickyBroadcast(intent);
			}
		}
	}

}
