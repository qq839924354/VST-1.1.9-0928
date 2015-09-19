package com.vst.itv52.v1.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageLoadUtil {
	public static InputStream getImageByUrl(String uri) throws IOException {
		URL url = new URL(uri);
		URLConnection conn = url.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
		return is;
	}

	public static Bitmap getBitmap(String uri) {
		try {
			InputStream is = new URL(uri).openStream();
			return BitmapFactory.decodeStream(is);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void saveSubjectBG(String url) {
		try {
			InputStream is = new URL(url).openStream();

			new ImageFileCache()
					.saveBitmap(url, BitmapFactory.decodeStream(is));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
