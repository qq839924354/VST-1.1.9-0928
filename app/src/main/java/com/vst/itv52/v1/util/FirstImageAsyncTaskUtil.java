package com.vst.itv52.v1.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.vst.itv52.v1.effect.ImageReflect;

public class FirstImageAsyncTaskUtil extends AsyncTask<String, Integer, Bitmap> {
	private String path;
	private ImageView imageView;
	private ImageView refimg;
	private Context context;
	private int refimgHeight = 0;
	private View view;

	public FirstImageAsyncTaskUtil(Context context, String path) {
		super();
		this.context = context;
		this.path = path;
	}

	public void setParams(ImageView imageView, ImageView refImageView,
			int refImgHeight, View view) {
		this.imageView = imageView;
		this.refimg = refImageView;
		this.refimgHeight = refImgHeight;
		this.view = view;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		System.out.println("-----------onPreExecute------------");
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		// TODO Auto-generated method stub
		System.out.println("-----------doInBackground------------");
		return BitmapUtil.getBitmap(context, path,true);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (result != null && imageView != null) {
			imageView.setImageBitmap(result);
			if (refimg != null) {
				// ImageReflect reflect = new ImageReflect(refimgHeight);
				if (view != null) {
					result = ImageReflect.convertViewToBitmap(view);
				}
				refimg.setImageBitmap(ImageReflect.createReflectedImage(result,
						refimgHeight));
			}
		} else {

		}
	}

	public Bitmap getNetImage() {
		URL url = null;
		Bitmap bitmap = null;
		InputStream is;
		try {
			// System.out.println("!!!!!!!!!!"+path);
			url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();// 这是网络读取的输入流
			// int length = (int) conn.getContentLength();
			// if (length != -1) {
			// byte[] imgData = new byte[length];
			// byte[] buffer = new byte[2048];
			// int readLen = 0;
			// int destPos = imgData.length;
			// while ((readLen = is.read(buffer)) > 0) {
			// System.arraycopy(buffer, 0, imgData, destPos, readLen);
			// destPos += readLen;
			// }
			// bitmap = BitmapFactory.decodeByteArray(imgData, 0,
			// imgData.length);
			// is.close();
			// System.gc();
			// };
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			System.gc();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}
