package com.vst.itv52.v1.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.NewsBean;

public class NewsListAdapter extends BaseAdapter {
	private ArrayList<NewsBean> newses;
	private Context context;
	private int playPosition = -1;

	public NewsListAdapter(ArrayList<NewsBean> newses, Context context) {
		super();
		if (newses != null) {
			this.newses = newses;
		} else {
			this.newses = new ArrayList<NewsBean>();
		}
		this.context = context;
	}

	public void stateChange(int position) {
		this.playPosition = position;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return newses.size();
	}

	@Override
	public Object getItem(int position) {
		return newses.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(R.layout.news_item,
				null);
		ImageView newsImg = (ImageView) convertView.findViewById(R.id.news_img);
		TextView newsTitle = (TextView) convertView
				.findViewById(R.id.news_text);
		NewsBean newsInfo = newses.get(position);
		if (playPosition == position) {
			newsImg.setImageResource(R.drawable.osd_play_hl);
			newsTitle.setText(newsInfo.title + "    （再次点击全屏）");
		} else {
			newsTitle.setText(newsInfo.title);
		}
		return convertView;
	}

}
