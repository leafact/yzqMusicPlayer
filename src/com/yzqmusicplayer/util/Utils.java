package com.yzqmusicplayer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.SQLite.MyMusicDB;
import com.yzqmusicplayer.activity.MainActivity;
import com.yzqmusicplayer.entity.Music;

public class Utils {
	public static Notification notify;

	// 遍历媒体数据库
	private static List<Music> ScannerMusic(Context context) {
		List<Music> listmusic = new ArrayList<Music>();
		if (getSdcardState()) {
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
					null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			// 遍历媒体数据库
			cursor.moveToFirst();
			while (cursor.isAfterLast() == false) {
				// 歌曲编号
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				// 歌曲ID
				int tractid = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				// 歌曲标题
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				// 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
				// 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				// 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				// 歌曲的总播放时长：MediaStore.Audio.Media.DURATION
				int duration = cursor
						.getInt(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				// 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
				Long size = cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				// 歌曲文件显示名字
				String disName = cursor
						.getString(cursor
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
				Music music = new Music();
				music.setId(id);
				music.setTractid(tractid);
				music.setTitle(title);
				music.setAlbum(album);
				music.setArtist(artist);
				music.setUrl(url);
				music.setDuration(duration);
				music.setSize(size);
				music.setDisName(disName);
				listmusic.add(music);
				cursor.moveToNext();
			}
			cursor.close();
		} else {
			Toast.makeText(context, "未发现本地音乐，请检查sdcard", Toast.LENGTH_SHORT)
					.show();
			ToastInfo(context, "未发现本地音乐=。=");
		}
		return listmusic;
	}

	// 判断sd卡是否存在，外部存储
	private static boolean getSdcardState() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	// 从数据库里面判断
	public static List<Music> JudgeMusic(Context context) {
		MyMusicDB db = MyMusicDB.getInstance(context);
		List<Music> musicdb = db.query();
		List<Music> musicprovider = Utils.ScannerMusic(context);
		int count = musicprovider.size() - musicdb.size();
		if (count != 0) {
			db.deleteAll();
			db.insert(musicprovider);
			if (count > 0)
				ToastInfo(context, "更新了" + count + "首歌");
			else {
				// 少了，说明被我删除了
				ToastInfo(context, "删除了" + -count + "首歌");
			}
		}
		return musicprovider;
	}

	// 从数据里中删除某一首歌曲
	public static int DeleteMusicById(Context context, int id) {
		// 删除provider中的数据
		int providerResult = context.getContentResolver().delete(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=?",
				new String[] { id + "" });
		return providerResult;
	}

	// ================我的收藏的处理============================
	// 获得我的收藏中的音乐列表
	public static List<Music> getCollectMusic(Context context) {
		MyMusicDB db = MyMusicDB.getInstance(context);
		List<Music> musicdb = db.queryCollect();
		return musicdb;
	}

	// 查询一个歌曲是否存在
	public static int getCollectMusicById(Context context, Music music) {
		MyMusicDB db = MyMusicDB.getInstance(context);
		int musicdb = db.queryCollectById(music);
		return musicdb;
	}
	public static long insertCollection(Context context, Music music) 
	{
		MyMusicDB db = MyMusicDB.getInstance(context);
		return  db.insertCollection(music);
	}
	public static long deleteCollectionById(Context context, Music music) 
	{
		MyMusicDB db = MyMusicDB.getInstance(context);
		return  db.deleteCollectionById(music);
	}

	// 显示通知栏
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showNotify(Context context) {
		// ..
		// 获取系统服务
		NotificationManager manager = (NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE);
		// 设置通知栏点击后跳转的MainActivity
		Intent intent = new Intent();
		intent.setClass(context, MainActivity.class);
		//intent.setAction("com.yzqmusicplayer.activity.MAIN");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 将intent包装在PendingIntent中传递给系统通知栏
		PendingIntent pintent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// 判断版本，不同版本的进行不同的notify
		// 2.3以上版本
		if (Build.VERSION.RELEASE.startsWith("2")) {
			notify = new Notification();
			notify.icon = R.drawable.ic_launcher1;
			notify.setLatestEventInfo(context, "yzq音乐播放器", "正在播放", pintent);
			manager.notify(0, notify);
		}// 3.0以上版本
		else {
			Notification.Builder builder = new Notification.Builder(context);
			builder.setSmallIcon(R.drawable.ic_launcher1);
			builder.setContentTitle("yzq音乐播放器");
			builder.setContentText("正在播放");
			builder.setContentIntent(pintent);
			// 可以设置为4.3以上版本的通知栏
			if (Build.VERSION.RELEASE.startsWith("4.3")) {
				notify = builder.build();
				// 可以优化，在通知消息界面布局
				notify.flags = Notification.FLAG_INSISTENT;
				manager.notify(0, notify);
			}// 4.3以下版本
			else {
				notify = builder.getNotification();
				manager.notify(0, notify);
			}
		}
		// 设置不可清除
		// notify.flags=Notification.FLAG_NO_CLEAR;
		// 设置通知铃声
		// notify.sound=Uri.parse("路径");
	}

	public static void updateLRC(String musicname) {

	}

	// 返回对应想要的0-range的数字
	public static int RandomNum(int range) {
		return new Random().nextInt(range);
	}

	private static void ToastInfo(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

}
