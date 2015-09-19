package com.vst.itv52.v1.player;

import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.custom.ItvToast;
import com.vst.itv52.v1.model.SharpnessEnum;
import com.vst.itv52.v1.model.VideoPlayUrl;
import com.vst.itv52.v1.model.VideoSource;
import com.vst.itv52.v1.util.ConstantUtil;

public class PlayerMenuContrl extends PopupWindow implements OnKeyListener,
		OnClickListener {
	private Context context;
	private Handler handler;
	private LinearLayout source, voice, scalor, fav, list;
	private LinearLayout choose, sharp, srt;
	private static LinearLayout setSrt;
	private TextView sourveTv, scalorTv, sharpTv, favTv, srtTv;
	private static TextView srtSetTex;
	private ImageView sourceLeft, sourceRight;
	private ImageView voiceLeft, voiceRight;
	private ImageView scalorLeft, scalorRight;
	private ImageView sharpLeft, sharpRight;
	private ImageView srtLeft, srtRight;
	private ImageView[] voiceLeve = new ImageView[10];
	private ArrayList<VideoSource> videoSources;
	private int sourceIndex = 0;
	private int sharpIndex = 0;
	private AudioManager audioManager;
	private int menuType;
	private int autoDismiss = 10000;
	private ArrayList<VideoPlayUrl> urls;
	private boolean canShow;
	private boolean waitChanged = true;
	Runnable autoHide = new Runnable() {
		@Override
		public void run() {
			dismiss();
		}
	};

	public PlayerMenuContrl(Context context, Handler handler, int menuType) {
		super();
		this.context = context;
		this.handler = handler;
		this.menuType = menuType;
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
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
		View root = inflater.inflate(R.layout.player_menu_contrl, null);

		voice = (LinearLayout) root.findViewById(R.id.player_menu_voice);
		voiceLeft = (ImageView) voice.findViewById(R.id.player_menu_voice_left);
		voiceRight = (ImageView) voice
				.findViewById(R.id.player_menu_voice_right);

		scalor = (LinearLayout) root.findViewById(R.id.player_menu_sclar);
		scalorTv = (TextView) scalor.findViewById(R.id.player_menu_sclar_tv);
		scalorLeft = (ImageView) scalor
				.findViewById(R.id.player_menu_sclar_left);
		scalorRight = (ImageView) scalor
				.findViewById(R.id.player_menu_sclar_right);
		// 设置字幕
		setSrt = (LinearLayout) root.findViewById(R.id.player_menu_srt_setting);
		srtSetTex = (TextView) root.findViewById(R.id.player_menu_srt_textSet);
		setSrt.setOnClickListener(this);
		if (menuType == ConstantUtil.MENU_LIVE) {
			fav = (LinearLayout) root.findViewById(R.id.player_menu_fav);
			favTv = (TextView) fav.findViewById(R.id.player_menu_fav_text);
			fav.setVisibility(View.VISIBLE);
			fav.setOnClickListener(this);
			list = (LinearLayout) root.findViewById(R.id.player_menu_list);
			list.setVisibility(View.VISIBLE);
			list.setOnClickListener(this);

			source = (LinearLayout) root.findViewById(R.id.player_menu_source);
			sourveTv = (TextView) source
					.findViewById(R.id.player_menu_source_tv);
			sourceLeft = (ImageView) source
					.findViewById(R.id.player_menu_source_left);
			sourceRight = (ImageView) source
					.findViewById(R.id.player_menu_source_right);
			source.setOnKeyListener(this);
			sourceLeft.setOnClickListener(this);
			sourceRight.setOnClickListener(this);

			// updateLiveMenu(((LivePlayer) context).getLiveSourceSize());
		} else if (menuType == ConstantUtil.MENU_VOD) {
			source = (LinearLayout) root.findViewById(R.id.player_menu_source);
			sourveTv = (TextView) source
					.findViewById(R.id.player_menu_source_tv);
			sourceLeft = (ImageView) source
					.findViewById(R.id.player_menu_source_left);
			sourceRight = (ImageView) source
					.findViewById(R.id.player_menu_source_right);
			source.setVisibility(View.VISIBLE);
			source.setOnKeyListener(this);
			sourceLeft.setOnClickListener(this);
			sourceRight.setOnClickListener(this);

			choose = (LinearLayout) root
					.findViewById(R.id.player_menu_chooseSet);
			choose.setOnClickListener(this);
			if (((VodPlayer) context).responseType().contains("电影")
					&& ((VodPlayer) context).responseSetSize() < 2) {
				choose.setVisibility(View.GONE);
			} else {
				choose.setVisibility(View.VISIBLE);
			}

			sharp = (LinearLayout) root.findViewById(R.id.player_menu_sharp);
			sharpLeft = (ImageView) sharp
					.findViewById(R.id.player_menu_sharp_left);
			sharpRight = (ImageView) sharp
					.findViewById(R.id.player_menu_sharp_right);
			sharpTv = (TextView) sharp.findViewById(R.id.player_menu_sharp_tv);
			sharp.setVisibility(View.VISIBLE);
			sharp.setOnKeyListener(this);
			sharpLeft.setOnClickListener(this);
			sharpRight.setOnClickListener(this);

			canShow = MyApp.getCanShowSRT();
			srt = (LinearLayout) root.findViewById(R.id.player_menu_srt);
			srtLeft = (ImageView) srt.findViewById(R.id.player_menu_srt_left);
			srtRight = (ImageView) srt.findViewById(R.id.player_menu_srt_right);
			srtTv = (TextView) srt.findViewById(R.id.player_menu_srt_tv);
			srt.setVisibility(View.VISIBLE);
			matchSRTSwitch();
			srt.setOnKeyListener(this);
			srt.setOnClickListener(this);
			srtLeft.setOnClickListener(this);
			srtRight.setOnClickListener(this);
		} else if (menuType == ConstantUtil.MENU_XLLX) {
			canShow = MyApp.getCanShowSRT();
			// // 搜索离线字幕
			 setSrt = (LinearLayout) root
			 .findViewById(R.id.player_menu_srt_setting);
//			 setSrt.setVisibility(View.VISIBLE);
			 setSrt.setOnClickListener(this);

			srt = (LinearLayout) root.findViewById(R.id.player_menu_srt);
			srtLeft = (ImageView) srt.findViewById(R.id.player_menu_srt_left);
			srtRight = (ImageView) srt.findViewById(R.id.player_menu_srt_right);
			srtTv = (TextView) srt.findViewById(R.id.player_menu_srt_tv);
			srt.setVisibility(View.VISIBLE);
			matchSRTSwitch();
			srt.setOnKeyListener(this);
			srt.setOnClickListener(this);
			srtLeft.setOnClickListener(this);
			srtRight.setOnClickListener(this);
		}

		voiceLeve[0] = (ImageView) voice.findViewById(R.id.menu_voice_1);
		voiceLeve[1] = (ImageView) voice.findViewById(R.id.menu_voice_2);
		voiceLeve[2] = (ImageView) voice.findViewById(R.id.menu_voice_3);
		voiceLeve[3] = (ImageView) voice.findViewById(R.id.menu_voice_4);
		voiceLeve[4] = (ImageView) voice.findViewById(R.id.menu_voice_5);
		voiceLeve[5] = (ImageView) voice.findViewById(R.id.menu_voice_6);
		voiceLeve[6] = (ImageView) voice.findViewById(R.id.menu_voice_7);
		voiceLeve[7] = (ImageView) voice.findViewById(R.id.menu_voice_8);
		voiceLeve[8] = (ImageView) voice.findViewById(R.id.menu_voice_9);
		voiceLeve[9] = (ImageView) voice.findViewById(R.id.menu_voice_10);

		voice.setOnKeyListener(this);
		scalor.setOnKeyListener(this);
		voiceLeft.setOnClickListener(this);
		voiceRight.setOnClickListener(this);
		scalorLeft.setOnClickListener(this);
		scalorRight.setOnClickListener(this);

		setContentView(root);
	}

	protected void updateLiveMenu(int sourceSize) {
		if (sourceSize > 1) {
			source.setVisibility(View.VISIBLE);
		} else {
			source.setVisibility(View.GONE);
		}
	}

	public ArrayList<VideoPlayUrl> getUrls() {
		return urls;
	}

	public void setUrls(ArrayList<VideoPlayUrl> urls) {
		this.urls = urls;
	}

	public void setVideoSources(ArrayList<VideoSource> videoSources,
			int sourceIndex) {
		if (videoSources != null) {
			this.videoSources = videoSources;
			// this.sourceIndex = sourceIndex;
			// sourveTv.setText(this.videoSources.get(this.sourceIndex)
			// .getSource());
			setVideoSources(sourceIndex);
		} else {
			this.videoSources = new ArrayList<VideoSource>();
			sourveTv.setText("无视频源");
		}
		// 少于2个源，不显示选源
		if (this.videoSources.size() <= 1) {
			source.setVisibility(View.GONE);
		}
	}

	public void setVideoSources(int sourceIndex) {
		if (sourceIndex >= 0 && sourceIndex < videoSources.size()) {
			this.sourceIndex = sourceIndex;
			sourveTv.setText(this.videoSources.get(this.sourceIndex).sourceName);
		}
	}

	private void setVoice(int flag) {
		// 减小音量
		if (flag == AudioManager.ADJUST_LOWER) {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			showVoiceLevel(getVoice());
			// 增大音量
		} else if (flag == AudioManager.ADJUST_RAISE) {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, flag,
					AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			showVoiceLevel(getVoice());
		}
	}

	public void showVoiceLevel(int level) {
		if (level <= 0) {
			for (ImageView img : voiceLeve) {
				img.setImageResource(R.drawable.menu_voice_outuse);
			}
		} else if (level > 0 && level <= 10) {
			for (int i = 0; i < 10; i++) {
				if (i < level) {
					voiceLeve[i].setImageResource(R.drawable.menu_voice_inuse);
				} else {
					voiceLeve[i].setImageResource(R.drawable.menu_voice_outuse);
				}
			}
		} else {
			for (ImageView img : voiceLeve) {
				img.setImageResource(R.drawable.menu_voice_inuse);
			}
		}
	}

	public int getVoice() {
		int currentVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		return (int) (((float) currentVolume / maxVolume) * 10);
	}

	/**
	 * 界面初始时显示缩放
	 * 
	 * @param flag
	 */
	public void setScalor(int flag) {
		if (scalorTv != null) {
			flag = flag % 3;
			switch (flag) {
			case VideoView.A_16X9:
				scalorTv.setText("16:9");
				break;
			case VideoView.A_4X3:
				scalorTv.setText("4:3");
				break;
			case VideoView.A_DEFALT:
				scalorTv.setText("原始比例");
				break;
			default:
				break;
			}
		}
	}

	public void setSharpness(ArrayList<VideoPlayUrl> urls, SharpnessEnum sharp) {
		this.urls = urls;
		sharpTv.setText(sharp.getName());
	}

	public void setFavText(String text) {
		favTv.setText(text);
	}

	private void matchSRTSwitch() {
		if (canShow) {
			canShow = true;
			srtTv.setText("点击搜索");
		} else {
			srtTv.setText("关闭");
			canShow = false;
		}

	}

	private void setCanShowSRT() {
		if (canShow) {
			canShow = false;
			// 停止刷新字幕的线程
			if (context instanceof VodPlayer) {
				VodPlayer.isFrush = false;
			}
			if (context instanceof XLLXPlayer) {
				XLLXPlayer.isFrush = false;
			}
		} else {
			canShow = true;
			handler.sendEmptyMessage(XLLXPlayer.SRT_ALLOW);
		}
		MyApp.setCanShowSRT(canShow);
		matchSRTSwitch();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.player_menu_source:
				if (menuType == ConstantUtil.MENU_VOD) {
					if (sourceIndex > 0) {
						sourceIndex--;
						setVideoSources(sourceIndex);
						handler.sendMessage(handler.obtainMessage(
								VodPlayer.MSG_SELECTSOURCE, sourceIndex, 0));
					}
				} else if (menuType == ConstantUtil.MENU_LIVE) {
					((LivePlayer) context).previousSource();
				}
				break;
			case R.id.player_menu_voice:
				setVoice(AudioManager.ADJUST_LOWER);
				break;
			case R.id.player_menu_sclar:
				switch (menuType) {
				case ConstantUtil.MENU_VOD:
					scalorTv.setText(((VodPlayer) context)
							.changScale(ConstantUtil.OPERATE_LEFT));
					break;
				case ConstantUtil.MENU_LIVE:
					scalorTv.setText(((LivePlayer) context)
							.changScale(ConstantUtil.OPERATE_LEFT));
					break;
				case ConstantUtil.MENU_BACK:
					scalorTv.setText(((TVBackActivity) context)
							.changeScalType(ConstantUtil.OPERATE_LEFT));
					break;
				case ConstantUtil.MENU_NEWS:
					scalorTv.setText(((NewsInformation) context)
							.changeScalType(ConstantUtil.OPERATE_LEFT));
					break;
				case ConstantUtil.MENU_XLLX:
					((XLLXPlayer) context)
							.changeScale(ConstantUtil.OPERATE_LEFT);
					break;
				}
				break;
			case R.id.player_menu_sharp:
				sharpIndex--;
				Log.d("info", "urls="+urls);
				if(urls==null){
					urls=getUrls();
				}
				if (urls !=null && !urls.isEmpty() && urls.size()>0) {
					if (sharpIndex < 0) {
						sharpIndex = urls.size() - 1;
					}
					sharpTv.setText(urls.get(sharpIndex).sharp.getName());
					MyApp.setSharpness(sharpIndex);
					handler.sendMessage(handler.obtainMessage(
							VodPlayer.MSG_PLAY,
							((VodPlayer) context).getCurrentPosition(),sharpIndex,
							urls.get(sharpIndex).playurl));
					scalorTv.setText("16:9");
				}
				break;
			case R.id.player_menu_srt:
				setCanShowSRT();
				break;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.player_menu_source:
				if (menuType == ConstantUtil.MENU_VOD) {
					if (sourceIndex < videoSources.size() - 1) {
						sourceIndex++;
						setVideoSources(sourceIndex);
						handler.sendMessage(handler.obtainMessage(
								VodPlayer.MSG_SELECTSOURCE, sourceIndex, 0));
					}
				} else if (menuType == ConstantUtil.MENU_LIVE) {
					((LivePlayer) context).nextSource();
				}
				break;
			case R.id.player_menu_voice:
				setVoice(AudioManager.ADJUST_RAISE);
				break;
			case R.id.player_menu_sclar:
				switch (menuType) {
				case ConstantUtil.MENU_VOD:
					scalorTv.setText(((VodPlayer) context)
							.changScale(ConstantUtil.OPERATE_RIGHT));
					break;
				case ConstantUtil.MENU_LIVE:
					scalorTv.setText(((LivePlayer) context)
							.changScale(ConstantUtil.OPERATE_RIGHT));
					break;
				case ConstantUtil.MENU_BACK:
					scalorTv.setText(((TVBackActivity) context)
							.changeScalType(ConstantUtil.OPERATE_RIGHT));
					break;
				case ConstantUtil.MENU_NEWS:
					scalorTv.setText(((NewsInformation) context)
							.changeScalType(ConstantUtil.OPERATE_RIGHT));
					break;
				case ConstantUtil.MENU_XLLX:
					((XLLXPlayer) context)
							.changeScale(ConstantUtil.OPERATE_RIGHT);
					break;
				}
				break;
			case R.id.player_menu_sharp://设置清晰度
				sharpIndex++;
				Log.d("info", "urls="+urls);
				
				if (urls != null&& !urls.isEmpty() && urls.size()>0 ) {
					if (sharpIndex >= urls.size()) {
						sharpIndex = 0;
					}
						sharpTv.setText(urls.get(sharpIndex).sharp.getName());
						MyApp.setSharpness(sharpIndex);
						Log.i("info", "sharpIndex="+sharpIndex);
						handler.sendMessage(handler.obtainMessage(
								VodPlayer.MSG_PLAY,
								((VodPlayer) context).getCurrentPosition(), 0,
								urls.get(sharpIndex).playurl));
						scalorTv.setText("16:9");
				}
				break;
			case R.id.player_menu_srt:
				setCanShowSRT();
				break;
			}
		}
		handler.removeCallbacks(autoHide);
		handler.postDelayed(autoHide, autoDismiss);
		return false;
	}

	// 显示字幕设置
	public static void showSrtSet(boolean isShow, String text) {
		if (isShow) {
			setSrt.setVisibility(View.VISIBLE);
		} else {
			setSrt.setVisibility(View.GONE);
		}
		srtSetTex.setText(text);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.player_menu_fav:
			((LivePlayer) context).favCurrentChannel();
			this.dismiss();
			break;
		case R.id.player_menu_list:
			Message msg = handler
					.obtainMessage(LivePlayer.MSG_LIVE_CHANNEL_LIST);
			Bundle data = new Bundle();
			data.putString(ConstantUtil.LIVE_TID_EXTRA, null);
			data.putInt(ConstantUtil.LIVE_INDEX_EXTRA, -1);
			msg.setData(data);
			handler.sendMessage(msg);
			this.dismiss();
			break;
		case R.id.player_menu_source_left:
			if (menuType == ConstantUtil.MENU_VOD) {
				if (sourceIndex > 0) {
					sourceIndex--;
					setVideoSources(sourceIndex);
					handler.sendMessage(handler.obtainMessage(
							VodPlayer.MSG_SELECTSOURCE, sourceIndex, 0));
				}
			} else if (menuType == ConstantUtil.MENU_LIVE) {
						((LivePlayer) context).previousSource();
			}
			break;
		case R.id.player_menu_source_right:
			if (menuType == ConstantUtil.MENU_VOD) {
				if (sourceIndex < videoSources.size() - 1) {
					sourceIndex++;
					setVideoSources(sourceIndex);
					handler.sendMessage(handler.obtainMessage(
							VodPlayer.MSG_SELECTSOURCE, sourceIndex, 0));
				}
			} else if (menuType == ConstantUtil.MENU_LIVE) {
				((LivePlayer) context).nextSource();
			}
			break;
		case R.id.player_menu_sclar_left:
			if (menuType == ConstantUtil.MENU_VOD) {
				scalorTv.setText(((VodPlayer) context)
						.changScale(ConstantUtil.OPERATE_LEFT));
			} else if (menuType == ConstantUtil.MENU_LIVE) {
				scalorTv.setText(((LivePlayer) context)
						.changScale(ConstantUtil.OPERATE_LEFT));
			}
			break;
		case R.id.player_menu_sclar_right:
			if (menuType == ConstantUtil.MENU_VOD) {
				scalorTv.setText(((VodPlayer) context)
						.changScale(ConstantUtil.OPERATE_RIGHT));
			} else if (menuType == ConstantUtil.MENU_LIVE) {
				scalorTv.setText(((LivePlayer) context)
						.changScale(ConstantUtil.OPERATE_RIGHT));
			}
			break;
		case R.id.player_menu_voice_left:
			setVoice(AudioManager.ADJUST_LOWER);
			break;
		case R.id.player_menu_voice_right:
			setVoice(AudioManager.ADJUST_RAISE);
			break;
		case R.id.player_menu_chooseSet:
			((VodPlayer) context).showChooseSetDialog(true);
			this.dismiss();
			break;
		case R.id.player_menu_sharp_left://设置清晰度
			sharpIndex--;
			Log.d("info", "urls="+urls);
			
			if (urls != null&& !urls.isEmpty() && urls.size()>0 ) {
			if (sharpIndex < 0) {
				sharpIndex = urls.size() - 1;
			}
			sharpTv.setText(urls.get(sharpIndex).sharp.getName());
			MyApp.setSharpness(sharpIndex);
			handler.sendMessage(handler.obtainMessage(VodPlayer.MSG_PLAY,
					((VodPlayer) context).getCurrentPosition(), sharpIndex,
					urls.get(sharpIndex).playurl));
			scalorTv.setText("16:9");
			}
			break;
		case R.id.player_menu_sharp_right://设置清晰度
			sharpIndex++;
			Log.d("info", "urls="+urls);
			
			if (urls != null&& !urls.isEmpty() && urls.size()>0 ) {
			if (sharpIndex >= urls.size()) {
				sharpIndex = 0;
			}
			sharpTv.setText(urls.get(sharpIndex).sharp.getName());
			MyApp.setSharpness(sharpIndex);
			handler.sendMessage(handler.obtainMessage(VodPlayer.MSG_PLAY,
					((VodPlayer) context).getCurrentPosition(), sharpIndex,
					urls.get(sharpIndex).playurl));
			scalorTv.setText("16:9");
			}
			break;
		case R.id.player_menu_srt_left:

		case R.id.player_menu_srt_right:
			setCanShowSRT();
			break;
		case R.id.player_menu_srt:// 搜索字幕
			if (MyApp.getCanShowSRT()) {
				String str = srtTv.getText().toString();
				if ("点击搜索".equals(str)) {
					if (menuType == ConstantUtil.MENU_XLLX) {
						((XLLXPlayer) context).showSearchSrtPop();
						dismiss();
					} else if (menuType == ConstantUtil.MENU_VOD) {
						((VodPlayer) context).showSearchSrtPop();
						dismiss();
					}
				}
			}
			break;
		case R.id.player_menu_srt_setting:// 字幕设置
			if (MyApp.getCanShowSRT()) {

				// 如果在迅雷离线这个类中
				if (context instanceof XLLXPlayer) {
					XLLXPlayer xllxPlayer = (XLLXPlayer) context;
					// 需要加标记，是显示下载的字幕还是离线的字幕
					// 显示离线字幕的设置
					if (XLLXPlayer.LXORDOWN == 1) {
						if ((xllxPlayer.getSrtList() != null && xllxPlayer
								.getSrtList().size() > 0)) {
							xllxPlayer.showSRTPop();
							// 如果没有的话 进入搜索界面
						}
						// 显示下载字幕的设置
					} else if (XLLXPlayer.LXORDOWN == 2) {
						if (xllxPlayer.getFiles() != null
								&& xllxPlayer.getFiles().size() > 0) {
							xllxPlayer.showSRTPop(xllxPlayer.getFiles());
						}
					}
					dismiss();
				}
				// 点播
				if (context instanceof VodPlayer) {
					VodPlayer vodPlayer = (VodPlayer) context;
					if (vodPlayer.getFiles() != null
							&& vodPlayer.getFiles().size() > 0) {
						vodPlayer.showSRTPop(vodPlayer.getFiles());
					}
				}
				dismiss();

			}
			break;
		}
		handler.removeCallbacks(autoHide);
		handler.postDelayed(autoHide, autoDismiss);
	}
	
	Runnable cancleChanged = new Runnable() {
		@Override
		public void run() {
			waitChanged = true;
		}
	};
	@Override
	public void dismiss() {
		handler.removeCallbacks(autoHide);
		super.dismiss();
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		handler.removeCallbacks(autoHide);
		handler.postDelayed(autoHide, autoDismiss);
		super.showAtLocation(parent, gravity, x, y);
	}

}
