package com.vst.itv52.v1.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.ChannelColumnBean;

public class TVBackColumnsAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ChannelColumnBean> columns;
	private String date;
	private int playingPosition = -1;// 正在播放的栏目的位置

	public TVBackColumnsAdapter(Context context,
			ArrayList<ChannelColumnBean> columns, String date, int playPostion) {
		super();
		this.context = context;
		this.date = date;
		this.playingPosition = playPostion;
		if (columns != null) {
			this.columns = columns;
		} else {
			this.columns = new ArrayList<ChannelColumnBean>();
		}
	}

	public void dataChanged(int position) {
		this.playingPosition = position;
		notifyDataSetChanged();
	}

	public void dataChanged(ArrayList<ChannelColumnBean> columns) {
		if (columns != null) {
			this.columns = columns;
		} else {
			this.columns = new ArrayList<ChannelColumnBean>();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return columns.size();
	}

	@Override
	public Object getItem(int position) {
		return columns.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.tv_back_column_item, null);
		TextView time = (TextView) convertView.findViewById(R.id.tv_back_time);
		TextView name = (TextView) convertView.findViewById(R.id.tv_back_name);
		ImageView log = (ImageView) convertView.findViewById(R.id.tv_back_log);
		ChannelColumnBean column = columns.get(position);
		time.setText(column.getTime());
		name.setText(column.getChannelText());
		if (position < columns.size() - 1) {
			int flag = isBeforeCurrentTime(date, column.getTime(),
					columns.get(position + 1).getTime());
			if (flag == 1) {
				log.setImageResource(R.drawable.huikan);
			} else if (flag == 2) {
				log.setImageResource(R.drawable.live);
			} else {
				log.setVisibility(View.GONE);
			}
			convertView.setTag(flag);
		} else {
			int flag = isBeforeCurrentTime(date, column.getTime(), null);
			if (flag == 3) {
				log.setVisibility(View.GONE);
			} else if (flag == 2) {
				log.setImageResource(R.drawable.live);
			} else {
				log.setImageResource(R.drawable.huikan);
			}
			convertView.setTag(flag);
		}
		if (playingPosition == position) {
			log.setImageResource(R.drawable.onplay);
			name.setText(column.getChannelText() + "    （再次点击全屏）");
		}
		return convertView;
	}

	/**
	 * 判断栏目时间是否超过当前时间
	 * 
	 * @param colDate
	 *            栏目日期
	 * @param time
	 *            当前栏目时间
	 * @param nextTime
	 *            下一个栏目时间
	 * @return true 為超前
	 */
	private int isBeforeCurrentTime(String colDate, String time, String nextTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date1 = null;
		String dat1 = colDate + " " + time;
		try {
			date1 = sdf.parse(dat1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long colTime = date1.getTime();
		// 当前栏目具有下一个栏目
		if (nextTime != null) {
			Date date2 = null;
			String dat2 = null;
			dat2 = colDate + " " + nextTime;
			try {
				date2 = sdf.parse(dat2);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			long colNextTime = date2.getTime();
			// 系统时间大于当前栏目，小于下一个栏目，标志直播
			if (System.currentTimeMillis() < colNextTime
					&& System.currentTimeMillis() > colTime) {
				return 2;// 直播
			} else if (System.currentTimeMillis() < colTime) {
				return 3;// 不标记
			} else {
				return 1;// 回看
			}
			// 当前栏目不具有下一个栏目，
		} else {
			// 当前系统时间小于栏目时间，不标记
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			String currdate = sdf.format(new Date());
			Log.i("info", "最后一栏，系统时间" + System.currentTimeMillis() + "栏目时间"
					+ colTime);
			if (System.currentTimeMillis() < colTime) {
				return 3;// 不标记
			} else if (!currdate.equals(colDate)) {
				return 1;// 回看
			} else {
				return 2;// 直播
			}
		}
	}
}
