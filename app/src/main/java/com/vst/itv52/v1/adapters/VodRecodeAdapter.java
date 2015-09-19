package com.vst.itv52.v1.adapters;

import java.util.ArrayList;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.VodRecode;

public class VodRecodeAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<VodRecode> recodes;
	// 表示当前适配器是否允许多选
	private boolean multiChoose = false;
	private FinalBitmap fb;
	private ArrayList<Integer> slectPositions;// 记录选中的位置

	public VodRecodeAdapter(Context context, ArrayList<VodRecode> videos) {
		this.mContext = context;
		setRecodes(videos);
		fb = FinalBitmap.create(context, context.getCacheDir().toString());
		fb.configLoadingImage(R.drawable.default_film_img);
		fb.configTransitionDuration(1000);
	}

	private void setRecodes(ArrayList<VodRecode> recodes) {
		if (recodes == null) {
			this.recodes = new ArrayList<VodRecode>();
		} else {
			this.recodes = recodes;
		}
	}

	public void setChoiceMode(boolean choiceable) {
		this.multiChoose = choiceable;
		if (choiceable == true) {
			slectPositions = new ArrayList<Integer>();
		} else {
			slectPositions = null;
		}
		notifyDataSetChanged();
	}

	public boolean getChoiceMode() {
		return this.multiChoose;
	}

	public ArrayList<Integer> getSlectPositions() {
		return slectPositions;
	}

	public void setSlectPositions(ArrayList<Integer> positions) {
		if (positions != null) {
			this.slectPositions = positions;
		} else {
			this.slectPositions = new ArrayList<Integer>();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return recodes.size();
	}

	@Override
	public Object getItem(int position) {
		return recodes.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.type_details_item, null);
			holder.superHd = (ImageView) convertView
					.findViewById(R.id.video_superHD);
			holder.poster = (ImageView) convertView
					.findViewById(R.id.video_poster);
			holder.banben = (TextView) convertView
					.findViewById(R.id.video_banben);
			holder.videoName = (TextView) convertView
					.findViewById(R.id.video_name);
			holder.gou = (ImageView) convertView.findViewById(R.id.video_gou);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 取得单个item的数据
		VodRecode typeDetails = recodes.get(position);
		holder.videoName.setText(typeDetails.title);
		holder.banben.setText(typeDetails.banben);
		if (typeDetails.banben.contains("高清")) {
			holder.superHd.setImageResource(R.drawable.sd);
			holder.superHd.setVisibility(View.VISIBLE);
		} else if (typeDetails.banben.contains("超清")) {
			holder.superHd.setImageResource(R.drawable.hd);
			holder.superHd.setVisibility(View.VISIBLE);
		} else {
			holder.superHd.setVisibility(View.GONE);
		}
		if (slectPositions != null && slectPositions.contains(position)) {
			holder.gou.setVisibility(View.VISIBLE);
		} else {
			holder.gou.setVisibility(View.GONE);
		}
		fb.display(holder.poster, typeDetails.imgUrl);
		return convertView;
	}

	public void changeData(ArrayList<VodRecode> recodes) {
		setRecodes(recodes);
		notifyDataSetChanged();
	}

	public void changData(Integer position) {
		if (multiChoose) {
			if (slectPositions.contains(position)) {
				slectPositions.remove(position);
			} else {
				slectPositions.add(position);
			}
			notifyDataSetChanged();
		}
	}

	public void chooseAll() {
		slectPositions = new ArrayList<Integer>();
		for (int i = 0; i < recodes.size(); i++) {
			slectPositions.add((Integer) i);
		}
		notifyDataSetChanged();
	}

	class ViewHolder {
		private ImageView superHd;
		private ImageView poster;
		private ImageView gou;
		private TextView banben;
		private TextView videoName;
	}

}
