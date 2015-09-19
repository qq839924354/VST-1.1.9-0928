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
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.UserInfo;
import com.vst.itv52.v1.util.ConfigUtil;
import com.vst.itv52.v1.util.ConstantUtil;

public class UserSetDatumChange extends BaseActivity implements
		OnFocusChangeListener, OnClickListener {
	private TextView stepName;
	private TextView intruduce;
	private UserInfo userInfo;
	private SharedPreferences share;
	private EditText inputEt;
	private TextView popTitle;
	private PopupWindow inputPop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_set_main);
		((ViewStub) findViewById(R.id.user_set_step2_info_datum)).inflate();
		share = getSharedPreferences("settingSPF", MODE_PRIVATE);
		getUserInfo();
		initDatumChangeView();
	}

	private void initDatumChangeView() {
		stepName = (TextView) findViewById(R.id.user_set_stepname);
		intruduce = (TextView) findViewById(R.id.user_set_main_step0_tv);
		stepName.setText("修改资料");

		Button sex, qq, name, address, card, phone, zip;
		sex = (Button) findViewById(R.id.user_set_info_datum_sex);
		qq = (Button) findViewById(R.id.user_set_info_datum_qq);
		name = (Button) findViewById(R.id.user_set_info_datum_name);
		address = (Button) findViewById(R.id.user_set_info_datum_add);
		card = (Button) findViewById(R.id.user_set_info_datum_card);
		phone = (Button) findViewById(R.id.user_set_info_datum_phone);
		zip = (Button) findViewById(R.id.user_set_info_datum_zip);

		sex.setOnFocusChangeListener(this);
		qq.setOnFocusChangeListener(this);
		name.setOnFocusChangeListener(this);
		address.setOnFocusChangeListener(this);
		card.setOnFocusChangeListener(this);
		phone.setOnFocusChangeListener(this);
		zip.setOnFocusChangeListener(this);

		sex.setOnClickListener(this);
		qq.setOnClickListener(this);
		name.setOnClickListener(this);
		address.setOnClickListener(this);
		card.setOnClickListener(this);
		phone.setOnClickListener(this);
		zip.setOnClickListener(this);
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
	public void onFocusChange(View v, boolean hasFocus) {
		// Editor editor = share.edit();
		switch (v.getId()) {
		case R.id.user_set_info_datum_sex:
			if (hasFocus) {
				String sex = share.getString("datum_sex", "");
				if (sex.equals("")) {
					if (userInfo != null) {
						intruduce.setText("您还未修改性别，点击可修改。不过您已在系统中登记为“"
								+ userInfo.getSex() + "性”（默认为男性）。");
					} else {
						intruduce.setText("您还未修改性别，点击可修改。");
					}
				} else {
					if (userInfo != null) {
						intruduce.setText("您即将把性别修改为“" + sex + "性”，您已在系统中登记为“"
								+ userInfo.getSex() + "性”。");
					} else {
						intruduce.setText("您即将把性别修改为“" + sex + "性”。");
					}
				}
			}
			break;
		case R.id.user_set_info_datum_qq:
			if (hasFocus) {
				String qq = share.getString("datum_qq", "0");
				if (qq.equals("0")) {
					if (userInfo != null) {
						if (userInfo.getQq().equals("0")) {
							intruduce
									.setText("您还没有修改QQ，点击可修改。您也还没有在系统中登记QQ号码，此项修改不能为空！");
						} else {
							intruduce.setText("您还没有修改QQ，点击可修改。您以前登记的QQ号码为："
									+ userInfo.getQq() + "。");
						}
					} else {
						intruduce.setText("您还没有修改QQ，点击可修改。此项修改不能为空！");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getQq().equals("0")) {
							intruduce.setText("您即将在系统中登记的QQ号码为：" + qq + "。");
						} else {
							intruduce.setText("您即将把QQ号码修改为：" + qq
									+ "，您以前登记的QQ号码为：" + userInfo.getQq() + "。");
						}
					} else {
						intruduce.setText("您即将把QQ号码修改为：" + qq + "。");
					}
				}
			}
			break;
		case R.id.user_set_info_datum_name:
			if (hasFocus) {
				String name = share.getString("datum_name", "");
				if (name.equals("")) {
					if (userInfo != null) {
						if (userInfo.getName().equals("-")) {
							intruduce
									.setText("您还没有修改姓名，点击可修改。您也还没有在系统中登记姓名。此项修改不能为空！");
						} else {
							intruduce.setText(userInfo.getName()
									+ "，您还没有修改姓名，点击可修改。");
						}
					} else {
						intruduce.setText("您还没有修改姓名，点击可修改。此项修改不能为空！");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getName().equals("-")) {
							intruduce.setText("您即将在系统中登记姓名为：" + name + "。");
						} else {
							intruduce.setText(userInfo.getName()
									+ "，您即将把姓名修改为：" + name + "。");
						}
					} else {
						intruduce.setText("您即将把姓名修改为：" + name + "。");
					}
				}
			}
			break;
		case R.id.user_set_info_datum_add:
			if (hasFocus) {
				String address = share.getString("datum_address", "");
				if (address.equals("")) {
					if (userInfo != null) {
						if (userInfo.getAddress().equals("-")) {
							intruduce.setText("您还没有修改地址，点击可修改。您也还没有在系统中登记地址。");
						} else {
							intruduce.setText("您还没有修改地址，点击可修改。您以前登记的地址是："
									+ userInfo.getAddress());
						}
					} else {
						intruduce.setText("您还没有修改地址，点击可修改。");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getAddress().equals("-")) {
							intruduce.setText("您即将在系统中登记的地址为：" + address + "。");
						} else {
							intruduce.setText("您即将把地址修改为：" + address
									+ "，您以前登记的地址是：" + userInfo.getAddress());
						}
					}
				}
			}
			break;
		case R.id.user_set_info_datum_card:
			if (hasFocus) {
				String card = share.getString("datum_card", "");
				if (card.equals("")) {
					if (userInfo != null) {
						if (userInfo.getCard().equals("0")) {
							intruduce
									.setText("您还没有修改身份证号码，点击可修改。您也还没有在系统中登记身份证。");
						} else {
							intruduce.setText("您还没有修改身份证号码，点击可修改。您以前登记的身份证号码是："
									+ userInfo.getCard());
						}
					} else {
						intruduce.setText("您还没有修改身份证号码，点击可修改。");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getCard().equals("0")) {
							intruduce.setText("您即将在系统中登记的身份证号码为：" + card + "。");
						} else {
							intruduce.setText("您即将把身份证号码修改为：" + card
									+ "，您以前登记的身份证号码是：" + userInfo.getCard());
						}
					}
				}
			}
			break;
		case R.id.user_set_info_datum_phone:
			if (hasFocus) {
				String phone = share.getString("datum_phone", "");
				if (phone.equals("")) {
					if (userInfo != null) {
						if (userInfo.getTel().equals("0")) {
							intruduce
									.setText("您还没有修改电话号码，点击可修改。您也还没有在系统中登记电话号码。");
						} else {
							intruduce.setText("您还没有修改电话号码，点击可修改。您以前登记的电话号码是："
									+ userInfo.getTel());
						}
					} else {
						intruduce.setText("您还没有修改电话号码，点击可修改。");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getTel().equals("0")) {
							intruduce.setText("您即将在系统中登记的电话号码为：" + phone + "。");
						} else {
							intruduce.setText("您即将把电话号码修改为：" + phone
									+ "，您以前登记的电话号码是：" + userInfo.getTel());
						}
					}
				}
			}
			break;
		case R.id.user_set_info_datum_zip:
			if (hasFocus) {
				String zip = share.getString("datum_zip", "");
				if (zip.equals("")) {
					if (userInfo != null) {
						if (userInfo.getZip().equals("0")) {
							intruduce
									.setText("您还没有修改邮政编码，点击可修改。您也还没有在系统中登记邮政编码。");
						} else {
							intruduce.setText("您还没有修改邮政编码，点击可修改。您以前登记的邮政编码是："
									+ userInfo.getZip());
						}
					} else {
						intruduce.setText("您还没有修改邮政编码，点击可修改。");
					}
				} else {
					if (userInfo != null) {
						if (userInfo.getZip().equals("0")) {
							intruduce.setText("您即将在系统中登记的邮政编码为：" + zip + "。");
						} else {
							intruduce.setText("您即将把邮政编码修改为：" + zip
									+ "，您以前登记的邮政编码是：" + userInfo.getZip());
						}
					}
				}
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// Intent intent = null;
		switch (v.getId()) {
		case R.id.user_set_info_datum_sex:

			break;
		case R.id.user_set_info_datum_qq:
			// createInputDialog("输入QQ号码", ConstantUtil.NUMBER_KEYBOARD);
			// inputPop.showAtLocation(v, Gravity.CENTER, 0, 0);
			// intent = new Intent(this, InputForNumber.class);
			// intent.putExtra("title", "输入QQ号码");
			// startActivity(intent);
			break;
		case R.id.user_set_info_datum_name:

			break;
		case R.id.user_set_info_datum_add:

			break;
		case R.id.user_set_info_datum_card:

			break;
		case R.id.user_set_info_datum_phone:

			break;
		case R.id.user_set_info_datum_zip:

			break;
		}
	}

	private void createInputDialog(String diaTitle, int flag) {
		View view = null;
		if (flag == ConstantUtil.NUMBER_KEYBOARD) {
			view = LayoutInflater.from(this).inflate(R.layout.input_number,
					null);
			popTitle = (TextView) view.findViewById(R.id.input_dialog_title);
			popTitle.setText(diaTitle);
			inputEt = (EditText) view.findViewById(R.id.input_number_et);
			final KeyboardView keyview = (KeyboardView) view
					.findViewById(R.id.keyboard_view_number);
			inputEt.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// new KeyBoardFor52ITV(UserSetDatumChange.this, inputEt,
					// ConstantUtil.NUMBER_KEYBOARD, keyview)
					// .showKeyboard();
					return false;
				}
			});
		}
		inputPop = new PopupWindow();
		inputPop.setFocusable(true);
		inputPop.setHeight(350);
		inputPop.setWidth(350);
		inputPop.setBackgroundDrawable(new BitmapDrawable());
		inputPop.setContentView(view);
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
