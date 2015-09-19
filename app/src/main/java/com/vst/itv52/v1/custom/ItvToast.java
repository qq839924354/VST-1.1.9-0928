package com.vst.itv52.v1.custom;

import com.vst.itv52.v1.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ItvToast extends Toast {
	private ImageView img;
	private TextView tv;
	private Context context;

	public ItvToast(Context context) {
		super(context);
		this.context = context;
		try {
			//这里可能会抛出加载不到正确资源的bug
			View view = LayoutInflater.from(context).inflate(R.layout.itv_toast,
					null);
			img = (ImageView) view.findViewById(R.id.itv_toast_img);
			tv = (TextView) view.findViewById(R.id.itv_toast_text);
			setView(view);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTextSize(float size) {
		tv.setTextSize(size);
	}

	public void setTextColor(int colorRes) {
		tv.setTextColor(context.getResources().getColor(colorRes));
	}

	public void setIcon(int imgRes) {
		img.setImageResource(imgRes);
	}

	public void setIcon(Drawable imgDr) {
		img.setImageDrawable(imgDr);
	}

	public void removeIcon() {
		img.setImageBitmap(null);
	}

	public void setText(String msg) {
		tv.setText(msg);
	}

	public void setText(int stringRes) {
		tv.setText(stringRes);
		// Toast.makeText(context, text, duration)
	}

	public static ItvToast makeText(Context context, String text, int duration) {
		ItvToast toast = new ItvToast(context);
		toast.setText(text);
		toast.setDuration(duration);
		return toast;
	}

	public static ItvToast makeText(Context context, int textRes, int duration) {
		ItvToast toast = new ItvToast(context);
		toast.setText(textRes);
		toast.setDuration(duration);
		return toast;
	}

	public static ItvToast makeText(Context context, int textRes, int duration,
			int imgRes) {
		ItvToast toast = new ItvToast(context);
		toast.setText(textRes);
		toast.setDuration(duration);
		toast.setIcon(imgRes);
		return toast;
	}

	public static ItvToast makeText(Context context, int textRes, int duration,
			Drawable imgDr) {
		ItvToast toast = new ItvToast(context);
		toast.setText(textRes);
		toast.setDuration(duration);
		toast.setIcon(imgDr);
		return toast;
	}

	public static ItvToast makeText(Context context, String text, int duration,
			Drawable imgDr) {
		ItvToast toast = new ItvToast(context);
		toast.setText(text);
		toast.setDuration(duration);
		toast.setIcon(imgDr);
		return toast;
	}

}
