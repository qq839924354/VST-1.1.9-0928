package com.vst.itv52.v1.model;

public class ChannelColumnBean {
	String time;
	String url;
	String channelText;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getChannelText() {
		return channelText;
	}

	public void setChannelText(String channelText) {
		this.channelText = channelText;
	}

	@Override
	public String toString() {
		return "ChannelColumnBean [time=" + time + ", url=" + url
				+ ", channelText=" + channelText + "]";
	}

}
