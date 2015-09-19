package com.vst.itv52.v1.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.VideoSet;

public class DetailsKeyListAdapter extends BaseAdapter {
	private List<VideoSet> sets;
	private LayoutInflater inflater;
	private Context context;

	public DetailsKeyListAdapter(Context context, List<VideoSet> sets) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		if (sets != null) {
			this.sets = sets;
		} else {
			this.sets = new ArrayList<VideoSet>();
		}
	}

	public void setDataChanged(List<VideoSet> sets) {
		this.sets = sets;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return sets.size();
	}

	@Override
	public Object getItem(int position) {
		return sets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.video_details_choose_arts_item,
				null);
		TextView tv = (TextView) convertView.findViewById(R.id.text);
		VideoSet set = sets.get(position);
		tv.setText(set.setName);
		return convertView;
	}

}
