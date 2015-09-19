package com.vst.itv52.v1.srt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpResult;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.model.ShooterSRTBean;
import com.vst.itv52.v1.util.StringUtil;
import com.vst.itv52.v1.util.parseString;

public class ShooterSRTGetter {

	/**
	 * 获取射手字幕的ID
	 * 
	 * @param keyWord
	 * @return
	 */
	private static String getSRTId(String keyWord) {
		BufferedReader br = null;
		String line = null;
		String sId = null;
		String url = "http://www.shooter.cn/search2/" + keyWord + "/?sort=rank";
		try {
			// 获得搜索某个影片字幕下载列表的网页
			Header[] extra = new Header[] {
					new BasicHeader("User-Agent", "Mozilla/5.0"),
					new BasicHeader("Referer", "http://shooter.cn/") };
			String html = HttpUtils.getContent(url, extra, null);
			// Log.i("info", "射手网的html=" + html);
			br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(html.getBytes("utf-8"))));
			while ((line = br.readLine()) != null) {
				if (line.contains("/xml/sub/")) {// 取得对应的行
					System.out.println("取得行：" + line);
					String[] strs = line.split("\"");// 拆分成字段
					for (int i = 0; i < strs.length; i++) {
						if (strs[i].contains("/xml/sub/")) {// 取得对应的字段
							System.out.println("取得字段：" + strs[i]);
							sId = strs[i].substring(strs[i].lastIndexOf("/"),
									strs[i].indexOf(".xml"));
							System.out.println("取得字幕ID：" + sId);
							break;
						}
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sId;
	}

	/**
	 * 获取到射手网某个影视的所有字幕下载列表
	 * 
	 * @param keyWord
	 * @return
	 */
	public static ArrayList<ShooterSRTBean> getShooterSrts(String keyWord) {
		BufferedReader br = null;
		String line = null;
		String sId = null;
		String title = null;
		ArrayList<ShooterSRTBean> shooterSrts = null;
		String url = "http://www.shooter.cn/search2/" + keyWord + "/?sort=rank";
		try {
			// 获得搜索某个影片字幕下载列表的网页
			Header[] extra = new Header[] {
					new BasicHeader("User-Agent", "Mozilla/5.0"),
					new BasicHeader("Referer", "http://shooter.cn/") };
//			long time = System.currentTimeMillis();
			String html = HttpUtils.getContent(url, extra, null);
//			Log.i("info", "耗时任务="+(System.currentTimeMillis()-time));
			br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(html.getBytes("utf-8"))));
			ShooterSRTBean shooterStr = null;
			shooterSrts = new ArrayList<ShooterSRTBean>();
//			long time2 = System.currentTimeMillis();
			while ((line = br.readLine()) != null) {
				// 取标题
				if (line.contains("introtitle")) {// 取得对应的行
					shooterStr = new ShooterSRTBean();
					title =  parseString.jiequ(line, " title=\"", "\" href=");
					//Log.i("System.out", "title=" + title);
					if (title != null) {
						shooterStr.setTitle(title.trim());
					}
				}
				// 取id
				if (line.contains("local_downfile") && !line.contains("MB\"")) {
					//Log.d("info", "id哪一行=" + line);
					sId = line.substring(line.indexOf(",")+1, line.indexOf(")"));
					if ( shooterStr != null && sId != null) {
						shooterStr.setId(sId.trim());
						if (shooterStr.getTitle() != null
								&& shooterStr.getTitle() != null) {
							shooterSrts.add(shooterStr);
						}
					}
				}
			}
//			Log.i("info", "耗时任务="+(System.currentTimeMillis()-time2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return shooterSrts;
	}

	/**
	 * 通过ID获取下载链接
	 * 
	 * @param keyWord
	 * @return
	 */
	private static String getTrueDownloadKey(String keyWord) {
		String sid = getSRTId(keyWord);
		String fileurl = "http://shooter.cn/files/file3.php?hash=duei7chy7gj59fjew73hdwh213f&fileid="
				+ sid;
		BufferedReader br = null;
		String line = null;
		try {
			Header[] extra = new Header[] {
					new BasicHeader("User-Agent", "Mozilla/5.0"),
					new BasicHeader("Referer", "http://shooter.cn/") };
			String html = HttpUtils.getContent(fileurl, extra, null);
			br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(html.getBytes("utf-8"))));
			while ((line = br.readLine()) != null) {
				System.out.println("取得行：" + line);
				return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据射手网上的资源id 获取下载连接
	 * 
	 * @param sid
	 * @return
	 */
	private static String getDownloadKey(String sid) {
		String fileurl = "http://shooter.cn/files/file3.php?hash=duei7chy7gj59fjew73hdwh213f&fileid="
				+ sid;
		BufferedReader br = null;
		String line = null;
		try {
			Header[] extra = new Header[] {
					new BasicHeader("User-Agent", "Mozilla/5.0"),
					new BasicHeader("Referer", "http://shooter.cn/") };
			String html = HttpUtils.getContent(fileurl, extra, null);
			br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(html.getBytes("utf-8"))));
			while ((line = br.readLine()) != null) {
				System.out.println("取得行：" + line);
				return line.trim();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据sid下载字幕zip
	 * 
	 * @param sid
	 * @param filePath
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static boolean downloadSRTZip(String sid, String filePath) throws UnsupportedEncodingException {
		String trueDownloadKey = getDownloadKey(sid);
		if (trueDownloadKey.contains("ERR:")) {
			return false;
		}
		String zipDownUrl = "http://livecdn.myvst.net/shooter.zip?k="
				+ StringUtil.toHexString(trueDownloadKey);
		Log.d("info", "字幕下载地址" + zipDownUrl);
		HttpResult result = HttpClientHelper.get(zipDownUrl);
		long filesize = -1;
		if (result.getHeader("Content-Length") != null) {
			filesize = Integer.parseInt(result.getHeader("Content-Length").getValue());
			Log.d("info","filesize=========="+filesize);
		}
		if(filesize<0 || (filesize >(1024*1024))){
			return false;
		}
		byte[] data = result.getResponse();
		//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~"+new String(data, "ANSI"));
		if (data != null) {
			// InputStream is=new ByteArrayInputStream(data);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
				fos.write(data);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 根据指定的连接地址和指定的存储路径下载
	 * 
	 * @param trueDownloadKey
	 * @param filePath
	 * @return
	 */
	public static boolean downloadSRTZipfromKey(String trueDownloadKey,
			String filePath) {
		String zipDownUrl = "http://livecdn.myvst.net/shooter.zip";
		HttpResult result = HttpClientHelper.post(zipDownUrl,
				new BasicNameValuePair[] { new BasicNameValuePair("k",
						trueDownloadKey) });
		byte[] data = result.getResponse();
		if (data != null) {
			// InputStream is=new ByteArrayInputStream(data);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
				fos.write(data);
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
