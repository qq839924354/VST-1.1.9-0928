package com.vst.itv52.v1.model;

public class XLLXUserInfo {
	public int autopay;
	public String usrname;
	public int growvalue;
	public String expiredate;
	public int level;
	public int isyear;
	public int daily;
	public String payname;
	public String nickname;
	public int isvip;
	public int paytype;

	@Override
	public String toString() {
		return "XLLXUserInfo [autopay=" + autopay + ", usrname=" + usrname
				+ ", growvalue=" + growvalue + ", expiredate=" + expiredate
				+ ", level=" + level + ", isyear=" + isyear + ", daily="
				+ daily + ", payname=" + payname + ", nickname=" + nickname
				+ ", isvip=" + isvip + ", paytype=" + paytype + "]";
	}

}
