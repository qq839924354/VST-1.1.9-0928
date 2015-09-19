package com.vst.itv52.v1.activity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.model.UserInfo;
import com.vst.itv52.v1.util.ConfigUtil;
import com.vst.itv52.v1.util.ConstantUtil;

public class UserSetPersonalInfo extends BaseActivity implements
		OnFocusChangeListener {
	private TextView stepName;
	private TextView intruduce;
	private UserInfo userInfo;
	private SharedPreferences share;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_set_main);
		((ViewStub) findViewById(R.id.user_set_step2_info_person)).inflate();
		share = getSharedPreferences("settingSPF", MODE_PRIVATE);
		getUserInfo();
		initPersonalInfoView();
	}

	private void initPersonalInfoView() {
		stepName = (TextView) findViewById(R.id.user_set_stepname);
		intruduce = (TextView) findViewById(R.id.user_set_main_step0_tv);
		stepName.setText("个人信息");

		Button personId, email, name, qq, point, regTime, login, thisAdd, lastAdd;
		personId = (Button) findViewById(R.id.user_set_info_person_id);
		email = (Button) findViewById(R.id.user_set_info_person_email);
		name = (Button) findViewById(R.id.user_set_info_person_nickname);
		qq = (Button) findViewById(R.id.user_set_info_person_qq);
		point = (Button) findViewById(R.id.user_set_info_person_point);
		regTime = (Button) findViewById(R.id.user_set_info_person_reg);
		login = (Button) findViewById(R.id.user_set_info_person_login);
		thisAdd = (Button) findViewById(R.id.user_set_info_person_this);
		lastAdd = (Button) findViewById(R.id.user_set_info_person_last);

		personId.setOnFocusChangeListener(this);
		email.setOnFocusChangeListener(this);
		name.setOnFocusChangeListener(this);
		qq.setOnFocusChangeListener(this);
		point.setOnFocusChangeListener(this);
		regTime.setOnFocusChangeListener(this);
		login.setOnFocusChangeListener(this);
		thisAdd.setOnFocusChangeListener(this);
		lastAdd.setOnFocusChangeListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		MyApp.playSound(ConstantUtil.MOVE_DOWN);
		switch (v.getId()) {
		case R.id.user_set_info_person_id:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您当前已登录账号：" + userInfo.getUserid()
							+ "，数字账号：" + userInfo.getMyuid());
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_email:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您登记的邮箱：" + userInfo.getEmail());
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_nickname:
			if (hasFocus) {
				if (userInfo != null) {
					if (userInfo.getName().equals("-")) {
						intruduce.setText("您尚未登记你的姓名，建议您去“修改资料”页面完善信息。");
					} else {
						intruduce.setText("您登记的姓名：" + userInfo.getName());
					}
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_qq:
			if (hasFocus) {
				if (userInfo != null) {
					if (userInfo.getQq().equals("0")) {
						intruduce.setText("您尚未登记QQ号码，建议您去“修改资料”页面完善信息。");
					} else {
						intruduce.setText("您登记的QQ号码为：" + userInfo.getQq());
					}
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_point:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您当前拥有 " + userInfo.getPoint() + " 积分。");
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_reg:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("从  " + userInfo.getRegnow()
							+ " 起，您就是本系统的用户啦！");
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_login:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您最近一次登录的时间是：" + userInfo.getLogintime1()
							+ "，然后上一次是： " + userInfo.getLogintime2()
							+ "，我没有帮你记错吧！");
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_this:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您" + userInfo.getLogin_text1()
							+ "，大致位置没有弄错吧。");
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		case R.id.user_set_info_person_last:
			if (hasFocus) {
				if (userInfo != null) {
					intruduce.setText("您" + userInfo.getLogin_text2()
							+ "，我想是不会记错。");
				} else {
					intruduce.setText("正在从服务器上获取您的个人用户信息，请稍候……");
				}
			}
			break;
		}
	}

	private void getUserInfo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				userInfo = parseUserData(ConfigUtil.getValue("USER_INFO_LINK"),
						share.getString("login_key", ""));
			}
		}).start();
	}

	/**
	 * 获取用户个人信息
	 * 
	 * @param url
	 *            链接地址
	 * @param key
	 *            登陆成功后返回的key
	 * @return
	 */
	public static UserInfo parseUserData(String url, String key) {
		UserInfo info = null;
		if (url.toLowerCase().startsWith("http://")) {
			InputStream input = null;
			BasicHttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000); // 连接超时
			HttpConnectionParams.setSoTimeout(httpParameters, 20000); // 超时设置
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler()); // 设置重试3
			try {
				HttpGet get = new HttpGet(url.trim());
				get.setHeader("Cookie", "key=" + key);
				HttpResponse response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					info = new UserInfo();
					input = response.getEntity().getContent();
					SAXReader reader = new SAXReader();
					Document document = reader.read(input);
					Element root = document.getRootElement();
					info.setMyuid(root.elementText("myuid"));
					info.setUserid(root.elementText("userid"));
					info.setPassword(root.elementText("password"));
					info.setPoint(root.elementText("point"));
					info.setMoney(root.elementText("money"));
					info.setEmail(root.elementText("email"));
					info.setName(root.elementText("name"));
					info.setSex(root.elementText("sex"));
					info.setQq(root.elementText("qq"));
					info.setTel(root.elementText("tel"));
					info.setZip(root.elementText("zip"));
					info.setCard(root.elementText("card"));
					info.setAddress(root.elementText("address"));
					info.setLogin_cs(root.elementText("login_cs"));
					info.setLoginip1(root.elementText("loginip1"));
					info.setLoginip2(root.elementText("loginip2"));
					info.setLogintime1(root.elementText("logintime1"));
					info.setLogintime2(root.elementText("logintime2"));
					info.setLogin_text1(root.elementText("login_text1"));
					info.setLogin_text2(root.elementText("login_text2"));
					info.setRegnow(root.elementText("regnow"));
					return info;
				}
			} catch (DocumentException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				client.getConnectionManager().shutdown();
			}
		}
		return null;
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
