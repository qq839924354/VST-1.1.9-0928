package com.vst.itv52.v1.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vst.itv52.v1.biz.WeatherBiz;
import com.vst.itv52.v1.model.CityWeatherInfoBean;
import com.vst.itv52.v1.util.HttpWorkTask;

public class WeatherReceiver extends BroadcastReceiver {

	public static final String RESPONSE_WEATHER = "com.mygica.itv52.v1.weatherservice.responseweather";

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(RESPONSE_WEATHER)) {
			if (context instanceof WeatherUpdateListener) {
				final WeatherUpdateListener listener = (WeatherUpdateListener) context;
				new HttpWorkTask<CityWeatherInfoBean>(
						new HttpWorkTask.ParseCallBack<CityWeatherInfoBean>() {

							@Override
							public CityWeatherInfoBean onParse() {
								String code = WeatherBiz.getCityCode(context);
								return WeatherBiz.getWeatherFromHttp(code);
							}
						},
						new HttpWorkTask.PostCallBack<CityWeatherInfoBean>() {

							@Override
							public void onPost(CityWeatherInfoBean result) {
								listener.updateWeather(result);
							}
						}).execute();
			}

		}
	}

	public interface WeatherUpdateListener {
		public void updateWeather(CityWeatherInfoBean bean);
	}
}
