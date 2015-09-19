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
import com.vst.itv52.v1.model.AppBean;

public class AppChooseListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AppBean> beans;

	public AppChooseListAdapter(Context context, ArrayList<AppBean> beans) {
		super();
		this.context = context;
		this.beans = beans;
	}

	@Override
	public int getCount() {
		return beans.size();
	}

	@Override
	public Object getItem(int position) {
		return beans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.news_item, null);
			holder = new ViewHolder();
			holder.appIcon = (ImageView) convertView
					.findViewById(R.id.news_img);
			holder.appName = (TextView) convertView
					.findViewById(R.id.news_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		AppBean bean = beans.get(position);
		holder.appName.setText(bean.getName());
		holder.appIcon.setImageDrawable(bean.getIcon());
		return convertView;
	}

	private class ViewHolder {
		private TextView appName;
		private ImageView appIcon;
	}

}
