package com.vst.itv52.v1.adapters;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.AppBean;

public class AppShowTopAdapter extends BaseAdapter {
	private HashMap<Integer, AppBean> topApps;
	private Context context;

	public AppShowTopAdapter(HashMap<Integer, AppBean> topApps, Context context) {
		super();
		this.topApps = topApps;
		this.context = context;
	}

	public void changData(HashMap<Integer, AppBean> topApps) {
		if (topApps == null) {
			topApps = new HashMap<Integer, AppBean>();
		}
		this.topApps = topApps;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return 9;
	}

	@Override
	public Object getItem(int position) {
		return topApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.app_dia_totop_item, null);
			holder.appIcon = (ImageView) convertView
					.findViewById(R.id.app_icon);
			holder.appName = (TextView) convertView.findViewById(R.id.app_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (topApps.get(position) != null) {
			holder.appIcon.setImageDrawable(topApps.get(position).getIcon());
			holder.appName.setText(topApps.get(position).getName());
		} else {
			holder.appIcon.setImageResource(R.drawable.add_apps);
			holder.appName.setText("（未添加）");
		}
		return convertView;
	}

	private class ViewHolder {
		private ImageView appIcon;
		private TextView appName;
	}

}
