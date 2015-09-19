package com.vst.itv52.v1.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.https.HttpClientHelper;
import com.vst.itv52.v1.https.HttpResult;
import com.vst.itv52.v1.util.ConfigUtil;
import com.vst.itv52.v1.util.ConstantUtil;

public class SettingServer extends BaseActivity implements OnClickListener {
	private TextView setName1;// 设置名称（小）
	private TextView setName2;// 设置名称（大）
	private ImageView setItemLog;// 设置图标
	private LinearLayout server1;// 服务器一
	private LinearLayout server2;// 服务器二
	private LinearLayout server3;// 服务器三
	private LinearLayout reserver;// 备用

	private TextView status1, status2, status3, status4;

	private static final int PINGEND = 0;
	private boolean isConnected = false;
	private ExecutorService pool = Executors.newFixedThreadPool(2);// 创建线程池
	private int i = 0;
	private String[] serverHosts = { ConfigUtil.getValue("SERVER_1"),
			ConfigUtil.getValue("SERVER_2"), ConfigUtil.getValue("SERVER_3"),
			ConfigUtil.getValue("SERVER_4") };
	private ArrayList<TextView> status = new ArrayList<TextView>();
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PINGEND:
				if (isConnected) {
					status.get(i).setText("在线");
					status.get(i).setTextColor(
							getResources().getColor(R.color.green));
				} else {
					status.get(i).setText("休息");
					status.get(i).setTextColor(
							getResources().getColor(R.color.detail_point));
				}
				i++;
				if (i < serverHosts.length) {
					pool.execute(downTask);
				}
				break;
			}
		};
	};
	private Runnable downTask = new Runnable() {
		@Override
		public void run() {
			//再次防止数组越界
			if (i < serverHosts.length) {
				isConnected = pingHost(serverHosts[i]);
				handler.sendEmptyMessage(PINGEND);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_main);
		((ViewStub) findViewById(R.id.set_server_set)).inflate();
		initServerView();
	}

	@Override
	protected void onStart() {
		super.onStart();
		pool.execute(downTask);
	}

	/* 服务器设置视图 */
	private void initServerView() {
		setName1 = (TextView) findViewById(R.id.set_name1);
		setName2 = (TextView) findViewById(R.id.set_name2);
		setItemLog = (ImageView) findViewById(R.id.set_item_log);
		setName1.setText("服务器选择");
		setName2.setText("服务器选择");
		setItemLog.setImageResource(R.drawable.set_server);

		server1 = (LinearLayout) findViewById(R.id.server_server1);
		server2 = (LinearLayout) findViewById(R.id.server_server2);
		server3 = (LinearLayout) findViewById(R.id.server_server3);
		reserver = (LinearLayout) findViewById(R.id.server_reserver);
		String server = MyApp.baseServer;

		if (server.equals(ConfigUtil.getValue("SERVER_1"))) {
			theOnlyCheckedis(server1);
		} else if (server.equals(ConfigUtil.getValue("SERVER_2"))) {
			theOnlyCheckedis(server2);
		} else if (server.equals(ConfigUtil.getValue("SERVER_3"))) {
			theOnlyCheckedis(server3);
		} else {
			theOnlyCheckedis(reserver);
		}
		status1 = (TextView) findViewById(R.id.set_item_status1);
		status2 = (TextView) findViewById(R.id.set_item_status2);
		status3 = (TextView) findViewById(R.id.set_item_status3);
		status4 = (TextView) findViewById(R.id.set_item_status4);
		status.add(status1);
		status.add(status2);
		status.add(status3);
		status.add(status4);
		initServerListener();
	}

	/* 服务器设置监听 */
	private void initServerListener() {
		server1.setOnClickListener(this);
		server2.setOnClickListener(this);
		server3.setOnClickListener(this);
		// reserver.setOnClickListener(this);
	}

	/**
	 * 保持唯一被选中
	 * 
	 * @param v
	 *            需要被选中的控件
	 */
	private void theOnlyCheckedis(View v) {
		ImageView checkLog1, checkLog2, checkLog3, checkLog4;
		checkLog1 = (ImageView) server1.findViewById(R.id.set_choose_log1);
		checkLog2 = (ImageView) server2.findViewById(R.id.set_choose_log2);
		checkLog3 = (ImageView) server3.findViewById(R.id.set_choose_log3);
		checkLog4 = (ImageView) reserver.findViewById(R.id.set_choose_log4);
		switch (v.getId()) {
		case R.id.server_server1:
			checkLog1.setVisibility(View.VISIBLE);
			checkLog2.setVisibility(View.INVISIBLE);
			checkLog3.setVisibility(View.INVISIBLE);
			checkLog4.setVisibility(View.INVISIBLE);
			break;
		case R.id.server_server2:
			checkLog1.setVisibility(View.INVISIBLE);
			checkLog2.setVisibility(View.VISIBLE);
			checkLog3.setVisibility(View.INVISIBLE);
			checkLog4.setVisibility(View.INVISIBLE);
			break;
		case R.id.server_server3:
			checkLog1.setVisibility(View.INVISIBLE);
			checkLog2.setVisibility(View.INVISIBLE);
			checkLog3.setVisibility(View.VISIBLE);
			checkLog4.setVisibility(View.INVISIBLE);
			break;
		case R.id.server_reserver:
			checkLog1.setVisibility(View.INVISIBLE);
			checkLog2.setVisibility(View.INVISIBLE);
			checkLog3.setVisibility(View.INVISIBLE);
			checkLog4.setVisibility(View.VISIBLE);
			break;
		}
	}

	private boolean pingHost(String hostName) {
		HttpResult result = HttpClientHelper.get(
				hostName,
				new Header[] { new BasicHeader(ConstantUtil.VER, ConfigUtil
						.getValue(ConfigUtil.VER)) });
		if (result != null && result.getStatuCode() == 200) {
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.server_server1:
			theOnlyCheckedis(server1);
			MyApp.setBase(ConfigUtil.getValue("SERVER_1"));
			break;
		case R.id.server_server2:
			theOnlyCheckedis(server2);
			MyApp.setBase(ConfigUtil.getValue("SERVER_2"));
			break;
		case R.id.server_server3:
			theOnlyCheckedis(server3);
			MyApp.setBase(ConfigUtil.getValue("SERVER_3"));
			break;
		case R.id.server_reserver:
			theOnlyCheckedis(reserver);
			MyApp.setBase(ConfigUtil.getValue("SERVER_4"));
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
