package com.vst.itv52.v1.effect;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class AnimationSetUtils {
	static int num = 0;

	public static void SetFlickerAnimation(final TextView v, final String info,
			final String temp) {
		v.setText(info);
		AlphaAnimation anim = new AlphaAnimation(0, 1);
		anim.setDuration(4000);
		anim.setRepeatCount(Animation.INFINITE);
		// anim1.setRepeatMode(Animation.REVERSE);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				num++;
				if (num == 10) {
					num = 0;
				}
				if (num % 2 == 0) {
					v.setText(info);
				} else {
					v.setText(temp);
				}
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub

			}
		});
		v.startAnimation(anim);

	}

	public static void SetMenuAnimation(final ImageView v, final int bg1,
			final int bg2) {
		v.setImageResource(bg1);
		AlphaAnimation anim = new AlphaAnimation(0.8f, 1);
		anim.setDuration(1000);
		anim.setRepeatCount(Animation.INFINITE);
		// anim1.setRepeatMode(Animation.REVERSE);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				num++;
				if (num == 10) {
					num = 0;
				}
				if (num % 2 == 0) {
					v.setImageResource(bg1);
				} else {
					v.setImageResource(bg2);
				}
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub

			}
		});
		v.startAnimation(anim);

	}

	public static Animation createScaleAnimation(float fromXScale,
			float toXScale, float fromYScale, float toYScale, long duration) {
		ScaleAnimation anim = new ScaleAnimation(fromXScale, toXScale,
				fromYScale, toYScale, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setFillAfter(true);
		anim.setInterpolator(new AccelerateInterpolator());
		anim.setDuration(duration);
		return anim;
	}

	public static Animation createAlphaAnimation(float fromAlpha,
			float toAlpha, long duration) {
		AlphaAnimation anim = new AlphaAnimation(fromAlpha, toAlpha);
		anim.setDuration(duration);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}

}
