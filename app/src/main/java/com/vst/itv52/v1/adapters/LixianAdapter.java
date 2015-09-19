package com.vst.itv52.v1.adapters;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.XLLXFileInfo;

public class LixianAdapter extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<XLLXFileInfo> files;

	public LixianAdapter(Context context, ArrayList<XLLXFileInfo> files) {
		super();
		this.context = context;
		setFiles(files);
	}

	public void updateData(ArrayList<XLLXFileInfo> files) {
		setFiles(files);
		notifyDataSetChanged();
	}

	private void setFiles(ArrayList<XLLXFileInfo> files) {
		if (files != null) {
			this.files = files;
		} else {
			this.files = new ArrayList<XLLXFileInfo>();
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return files.get(groupPosition).btFiles[childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.lixian_video_list_item, null);
		ImageView fileType = (ImageView) convertView
				.findViewById(R.id.lixian_videolist_item_filetype);
		TextView filename = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_filename);
		TextView duration = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_duration);
		TextView translate = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_translate);
		Space space = (Space) convertView.findViewById(R.id.lixian_item_space);
		space.setVisibility(View.VISIBLE);
		XLLXFileInfo info = files.get(groupPosition).btFiles[childPosition];
		fileType.setImageResource(R.drawable.lixian_filetype_videofile);
		filename.setText(info.file_name);
		if (info.filesize.trim().equals("null") || info.filesize == null
				|| info.filesize.trim().equals("0")) {
			duration.setText("转码中……");
		} else {
			duration.setText(setFileSize(Long.valueOf(info.filesize)));
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (files.get(groupPosition).isDir) {
			return files.get(groupPosition).btFiles.length;
		} else {
			return 0;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		if (files.size() < files.get(0).recodenum
				&& groupPosition == getGroupCount() - 1) {
			return null;
		} else {
			return files.get(groupPosition);
		}

	}

	@Override
	public int getGroupCount() {
		if (files != null && files.size() > 0
				&& files.size() < files.get(0).recodenum) {
			return files.size() + 1;
		} else if (files != null && files.size() > 0) {
			return files.size();
		} else {
			return 0;
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.lixian_video_list_item, null);
		ImageView fileType = (ImageView) convertView
				.findViewById(R.id.lixian_videolist_item_filetype);
		TextView filename = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_filename);
		TextView duration = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_duration);
		TextView translate = (TextView) convertView
				.findViewById(R.id.lixian_videolist_item_translate);
		if (groupPosition < files.size()) {
			XLLXFileInfo info = files.get(groupPosition);
			if (info.isDir) {
				fileType.setImageResource(R.drawable.lixian_filetype_btdir);
				duration.setText("文件夹");
			} else {
				fileType.setImageResource(R.drawable.lixian_filetype_videofile);
				if (info.filesize.trim().equals("null")
						|| info.filesize == null
						|| info.filesize.trim().equals("0")) {
					duration.setText("转码中……");
				} else {
					duration.setText(setFileSize(Long.valueOf(info.filesize
							.trim())));
				}
			}
			filename.setText(info.file_name);
		} else {
			if (files.size() > 0 && files.size() < files.get(0).recodenum) {
				// convertView = null;
				convertView = LayoutInflater.from(context).inflate(
						android.R.layout.simple_list_item_1, null);
				convertView
						.setBackgroundResource(R.drawable.lixian_videolist_item_bg);
				TextView more = (TextView) convertView
						.findViewById(android.R.id.text1);
				// LayoutParams lp = new LayoutParams(
				// ViewGroup.LayoutParams.MATCH_PARENT,
				// ViewGroup.LayoutParams.WRAP_CONTENT);
				// convertView.setLayoutParams(lp);
				// TextView more = new TextView(context);
				more.setText("加载更多……");
				more.setTextColor(Color.WHITE);
				more.setTextSize(24);
				more.setGravity(Gravity.CENTER);
				// ((LinearLayout) convertView).addView(more);
			}
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/**
	 * 将长整形时间转换成hh:mm:ss
	 * 
	 * @param timeMs
	 * @return
	 */
	private String stringForTime(long timeMs) {
		int totalSeconds = (int) (timeMs / 1000);
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		} else {
			return String.format("%02d:%02d", minutes, seconds);
		}
	}

	/**
	 * 文件大小单位转换
	 * 
	 * @param size
	 * @return
	 */
	private String setFileSize(long size) {
		DecimalFormat df = new DecimalFormat("###.##");
		float f = ((float) size / (float) (1024 * 1024));

		if (f < 1.0) {
			float f2 = ((float) size / (float) (1024));
			return df.format(new Float(f2).doubleValue()) + "KB";

		} else if (f >= 1.0 && f < 1024.0) {
			return df.format(new Float(f).doubleValue()) + "M";
		} else {
			float f3 = f / (float) 1024;
			return df.format(new Float(f3).doubleValue()) + "G";
		}

	}

}
