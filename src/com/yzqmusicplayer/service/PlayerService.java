package com.yzqmusicplayer.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.activity.PlayingActivity;
import com.yzqmusicplayer.entity.Music;
import com.yzqmusicplayer.util.Iinfo;
import com.yzqmusicplayer.util.Utils;

public class PlayerService extends Service {
	private MediaPlayer player;
	private Uri uri;
	public List<Music> record=new ArrayList<Music>();

	public class MyBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("onBind");
		return new MyBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public void onCreate() {
		System.out.println("onCreate");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (player != null) {
			player.stop();
			player.release();
		}
		super.onDestroy();
	}

	public void play(Music music) {
		if (player != null) {
			player.stop();
			player.reset();
			player.release();
		}
		//显示记录的音乐，最多为10个，超过10个就把第一个移除
		if(record.size()>=10)
		{
			record.remove(0);
		}
		//发送广播给正在播放界面的歌曲和歌手發送給兩個接受者
		Intent intent=new Intent("com.yzqmusicplayer.activity.changeSongAndSinger");
		intent.putExtra("currentMusic", music);
		sendBroadcast(intent);
		//开始的时候此时改变按钮
		sendBroadcast(new Intent("com.yzqmusicplayer.activity.changepauseToplayButton"));
		//这里的music对象没有重写hashcode和equals方法，每次都会添加
		//是否要写这个功能有待商榷
		record.add(music);
		uri = Uri.parse(music.getUrl());
		player = MediaPlayer.create(this, uri);
		player.start();
		// 判断结束的监听
		MusicCompletionListener mcl = new MusicCompletionListener();
		player.setOnCompletionListener(mcl);
	}

	public void playToPause() {
		player.pause();
		//改变所有的按钮
		sendBroadcast(new Intent("com.yzqmusicplayer.activity.changeplayTopauseButton"));
	}
	public void pauseToplay() {
		if (player != null) {
			player.start();
		} else {
			//第一次上来就点发送广播播放下一首歌曲
			Intent intent = new Intent();
			intent.setAction("com.yzqmusicplayer.activity.playnextmusic");
			sendBroadcast(intent);
		}
		//改变相关按钮以及TextView
		sendBroadcast(new Intent("com.yzqmusicplayer.activity.changepauseToplayButton"));
	}
	//移动改变歌曲进度
	public void seekTo(int seekposition)
	{
	    player.seekTo(seekposition);
	    player.start();
	    
	}
	class MusicCompletionListener implements OnCompletionListener {
		// 当一个音乐播放完成的时候，通过不同的播放方式发送不同的广播
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mp != null) {
				if (mp.isPlaying())
					mp.stop();
				mp.reset();
				mp.release();
				mp = null;
				player = null;
			}
			Intent intent = new Intent();
			//通过sp判断是什么播放方式发送不同的广播
			SharedPreferences sp=getSharedPreferences("playing_info", MODE_PRIVATE);
			int currentrule = sp.getInt("playing_rule", Iinfo.PLAY_RULE_ORDER);
			switch (currentrule) {
			case Iinfo.PLAY_RULE_ORDER:
				intent.setAction("com.yzqmusicplayer.activity.playnextmusic");
				break;
			case Iinfo.PLAY_RULE_SINGLE:
				intent.setAction("com.yzqmusicplayer.activity.playsinglemusic");
				break;
			case Iinfo.PLAY_RULE_RANDOM:
				intent.setAction("com.yzqmusicplayer.activity.playrandommusic");
				break;
			}
			sendBroadcast(intent);
		}
	}
	//判断播放的对象是不是存在
	public boolean isPlayerExist() {
		return null!=player;
	}
	//判断播放的对象是不是暂停
	public boolean isPause() {
		return null!=player&&(!player.isPlaying());
	}
	//获得正在播放的音乐的进度
	public int getCurrentPosition()
	{
		return null!=player?player.getCurrentPosition():0;
	}
}
