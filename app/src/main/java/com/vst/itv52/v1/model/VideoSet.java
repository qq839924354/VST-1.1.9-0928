package com.vst.itv52.v1.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 视频 集 信息 ，第几集的 信息
 * 
 * @author shenhui
 * 
 */

public class VideoSet implements Serializable {

	private static final long serialVersionUID = -2138928193462662024L;
	@JsonProperty("name")
	public String setName; // 视频集 名称
	@JsonProperty("url")
	public String link; // 播放地址
	@JsonIgnore
	public ArrayList<VideoPlayUrl> playUrls; // 清晰度 ， 播放地址

	@Override
	public String toString() {
		return "VideoSet [setName=" + setName + ", link=" + link
				+ ", playUrls=" + playUrls + "]";
	}

}
