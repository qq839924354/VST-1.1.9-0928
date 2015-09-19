package com.vst.itv52.v1.biz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoInfo;

public class VideoTJBiz {

	/**
	 * 首先从文件获取
	 * 
	 * @param url
	 * @return
	 */
	public static ArrayList<VideoInfo> parseTJ(Context context, String baseurl,boolean fromCache) {
		String url = baseurl + "recommend";
//		System.out.println("推荐页"+url);
		ArrayList<VideoInfo> list = null;
		/**
		 * 从文件获取
		 */
		File tjFile = new File(context.getCacheDir(), "tjFile");
		if(fromCache){
			if (tjFile.exists()) {
				try {
					list = new ArrayList<VideoInfo>();
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.readTree(tjFile);
					JsonNode video = rootNode.path("video");
					for (JsonNode jsonNode : video) {
						list.add(mapper.treeToValue(jsonNode, VideoInfo.class));
					}
					return list;
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
		String json = HttpUtils.getContent(url, null, null);
		if (json != null) {
//			System.out.println(json);
			try {
				// 保存文件
				FileOutputStream fos = new FileOutputStream(tjFile);
				ByteArrayInputStream bis = new ByteArrayInputStream(
						json.getBytes());
				int count = -1;
				byte[] buffer = new byte[2048];
				while ((count = bis.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				bis.close();

				list = new ArrayList<VideoInfo>();
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(json);
				JsonNode video = rootNode.path("video");
				for (JsonNode jsonNode : video) {
					list.add(mapper.treeToValue(jsonNode, VideoInfo.class));
				}
				return list;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static ArrayList<VideoInfo> parseSubject(String url, String zid) {
		String json = HttpUtils.getContent(url, null, new NameValuePair[] {
				new BasicNameValuePair("zid", zid) });
		if (json != null) {
			try {
				ArrayList<VideoInfo> list = new ArrayList<VideoInfo>();
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(json);
				JsonNode video = rootNode.path("video");
				for (JsonNode jsonNode : video) {
					list.add(mapper.treeToValue(jsonNode, VideoInfo.class));
				}
				return list;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
