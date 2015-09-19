package com.vst.itv52.v1.model;

import java.util.ArrayList;

/**
 * 搜索页面数据类
 * 
 * @author mygica-hsj
 * 
 */
public class VideoList {
	public int zurpage; // 一页显示的个数
	public int punpage; // 当前页数
	public int maxpage; // 最大页数
	public int video_count; // 视频总数
	public ArrayList<VideoInfo> video; // 视频基本信息列表

	@Override
	public String toString() {
		return "VideoList [zurpage=" + zurpage + ", punpage=" + punpage
				+ ", maxpage=" + maxpage + ", video_count=" + video_count
				+ ", videos=" + video + "]";
	}

}
