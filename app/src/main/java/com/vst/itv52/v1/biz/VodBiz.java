package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import android.content.Context;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.SharpnessEnum;
import com.vst.itv52.v1.model.VideoPlayUrl;

public class VodBiz {
	private Context mContext;
	private static final String TAG = "VodBiz";

	public VodBiz(Context context) {
		mContext = context;
	}

	public ArrayList<VideoPlayUrl> getPlayUris(String url, String link,
			Header hdHead) {
		url = url + link + ".xml";
		// Log.d(TAG, "getPlayUris  url = " + url);
//http://cdn.91vst.com/v_xml/
//http://v.youku.com/v_show/id_XMTI1ODc5MjU2NA==.html
		
//Player-HD: 2
		url = link;

		try {
			String xml = HttpUtils.getContent(url, null, null);
			//savaFile(xml);
			SAXReader reader = new SAXReader();
			Document document = reader.read(new ByteArrayInputStream(xml
					.getBytes("utf-8")));
			Element root = document.getRootElement();
			int ret = Integer.parseInt(root.elementTextTrim("ret"));
			if (ret == 0) {
				ArrayList<VideoPlayUrl> urls = new ArrayList<VideoPlayUrl>();
				if (root.element("geturl") != null) { // 迅雷离线
					String geturl = root.elementText("geturl");
					String referer = root.elementText("referer");
					Header head = new BasicHeader("referer", referer);
					return XLLXBiz.getPlayUrl(geturl, new Header[] { head },
							null);
				} else if (root.element("returl") != null) { // 迅雷看看
					String returl = root.elementText("returl");
					List<Element> playElements = root.elements("play");
					for (int i = 0; i < playElements.size(); i++) {
						String tempUrl = playElements.get(i).elementTextTrim(
								"url");
						// Log.d(TAG, tempUrl);
						// 先置于null
						String content = null;
						try {
							// 内存溢出
							content = HttpUtils.getContent(tempUrl, null, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
						// Log.d(TAG, content);
						String enCodeStr = URLEncoder.encode(content, "utf-8");
						// Log.d(TAG, enCodeStr);
						String requestUrl = returl.replace("@post", enCodeStr)
								.replace("@link", tempUrl);
						// Log.d(TAG, requestUrl);
						String resultUrl = HttpUtils.getContent(requestUrl,
								null, null);
						// Log.d(TAG, resultUrl);
						VideoPlayUrl playurl = new VideoPlayUrl();
						playurl.playurl = resultUrl;
						playurl.sharp = SharpnessEnum.getSharp(Integer
								.parseInt(playElements.get(i).attributeValue(
										"q")));
						System.out.println(playurl.toString());
						urls.add(playurl);
					}
					return urls;
				} else { // 其他
					List<Element> playElements = root.elements("play");
					for (int i = 0; i < playElements.size(); i++) {
						VideoPlayUrl playurl = new VideoPlayUrl();
						playurl.playurl = playElements.get(i).elementTextTrim(
								"url");
						playurl.sharp = SharpnessEnum.getSharp(Integer
								.parseInt(playElements.get(i).attributeValue(
										"q")));
						System.out.println(playurl.toString());
						urls.add(playurl);
					}
					return urls;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	void savaFile(String conent) {
		try {
			FileOutputStream file = mContext.openFileOutput("liangming",
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
