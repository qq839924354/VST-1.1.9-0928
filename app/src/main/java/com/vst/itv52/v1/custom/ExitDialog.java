package com.vst.itv52.v1.custom;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpResult;
import com.vst.itv52.v1.util.CMDExecute;
import com.vst.itv52.v1.util.MD5Util;

public class ExitDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Context context;
	private long duration;// 使用时长
	private LinearLayout confirm, cancle, msgLine;
	private TextView line1, line2;
	private boolean isNet = false;// 是否是网络提示
	private int activityType = 2;// 1直播，0点播，2全聚合
	private static final int LINGCHEN = 0;// 0-5
	private static final int QINGCHEN = 1;// 5-7
	private static final int ZAOSHANG = 2;// 7-9
	private static final int SHANGWU = 3;// 9-12
	private static final int ZHONGWU = 4;// 12-14
	private static final int XIAWU = 5;// 14-18
	private static final int BANGWAN = 6;// 18-19
	private static final int WANSHANG = 7;// 19-22
	private static final int SHENYE = 8;// 22-24

	private int[] stringID = { R.string.exit_greet_ling,
			R.string.exit_greet_zao, R.string.exit_grret_shang,
			R.string.exit_greet_zhong, R.string.exit_greet_xia,
			R.string.exit_greet_wan };

	public ExitDialog(Context context) {
		super(context, R.style.CustomDialog);
		this.context = context;
		View view = LayoutInflater.from(context).inflate(
				R.layout.itv_exit_dialog_layout, null);
		line1 = (TextView) view.findViewById(R.id.itv_exit_msg_line1);
		line2 = (TextView) view.findViewById(R.id.itv_exit_msg_line2);
		confirm = (LinearLayout) view.findViewById(R.id.itv_exit_ok);
		cancle = (LinearLayout) view.findViewById(R.id.itv_exit_cancle);
		msgLine = (LinearLayout) view.findViewById(R.id.itv_exit_msg);
		setContentView(view);
		confirm.setOnClickListener(this);
		cancle.setOnClickListener(this);
	}

	@Override
	public void dismiss() {
		isNet = false;
		super.dismiss();
	}

	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}

	public void setMsgLineVisible() {
		msgLine.setVisibility(View.VISIBLE);
	}

	@Override
	public void setOnCancelListener(OnCancelListener listener) {
		isNet = false;
		super.setOnCancelListener(listener);
	}

	public void setIsNet(boolean isNet) {
		this.isNet = isNet;
	}

	public boolean isNet() {
		return isNet;
	}

	public void setNet(boolean isNet) {
		this.isNet = isNet;
	}

	public void updateDuration(long duration) {
		System.out.println("SendDuration:" + duration);
		this.duration = duration;
	}

	@Override
	public void setTitle(int titleId) {
		line1.setText(titleId);
	}

	@Override
	public void setTitle(CharSequence title) {
		line1.setText(title);
	}

	public void setMessage(String msg) {
		line2.setText(msg);
	}

	public void setMessage(int msgResId) {
		line2.setText(msgResId);
	}

	public void setButtonConfirm(String confirmText) {
		((TextView) confirm.findViewById(R.id.itv_exit_confirm_tv))
				.setText(confirmText);
	}

	public void setButtonCancle(String cancleText) {
		((TextView) cancle.findViewById(R.id.itv_exit_cancle_tv))
				.setText(cancleText);
	}

	public void matchTimePeriodSentence() {
		int period = getTimePeriod();
		switch (period) {
		case LINGCHEN:
			line2.setText(stringID[0]);
			break;
		case QINGCHEN:

			break;
		case ZAOSHANG:
			line2.setText(stringID[1]);
			break;
		case SHANGWU:
			line2.setText(stringID[2]);
			break;
		case ZHONGWU:
			line2.setText(stringID[3]);
			break;
		case XIAWU:
			line2.setText(stringID[4]);
			break;
		case BANGWAN:

			break;
		case WANSHANG:
			line2.setText(stringID[5]);
			break;
		case SHENYE:

			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.itv_exit_ok:
			//如果是网络的原因 直接启动无线网络
			if (isNet) {
				// 4.0 以上这样直接启动无线网络设置会出现异常
				// context.startActivity(new Intent(
				// Settings.ACTION_WIRELESS_SETTINGS));
				if (android.os.Build.VERSION.SDK_INT > 10) {
					context.startActivity(new Intent(
							android.provider.Settings.ACTION_SETTINGS));
				} else {
					context.startActivity(new Intent(
							android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}
			} else {
				if (MyApp.isOnline) {
					postUseDuration();
				}
				((Activity) context).finish();
			}
			dismiss();
			break;
		case R.id.itv_exit_cancle:
			dismiss();
			break;
		}
	}

	public void postUseDuration() {
		new Thread() {
			@Override
			public void run() {
				String url = "http://so.52itv.cn/vst_cn/user_count.php";
				NameValuePair[] pairs = new NameValuePair[5];
				pairs[0] = new BasicNameValuePair("st",
						String.valueOf(activityType));
				pairs[1] = new BasicNameValuePair("time",
						String.valueOf(duration));
				pairs[2] = new BasicNameValuePair("mac", get_user_mac());
				pairs[3] = new BasicNameValuePair("name", Build.MODEL);
				pairs[4] = new BasicNameValuePair("ver", Build.VERSION.RELEASE);
				Header[] headers = new Header[] {
						new BasicHeader("User-Mac", get_user_mac()),
						new BasicHeader("User-Key", MD5Util.getMD5String(
								"time-"
										+ String.valueOf(
												System.currentTimeMillis())
												.substring(0, 8)
										+ "/key-52itvlive").substring(0, 16)),
						new BasicHeader("User-Agent",
								"GGwlPlayer/QQ243944493 (" + Build.MODEL + ")") };
				HttpResult result = HttpClientHelper.post(url, headers, pairs);
				if (result != null && result.getStatuCode() == 200) {
					String strResult = result.getText(HTTP.UTF_8);
					MyApp.setApkRunTime(0);
					Log.d("info", "服务器返回判定结果  " + strResult);
				} else {
					Log.d("info", "没有返回值");
				}

			}
		}.start();
	}

	private int getTimePeriod() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour >= 0 && hour < 5) {
			return LINGCHEN;
		} else if (hour >= 5 && hour < 9) {
			return ZAOSHANG;
		} else if (hour >= 9 && hour < 12) {
			return SHANGWU;
		} else if (hour >= 12 && hour < 14) {
			return ZHONGWU;
		} else if (hour >= 14 && hour < 19) {
			return XIAWU;
		} else {
			return WANSHANG;
		}
	}

	private String get_user_mac() {
		String u_mac = fetch_mac_eth().trim();
		if (u_mac == null || u_mac.length() != 17) {
			u_mac = fetch_mac_wlan().trim();
		}
		return u_mac;
	}

	/**
	 * mac地址,无线
	 * 
	 * @return
	 */
	private String fetch_mac_wlan() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/sys/class/net/wlan0/address" };
			result = cmdexe.run(args, "system/bin/");
			result = result.substring(0, result.indexOf("\n"));
			// 防止获取不到返回乱码
			if (result.length() > 28) {
				result = result.substring(0, 28);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * mac地址,有线
	 * 
	 * @return
	 */
	private String fetch_mac_eth() {
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/cat", "/sys/class/net/eth0/address" };
			result = cmdexe.run(args, "system/bin/");
			result = result.substring(0, result.indexOf("\n"));
			// 防止获取不到返回乱码
			if (result.length() > 27) {
				result = result.substring(0, 27);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

}
