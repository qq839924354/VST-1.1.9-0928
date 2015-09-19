package com.vst.itv52.v1.activity;

import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.LixianAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.XLLXBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.model.XLLXFileInfo;
import com.vst.itv52.v1.model.XLLXUserInfo;
import com.vst.itv52.v1.player.XLLXPlayer;

/**
 * 1.获取 sessionid <br>
 * 2.sessionid 登陆 获取用户信息<br>
 * 3.新用户 登陆<br>
 * 4.获取 UID
 * 
 * @author shenhui
 * 
 */
public class LixianActivity extends BaseActivity {

	private Context context = null;
	private final static int LOGIN_SUCESS = 1;
	private final static int LOGIN_ERROR = 2;

	private final static int REFESH_USERINFO = 3;
	private final static int REFRESH_LIST = 4;
	private final static int START_LOGIN = 5;
	private final static int LOGIN_VERIFY_NEED = 6;
	private final static int LOGIN_VERIFY_NO = 7;
	private final static int SHOW_VERIFY = 8;
	private static int User_UID_CHID = 0;
	private int pageIndex = 1;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case START_LOGIN:
				progressShow();
				break;
			case LOGIN_SUCESS:
				MyApp.pool.execute(getUserInfo);
				break;
			case LOGIN_VERIFY_NEED:
				layoutProving.setVisibility(View.VISIBLE);
				MyApp.pool.execute(getVerify);
				break;
			case LOGIN_VERIFY_NO:
				layoutProving.setVisibility(View.INVISIBLE);
				break;
			case SHOW_VERIFY:
				Bitmap bm = (Bitmap) msg.obj;
				verifyImg.setImageBitmap(bm);
				break;
			case LOGIN_ERROR:
				progressDismiss();
				int flag = msg.arg1;
				if (flag == 2) {
					setLogin(false);
					autoTextPassword.setText("");
					toast.setText(R.string.toast_xllx_pwd_err);
				} else if (flag == 4 || flag == 5) {
					setLogin(false);
					autoTextUsername.setText("");
					autoTextPassword.setText("");
					toast.setText(R.string.toast_xllx_uid_unexsit);
				} else if (flag == 6) {
					setLogin(false);
					autoTextUsername.setText("");
					autoTextPassword.setText("");
					toast.setText(R.string.toast_xllx_uid_lock);
				} else if (flag == 1) {
					setLogin(false);
					layoutProving.setVisibility(View.INVISIBLE);
					toast.setText(R.string.toast_xllx_verify);
				} else if (flag == 10) {
					setLogin(false);
					toast.setText(R.string.toast_xllx_uinfo_err);
					if (XLLXBiz.getUsrname(context) != null) {
						// btnUserinfoRetry.setVisibility(View.VISIBLE);
					}
				} else if (flag == 11) {
					setLogin(true);
					listVideo.setEmptyView(btnVideolistRetry);
					toast.setText(R.string.toast_xllx_list_err);
				} else if (flag == 16) {
					toast.setText(R.string.toast_xllx_connect_err);
				}
				toast.show();
				break;
			case REFESH_USERINFO:
				// 刷新 用户 信息 UI
				progressDismiss();
				userInfo = (XLLXUserInfo) msg.obj;
				layoutUserinfo.setVisibility(View.VISIBLE);
				layoutLogin.setVisibility(View.INVISIBLE);
				setLogin(true);
				txtNickname.setText("昵称：" + userInfo.nickname);
				txtUsrname.setText("账号：" + userInfo.usrname);
				if (userInfo.isvip == 1) {
					txtLevel.setText("会员等级：VIP" + userInfo.level);
					txtExpiredate.setText("到期日期：" + userInfo.expiredate);
				} else {
					txtLevel.setText("会员等级：非会员");
					txtExpiredate.setVisibility(View.GONE);
				}
				setLogin(true);
				MyApp.pool.execute(getVideoList);
				progressShow();
				break;
			case REFRESH_LIST:
				// 刷新 列表UI
				progressDismiss();
				listVideo.setEmptyView(txtEmpty);
				ArrayList<XLLXFileInfo> list = (ArrayList<XLLXFileInfo>) msg.obj;
				videoList.addAll(list);
				adapter.notifyDataSetChanged();
				listVideo.setSelection((pageIndex - 1) * 30);
				listVideo.requestFocus();
				break;
			default:
				break;
			}
		}
	};

	XLLXUserInfo userInfo = null;

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
		if (isLogin) {
			btnLogin_out.setText("注销");
			layoutUserinfo.setVisibility(View.VISIBLE);
			layoutLogin.setVisibility(View.INVISIBLE);
		} else {
			btnLogin_out.setText("登陆");
			layoutUserinfo.setVisibility(View.INVISIBLE);
			layoutLogin.setVisibility(View.VISIBLE);
		}
	}

	boolean isFirstLogin = true;

	private Runnable getUserInfo = new Runnable() {

		@Override
		public void run() {

			Log.d("", "getUserInfo --------->>>>>");
			XLLXUserInfo userInfo = null;
			if (!isFirstLogin) {
				userInfo = XLLXBiz.getUserInfoFromLocal(context);
			} else {
				userInfo = XLLXBiz.getUser(context, getCookieHeader());
			}

			if (userInfo != null) {
				Message msg = handler.obtainMessage(REFESH_USERINFO, userInfo);
				handler.sendMessage(msg);
			} else { // session 过期
				Message msg = handler.obtainMessage(LOGIN_ERROR, 10, -1);
				handler.sendMessage(msg);
			}
		}
	};

	private Runnable login = new Runnable() {
		@Override
		public void run() {
			int flag = XLLXBiz.Login(context, autoTextUsername.getText()
					.toString(), autoTextPassword.getText().toString(),
					verifycode.getText().toString());
			System.out.println("登录：flag=" + flag);
			if (flag == 0 || flag == 11) { // 登陆成功
				handler.sendEmptyMessage(LOGIN_SUCESS);
			} else { // 失败
				Message msg = handler.obtainMessage(LOGIN_ERROR, flag, -1);
				handler.sendMessage(msg);
			}
		}
	};

	private Runnable autoLogin = new Runnable() {
		@Override
		public void run() {
			String userName = XLLXBiz.getUsrname(context);
			String userPWD = XLLXBiz.getUserPWD(context);
			if (userName != null && !userName.isEmpty() && userPWD != null
					&& !userPWD.isEmpty()) {
				int flag = XLLXBiz.Login(context, userName, userPWD, null);
				System.out.println("自动登录：flag=" + flag);
				if (flag == 0 || flag == 11) { // 登陆成功
					handler.sendEmptyMessage(LOGIN_SUCESS);
				} else { // 失败
					Message msg = handler.obtainMessage(LOGIN_ERROR, flag, -1);
					handler.sendMessage(msg);
				}
			}
		}
	};

	private Runnable checkVerify = new Runnable() {

		@Override
		public void run() {
			int flag = XLLXBiz.checkVerify(context, autoTextUsername.getText()
					.toString());
			System.out.println("验证：flag=" + flag);
			if (flag == 0) {// 不需要
				handler.sendEmptyMessage(LOGIN_VERIFY_NO);
			} else if (flag == 1) {// 需要
				handler.sendEmptyMessage(LOGIN_VERIFY_NEED);
			}
		}
	};

	private Runnable getVerify = new Runnable() {
		@Override
		public void run() {
			Bitmap bm = XLLXBiz.getVerify(LixianActivity.this);
			if (bm != null) {
				Message msg = new Message();
				msg.obj = bm;
				msg.what = SHOW_VERIFY;
				handler.sendMessage(msg);
			}
		}
	};

	ArrayList<XLLXFileInfo> videoList = new ArrayList<XLLXFileInfo>();

	private Runnable getVideoList = new Runnable() {

		@Override
		public void run() {

			Log.d("", "getVideoList --------->>>>>");

			ArrayList<XLLXFileInfo> list = XLLXBiz.getVideoList(context, 30,
					pageIndex);
			if (list != null) {
				Message msg = handler.obtainMessage(REFRESH_LIST, list);
				handler.sendMessage(msg);
			} else {
				System.out.println("获取列表失败");
				Message msg = handler.obtainMessage(LOGIN_ERROR, 11, -1);
				handler.sendMessage(msg);
			}
		}
	};

	boolean isLogin = false; //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lixian_video_list_layout);
		context = this;
		toast = new ItvToast(this);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setIcon(R.drawable.toast_err);
		initView();
		MyApp.pool.execute(autoLogin);
		layoutLogin.setVisibility(View.VISIBLE);
		layoutUserinfo.setVisibility(View.INVISIBLE);
		if (XLLXBiz.getCookie(context) != null) {
			isFirstLogin = false;
			autoTextUsername.setText(XLLXBiz.getUsrname(context));
			handler.sendEmptyMessage(START_LOGIN);
			MyApp.pool.execute(getUserInfo);
		}
	}

	private EditText autoTextUsername;
	private EditText autoTextPassword;
	private EditText verifycode;
	private ImageButton verifyImg;
	private View layoutUserinfo, layoutLogin, layoutProving;
	private ExpandableListView listVideo;
	private Button btnLogin_out;
	private TextView txtUsrname, txtNickname, txtExpiredate, txtLevel;
	private LixianAdapter adapter;
	private int expandFlag = -1;
	private Button btnUserinfoRetry, btnVideolistRetry;
	private TextView txtEmpty;
	private ItvToast toast;

	private void initView() {

		txtEmpty = (TextView) findViewById(R.id.lixian_videolist_empty_txt);
		btnUserinfoRetry = (Button) findViewById(R.id.lixian_getuserinfo_retry_btn);
		btnUserinfoRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyApp.pool.execute(getUserInfo);
			}
		});
		btnVideolistRetry = (Button) findViewById(R.id.lixian_getvideolist_retry_btn);
		btnVideolistRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyApp.pool.execute(getVideoList);
			}
		});

		layoutUserinfo = findViewById(R.id.lixian_userinfo_layout);
		layoutLogin = findViewById(R.id.lixian_login_layout);
		layoutProving = findViewById(R.id.lixian_login_proving_layout);
		btnLogin_out = (Button) findViewById(R.id.lixian_login_logout_btn);
		autoTextUsername = (EditText) findViewById(R.id.lixian_login_username_autotxt);
		autoTextUsername.setText(XLLXBiz.getUsrname(context));
		autoTextPassword = (EditText) findViewById(R.id.lixian_login_password_autotxt);
		verifycode = (EditText) findViewById(R.id.lixian_login_proving_et);
		verifyImg = (ImageButton) findViewById(R.id.lixian_login_proving_img);
		listVideo = (ExpandableListView) findViewById(R.id.lixian_video_list);
		listVideo.setGroupIndicator(null);
		adapter = new LixianAdapter(this, videoList);
		listVideo.setAdapter(adapter);
		txtUsrname = (TextView) findViewById(R.id.lixian_userinfo_usrname);
		txtNickname = (TextView) findViewById(R.id.lixian_userinfo_nickname);

		txtLevel = (TextView) findViewById(R.id.lixian_userinfo_level);
		txtExpiredate = (TextView) findViewById(R.id.lixian_userinfo_expiredate);

		autoTextPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String userName = autoTextUsername.getText().toString();
				if (hasFocus && userName != null && !userName.isEmpty()) {
					System.out.println("检测是否需要验证码=====================》》》》");
					MyApp.pool.execute(checkVerify);
				}
			}
		});
		verifyImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyApp.pool.execute(getVerify);
			}
		});

		listVideo.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				// 保持只有1个一级列表处于展开状态
				for (int i = 0; i < adapter.getGroupCount(); i++) {
					if (i != groupPosition) {
						listVideo.collapseGroup(i);
					}
				}
			}
		});

		listVideo.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					final int groupPosition, long id) {
				if (groupPosition < videoList.size()) {
					final XLLXFileInfo fileInfo = videoList.get(groupPosition);
					System.out.println(fileInfo.toString());
					// 是文件夹，展开二级列表
					if (fileInfo.isDir) {
						if (fileInfo.btFiles == null) {
							new Thread(new Runnable() {
								public void run() {
									XLLXFileInfo[] btSub = XLLXBiz.getSubFile(
											context, fileInfo);
									if (btSub != null) { // 获取成功
										handler.post(new Runnable() {
											public void run() {
												adapter.notifyDataSetChanged();
												listVideo
														.expandGroup(groupPosition);
												expandFlag = groupPosition;
											}
										});
									}
								}
							}).start();

						} else {
							// 已展开，就关闭
							if (expandFlag == groupPosition) {
								listVideo.collapseGroup(groupPosition);
								expandFlag = -1;
							} else {
								listVideo.expandGroup(groupPosition);
								expandFlag = groupPosition;
							}
						}

					} else {
						if (fileInfo.filesize == null
								|| fileInfo.filesize.equals("0")) {
							toast.setText(R.string.toast_xllx_transcoding);
							toast.show();
						} else {
							Intent intent = new Intent(LixianActivity.this,
									XLLXPlayer.class);
							intent.putExtra("XLLX", fileInfo);
							startActivity(intent);
						}
					}
					// 加载更多数据
				} else {
					pageIndex++;
					progressShow();
					MyApp.pool.execute(getVideoList);
				}
				return true;// false会显示重复数据
			}
		});
		listVideo.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				XLLXFileInfo fileInfo = videoList.get(groupPosition).btFiles[childPosition];
				System.out.println(fileInfo.toString());
				if (fileInfo.filesize == null || fileInfo.filesize.equals("0")) {
					toast.setText(R.string.toast_xllx_transcoding);
					toast.show();
				} else {
					Intent intent = new Intent(LixianActivity.this,
							XLLXPlayer.class);
					intent.putExtra("XLLX", fileInfo);
					startActivity(intent);
				}
				return true;
			}
		});

		btnLogin_out.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isLogin) {
					// 登陆状态 执行登出
					XLLXBiz.Logout(context);
					videoList.clear();
					adapter.notifyDataSetChanged();
					layoutLogin.setVisibility(View.VISIBLE);
					layoutUserinfo.setVisibility(View.INVISIBLE);
					setLogin(false);
					pageIndex = 1;
					isFirstLogin = true;
				} else {
					// 执行登路
					if (TextUtils
							.isEmpty(autoTextUsername.getText().toString())) {
						toast.setText(R.string.toast_xllx_in_uid);
						toast.show();
					} else if (TextUtils.isEmpty(autoTextPassword.getText()
							.toString())) {
						toast.setText(R.string.toast_xllx_in_pwd);
						toast.show();
					} else if (layoutProving.getVisibility() == View.VISIBLE
							&& TextUtils.isEmpty(verifycode.getText()
									.toString())) {
						toast.setText(R.string.toast_xllx_in_verify);
						toast.show();
					} else { // 执行登陆操作
						MyApp.pool.execute(login);
						progressShow();
					}
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private Header getCookieHeader() {
		String cookie = XLLXBiz.getCookie(context);
		if (cookie == null) {
			return null;
		}
		return new BasicHeader(XLLXBiz.COOKIE, cookie);
	}

	private void call_start_UID() {
		final EditText inputServer = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("自定义UID").setIcon(android.R.drawable.ic_dialog_info)
				.setView(inputServer).setNegativeButton("取消", null);
		builder.setPositiveButton("搜索", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String UserUID = inputServer.getText().toString();
				if (XLLXBiz.isNumeric(UserUID) && UserUID.length() > 0) {
					XLLXBiz.saveUserUID(context, UserUID);
				} else {
					XLLXBiz.saveUserUID(context, "-");
				}
				pageIndex = 1;
				videoList.clear();
				MyApp.pool.execute(getVideoList);
				Log.d("", "输入内容：" + inputServer.getText().toString());
			}
		});
		builder.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("", "keyCode=" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_7) {
			if (User_UID_CHID > 1) {
				call_start_UID();
				User_UID_CHID = 0;
				Log.d("", "启动对话框");
			}
			return true;
		} else if (keyCode == 7) {
			User_UID_CHID = User_UID_CHID + 1;
			if (User_UID_CHID > 2) {
				User_UID_CHID = 0;
			}
			return true;
		} else if (keyCode == 183 || keyCode == 185) {
			LinearLayout v = new LinearLayout(context);
			v.setOrientation(LinearLayout.HORIZONTAL);
			Dialog dialog = new Dialog(context);
			EditText et = new EditText(context);
			Button btn = new Button(context);
			btn.setText("确认");
			v.addView(et);
			v.addView(btn);
			dialog.setContentView(v);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
