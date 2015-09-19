package com.vst.itv52.v1.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.WeatherBiz;
import com.vst.itv52.v1.broadcast.WeatherReceiver;
import com.vst.itv52.v1.broadcast.WeatherReceiver.WeatherUpdateListener;
import com.vst.itv52.v1.model.CityWeatherInfoBean;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.StringUtil;
/**
 * 天气设置
 * @author w
 *
 */
public class SettingWeather extends BaseActivity implements
		WeatherUpdateListener, OnKeyListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标

	private ImageView infoImage1, infoImage2;
	private TextView cityText, weatherInfo, tempText;

	private WeatherReceiver weatherReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_weather)).inflate();
		try {
			db = SQLiteDatabase.openDatabase(getDatabasePath("db_weather.db")
					.toString(), null, SQLiteDatabase.OPEN_READONLY);
		} catch (Exception e) {
			e.printStackTrace();
			WeatherBiz.copyWetherData(this);
			db = SQLiteDatabase.openDatabase(getDatabasePath("db_weather.db")
					.toString(), null, SQLiteDatabase.OPEN_READONLY);
		}
		provinceCursor = db.query("provinces", null, null, null, null, null,
				null);
		initView();
		weatherReceiver = new WeatherReceiver();
		registerReceiver(weatherReceiver, new IntentFilter(
				WeatherReceiver.RESPONSE_WEATHER));

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(weatherReceiver);
		if (provinceCursor != null)
			provinceCursor.close();
		if (cityCursor != null)
			cityCursor.close();
		if (db != null)
			db.close();
		super.onDestroy();
	}

	private TextView province; // 省份spinner
	private TextView city; // 城市spinner
	private CursorAdapter provinceAdapter;
	private CursorAdapter cityAdapter;
	private ListView provinceList;
	private ListView cityList;
	private int provinceSeletion;
	private int citySeletion;
	private SQLiteDatabase db;
	private Cursor provinceCursor;
	private Cursor cityCursor;
	PopupWindow popup = null;

	private void initView() {

		popup = new PopupWindow(getBaseContext());
		popup.setWidth(200);
		popup.setHeight(300);
		popup.setBackgroundDrawable(new BitmapDrawable());
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);

		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("天气设置");
		setName2.setText("天气设置");
		setItemLog.setImageResource(R.drawable.weather_setup);
		province = (TextView) findViewById(R.id.setting_weather_provice);
		city = (TextView) findViewById(R.id.setting_weather_city);

		int provinceId = getSharedPreferences("settingSPF",
				Context.MODE_PRIVATE).getInt("weather_province_id", 1);
		province.setText(getProvice(provinceId));
		int cityId = getSharedPreferences("settingSPF", Context.MODE_PRIVATE)
				.getInt("weather_city_id", 1);
		city.setText(getCity(cityId));
		cityCursor = db.query("citys", null, "province_id = ? ",
				new String[] { provinceId + "" }, null, null, null);

		provinceList = new ListView(getBaseContext());
		provinceList.setBackgroundResource(R.drawable.submenu_list_bg);
		provinceAdapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.type_details_submenu, provinceCursor,
				new String[] { "name" }, new int[] { R.id.sunmenu_text });
		provinceList.setAdapter(provinceAdapter);
		provinceList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println(id);
				String s = provinceCursor.getString(provinceCursor
						.getColumnIndex("name"));
				province.setText(s);
				provinceSeletion = position;
				if (cityCursor != null)
					cityCursor.close();
				cityCursor = db.query("citys", null, "province_id = ? ",
						new String[] { id + "" }, null, null, null);
				citySeletion = 0;
				cityAdapter.changeCursor(cityCursor);
				cityCursor.moveToPosition(citySeletion);
				city.setText(cityCursor.getString(cityCursor
						.getColumnIndex("name")));
				/* 修改配置文件 */
				SharedPreferences sp = getSharedPreferences("settingSPF",
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				int provinceId = provinceCursor.getInt(provinceCursor
						.getColumnIndex("_id"));
				editor.putInt("weather_province_id", provinceId);
				String cityCode = cityCursor.getString(cityCursor
						.getColumnIndex("city_num"));
				int cityId = cityCursor.getInt(cityCursor.getColumnIndex("_id"));
				editor.putString("weather_city_code", cityCode);
				editor.putInt("weather_city_id", cityId);
				editor.commit();
				popup.dismiss();
			}
		});

		provinceList.setOnKeyListener(this);

		cityList = new ListView(getBaseContext());
		cityList.setBackgroundResource(R.drawable.submenu_list_bg);
		cityAdapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.type_details_submenu, cityCursor,
				new String[] { "name" }, new int[] { R.id.sunmenu_text });
		cityList.setAdapter(cityAdapter);
		cityList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println(id);
				String s = cityCursor.getString(cityCursor
						.getColumnIndex("name"));
				city.setText(s);
				citySeletion = position;
				/* 修改配置文件 */
				SharedPreferences sp = getSharedPreferences("settingSPF",
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				String cityCode = cityCursor.getString(cityCursor
						.getColumnIndex("city_num"));
				int cityId = cityCursor.getInt(cityCursor.getColumnIndex("_id"));
				editor.putString("weather_city_code", cityCode);
				editor.putInt("weather_city_id", cityId);
				editor.commit();
				getApplicationContext().sendBroadcast(
						new Intent(WeatherReceiver.RESPONSE_WEATHER));
				popup.dismiss();
			}
		});

		cityList.setOnKeyListener(this);

		province.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				provinceList.setSelection(provinceSeletion);
				popup.setContentView(provinceList);
				popup.update(province, -1, -1);
				popup.showAsDropDown(province);
			}
		});

		city.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cityList.setSelection(citySeletion);
				popup.setContentView(cityList);
				popup.update(city, -1, -1);
				popup.showAsDropDown(city);
			}
		});

		infoImage1 = (ImageView) findViewById(R.id.weather_set_infoimage1);
		infoImage2 = (ImageView) findViewById(R.id.weather_set_infoimage2);
		cityText = (TextView) findViewById(R.id.citytext);
		weatherInfo = (TextView) findViewById(R.id.weatherytext);
		tempText = (TextView) findViewById(R.id.temptext);

	}

	private String getProvice(int id) {
		String name = null;
		Cursor c = db.query("provinces", null, "_id = ? ", new String[] { id
				+ "" }, null, null, null);
		if (c.moveToFirst()) {
			name = c.getString(c.getColumnIndex("name"));
		}
		c.close();
		return name;
	}

	private String getCity(int id) {
		String name = null;
		Cursor c = db.query("citys", null, "_id = ? ",
				new String[] { id + "" }, null, null, null);
		if (c.moveToFirst()) {
			name = c.getString(c.getColumnIndex("name"));
		}
		c.close();
		return name;
	}

	private void reflushWeather(CityWeatherInfoBean bean) {
		if (bean == null) {
			return;
		}

		cityText.setText(bean.getCityName());
		weatherInfo.setText(bean.getWeatherInfo());
		int[] ids = StringUtil.getWeaResByWeather(bean.getWeatherInfo());
		if (ids[0] != 0) {
			infoImage1.setVisibility(View.VISIBLE);
			infoImage1.setImageResource(ids[0]);
		} else {
			infoImage1.setVisibility(View.GONE);
		}
		if (ids[1] != 0) {
			infoImage2.setVisibility(View.VISIBLE);
			infoImage2.setImageResource(ids[1]);
		} else {
			infoImage2.setVisibility(View.GONE);
		}
		tempText.setText(bean.getfTemp() + "~" + bean.gettTemp());
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
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP
					|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				MyApp.playSound(ConstantUtil.MOVE_DOWN);
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				MyApp.playSound(ConstantUtil.COMFIRE);
			}
		}
		return false;
	}

	@Override
	public void updateWeather(CityWeatherInfoBean bean) {
		reflushWeather(bean);
	}

}
