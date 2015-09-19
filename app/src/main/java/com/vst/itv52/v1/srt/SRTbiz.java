package com.vst.itv52.v1.srt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.TreeMap;

public class SRTbiz {
	

	/**
	 * 
	 * 解析SRT字幕文件，如从射手下载的字幕文件，以及用户自己指定的字幕文件
	 * 
	 *
	 * @param file  字幕文件
	 * @param timeError 字幕调整时间 单位是S
	 * @return
	 */
	public static Map<Integer, SRTBean> parseSrt(File file,int timeError) {
		FileInputStream inputStream = null;
		//InputStreamReader read=null;
		try {
			//read= new InputStreamReader(new FileInputStream(file),"UTF-8"); 
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;// 有异常，就没必要继续下去了
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return parseSrt(inputStream,timeError);
	}

	/**
	 * 从指定的网络连接解析SRT字幕，如迅雷离线字幕接口
	 * 
	 * @param srtUrl
	 * @return
	 */
	public static Map<Integer, SRTBean> parseSrt(String srtUrl,int timeError) {
		InputStream is = null;
		try {
			URLConnection conn = new URL(srtUrl).openConnection();
			conn.connect();
			is = conn.getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return parseSrt(is,timeError);
	}

	/**
	 * 通过输入流解析字幕
	 * 
	 * @param is
	 * @return
	 */
	private static Map<Integer, SRTBean> parseSrt(InputStream is,int timeError) {
		Map<Integer, SRTBean> srtMap = null;
		BufferedReader br = null;
		srtMap = new TreeMap<Integer, SRTBean>();
		StringBuffer sb = new StringBuffer();
		String line = null;
		byte[] first3bytes = new byte[3];
		BufferedInputStream in = new BufferedInputStream(is);
		int key = 0;
		String encode=null;
		try {
			in.mark(4);
			in.read(first3bytes);
			in.reset();
			/* 识别输入流文件格式 */
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
					&& first3bytes[2] == (byte) 0xBF) {
				encode="UTF-8";
				System.out.println("SRT，识别到输入流编码格式了，是：UTF-8");
				br = new BufferedReader(new InputStreamReader(in, "utf-8"));
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFE) {
				encode="Unicode";
				System.out.println("SRT，识别到输入流编码格式了，是：Unicode");
				br = new BufferedReader(new InputStreamReader(in, "unicode"));
			} else if (first3bytes[0] == (byte) 0xFE
					&& first3bytes[1] == (byte) 0xFF) {
				encode="UTF-16BE";
				System.out.println("SRT，识别到输入流编码格式了，是：UTF-16BE");
				br = new BufferedReader(new InputStreamReader(in, "utf-16be"));
			} else if (first3bytes[0] == (byte) 0xFF
					&& first3bytes[1] == (byte) 0xFF) {
				encode="UTF-16LE";
				System.out.println("SRT，识别到输入流编码格式了，是：UTF-16LE");
				br = new BufferedReader(new InputStreamReader(in, "utf-16le"));
			} else {
				encode="GBK";
				System.out.println("SRT，识别到输入流编码格式了，是：GBK");
				br = new BufferedReader(new InputStreamReader(in, "GBK"));
			}
			/**
			 * 1 00:00:01,220 --> 00:00:03,220 
			 */
			while ((line = br.readLine()) != null) {
				if (!line.equals("")) {
					sb.append(line).append("@");
					continue;
				}
				String[] parseStrs = sb.toString().split("@");
				// 该if为了适应一开始就有空行以及其他不符格式的空行情况
				if (parseStrs.length < 3) {
					sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
					continue;
				}
				SRTBean srt = new SRTBean();
				// 解析开始和结束时间
				String timeTotime = parseStrs[1];
				int begin_hour = Integer.parseInt(timeTotime.substring(0, 2));
				int begin_mintue = Integer.parseInt(timeTotime.substring(3, 5));
				int begin_scend = Integer.parseInt(timeTotime.substring(6, 8));
				int begin_milli = Integer.parseInt(timeTotime.substring(9, 12));
				int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend-timeError)
						* 1000 + begin_milli;
				int end_hour = Integer.parseInt(timeTotime.substring(17, 19));
				int end_mintue = Integer.parseInt(timeTotime.substring(20, 22));
				int end_scend = Integer.parseInt(timeTotime.substring(23, 25));
				int end_milli = Integer.parseInt(timeTotime.substring(26, 29));
				int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend-timeError)
						* 1000 + end_milli;
				// System.out.println("开始:" + begin_hour + ":" + begin_mintue
				// + ":" + begin_scend + ":" + begin_milli + "="
				// + beginTime + "ms");
				// System.out.println("结束:" + end_hour + ":" + end_mintue + ":"
				// + end_scend + ":" + end_milli + "=" + endTime + "ms");
				// 解析字幕文字
				String srtBody = "";
				// 可能1句字幕，也可能2句及以上。
				for (int i = 2; i < parseStrs.length; i++) {
					srtBody += parseStrs[i] + "<br/>";
				}
				// 删除最后一个"\n"
				srtBody = srtBody.substring(0, srtBody.length() - 5);
				// 设置SRT
				srt.setBeginTime(beginTime);
				srt.setEndTime(endTime);
				srt.setSrtBody(srtBody);
				// 插入队列
				srtMap.put(key, srt);
				key++;
				sb.delete(0, sb.length());// 清空，否则影响下一个字幕元素的解析
			}
			return srtMap;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				br.close();
				is.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
