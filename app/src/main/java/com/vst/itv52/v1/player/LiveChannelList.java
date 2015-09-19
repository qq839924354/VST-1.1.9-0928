package com.vst.itv52.v1.player;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.adapters.ChannelListAdapter;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.biz.LiveBiz;
import com.vst.itv52.v1.db.LiveDataHelper;
import com.vst.itv52.v1.db.VSTDBHelper;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.model.LiveTypeInfo;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.HttpWorkTask;

public class LiveChannelList extends PopupWindow {

	private Context mContetx;
	private ArrayList<LiveTypeInfo> channelTypeList;
	private int tidIndex;
	private Handler mHandler;
	private LiveDataHelper helper;
	private int autoHideDelay;
	// private SharedPreferences sp;
	protected ArrayList<LiveChannelInfo> liveInfos = null;

	public LiveChannelList(Context context, Handler handler, int delay) {
		super(context);
		mContetx = context;
		mHandler = handler;
		autoHideDelay = delay;
		// sp = mContetx.getSharedPreferences("settingSPF",
		// Context.MODE_PRIVATE);
		helper = LiveDataHelper.getInstance(mContetx);
		init();
	}

	public LiveChannelList(Context context, Handler handler) {
		this(context, handler, 8000);
	}

	// ChannelListAdapter adapter;
	private TextView txtChannelType;
	private ListView channellist;
	private LinearLayout epgsLayout;
	private TextView currEpg, nextEpg;

	public void init() {
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setAnimationStyle(R.style.live_channelList_animation);
		setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		MyLinearLayout root = new MyLinearLayout(mContetx);
		LayoutInflater inflater = (LayoutInflater) mContetx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = (LinearLayout) inflater.inflate(
				R.layout.live_channel_list_layout, root);
		channelTypeList = helper.getTypeList();
		txtChannelType = (TextView) view
				.findViewById(R.id.live_channellist_layout_channeltype_txt);
		ImageView typeLeft = (ImageView) view
				.findViewById(R.id.changetype_left);
		ImageView typeRight = (ImageView) view
				.findViewById(R.id.changetype_right);
		if (channelTypeList !=null ) {
			txtChannelType.setText(channelTypeList.get(tidIndex).tname);
		}
		channellist = (ListView) view
				.findViewById(R.id.live_channellist_layout_channel_list);
		epgsLayout = (LinearLayout) view.findViewById(R.id.channellist_epgs);
		currEpg = (TextView) view.findViewById(R.id.channlelist_curr_epg);
		nextEpg = (TextView) view.findViewById(R.id.channlelist_next_epg);

		TextView emptyView = (TextView) view
				.findViewById(R.id.live_channellist_layout_empty_txt);
		channellist.setEmptyView(emptyView);

		typeLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeListLeft();
			}
		});

		typeRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeListRight();
			}
		});

		channellist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Message msg = mHandler.obtainMessage();
				msg.what = LivePlayer.MSG_CHANGECHANNEL;
				msg.obj = ((ChannelListAdapter) parent.getAdapter())
						.getItem(position);
				mHandler.sendMessage(msg);
				dismiss();
			}
		});

		channellist.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				mHandler.removeCallbacks(hide);
				mHandler.postDelayed(hide, autoHideDelay);
				if (MyApp.getChanState() != 2) {
					mHandler.removeCallbacks(showEpg);
					mHandler.postDelayed(showEpg, 1000);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		/* 对收藏按键监听 Keycode = 183 */
		channellist.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == 183 && event.getAction() == KeyEvent.ACTION_DOWN) {
					if (v == channellist) {
						mHandler.removeCallbacks(hide);
						mHandler.postDelayed(hide, autoHideDelay);
						LiveChannelInfo channel = (LiveChannelInfo) channellist
								.getSelectedItem();
						if (channel.favorite) { // 已经收藏
							helper.updateChannelFav(channel.vid, false);
							channel.favorite = false;
							((ChannelListAdapter) channellist.getAdapter())
									.notifyDataSetChanged();
						} else { // 没有收藏
							// 增加收藏
							helper.updateChannelFav(channel.vid, true);
							channel.favorite = true;
							((ChannelListAdapter) channellist.getAdapter())
									.notifyDataSetChanged();
						}
						return true;
					}
				}
				return false;
			}
		});
		setContentView(root);
	}

	private Runnable showEpg = new Runnable() {
		@Override
		public void run() {
			if (MyApp.getChanState() != 2) {
				getChannelEpg(channellist.getSelectedItemPosition());
			}
		}
	};

	private void getChannelEpg(final int position) {
		epgsLayout.setVisibility(View.VISIBLE);
		epgsLayout.startAnimation(AnimationUtils.loadAnimation(mContetx,
				R.anim.fade_in));
		new HttpWorkTask<Bundle>(new HttpWorkTask.ParseCallBack<Bundle>() {

			@Override
			public Bundle onParse() {
				try {
					return LiveBiz.getLiveEPG(mContetx,
							String.valueOf(liveInfos.get(position).epgid));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}, new HttpWorkTask.PostCallBack<Bundle>() {
			@Override
			public void onPost(Bundle result) {
				if (result == null) {
					if (MyApp.LiveEpg != "-" && MyApp.LiveNextEpg != "-") {
						currEpg.setText(MyApp.LiveEpg);
						nextEpg.setText(MyApp.LiveNextEpg);
					} else {
						currEpg.setText("当前节目：以实际播放为准");
						nextEpg.setText("下个节目：以实际播放为准");
					}
				} else {
					currEpg.setText(result.getString("dqjm"));
					nextEpg.setText(result.getString("xgjm"));
				}
			}
		}).execute();
	}

	@Override
	public void dismiss() {
		mHandler.removeCallbacks(hide);
		epgsLayout.setVisibility(View.INVISIBLE);
		epgsLayout.startAnimation(AnimationUtils.loadAnimation(mContetx,
				R.anim.fade_out));
		// selIndex = 0;
		super.dismiss();
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		update();
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, autoHideDelay);

	}

	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		super.showAsDropDown(anchor, xoff, yoff);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, autoHideDelay);
	}

	private static final String TAG = "LiveChannelList";

	public void refreshView(String tid, int position, int flag) {
		Log.i(TAG, "tid =" + tid + ",position =" + position);
		updateIndex(tid);
		liveInfos = helper.getChannelListByTid(tid);
		/**
		 * 自定义、收藏频道控制
		 */
		if (!tid.equals(VSTDBHelper.CUSTOM_TID)
				&& !tid.equals(VSTDBHelper.FAVORITE_TID)) {
			txtChannelType.setText(channelTypeList.get(tidIndex).tname);
		} else {
			// 列表为空;
			if (liveInfos == null) {
				// 跳过此列表，重新加载下一个类型类表
				if (flag == ConstantUtil.OPERATE_LEFT) {
					changeListLeft();
					return;
				} else if (flag == ConstantUtil.OPERATE_RIGHT) {
					changeListRight();
					return;
				}
			} else {
				if (tid.equals(VSTDBHelper.CUSTOM_TID)
						&& MyApp.getChanState() != 1) {
					if (flag == ConstantUtil.OPERATE_LEFT) {
						changeListLeft();
						return;
					} else if (flag == ConstantUtil.OPERATE_RIGHT) {
						changeListRight();
						return;
					}
				}
				txtChannelType.setText(channelTypeList.get(tidIndex).tname);
			}
		}
		ChannelListAdapter adapter = new ChannelListAdapter(mContetx, liveInfos);
		channellist.setAdapter(adapter);
		channellist.requestFocus();
		channellist.setSelection(position);
	}

	private void updateIndex(String tid) {
		for (int i = 0; i < channelTypeList.size(); i++) {
			if (channelTypeList.get(i).tid.equals(tid)) {
				tidIndex = i;
				break;
			}
		}
		Log.i(TAG, "tidindex =" + tidIndex);
	}

	Runnable hide = new Runnable() {
		@Override
		public void run() {
			dismiss();
		}
	};

	public void relese() {
		if (isShowing()) {
			dismiss();
		}
		mHandler.removeCallbacks(hide);
	}

	protected void changeListLeft() {
		tidIndex = tidIndex - 1;
		if (tidIndex < 0) {
			tidIndex = channelTypeList.size() - 1;
		}
		refreshView(channelTypeList.get(tidIndex).tid, 0,
				ConstantUtil.OPERATE_LEFT);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, autoHideDelay);
	}

	protected void changeListRight() {
		tidIndex = tidIndex + 1;
		if (tidIndex > channelTypeList.size() - 1) {
			tidIndex = 0;
		}
		refreshView(channelTypeList.get(tidIndex).tid, 0,
				ConstantUtil.OPERATE_RIGHT);
		mHandler.removeCallbacks(hide);
		mHandler.postDelayed(hide, autoHideDelay);
	}

	/**
	 * 重写 dispatch 方法 拦截 左右按键的事件
	 * 
	 * @author shenhui
	 * 
	 */
	class MyLinearLayout extends LinearLayout {

		public MyLinearLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public MyLinearLayout(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			int keyCode = event.getKeyCode();
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				changeListRight();
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				changeListLeft();
				return true;
			}
			return super.dispatchKeyEvent(event);
		}
	}
}
