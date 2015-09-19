package com.vst.itv52.v1.model;

import java.io.Serializable;
import java.util.Arrays;

public class XLLXFileInfo implements Serializable {

	/*
	 * playflag == 4 && duration > 0 && file_size > 0 这个就是可以播放的视频，已转码完成！
	 * playflag == 5 正在转码中 zjjtv<my@52itv.cn> 21:28:20 src_url = bt:// &&
	 * playflag == 4 就是BT文件，需要打开目录
	 */

	private static final long serialVersionUID = -3255391871748282880L;
	public String file_name;
	public String src_url; // bt文件的话 为 上级src_url +"/"+index
	public String userid; // 文件 bt 都有
	public String gcid; // bt 没有
	public String filesize; // size 为 0 或 null 未完成转码
	public String duration; // 0
	public String createTime; //
	public String cid;
	public boolean isDir = false;
	public int playflag; //
	public int recodenum;
	public XLLXFileInfo[] btFiles;
	public String urlhash;

	@Override
	public String toString() {
		return "XLLXFileInfo [file_name=" + file_name + ", src_url=" + src_url
				+ ", userid=" + userid + ", gcid=" + gcid + ", filesize="
				+ filesize + ", duration=" + duration + ", createTime="
				+ createTime + ", cid=" + cid + ", isDir=" + isDir
				+ ", playflag=" + playflag + ", recodenum=" + recodenum
				+ ", btFiles=" + Arrays.toString(btFiles) + ", urlhash="
				+ urlhash + "]";
	}

}
