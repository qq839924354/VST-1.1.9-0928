package com.vst.itv52.v1.biz;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.NewsBean;

public class NewsBiz {

	public static ArrayList<NewsBean> parseNews(String url) {
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);

		ArrayList<NewsBean> newsBeans = new ArrayList<NewsBean>();
		try {
			JSONObject object = new JSONObject(json);
			JSONArray date = object.getJSONArray("video");
			String playUrl = object.getString("playurl");
			for (int i = 0; i < date.length(); i++) {
				JSONObject object2 = (JSONObject) date.opt(i);
				NewsBean bean = new NewsBean();
				bean.id = object2.getString("id");
				bean.title = object2.getString("title");
				bean.img = object2.getString("img");
				bean.playurl = playUrl;
				newsBeans.add(bean);
			}
			return newsBeans;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
