package com.vst.itv52.v1.srt;

/**
 * 此类不是离线的字幕，而是用来获取字幕的相关信息
 * 
 * @author mygica-hsj
 * 
 */
public class LXSRT {
	private String surl;//字幕下载链接
	private String sname;//字幕文件名
	private String scid;
	private String language;//语言

	public String getSurl() {
		return surl;
	}

	public void setSurl(String surl) {
		this.surl = surl;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public String getScid() {
		return scid;
	}

	public void setScid(String scid) {
		this.scid = scid;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "LXSRT [surl=" + surl + ", sname=" + sname + ", scid=" + scid
				+ ", language=" + language + "]";
	}

}
