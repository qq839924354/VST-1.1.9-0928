package com.vst.itv52.v1.adapters;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;

import com.vst.itv52.v1.R;

public class DetailsKeyTabAdapter extends BaseAdapter {
	private Context context;
	private List<String> list;
	private int selectedTab;

	public DetailsKeyTabAdapter(Context context, List<String> list) {
		this.context = context;
		TypedArray arr = context.obtainStyledAttributes(R.styleable.MyGallery);
		arr.recycle();
		if (list == null) {
			list = Collections.emptyList();
		} else {
			this.list = list;
		}
	}

	public void setSelectedTab(int tab) {
		if (tab != selectedTab) {
			selectedTab = tab;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Button btn = new Button(context);
		btn.setTextColor(Color.LTGRAY);
		btn.setTextSize(20);
		btn.setSingleLine(true);// 单行
		btn.setEllipsize(TruncateAt.MARQUEE);// 跑马灯
		btn.setMarqueeRepeatLimit(3);// 无限重复
		btn.setText(list.get(position));// ѭ��
		btn.setLayoutParams(new Gallery.LayoutParams(120, 55));
		btn.setGravity(Gravity.CENTER);
		btn.setBackgroundResource(R.drawable.video_details_btn10_selector);
		return btn;
	}

}
