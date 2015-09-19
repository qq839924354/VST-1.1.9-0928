package com.vst.itv52.v1.adapters;

import java.util.ArrayList;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class AllPagesAdapter extends PagerAdapter {
	private ArrayList<View> views;

	public AllPagesAdapter(ArrayList<View> views) {

		super();
		this.views = views;

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);

	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {

	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View child = views.get(position);
		container.addView(child);
		return child;
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;

	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	public void dataChanged(ArrayList<View> views) {
		this.views = views;
		notifyDataSetChanged();
	}

}
