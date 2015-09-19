package com.vst.itv52.v1.biz;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.model.VideoList;

public class SerachBiz {

	public static VideoList parseSerachResult(String url, String kw, int page) {
		url = url + "&wd=" + kw + "&page=" + page;

		String json = HttpUtils.getContent(url, null, null);
		if (json != null) {
			// System.out.println(json);
			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode = mapper.readTree(json);
				JsonNode videoNode = rootNode.path("video");
				for (JsonNode jsonNode : videoNode) {
					mapper.treeToValue(jsonNode, VideoInfo.class);
				}
				return mapper.treeToValue(rootNode, VideoList.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// public static VideoList parseSerachResult(String url) {
	// String json = HttpUtils.getContent(url, null, null);
	// System.out.println(json);
	// try {
	// ObjectMapper mapper = new ObjectMapper();
	// JsonNode rootNode = mapper.readTree(json);
	// JsonNode videoNode = rootNode.path("video");
	// for (JsonNode jsonNode : videoNode) {
	// mapper.treeToValue(jsonNode, VideoInfo.class);
	// }
	// return mapper.treeToValue(rootNode, VideoList.class);
	// } catch (JsonProcessingException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
}
