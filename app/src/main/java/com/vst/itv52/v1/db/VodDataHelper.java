package com.vst.itv52.v1.db;

import java.util.ArrayList;
import java.util.Observable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vst.itv52.v1.model.VodRecode;

public class VodDataHelper extends Observable {

	private VSTDBHelper dbHelper;
	private static VodDataHelper mInstance = null;

	public static final int FAV = 1;
	public static final int ZHUI = 2;
	public static final int RECODE = 3;

	private VodDataHelper(Context mContext) {
		super();
		dbHelper = VSTDBHelper.getInstance(mContext);
	}

	public static synchronized VodDataHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new VodDataHelper(context);
		}
		return mInstance;
	}

	// ==============================记录的增删改查===========================

	/**
	 * 插入记录
	 * 
	 * @param recode
	 */
	public void insertRecode(VodRecode recode) {
		VodRecode _recode = queryRecode(recode.id, recode.type);
		if (_recode != null) { // 记录存在 则 先删除
			deleteRecodes(recode.id, recode.type);
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(VSTDBHelper.VOD_FILM_ID, recode.id);
		values.put(VSTDBHelper.VOD_FILM_TITLE, recode.title);
		values.put(VSTDBHelper.VOD_IMAGE_URL, recode.imgUrl);
		values.put(VSTDBHelper.VOD_BANBEN, recode.banben);
		values.put(VSTDBHelper.VOD_UPDATE_TIME, System.currentTimeMillis());
		values.put(VSTDBHelper.VOD_RECODE_TYPE, recode.type);
		values.put(VSTDBHelper.SETINDEX, recode.setIndex);
		values.put(VSTDBHelper.SOURCEINDEX, recode.sourceIndex);
		values.put(VSTDBHelper.POSITION, recode.positon);
		// values.put(VSTDBHelper.VOD_FAV, recode.isfav);
		db.insert(VSTDBHelper.VOD_TABLE, null, values);
		db.close();
		Log.d("", "insert recode ======================");
		setChanged();
		notifyObservers();
	}

	/**
	 * 删除记录
	 * 
	 * @param id
	 * @param type
	 */
	public void deleteRecodes(int id, int type) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(VSTDBHelper.VOD_TABLE, VSTDBHelper.VOD_FILM_ID + " = ? and "
				+ VSTDBHelper.VOD_RECODE_TYPE + " = ? ", new String[] {
				id + "", type + "" });
		db.close();
		setChanged();
		notifyObservers();
	}

	/**
	 * 删除 记录
	 * 
	 * @param type
	 */
	public void deleteRecodes(int type) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete(VSTDBHelper.VOD_TABLE, VSTDBHelper.VOD_RECODE_TYPE + "=? ",
				new String[] { type + "" });
		db.close();
		setChanged();
		notifyObservers();
	}

	/**
	 * 查询记录
	 * 
	 * @param id
	 *            film_id
	 * @param type
	 *            记录类型
	 * @return
	 */
	public VodRecode queryRecode(int id, int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VSTDBHelper.VOD_TABLE, null,
				VSTDBHelper.VOD_FILM_ID + "=? and "
						+ VSTDBHelper.VOD_RECODE_TYPE + "=? ", new String[] {
						id + "", type + "" }, null, null,
				VSTDBHelper.VOD_UPDATE_TIME + " desc ");
		VodRecode recode = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			recode = new VodRecode();
			recode.id = c.getInt(c.getColumnIndex(VSTDBHelper.VOD_FILM_ID));
			recode.title = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_FILM_TITLE));
			recode.banben = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_BANBEN));
			recode.imgUrl = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_IMAGE_URL));
			recode.type = c.getInt(c
					.getColumnIndex(VSTDBHelper.VOD_RECODE_TYPE));
			recode.lastTime = c.getLong(c
					.getColumnIndex(VSTDBHelper.VOD_UPDATE_TIME));
			recode.sourceIndex = c.getInt(c
					.getColumnIndex(VSTDBHelper.SOURCEINDEX));
			recode.setIndex = c.getInt(c.getColumnIndex(VSTDBHelper.SETINDEX));
			recode.positon = c.getInt(c.getColumnIndex(VSTDBHelper.POSITION));
		}
		c.close();
		db.close();
		return recode;
	}

	/**
	 * 查询记录
	 * 
	 * @param type
	 *            记录类型
	 * @return
	 */
	public ArrayList<VodRecode> queryRecodes(int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VSTDBHelper.VOD_TABLE, null,
				VSTDBHelper.VOD_RECODE_TYPE + "=? ",
				new String[] { type + "" }, null, null,
				VSTDBHelper.VOD_UPDATE_TIME + " desc ");
		ArrayList<VodRecode> recodes = new ArrayList<VodRecode>();
		if (c.getCount() > 0) {
//			recodes = new ArrayList<VodRecode>();
			while (c.moveToNext()) {
				VodRecode recode = new VodRecode();
				recode.id = c.getInt(c.getColumnIndex(VSTDBHelper.VOD_FILM_ID));
				recode.title = c.getString(c
						.getColumnIndex(VSTDBHelper.VOD_FILM_TITLE));
				recode.banben = c.getString(c
						.getColumnIndex(VSTDBHelper.VOD_BANBEN));
				recode.imgUrl = c.getString(c
						.getColumnIndex(VSTDBHelper.VOD_IMAGE_URL));
				recode.type = c.getInt(c
						.getColumnIndex(VSTDBHelper.VOD_RECODE_TYPE));
				recode.lastTime = c.getLong(c
						.getColumnIndex(VSTDBHelper.VOD_UPDATE_TIME));
				recode.sourceIndex = c.getInt(c
						.getColumnIndex(VSTDBHelper.SOURCEINDEX));
				recode.setIndex = c.getInt(c
						.getColumnIndex(VSTDBHelper.SETINDEX));
				recode.positon = c.getInt(c
						.getColumnIndex(VSTDBHelper.POSITION));
				recodes.add(recode);
			}
		}
		c.close();
		db.close();
		return recodes;
	}

	/**
	 * 查询记录条数
	 * 
	 * @param type
	 * @return
	 */
	public int queryRecodeCount(int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VSTDBHelper.VOD_TABLE, null,
				VSTDBHelper.VOD_RECODE_TYPE + "=? ",
				new String[] { type + "" }, null, null,
				VSTDBHelper.VOD_UPDATE_TIME + " desc ");
		int count = c.getCount();
		c.close();
		db.close();
		return count;
	}

	/**
	 * 查询 最近记录
	 * 
	 * @param type
	 * @return
	 */
	public VodRecode queryLastRecode(int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VSTDBHelper.VOD_TABLE, null,
				VSTDBHelper.VOD_RECODE_TYPE + "=? ",
				new String[] { type + "" }, null, null,
				VSTDBHelper.VOD_UPDATE_TIME + " desc ");
		VodRecode recode = null;
		if (c.getCount() > 0) {
			c.moveToFirst();
			recode = new VodRecode();
			recode.id = c.getInt(c.getColumnIndex(VSTDBHelper.VOD_FILM_ID));
			recode.title = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_FILM_TITLE));
			recode.banben = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_BANBEN));
			recode.imgUrl = c.getString(c
					.getColumnIndex(VSTDBHelper.VOD_IMAGE_URL));
			recode.type = c.getInt(c
					.getColumnIndex(VSTDBHelper.VOD_RECODE_TYPE));
			recode.lastTime = c.getLong(c
					.getColumnIndex(VSTDBHelper.VOD_UPDATE_TIME));
			recode.sourceIndex = c.getInt(c
					.getColumnIndex(VSTDBHelper.SOURCEINDEX));
			recode.setIndex = c.getInt(c.getColumnIndex(VSTDBHelper.SETINDEX));
			recode.positon = c.getInt(c.getColumnIndex(VSTDBHelper.POSITION));

		}
		c.close();
		db.close();
		return recode;
	}

	public boolean queryHasRecode(int id, int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(VSTDBHelper.VOD_TABLE, null,
				VSTDBHelper.VOD_RECODE_TYPE + "=? and "
						+ VSTDBHelper.VOD_FILM_ID + "=?", new String[] {
						type + "", id + "" }, null, null,
				VSTDBHelper.VOD_UPDATE_TIME + " desc ");
		boolean hasRecode = false;
		if (c.getCount() > 0) {
			hasRecode = true;
		}
		c.close();
		db.close();
		return hasRecode;
	}

}
