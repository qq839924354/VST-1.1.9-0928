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

public class TypeDetailsSubMenuAdapter extends BaseAdapter {
	private Context context;
	private int selcted = -1;
	private ArrayList<String> list;

	public TypeDetailsSubMenuAdapter(Context context, ArrayList<String> set) {
		this.context = context;
		if (set != null) {
			this.list = set;
		} else {
			list = new ArrayList<String>();
		}
	}

	public void setSelctItem(int position) {
		this.selcted = position;
		notifyDataSetChanged();
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
		convertView = LayoutInflater.from(context).inflate(
				R.layout.type_details_filter_item, null);
		TextView text = (TextView) convertView.findViewById(R.id.filter_name);
		ImageView gou = (ImageView) convertView.findViewById(R.id.filter_gou);
		if (selcted == position) {
			gou.setVisibility(View.VISIBLE);
			convertView.setBackgroundResource(R.drawable.filter_sleted);
		}
		text.setText(list.get(position));
		return convertView;
	}
}
