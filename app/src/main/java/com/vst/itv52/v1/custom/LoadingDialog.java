package com.vst.itv52.v1.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class LoadingDialog extends Dialog {
	// private Context context;
	private TextView loadingMsgTv;

	public LoadingDialog(Context context) {
		super(context, R.style.MyDialog);
		// this.context = context;
		View view = LayoutInflater.from(context).inflate(
				R.layout.itv_loading_dialog, null);
		loadingMsgTv = (TextView) view.findViewById(R.id.loading_tv);
		ImageView loadingImg = (ImageView) view.findViewById(R.id.loading_img);
		Animation rotate = AnimationUtils.loadAnimation(context,
				R.anim.loading_rotate);
		loadingImg.startAnimation(rotate);
		setCancelable(true);
		setContentView(view);
	}

	public void setLoadingMsg(String msg) {
		loadingMsgTv.setText(msg);
	}

	public void setLoadingMsg(int msgId) {
		loadingMsgTv.setText(msgId);
	}

	public void setMsgGone() {
		loadingMsgTv.setVisibility(View.GONE);
	}

}
