package com.vst.itv52.v1.model;

import java.io.Serializable;

public class NewsBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2655083644962652445L;
	public String id;
	public String title;
	public String img;
	public String playurl;

	@Override
	public String toString() {
		return "NewsBean [id=" + id + ", title=" + title + ", img=" + img
				+ ", playurl=" + playurl + "]";
	}

}
