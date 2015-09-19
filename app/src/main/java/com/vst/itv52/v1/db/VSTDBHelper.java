package com.vst.itv52.v1.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VSTDBHelper extends SQLiteOpenHelper {

	private static final String TAG = "VSTDBHelper";
	public static final String DBNAME = "VST.db3";
	public static final int VERSION = 12;
	private static VSTDBHelper mInstance = null;
	private Context context;

	// ==============================live_channel_info===========================
	public static final String TNAME_CHANNEL_INFO = "channel_info";// 本地的直播列表
	public static final String TNAME_CHANNEL_INFO_NET = "channel_info_net";// 网络的直播列表
	public static final String TNAME_TYPE_INFO = "type_info";
	public static final String TNAME_TYPE_INFO_NET = "type_info_net";
	public static final String TNAME_LIVE_RECODE = "live_recode";
	public static final String VNAME_CHANNEL_INFO = "channel_info_view";
	public static final String VNAME_CHANNEL_INFO_NET = "channel_info_net_view";

	public static final String VID = "vid";
	public static final String VNAME = "vname";
	public static final String NUM = "num";
	public static final String EPGID = "epgid";
	public static final String AREA = "area";
	public static final String QUALITY = "quality";
	public static final String HUIBO = "huibo";
	public static final String PINYIN = "pinyin";
	public static final String TID = "tid";
	public static final String TNAME = "tname";
	public static final String FAVORIT = "favorit";
	public static final String DURATION = "duration";
	public static final String LASTSOURCE = "lastsource";
	public static final String SOURCETEXT = "sourcetext";
	public static final String UPDATETIME = "updatetime";

	public static final String FAVORITE_TID = "favorite_tid";
	public static final String CUSTOM_TID = "custom_tid";
	public static final String FAVORITE_TNAME = "我的收藏";
	public static final String CUSTOM_TNAME = "自定义";

	private static final String CREATE_CHANNEL_INFO = "CREATE TABLE IF NOT EXISTS "
			+ TNAME_CHANNEL_INFO
			+ "(  "
			+ VID
			+ " integer NOT NULL,  "
			+ NUM
			+ " integer ,"
			+ VNAME
			+ " text NOT　NULL ,"
			+ TID
			+ " text , "
			+ SOURCETEXT
			+ " text , "
			+ EPGID
			+ " text , "
			+ HUIBO
			+ " text , "
			+ QUALITY + " text , " + PINYIN + " text ) ";
	private static final String CREATE_CHANNEL_INFO_NET = "CREATE TABLE IF NOT EXISTS "
			+ TNAME_CHANNEL_INFO_NET
			+ "(  "
			+ VID
			+ " integer NOT NULL,  "
			+ NUM
			+ " integer ,"
			+ VNAME
			+ " text NOT　NULL ,"
			+ TID
			+ " text , "
			+ SOURCETEXT
			+ " text , "
			+ EPGID
			+ " text , "
			+ HUIBO
			+ " text , " + QUALITY + " text , " + PINYIN + " text ) ";

	private static final String DROP_CHANNEL_INFO = "DROP TABLE IF EXISTS "
			+ TNAME_CHANNEL_INFO;

	private static final String DROP_CHANNEL_INFO_NET = "DROP TABLE IF EXISTS "
			+ TNAME_CHANNEL_INFO_NET;

	private static final String CREATE_LIVE_RECODE = "CREATE TABLE IF NOT EXISTS "
			+ TNAME_LIVE_RECODE
			+ "( "
			+ VID
			+ " integer NOT NULL, "
			+ DURATION
			+ " integer default 0 ,"
			+ LASTSOURCE
			+ "  integer default 0 ,"
			+ FAVORIT + " integer DEFAULT 0 ) ";
	private static final String DROP_LIVE_RECODE = "DROP TABLE IF EXISTS "
			+ TNAME_LIVE_RECODE;

	private static final String CREATE_VIEW_CHANNEL_INFO = "CREATE VIEW IF NOT EXISTS "
			+ VNAME_CHANNEL_INFO
			+ " AS select * from "
			+ VSTDBHelper.TNAME_CHANNEL_INFO
			+ " LEFT JOIN  "
			+ VSTDBHelper.TNAME_LIVE_RECODE
			+ " ON channel_info.vid=live_recode.vid";

	private static final String CREATE_VIEW_CHANNEL_INFO_NET = "CREATE VIEW IF NOT EXISTS "
			+ VNAME_CHANNEL_INFO_NET
			+ " AS select * from "
			+ VSTDBHelper.TNAME_CHANNEL_INFO_NET
			+ " LEFT JOIN  "
			+ VSTDBHelper.TNAME_LIVE_RECODE
			+ " ON channel_info_net.vid=live_recode.vid";

	private static final String DROP_VIEW_CHANNEL_INFO = "DROP VIEW IF EXISTS "
			+ VNAME_CHANNEL_INFO;

	private static final String DROP_VIEW_CHANNEL_INFO_NET = "DROP VIEW IF EXISTS "
			+ VNAME_CHANNEL_INFO_NET;

	private static final String CREATE_TYPE_INFO = "CREATE TABLE IF NOT EXISTS "
			+ TNAME_TYPE_INFO
			+ " ( "
			+ TID
			+ " text NOT NULL , "
			+ TNAME
			+ " text );";
	private static final String CREATE_TYPE_INFO_NET = "CREATE TABLE IF NOT EXISTS "
			+ TNAME_TYPE_INFO_NET
			+ " ( "
			+ TID
			+ " text NOT NULL , "
			+ TNAME
			+ " text );";

	private static final String DROP_TYPE_INFO = "DROP TABLE IF EXISTS "
			+ TNAME_TYPE_INFO;
	private static final String DROP_TYPE_INFO_NET = "DROP TABLE IF EXISTS "
			+ TNAME_TYPE_INFO_NET;

	// ==============================live_channel_info===========================

	// ==============================vod_info===========================
	public static final String VOD_TABLE = "vod_info";
	public static final String VOD_FILM_ID = "id";
	public static final String VOD_FILM_TITLE = "title";
	public static final String VOD_BANBEN = "banben";
	public static final String VOD_IMAGE_URL = "image";
	public static final String VOD_UPDATE_TIME = "updatetime";
	public static final String SETINDEX = "setIndex";
	public static final String SOURCEINDEX = "sourceIndex";
	public static final String POSITION = "position";
	// public static final String VOD_FAV = "fav";
	public static final String VOD_RECODE_TYPE = "type";
	public static final String VOD_QXD = "qxd";
	public static final String VOD_SOURCE_SETS = "source_sets";

	private static final String CREATE_VOD_TABLE = "create table if not exists "
			+ VOD_TABLE
			+ " ( "
			+ VOD_FILM_ID
			+ " Integer not null ,"
			+ VOD_FILM_TITLE
			+ " text  not null ,"
			+ VOD_BANBEN
			+ " text not null ,"
			+ VOD_IMAGE_URL
			+ " text not null ,"
			+ VOD_RECODE_TYPE
			+ " Integer not null ,"
			+ VOD_UPDATE_TIME
			+ " long not null , "
			+ SOURCEINDEX
			+ " Integer  ,"
			+ SETINDEX
			+ " Integer ,"
			+ POSITION
			+ " Integer ,"
			+ VOD_QXD
			+ " text,"
			+ VOD_SOURCE_SETS + " Integer ) ";

	private static final String DROP_VOD_TABLE = "DROP TABLE IF EXISTS "
			+ VOD_TABLE;

	// ==============================vod_info===========================

	private VSTDBHelper(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	public static synchronized VSTDBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VSTDBHelper(context);
		}
		return mInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_VOD_TABLE);
		db.execSQL(CREATE_CHANNEL_INFO);
		db.execSQL(CREATE_CHANNEL_INFO_NET);
		db.execSQL(CREATE_TYPE_INFO);
		db.execSQL(CREATE_TYPE_INFO_NET);
		db.execSQL(CREATE_LIVE_RECODE);
		db.execSQL(CREATE_VIEW_CHANNEL_INFO);
		db.execSQL(CREATE_VIEW_CHANNEL_INFO_NET);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_VOD_TABLE);
		db.execSQL(DROP_CHANNEL_INFO);
		db.execSQL(DROP_CHANNEL_INFO_NET);
		db.execSQL(DROP_TYPE_INFO);
		db.execSQL(DROP_TYPE_INFO_NET);
		db.execSQL(DROP_LIVE_RECODE);
		db.execSQL(DROP_VIEW_CHANNEL_INFO);
		db.execSQL(DROP_VIEW_CHANNEL_INFO_NET);
		onCreate(db);
	}

}
