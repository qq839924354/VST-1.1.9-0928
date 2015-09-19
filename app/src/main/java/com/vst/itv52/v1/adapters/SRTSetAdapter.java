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

public class SRTSetAdapter extends BaseAdapter {
	private String[] texts;
	private Context context;
	private int selcted = -1;

	public SRTSetAdapter(String[] texts, Context context) {
		super();
		setTexts(texts);
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
		TextView text = (TextView) convertView
				.findViewById(R.id.srt_set_item_name);
		ImageView gou = (ImageView) convertView
				.findViewById(R.id.srt_set_item_gou);
		if (selcted == position) {
			gou.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.drawable.filter_sleted);
		}
		//设置字幕是从网页上下载的对应的字幕
		text.setText(Html.fromHtml(texts[position]));
		return convertView;
	}

}
