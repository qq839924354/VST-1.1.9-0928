package com.vst.itv52.v1.biz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.CityWeatherInfoBean;

public class WeatherBiz {

	private static final String dbName = "db_weather.db";

	public static void copyWetherData(Context context) {
		byte[] buf = new byte[30720]; // 30k
		try {
			File file = context.getDatabasePath(dbName);
			FileOutputStream os = new FileOutputStream(file);// 得到数据库文件的写入流
			InputStream is = context.getAssets().open(dbName);// 得到数据库文件的数据流
			int count = -1;
			while ((count = is.read(buf)) != -1) {
				os.write(buf, 0, count);
			}
			is.close();
			os.close();
			System.out.println("copy sucess");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			buf = null;
		}
	}

	public static final String DEFAULT_CITYCODE = "101010100"; // 默认北京

	/**
	 * 获取默认的城市编号 先取偏好设置，有设置直接返回，如果没有则自动粗略定位，定位失败初始为北京
	 * 
	 * @param context
	 * @return
	 */
	public static String getCityCode(final Context context) {
		SharedPreferences sp = context.getSharedPreferences("settingSPF",
				Context.MODE_PRIVATE);
		String cityCode = sp.getString("weather_city_code", "0");
		if (cityCode.equals("0")) {
			String cityCodeAuto = getRoughlyLocation(context);
			if (cityCodeAuto != null) {
				return cityCodeAuto;
			} else {
				return DEFAULT_CITYCODE;
			}
		} else {
			return cityCode;
		}
	}

	public static CityWeatherInfoBean getWeatherFromHttp(String cityCode) {
		String url = "http://www.weather.com.cn/data/cityinfo/" + cityCode
				+ ".html";
		String json = HttpUtils.getContent(url, null, null);
		if (json != null) {
			try {
				CityWeatherInfoBean bean = new CityWeatherInfoBean();
				JSONObject jsonObject = new JSONObject(json);
				JSONObject jsonInfro = jsonObject.getJSONObject("weatherinfo");
				bean.setCityId(jsonInfro.getString("cityid"));
				bean.setCityName(jsonInfro.getString("city"));
				bean.setfTemp(jsonInfro.getString("temp1"));
				bean.settTemp(jsonInfro.getString("temp2"));
				bean.setDnstr(jsonInfro.getString("img1"));
				bean.setWeatherInfo(jsonInfro.getString("weather"));
				return bean;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取当前的粗略位置
	 * 
	 * @return
	 */
	public static String getRoughlyLocation(Context context) {
		String location[] = new String[4];
		SQLiteDatabase weatherDb;
		String url = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=";
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			JSONObject object = new JSONObject(json);
			location[0] = object.getString("country");// 国
			location[1] = object.getString("province");// 省
			location[2] = object.getString("city");// 市
			location[3] = object.getString("district");// 区
			// 将获取到的位置转化为城市编码
			weatherDb = SQLiteDatabase.openDatabase(
					context.getDatabasePath("db_weather.db").toString(), null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;// 如果JSON解析出错直接返回空
		} catch (Exception e) {
			e.printStackTrace();
			copyWetherData(context);
			weatherDb = SQLiteDatabase.openDatabase(
					context.getDatabasePath("db_weather.db").toString(), null,
					SQLiteDatabase.OPEN_READONLY);
		}
		// 查询城市编号
		Cursor cursor = null;
		if (location[3] != null && !location[3].equals("")) {
			cursor = weatherDb.query("citys", new String[] { "city_num" },
					"name=?", new String[] { location[2] + "." + location[3] },
					null, null, null);
		} else {
			cursor = weatherDb.query("citys", new String[] { "city_num" },
					"name=?", new String[] { location[2] }, null, null, null);
		}
		if (cursor.getCount() > 0 && cursor.moveToFirst()) {
			String citycode = cursor.getString(cursor
					.getColumnIndex("city_num"));
			cursor.close();
			weatherDb.close();
			return citycode;
		} else {
			return null;
		}
	}

}
