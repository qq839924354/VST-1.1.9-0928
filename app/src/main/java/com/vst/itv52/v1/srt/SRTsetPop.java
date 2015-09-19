package com.vst.itv52.v1.srt;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.SRTNameAdapter;
import com.vst.itv52.v1.adapters.SRTSetAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.player.VodPlayer;
import com.vst.itv52.v1.player.XLLXPlayer;

/**
 * 设置字幕的类
 * 
 * @author w
 * 
 */
public class SRTsetPop extends PopupWindow implements OnItemClickListener {
	private int autoDismiss = 20000;// 10秒隐藏
	private Context context;
	private Handler handler;
	private int netORxlFlag;
	// 字体大小
	private String[] textSizes = { "24", "27", "30", "34", "38", "42", "46",
			"50" };
	// 颜色名称
	private String[] colorNames = { "<font color=\"#ffffff\">白色</font>",
			"<font color=\"#ffff00\">黄色</font>",
			"<font color=\"#0066ff\">蓝色</font>",
			"<font color=\"#009900\">绿色</font>",
			"<font color=\"#000000\">黑色</font>",
			"<font color=\"#999999\">灰色</font>",
			"<font color=\"#ff00ff\">紫色</font>" };
	// 字体的颜色设置和背景颜色
	private int[][] textColors = { { 0xffffffff, 0xff333333 },
			{ 0xffffff00, 0xff330033 }, { 0xff0066ff, 0xff333300 },
			{ 0xff009900, 0xff003333 }, { 0xff000000, 0xffffffff },
			{ 0xff999999, 0xffccffff }, { 0xffff00ff, 0xff4f77a2 } };
	private String[] textLocation = { "-2", "-1", "0", "1", "2", "3", "4", "5" };
	private String errorTimer[] = { "1","2","5", "10", "15", "20", "25", "30", "35",
			"40", "45", "50", "-1","-2","-5", "-10", "-15", "-20", "-25", "-30", "-35",
			"-40", "-45", "-50" };
	private ArrayList<LXSRT> lxSrts;
	private SRTSetAdapter srtAdapter;
	private ArrayList<String> fileNames;
	private ListView srtList, sizeList, colorList, locationList, errorList;

	private Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			try {
				dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public SRTsetPop(Context context, Handler handler) {
		super();
		this.context = context;
		this.handler = handler;
		init();
	}

	private void init() {
		setBackgroundDrawable(context.getResources().getDrawable(
				android.R.color.transparent));
		setFocusable(true);
		setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = inflater.inflate(R.layout.srt_set_pop, null);

		srtList = (ListView) root.findViewById(R.id.srt_set_pop_list);
		sizeList = (ListView) root.findViewById(R.id.srt_set_pop_size);
		colorList = (ListView) root.findViewById(R.id.srt_set_pop_color);
		locationList = (ListView) root.findViewById(R.id.srt_set_pop_location);
		errorList = (ListView) root.findViewById(R.id.srt_set_pop_settingTime);
		setContentView(root);
	}

	/**
	 * 从迅雷离线下载过来的字幕列表
	 * 
	 * @param lxsrts
	 */
	public void setLxSrts(ArrayList<LXSRT> lxsrts) {
		this.netORxlFlag = 1;
		// 获得字幕集合列表
		if (lxsrts != null) {
			this.lxSrts = lxsrts;
			System.out.println("可用字幕数2：" + lxSrts.size());
		} else {
			this.lxSrts = new ArrayList<LXSRT>();
		}
		initData();
		initListener();
	}

	/**
	 * 从射手网下载的字幕压缩包的内的文件名列表
	 * 
	 * @param fileNames
	 */
	public void setNetSrts(ArrayList<String> fileNames) {
		this.netORxlFlag = 2;
		// 获得字幕集合列表
		if (fileNames != null) {
			this.fileNames = fileNames;
			System.out.println("网络字幕文件数：" + fileNames.size());
		} else {
			this.fileNames = new ArrayList<String>();
		}
		initData();
		initListener();
	}

	private void initData() {
		String[] listArray = null;
		if (netORxlFlag == 1) {
			listArray = new String[lxSrts.size()];
			for (int i = 0; i < lxSrts.size(); i++) {
				listArray[i] = lxSrts.get(i).getSname();
			}
		} else if (netORxlFlag == 2) {
			listArray = new String[fileNames.size()];
			for (int i = 0; i < fileNames.size(); i++) {
				listArray[i] = fileNames.get(i);
			}
		}
		SRTNameAdapter adapter = new SRTNameAdapter(listArray, context,
				netORxlFlag);
		srtList.setAdapter(adapter);
		// 字幕文字大小
		srtAdapter = new SRTSetAdapter(textSizes, context);
		sizeList.setAdapter(srtAdapter);
		// 字幕颜色
		srtAdapter = new SRTSetAdapter(colorNames, context);
		colorList.setAdapter(srtAdapter);
		// 字幕显示位置
		srtAdapter = new SRTSetAdapter(textLocation, context);
		locationList.setAdapter(srtAdapter);
		// 字幕错位调整
		srtAdapter = new SRTSetAdapter(errorTimer, context);
		errorList.setAdapter(srtAdapter);
	}

	/**
	 * 更新对字幕设置的选项
	 */
	private void updateSelection() {
		/**
		 * 从全局Aplication中取到用户设置的字体大小颜色和位置的默认属性
		 */
		int size = MyApp.getSRTTextSize();
		int[] color = MyApp.getSRTTextColor();
		int location = MyApp.getSSRTLocation();
		// 设置字体大小
		for (int i = 0; i < textSizes.length; i++) {
			if (size == Integer.valueOf(textSizes[i])) {
				((SRTSetAdapter) sizeList.getAdapter()).setSelctItem(i);
				break;
			}
		}
		// 设置字幕颜色
		for (int j = 0; j < textColors.length; j++) {
			// 从全局变量中取出来的字幕颜色设置。如果相等则更新字幕
			if (color[0] == textColors[j][0]) {
				((SRTSetAdapter) colorList.getAdapter()).setSelctItem(j);
				break;
			}
		}
		// 设置字幕位置
		for (int k = 0; k < textLocation.length; k++) {
			if (location == Integer.valueOf(textLocation[k])) {
				((SRTSetAdapter) locationList.getAdapter()).setSelctItem(k);
				break;
			}
		}

		// 设置调整时间
		for (int n = 0; n < errorTimer.length; n++) {
			if (location == Integer.valueOf(errorTimer[n])) {
				((SRTSetAdapter) errorList.getAdapter()).setSelctItem(n);
				break;
			}
		}
//		if (fileNames != null && fileNames.size() > 0) {
//			((SRTNameAdapter) srtList.getAdapter()).setSelctItem(0);
//		} else if (lxSrts != null && lxSrts.size() > 0) {
//			((SRTSetAdapter) srtList.getAdapter()).setSelctItem(0);
//		}
		try {
			//默认选择第一个
			if (context instanceof XLLXPlayer) {
				if((fileNames!=null && fileNames.size()>0)||(lxSrts!=null &&lxSrts.size()>0)){
					// 下载的字幕列表某项被选中
					Message msg = handler.obtainMessage(XLLXPlayer.SRT_SELECTED);
					msg.arg1 = 0;
					msg.arg2 = netORxlFlag;
					msg.obj = ((SRTNameAdapter) srtList.getAdapter()).getItem(0);
					filePath = (String) ((SRTNameAdapter) srtList.getAdapter())
							.getItem(0);
					handler.sendMessage(msg);
					((SRTNameAdapter) srtList.getAdapter()).setSelctItem(0);
				}
			} else if (context instanceof VodPlayer) {
				// 下载的字幕列表某项被选中
				Message msg = handler.obtainMessage(VodPlayer.SRT_SELECTED);
				msg.arg1 = 0;
				msg.arg2 = netORxlFlag;
				msg.obj = ((SRTNameAdapter) srtList.getAdapter()).getItem(0);
				filePath = (String) ((SRTNameAdapter) srtList.getAdapter())
						.getItem(0);
				handler.sendMessage(msg);
				((SRTNameAdapter) srtList.getAdapter()).setSelctItem(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initListener() {
		srtList.setOnItemClickListener(this);
		sizeList.setOnItemClickListener(this);
		colorList.setOnItemClickListener(this);
		locationList.setOnItemClickListener(this);
		errorList.setOnItemClickListener(this);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		updateSelection();// 设置存储的默认字幕属性
		handler.postDelayed(autoHide, autoDismiss);// 10秒后执行自动隐藏
		super.showAtLocation(parent, gravity, x, y);
	}

	private String filePath;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 字幕列表的时候，发送消息到迅雷离线文件播放页面
		if (parent.equals(srtList)) {
			if (context instanceof XLLXPlayer) {
				if (netORxlFlag == 2) {
					// 下载的字幕列表某项被选中
					Message msg = handler
							.obtainMessage(XLLXPlayer.SRT_SELECTED);
					msg.arg1 = position;
					msg.arg2 = netORxlFlag;
					msg.obj = ((SRTNameAdapter) srtList.getAdapter())
							.getItem(position);
					filePath = (String) ((SRTNameAdapter) srtList.getAdapter())
							.getItem(position);
					handler.sendMessage(msg);
					((SRTNameAdapter) srtList.getAdapter())
							.setSelctItem(position);
				} else if (netORxlFlag == 1) {
					try {
						Message msg = handler
								.obtainMessage(XLLXPlayer.SRT_SELECTED);
						msg.arg1 = position;
						msg.arg2 = netORxlFlag;
						handler.sendMessage(msg);
						((SRTSetAdapter) srtList.getAdapter())
								.setSelctItem(position);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (context instanceof VodPlayer) {
				// 下载的字幕列表某项被选中
				Message msg = handler.obtainMessage(VodPlayer.SRT_SELECTED);
				msg.arg1 = position;
				msg.arg2 = netORxlFlag;
				msg.obj = ((SRTNameAdapter) srtList.getAdapter())
						.getItem(position);
				filePath = (String) ((SRTNameAdapter) srtList.getAdapter())
						.getItem(position);
				handler.sendMessage(msg);
				((SRTNameAdapter) srtList.getAdapter()).setSelctItem(position);
			}

		} else if (parent.equals(sizeList)) {
			// 设置字幕字体
			if (context instanceof XLLXPlayer) {
				XLLXPlayer new_name = (XLLXPlayer) context;
				new_name.setSRTTextSize(Integer.valueOf(textSizes[position]));
			} else if (context instanceof VodPlayer) {
				VodPlayer new_name = (VodPlayer) context;
				new_name.setSRTTextSize(Integer.valueOf(textSizes[position]));
			}
			((SRTSetAdapter) sizeList.getAdapter()).setSelctItem(position);
			MyApp.setSRTTextSize(Integer.valueOf(textSizes[position]));
		} else if (parent.equals(colorList)) {
			// 设置字幕颜色
			if (context instanceof XLLXPlayer) {
				XLLXPlayer new_name = (XLLXPlayer) context;
				new_name.setSRTTextColor(textColors[position]);
			} else if (context instanceof VodPlayer) {
				VodPlayer new_name = (VodPlayer) context;
				new_name.setSRTTextColor(textColors[position]);
			}
			((SRTSetAdapter) colorList.getAdapter()).setSelctItem(position);
			MyApp.setSRTTEXTColor(textColors[position]);
		} else if (parent.equals(locationList)) {
			if (context instanceof XLLXPlayer) {
				XLLXPlayer new_name = (XLLXPlayer) context;
				new_name.setSRTTextLoaction(Integer
						.valueOf(textLocation[position]));
			} else if (context instanceof VodPlayer) {
				VodPlayer new_name = (VodPlayer) context;
				new_name.setSRTTextLoaction(Integer
						.valueOf(textLocation[position]));
			}
			((SRTSetAdapter) locationList.getAdapter()).setSelctItem(position);
			MyApp.setSRTLocation(Integer.valueOf(textLocation[position]));
		} else if (parent.equals(errorList)) {
			if (context instanceof XLLXPlayer) {
				XLLXPlayer new_name = (XLLXPlayer) context;
				new_name.setTimerError(Integer.valueOf(errorTimer[position]));
				// 重新解析一次字幕
				Message msg = handler.obtainMessage(XLLXPlayer.SRT_SELECTED);
				msg.arg1 = position;
				msg.arg2 = netORxlFlag;
				msg.obj = filePath;
				handler.sendMessage(msg);
			} else if (context instanceof VodPlayer) {
				VodPlayer new_name = (VodPlayer) context;
				new_name.setTimerError(Integer.valueOf(errorTimer[position]));
				// 重新解析一次字幕
				Message msg = handler.obtainMessage(VodPlayer.SRT_SELECTED);
				msg.arg1 = position;
				msg.arg2 = netORxlFlag;
				msg.obj = filePath;
				handler.sendMessage(msg);
			}
			((SRTSetAdapter) errorList.getAdapter()).setSelctItem(position);
		}
		// 在执行影藏悬挂字幕 之前需要暂停隐藏线程
		handler.removeCallbacks(autoHide);
		handler.postDelayed(autoHide, autoDismiss);
	}

}
