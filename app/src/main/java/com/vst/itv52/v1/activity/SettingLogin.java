package com.vst.itv52.v1.activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.effect.ScaleAnimEffect;
import com.vst.itv52.v1.model.LogMsgInfo;
import com.vst.itv52.v1.util.ConfigUtil;
import com.vst.itv52.v1.util.MD5Util;
import com.vst.itv52.v1.util.StringUtil;

public class SettingLogin extends BaseActivity implements OnClickListener,
		OnItemClickListener, OnDismissListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	/* 註冊登陸 */
	private EditText userName;
	private EditText passWord;
	private EditText passCode;
	private RelativeLayout relaCode;
	private ImageView btnImgCode;
	private RadioGroup rbCookieDate;
	private Button showPop;
	private Button login;
	private Button regest;
	private ArrayList<String> mList = new ArrayList<String>();
	private PopupWindow mPopup;
	private boolean isShowing = false;
	private ArrayAdapter<String> mAdapter;
	private ListView mListView;
	private boolean mInitPop = false;
	private static final int LOGIN_RESULT = 0x678;
	// private static final int LOGIN_AUTO = 0x789;
	private boolean success = false;
	private static final int REGESTED_SUCCESS = 0x159;
	private String cookieSaveDate;
	private String cookieString;
	private ScaleAnimEffect animEffect;// 产生缩放的
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOGIN_RESULT:
				String title = "";
				String mesg = "";
				if (success) {
					title = "登录成功";
					if (logMsgInfo != null) {
						mesg = logMsgInfo.getLogMsg();
					} else {
						mesg = "恭喜你，登录成功。";
					}

				} else {
					login.setText("登录");
					title = "登录失败";
					if (logMsgInfo != null) {
						mesg = logMsgInfo.getLogMsg();
					} else {
						mesg = "⊙﹏⊙b哎~~手一抖，用户名或密码就输错了，再试一次吧……";
					}
					// 重新获取一次验证码
					MyApp.pool.execute(getCodeImage);
				}
				createLogDialog(title, mesg);

				break;
			case MSG_DOWNLOADE_CODE:
				// 给控件设置图片
				try {
					String pathName = getCacheDir() + "code.png";
					Bitmap bm = BitmapFactory.decodeFile(pathName);
					btnImgCode.setImageBitmap(bm);
					Log.i("info", "cookie=" + cookieString);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}

		public void createLogDialog(String title, String mesg) {
			AlertDialog dialog = new Builder(SettingLogin.this)
					.setTitle(title)
					.setMessage(mesg)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if (success) {
										userName.setText("");
										passWord.setText("");
										passCode.setText("");
										/* 跳转到下一级界面 */
										Intent intent = new Intent(
												SettingLogin.this,
												UserSetMain.class);
										startActivity(intent);
										finish();
										overridePendingTransition(R.anim.zoout,
												R.anim.zoin);
									} else {
										if (logMsgInfo != null) {
											if (logMsgInfo.getLogCode()==1||logMsgInfo
												.getLogCode()==2) {
												userName.setText("");
											} else if (logMsgInfo
													.getLogCode()==3) {
												passWord.setText("");
											} else if (logMsgInfo
													.getLogCode()==-1) {
												relaCode.setVisibility(View.VISIBLE);
											}
										} else {
											userName.setText("");
											passWord.setText("");
										}
										passCode.setText("");
									}
								}
							}).create();
			dialog.show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			this.animEffect = new ScaleAnimEffect();
			setContentView(R.layout.setting_main);
			((ViewStub) findViewById(R.id.set_login_regest)).inflate();
			initLoginView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 登陆/注册视图 */
	private void initLoginView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("登录/注册");
		setName2.setText("登录/注册");
		setItemLog.setImageResource(R.drawable.reg_setup);
		// 新增项
		passCode = (EditText) findViewById(R.id.setting_login_passCode);
		relaCode = (RelativeLayout) findViewById(R.id.relaCode);
		frameCode = (FrameLayout) findViewById(R.id.frameImage);
		btnImgCode = (ImageView) findViewById(R.id.setting_login_code);
		btnImgCode.requestFocus();
		rbCookieDate = (RadioGroup) findViewById(R.id.rbCookieData);
		cookieSaveDate = "7";
		userName = (EditText) findViewById(R.id.setting_login_username);
		passWord = (EditText) findViewById(R.id.setting_login_password);
		showPop = (Button) findViewById(R.id.setting_login_users);

		login = (Button) findViewById(R.id.setting_login_login);
		regest = (Button) findViewById(R.id.setting_login_regest);
		initUserNames();
		initLoginListener();
		//MyApp.
	}

	/* 登陆/注册监听 */
	private void initLoginListener() {
		rbCookieDate.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				if (R.id.rbOneDay == arg1) {
					cookieSaveDate = "1";
				} else if (R.id.rbSevenDay == arg1) {
					cookieSaveDate = "7";
				} else if (R.id.rbMounth == arg1) {
					cookieSaveDate = "30";
				}
			}
		});
		login.setOnClickListener(this);
		regest.setOnClickListener(this);
		showPop.setOnClickListener(this);
		btnImgCode.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if (hasFocus) {
					showOnFocusAnimation();
					// flyWhiteBorder(140, 40, 40, 10);
				} else {
					showLooseFocusAinimation();
				}

			}
		});
		btnImgCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 刷新验证码
				MyApp.pool.execute(getCodeImage);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void initUserNames() {
		ObjectInputStream ois = null;
		try {
			InputStream is = openFileInput("account.obj");
			ois = new ObjectInputStream(is);
			mList = (ArrayList<String>) ois.readObject();
			if (mList.size() > 0) {
				userName.setText(mList.get(mList.size() - 1));
			}
			if (mList.size() > 1) {
				showPop.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final String CODEPATH = "http://my.91vst.com/verifycode";

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_login_login:
			final String user = userName.getText().toString().trim();
			final String pwd = passWord.getText().toString();
			final String code = passCode.getText().toString();
			// 获取验证码
			MyApp.pool.execute(getCodeImage);
			if (user == null || pwd == null || user.isEmpty() || pwd.isEmpty()) {
				ItvToast toast = new ItvToast(SettingLogin.this);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setIcon(R.drawable.toast_err);
				toast.setText(R.string.toast_set_login_in);
				toast.show();
			} else {
				login.setText("登录中……");
				new Thread(new Runnable() {
					@Override
					public void run() {
						success = loginToServer(user, pwd, code, cookieSaveDate);
						handler.sendEmptyMessage(LOGIN_RESULT);
					}
				}).start();
			}
			break;
		case R.id.setting_login_regest:
			startActivityForResult(new Intent(SettingLogin.this,
					SettingRegest.class), REGESTED_SUCCESS);
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			break;
		case R.id.setting_login_users:
			Log.i("info", "展开");
			if (mList != null && mList.size() > 0 && !mInitPop) {
				mInitPop = true;
				initPopup();
			}
			if (mPopup != null) {
				if (!isShowing) {
					mPopup.showAsDropDown(userName, 0, 0);
					showPop.setText("-");
					isShowing = true;
				} else {
					showPop.setText("+");
					mPopup.dismiss();
				}
			}
			break;
		}
	}

	private static final int MSG_DOWNLOADE_CODE = 111;
	/**
	 * 获取验证码和验证码图片cookie和下载位图
	 */
	Runnable getCodeImage = new Runnable() {

		@Override
		public void run() {
			try {
				URL url = new URL(CODEPATH);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				// connection.setRequestMethod("POST");
				connection.setRequestProperty("User-Agent",
						"GGwlPlayer/QQ243944493");
				cookieString = connection.getHeaderField("set-cookie");
				Log.i("info", "downloadCookie=" + cookieString);
				// long filelength = connection.getContentLength();
				if (connection.getResponseCode() == 200) {
					DataInputStream in = new DataInputStream(
							connection.getInputStream());
					// 如果文件存在 则删除文件
					File file = new File(getCacheDir() + "code.png");
					if (file.exists()) {
						file.delete();
					}
					DataOutputStream out = new DataOutputStream(
							new FileOutputStream(getCacheDir() + "code.png"));
					byte[] buffer = new byte[4096];
					int count = -1;
					while ((count = in.read(buffer)) != -1) {
						out.write(buffer, 0, count);
					}
					out.close();
					in.close();
					connection.disconnect();
					handler.sendEmptyMessage(MSG_DOWNLOADE_CODE);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	};

	private void initPopup() {
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, mList);
		mListView = new ListView(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		int height = 200;
		int width = userName.getWidth();
		System.out.println(width);
		mPopup = new PopupWindow(mListView, width, height, true);
		mPopup.setOutsideTouchable(true);
		mPopup.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.setup_bg_left));
		mPopup.setOnDismissListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String user = data.getExtras().getString("userName");
			String pass = data.getExtras().getString("passWord");
			userName.setText(user);
			passWord.setText(pass);
		}
	}

	private LogMsgInfo logMsgInfo;// 返回信息

	/**
	 * 登陆到服务器
	 * 
	 * @param user
	 *            用户名
	 * @param pwd
	 *            密码
	 * @param code
	 *            验证码
	 * @param cookieDate
	 *            保存天数
	 * @return
	 */
	private boolean loginToServer(String user, String pwd, String code,
			String cookieDate) {
		HttpPost httpRequest = new HttpPost(ConfigUtil.getValue("LOGIN"));
		httpRequest.addHeader("Cookie", cookieString);
		Log.i("info", "LogcodeCookie=" + cookieString);
		Log.i("info", "logUserName=" + user);
		Log.i("info", "logCode=" + code);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (code != null) {
			pwd = MD5Util.getMD5String(code
					+ MD5Util.getMD5String(StringUtil.toHexString(MD5Util
							.getMD5String(pwd))));
		} else {
			pwd = MD5Util.getMD5String(MD5Util.getMD5String(StringUtil
					.toHexString(MD5Util.getMD5String(pwd))));
		}
		Log.i("info", "logPassword=" + pwd);
		params.add(new BasicNameValuePair("c", code));
		params.add(new BasicNameValuePair("u", user));
		params.add(new BasicNameValuePair("p", pwd));
		params.add(new BasicNameValuePair("t", cookieDate));
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse httpResponse = client.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				Log.i("info", "服务器返回判定结果" + strResult);
				wirteAccountToFile();
				logMsgInfo =parseJson(strResult);
				Log.i("info", "logMsgInfo="+logMsgInfo);
				if(logMsgInfo !=null && logMsgInfo.getLogCode()==0){
					Log.d("info", "logMsgInfo.getCookie()="+logMsgInfo.getCookie());
					MyApp.setLoginKey(logMsgInfo.getCookie());
					return true;
				}else {
					return false;
				}
			} else {
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	// 普通Json数据解析 
		private LogMsgInfo parseJson(String strResult) { 
			try { 
				LogMsgInfo logMsgInfo1=new LogMsgInfo();
				JSONObject jsonObj = new JSONObject(strResult); 
				int logCode=jsonObj.getInt("suc");
				String logCookie = jsonObj.getString("cookie"); 
				String logMsg = jsonObj.getString("msg"); 
				logMsgInfo1.setLogCode(logCode);
				logMsgInfo1.setCookie(logCookie);
				logMsgInfo1.setLogMsg(logMsg);
				return logMsgInfo1;
			} catch (JSONException e) { 
				System.out.println("Json parse error"); 
				e.printStackTrace(); 
			} 
			return null;
		} 

	private void wirteAccountToFile() {
		String input = userName.getText().toString();
		mList.remove(input);
		mList.add(input);
		if (mList.size() > 10) {
			mList.remove(0);
		}
		ObjectOutputStream out = null;
		try {
			FileOutputStream os = openFileOutput("account.obj", MODE_PRIVATE);
			out = new ObjectOutputStream(os);
			out.writeObject(mList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		userName.setText(mList.get(position));
		mPopup.dismiss();
		showPop.setText("+");
	}

	@Override
	public void onDismiss() {
		isShowing = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

	private FrameLayout frameCode;

	/**
	 * 获得焦点
	 * 
	 * @param position
	 */
	private void showOnFocusAnimation() {
		frameCode.bringToFront();
		animEffect.setAttributs(1.0f, 1.30f, 1.0f, 1.30f, 100);
		Animation anim = animEffect.createAnimation();
		btnImgCode.startAnimation(anim);
	}

	/**
	 * 失去焦点
	 * 
	 * @param position
	 */
	private void showLooseFocusAinimation() {
		animEffect.setAttributs(1.30f, 1.0f, 1.30f, 1.0f, 100);
		btnImgCode.startAnimation(animEffect.createAnimation());
	}

}
