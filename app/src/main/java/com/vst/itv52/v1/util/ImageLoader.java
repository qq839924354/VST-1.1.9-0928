package com.vst.itv52.v1.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	private static ImageLoader instance;// 单例
	private ExecutorService executorService;// 线程池
	private ImageMemoryCache memoryCache; // 内存缓存
	private ImageFileCache fileCache;// 文件缓存
	private Map<String, ImageView> taskMap;// 存放任务，该map以一个url对应一个ImageView对象
	private boolean allowLoad = true;// 是否允许加载图片

	private ImageLoader(Context context) {
		int cpuNums = Runtime.getRuntime().availableProcessors();
		this.executorService = Executors.newFixedThreadPool(cpuNums + 1);// 根据cpu的使用情况定义线程池的大小，使用并发

		this.memoryCache = new ImageMemoryCache(context);// 内存缓存

		this.fileCache = new ImageFileCache();// 文件缓存

		this.taskMap = new HashMap<String, ImageView>();// 储存对应视图的hashmap

	}

	public static ImageLoader getInstance(Context context) {
		if (instance == null) {
			instance = new ImageLoader(context);
		}
		return instance;
	}

	public void restore() {
		this.allowLoad = true;
	}

	public void lock() {
		this.allowLoad = false;
	}

	public void unlock() {
		this.allowLoad = true;
		doTask();
	}

	public void clearTask() {
		taskMap.clear();
		Log.i("info", "========手动清除下载任务========");
	}

	public void addTask(String url, ImageView img) {
		// 先从内存中获取，取到则直接加载
		Bitmap bitmap = memoryCache.getBitmapFromCache(url);
		if (bitmap != null) {
			img.setImageBitmap(bitmap);// 如果缓存内存在该图片直接设置上去，不开启异步任务
		} else {
			// 如果没有则开始异步任务执行本地下载或网络下载并将图片设置标记tag，如果允许下载则开始，这里可以使用布尔值控制连续翻页
			synchronized (taskMap) {
				img.setTag(url);
				taskMap.put(Integer.toString(img.hashCode()), img);
			}
			if (allowLoad) {
				doTask();
			}
		}
	}

	/**
	 * 加载存放任务的图片
	 */
	private void doTask() {
		// TODO Auto-generated method stub
		synchronized (taskMap) {
			Collection<ImageView> con = taskMap.values();
			// 对map遍历，执行下载的任务，每一次下载将对应一个url传进去
			for (ImageView i : con) {
				if (i != null) {
					if (i.getTag() != null) {
						loadImage((String) i.getTag(), i);
					}
				}
			}
			taskMap.clear();
		}
	}

	private void loadImage(String url, ImageView img) {
		// 下载任务将被提交到线程池进行管理
		this.executorService.submit(new TaskWithResult(
				new TaskHandler(url, img), url));
	}

	/**
	 * 获取图片，从三个地方获取，首先是内存获取，然后是缓存获取，最后从网络获取
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmap(String url) {
		Bitmap result = memoryCache.getBitmapFromCache(url);
		if (result == null) {
			result = fileCache.getImage(url);
			if (result == null) {
				// 网络下载图片
				result = BitmapUtil.getBitmap(url);
				if (result != null) {
					fileCache.saveBitmap(url, result);
					memoryCache.addBitmapToCache(url, result);
				}
			} else {
				memoryCache.addBitmapToCache(url, result);// 即使从本地读取的也存入缓存
			}
		}
		return result;
	}

	/**
	 * 子线程任务异步下载
	 * 
	 * @author Administrator
	 * 
	 */
	private class TaskWithResult implements Callable<String> {
		private String url;
		private Handler handler;

		public TaskWithResult(Handler handler, String url) {
			this.url = url;
			this.handler = handler;
		}

		@Override
		public String call() throws Exception {
			Message msg = new Message();
			msg.obj = getBitmap(url);
			if (msg.obj != null) {
				handler.sendMessage(msg);
			}
			return url;
		}
	}

	private class TaskHandler extends Handler {
		String url;
		ImageView img;

		public TaskHandler(String url, ImageView img) {
			this.url = url;
			this.img = img;
		}

		@Override
		public void handleMessage(Message msg) {
			/*** 查看ImageView需要显示的图片是否被改变 ***/
			if (img.getTag().equals(url)) {
				if (msg.obj != null) {
					Bitmap bitmap = (Bitmap) msg.obj;
					img.setImageBitmap(bitmap);
				}
			}
		}
	}
}
