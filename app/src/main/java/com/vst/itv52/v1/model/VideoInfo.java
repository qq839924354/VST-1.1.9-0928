package com.vst.itv52.v1.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoInfo implements Serializable {

	private static final long serialVersionUID = 991029316222408987L;
	public int id;
	public String mark;//评分
	public String title;//标题
	public String img;//海报地址
	public String qxd;//清晰度
	public String banben;//版本（时长）

	public String zid;// 专辑ID
	public String logo;// 专辑全屏背景
	@Override
	public String toString() {
		return "VideoInfo [id=" + id + ", mark=" + mark + ", title=" + title
				+ ", img=" + img + ", qxd=" + qxd + ", banben=" + banben
				+ ", zid=" + zid + ", logo=" + logo + "]";
	}


}
