package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.vst.itv52.v1.R.string;
import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpResult;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.SharpnessEnum;
import com.vst.itv52.v1.model.VideoPlayUrl;
import com.vst.itv52.v1.model.XLLXFileInfo;
import com.vst.itv52.v1.model.XLLXUserInfo;
import com.vst.itv52.v1.srt.LXSRT;
import com.vst.itv52.v1.util.MD5Util;

public class XLLXBiz {

	private static final String TAG = "XunleiBiz";
	private static final String LOGIN_URL = "http://login.xunlei.com/sec2login/";
	private static final String VER_CODE_URL = "http://login.xunlei.com/check";
	public static final String PARSE_URL = "http://i.vod.xunlei.com/req_get_method_vod";
	public static final String LIST_URL = "http://i.vod.xunlei.com/req_history_play_list/req_num/30/req_offset/0";
	public static final String USERINFO_URL = "http://dynamic.vip.xunlei.com/login/asynlogin_contr/asynProxy/";
	public static final String LOGOUT_URL = "http://login.xunlei.com/unregister";
	private static final String CHECK_RESULT = "check_result";
	public static final String REFERER = "http://vod.xunlei.com/client/cplayer.html";
	public static final String SESSIONID = "sessionid";
	public static final String USERID = "userid";
	public static final String COOKIE = "Cookie";
	public static final String USERKEY = "xllx_userKey";
	public static final String USERPWD = "xllx_pwd";
	public static final String DEFAULT_UserID = "xllx_userID";
	public static final String DEFAULT_VERIFY = "xllx_verify";
	public static final String COOKIE_URL = "xunlei.com";
	public static final String XL_PREFERENCES = "xl";

	/**
	 * 获取XL的 登陆的 cookie 信息
	 * 
	 * cookie 信息 需要保存 通过 cookie 得到用户信息
	 * 
	 * @param user
	 * @param pwd
	 * @return
	 */

	/**
	 * login_flag <br>
	 * "登录成功", 0 <br>
	 * "验证码错误", 1 <br>
	 * "密码错误", 2 <br>
	 * "服务器忙", 3 <br>
	 * "帐号不存在", 4 <br>
	 * "帐号不存在", 5 <br>
	 * "帐号被锁定", 6 <br>
	 * "服务器忙", 7 <br>
	 * "服务器忙", 8 <br>
	 * "非法验证码", 9 <br>
	 * "非法验证码", 10<br>
	 * "验证码超时", 11 <br>
	 * "登录页面无效1", 12 <br>
	 * "登录页面无效2" 13 <br>
	 * "登录页面无效3", 14<br>
	 * "登录页面无效4", 15 <br>
	 * "网络超时，请重新登录"16<br>
	 * 
	 * @param cookieStore
	 * @return
	 */
	private static int getLoginFlag(Context context, Cookie[] cookies) {
		int flag = -1;
		String sessionid = null;
		String uid = null;
		String usrname = null;
		if (cookies != null && cookies.length > 0) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equalsIgnoreCase("blogresult")) {
					flag = Integer.parseInt(cookies[i].getValue());
				} else if (cookies[i].getName().equalsIgnoreCase("sessionid")) {
					sessionid = cookies[i].getValue();
				} else if (cookies[i].getName().equalsIgnoreCase("userid")) {
					uid = cookies[i].getValue();
				} else if (cookies[i].getName().equalsIgnoreCase("usrname")) {
					usrname = cookies[i].getValue();
				}
			}
			if (flag == 0) {
				// 成功登陆 保存cookie信息 UID
				saveUsrname(context, usrname);
				saveSessionid(context, sessionid);
				saveUID(context, uid);
				saveCookies(context, cookies);
			}
		}
		return flag;
	}

	/**
	 * 登陆成功 保存 UID
	 * 
	 * @param context
	 * @param uid
	 */
	private static void saveUID(Context context, String uid) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USERID, uid);
		editor.commit();
	}

	/**
	 * 获取 UID
	 * 
	 * @param context
	 * @return
	 */
	public static String getUID(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(USERID, null);
	}

	private static final String USRNAME = "usrnmae";

	private static void saveUsrname(Context context, String usrname) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USRNAME, usrname);
		editor.commit();
	}

	public static String getUsrname(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(USRNAME, null);
	}

	private static void saveUserPWD(Context context, String usrpass) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USERPWD, MD5Util.getMD5String(usrpass));
		editor.commit();
	}

	public static String getUserPWD(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(USERPWD, null);
	}

	private static void saveUserKey(Context context, String usrkey) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(USERKEY, usrkey);
		editor.commit();
	}

	public static String getUserKey(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(USERKEY, null);
	}

	private static void saveDefaultVerify(Context context, String verify) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(DEFAULT_VERIFY, verify);
		editor.commit();
	}

	public static String getDefaultVerify(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(DEFAULT_VERIFY, null);
	}

	public static void saveUserUID(Context context, String UserID) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(DEFAULT_UserID, UserID);
		editor.commit();
	}

	public static String getUserUID(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(DEFAULT_UserID, null);
	}

	/**
	 * 保存 sessionid
	 * 
	 * @param context
	 * @param cookie
	 */
	private static void saveSessionid(Context context, String sessionid) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(SESSIONID, sessionid);
		editor.commit();
	}

	/**
	 * 获取 sessionid
	 * 
	 * @param context
	 * @return
	 */

	public static String getSessionid(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(SESSIONID, null);
	}

	/**
	 * 保存cookie
	 * 
	 * @param context
	 * @param cookies
	 */
	public static void saveCookies(Context context, Cookie... cookies) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		String cookieString = null;
		if (cookies != null && cookies.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < cookies.length; i++) {
				if (i > 0) {
					sb.append(";");
				}
				sb.append(String.format("%s=%s", cookies[i].getName(),
						cookies[i].getValue()));
			}
			cookieString = sb.toString();
		}

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(COOKIE, cookieString);
		editor.commit();
	}

	/**
	 * 获取 cookie 信息
	 * 
	 * @param context
	 * @return
	 */
	public static String getCookie(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		return preferences.getString(COOKIE, null);
	}

	/**
	 * 新用户登陆
	 * 
	 * @param user
	 * @param pwd
	 * @return CookieStore cookie 信息
	 */
	public static int Login(Context context, String user, String pwd,
			String verifycode) {
		saveUserUID(context, "-");
		saveUsrname(context, user);
		saveUserPWD(context, pwd);
		HttpResult loginResult = null;
		if (verifycode == null || verifycode.isEmpty()) {
			verifycode = getDefaultVerify(context);
		}
		Header[] headers = new Header[] { new BasicHeader(XLLXBiz.COOKIE,
				"VERIFY_KEY=" + getUserKey(context)) };
		BasicNameValuePair pwdvaluePair;
		if (pwd.length() == 32) {
			pwdvaluePair = new BasicNameValuePair("p",
					MD5Util.getMD5String(MD5Util.getMD5String(pwd))
							+ verifycode.toUpperCase());
		} else {
			pwdvaluePair = new BasicNameValuePair("p",
					MD5Util.getMD5String(MD5Util.getMD5String(MD5Util
							.getMD5String(pwd)) + verifycode.toUpperCase()));
		}
		loginResult = HttpClientHelper.post(LOGIN_URL, headers,
				new NameValuePair[] { new BasicNameValuePair("u", user),
						new BasicNameValuePair("login_enable", "1"),
						new BasicNameValuePair("login_hour", "720"),
						pwdvaluePair,
						new BasicNameValuePair("verifycode", verifycode) },
				null);
		return getLoginFlag(context, loginResult.getCookies());
	}

	/**
	 * 检测账号是否需要验证码
	 * 
	 * @param context
	 * @param user
	 * @return 0，不需要 1，需要
	 */
	public static int checkVerify(Context context, String user) {
		HttpResult verResult = HttpClientHelper.get(
				VER_CODE_URL,
				null,
				new NameValuePair[] {
						new BasicNameValuePair("u", user),
						new BasicNameValuePair("cachetime", String
								.valueOf(System.currentTimeMillis())) });
		Cookie verCodeCookie = verResult.getCookie(CHECK_RESULT);
		String checkResult = verCodeCookie.getValue();
		System.out.println("CHECK_RESULT：" + checkResult);
		saveDefaultVerify(context,
				checkResult.substring(checkResult.indexOf(":") + 1));
		if (checkResult.charAt(0) == '0') {
			String VERIFY_KEY = verResult.getCookie("VERIFY_KEY").getValue();
			saveUserKey(context, VERIFY_KEY);
			return 0;
		}
		return 1;
	}

	public static Bitmap getVerify(Context context) {
		String url = "http://verify.xunlei.com/image?cachetime="
				+ System.currentTimeMillis();
		System.out.println("获取验证码===>>>" + url);
		HttpResult result = HttpClientHelper.get(url);
		byte[] data = null;
		if (result != null && result.getStatuCode() == HttpStatus.SC_OK) {
			Cookie cookie = result.getCookie("VERIFY_KEY");
			data = result.getResponse();
			if (cookie == null) {
				return null;
			}
			String value = cookie.getValue();
			System.out.println(value);
			saveUserKey(context, value);
		}
		Bitmap bm = null;
		if (data != null) {
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			bm = BitmapFactory.decodeStream(is);
		}
		return bm;
	}

	/**
	 * 注销 删除 cookie 信息
	 * 
	 * @param sessionid
	 * @return
	 */
	public static void Logout(Context context) {
		// 清除 配置 信息
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(USERID);
		editor.remove(SESSIONID);
		editor.remove(COOKIE);
		editor.remove(USRNAME);
		editor.remove(USERPWD);
		editor.remove("autopay");
		editor.remove("daily");
		editor.remove("expiredate");
		editor.remove("isvip");
		editor.remove("level");
		editor.remove("nickname");
		editor.remove("usrname");
		editor.remove("payname");
		editor.commit();
	}

	/**
	 * 获取 用户 信息
	 * 
	 * @param url
	 * @param cookieStore
	 * @return
	 */
	public static XLLXUserInfo getUser(Context context, Header cookies) {
		String content = HttpUtils.getContent(USERINFO_URL,
				new Header[] { cookies }, null);
		if (content != null) {
			try {
				String json = content.substring(content.indexOf("{"));
				XLLXUserInfo info = new XLLXUserInfo();
				JSONTokener jsonParser = new JSONTokener(json);
				JSONObject object = (JSONObject) jsonParser.nextValue();
				info.autopay = Integer.parseInt(object.getString("autopay"));
				info.daily = Integer.parseInt(object.getString("daily"));
				info.expiredate = object.getString("expiredate");
				info.growvalue = Integer
						.parseInt(object.getString("growvalue"));
				info.isvip = Integer.parseInt(object.getString("isvip"));
				info.isyear = Integer.parseInt(object.getString("isvip"));
				info.level = Integer.parseInt(object.getString("level"));
				info.nickname = object.getString("nickname");
				info.usrname = object.getString("usrname");
				info.payname = object.getString("payname");
				saveUserInfo(context, info);
				return info;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;

	}

	private static void saveUserInfo(Context context, XLLXUserInfo info) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("autopay", info.autopay);
		editor.putInt("daily", info.daily);
		editor.putString("expiredate", info.expiredate);
		editor.putInt("isvip", info.isvip);
		editor.putInt("level", info.level);
		editor.putString("nickname", info.nickname);
		editor.putString("usrname", info.usrname);
		editor.putString("payname", info.payname);
		editor.commit();
	}

	public static XLLXUserInfo getUserInfoFromLocal(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				XL_PREFERENCES, Context.MODE_PRIVATE);
		XLLXUserInfo info = new XLLXUserInfo();
		info.autopay = preferences.getInt("autopay", -1);
		info.daily = preferences.getInt("daily", -1);
		info.expiredate = preferences.getString("expiredate", "");
		info.growvalue = preferences.getInt("growvalue", -1);
		info.isvip = preferences.getInt("isvip", -1);
		info.isyear = preferences.getInt("isvip", -1);
		info.level = preferences.getInt("level", -1);
		info.nickname = preferences.getString("nickname", "");
		info.usrname = preferences.getString("usrname", "");
		info.payname = preferences.getString("payname", "");
		return info;
	}

	public static XLLXFileInfo[] getSubFile(Context context, XLLXFileInfo btdir) {
		String CookieUID = XLLXBiz.getUserUID(context);
		if (isNumeric(CookieUID) && CookieUID != "-") {
			CookieUID = "userid=" + CookieUID + ";";
		} else {
			CookieUID = "userid=" + XLLXBiz.getUID(context) + "; sessionid="
					+ XLLXBiz.getSessionid(context);
		}
		Log.d(TAG, "CookieUID= " + CookieUID);
		Header cookie = new BasicHeader(XLLXBiz.COOKIE, CookieUID);
		try {
			String suburl = "http://i.vod.xunlei.com/req_subBT/info_hash/"
					+ btdir.src_url.substring(5) + "/req_num/30/req_offset/0";
			String btjson = HttpUtils.getContent(suburl,
					new Header[] { cookie }, null);
			btjson = URLDecoder.decode(btjson);
			JSONTokener btjsonParser = new JSONTokener(btjson);
			JSONObject btobject = (JSONObject) btjsonParser.nextValue();
			JSONObject btresp = btobject.getJSONObject("resp");
			JSONArray btsubfile_list = btresp.getJSONArray("subfile_list");
			XLLXFileInfo[] btFiles = new XLLXFileInfo[btsubfile_list.length()];
			for (int j = 0; j < btsubfile_list.length(); j++) {
				JSONObject btsubfile = btsubfile_list.getJSONObject(j);
				Log.d(TAG, "btsubfile = " + btsubfile);
				btFiles[j] = new XLLXFileInfo();
				btFiles[j].file_name = btsubfile.getString("name");
				btFiles[j].src_url = btdir.src_url + "/"
						+ btsubfile.getString("index");
				btFiles[j].createTime = btdir.createTime;
				btFiles[j].duration = btsubfile.getString("duration");
				btFiles[j].filesize = btsubfile.getString("file_size");
				btFiles[j].gcid = btsubfile.getString("gcid");
				btFiles[j].userid = btdir.userid;
			}
			btdir.btFiles = btFiles;
			return btFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 获取 XL 云播的 视频列表
	 * 
	 * @param context
	 * @param per
	 *            一页的个数 >=1
	 * @param page
	 *            页码 从第一页开始
	 * @return
	 */
	public static ArrayList<XLLXFileInfo> getVideoList(Context context,
			int per, int page) {
		try {
			String CookieUID = XLLXBiz.getUserUID(context);
			if (isNumeric(CookieUID) && CookieUID != "-") {
				CookieUID = "userid=" + CookieUID + ";";
			} else {
				CookieUID = "userid=" + XLLXBiz.getUID(context)
						+ "; sessionid=" + XLLXBiz.getSessionid(context);
			}
			Log.d(TAG, "CookieUID= " + CookieUID);
			Header cookie = new BasicHeader(XLLXBiz.COOKIE, CookieUID);
			String url = "http://i.vod.xunlei.com/req_history_play_list/req_num/"
					+ per + "/req_offset/" + per * (page - 1);

			String json = HttpUtils.getContent(
					url,
					new Header[] { cookie },
					new NameValuePair[] {
							new BasicNameValuePair("type", "all"),
							new BasicNameValuePair("order", "create"),
							new BasicNameValuePair("t", String.valueOf(System
									.currentTimeMillis())) });
			if (json == null) {
				return null;
			}
			json = URLDecoder.decode(json);
			Log.d(TAG, "getVideoList  json= " + json);
			JSONTokener jsonParser = new JSONTokener(json);
			JSONObject object = (JSONObject) jsonParser.nextValue();
			JSONObject resp = object.getJSONObject("resp");
			int recodenum = resp.getInt("record_num");
			Log.d(TAG, "getVideoList  record_num= " + recodenum);
			JSONArray history_play_list = resp
					.getJSONArray("history_play_list");
			ArrayList<XLLXFileInfo> list = new ArrayList<XLLXFileInfo>();
			for (int i = 0; i < history_play_list.length(); i++) {
				XLLXFileInfo info = new XLLXFileInfo();
				JSONObject historyPlay = history_play_list.getJSONObject(i);
				info.file_name = (historyPlay.getString("file_name"));
				info.src_url = (historyPlay.getString("src_url"));
				info.createTime = historyPlay.getString("createtime");
				info.duration = historyPlay.getString("duration");
				info.filesize = historyPlay.getString("file_size");
				info.userid = historyPlay.getString("userid");
				info.urlhash = historyPlay.getString("url_hash");
				info.gcid = historyPlay.getString("gcid");
				info.cid = historyPlay.getString("cid");
				info.recodenum = recodenum;
				if (info.src_url.contains("bt://")) {
					info.isDir = true;
				}
				list.add(info);
			}
			return list;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取 迅雷 云播的 地址
	 * 
	 * @param url
	 * @param headers
	 * @param pairs
	 * @return
	 */
	public static ArrayList<VideoPlayUrl> getPlayUrl(String url,
			Header[] headers, NameValuePair[] pairs) {
		try {
			ArrayList<VideoPlayUrl> urls = new ArrayList<VideoPlayUrl>();
			String json = HttpUtils.getContent(url, headers, pairs);
			if (TextUtils.equals(json, null)) {
				return null;
			}
			JSONTokener jsonParser = new JSONTokener(json);
			JSONObject object = (JSONObject) jsonParser.nextValue();
			JSONObject resp = object.getJSONObject("resp");
			JSONArray array = resp.getJSONArray("vodinfo_list");
			for (int i = 0; i < array.length(); i++) {
				VideoPlayUrl playurl = new VideoPlayUrl();
				JSONObject urlobject = array.getJSONObject(i);
				playurl.playurl = urlobject.getString("vod_url");
				playurl.sharp = SharpnessEnum.getSharp(i);
				urls.add(playurl);
			}
			return urls;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 播放地址解析
	 * 
	 * 先用这高清格式去解析播放地址,也需要cookie
	 * 
	 * 前提，如果 gcid 为NULL ，直接跳过 使用src_url 去解析。
	 * 
	 * http://i.vod.xunlei.com/vod_dl_all?userid="+userid+"&gcid="+gcid+"&filename
	 * ="+filename;
	 * 
	 * 如果解析地址都为空，也直接用src_url解析
	 * 
	 * http://i.vod.xunlei.com/req_get_method_vod?url="+urlencode(src_url)+"&
	 * video_name
	 * ="+filename+"&platform="+清晰度(0或者1再或者2)+"&userid="+userid+"&vip=1
	 * &sessionid="+sessionid+"&cache="+动态time+"&from=vlist
	 * 
	 * String link = "http://i.vod.xunlei.com/vod_dl_all?userid=" + info.userid
	 * + "&gcid=" + info.gcid + "&filename=" + info.file_name;
	 */

	private static final String GET_PLAY_URL_1 = "http://i.vod.xunlei.com/vod_dl_all";

	public static ArrayList<VideoPlayUrl> getLXPlayUrl_1(Context context,
			XLLXFileInfo info) {
		ArrayList<VideoPlayUrl> urls = new ArrayList<VideoPlayUrl>();
		try {
			if (info.isDir) { // 文件夹 没有解析播放地址
				return urls;
			}
			String CookieUID = "userid=" + XLLXBiz.getUID(context)
					+ "; sessionid=" + XLLXBiz.getSessionid(context);
			String list_json = HttpUtils
					.getContent(GET_PLAY_URL_1, new Header[] { new BasicHeader(
							XLLXBiz.COOKIE, CookieUID) },
							new NameValuePair[] {
									new BasicNameValuePair("userid",
											info.userid),
									new BasicNameValuePair("gcid", info.gcid),
									new BasicNameValuePair("filename",
											URLEncoder.encode(info.src_url)) });
			JSONTokener jsonParser = new JSONTokener(list_json);
			JSONObject object = (JSONObject) jsonParser.nextValue();
			JSONObject full_HD_Object = object.getJSONObject("Full_HD");
			Log.d(TAG, full_HD_Object + "");
			if (full_HD_Object.has("url") && !full_HD_Object.isNull("url")) {
				VideoPlayUrl playurl = new VideoPlayUrl();
				playurl.playurl = full_HD_Object.getString("url");
				playurl.sharp = SharpnessEnum.getSharp(3);
				urls.add(playurl);
			}

			JSONObject HD_Object = object.getJSONObject("HD");
			if (HD_Object.has("url") && !HD_Object.isNull("url")) {
				VideoPlayUrl playurl = new VideoPlayUrl();
				playurl.playurl = HD_Object.getString("url");
				playurl.sharp = SharpnessEnum.getSharp(2);
				urls.add(playurl);
			}
			JSONObject SD_Object = object.getJSONObject("SD");
			if (SD_Object.has("url") && !SD_Object.isNull("url")) {
				VideoPlayUrl playurl = new VideoPlayUrl();
				playurl.playurl = SD_Object.getString("url");
				playurl.sharp = SharpnessEnum.getSharp(0);
				urls.add(playurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
	}

	/*
	 * http://i.vod.xunlei.com/req_get_method_vod?url="+urlencode(src_url)+"&
	 * video_name
	 * ="+filename+"&platform="+清晰度(0或者1再或者2)+"&userid="+userid+"&vip=1
	 * &sessionid="+sessionid+"&cache="+动态time+"&from=vlist
	 */

	private static final String GET_PLAY_URL_2 = "http://i.vod.xunlei.com/req_get_method_vod";

	public static ArrayList<VideoPlayUrl> getLXPlayUrl_2(Context context,
			XLLXFileInfo info) {
		ArrayList<VideoPlayUrl> urls = new ArrayList<VideoPlayUrl>();
		try {
			// BT文件夹转码没有完成或失败
			if (info.isDir || info.file_name == null || info.src_url == null) {
				return urls;
			}
			String CookieUID = "userid=" + XLLXBiz.getUID(context)
					+ "; sessionid=" + XLLXBiz.getSessionid(context);
			for (int i = 2; i > 0; i--) {
				String json = HttpUtils.getContent(
						GET_PLAY_URL_2,
						new Header[] {
								new BasicHeader(XLLXBiz.COOKIE, CookieUID),
								new BasicHeader("Referer", REFERER) },
						new NameValuePair[] {
								new BasicNameValuePair("url", URLEncoder
										.encode(info.src_url)),
								new BasicNameValuePair("video_name", URLEncoder
										.encode(info.file_name)),
								new BasicNameValuePair("platform", i + ""),
								new BasicNameValuePair("userid", XLLXBiz
										.getUID(context)),
								new BasicNameValuePair("vip", "1"),
								new BasicNameValuePair("sessionid", XLLXBiz
										.getSessionid(context)),
								new BasicNameValuePair("cache", System
										.currentTimeMillis() + ""),
								new BasicNameValuePair("from", "vlist") });

				Log.d(TAG, "play url 2 : json =" + json);
				JSONTokener jsonParser = new JSONTokener(json);
				JSONObject object = (JSONObject) jsonParser.nextValue();
				JSONObject resp = object.getJSONObject("resp");
				if (resp.has("src_info")) {

					JSONArray array = resp.getJSONArray("vodinfo_list");
					Log.d("", "" + array.length());
					for (int j = 0; j < array.length(); j++) {
						VideoPlayUrl playurl = new VideoPlayUrl();
						JSONObject urlobject = array.getJSONObject(j);
						playurl.playurl = urlobject.getString("vod_url");
						playurl.sharp = SharpnessEnum.getSharp(j);
						urls.add(playurl);
					}
				}
				if (urls.size() > 0) {
					return urls;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
	}

	public static ArrayList<VideoPlayUrl> getLXPlayUrl(Context context,
			XLLXFileInfo info) {

		ArrayList<VideoPlayUrl> urls = getLXPlayUrl_1(context, info);
		if (urls.size() > 0) {
			return urls;
		} else {
			return getLXPlayUrl_2(context, info);
		}

	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * http://i.vod.xunlei.com/subtitle/list?gcid=
	 * CB957B71B95D0C03678AACE628DCE12D1F565338
	 * &cid=DDDE9CECC640BE251D5885DE2DBC013C0B1F7715
	 * &userid=58531257&t=1377049152562 解析字幕列表
	 * 
	 * @param gcid
	 *            cid userid t
	 * 
	 * @return
	 */
	public static ArrayList<LXSRT> getLxSrtsInfo(XLLXFileInfo info) {
		String url = "http://i.vod.xunlei.com/subtitle/list";
		String json = HttpUtils.getContent(url, null, new NameValuePair[] {
				new BasicNameValuePair("gcid", info.gcid),
				new BasicNameValuePair("cid", info.cid),
				new BasicNameValuePair("userid", info.userid),
				new BasicNameValuePair("t", System.currentTimeMillis() + "") });
		if (json != null) {
			System.out.println(json);
			ArrayList<LXSRT> lxList = new ArrayList<LXSRT>();
			try {
				JSONTokener jsonParser = new JSONTokener(json);
				JSONObject object = (JSONObject) jsonParser.nextValue();
				// JSONObject resp = object.getJSONObject("resp");
				JSONArray array = object.getJSONArray("sublist");
				for (int i = 0; i < array.length(); i++) {
					LXSRT lxsrt = new LXSRT();
					JSONObject lxsrtobject = array.getJSONObject(i);
					lxsrt.setSurl(lxsrtobject.getString("surl"));
					lxsrt.setSname(lxsrtobject.getString("sname"));
					lxsrt.setScid(lxsrtobject.getString("scid"));
					lxsrt.setLanguage(lxsrtobject.getString("language"));
					lxList.add(lxsrt);
				}
				Log.i("info", "字幕信息"+lxList.toString());
				return lxList;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
