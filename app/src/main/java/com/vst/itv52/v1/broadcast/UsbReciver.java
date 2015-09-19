package com.vst.itv52.v1.broadcast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.db.VSTDBHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;

/**
 * 插入U盘 扫描 U盘中 的 文件夹 和 tv.txt 文件
 * 
 * @author shenhui
 * 
 */
public class UsbReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("starttime = " + System.currentTimeMillis());
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			Uri uri = intent.getData();
			File dir = new File(uri.getPath());
			ArrayList<String[]> list = getAllCustomer(dir);
			if (list != null && list.size() > 0) {
				int add_num =0;
				// 有自定义的列表 操作 数据库 删除 老的
				ArrayList<LiveChannelInfo> channels = new ArrayList<LiveChannelInfo>();
				for (int i = 0; i < list.size() && i < 999; i++) {
					add_num = add_num + 1;
					String url_1 = list.get(i)[0].replace(";", "#");
					String url_2 = list.get(i)[1].replace(";", "#");
					LiveChannelInfo live = new LiveChannelInfo();
					live.vid = i;
					live.num = (i + 1) + 5000;
					live.quality = "SD";
					if (url_1.contains("http") || url_1.contains("rtsp") || url_1.contains("rtmp") || url_1.contains("mms"))
					{
						live.vname = url_2;
						live.liveSources = new String[] { url_1 };
					}
					else
					{
						live.vname = url_1;
						live.liveSources = new String[] { url_2 };
					}
					live.tid = new String[] { VSTDBHelper.CUSTOM_TID };
					channels.add(live);
				}
				LiveDataHelper.getInstance(context).deleteChannels(VSTDBHelper.CUSTOM_TID);
				LiveDataHelper.getInstance(context).insertChannels(channels);
				Toast.makeText(context, "VST全聚合提示：在U盘(TV.TXT)找到自定义频道："+String.valueOf(add_num)+" 个", Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(context, "VST全聚合提示：没有在U盘找到新频道！！！", Toast.LENGTH_SHORT).show();
			}
			list = null;
		}
		System.out.println("endtime = " + System.currentTimeMillis());
	}

	private ArrayList<String[]> parseTxt(File file) {
		ArrayList<String[]> list = new ArrayList<String[]>();
		BufferedReader reader; 
		try {
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			System.out.println("开始读U盘tv.txt自定义列表了。。。。。。。。");
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream in = new BufferedInputStream(fis);
			in.mark(4);
			byte[] first3bytes = new byte[3];
			in.read(first3bytes);
			in.reset();
			if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {
				System.out.println("嗯，识别到文件编码格式了，是：UTF-8");
				reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			} else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
				System.out.println("嗯，识别到文件编码格式了，是：Unicode");
				reader = new BufferedReader(new InputStreamReader(in, "unicode"));
			} else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
				System.out.println("嗯，识别到文件编码格式了，是：UTF-16BE");
				reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
			} else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
				System.out.println("嗯，识别到文件编码格式了，是：UTF-16LE");
				reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
			} else {
				System.out.println("嗯，识别到文件编码格式了，是：GBK");
				reader = new BufferedReader(new InputStreamReader(in, "GBK"));
			}
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] strs = line.split("[,，| ]", 2);
				if (strs.length == 2) {
					list.add(strs);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private ArrayList<String[]> getAllCustomer(File dir) { // 可能是空文件夹 //扫描
		ArrayList<String[]> lists = new ArrayList<String[]>();
		
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				File file = new File(dir, filename);
				if (file.isDirectory() || filename.equalsIgnoreCase("tv.txt")) {
					return true;
				}
				return false;
			}
		});

		if (files == null || files.length == 0) {
			return lists;
		}
		for (File file : files) {
			if (file.isFile()) {
				lists.addAll(parseTxt(file));
			} else {
				lists.addAll(getAllCustomer(file));
			}
		}
		return lists;
	}
}
