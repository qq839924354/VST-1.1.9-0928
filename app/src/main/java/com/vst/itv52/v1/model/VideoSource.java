package com.vst.itv52.v1.model;
import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

public class VideoSource implements Serializable {
	private static final long serialVersionUID = 4134329126322121094L;
	@JsonProperty("site")
	public String sourceName; // 视频源信息
	@JsonProperty("list")
	public ArrayList<VideoSet> sets;// 视频集信息

	@Override
	public String toString() {
		return "VideoSource [sourceName=" + sourceName + ", sets=" + sets + "]";
	}

}
