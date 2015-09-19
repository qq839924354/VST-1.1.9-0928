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
import com.vst.itv52.v1.model.LiveChannelInfo;

public class ChannelListAdapter extends BaseAdapter {

	private ArrayList<LiveChannelInfo> channelList;
	private Context context;

	public ChannelListAdapter(Context context,
			ArrayList<LiveChannelInfo> channelList) {
		this.context = context;
		setChannelList(channelList);
	}

	public void setChannelList(ArrayList<LiveChannelInfo> channelList) {
		if (channelList == null) {
			this.channelList = new ArrayList<LiveChannelInfo>();
		} else {
			this.channelList = channelList;
		}
	}

	@Override
	public int getCount() {
		return channelList.size();
	}

	@Override
	public Object getItem(int position) {
		return channelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LiveChannelInfo channel = channelList.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.live_channel_list_item, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) convertView
					.findViewById(R.id.live_channellist_item_name_txt);
			holder.txtNum = (TextView) convertView
					.findViewById(R.id.live_channellist_item_num_txt);
			holder.imgSharp = (ImageView) convertView
					.findViewById(R.id.live_channellist_item_sharp_img);
			holder.imgFav = (ImageView) convertView
					.findViewById(R.id.live_channellist_item_fav_img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txtName.setText(channel.vname);

		holder.txtNum.setText(String.valueOf(channel.num));
		// if (channel.num > 0) {
		// holder.txtNum.setText(String.valueOf(channel.num));
		// } else {
		// holder.txtNum.setText("NONE");
		// }
		if (channel.quality == null) {
			holder.imgSharp
					.setImageResource(R.drawable.live_channel_list_item_sharp_default);
		} else if (channel.quality.equalsIgnoreCase("hd")) {
			holder.imgSharp
					.setImageResource(R.drawable.live_channel_list_item_sharp_hd);
		} else if (channel.quality.equalsIgnoreCase("sd")) {
			holder.imgSharp
					.setImageResource(R.drawable.live_channel_list_item_sharp_sd);
		}

		holder.imgFav.setVisibility(channel.favorite ? View.VISIBLE
				: View.INVISIBLE);

		return convertView;
	}

	class ViewHolder {
		TextView txtName;
		TextView txtNum;
		ImageView imgSharp;
		ImageView imgFav;
	}

}