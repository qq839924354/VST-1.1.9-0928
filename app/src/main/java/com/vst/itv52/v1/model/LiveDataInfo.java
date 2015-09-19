package com.vst.itv52.v1.model;

import java.util.ArrayList;

public class LiveDataInfo {

	public int tvnum;
	public long uptime;
	public String playhost;

	public ArrayList<Typeinfo> type;
	public ArrayList<ChannelInfo> live;

	public static class Typeinfo {

		public String id;
		public String name;

	}

	public static class ChannelInfo {
		public int id;
		public int num;
		public String name;
		public String itemid;
		public String pinyin;
		public String quality;
		public String huibo;
		public String epgid;
		public String urllist;
//		/area  icon
		public String area;
		
		public String icon;

	}

}
