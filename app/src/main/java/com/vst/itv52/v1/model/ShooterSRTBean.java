package com.vst.itv52.v1.model;

import java.io.Serializable;

/**
 * 射手网上搜索字幕的类
 * 
 * @author w
 * 
 */
public class ShooterSRTBean implements Serializable {
	private static final long serialVersionUID = 7215731048431707954L;

	private String id;// video字幕id
	private String title;// video字幕的名称

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "id="+id+",title="+title;
	}
}
