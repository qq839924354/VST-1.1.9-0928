package com.vst.itv52.v1.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vst.itv52.v1.R;

public class SettingSpeedTest extends BaseActivity implements OnClickListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private ExecutorService pool = Executors.newFixedThreadPool(2);// 创建线程池
	private TextView bottomtv;
	private Button sourceBtn;

	private int downTime = 0;
	private static int TESTTIME = 5000;
	private int speed = 0;
	private int i = 0;
	private String[] urls = {
			"http://data.video.qiyi.com/videos/other/20130328/a84f4706a6c5f33fb9db4a419d08476d.f4v",
			"http://g3.letv.cn/14/52/33/2141429372.0.flv",
			"http://video.store.qq.com/o0012t23yb9.mp4",
			"http://data.video.qiyi.com/videos/other/20130328/a84f4706a6c5f33fb9db4a419d08476d.f4v" };// 这两处后面试用采集的源
	private ArrayList<TextView> textList = new ArrayList<TextView>();
	private ArrayList<ProgressBar> progressList = new ArrayList<ProgressBar>();
	private ArrayList<ImageView> imgaList = new ArrayList<ImageView>();
	private int[] speeds = new int[4];
	private static final int START = 0;
	private static final int TESTING = 1;
	private static final int FINISH = 2;
	private SharedPreferences share;

	private Runnable downTask = new Runnable() {
		@Override
		public void run() {
			try {
				downSourceCheck(urls[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START:
				progressList.get(i).setMax(TESTTIME);
				textList.get(i).setVisibility(View.VISIBLE);
				break;
			case TESTING:
				textList.get(i).setText(speed + "kb/sec");
				progressList.get(i).setProgress(downTime);
				break;
			case FINISH:
				textList.get(i).setText(speed + "kb/sec");
				speeds[i] = speed;
				speed = 0;
				downTime = 0;
				i++;
				if (i > 3) {
					bottomtv.setText("建议您选择的高清源为：");
					for (int m = 0; m < speeds.length; m++) {
						if (speeds[m] >= 1000) {
							imgaList.get(m).setVisibility(View.VISIBLE);
						}
					}
					// 记录最低速度
					Arrays.sort(speeds);
					Editor editor = share.edit();
					editor.putInt("local_speed", speeds[0]);
					editor.commit();
					sourceBtn.setText("开始资源测速");
					sourceBtn.setEnabled(true);
					// localBtn.setEnabled(true);
					i = 0;
					return;
				}
				pool.execute(downTask);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_speed)).inflate();
		share = getSharedPreferences("settingSPF", MODE_PRIVATE);
		initSpeedView();
	}

	private void initSpeedView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("速度测试");
		setName2.setText("速度测试");
		setItemLog.setImageResource(R.drawable.testspeet_setup);

		bottomtv = (TextView) findViewById(R.id.bottomtv);

		ProgressBar pb1 = (ProgressBar) findViewById(R.id.down_pb1);
		TextView tv1 = (TextView) findViewById(R.id.tv1);
		ImageView ssim1 = (ImageView) findViewById(R.id.ssim1);
		progressList.add(pb1);
		textList.add(tv1);
		imgaList.add(ssim1);

		ProgressBar pb2 = (ProgressBar) findViewById(R.id.down_pb2);
		TextView tv2 = (TextView) findViewById(R.id.tv2);
		ImageView ssim2 = (ImageView) findViewById(R.id.ssim2);
		progressList.add(pb2);
		textList.add(tv2);
		imgaList.add(ssim2);

		ProgressBar pb3 = (ProgressBar) findViewById(R.id.down_pb3);
		TextView tv3 = (TextView) findViewById(R.id.tv3);
		ImageView ssim3 = (ImageView) findViewById(R.id.ssim3);
		progressList.add(pb3);
		textList.add(tv3);
		imgaList.add(ssim3);

		ProgressBar pb4 = (ProgressBar) findViewById(R.id.down_pb4);
		TextView tv4 = (TextView) findViewById(R.id.tv4);
		ImageView ssim4 = (ImageView) findViewById(R.id.ssim4);
		progressList.add(pb4);
		textList.add(tv4);
		imgaList.add(ssim4);
		sourceBtn = (Button) findViewById(R.id.setting_speed_source);
		sourceBtn.setOnClickListener(this);
	}

	private void downSourceCheck(String url) throws IOException {
		URL myURL = new URL(url);
		URLConnection conn = myURL.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
		byte buf[] = new byte[1024];
		long size = 0;
		handler.sendEmptyMessage(START);
		int numread;
		long recode1 = System.currentTimeMillis();
		long recode2 = 0;
		while ((numread = is.read(buf)) != -1) {
			size += numread;
			recode2 = System.currentTimeMillis();
			int textTime = (int) (recode2 - recode1);
			if (textTime >= 500) {
				recode1 = recode2;
				downTime += textTime;
				speed = (int) ((size * 1000) / (downTime * 1024));
				handler.sendEmptyMessage(TESTING);
			}
			if (downTime >= TESTTIME) {
				speed = (int) ((size * 1000) / (TESTTIME * 1024));
				handler.sendEmptyMessage(FINISH);
				break;
			}
		}
		is.close();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_speed_source:
			sourceBtn.setText("测试中……");
			progressList.get(0).setProgress(0);
			progressList.get(1).setProgress(0);
			progressList.get(2).setProgress(0);
			progressList.get(3).setProgress(0);
			pool.execute(downTask);
			bottomtv.setVisibility(View.VISIBLE);
			sourceBtn.setEnabled(false);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		}
		return super.onKeyDown(keyCode, event);
	}

}
