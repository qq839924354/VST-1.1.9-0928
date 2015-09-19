package com.vst.itv52.v1.view;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;

public class MyGallery extends Gallery implements OnItemSelectedListener {
	private static final int MSG_ZOOM_IN = 1;
	private static final long DELAY = 100;
	private View mPrev;

	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnItemSelectedListener(this);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (mPrev != null) {
			zoomOut();
		}
		mPrev = view;
		mGalleryHandler.removeCallbacksAndMessages(view);
		Message msg = Message.obtain(mGalleryHandler, MSG_ZOOM_IN, view);
		mGalleryHandler.sendMessageDelayed(msg, DELAY);
	}

	public void onNothingSelected(AdapterView<?> parent) {

		if (mPrev != null) {
			zoomOut();
			mPrev = null;
		}
	}

	private void zoomOut() {
		if (mGalleryHandler.hasMessages(MSG_ZOOM_IN, mPrev)) {
			mGalleryHandler.removeCallbacksAndMessages(mPrev);
		} else {
			ZoomAnimation a = (ZoomAnimation) mPrev.getAnimation();

			if (a != null) {
				a.resetForZoomOut();
				mPrev.startAnimation(a);
			}
		}
	}

	Handler mGalleryHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			if (msg.what == MSG_ZOOM_IN) {
				View view = (View) msg.obj;
				Animation a = new ZoomAnimation(view, 1, 1.2f, 0, 100);
				view.startAnimation(a);
			}
		}
	};

	class ZoomAnimation extends Animation {
		private float mFrom;
		private float mTo;
		private float mOffsetY;
		private int mPivotX;
		private int mPivotY;
		private float mInterpolatedTime;

		public ZoomAnimation(View v, float from, float to, float offsetY,
				int duration) {
			super();
			mFrom = from;
			mTo = to;
			mOffsetY = offsetY * v.getHeight();
			setDuration(duration);
			setFillAfter(true);
			mPivotX = v.getWidth() / 2;
			mPivotY = v.getHeight() / 2;
		}

		public void resetForZoomOut() {
			reset();
			mOffsetY = 0;
			mFrom = mFrom + (mTo - mFrom) * mInterpolatedTime;
			mTo = 1;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {

			float s = mFrom + (mTo - mFrom) * interpolatedTime;
			Matrix matrix = t.getMatrix();
			matrix.preScale(s, s, mPivotX, mPivotY);
		}
	}
}
