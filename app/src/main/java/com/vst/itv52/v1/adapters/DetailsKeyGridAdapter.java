package com.vst.itv52.v1.adapters;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class DetailsKeyGridAdapter extends BaseAdapter {
	private List<String> list;
	private Context context;

	public DetailsKeyGridAdapter(Context context, List<String> list) {
		this.context = context;
		if (list != null) {
			this.list = list;
		} else {
			list = Collections.emptyList();
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
		TextView text = new TextView(context);
		text.setTextColor(Color.LTGRAY);
		text.setTextSize(20);
		text.setSingleLine(true);// 单行
		text.setEllipsize(TruncateAt.MARQUEE);// 跑马灯
		text.setMarqueeRepeatLimit(3);// 无限重复
		text.setText(list.get(position));
		text.setLayoutParams(new AbsListView.LayoutParams(130, 55));
		text.setGravity(Gravity.CENTER);
		text.setBackgroundResource(R.drawable.video_details_btn10_selector);
		return text;
	}

}
