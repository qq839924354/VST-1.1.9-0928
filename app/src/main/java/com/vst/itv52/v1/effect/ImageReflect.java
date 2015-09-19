package com.vst.itv52.v1.effect;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.view.View;
import android.view.View.MeasureSpec;

public class ImageReflect {
	/**
	 * 图片倒影效果实现
	 */
	private static int reflectImageHeight = 100;

	/**
	 * view界面转换成bitmap
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	/**
	 * 将bitmap设置倒影
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap bitmap, int reflectHeight) {

		int width = bitmap.getWidth();

		int height = bitmap.getHeight();
		if (height <= reflectHeight) {
			return null;

		}

		Matrix matrix = new Matrix();

		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height
				- reflectHeight, width, reflectHeight, matrix, true);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, reflectHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(reflectionImage, 0, 0, null);
		LinearGradient shader = new LinearGradient(0, 0, 0,
				bitmapWithReflection.getHeight()

				, 0x80ffffff, 0x00ffffff, TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setShader(shader);

		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint);
		return bitmapWithReflection;

	}

	public static Bitmap createCutReflectedImage(Bitmap bitmap, int cutHeight) {

		int width = bitmap.getWidth();

		int height = bitmap.getHeight();
		int totleHeight = reflectImageHeight + cutHeight;

		if (height <= totleHeight) {
			return null;
		}

		Matrix matrix = new Matrix();

		matrix.preScale(1, -1);

		System.out.println(height - reflectImageHeight - cutHeight);
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height
				- reflectImageHeight - cutHeight, width, reflectImageHeight,
				matrix, true);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				reflectImageHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(reflectionImage, 0, 0, null);
		LinearGradient shader = new LinearGradient(0, 0, 0,
				bitmapWithReflection.getHeight()

				, 0x80ffffff, 0x00ffffff, TileMode.CLAMP);

		Paint paint = new Paint();
		paint.setShader(shader);

		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, 0, width, bitmapWithReflection.getHeight(), paint);
		if (!reflectionImage.isRecycled()) {
			reflectionImage.recycle();
		}
		// if (!bitmap.isRecycled()) {
		// bitmap.recycle();
		// }
		System.gc();
		return bitmapWithReflection;

	}
}
