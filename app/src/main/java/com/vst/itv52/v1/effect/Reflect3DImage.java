package com.vst.itv52.v1.effect;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;

public class Reflect3DImage {
	/**
	 * 3D效果
	 * 
	 * @param srcBitmap
	 * @return
	 */
	public static Bitmap skewImage(Bitmap srcBitmap, int picWidth,
			int picHeight, int height) {
		srcBitmap = Bitmap.createScaledBitmap(srcBitmap, picWidth, picHeight,
				true);
		Bitmap bitmap = createReflectedImage(srcBitmap, height);
		Camera camera = new Camera();
		camera.save();
		Matrix matrix = new Matrix();
		camera.rotateY(15);
		camera.getMatrix(matrix);
		camera.restore();
		matrix.preTranslate(-bitmap.getWidth() >> 1, -bitmap.getHeight() >> 1);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		Bitmap canvasBitmap = Bitmap.createBitmap(newBitmap.getWidth(),
				newBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(canvasBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		// paint.setStyle(Style.FILL);
		canvas.drawBitmap(newBitmap, 0, 0, paint);
		newBitmap.recycle();
		// bitmap.recycle();
		return canvasBitmap;

	}

	/**
	 * 全倒影
	 * 
	 * @param originalImage
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap originalImage,
			int imageHeight) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height
				- imageHeight, width, imageHeight, matrix, false);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + imageHeight), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		Paint defaultpPaint = new Paint();
		canvas.drawBitmap(originalImage, 0, 0, defaultpPaint);
		canvas.drawBitmap(reflectionImage, 0, height, defaultpPaint);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight(),
				0x70ffffff, 0x00ffffff, TileMode.MIRROR);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight(),
				paint);
		reflectionImage.recycle();
		return bitmapWithReflection;

	}
}
