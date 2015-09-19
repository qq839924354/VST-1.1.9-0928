package com.vst.itv52.v1.player;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.vst.itv52.v1.R;
import com.vst.itv52.v1.activity.BaseActivity;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.XLLXBiz;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.custom.LoadingDialog;
import com.vst.itv52.v1.model.ShooterSRTBean;
import com.vst.itv52.v1.model.VideoPlayUrl;
import com.vst.itv52.v1.model.XLLXFileInfo;
import com.vst.itv52.v1.player.VodPlayer.MyTimerTask;
import com.vst.itv52.v1.srt.LXSRT;
import com.vst.itv52.v1.srt.SRTBean;
import com.vst.itv52.v1.srt.SRTbiz;
import com.vst.itv52.v1.srt.SRTsetPop;
import com.vst.itv52.v1.srt.SearchSrtPop;
import com.vst.itv52.v1.srt.ShooterSRTGetter;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.HttpWorkTask;
import com.vst.itv52.v1.util.StringFilter;
import com.vst.itv52.v1.util.HttpWorkTask.ParseCallBack;
import com.vst.itv52.v1.util.HttpWorkTask.PostCallBack;

public class XLLXPlayer extends BaseActivity implements OnErrorListener,
		OnPreparedListener, OnCompletionListener {
	public static final String TAG = "XLLXPlayer";
	private VideoView mVideoView;
	private TextView srtTv;// 显示字幕的textview
	private PlayerMenuContrl menuContrl;
	private SRTsetPop srtPop;// 显示字幕的popWindow
	private ArrayList<LXSRT> srtList;// 字幕列表
	private Map<Integer, SRTBean> srtMap;// 字幕
	private int srtIndex = 0;// 字幕的位置
	private SearchSrtPop searchSrtPop;
	private static final int PLAY = 0;
	private static final int ERROR = 1;
	private static final int ISDIR = 2;
	private static final int SRT_LIST = 3;
	private static final int SRT = 4;
	public static final int SRT_SELECTED = 5;
	public static final int SRT_ALLOW = 6;
	private int netOrxlFlag = 1;
	private String srtNetPath;
	// //定时刷新任务
	private Timer frushSRTTimer = new Timer(); 
	public static boolean isFrush; //是否刷新
	private static final int MSG_FRUSH_SRT = 12313;//刷新字幕消息
	private MyTimerTask frushTimeTask;//刷新字幕线程
	private String currentStr = "";
	private static final int MSG_DISMISS_SRT = 787878;//隐藏显示字幕消息
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PLAY:
				mVideoView.setVideoURI(Uri.parse(urls.get(i).playurl));
				mVideoView.start();
				break;
			case ERROR:
				if (urls.size() - 1 > i) {
					i++;
					handler.sendEmptyMessage(PLAY);
				} else {
					ItvToast.makeText(XLLXPlayer.this, "正在转码中……",
							Toast.LENGTH_LONG).show();
				}
				break;
			case ISDIR:// 是一个文件夹
				ItvToast.makeText(XLLXPlayer.this, "点击的为文件夹", Toast.LENGTH_LONG)
						.show();
				break;
			case SRT_LIST:// 获得网络解析过来的字幕列表之后设置
				if (srtPop == null) {
					// 如果字幕popwindow为空 则创建字幕popwindow
					srtPop = new SRTsetPop(XLLXPlayer.this, handler);
					srtPop.setLxSrts(srtList);
				}
				// 默认显示字幕列表中的一个字幕版本
				if (srtList != null && !srtList.isEmpty()) {
					MyApp.pool.execute(parseSrt);
				}
				break;
			case SRT:// 显示字幕
				// 允许显示悬挂字幕，且字幕不为空
				if (MyApp.getCanShowSRT() && srtMap != null) {
					// 刷新字幕
					isFrush = true;
					if (frushTimeTask != null) {
						frushTimeTask.cancel(); // 将原任务从队列中移除
					}
					handler.removeMessages(MSG_DISMISS_SRT);
					frushTimeTask = new MyTimerTask();
					frushSRTTimer.schedule(frushTimeTask, 200, 500);
				} else {
					srtTv.setText("");

				}
				break;
			case SRT_SELECTED:// 字幕列表被选中
				srtIndex = msg.arg1;
				netOrxlFlag = msg.arg2;
				// 迅雷离线传递过来的字幕
				if (netOrxlFlag == 1) {
					if (srtList != null && srtList.size() > srtIndex) {
						// 开启线程池，两个线程去解析字幕
						MyApp.pool.execute(parseSrt);
					}
					// 射手网下载的字幕列表
				} else if (netOrxlFlag == 2) {
					srtNetPath = (String) msg.obj;
					Log.d("info", "msg.obj=" + srtNetPath);
					// 从网络加载字幕
					MyApp.pool.execute(parseNetSrt);
				}
				break;
			case SRT_ALLOW:
				allowShowSRT();
				break;
			case MSG_DISMISS_SRT:
				srtTv.setText("");
				break;
			case MSG_FRUSH_SRT:
				try {
					// Log.i("info", "当前字幕="+msg.obj.toString());
					String str = (String) msg.obj;
					if (msg.obj != null) {
						if (!currentStr.equals(str)) {
							srtTv.setText(Html.fromHtml(str));
							currentStr = str;
							handler.removeMessages(MSG_DISMISS_SRT);
							handler.sendEmptyMessageDelayed(MSG_DISMISS_SRT,
									2000);
						}
					} else {
						srtTv.setText("");
					}
				} catch (Exception e) {
					srtTv.setText("");
					e.printStackTrace();
				}
				break;
			}
		}
	};

	XLLXCtr xllxCtr;
	XLLXFileInfo info;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xllx_player_layout);
		progressShow();
		mVideoView = (VideoView) findViewById(R.id.xllx_videoview);
		srtTv = (TextView) findViewById(R.id.xllx_srt_tv);
		menuContrl = new PlayerMenuContrl(this, handler, ConstantUtil.MENU_XLLX);
		mVideoView.setOnPreparedListener(this);
		// mVideoView.setOnInfoListener(this);
		mVideoView.setOnErrorListener(this);
		mVideoView.setOnCompletionListener(this);
		xllxCtr = new XLLXCtr(this, mVideoView, handler);
		Intent i = getIntent();
		info = (XLLXFileInfo) i.getSerializableExtra("XLLX");
		xllxCtr.setVideoName(info.file_name);
		// 开启两个线程执行获得播放列表和悬挂字幕列表
		MyApp.pool.execute(getPlayUrl);
		MyApp.pool.execute(getsrtList);
		updateSRTShow();
	}

	/**
	 * 下载的字幕文件
	 */
	private ArrayList<String> files;

	public ArrayList<String> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}

	int i = 0;
	ArrayList<VideoPlayUrl> urls;
	Runnable getPlayUrl = new Runnable() {
		@Override
		public void run() {
			if (info.isDir) {
				handler.sendEmptyMessage(ISDIR);
				return;
			}
			urls = XLLXBiz.getLXPlayUrl(XLLXPlayer.this, info);
			Log.d(TAG, urls + "");
			if (urls != null && urls.size() > 0) {
				xllxCtr.setSharpness(urls, urls.get(0).sharp);
				handler.sendEmptyMessage(PLAY);
			} else {
				handler.sendEmptyMessage(ERROR);
			}
		}
	};

	Runnable getsrtList = new Runnable() {
		@Override
		public void run() {
			if (MyApp.getCanShowSRT()) {
				// 从迅雷官方解析，获得解析字幕
				srtList = XLLXBiz.getLxSrtsInfo(info);
				if (srtList != null && srtList.size() > 0) {
					handler.sendEmptyMessage(SRT_LIST);
					System.out.println("可用字幕数1：" + srtList.size());
					PlayerMenuContrl.showSrtSet(true, "设置字幕(共" + srtList.size()
							+ "条字幕)");
					LXORDOWN = 1;
				} else {
					PlayerMenuContrl.showSrtSet(false, "");
				}
			}
		}
	};

	/**
	 * 获取离线文件的列表
	 * 
	 * @return
	 */
	public ArrayList<LXSRT> getSrtList() {
		return srtList;
	}

	private int timerError; // 字幕错位时间调整值 单位是s;

	public void setTimerError(int timerError) {
		this.timerError = timerError;
	}

	// 是显示离线字幕的设置还是显示下载字幕的设置标记 1 为离线 2 为网络
	public static int LXORDOWN = 1;
	/**
	 * 解析字幕的线程
	 */
	Runnable parseSrt = new Runnable() {
		@Override
		public void run() {
			// 判断是否开启悬挂字幕
			if (MyApp.getCanShowSRT()) {
				// 根据得到的网络字幕地址。联网解析
				srtMap = SRTbiz.parseSrt(srtList.get(srtIndex).getSurl(),
						timerError);
				if (srtMap != null) {
					// 更新字幕
					handler.sendEmptyMessage(SRT);
				}
			}
		}
	};

	/**
	 * 解析射手网字幕的线程
	 */
	Runnable parseNetSrt = new Runnable() {
		@Override
		public void run() {
			// 判断是否开启悬挂字幕
			if (MyApp.getCanShowSRT()) {
				Log.d("info", "srtNetPath=" + srtNetPath);
				if (srtNetPath != null) {
					// 压缩文件的包
					srtMap = SRTbiz
							.parseSrt(new File(srtNetPath.replace("\\", "/")),
									timerError);
					Log.d("info", srtMap.toString());
					if (srtMap != null) {
						// 更新字幕
						handler.sendEmptyMessage(SRT);
					}
				}
			}
		}
	};

	/**
	 * 允许显示字幕，用于由关到开时的字幕下载解析
	 */
	private void allowShowSRT() {
		if (srtList == null) {
			MyApp.pool.execute(getsrtList);
		}
	}

	class MyTimerTask extends TimerTask {
		//
		@Override
		public void run() {
			if (isFrush) {
				try {
					int currentPosition = mVideoView.getCurrentPosition();
					Iterator<Integer> keys = srtMap.keySet().iterator();
					// 通过while循环遍历比较
					while (keys.hasNext()) {
						Integer key = keys.next();
						SRTBean srtbean = srtMap.get(key);
						// // 判断当前播放时间是否在字幕的开始时间和结束时间之内
						Message msg = Message.obtain();
						msg.what = MSG_FRUSH_SRT;
						if (currentPosition > srtbean.getBeginTime()
								&& currentPosition < srtbean.getEndTime()) {
							if (!srtbean.getSrtBody().equals(currentStr)) {
								msg.obj = srtbean.getSrtBody();
								handler.sendMessage(msg);
							}
							break;
							// }
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void onStart() {
		Log.i(TAG, "------------onStart-------------");
		super.onStart();

	}

	public void onResume() {
		Log.i(TAG, "--------------onResume----------");
		mVideoView.start();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		Log.i(TAG, "-------------onPause------------");
		mVideoView.pause();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "----------------onDestroy-----------------");
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		if (searchSrtPop != null && searchSrtPop.isShowing()) {
			searchSrtPop.dismiss();
		}
		if (srtPop != null && srtPop.isShowing()) {
			srtPop.dismiss();
		}
		if (xllxCtr != null && xllxCtr.isShowing()) {
			xllxCtr.dismiss();
		}
		if (frushTimeTask != null) {
			frushTimeTask.cancel();
		}
		isFrush = false;
		handler.removeMessages(MSG_DISMISS_SRT);
		searchCount = 0;
		releaseSource();
		super.onDestroy();
	}

	private void releaseSource() {
		mVideoView=null;
		menuContrl=null;
		srtPop=null;// 显示字幕的popWindow
		srtList=null;// 字幕列表
		srtMap=null;// 字幕
		searchSrtPop=null;
		srtNetPath=null;
		// //定时刷新任务
		frushSRTTimer =null; 
		frushTimeTask=null;//刷新字幕线程
	}

	/**
	 * 显示字幕设置popwindow
	 */
	public void showSRTPop() {
		// 先置于空
		srtPop = null;
		srtPop = new SRTsetPop(this, handler);
		// 设置字幕列表
		srtPop.setLxSrts(srtList);
		// 显示在屏幕中间位置
		srtPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
	}

	public void showSRTPop(ArrayList<String> fileNames) {
		// 先置于空
		srtPop = null;
		srtPop = new SRTsetPop(this, handler);
		// 设置字幕列表
		srtPop.setNetSrts(fileNames);
		// 显示在屏幕中间位置
		srtPop.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
	}

	/**
	 * 设置自定义悬挂字幕大小
	 * 
	 * @param size
	 */
	public void setSRTTextSize(int size) {
		if (srtTv != null)
			srtTv.setTextSize(size);
	}

	/**
	 * 设置悬挂字幕颜色
	 * 
	 * @param colors
	 */
	public void setSRTTextColor(int[] colors) {
		if (srtTv != null) {
			srtTv.setTextColor(colors[0]);
			srtTv.setShadowLayer(3, 2, -2, colors[1]);
		}
	}

	/**
	 * 设置悬挂字幕位置
	 * 
	 * @param level
	 */
	public void setSRTTextLoaction(int level) {
		if (srtTv != null) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
			int buttom = 40 + 20 * level;
			lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			lp.setMargins(0, 0, 0, buttom);
			srtTv.setLayoutParams(lp);
		}
	}

	/**
	 * 更新悬挂字幕属性
	 */
	private void updateSRTShow() {
		int size = MyApp.getSRTTextSize();
		int[] color = MyApp.getSRTTextColor();
		int location = MyApp.getSSRTLocation();
		setSRTTextSize(size);
		setSRTTextColor(color);
		setSRTTextLoaction(location);
	}

	int scalemod = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(keyCode);
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				&& event.getRepeatCount() == 0) {
			xllxCtr.show(XLLXCtr.SEEK);
			return true;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				&& event.getRepeatCount() == 0) {
			xllxCtr.show(XLLXCtr.SEEK);
			return true;
		} else if (keyCode == 185) { // 185 是比例 A11
			/**
			 * 其他按键
			 */
			changeScale(ConstantUtil.OPERATE_RIGHT);

		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				&& event.getRepeatCount() == 0) {
			xllxCtr.show(XLLXCtr.PAUSE);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (menuContrl != null && menuContrl.isShowing()) {
				menuContrl.dismiss();
			} else {
				menuContrl.showAtLocation(mVideoView, Gravity.CENTER, 0, 0);
				menuContrl.setScalor(scalemod);
				menuContrl.showVoiceLevel(menuContrl.getVoice());
				// xllxCtr.show(XLLXCtr.PAUSE);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 用于操作菜单上对屏幕比例的控制
	 * 
	 * @param derection
	 */
	public void changeScale(int derection) {
		if (derection == ConstantUtil.OPERATE_RIGHT) {
			scalemod += 1;
		} else if (derection == ConstantUtil.OPERATE_LEFT) {
			if (scalemod > 0) {
				scalemod--;
			} else {
				scalemod = 3;
			}
		}
		scalemod = (scalemod + 1) % 3;
		mVideoView.selectScales(scalemod);
		menuContrl.setScalor(scalemod);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		handler.sendEmptyMessage(ERROR);
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.i(TAG, "onPrepared-----------");
		progressDismiss();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		onBackPressed();
	}

	private void exit() {
		if (waitExit) {
			waitExit = false;
			ItvToast toast = new ItvToast(this);
			toast.setText(R.string.toast_exit_hint);
			toast.setIcon(R.drawable.toast_shut);
			toast.show();
			handler.postDelayed(cancleExit, 2000);
		} else {
			finish();
		}
	}

	private boolean waitExit = true;
	Runnable cancleExit = new Runnable() {
		@Override
		public void run() {
			waitExit = true;
		}
	};

	/**
	 * 显示搜索字幕的popwindow
	 */
	private static int searchCount = 0;

	public void showSearchSrtPop() {
		if (menuContrl != null && menuContrl.isShowing()) {
			menuContrl.dismiss();
		}
		// 过滤某些特定的关键字
		final String videoName = StringFilter.FilterString(info.file_name);
		if (videoName.length() > 1) {
			if (searchCount < 1) {
				Log.i("info", "离线文件名：" + videoName);
				getSrtList(videoName);
			} else {
				if (searchSrtPop != null) {
					searchSrtPop.showAtLocation(mVideoView, Gravity.CENTER, 0,
							0);
				} else {
					ItvToast toast = ItvToast.makeText(this, "搜索不到字幕...", 3000);
					toast.show();
				}
			}
		} else {
			ItvToast toast = ItvToast.makeText(this, "搜索不到字幕...", 3000);
			toast.show();
		}
	}

	/**
	 * 获取字幕列表
	 * 
	 * @param videoName
	 */
	public void getSrtList(final String videoName) {
		try {
			searchCount = 1;
			final LoadingDialog progressDialog1 = new LoadingDialog(this);
			progressDialog1.setLoadingMsg("加载中...");
			progressDialog1.show();
			new HttpWorkTask<ArrayList<ShooterSRTBean>>(
					new ParseCallBack<ArrayList<ShooterSRTBean>>() {

						@Override
						public ArrayList<ShooterSRTBean> onParse() {

							try {
								return ShooterSRTGetter.getShooterSrts(videoName);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					}, new PostCallBack<ArrayList<ShooterSRTBean>>() {

						@Override
						public void onPost(ArrayList<ShooterSRTBean> reult) {
							progressDialog1.dismiss();
							if (reult != null && reult.size() > 0) {
								searchSrtPop = new SearchSrtPop(
										XLLXPlayer.this, reult, handler,
										videoName);
								// 显示在屏幕中间位置
								searchSrtPop.showAtLocation(mVideoView,
										Gravity.CENTER, 0, 0);
							} else {
								ItvToast toast = ItvToast.makeText(
										XLLXPlayer.this, "暂无该视频的字幕", 3000);
								toast.show();
							}
						}
					}).execute();
		} catch (Exception e) {
			ItvToast toast = ItvToast.makeText(this, "网络加载出错，请重试！", 3000);
			toast.show();
			e.printStackTrace();
		}
	}
}
