package com.vst.itv52.v1.app;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.vst.itv52.v1.R;
import com.vst.itv52.v1.https.HttpUtils;
import com.vst.itv52.v1.util.ConfigUtil;
import com.vst.itv52.v1.util.ConstantUtil;
import com.vst.itv52.v1.util.MD5Util;

public class MyApp extends Application {

	public static ExecutorService pool = null;
	public static String baseServer;
	public static int scaleMod;//比例模式
	public static int sharpness;//清晰度 锐利
	public static int liveLrFunction;
	public static int liveUdFunction;
	public static SQLiteOpenHelper dbHeper;
	public static SharedPreferences sp;
	public static boolean isOnline = true;
	private static HashMap<String, Integer> soundMap;//声音
	private static SoundPool soundPool;
	private static boolean loaded = false;
	public static Context context;
	public static boolean flyWhiteBorder = false;
	public static long enjoyStart;//开始享用，欣赏时间，打开软件的时间

	public static String User_Mac = "";
	public static String LiveSeek = "0";
	public static String LiveEpg = "-";
	public static String LiveNextEpg = "-";
	public static String LiveNextUrl = "-";
	public static String LiveCookie = "";

	public static String Live_Range = "live.gslb.letv.com/gslb";//"live.gslb.letv.com/gslb";//范围
	public static String Live_Referer = "flv.cntv.wscdns.com|91vst.com";// "flv.cntv.wscdns.com|myvst.net@http://cdn.52itv.cn/|ahtv.cn";

	public static String User_Ver = "GGwlPlayer/QQ243944493 ("+ Build.MODEL + ")";
	//public static String User_Agent = "AppleCoreMedia/1.0.0.9A405 (iPad; U; CPU OS 5_0_1 like Mac OS X; zh_cn)";
	public static String User_Agent = "VST-2.0";//1.0.0.9A405 (iPad; U; CPU OS 5_0_1 like Mac OS X; zh_cn)";
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(getApplicationContext());
		// 创建全局的线程池
		pool = Executors.newFixedThreadPool(2);

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		if (dm.heightPixels == 720 || dm.widthPixels == 1280) {
			flyWhiteBorder = true;
		} else {
			flyWhiteBorder = false;
		}
		// 初始化基本设置
		sp = getApplicationContext().getSharedPreferences("settingSPF",
				Context.MODE_PRIVATE);
		baseServer = sp.getString("server",
				ConfigUtil.getValue(randomDefaultServer()));
		scaleMod = sp.getInt("scalemod", 2);
		sharpness = sp.getInt("sharpness", 2);// 默认高清
		liveLrFunction = sp.getInt("livelr", 1);// 左右换源
		liveUdFunction = sp.getInt("liveud", 0);// 上增加频道
		enjoyStart = System.currentTimeMillis();
		initSound();
	}

	/**
	 * 随机获取properties的key的value
	 * @return
	 */
	private String randomDefaultServer() {
		int index = new Random().nextInt(3);
		System.out.println("SERVER_INDEX"+index);
		if (index == 0) {
			return ConstantUtil.SERVER_1;
		} else if (index == 1) {
			return ConstantUtil.SERVER_2;
		} else {
			return ConstantUtil.SERVER_3;
		}
	}

	private void initSound() {
		soundMap = new HashMap<String, Integer>();
		// ((Activity)
		// getApplicationContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				loaded = true;
			}
		});
		soundMap.put(ConstantUtil.BACK, soundPool.load(this, R.raw.back, 1));
		soundMap.put(ConstantUtil.BACK_TO_TOP,
				soundPool.load(this, R.raw.back_to_top, 1));
		soundMap.put(ConstantUtil.COMFIRE,
				soundPool.load(this, R.raw.comfire, 1));
		soundMap.put(ConstantUtil.MOVE_DOWN,
				soundPool.load(this, R.raw.move_bottom, 1));
		soundMap.put(ConstantUtil.MOVE_LEFT,
				soundPool.load(this, R.raw.move_left, 1));
		soundMap.put(ConstantUtil.MOVE_RIGHT,
				soundPool.load(this, R.raw.move_right, 1));
		soundMap.put(ConstantUtil.NET_CONNECTED,
				soundPool.load(this, R.raw.net_connected, 1));
		soundMap.put(ConstantUtil.NET_FOUND,
				soundPool.load(this, R.raw.net_found, 1));
		soundMap.put(ConstantUtil.TOP_FLOAT_DISABLED,
				soundPool.load(this, R.raw.top_float_disabled, 1));
		soundMap.put(ConstantUtil.TOP_FLOAT,
				soundPool.load(this, R.raw.top_float, 1));
		soundMap.put(ConstantUtil.PAGE_CHANGE,
				soundPool.load(this, R.raw.page_change, 1));
	}

	public static void playSound(final String action) {

		AudioManager amgr = (AudioManager) context
				.getSystemService(AUDIO_SERVICE);
		float volumeCurrent = amgr.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volumeMax = amgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = volumeCurrent / volumeMax;
		if (loaded) {
			if (sp.getBoolean("play_sound", true)) {
				soundPool.play(soundMap.get(action), volume, volume, 1, 0, 1f);
			}
		}
	}

	/**
	 * 生成 直播KEY
	 * 
	 * @return
	 */
//	public static String get_livekey() {
//		 String str = "QQ243944493-by-zjjtv/uuid:" + "";
//			return MD5Util.getMD5String(""+ String.valueOf(System.currentTimeMillis()).substring(0, 8) + "-time-key-"+str).substring(0, 16);
//		//return MD5Util.getMD5String("time-"+ String.valueOf(System.currentTimeMillis()).substring(0, 8) + "/key-52itvlive").substring(0, 16);
//	}
//	
	
	  public static String get_livekey()
	  {
	    StringBuilder localStringBuilder = new StringBuilder("time-");
	    String str = String.valueOf(System.currentTimeMillis()).substring(0, 8);
	    return MD5Util.getMD5String(str + "/key-52itvlive").substring(0, 16);
	  }
	
	/**
	 *  抓取网页源码
	 * 
	 */
	public static String curl(String url){
		Header[] extra = new Header[]{
				new BasicHeader("User-Agent", User_Agent),
				new BasicHeader("User-Mac", User_Mac),
				new BasicHeader("User-Key", get_livekey()),
				new BasicHeader("User-Ver", User_Ver),
		} ;
		return HttpUtils.getContent(url, extra, null);
	}
	
	public static void setLive_Cookie(String Cookie) {
		MyApp.LiveCookie = Cookie;
		Editor editor = sp.edit();
		editor.putString("LiveCookie", Cookie);
		editor.commit();
	}

	public static void setUserMac(String str) {
		MyApp.User_Mac = str;
		Editor editor = sp.edit();
		editor.putString("User_Mac", str);
		editor.commit();
	}

	public static void setLiveSeek(String str) {
		MyApp.LiveSeek = str;
		Editor editor = sp.edit();
		editor.putString("LiveSeek", str);
		editor.commit();
	}

	public static void setLiveEpg(String str) {
		MyApp.LiveEpg = str;
		Editor editor = sp.edit();
		editor.putString("LiveEpg", str);
		editor.commit();
	}

	public static void setLiveNextEpg(String str) {
		MyApp.LiveNextEpg = str;
		Editor editor = sp.edit();
		editor.putString("LiveNextEpg", str);
		editor.commit();
	}

	public static void setLiveNextUrl(String str) {
		MyApp.LiveNextUrl = str;
		Editor editor = sp.edit();
		editor.putString("LiveNextUrl", str);
		editor.commit();
	}

	public static void setScaleMod(int scaleMod) {
		MyApp.scaleMod = scaleMod;
		Editor editor = sp.edit();
		editor.putInt("scalemod", scaleMod);
		editor.commit();
	}

	public static void setBase(String base) {
		MyApp.baseServer = base;
		Editor editor = sp.edit();
		editor.putString("server", base);
		editor.commit();
	}

	public static void setLoginKey(String key) {
		Editor editor = sp.edit();
		editor.putString("login_key", key);
		editor.commit();
	}

	/* 0、默认官方列表；1、官方和本地自定义；2、网络自定义 ;3、全部*/
	public static void setChanState(int listType) {
		Editor editor = sp.edit();
		editor.putInt("channel_list_type", listType);
		editor.commit();
	}

	public static void setAutoLive(int autoMod) {
		Editor editor = sp.edit();
		editor.putInt("auto_play_live", autoMod);
		editor.commit();
	}

	public static void setForgiveVersion(int version) {
		Editor editor = sp.edit();
		editor.putInt("forgive_version", version);
		editor.commit();
	}

	public static void setSearchKeybord(String bord) {
		Editor editor = sp.edit();
		editor.putString("search_keybord", bord);
		editor.commit();
	}

	/** 设置是否显示字幕 */
	public static void setCanShowSRT(boolean canshow) {
		Editor editor = sp.edit();
		editor.putBoolean("show_srt", canshow);
		editor.commit();
	}

	/** 设置字幕字体大小 */
	public static void setSRTTextSize(int size) {
		Editor editor = sp.edit();
		editor.putInt("srt_text_size", size);
		editor.commit();
	}

	/** 设置字幕字体颜色 */
	public static void setSRTTEXTColor(int[] colors) {
		Editor editor = sp.edit();
		editor.putInt("srt_text_color", colors[0]);
		editor.putInt("srt_text_shadow", colors[1]);
		editor.commit();
	}

	/** 设置字幕偏移量 */
	public static void setSRTLocation(int level) {
		Editor editor = sp.edit();
		editor.putInt("srt_text_location", level);
		editor.commit();
	}

	/** 获取字幕偏移量 */
	public static int getSSRTLocation() {
		return sp.getInt("srt_text_location", 0);
	}

	/** 获取字幕颜色 */
	public static int[] getSRTTextColor() {
		int[] colors = new int[2];
		colors[0] = sp.getInt("srt_text_color", 0xffffffff);
		colors[1] = sp.getInt("srt_text_shadow", 0xff333333);
		return colors;
	}

	/** 获取字幕字体大小 */
	public static int getSRTTextSize() {
		return sp.getInt("srt_text_size", 38);
	}

	public static boolean getCanShowSRT() {
		return sp.getBoolean("show_srt", false);
	}

	public static String getSearchKeyBord() {
		return sp.getString("search_keybord", "full");
	}

	public static int getForgiveVersion() {
		return sp.getInt("forgive_version", 0);
	}

	public static int getAutoLive() {
		return sp.getInt("auto_play_live", 0);
	}

	/** 0、默认官方列表；1、官方和本地自定义；2、网络自定义 */
	public static int getChanState() {
		return sp.getInt("channel_list_type", 0);
	}

	public static int getLastChannel() {
		if (getChanState() == 2) {
			return sp.getInt("last_channel_net", 10000);
		} else {
			return sp.getInt("last_channel", 10001);
		}
	}

	public static String getLoginKey() {
		return sp.getString("login_key", null);
	}

	public static void setLastChannel(int vid) {
		Editor editor = sp.edit();
		if (getChanState() == 2) {
			editor.putInt("last_channel_net", vid);
		} else {
			editor.putInt("last_channel", vid);
		}
		editor.commit();
	}

	public static void setSharpness(int sharpness) {
		MyApp.sharpness = sharpness;
		Editor editor = sp.edit();
		editor.putInt("sharpness", sharpness);
		editor.commit();
	}

	public static void setLiveLrFunction(int func) {
		liveLrFunction = func;
		Editor editor = sp.edit();
		editor.putInt("livelr", func);
		editor.commit();
	}

	public static void setLiveUdFunction(int func) {
		liveUdFunction = func;
		Editor editor = sp.edit();
		editor.putInt("liveud", func);
		editor.commit();
	}

	public static long getApkRunTime() {
		return sp.getLong("APKRunTime", 0);
	}

	public static void setApkRunTime(long times) {
		Editor editor = sp.edit();
		editor.putLong("APKRunTime", times);
		editor.commit();
	}

	public static void setVodJump(boolean jump) {
		Editor editor = sp.edit();
		editor.putBoolean("vod_jump", jump);
		editor.commit();
	}

	public static boolean getVodJump() {
		return sp.getBoolean("vod_jump", false);
	}

	public static void setJumpStart(int start) {
		Editor editor = sp.edit();
		editor.putInt("jump_start", start * 1000);
		editor.commit();
	}

	public static void setJumpSEnd(int end) {
		Editor editor = sp.edit();
		editor.putInt("jump_end", end * 1000);
		editor.commit();
	}

	public static int getJumpStart() {
		return sp.getInt("jump_start", 90000);
	}

	public static int getJumpEnd() {
		return sp.getInt("jump_end", 90000);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		pool.isTerminated();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	public static void setLive_Referer(String referer) {
		MyApp.Live_Referer = referer;
		Editor editor = sp.edit();
		editor.putString("Live_Referer", referer);
		editor.commit();
	}

	public static void setLive_Range(String range) {
		//获取Ip地址
		MyApp.Live_Range = range;
		Editor editor = sp.edit();
		editor.putString("Live_Range", range);
		editor.commit();
	}
	
	public static void setTvRecommend(String url) {
		MyApp.Live_Range = url;
		Editor editor = sp.edit();
		editor.putString("tv_reommend", url);
		editor.commit();
	}
	public static String getTvRecommend() {
		
		return sp.getString("tv_reommend", "");
	}
	
	
	public static String e(String paramString)
	  {
	    String str1 = "";
	    DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
	    localDefaultHttpClient.getParams().setParameter("http.socket.timeout", Integer.valueOf(3000));
	    localDefaultHttpClient.getParams().setParameter("http.connection.timeout", Integer.valueOf(2000));
	    localDefaultHttpClient.getParams().setParameter("http.protocol.handle-redirects", Boolean.valueOf(true));
	    HttpGet localHttpGet = new HttpGet(paramString);
	    localHttpGet.setHeader("User-Agent", "VST-2.0");
	    if ((str1 != null) && (str1.length() > 45))
	      localHttpGet.setHeader("Cookie", "key=" + str1);
	    try
	    {
	      HttpResponse localHttpResponse = localDefaultHttpClient.execute(localHttpGet);
	      int i1 = localHttpResponse.getStatusLine().getStatusCode();
	      String localObject = null;
	      if (i1 == 200)
	      {
	        String str2 = EntityUtils.toString(localHttpResponse.getEntity(), "UTF-8");
	        localObject = str2;
	      }
	      return localObject;
	    }
	    catch (Throwable localThrowable)
	    {
	      localThrowable.printStackTrace();
	    }
	    return null;
	  }
	
	
}
