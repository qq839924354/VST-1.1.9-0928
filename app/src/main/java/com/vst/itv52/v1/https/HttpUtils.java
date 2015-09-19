package com.vst.itv52.v1.https;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

import android.R.integer;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.util.MD5Util;

public class HttpUtils {
	// 网络连接部分
	private static final String TAG = "HttpUtils";
	public static int count;
	public static String getContent(String url, Header[] headers,
			NameValuePair[] pairs ) {
		// if(!isNetworkAvailable(context)){
		// return null ;
		// }
		if (!TextUtils.isEmpty(url)
				&& (url.contains("52itv.cn") || url.contains("hdplay.cn") || url.contains("myvst.net") || url.contains("91vst.com"))) {
			Header[] extra = null;
			if (MyApp.getChanState() == 2) {
				System.out.println("Live list setting:NET");
				String loginKey = MyApp.getLoginKey();
				if (loginKey != null && loginKey != "" && loginKey.length() > 60) {
					extra = new BasicHeader[5];
					extra[4] = new BasicHeader("Cookie", "key=" + loginKey);
					System.out.println("Get User Live Cookie - > " + loginKey);
				} else {
					extra = new BasicHeader[4];
				}
			} else {
				System.out.println("Live list setting:LOCAL");
				extra = new BasicHeader[4];
			}
			extra[0] = new BasicHeader("User-Agent", MyApp.User_Agent);
			extra[1] = new BasicHeader("User-Mac", MyApp.User_Mac);
			extra[2] = new BasicHeader("User-Ver", MyApp.User_Ver);
			extra[3] = new BasicHeader("User-Key", MyApp.get_livekey());
			if (headers == null) {
				headers = extra;
			} else {
				Header[] temp = new Header[headers.length + extra.length];
				System.arraycopy(headers, 0, temp, 0, headers.length);
				System.arraycopy(extra, 0, temp, extra.length, extra.length);
				headers = temp;
			
			}
		}
		String content = null;
		System.out.println("直播："+url);
		HttpResult result = null;
		result = HttpClientHelper.get(url, headers, pairs);
		if (result != null && result.getStatuCode() == HttpStatus.SC_OK) {
			try {
				//内存溢出
				content = result.getHtml();
				Log.i("liangmingcontent", "content:" + content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		savaFile(content);
		// Log.d(TAG, "content= " + content);
		return content;
	}

	public static String getContent(String url, Header[] headers,
			NameValuePair[] pairs, String ctype ) {
		// if(!isNetworkAvailable(context)){
		// return null ;
		// }
		if (!TextUtils.isEmpty(url)
				&& (url.contains("52itv.cn") || url.contains("hdplay.cn") || url.contains("myvst.net") || url.contains("91vst.com"))) {
			Header[] extra = null;
			if (MyApp.getChanState() == 2) {
				System.out.println("Live list setting:NET");
				String loginKey = MyApp.getLoginKey();
				if (loginKey != null && loginKey != "" && loginKey.length() > 60) {
					extra = new BasicHeader[5];
					extra[4] = new BasicHeader("Cookie", "key=" + loginKey);
					System.out.println("Get User Live Cookie - > " + loginKey);
				} else {
					extra = new BasicHeader[4];
				}
			} else {
				System.out.println("Live list setting:LOCAL");
				extra = new BasicHeader[4];
			}
			extra[0] = new BasicHeader("User-Agent", MyApp.User_Agent);
			extra[1] = new BasicHeader("User-Mac", MyApp.User_Mac);
			extra[2] = new BasicHeader("User-Ver", MyApp.User_Ver);
			extra[3] = new BasicHeader("User-Key", MyApp.get_livekey());
			if (headers == null) {
				headers = extra;
			} else {
				Header[] temp = new Header[headers.length + extra.length];
				System.arraycopy(headers, 0, temp, 0, headers.length);
				System.arraycopy(extra, 0, temp, extra.length, extra.length);
				headers = temp;
			}
		}
		String content = null;
		System.out.println("直播："+url);
		HttpResult result = null;
		if (ctype == "post") {
			result = HttpClientHelper.post(url, headers, pairs);
		}
		else {
			result = HttpClientHelper.get(url, headers, pairs);
		}
		if (result != null && result.getStatuCode() == HttpStatus.SC_OK) {
			content = result.getHtml();
		}
		savaFile(content);
		// Log.d(TAG, "content= " + content);
		return content;
	}

	public static String getResultRedirecUrl(String url, Header[] headers,
			NameValuePair[] pairs) {
		HttpURLConnection conn = null;
		if (!TextUtils.isEmpty(url))
		// &&(url.contains("52itv.cn")||url.contains("hdplay.cn")||url.contains("myvst.net")))
		{
			Header[] extra = new Header[] {
					new BasicHeader("User-Agent", MyApp.User_Agent),
					new BasicHeader("User-Mac", MyApp.User_Mac),
					new BasicHeader("User-Key", MyApp.get_livekey()),
					new BasicHeader("User-Ver", MyApp.User_Ver), };
			if (headers == null) {
				headers = extra;
			} else {
				Header[] temp = new Header[headers.length + extra.length];
				System.arraycopy(headers, 0, temp, 0, headers.length);
				System.arraycopy(extra, 0, temp, extra.length, extra.length);
				headers = temp;
			}
		}
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			if (headers != null && headers.length > 0) {
				for (Header header : headers) {
					conn.setRequestProperty(header.getName(), header.getValue());
				}
			}
			System.out.println("返回码: " + conn.getResponseCode());
			return conn.getURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		return url;
	}

	public static byte[] getBinary(String url, Header[] headers,
			NameValuePair[] pairs) {
		// if(!isNetworkAvailable(context)){
		// return null ;
		// }
		byte[] binary = null;
		HttpResult result = HttpClientHelper.get(url, headers, pairs, null, 0);
		if (result != null && result.getStatuCode() == HttpStatus.SC_OK) {
			binary = result.getResponse();
		}
		// Log.d(TAG, "binary= " + binary);
		return binary;
	}

	// ------------------------------------------------------------------------------------------
	// 网络连接判断
	// 判断是否有网络
	public static boolean isNetworkAvailable(Context context) {
		return NetWorkHelper.isNetworkAvailable(context);
	}

	// 判断以太网络是否可用
	public static boolean isEthernetDataEnable(Context context) {
		String TAG = "httpUtils.isEthernetDataEnable()";
		try {
			return NetWorkHelper.isEthernetDataEnable(context);
		} catch (Exception e) {
			//Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 判断wifi网络是否可用
	public static boolean isWifiDataEnable(Context context) {
		String TAG = "httpUtils.isWifiDataEnable()";
		try {
			return NetWorkHelper.isWifiDataEnable(context);
		} catch (Exception e) {
			//Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	
	
public static	void savaFile(String conent) {
	count++;
		try {
			FileOutputStream file = MyApp.context.openFileOutput("liangming"+count,
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);

			file.write(conent.getBytes());
			file.flush();
			file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
