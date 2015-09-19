package com.vst.itv52.v1.model;

import java.io.Serializable;

public class VideoPlayUrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3842644477264449726L;
	public SharpnessEnum sharp;
	public String playurl;

	@Override
	public String toString() {
		return "VideoPlayUrl [sharp=" + sharp + ", playurl=" + playurl + "]";
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof VideoPlayUrl) {
			VideoPlayUrl url = (VideoPlayUrl) o;
			if (this.sharp.getIndex() == url.sharp.getIndex()
					&& this.playurl.equalsIgnoreCase(url.playurl)) {
				return true;
			}
		}
		return false;
	}

}
