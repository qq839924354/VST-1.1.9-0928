package com.vst.itv52.v1.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.AppBean;

public class ApplicationAdapter extends BaseAdapter {
	private ArrayList<AppBean> lancherInfos;
	private Context context;
	// private int index = -1;
	private int[] bgSelector = { R.drawable.dark_no_shadow,
			R.drawable.blue_no_shadow, R.drawable.green_no_shadow,
			R.drawable.orange_no_shadow, R.drawable.pink_no_shadow };
	private int randomTemp = -1;// 随机数缓存，用于避免连续产生2个相同的随机数
	private int bgSize = bgSelector.length;

	public void setLauncher(ArrayList<AppBean> infos) {
		if (infos != null) {
			this.lancherInfos = infos;
		} else {
			this.lancherInfos = new ArrayList<AppBean>();
		}
	}

	public ApplicationAdapter(Context context, ArrayList<AppBean> infos) {
		this.context = context;
		this.setLauncher(infos);
	}

	@Override
	public int getCount() {
		return lancherInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return lancherInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void changeData(ArrayList<AppBean> dateList) {
		this.lancherInfos = dateList;
		notifyDataSetChanged();
	}

	// public void changeData(ArrayList<AppBean> dataList, int index) {
	// this.lancherInfos = dataList;
	// this.index = index;
	// notifyDataSetChanged();
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(context).inflate(
				R.layout.app_list_item, null);
		ImageView appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
		TextView tvName = (TextView) convertView.findViewById(R.id.app_name);
		ImageView newApp = (ImageView) convertView.findViewById(R.id.new_app);
		AppBean info = lancherInfos.get(position);
		appIcon.setImageDrawable(info.getIcon());
		tvName.setText(info.getName());
		// if (isANewApp(info)) {
		// newApp.setVisibility(View.VISIBLE);
		// }
		convertView.setLayoutParams(new GridView.LayoutParams(160, 160));
		// convertView.setBackgroundResource(bgSelector[createRandom(bgSize)]);
		return convertView;
	}

	private int createRandom(int size) {
		Random random = new Random();
		int randomIndex = random.nextInt(size);
		// 如果本次随机与上次一样，重新随机
		while (randomIndex == randomTemp) {
			randomIndex = random.nextInt(size);
		}
		randomTemp = randomIndex;
		return randomTemp;
	}

	private boolean isANewApp(AppBean appInfo) {
		long installTime = new File(appInfo.getDataDir()).lastModified();
		long newDuration = 3 * 24 * 60 * 60 * 1000;
		if (installTime + newDuration >= System.currentTimeMillis()) {
			return true;
		} else {
			return false;
		}
	}
}
