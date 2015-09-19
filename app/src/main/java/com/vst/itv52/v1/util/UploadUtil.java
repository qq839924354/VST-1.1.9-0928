package com.vst.itv52.v1.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

/**
 * 上传工具类
 * 
 * @author YINZHIPING
 * 
 *         GMYZ @(#)UploadUtil.java
 * 
 * @version 1.0
 * 
 * @Copyright 2013-1-4 下午1:20:08
 */
public class UploadUtil {
	private static final String TAG = "uploadFile";
	private static final int SO_TIME_OUT = 100 * 1000; // 超时时间

	/**
	 * android上传文件到服务器
	 * 
	 * @param file
	 *            需要上传的文件
	 * @param RequestURL
	 *            请求的rul
	 * @return 返回响应的内容
	 */
	public static boolean uploadFile(File file, String RequestURL) {
		System.out.println("tag7");
		if (file != null && file.exists()) {
			System.out.println(file.getAbsolutePath());
			System.out.println("tag8");
			BasicHttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 5000); // 连接超时
			HttpConnectionParams.setSoTimeout(httpParameters, SO_TIME_OUT);
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			HttpPost post = new HttpPost(RequestURL);
			try {
				System.out.println("tag9");
				MultipartEntity multipartEntity = new MultipartEntity();
				multipartEntity.addPart("file", new FileBody(file));
				multipartEntity.addPart("p", new StringBody("243944493"));
				post.setEntity(multipartEntity);
				HttpResponse response = client.execute(post);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					System.out.println("tag10");
					System.out.println(EntityUtils.toString(response
							.getEntity()));
					File[] files = file.getParentFile().listFiles();
					for (File f : files) {
						f.delete();
					}
					return true;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				client.getConnectionManager().shutdown();
			}
		}
		return false;
	}

}