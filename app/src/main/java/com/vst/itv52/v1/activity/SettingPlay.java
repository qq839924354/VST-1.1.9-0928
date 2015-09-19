package com.vst.itv52.v1.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.LiveBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.service.TaskService;
import com.vst.itv52.v1.util.ConstantUtil;

public class SettingPlay extends BaseActivity implements OnClickListener,
		OnKeyListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private ImageView sharpLeft, sharpRight, scalorLeft, scalorRight;
	private ImageView lrLeft, lrRight, udLeft, udRight;
	private ImageView jumpLeft, jumpRight, defineLeft, defineRight;
	private ImageView autoliveLeft, autoliveRight;
	private TextView sharpTv, scalorTv, lrTv, udTv, jumpTv;
	private TextView defineTv, renewTv, autoliveTv;
	private LinearLayout sharp, scalor, lr, ud, jump, define, renew, autolive;
	private LiveUpdateReceiver liveUpdateReceiver;

	private int sharpIndex;
	private int scalIndex;
	private int lrIndex;
	private int udIndex;
	private int autoMod;// 快速进入直播方式
	private boolean isJump;
	private int listType;// 直播类表类型
	public static final int AUTOLIVE_NO = 0;
	public static final int AUTOLIVE_BOXBOOT = 1;
	public static final int AUTOLIVE_APPBOOT = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_play)).inflate();
		sharpIndex = MyApp.sharpness;
		scalIndex = MyApp.scaleMod;
		lrIndex = MyApp.liveLrFunction;
		udIndex = MyApp.liveUdFunction;
		isJump = MyApp.getVodJump();
		listType = MyApp.getChanState();
		autoMod = MyApp.getAutoLive();

		initPlayView();
		liveUpdateReceiver = new LiveUpdateReceiver();
		registerReceiver(liveUpdateReceiver, new IntentFilter(
				LiveBiz.LIVEUPDAT_ACTION));
	}

	private void initPlayView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);

		setName1.setText("播放首选项");
		setName2.setText("播放首选项");
		setItemLog.setImageResource(R.drawable.vod_setup);

		sharpLeft = (ImageView) findViewById(R.id.set_sharp_left);
		sharpRight = (ImageView) findViewById(R.id.set_sharp_right);
		scalorLeft = (ImageView) findViewById(R.id.set_scalor_left);
		scalorRight = (ImageView) findViewById(R.id.set_scalor_right);
		sharpTv = (TextView) findViewById(R.id.set_sharp_tv);
		scalorTv = (TextView) findViewById(R.id.set_scalor_tv);
		sharpTv.setText(matchSharpTag(sharpIndex));
		scalorTv.setText(matchScalMod(scalIndex));
		sharp = (LinearLayout) findViewById(R.id.set_sharp);
		scalor = (LinearLayout) findViewById(R.id.set_scalor);
		sharpLeft.setOnClickListener(this);
		sharpRight.setOnClickListener(this);
		scalorLeft.setOnClickListener(this);
		scalorRight.setOnClickListener(this);
		sharp.setOnKeyListener(this);
		scalor.setOnKeyListener(this);

		lrLeft = (ImageView) findViewById(R.id.set_lr_left);
		lrRight = (ImageView) findViewById(R.id.set_lr_right);
		udLeft = (ImageView) findViewById(R.id.set_ud_left);
		udRight = (ImageView) findViewById(R.id.set_ud_right);
		lrTv = (TextView) findViewById(R.id.set_lr_tv);
		udTv = (TextView) findViewById(R.id.set_ud_tv);
		lrTv.setText(matchLrFunction(lrIndex));
		udTv.setText(matchUdFunction(udIndex));
		lr = (LinearLayout) findViewById(R.id.set_lr);
		ud = (LinearLayout) findViewById(R.id.set_ud);
		lrLeft.setOnClickListener(this);
		lrRight.setOnClickListener(this);
		udLeft.setOnClickListener(this);
		udRight.setOnClickListener(this);
		lr.setOnKeyListener(this);
		ud.setOnKeyListener(this);

		jumpLeft = (ImageView) findViewById(R.id.set_jump_left);
		jumpRight = (ImageView) findViewById(R.id.set_jump_right);
		defineLeft = (ImageView) findViewById(R.id.set_defined_left);
		defineRight = (ImageView) findViewById(R.id.set_defined_right);
		jumpTv = (TextView) findViewById(R.id.set_jump_tv);
		defineTv = (TextView) findViewById(R.id.set_defined_tv);
		jumpTv.setText(matchFunction(isJump));
		defineTv.setText(matchListType(listType));
		jump = (LinearLayout) findViewById(R.id.set_jump);
		define = (LinearLayout) findViewById(R.id.set_defined);
		jumpLeft.setOnClickListener(this);
		jumpRight.setOnClickListener(this);
		defineLeft.setOnClickListener(this);
		defineRight.setOnClickListener(this);
		jump.setOnKeyListener(this);
		define.setOnKeyListener(this);

		autoliveLeft = (ImageView) findViewById(R.id.set_autolive_left);
		autoliveRight = (ImageView) findViewById(R.id.set_autolive_right);
		autoliveTv = (TextView) findViewById(R.id.set_autolive_tv);
		autoliveTv.setText(matchAutoLive(autoMod));
		autolive = (LinearLayout) findViewById(R.id.set_autolive);
		autolive.setOnKeyListener(this);
		autoliveLeft.setOnClickListener(this);
		autoliveRight.setOnClickListener(this);

		renew = (LinearLayout) findViewById(R.id.set_renew);
		renewTv = (TextView) findViewById(R.id.set_renew_tv);
		renew.setOnClickListener(this);
	}

	private String matchSharpTag(int sharpness) {
		String sharpTag = null;
		switch (sharpness) {
		case 0:
			sharpTag = "流畅";
			break;
		case 1:
			sharpTag = "标清";
			break;
		case 2:
			sharpTag = "高清";
			break;
		case 3:
			sharpTag = "超清";
			break;
		case 4:
			sharpTag = "蓝光";
			break;
		case 5:
			sharpTag = "3D";
			break;
		}
		return sharpTag;
	}

	private String matchScalMod(int scalMod) {
		String scalTag = null;
		switch (scalMod) {
		case 0:
			scalTag = "原始比例";
			break;
		case 1:
			scalTag = "4:3";
			break;
		case 2:
			scalTag = "16:9";
			break;
		}
		return scalTag;
	}

	private String matchLrFunction(int func) {
		String function = null;
		if (func == 0) {
			function = "左右键调节音量";
		} else {
			function = "左右键换源";
		}
		return function;
	}

	private String matchUdFunction(int func) {
		String function = null;
		if (func == 0) {
			function = "上键下一个频道";
		} else {
			function = "下键下一个频道";
		}
		return function;
	}

	private String matchFunction(boolean inTrue) {
		if (inTrue) {
			return "启用";
		} else {
			return "停用";
		}
	}

	private String matchAutoLive(int liveMod) {
		if (liveMod == AUTOLIVE_NO) {
			return "停用";
		} else if (liveMod == AUTOLIVE_BOXBOOT) {
			return "开机进入直播";
		} else if (liveMod == AUTOLIVE_APPBOOT) {
			return "软件启动进入直播";
		} else {
			return null;
		}
	}

	private String matchListType(int listtype) {
		// if (listtype == 0) {
		// return "官方默认";
		// } else
		if (listtype == ConstantUtil.LIVE_LIST_ALL) {
			return "所有列表";
		} else if (listtype == ConstantUtil.LIVE_LIST_NET) {
			return "网络自定义";
		} else {
			return "官方默认";
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.set_sharp_left:
			setSharpLeft();
			break;
		case R.id.set_sharp_right:
			setSharpRight();
			break;
		case R.id.set_scalor_left:
			setScalLeft();
			break;
		case R.id.set_scalor_right:
			setScalRight();
			break;
		case R.id.set_lr_left:
		case R.id.set_lr_right:
			setLrFunction();
			break;
		case R.id.set_ud_left:
		case R.id.set_ud_right:
			setUdFunction();
			break;
		case R.id.set_jump_left:
		case R.id.set_jump_right:
			setJump();
			break;
		case R.id.set_defined_left:
			setChannelListType();
			break;
		case R.id.set_defined_right:
			setChannelListType();
			break;
		case R.id.set_autolive_left:
			setAutoLiveLeft();
			break;
		case R.id.set_autolive_right:
			setAutoLiveRight();
			break;
		case R.id.set_renew:
			renewTv.setText("正在更新……");
			Intent service = new Intent(SettingPlay.this, TaskService.class);
			service.putExtra(TaskService.PARAM_IN_MSG,
					TaskService.PARAM_UPDATE_LIVE);
			SettingPlay.this.startService(service);
			renew.setClickable(false);
			break;
		}
	}

	private void setSharpLeft() {
		if (sharpIndex > 0) {
			sharpIndex--;
		} else {
			sharpIndex = 5;
		}
		sharpTv.setText(matchSharpTag(sharpIndex));
		MyApp.setSharpness(sharpIndex);
	}

	private void setSharpRight() {
		if (sharpIndex < 5) {
			sharpIndex++;
		} else {
			sharpIndex = 0;
		}
		sharpTv.setText(matchSharpTag(sharpIndex));
		MyApp.setSharpness(sharpIndex);
	}

	private void setScalLeft() {
		if (scalIndex > 0) {
			scalIndex--;
		} else {
			scalIndex = 2;
		}
		scalorTv.setText(matchScalMod(scalIndex));
		MyApp.setScaleMod(scalIndex);
	}

	private void setScalRight() {
		if (scalIndex < 2) {
			scalIndex++;
		} else {
			scalIndex = 0;
		}
		scalorTv.setText(matchScalMod(scalIndex));
		MyApp.setScaleMod(scalIndex);
	}

	private void setLrFunction() {
		if (lrIndex == 0) {
			lrIndex = 1;
		} else {
			lrIndex = 0;
		}
		lrTv.setText(matchLrFunction(lrIndex));
		MyApp.setLiveLrFunction(lrIndex);
	}

	private void setUdFunction() {
		if (udIndex == 0) {
			udIndex = 1;
		} else {
			udIndex = 0;
		}
		udTv.setText(matchUdFunction(udIndex));
		MyApp.setLiveUdFunction(udIndex);
	}

	private void setJump() {
		if (isJump) {
			isJump = false;
		} else {
			isJump = true;
			showJumpDialog();
		}
		jumpTv.setText(matchFunction(isJump));
		MyApp.setVodJump(isJump);
	}

	private void setChannelListType() {
		// if (listType > 1) {
		// listType--;
		// } else {
		// listType = 3;
		// }
		if (listType == ConstantUtil.LIVE_LIST_DEF_LOCAL) {
			listType = ConstantUtil.LIVE_LIST_NET;
			if (MyApp.getLoginKey() == null) {
				showNetListHintDialog();
			} else {
				MyApp.setChanState(listType);
				LiveChannelInfo lastChannel = LiveDataHelper.getInstance(this)
						.getChannelByVid(10000);// 查询VST电影频道是否存在列表中
				if (lastChannel == null) {// 不存在则说明列表没有更新过，更新
					Intent service = new Intent(SettingPlay.this,
							TaskService.class);
					service.putExtra(TaskService.PARAM_IN_MSG,
							TaskService.PARAM_UPDATE_LIVE);
					SettingPlay.this.startService(service);
				}
			}
		} else if(listType==ConstantUtil.LIVE_LIST_NET){
			listType = ConstantUtil.LIVE_LIST_ALL;
			MyApp.setChanState(listType);
		}else{
			listType=ConstantUtil.LIVE_LIST_DEF_LOCAL;
			MyApp.setChanState(listType);
		}
		// if (listType == 2) {
		// }
		defineTv.setText(matchListType(listType));
	}

	// private void setChannelListTypeRight() {
	// if (listType < 3) {
	// listType++;
	// if (listType == 2) {
	// if (MyApp.getLoginKey() == null) {
	// showNetListHintDialog();
	// } else {
	// MyApp.setChanState(listType);
	// }
	// }
	// } else {
	// listType = 1;
	// MyApp.setChanState(listType);
	// }
	// defineTv.setText(matchListType(listType));
	// }

	private void setAutoLiveLeft() {
		if (autoMod > 0) {
			autoMod--;
		} else {
			autoMod = 2;
		}
		autoliveTv.setText(matchAutoLive(autoMod));
		MyApp.setAutoLive(autoMod);
	}

	private void setAutoLiveRight() {
		if (autoMod < 2) {
			autoMod++;
		} else {
			autoMod = 0;
		}
		autoliveTv.setText(matchAutoLive(autoMod));
		MyApp.setAutoLive(autoMod);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch (v.getId()) {
		case R.id.set_sharp:
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setSharpLeft();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setSharpRight();
			}
			break;
		case R.id.set_scalor:
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setScalLeft();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setScalRight();
			}
			break;
		case R.id.set_lr:
			if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setLrFunction();
			}
			break;
		case R.id.set_ud:
			if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setUdFunction();
			}
			break;
		case R.id.set_jump:
			if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setJump();
			}
			break;
		case R.id.set_defined:
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setChannelListType();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setChannelListType();
			}
			break;
		case R.id.set_autolive:
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setAutoLiveLeft();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				setAutoLiveRight();
			}
			break;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(liveUpdateReceiver);
		super.onDestroy();
	}

	private class LiveUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(LiveBiz.LIVEUPDAT_ACTION)) {
				ItvToast toast = new ItvToast(SettingPlay.this);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setIcon(R.drawable.toast_smile);
				toast.setText(R.string.toast_set_live_renew_ok);
				toast.show();
				renew.setClickable(true);
				renewTv.setText("更新完成");
			}
		}
	}

	private void showJumpDialog() {
		final Dialog dialog = new Dialog(this, R.style.CustomDialog);
		View view = LayoutInflater.from(this).inflate(R.layout.set_jump_dialog,
				null);
		final EditText start = (EditText) view
				.findViewById(R.id.set_vod_jump_start_et);
		final EditText end = (EditText) view
				.findViewById(R.id.set_vod_jump_end_et);
		start.setText(MyApp.getJumpStart() / 1000 + "");
		end.setText(MyApp.getJumpEnd() / 1000 + "");
		Button ok = (Button) view.findViewById(R.id.jump_dialog_ok);
		Button cancle = (Button) view.findViewById(R.id.jump_dialog_cancle);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String startTime = start.getText().toString();
				String endTime = end.getText().toString();
				try {
					if (startTime != null && !startTime.isEmpty()) {
						MyApp.setJumpStart(Integer.valueOf(startTime));
					}
					if (endTime != null && !endTime.isEmpty()) {
						MyApp.setJumpSEnd(Integer.valueOf(endTime));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		});
		cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setContentView(view);
		dialog.show();
	}

	private void showNetListHintDialog() {
		// final String loginKey = MyApp.getLoginKey();
		AlertDialog dialog = new Builder(this)
				.setTitle("温馨提示")
				.setMessage(R.string.dialog_use_net_live_hint)
				.setPositiveButton("现在去登录",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// if (loginKey == null) {
								Intent intent = new Intent(SettingPlay.this,
										SettingLogin.class);
								startActivity(intent);
								overridePendingTransition(R.anim.zoout,
										R.anim.zoin);
								finish();
								// } else {
								// MyApp.setChanState(listType);
								// }
							}
						}).setCancelable(false).create();
		dialog.show();
	}

}
