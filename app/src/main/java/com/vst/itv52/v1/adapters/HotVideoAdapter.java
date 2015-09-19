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
import com.vst.itv52.v1.model.VideoInfo;
import com.vst.itv52.v1.util.FirstImageAsyncTaskUtil;

public class HotVideoAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<VideoInfo> beans;

	public HotVideoAdapter(Context context, ArrayList<VideoInfo> beans) {
		super();
		this.context = context;
		if (beans != null) {
			this.beans = beans;
		} else {
			this.beans = new ArrayList<VideoInfo>();
		}
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
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.video_details_recommend_item, null);
			holder.poster = (ImageView) convertView
					.findViewById(R.id.details_recommend_poster);
			holder.videoName = (TextView) convertView
					.findViewById(R.id.details_recommend_name);
			holder.refimg = (ImageView) convertView
					.findViewById(R.id.details_recommend_ref);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		VideoInfo info = beans.get(position);
		holder.videoName.setText(info.title);
		FirstImageAsyncTaskUtil task = new FirstImageAsyncTaskUtil(context,
				beans.get(position).img);
		task.setParams(holder.poster, holder.refimg, 50, null);
		task.execute();
		return convertView;
	}

	class ViewHolder {
		private ImageView poster;
		private TextView videoName;
		private ImageView refimg;
	}

}
