package com.vst.itv52.v1.adapters;

import java.util.List;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.VideoSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PlayerChooseArtAdapter extends BaseAdapter {
	private Context context;
	private List<VideoSet> childSets;
	private int groupIndex;

	public PlayerChooseArtAdapter(Context context, List<VideoSet> childSets,
			int groupIndex) {
		super();
		this.context = context;
		this.childSets = childSets;
		this.groupIndex = groupIndex;
	}

	public void setDataChanged(List<VideoSet> childSets) {
		this.childSets = childSets;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return childSets.size();
	}

	@Override
	public Object getItem(int position) {
		return childSets.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.player_choose_art_item, null);
		}
		TextView setName = (TextView) convertView
				.findViewById(R.id.player_choose_text);
		setName.setText(childSets.get(position).setName);
		return convertView;
	}

}
