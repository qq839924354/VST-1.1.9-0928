package com.vst.itv52.v1.model;

public class UserInfo {
	private String myuid;// 数字ID
	private String userid;// 用户名
	private String password;// 密码
	private String point;// 积分
	private String money;// 余额
	private String email;// 邮箱
	private String name;// 昵称
	private String sex;// 性别
	private String qq;// QQ
	private String tel;// 电话
	private String zip;// 邮政编码
	private String card;// 身份证
	private String address;// 地址
	private String login_cs;// 登陆次数
	private String loginip1;// 本次登陆IP
	private String loginip2;// 上次登录IP
	private String logintime1;// 本次登陆时间
	private String logintime2;// 上次登录时间
	private String login_text1;// 本次登陆地点
	private String login_text2;// 上次登录地点
	private String regnow;// 注册时间

	public String getMyuid() {
		return myuid;
	}

	public void setMyuid(String myuid) {
		this.myuid = myuid;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLogin_cs() {
		return login_cs;
	}

	public void setLogin_cs(String login_cs) {
		this.login_cs = login_cs;
	}

	public String getLoginip1() {
		return loginip1;
	}

	public void setLoginip1(String loginip1) {
		this.loginip1 = loginip1;
	}

	public String getLoginip2() {
		return loginip2;
	}

	public void setLoginip2(String loginip2) {
		this.loginip2 = loginip2;
	}

	public String getLogintime1() {
		return logintime1;
	}

	public void setLogintime1(String logintime1) {
		this.logintime1 = logintime1;
	}

	public String getLogintime2() {
		return logintime2;
	}

	public void setLogintime2(String logintime2) {
		this.logintime2 = logintime2;
	}

	public String getLogin_text1() {
		return login_text1;
	}

	public void setLogin_text1(String login_text1) {
		this.login_text1 = login_text1;
	}

	public String getLogin_text2() {
		return login_text2;
	}

	public void setLogin_text2(String login_text2) {
		this.login_text2 = login_text2;
	}

	public String getRegnow() {
		return regnow;
	}

	public void setRegnow(String regnow) {
		this.regnow = regnow;
	}

	@Override
	public String toString() {
		return "UserInfo [myuid=" + myuid + ", userid=" + userid
				+ ", password=" + password + ", point=" + point + ", money="
				+ money + ", email=" + email + ", name=" + name + ", sex="
				+ sex + ", qq=" + qq + ", tel=" + tel + ", zip=" + zip
				+ ", card=" + card + ", address=" + address + ", login_cs="
				+ login_cs + ", loginip1=" + loginip1 + ", loginip2="
				+ loginip2 + ", logintime1=" + logintime1 + ", logintime2="
				+ logintime2 + ", login_text1=" + login_text1
				+ ", login_text2=" + login_text2 + ", regnow=" + regnow + "]";
	}

}
