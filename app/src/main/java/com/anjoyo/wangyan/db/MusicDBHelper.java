package com.anjoyo.wangyan.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.anjoyo.wangyan.constant.DbFinal;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.entity.MusicInfo;

/**
 * 数据库帮助类
 *
 */
public class MusicDBHelper extends SQLiteOpenHelper {

	private SQLiteDatabase db = this.getWritableDatabase();

	public MusicDBHelper(Context context) {
		super(context, DbFinal.DB_NAME, null, DbFinal.DB_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		// 建本地曲库表，专辑，歌手
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbFinal.TABLE_LOCALMUSIC
				+ " (" + DbFinal.LOCAL_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + DbFinal.LOCAL_TITLE
				+ " TEXT UNIQUE NOT NULL," + DbFinal.LOCAL_ARTIST + " TEXT,"
				+ DbFinal.LOCAL_ALBUM + " TEXT," + DbFinal.LOCAL_PATH
				+ " TEXT NOT NULL," + DbFinal.LOCAL_DURATION
				+ " LONG NOT NULL," + DbFinal.LOCAL_FILE_SIZE
				+ " LONG NOT NULL," + DbFinal.LOCAL_LRC_TITLE + " TEXT"
				+ DbFinal.LOCAL_LRC_PATH + " TEXT"
				+ DbFinal.LOCAL_ALBUM_IMG_TITLE + " TEXT"
				+ DbFinal.LOCAL_ALBUM_IMG_PATH + " TEXT" + ");");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbFinal.TABLE_FAVORITES
				+ " (" + DbFinal.FAVORITES_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbFinal.FAVORITES_LOCAL_ID + " INTEGER UNIQUE NOT NULL);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbFinal.TABLE_ARTIST + " ("
				+ DbFinal.ARTIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbFinal.ARTIST_LOCAL_ARTIST + " TEXT UNIQUE NOT NULL);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbFinal.TABLE_ALBUM + " ("
				+ DbFinal.ALBUM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbFinal.ALBUM_LOCAL_ALBUM + " TEXT UNIQUE NOT NULL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + DbFinal.TABLE_LOCALMUSIC);
		db.execSQL("DROP TABLE IF EXISTS " + DbFinal.TABLE_FAVORITES);
		db.execSQL("DROP TABLE IF EXISTS " + DbFinal.TABLE_ALBUM);
		db.execSQL("DROP TABLE IF EXISTS " + DbFinal.TABLE_ARTIST);
		onCreate(db);
	}

	/**
	 * 清空本地列表
	 */
	public void clearLocal() {

		db.execSQL("DROP TABLE IF EXISTS " + DbFinal.TABLE_LOCALMUSIC);
		onCreate(db);
	}

	/**
	 * 插入本地音乐列表
	 *
	 * @param music
	 *            实体类
	 * @return Long 插入行id
	 */
	public Long insertLocal(MusicInfo music) {
		ContentValues values = new ContentValues();
		values.put(DbFinal.LOCAL_TITLE, music.getTitle());
		values.put(DbFinal.LOCAL_ARTIST, music.getArtist());
		values.put(DbFinal.LOCAL_ALBUM, music.getAlbum());
		values.put(DbFinal.LOCAL_PATH, music.getPath());
		values.put(DbFinal.LOCAL_DURATION, music.getDuration());
		values.put(DbFinal.LOCAL_FILE_SIZE, music.getSize());
		values.put(DbFinal.LOCAL_LRC_TITLE, music.getLyric_file_name());
		Long i = db.insert(DbFinal.TABLE_LOCALMUSIC, null, values);
		return i;
	}

	/**
	 * 插入收藏列表
	 *
	 * @param music
	 *            实体类
	 * @return Long 插入行id
	 */
	public Long insertFav(MusicInfo music) {
		ContentValues values = new ContentValues();
		values.put(DbFinal.FAVORITES_LOCAL_ID, music.getId());
		Long i = db.insert(DbFinal.TABLE_FAVORITES, null, values);
		return i;
	}

	/**
	 * 插入歌手列表
	 *
	 * @param music
	 *            实体类
	 * @return Long 插入行id
	 */
	public Long insertArtist(MusicInfo music) {
		ContentValues values = new ContentValues();
		values.put(DbFinal.ARTIST_LOCAL_ARTIST, music.getArtist());
		Long i = db.insert(DbFinal.TABLE_ARTIST, null, values);
		return i;
	}

	/**
	 * 插入专辑列表
	 *
	 * @param music
	 *            实体类
	 * @return Long 插入行id
	 */
	public Long insertAlbum(MusicInfo music) {
		ContentValues values = new ContentValues();
		values.put(DbFinal.ALBUM_LOCAL_ALBUM, music.getAlbum());
		Long i = db.insert(DbFinal.TABLE_ALBUM, null, values);
		return i;
	}

	/**
	 * 查询本地音乐数据库
	 *
	 * @return Cursor 查询本地数据库返回
	 */
	public Cursor queryLocalByID() {
		Cursor cur = db.query(DbFinal.TABLE_LOCALMUSIC, null, null, null, null,
				null, DbFinal.LOCAL_ID + " asc");
		return cur;
	}

	/**
	 * 查询收藏数据库
	 *
	 * @return Cursor 查询收藏数据库返回
	 */
	public Cursor queryFavByID() {
		Cursor cur = db.query(DbFinal.TABLE_FAVORITES, null, null, null, null,
				null, DbFinal.LOCAL_ID + " asc");
		return cur;
	}

	/**
	 * 根据收藏查到的id查询本地数据库，得到歌曲信息
	 *
	 * @return Cursor 查询本地数据库返回
	 */
	public Cursor queryFavFromLocal() {
		Cursor idCursor = db.query(DbFinal.TABLE_FAVORITES, null, null, null,
				null, null, DbFinal.LOCAL_ID + " asc");
		String selection = DbFinal.LOCAL_ID + "=?";
		String selectionArgs[] = new String[idCursor.getCount()];
		if (idCursor.getCount() != 0) {
			idCursor.moveToFirst();
			Log.e(PlayerFinal.TAG, "查询到的总数" + idCursor.getCount() + "数组长度"
					+ selectionArgs.length);
			int i = 0;
			do {
				selectionArgs[i] = String.valueOf(idCursor.getInt(idCursor
						.getColumnIndex(DbFinal.FAVORITES_LOCAL_ID)));
				Log.e(PlayerFinal.TAG, i + "========" + selectionArgs[i]);
				i++;
				idCursor.moveToNext();
			} while (!idCursor.isAfterLast());
		}
		Cursor cur = db.query(DbFinal.TABLE_LOCALMUSIC, null, selection,
				selectionArgs, null, null, DbFinal.LOCAL_ID + " asc");
		return cur;
	}

	/**
	 * 查询歌手列表
	 *
	 * @return Cursor 专辑表返回的cursor
	 */
	public Cursor queryArtistByID() {
		Cursor cur = db.query(DbFinal.TABLE_ARTIST, null, null, null, null,
				null, DbFinal.ARTIST_ID + " asc");
		return cur;
	}

	/**
	 * 查询专辑列表
	 *
	 * @return Cursor 专辑表返回的cursor
	 */
	public Cursor queryAlbumByID() {
		Cursor cur = db.query(DbFinal.TABLE_ALBUM, null, null, null, null,
				null, DbFinal.ALBUM_ID + " asc");
		return cur;
	}

	/**
	 * 根据查询本地数据库得到的cursor得到歌曲信息集合
	 *
	 * @param curLocal
	 *            本地数据库查询到的cursor
	 * @return ArrayList<MusicInfo> 歌曲实体类集合
	 */
	public ArrayList<MusicInfo> getMusicListFromLocal(Cursor curLocal) {

		if (curLocal.getCount() != 0) {
			curLocal.moveToFirst();
			ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();
			do {
				MusicInfo music = new MusicInfo();
				music.setTitle(curLocal.getString(curLocal
						.getColumnIndex(DbFinal.LOCAL_TITLE)));
				music.setArtist(curLocal.getString(curLocal
						.getColumnIndex(DbFinal.LOCAL_ARTIST)));
				music.setAlbum(curLocal.getString(curLocal
						.getColumnIndex(DbFinal.LOCAL_ALBUM)));
				music.setPath(curLocal.getString(curLocal
						.getColumnIndex(DbFinal.LOCAL_PATH)));
				music.setDuration(curLocal.getLong(curLocal
						.getColumnIndex(DbFinal.LOCAL_DURATION)));
				music.setSize(curLocal.getLong(curLocal
						.getColumnIndex(DbFinal.LOCAL_FILE_SIZE)));
				music.setLyric_file_name(curLocal.getString(curLocal
						.getColumnIndex(DbFinal.LOCAL_LRC_TITLE)));
				musicList.add(music);
				curLocal.moveToNext();
			} while (!curLocal.isAfterLast());
			return musicList;
		}
		return null;

	}

	/**
	 * 删除本地数据库相应歌曲条目
	 *
	 * @param id
	 *            歌曲在本地数据库的id
	 * @return int 受影响的行数
	 */
	public int delLocal(String title) {
		int i = db.delete(DbFinal.TABLE_LOCALMUSIC, DbFinal.LOCAL_TITLE + "=?",
				new String[] { title });
		return i;
	}

	/**
	 * 删除收藏列表中相应歌曲条目
	 *
	 * @param id
	 *            歌曲在本地数据库的id
	 * @return int 受影响的行数
	 */
	public int delFav(int id) {
		int i = db.delete(DbFinal.TABLE_FAVORITES, DbFinal.FAVORITES_LOCAL_ID
				+ "=?", new String[] { id + "" });
		return i;
	}

	/**
	 * 根据artist查询本地数据库，得到歌曲信息
	 *
	 * @param artist
	 *            歌手名
	 * @return Cursor 查询数据库返回的cursor
	 */
	public Cursor queryLocalByArtist(String artist) {
		String selection = DbFinal.LOCAL_ARTIST + "=?";
		String selectionArgs[] = { artist };
		Cursor cur = db.query(DbFinal.TABLE_LOCALMUSIC, null, selection,
				selectionArgs, null, null, DbFinal.LOCAL_ID + " asc");
		return cur;
	}

	/**
	 * 根据album查询本地数据库，得到歌曲信息
	 *
	 * @param album
	 *            专辑名
	 * @return Cursor 查询数据库返回的cursor
	 */
	public Cursor queryLocalByAlbum(String album) {
		String selection = DbFinal.LOCAL_ALBUM + "=?";
		String selectionArgs[] = { album };
		Cursor cur = db.query(DbFinal.TABLE_LOCALMUSIC, null, selection,
				selectionArgs, null, null, DbFinal.LOCAL_ID + " asc");
		return cur;
	}

	@Override
	public synchronized void close() {

		if (db != null) {
			db.close();
		}
		super.close();
	}
}
