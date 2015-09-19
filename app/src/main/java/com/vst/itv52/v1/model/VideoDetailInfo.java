package com.vst.itv52.v1.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 电视剧 电影 详情信息
 */

public class VideoDetailInfo implements Serializable {

	private static final long serialVersionUID = 6340622314169844477L;
	public int id; // 电视 id
	public String title; // 名称
	public String img; // 图片地址
	public String type;// 影视类型
	public String year; // 年份
	public String area; // 地区
	public String dur;
	public String language;
	public String cate; // 类别 娱乐
	public String director; // 导演
	public String actor; // 演员
	public String info; // 介绍
	public int doubanid;
	public String mark;
	public String banben; // 版本 DVD 高清 标清 更新到30集
	public int playcount; // 播放次数
	public String update; // 更新时间
	public int setnumber;
	public ArrayList<VideoSource> playlist; // 视频源信息列表
	@JsonProperty("new_top")
	public ArrayList<VideoInfo> recommends;
	public String nexturl;

	@Override
	public String toString() {
		return "VideoDetailInfo [id=" + id + ", title=" + title + ", img="
				+ img + ", type=" + type + ", year=" + year + ", area=" + area
				+ ", dur=" + dur + ", language=" + language + ", cate=" + cate
				+ ", director=" + director + ", actor=" + actor + ", info="
				+ info + ", doubanid=" + doubanid + ", mark=" + mark
				+ ", banben=" + banben + ", playcount=" + playcount
				+ ", update=" + update + ", setnumber=" + setnumber
				+ ", playlist=" + playlist + ", recommends=" + recommends
				+ ", nexturl=" + nexturl + "]";
	}

}
