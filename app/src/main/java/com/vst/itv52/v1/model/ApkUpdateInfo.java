package com.vst.itv52.v1.model;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApkUpdateInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6652507875072567089L;
	public String appname;
	public String verName;
	public String instruction;
	public int verCode;
	public String apkurl;
	public String apkmd5;
	public String apkpath;

}
