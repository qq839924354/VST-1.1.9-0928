package com.vst.itv52.v1.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * 已安装的APP信息描述类
 * 
 * @author Administrator
 * 
 */
public class AppLancherJavaBean {
	private Drawable icon;// 图标
	private String name;// 名字
	private Intent intent;// 跳转
	private String dataDir;// 路径

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

}
