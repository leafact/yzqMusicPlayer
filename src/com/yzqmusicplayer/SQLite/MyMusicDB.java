package com.yzqmusicplayer.SQLite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yzqmusicplayer.model.Music;

public class MyMusicDB {
	private SQLiteDatabase sdb;

	private MyMusicDB(Context context) {
		MyMusicHelper my = new MyMusicHelper(context, "music", null, 1);
		sdb = my.getWritableDatabase();
	}

	private static MyMusicDB mdb;

	public static MyMusicDB getInstance(Context context) {
		if (mdb == null) {
			return new MyMusicDB(context);
		}
		return mdb;
	}

	// 对music_info数据库查询
	public List<Music> query() {
		Cursor cursor = sdb.query("music_info", null, null, null, null, null,
				null);
		return CursorToList(cursor);
	}

	public List<Music> queryCollect() {
		Cursor cursor = sdb.query("collection_info", null, null, null, null,
				null, null);
		return CursorToList(cursor);
	}

	public long insertCollection(Music music) {
		ContentValues values = MusicToContentValues(music);
		long insert = sdb.insert("collection_info", null, values);
		return insert;
	}

	public long deleteCollectionById(Music music) {
		long insert = sdb.delete("collection_info", "_id=?",
				new String[] { music.getId() + "" });
		return insert;
	}

	public int queryCollectById(Music music) {
		Cursor cursor = sdb.query("collection_info", null, "_id=?",
				new String[] { music.getId() + "" }, null, null, null);
		return cursor.getCount();
	}

	public long insert(List<Music> list) {
		// 遇到一个错误结束插入
		for (Music music : list) {
			ContentValues values = MusicToContentValues(music);
			long insert = sdb.insert("music_info", null, values);
			if (insert == -1)
				return insert;
		}
		return 1;
	}

	// 对于music_info全部删除
	public void deleteAll() {
		sdb.delete("music_info", null, null);
	}

	// public int deleteById(int id)
	// {
	// //返回值为删除的条数
	// return sdb.delete("music_info","_id=?", new String[]{id+""});
	// }

	public List<Music> CursorToList(Cursor c) {
		List<Music> list = new ArrayList<Music>();
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Music music = new Music();
				music.setId(c.getInt(c.getColumnIndex("_id")));
				music.setTractid(c.getInt(c.getColumnIndex("_tractid")));
				music.setTitle(c.getString(c.getColumnIndex("_title")));
				music.setAlbum(c.getString(c.getColumnIndex("_album")));
				music.setArtist(c.getString(c.getColumnIndex("_artist")));
				music.setUrl(c.getString(c.getColumnIndex("_url")));
				music.setDuration(c.getInt(c.getColumnIndex("_duration")));
				music.setSize(c.getInt(c.getColumnIndex("_size")));
				music.setDisName(c.getString(c.getColumnIndex("_disName")));
				list.add(music);
				c.moveToNext();
			}
		}
		return list;
	}

	public ContentValues MusicToContentValues(Music music) {
		ContentValues values = new ContentValues();
		values.put("_id", music.getId());
		values.put("_tractid", music.getTractid());
		values.put("_title", music.getTitle());
		values.put("_album", music.getAlbum());
		values.put("_artist", music.getArtist());
		values.put("_url", music.getUrl());
		values.put("_duration", music.getDuration());
		values.put("_size", music.getSize());
		values.put("_disName", music.getDisName());
		return values;
	}
}
