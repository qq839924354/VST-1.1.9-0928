package com.vst.itv52.v1.model;

import java.util.ArrayList;

public class ChannelColumns {
	private String title;
	private String playdate;
	private String liveurl;
	private ArrayList<ChannelColumnBean> list;
	private String m3u8;
	private String nexturl;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPlaydate() {
		return playdate;
	}

	public void setPlaydate(String playdate) {
		this.playdate = playdate;
	}

	public String getLiveurl() {
		return liveurl;
	}

	public void setLiveurl(String liveurl) {
		this.liveurl = liveurl;
	}

	public ArrayList<ChannelColumnBean> getList() {
		return list;
	}

	public void setList(ArrayList<ChannelColumnBean> list) {
		this.list = list;
	}

	public String getM3u8() {
		return m3u8;
	}

	public void setM3u8(String m3u8) {
		this.m3u8 = m3u8;
	}

	public String getNexturl() {
		return nexturl;
	}

	public void setNexturl(String nexturl) {
		this.nexturl = nexturl;
	}

	@Override
	public String toString() {
		return "ChannelColumns [title=" + title + ", playdate=" + playdate
				+ ", liveurl=" + liveurl + ", list=" + list + ", m3u8=" + m3u8
				+ ", nexturl=" + nexturl + "]";
	}

}
