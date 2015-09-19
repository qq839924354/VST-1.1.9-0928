package com.vst.itv52.v1.model;

import java.io.Serializable;
import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LiveChannelInfo implements Serializable {

	private static final long serialVersionUID = 2091324006672211407L;
	@JsonProperty("id")
	public int vid; // vid 频道id not null
	@JsonProperty("name")
	public String vname; // 频道名 not null
	public int num;
	public String itemid ;
	public String urllist ;
	public String epgid;
	public String huibo;
	public String quality;
	public String pinyin;
	
	public boolean favorite;
	public long duration; // 播放时长
	public int lastSource;
	public String[] tid;
	public String[] liveSources;

	public String getSourceText(String[] liveSources) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < liveSources.length; i++) {
			sb.append(liveSources[i]).append("#");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getTidText(String[] tid) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tid.length; i++) {
			sb.append(tid[i]).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public String getSourceUrl(int index) {
		if (liveSources != null && liveSources.length > 0
				&& index < liveSources.length) {
			return liveSources[index];
		}
		return null;
	}

	@Override
	public String toString() {
		return "LiveChannelInfo [vid=" + vid + ", vname=" + vname + ", tid="
				+ tid[0] + ", liveSources=" + Arrays.toString(liveSources)
				+ ", epgid=" + epgid + ", huibo=" + huibo + ", quality="
				+ quality + ", pinyin=" + pinyin + ", favorite=" + favorite
				+ ", duration=" + duration + ", lastSource=" + lastSource + "]";
	}

}
