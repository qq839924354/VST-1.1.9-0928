package com.vst.itv52.v1.adapters;


import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class SRTNameAdapter extends BaseAdapter {
	private String[] texts;
	private Context context;
	private int selcted = -1;
	private int netOrFlag;

	public SRTNameAdapter(String[] texts, Context context, int netOrFlag) {
		super();
		setTexts(texts);
		this.netOrFlag = netOrFlag;
		this.context = context;
	}

	public void setSelctItem(int position) {
		this.selcted = position;
		notifyDataSetChanged();
	}

	private void setTexts(String[] texts) {
		if (texts != null) {
			this.texts = texts;
		} else {
			this.texts = new String[] {};
		}
	}

	@Override
	public int getCount() {
		return texts.length;
	}

	@Override
	public Object getItem(int position) {
		return texts[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.srt_set_item, null);
		TextView text;

		text = (TextView) convertView.findViewById(R.id.srt_set_item_name);

		ImageView gou = (ImageView) convertView
				.findViewById(R.id.srt_set_item_gou);
		if (selcted == position) {
			gou.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.drawable.filter_sleted);
		}
		// 如果是直接来至于迅雷离线的字幕
		if (netOrFlag == 1) {
			text.setText(Html.fromHtml(texts[position]));
			// 如果是网络下载的字幕
		} else if (netOrFlag == 2) {
			//取最后一个反斜杠后的字幕名称
			String fileName=texts[position].substring(texts[position].lastIndexOf("\\")+1, texts[position].length());
				text.setText(new String(fileName));
			//将绝对路径设为标记
			text.setTag(texts[position]);
		}
		return convertView;
	}

}
