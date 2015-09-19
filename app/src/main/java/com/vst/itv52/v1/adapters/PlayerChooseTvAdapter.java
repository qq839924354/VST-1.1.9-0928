package com.vst.itv52.v1.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.VideoSet;

public class PlayerChooseTvAdapter extends BaseAdapter {
	private Context context;
	private List<VideoSet> childSets;
	private int groupIndex;

	public PlayerChooseTvAdapter(Context context, List<VideoSet> childSets,
			int groupIndex) {
		super();
		this.context = context;
		this.childSets = childSets;
		this.groupIndex = groupIndex;
	}

	public void setDataChanged(List<VideoSet> childSets, int groupIndex) {
		this.childSets = childSets;
		this.groupIndex = groupIndex;
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
		convertView = createSetBTN(groupIndex * 30 + position);
		return convertView;
	}

	private View createSetBTN(int index) {
		final TextView btn = new TextView(context);
		btn.setWidth(120);
		btn.setHeight(55);
		btn.setText("第" + (index + 1) + "集");
		btn.setTextSize(18);
		btn.setTag(index - 1);
		btn.setGravity(Gravity.CENTER);
		btn.setBackgroundResource(R.drawable.video_details_btn_selector);
		btn.setTextColor(Color.LTGRAY);
		return btn;
	}

}
