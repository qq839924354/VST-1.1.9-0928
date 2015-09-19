package com.vst.itv52.v1.model;

import java.util.Map;

/**
 * 电视回看频道信息类
 * 
 * @author mygica-hsj
 * 
 */
public class TVBackChannelJavabean {
	private Map<String, String> dateMap;
	private Map<String, String> tvMap;
	private String nexturl;

	public Map<String, String> getDateMap() {
		return dateMap;
	}

	public void setDateMap(Map<String, String> dateMap) {
		this.dateMap = dateMap;
	}

	public Map<String, String> getTvMap() {
		return tvMap;
	}

	public void setTvMap(Map<String, String> tvMap) {
		this.tvMap = tvMap;
	}

	public String getNexturl() {
		return nexturl;
	}

	public void setNexturl(String nexturl) {
		this.nexturl = nexturl;
	}

	@Override
	public String toString() {
		return "TVBackChannelJavabean [dateMap=" + dateMap + ", tvMap=" + tvMap
				+ ", nexturl=" + nexturl + "]";
	}

}
