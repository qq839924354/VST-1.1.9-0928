package com.vst.itv52.v1.adapters;

import java.util.ArrayList;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.model.ShooterSRTBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchSrtAdapter extends BaseAdapter {
	private TextView srtName;
	private ArrayList<ShooterSRTBean> shooterSRTBeans;
	private Context context;
	
	
	public void setShooterSRTBeans(ArrayList<ShooterSRTBean> shooterSRTBeans) {
		if(shooterSRTBeans!=null){
			this.shooterSRTBeans = shooterSRTBeans;
		}else{
			this.shooterSRTBeans=new ArrayList<ShooterSRTBean>();
		}
	}

	public SearchSrtAdapter(Context context, ArrayList<ShooterSRTBean> shooterSRTBeans){
		setShooterSRTBeans(shooterSRTBeans);
		this.context=context;
	}
	@Override
	public int getCount() {
		return shooterSRTBeans.size();
	}

	@Override
	public ShooterSRTBean getItem(int position) {
		// TODO Auto-generated method stub
		return shooterSRTBeans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView=LayoutInflater.from(context).inflate(R.layout.search_srt_item, null);
		this.srtName=(TextView) convertView.findViewById(R.id.srtName);
		if(shooterSRTBeans.get(position)!=null){
			this.srtName.setText(shooterSRTBeans.get(position).getTitle());
		}
		return convertView;
	}

}
