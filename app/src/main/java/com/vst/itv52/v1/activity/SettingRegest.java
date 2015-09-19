package com.vst.itv52.v1.activity;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpResult;
import com.vst.itv52.v1.util.BoxInfoFetcher;
import com.vst.itv52.v1.util.ConfigUtil;

public class SettingRegest extends BaseActivity implements
		OnFocusChangeListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private ViewStub include;// 子界面嵌套

	private EditText userName;
	private EditText password1;
	private EditText password2;
	private EditText phone;
	private EditText email;
	private Button regest;
	private TextView checkName;
	private TextView checkEmail;
	private TextView checkPWD;
	private boolean isUseful = false;// 用户名是否可用
	private boolean beTheSame = false;// 两次输入密码是否一致
	private boolean isUsable = false;// 邮箱是否已注册过
	private static final int CHECK_PWD = 0;
	private static final int CHECK_USERNAME = 1;
	private static final int USERNAME_EMPTY = 2;
	private static final int CHECK_EMAIL = 3;
	private static final int EMAIL_EMPTY = 4;
	private static final int USERNAME_ERR = 5;
	private static final int EMAIL_ERR = 6;
	private static final int SERVER_RESPONSE = 7;
	private static final int REGEST_SUCCESS = 8;
	private static final String TAG = "Regest";

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CHECK_PWD:
				String pwd1 = password1.getText().toString();
				String pwd2 = password2.getText().toString();
				if (pwd1.isEmpty() && pwd2.isEmpty()) {
					checkPWD.setText("检测两次密码输入是否一致");
					checkPWD.setTextColor(getResources().getColor(R.color.dark));
					beTheSame = false;
				} else if (pwd1.equals(pwd2)) {
					checkPWD.setText("两次密码输入一致");
					checkPWD.setTextColor(getResources()
							.getColor(R.color.green));
					beTheSame = true;
				} else {
					checkPWD.setText("两次密码输入不一致，请确认");
					checkPWD.setTextColor(getResources().getColor(R.color.red));
					beTheSame = false;
				}
				break;
			case CHECK_USERNAME:
				if (isUseful) {
					checkName.setText("当前用户名可用");
					checkName.setTextColor(getResources().getColor(
							R.color.green));
				} else {
					checkName.setText("当前用户名已被占用，请更换");
					checkName
							.setTextColor(getResources().getColor(R.color.red));
				}
				break;
			case USERNAME_EMPTY:
				checkName.setText("检测用户名是否可用");
				checkName.setTextColor(getResources().getColor(R.color.dark));
				break;
			case CHECK_EMAIL:
				if (isUsable) {
					checkEmail.setText("当前邮箱可以注册");
					checkEmail.setTextColor(getResources().getColor(
							R.color.green));
				} else {
					checkEmail.setText("当前邮箱已被注册，请更换");
					checkEmail.setTextColor(getResources()
							.getColor(R.color.red));
				}
				break;
			case EMAIL_EMPTY:
				checkEmail.setText("用于找回密码的联系方式");
				checkEmail.setTextColor(getResources().getColor(R.color.dark));
				break;
			case USERNAME_ERR:
				checkName.setText("-_-!您输入的用户名格式不符合要求");
				checkName.setTextColor(getResources().getColor(R.color.red));
				break;
			case EMAIL_ERR:
				checkEmail.setText("-_-!您输入的邮箱格式不符合要求");
				checkEmail.setTextColor(getResources().getColor(R.color.red));
				break;
			case SERVER_RESPONSE:
				String response = (String) msg.obj;
				AlertDialog dialog = new Builder(SettingRegest.this)
						.setTitle("Sorry")
						.setMessage(response + "!")
						.setNegativeButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).create();
				dialog.show();
				break;
			case REGEST_SUCCESS:
				// userName.setText("");
				// email.setText("");
				// password1.setText("");
				// password2.setText("");
				// phone.setText("");
				AlertDialog dialog2 = new Builder(SettingRegest.this)
						.setTitle("注册成功")
						.setMessage("(*^__^*)恭喜你注册成功。")
						.setNegativeButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent();
										intent.putExtra("userName", userName
												.getText().toString());
										intent.putExtra("passWord", password1
												.getText().toString());
										SettingRegest.this.setResult(RESULT_OK,
												intent);
										SettingRegest.this.finish();
										overridePendingTransition(
												R.anim.zoomin, R.anim.zoomout);
									}
								}).create();
				dialog2.show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		initView();
		initListener();
	}

	private void initView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("用户注册");
		setName2.setText("用户注册");
		setItemLog.setImageResource(R.drawable.reg_setup);
		include = (ViewStub) findViewById(R.id.set_regest);
		include.inflate();

		userName = (EditText) findViewById(R.id.setting_regest_username);
		password1 = (EditText) findViewById(R.id.setting_regest_password1);
		password2 = (EditText) findViewById(R.id.setting_regest_password2);
		phone = (EditText) findViewById(R.id.setting_regest_phone);
		email = (EditText) findViewById(R.id.setting_regest_email);
		regest = (Button) findViewById(R.id.setting_regest_btn);
		checkName = (TextView) findViewById(R.id.setting_regest_check_name);
		checkEmail = (TextView) findViewById(R.id.setting_regest_check_email);
		checkPWD = (TextView) findViewById(R.id.setting_regest_check_pwd);
	}

	private void initListener() {
		userName.setOnFocusChangeListener(this);
		email.setOnFocusChangeListener(this);
		password1.setOnFocusChangeListener(this);
		password2.setOnFocusChangeListener(this);

		regest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isUseful && beTheSame && isUsable) {
					final String name = userName.getText().toString().trim();
					final String e_mail = email.getText().toString().trim();
					final String pwd1 = password1.getText().toString();
					final String pwd2 = password2.getText().toString();
					final String phoneNum = phone.getText().toString().trim();
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (regestToServer(name, e_mail, pwd1, pwd2,
									phoneNum)) {
								handler.sendEmptyMessage(REGEST_SUCCESS);
								// 注册成功，关联盒子硬件信息
								String url = ConfigUtil
										.getValue("BOXINFO_SEND");
								String info = Base64.encodeToString(
										new BoxInfoFetcher().fetchBoxInfo()
												.getBytes(), Base64.DEFAULT);
								NameValuePair[] pairs = new NameValuePair[1];
								pairs[0] = new BasicNameValuePair("post=", info);
								HttpResult result = HttpClientHelper.post(url,
										pairs);
								if (result != null
										&& result.getStatuCode() == 200) {
									String strResult = result
											.getText(HTTP.UTF_8);
									Log.i(TAG, "服务器返回判定结果SendBoxInfo"
											+ strResult);
								} else {
									Log.i(TAG, "没有返回值");
								}
							}else{
								
							}
						}
					}).start();
				} else {
					ItvToast toast = new ItvToast(SettingRegest.this);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setText(R.string.toast_set_regest_input_err);
					toast.show();
				}
			}
		});
	}

	protected boolean regestToServer(String name, String e_mail, String pwd1,
			String pwd2, String phoneNum) {
		NameValuePair[] params = new NameValuePair[6];
		params[0] = new BasicNameValuePair("code", "itv");
		params[1] = new BasicNameValuePair("userid", name);
		params[2] = new BasicNameValuePair("email", e_mail);
		params[3] = new BasicNameValuePair("userpass", pwd1);
		params[4] = new BasicNameValuePair("password", pwd2);
		params[5] = new BasicNameValuePair("tel", phoneNum);
		HttpResult result = HttpClientHelper.post(
				ConfigUtil.getValue("REGEST"), params);
		if (result != null && result.getStatuCode() == 200) {
			String strResult = result.getText(HTTP.UTF_8);
			Log.i(TAG, "服务器返回判定结果Regest:" + strResult);
			if (strResult.contains("false") || strResult.contains("true")) {
				return Boolean.valueOf(strResult);
			} else {
				Message message = new Message();
				message.what = SERVER_RESPONSE;
				message.obj = strResult;
				handler.sendMessage(message);
			}
		} else {
			return false;
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
		case R.id.setting_regest_username:
			if (hasFocus) {
				checkName.setText("用户名长度5-13,且只能以字母开始");
				checkName.setTextColor(getResources().getColor(R.color.dark));
			}
			break;
		case R.id.setting_regest_email:
			if (hasFocus) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String name = userName.getText().toString().trim();
						if (name != null && !name.equals("")) {
							if (!isUseful) {
								isUseful = checkIsUseful("userid", name);
								handler.sendEmptyMessage(CHECK_USERNAME);
							}
						} else {
							handler.sendEmptyMessage(USERNAME_EMPTY);
						}
					}
				}).start();
			}
			break;
		case R.id.setting_regest_password1:
			if (hasFocus) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String name = email.getText().toString().trim();
						if (name != null && !name.equals("")) {
							if (!isUsable) {
								isUsable = checkIsUseful("email", name);
								handler.sendEmptyMessage(CHECK_EMAIL);
							}
						} else {
							handler.sendEmptyMessage(EMAIL_EMPTY);
						}
					}
				}).start();
			}
			break;
		case R.id.setting_regest_password2:
			final Timer timer = new Timer();
			if (hasFocus) {
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						handler.sendEmptyMessage(CHECK_PWD);
						if (beTheSame) {
							timer.cancel();
						}
					}
				}, 1000, 1500);
			} else {
				timer.cancel();
			}
			break;
		}
	}

	/**
	 * 检测新注册用户名、邮箱是否可用
	 * 
	 * @return
	 */
	protected boolean checkIsUseful(String type, String name) {
		NameValuePair[] params = new NameValuePair[1];
		params[0] = new BasicNameValuePair(type, name);
		HttpResult result = HttpClientHelper.post(
				ConfigUtil.getValue("REGEST_CHECK"), params);
		if (result != null && result.getStatuCode() == 200) {
			String strResult = result.getText(HTTP.UTF_8);
			Log.i(TAG, "服务器返回判定结果Check:" + strResult);
			if (strResult.contains("false") || strResult.contains("true")) {
				return Boolean.valueOf(strResult);
			} else {
				if (type.equals("userid")) {
					handler.sendEmptyMessage(USERNAME_ERR);
				} else if (type.equals("email")) {
					handler.sendEmptyMessage(EMAIL_ERR);
				}
			}
		} else {
			return false;
		}
		return false;
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
