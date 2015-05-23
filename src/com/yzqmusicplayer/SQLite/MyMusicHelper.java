package com.yzqmusicplayer.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyMusicHelper extends SQLiteOpenHelper {

	public MyMusicHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建两张表,一张是歌曲的表,一张是收藏的表
		db.execSQL("drop table  if  exists  music_info");
		db.execSQL("drop table  if  exists collection_info");
		StringBuffer sql = new StringBuffer();

		sql.append("create table music_info(").append("_id integer,")
				.append("_tractid integer,").append("_title text,")
				.append("_album text,").append("_artist text,")
				.append("_url text,").append("_duration integer,")
				.append("_size integer,").append("_disName text)");
		db.execSQL(sql.toString());
		StringBuffer sql2 = new StringBuffer();
		sql2.append("create table collection_info(").append("_id integer,")
				.append("_tractid integer,").append("_title text,")
				.append("_album text,").append("_artist text,")
				.append("_url text,").append("_duration integer,")
				.append("_size integer,").append("_disName text)");
		db.execSQL(sql2.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
