package com.vst.itv52.v1.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class MySpinner extends Spinner implements OnItemClickListener {

	public MySpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init();
	}

	public MySpinner(Context context) {
		super(context);
		// init();
	}

	public void init() {
		mPopup = new PopupWindow(getContext());
		list = new ListView(getContext());
		mPopup.setWindowLayoutMode(-2, -2);
		mPopup.setBackgroundDrawable(new BitmapDrawable());
		mPopup.setFocusable(true);
		mPopup.setOutsideTouchable(true);
		list.setLayoutParams(new LayoutParams(-2, -2));
		list.setOnItemClickListener(this);
		mPopup.setContentView(list);
	}

	private PopupWindow mPopup = null;
	private ListView list = null;
	private int selection = 0;

	public void setSelection(int selection) {
		this.selection = selection;
	}

	@Override
	public boolean performClick() {
		init();

		SpinnerAdapter adapter = getAdapter();
		if (adapter instanceof BaseAdapter) {
			list.setAdapter((BaseAdapter) adapter);
			// list.setSelection(selection);
		}
		mPopup.showAsDropDown(this);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		setSelection(position);
		if (mPopup.isShowing()) {
			mPopup.dismiss();
		}
	}

}
