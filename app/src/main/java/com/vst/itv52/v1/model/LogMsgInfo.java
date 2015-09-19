package com.vst.itv52.v1.model;

import java.io.Serializable;
/**
 * 返回码类
 * @author w
 *
 */
public class LogMsgInfo implements Serializable{
	
	private static final long serialVersionUID = 7624025724185349899L;
	
	private int logCode; //返回码
	private String logMsg; //返回消息
	private String cookie;//cookie信息
	
	public int getLogCode() {
		return logCode;
	}
	public void setLogCode(int logCode) {
		this.logCode = logCode;
	}
	public String getLogMsg() {
		return logMsg;
	}
	public void setLogMsg(String logMsg) {
		this.logMsg = logMsg;
	}
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	
}
