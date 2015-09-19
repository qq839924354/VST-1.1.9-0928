package com.vst.itv52.v1.biz;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.ChannelColumnBean;
import com.vst.itv52.v1.model.ChannelColumns;
import com.vst.itv52.v1.model.TVBackChannelJavabean;

public class TvBackBiz {

	/**
	 * 解析频道列表
	 * 
	 * @param url
	 * @return
	 */
	public static TVBackChannelJavabean praseChannelList(String url) {
		// url = "http://v.52itv.cn/vst_cn/tvback.php";//http://livecdn.91vst.com/tvback.php
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);

		TVBackChannelJavabean channelbean = new TVBackChannelJavabean();
		// ObjectMapper mapper = new ObjectMapper();
		// JsonNode rootNode = mapper.readTree(json);
		// JsonNode type = rootNode.path("datalist");

		try {
			JSONObject object = new JSONObject(json);
			JSONArray date = object.getJSONArray("datelist");
			Map<String, String> dateMap = new LinkedHashMap<String, String>();
			for (int i = 0; i < date.length(); i++) {
				JSONObject object2 = (JSONObject) date.opt(i);
				dateMap.put(object2.getString("name"),
						object2.getString("date"));
			}
			channelbean.setDateMap(dateMap);
			JSONArray channel = object.getJSONArray("tvback");
			Map<String, String> tvMap = new LinkedHashMap<String, String>();
			for (int j = 0; j < channel.length(); j++) {
				JSONObject object3 = (JSONObject) channel.opt(j);
				tvMap.put(object3.getString("vid"),
						object3.getString("channel"));
			}
			channelbean.setTvMap(tvMap);
			////////////////////////////////////////0613
			//channelbean.setNexturl(object.getString("nexturl"));

			return channelbean;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析栏目列表
	 * 
	 * @param url
	 * @param vid
	 * @param date
	 * @return
	 */
	public static ChannelColumns parseColumns(String url,
			Map<String, String> map) {
		// String url = "http://v.52itv.cn/vst_cn/tvback.php";
		// NameValuePair[] pairs = new NameValuePair[2];
		// pairs[0] = new BasicNameValuePair("vid", vid);
		// pairs[1] = new BasicNameValuePair("date", date);
//		url += ".json";
//		System.out.println("back url = "+ url);
		//url = 	MyApp.e(url);
		NameValuePair[] pairs = HttpClientHelper.mapToPairs(map);
		String json = HttpUtils.getContent(url, null, pairs);
		if (json == null) {
			return null;
		}
//		System.out.println(json);
		try {
			ChannelColumns columns = new ChannelColumns();
			JSONObject object = new JSONObject(json);
			columns.setTitle(object.getString("title"));
			columns.setPlaydate(object.getString("playdate"));
			columns.setLiveurl(object.getString("liveurl"));
			JSONArray columnList = object.getJSONArray("tvback");
			ArrayList<ChannelColumnBean> columnBeans = new ArrayList<ChannelColumnBean>();
			for (int i = 0; i < columnList.length(); i++) {
				ChannelColumnBean bean = new ChannelColumnBean();
				JSONObject object2 = (JSONObject) columnList.opt(i);
				bean.setTime(object2.getString("time"));
				bean.setChannelText(object2.getString("name"));
				bean.setUrl(object2.getString("link"));
				columnBeans.add(bean);
			}
			columns.setList(columnBeans);
			
			
			
			//columns.setNexturl(object.getString("playurl"));
			return columns;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList parseMP4url(String url) {
		// url = "http://v.52itv.cn/vst_cn/tvback.php";
		// NameValuePair[] pairs = new NameValuePair[1];
		// pairs[0] = new BasicNameValuePair("link", null);
	//url = 	MyApp.e(url);
		url = "http://livecdn.91vst.com/tvback.php/?link=" + url;
		String json = HttpUtils.getContent(url, null, null);
		if (json == null) {
			return null;
		}
//		System.out.println(json);  {"play":[{"dur":"0","url":"http://url.52itv.cn/vlive/pptv/anhui.m3u8?start=1434144660&end=1434148200&k=7d575a51397c499ad13c6f422411c300-1434199164"}]}
		try {
			JSONObject object = new JSONObject(json);
			JSONArray playlist = object.getJSONArray("play");
			Uri[] mp4Uri = new Uri[playlist.length()];
			int[] mp4Dur = new int[playlist.length()];
			for (int i = 0; i < playlist.length(); i++) {
				JSONObject playlink = (JSONObject) playlist.opt(i);
				mp4Dur[i] = Integer.parseInt(playlink.getString("dur"));
				mp4Uri[i] = Uri.parse(playlink.getString("url"));
			}
			ArrayList list = new ArrayList();
			list.add(mp4Uri);
			list.add(mp4Dur);
			return list;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * {"datelist":[{"name":"\u661f\u671f\u516d 06\/13","date":"2015-06-13"},{"name":"\u661f\u671f\u4e94 06\/12","date":"2015-06-12"},{"name":"\u661f\u671f\u56db 06\/11","date":"2015-06-11"},{"name":"\u661f\u671f\u4e09 06\/10","date":"2015-06-10"},{"name":"\u661f\u671f\u4e8c 06\/09","date":"2015-06-09"},{"name":"\u661f\u671f\u4e00 06\/08","date":"2015-06-08"},{"name":"\u661f\u671f\u65e5 06\/07","date":"2015-06-07"}],"tvback":[{"vid":"10001","channel":"\u4e2d\u592e\u4e00\u53f0"},{"vid":"10002","channel":"\u4e2d\u592e\u4e8c\u53f0"},{"vid":"10003","channel":"\u4e2d\u592e\u4e09\u53f0"},{"vid":"10004","channel":"\u4e2d\u592e\u56db\u53f0"},{"vid":"10005","channel":"\u4e2d\u592e\u4e94\u53f0"},{"vid":"10007","channel":"\u4e2d\u592e\u4e03\u53f0"},{"vid":"10008","channel":"\u4e2d\u592e\u516b\u53f0"},{"vid":"10010","channel":"\u4e2d\u592e\u5341\u53f0"},{"vid":"10011","channel":"\u4e2d\u592e\u5341\u4e00"},{"vid":"10012","channel":"\u4e2d\u592e\u5341\u4e8c"},{"vid":"10013","channel":"\u4e2d\u592e\u65b0\u95fb"},{"vid":"10014","channel":"\u4e2d\u592e\u5c11\u513f"},{"vid":"10015","channel":"\u4e2d\u592e\u97f3\u4e50"},{"vid":"10043","channel":"\u6e56\u5357\u536b\u89c6"},{"vid":"10044","channel":"\u6c5f\u82cf\u536b\u89c6"},{"vid":"10054","channel":"\u5e7f\u4e1c\u536b\u89c6"},{"vid":"10079","channel":"\u6df1\u5733\u536b\u89c6"},{"vid":"10047","channel":"\u5c71\u4e1c\u536b\u89c6"},{"vid":"10057","channel":"\u6d59\u6c5f\u536b\u89c6"},{"vid":"10045","channel":"\u6e56\u5317\u536b\u89c6"},{"vid":"10061","channel":"\u5317\u4eac\u536b\u89c6"},{"vid":"10064","channel":"\u4e1c\u65b9\u536b\u89c6"},{"vid":"10069","channel":"\u5929\u6d25\u536b\u89c6"},{"vid":"10053","channel":"\u9ed1\u9f99\u6c5f\u536b\u89c6"},{"vid":"10058","channel":"\u6cb3\u5317\u536b\u89c6"},{"vid":"10062","channel":"\u5b89\u5fbd\u536b\u89c6"},{"vid":"10066","channel":"\u8fbd\u5b81\u536b\u89c6"},{"vid":"10586","channel":"\u91cd\u5e86\u536b\u89c6"},{"vid":"10056","channel":"\u5e7f\u897f\u536b\u89c6"},{"vid":"10046","channel":"\u4e1c\u5357\u536b\u89c6"},{"vid":"10051","channel":"\u5c71\u897f\u536b\u89c6"},{"vid":"10059","channel":"\u8d35\u5dde\u536b\u89c6"},{"vid":"10060","channel":"\u6cb3\u5357\u536b\u89c6"},{"vid":"10049","channel":"\u9752\u6d77\u536b\u89c6"},{"vid":"10063","channel":"\u56db\u5ddd\u536b\u89c6"},{"vid":"10065","channel":"\u6c5f\u897f\u536b\u89c6"},{"vid":"10048","channel":"\u7518\u8083\u536b\u89c6"},{"vid":"10067","channel":"\u5409\u6797\u536b\u89c6"},{"vid":"10075","channel":"\u9655\u897f\u536b\u89c6"},{"vid":"10050","channel":"\u5b81\u590f\u536b\u89c6"},{"vid":"10080","channel":"\u65c5\u6e38\u536b\u89c6"},{"vid":"10070","channel":"\u4e91\u5357\u536b\u89c6"},{"vid":"10071","channel":"\u5185\u8499\u53e4\u536b\u89c6"},{"vid":"10073","channel":"\u897f\u85cf\u536b\u89c6"},{"vid":"10072","channel":"\u65b0\u7586\u536b\u89c6"},{"vid":"10055","channel":"\u73e0\u6c5f\u9891\u9053"},{"vid":"10074","channel":"\u5357\u65b9\u536b\u89c6"},{"vid":"10052","channel":"\u5175\u56e2\u536b\u89c6"},{"vid":"10076","channel":"\u53a6\u95e8\u536b\u89c6"},{"vid":"10077","channel":"\u6d77\u5ce1\u536b\u89c6"},{"vid":"10137","channel":"\u7b2c\u4e00\u8d22\u7ecf"},{"vid":"10408","channel":"\u6cb3\u5317\u5f71\u89c6"},{"vid":"10068","channel":"VST\u7535\u5f71\u53f0"}]}
	 */
}
