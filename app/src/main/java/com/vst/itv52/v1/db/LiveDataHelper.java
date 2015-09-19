package com.vst.itv52.v1.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vst.itv52.v1.app.MyApp;
import com.vst.itv52.v1.model.LiveChannelInfo;
import com.vst.itv52.v1.model.LiveDataInfo;
import com.vst.itv52.v1.model.LiveDataInfo.ChannelInfo;
import com.vst.itv52.v1.model.LiveDataInfo.Typeinfo;
import com.vst.itv52.v1.model.LiveTypeInfo;
import com.vst.itv52.v1.util.ConstantUtil;

public class LiveDataHelper {
	private static final String TAG = "LiveDataHelper";
	private VSTDBHelper dbHelper;
	private static String viewName;
	private static String tableName;
	private static String typeName;

	private static LiveDataHelper mInstance = null;

	private LiveDataHelper(Context mContext) {
		super();
		dbHelper = VSTDBHelper.getInstance(mContext);
	}

	public static synchronized LiveDataHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LiveDataHelper(context);
		}
		/* 获取设置中的直播列表类型，确定将要操作的是哪张表 */
		if (MyApp.getChanState() == 2) {
			viewName = VSTDBHelper.VNAME_CHANNEL_INFO_NET;
			tableName = VSTDBHelper.TNAME_CHANNEL_INFO_NET;
			typeName = VSTDBHelper.TNAME_TYPE_INFO_NET;
		} else {
			viewName = VSTDBHelper.VNAME_CHANNEL_INFO;
			tableName = VSTDBHelper.TNAME_CHANNEL_INFO;
			typeName = VSTDBHelper.TNAME_TYPE_INFO;
		}
		System.out.println("Current Live Table:" + tableName
				+ "; Current Live View:" + viewName);
		return mInstance;
	}

	/**
	 * 根据 tid 得到 channel list 包括 收藏 和自定义
	 * 
	 * @param tid
	 * @return
	 */
	public ArrayList<LiveChannelInfo> getChannelListByTid(String tid) {
		System.out.println("getChannelListByTid>>>>> " + tid);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<LiveChannelInfo> channelList = null;
		Cursor c = null;
		if (MyApp.getChanState() == ConstantUtil.LIVE_LIST_ALL
				&& tid.equals("-1")) {
			c = db.query(VSTDBHelper.VNAME_CHANNEL_INFO_NET, null, null, null,
					null, null, null);
		} else if (tid.equals(VSTDBHelper.FAVORITE_TID)) {
			c = db.query(viewName, null, VSTDBHelper.FAVORIT + "=?",
					new String[] { "1" }, VSTDBHelper.VID, null, null);
		} else {
			c = db.query(viewName, null, " tid like ?", new String[] { "%"
					+ tid + "%" }, null, null, null);
		}
		if (c.getCount() > 0) {
			channelList = new ArrayList<LiveChannelInfo>();
			while (c.moveToNext()) {
				LiveChannelInfo info = new LiveChannelInfo();
				info.vid = c.getInt(c.getColumnIndex(VSTDBHelper.VID));
				info.num = c.getInt(c.getColumnIndex(VSTDBHelper.NUM));
				info.vname = c.getString(c.getColumnIndex(VSTDBHelper.VNAME));
				info.tid = new String[] { tid };
				info.epgid = c.getString(c.getColumnIndex(VSTDBHelper.EPGID));
				info.huibo = c.getString(c.getColumnIndex(VSTDBHelper.HUIBO));
				info.quality = c.getString(c
						.getColumnIndex(VSTDBHelper.QUALITY));
				info.pinyin = c.getString(c.getColumnIndex(VSTDBHelper.PINYIN));
				String sourceText = c.getString(c
						.getColumnIndex(VSTDBHelper.SOURCETEXT));
				if (sourceText.contains("%0A")) {
					info.liveSources = sourceText.split("%0A");
				} else {
					info.liveSources = sourceText.split("#");
				}
				info.lastSource = c.getInt(c
						.getColumnIndex(VSTDBHelper.LASTSOURCE));
				info.duration = c.getLong(c
						.getColumnIndex(VSTDBHelper.DURATION));
				info.favorite = (c
						.getInt(c.getColumnIndex(VSTDBHelper.FAVORIT)) != 0);
				channelList.add(info);
			}
		}
		c.close();
		db.close();
		return channelList;
	}
	public LiveChannelInfo getChannelByVid(int vid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor c = db.query(viewName, null, VSTDBHelper.VID + "=" + vid,
					null, null, null, null);
			LiveChannelInfo info = null;
			if (c.getCount() > 0) {
				c.moveToFirst();
				info = new LiveChannelInfo();
				info.vid = c.getInt(c.getColumnIndex(VSTDBHelper.VID));
				info.vname = c.getString(c.getColumnIndex(VSTDBHelper.VNAME));
				info.num = c.getInt(c.getColumnIndex(VSTDBHelper.NUM));
				info.tid = c.getString(c.getColumnIndex(VSTDBHelper.TID))
						.split(",");
				info.epgid = c.getString(c.getColumnIndex(VSTDBHelper.EPGID));
				info.huibo = c.getString(c.getColumnIndex(VSTDBHelper.HUIBO));
				info.quality = c.getString(c
						.getColumnIndex(VSTDBHelper.QUALITY));
				info.pinyin = c.getString(c.getColumnIndex(VSTDBHelper.PINYIN));
				String sourceText = c.getString(c
						.getColumnIndex(VSTDBHelper.SOURCETEXT));
				if (sourceText.contains("%0A")) {
					info.liveSources = sourceText.split("%0A");
				} else {
					info.liveSources = sourceText.split("#");
				}
				info.lastSource = c.getInt(c
						.getColumnIndex(VSTDBHelper.LASTSOURCE));
				info.duration = c.getLong(c
						.getColumnIndex(VSTDBHelper.DURATION));
				info.favorite = (c
						.getInt(c.getColumnIndex(VSTDBHelper.FAVORIT)) != 0);
			}
			c.close();
			db.close();
			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取 最近的频道 依据时长
	 * 
	 * @return
	 */
	public LiveChannelInfo getLastChannel() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(viewName, null, null, null, VSTDBHelper.VID, null,
				VSTDBHelper.DURATION + " desc ", "1");
		LiveChannelInfo info = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			info = new LiveChannelInfo();
			info.vid = c.getInt(c.getColumnIndex(VSTDBHelper.VID));
			info.vname = c.getString(c.getColumnIndex(VSTDBHelper.VNAME));
			info.num = c.getInt(c.getColumnIndex(VSTDBHelper.NUM));
			info.tid = c.getString(c.getColumnIndex(VSTDBHelper.TID))
					.split(",");
			info.epgid = c.getString(c.getColumnIndex(VSTDBHelper.EPGID));
			info.huibo = c.getString(c.getColumnIndex(VSTDBHelper.HUIBO));
			info.quality = c.getString(c.getColumnIndex(VSTDBHelper.QUALITY));
			info.pinyin = c.getString(c.getColumnIndex(VSTDBHelper.PINYIN));
			String sourceText = c.getString(c
					.getColumnIndex(VSTDBHelper.SOURCETEXT));
			if (sourceText.contains("%0A")) {
				info.liveSources = sourceText.split("%0A");
			} else {
				info.liveSources = sourceText.split("#");
			}
			info.lastSource = c
					.getInt(c.getColumnIndex(VSTDBHelper.LASTSOURCE));
			info.duration = c.getLong(c.getColumnIndex(VSTDBHelper.DURATION));
			info.favorite = (c.getInt(c.getColumnIndex(VSTDBHelper.FAVORIT)) != 0);
		}
		c.close();
		db.close();
		return info;
	}

	/**
	 * 更新 是否 收藏
	 * 
	 * @param channel
	 */
	public void updateChannelFav(int vid, boolean isfav) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(VSTDBHelper.FAVORIT, isfav ? 1 : 0);
		int i = db.update(VSTDBHelper.TNAME_LIVE_RECODE, values,
				VSTDBHelper.VID + "=?", new String[] { vid + "" });
		if (i == 0) { // 更新失败则插入新纪录
			values.put(VSTDBHelper.VID, vid + "");
			db.insert(VSTDBHelper.TNAME_LIVE_RECODE, null, values);
		}
		db.close();
	}

	/**
	 * 获取 所有 分类 包括 自定义 包括 收藏
	 * 
	 * @return
	 */
	public ArrayList<LiveTypeInfo> getTypeList() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<LiveTypeInfo> list = null;
		Cursor c = db.query(typeName, null, null, null, null, null, null);
		if (c != null && c.getCount() > 0) {
			list = new ArrayList<LiveTypeInfo>();
			while (c.moveToNext()) {
				LiveTypeInfo type = new LiveTypeInfo();
				type.tid = c.getString(c.getColumnIndex(VSTDBHelper.TID));
				type.tname = c.getString(c.getColumnIndex(VSTDBHelper.TNAME));
				list.add(type);
			}
			if (MyApp.getChanState() == ConstantUtil.LIVE_LIST_ALL) {
				list.add(new LiveTypeInfo("-1", "网络自定义"));
			}
		}
		c.close();
		db.close();
		return list;
	}

	public LiveChannelInfo getChannelInfoByNum(int num) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		LiveChannelInfo info = null;
		Cursor c = db.query(viewName, null, VSTDBHelper.NUM + "=?",
				new String[] { num + "" }, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			info = new LiveChannelInfo();
			info.vid = c.getInt(c.getColumnIndex(VSTDBHelper.VID));
			info.num = c.getInt(c.getColumnIndex(VSTDBHelper.NUM));
			info.vname = c.getString(c.getColumnIndex(VSTDBHelper.VNAME));
			info.tid = c.getString(c.getColumnIndex(VSTDBHelper.TID))
					.split(",");
			info.epgid = c.getString(c.getColumnIndex(VSTDBHelper.EPGID));
			info.huibo = c.getString(c.getColumnIndex(VSTDBHelper.HUIBO));
			info.quality = c.getString(c.getColumnIndex(VSTDBHelper.QUALITY));
			info.pinyin = c.getString(c.getColumnIndex(VSTDBHelper.PINYIN));
			String sourceText = c.getString(c
					.getColumnIndex(VSTDBHelper.SOURCETEXT));
			if (sourceText.contains("%0A")) {
				info.liveSources = sourceText.split("%0A");
			} else {
				info.liveSources = sourceText.split("#");
			}
			info.lastSource = c
					.getInt(c.getColumnIndex(VSTDBHelper.LASTSOURCE));
			info.duration = c.getLong(c.getColumnIndex(VSTDBHelper.DURATION));
			info.favorite = (c.getInt(c.getColumnIndex(VSTDBHelper.FAVORIT)) != 0);
		}
		c.close();
		db.close();
		return info;
	}

	/**
	 * 更新 频道 的 播放时长
	 * 
	 * @param vid
	 * @param duration
	 */
	public void updatePlayDuration(int vid, long duration) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(VSTDBHelper.DURATION, duration);
		// /*
		// * 1.查询 已经播放的时间 2.时间增加 3.update记录
		// */
		// Cursor c = db.query(viewName, null,
		// VSTDBHelper.VID + "=?", new String[] { vid + "" },
		// VSTDBHelper.VID, null, null);
		// if (c.getCount() > 0) {
		// c.moveToFirst();
		// long oldDuration = c
		// .getLong(c.getColumnIndex(VSTDBHelper.DURATION));
		// duration = duration + oldDuration;
		// }
		// c.close();

		int i = db.update(VSTDBHelper.TNAME_LIVE_RECODE, values,
				VSTDBHelper.VID + "=?", new String[] { vid + "" });
		if (i == 0) {
			values.put(VSTDBHelper.VID, vid + "");
			db.insert(VSTDBHelper.TNAME_LIVE_RECODE, null, values);
		}
		db.close();
		// notifyObservers();
	}

	/**
	 * 更新 最后 播放源
	 * 
	 * @param sid
	 * @param currentTime
	 */

	public void updateLastSoure(int vid, int lastsource) {
		Log.i(TAG, "insertLastSoure>>>  vid = " + vid + ",url = " + lastsource);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(VSTDBHelper.LASTSOURCE, lastsource);
		int i = db.update(VSTDBHelper.TNAME_LIVE_RECODE, values,
				VSTDBHelper.VID + "=?", new String[] { vid + "" });
		if (i == 0) {
			values.put(VSTDBHelper.VID, vid + "");
			db.insert(VSTDBHelper.TNAME_LIVE_RECODE, null, values);
		}
		db.close();
	}

	/**
	 * 删除某个类型的 主要是删除 自定义的
	 * 
	 * @param tid
	 */
	public void deleteChannels(String tid) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int i = db.delete(tableName, VSTDBHelper.TID + "=?",
				new String[] { tid });
		db.close();
		Log.d(TAG, "delete recode : tid = " + tid + " , rows = " + i);
	}

	/**
	 * 插入 channel
	 * 
	 */
	public void insertChannel(LiveChannelInfo info) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(VSTDBHelper.VID, info.vid);
		values.put(VSTDBHelper.VNAME, info.vname);
		values.put(VSTDBHelper.NUM, info.num);
		values.put(VSTDBHelper.TID, info.getTidText(info.tid));
		values.put(VSTDBHelper.SOURCETEXT, info.getSourceText(info.liveSources));
		values.put(VSTDBHelper.EPGID, info.epgid);
		values.put(VSTDBHelper.HUIBO, info.huibo);
		values.put(VSTDBHelper.QUALITY, info.quality);
		values.put(VSTDBHelper.PINYIN, info.pinyin);
		db.insert(tableName, null, values);
		db.close();
	}

	/**
	 * 插入 多条记录 注意 使用 事务
	 * 
	 * @param channels
	 */
	public void insertChannels(ArrayList<LiveChannelInfo> channels) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			for (int i = 0; i < channels.size(); i++) {
				values.clear();
				LiveChannelInfo info = channels.get(i);
				values.put(VSTDBHelper.VID, info.vid);
				values.put(VSTDBHelper.VNAME, info.vname);
				values.put(VSTDBHelper.NUM, info.num);
				values.put(VSTDBHelper.TID, info.getTidText(info.tid));
				values.put(VSTDBHelper.SOURCETEXT,
						info.getSourceText(info.liveSources));
				values.put(VSTDBHelper.EPGID, info.epgid);
				values.put(VSTDBHelper.HUIBO, info.huibo);
				values.put(VSTDBHelper.QUALITY, info.quality);
				values.put(VSTDBHelper.PINYIN, info.pinyin);
				db.insert(tableName, null, values);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}

	/**
	 * 插入 多条记录 注意 使用 事务
	 * 
	 * @param types
	 */
	public void insertTypes(ArrayList<LiveTypeInfo> types) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			for (int i = 0; i < types.size(); i++) {
				values.clear();
				LiveTypeInfo type = types.get(i);
				values.put(VSTDBHelper.TID, type.tid);
				values.put(VSTDBHelper.TNAME, type.tname);
				db.insert(typeName, null, values);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}


	/**
	 * 插入直播数据
	 * 
	 * @param data
	 */
	public boolean initLiveDB(LiveDataInfo data) {
		boolean b = false;
		if (data == null) {
			return false;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			try {
				db.delete(typeName, null, null);
				db.delete(tableName, " tid not like ? ",
						new String[] { VSTDBHelper.CUSTOM_TID });
			} catch (Exception e) {
				e.printStackTrace();
				dbHelper.onCreate(db);
			}

			ContentValues values = new ContentValues();

			if (data.tvnum > 0) {
				ArrayList<Typeinfo> types = data.type;
				if (types != null && types.size() > 0) {
					for (int i = 0; i < types.size(); i++) {
						values.clear();
						LiveDataInfo.Typeinfo type = data.type.get(i);
						// System.out.println(type.toString());
						values.put(VSTDBHelper.TID, "(" + type.id + ")");
						values.put(VSTDBHelper.TNAME, type.name);
						db.insert(typeName, null, values);
					}
					values.clear();
					values.put(VSTDBHelper.TID, VSTDBHelper.CUSTOM_TID);
					values.put(VSTDBHelper.TNAME, VSTDBHelper.CUSTOM_TNAME);
					db.insert(typeName, null, values);
					values.clear();
					values.put(VSTDBHelper.TID, VSTDBHelper.FAVORITE_TID);
					values.put(VSTDBHelper.TNAME, VSTDBHelper.FAVORITE_TNAME);
					db.insert(typeName, null, values);
				}
				ArrayList<ChannelInfo> channels = data.live;
				if (channels != null && channels.size() > 0) {

					for (int i = 0; i < channels.size(); i++) {
						values.clear();
						LiveDataInfo.ChannelInfo info = channels.get(i);
						// System.out.println(info.toString());
						String live_itemid = "";
						if (info.itemid.contains(",")) {
							live_itemid = "(" + info.itemid.replace(",", "),(")
									+ ")";
						} else {
							live_itemid = "(" + info.itemid + ")";
						}
						values.put(VSTDBHelper.VID, info.id);
						values.put(VSTDBHelper.VNAME, info.name);
						values.put(VSTDBHelper.NUM, info.num);
						values.put(VSTDBHelper.TID, live_itemid);
						values.put(VSTDBHelper.SOURCETEXT, info.urllist);
						values.put(VSTDBHelper.EPGID, info.epgid);
						values.put(VSTDBHelper.HUIBO, info.huibo);
						values.put(VSTDBHelper.QUALITY, info.quality);
						values.put(VSTDBHelper.PINYIN, info.pinyin);
						db.insert(tableName, null, values);
					}
				}
			}
			db.setTransactionSuccessful();
			b = true;
		} finally {
			db.endTransaction();
		}
		db.close();
		return b;
	}
}
