package com.yzqmusicplayer.activity;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.model.Music;
import com.yzqmusicplayer.model.MyLrc;
import com.yzqmusicplayer.service.PlayerService;
import com.yzqmusicplayer.util.Iinfo;
import com.yzqmusicplayer.util.LRCTextView;
import com.yzqmusicplayer.util.LrcUtil;
import com.yzqmusicplayer.util.Trans2PinYin;
import com.yzqmusicplayer.util.Utils;

public class PlayingActivity extends Activity {
	private SeekBar mSeekbar;
	private final int UPDATE_STARTTIME_AND_SEEKBOR = 1;
	private PlayerService mService = MainActivity.mService;
	private Music currentMusic = mService.record
			.get(mService.record.size() - 1);
	// 上面的back按钮，info按钮，我的收藏
	private ImageButton playing_imgbtn_back, playing_imgbtn_info,
			playing_imgbtn_collect;

	private ImageButton playing_imgbtn_pre, playing_imgbtn_play,
			playing_imgbtn_next, playing_imgbtn_rule;
	// 上面的歌曲，歌手，下面的歌曲播放时间，歌曲播放总时长
	private TextView playing_text_song, playing_text_singer,
			playing_text_starttime, playing_text_endtime;
	private LRCTextView playing_text_lrc;
	// 初始化前一首歌的位置
	public static int preMusic = 0;
	// 进度条的位置
	private int currentPosition;
	private Handler handler;
	private TimerTask timertask;
	// 广播获得改变的音乐
	private CurrentMusicReceiver currentReceiver;

	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置启动动画，同时在清单文件中设置activity中theme为
		// Translucent.NoTitleBar透明并且看不到app的name
		overridePendingTransition(R.anim.in_from_right, 0);
		setContentView(R.layout.activity_playing);
		initView();
		registercurrentReceiver();
		// 初始化SeekBar以及不断更新SeekBar
		initSeekBar();
		// 进度条用timertask来每隔一秒发一个消息
		startprogress();
		// 开始一个歌词的显示
		// startLrc();
	}

	/*
	 * private void startLrc() { new Thread(new Runnable() { int i = 0;
	 * List<Integer> mTimeList = LrcUtil.getTimes();
	 * 
	 * @Override public void run() { while (!mService.isPause()) {
	 * handler.post(new Runnable() {
	 * 
	 * @Override public void run() { playing_text_lrc.invalidate(); } }); try {
	 * Thread.sleep(mTimeList.get(i + 1) - mTimeList.get(i)); } catch
	 * (InterruptedException e) { } i++; } } }).start(); }
	 */

	@SuppressLint("NewApi")
	private void initView() {
		playing_imgbtn_back = (ImageButton) findViewById(R.id.playing_imgbtn_back);
		playing_imgbtn_info = (ImageButton) findViewById(R.id.playing_imgbtn_info);
		playing_imgbtn_collect = (ImageButton) findViewById(R.id.playing_imgbtn_collect);

		playing_text_song = (TextView) findViewById(R.id.playing_text_song);
		playing_text_singer = (TextView) findViewById(R.id.playing_text_singer);
		playing_text_starttime = (TextView) findViewById(R.id.playing_text_starttime);
		playing_text_endtime = (TextView) findViewById(R.id.playing_text_endtime);
		playing_text_lrc = (LRCTextView) findViewById(R.id.playing_textview_lrc);
		playing_imgbtn_pre = (ImageButton) findViewById(R.id.playing_imgbtn_pre);
		playing_imgbtn_play = (ImageButton) findViewById(R.id.playing_imgbtn_play);
		playing_imgbtn_next = (ImageButton) findViewById(R.id.playing_imgbtn_next);
		playing_imgbtn_rule = (ImageButton) findViewById(R.id.playing_imgbtn_rule);
		mSeekbar = (SeekBar) findViewById(R.id.seekBar_PlayProgress);
		SeekBarChangeListener scl = new SeekBarChangeListener();
		mSeekbar.setOnSeekBarChangeListener(scl);
		// 设置back和info，我的收藏的按钮监听事件
		TopViewListener tvl = new TopViewListener();
		playing_imgbtn_back.setOnClickListener(tvl);
		playing_imgbtn_info.setOnClickListener(tvl);
		playing_imgbtn_collect.setOnClickListener(tvl);
		playing_text_song.setText(currentMusic.getTitle());
		playing_text_singer.setText(currentMusic.getArtist());
		playing_text_endtime.setText(currentMusic.getStringDuration());
		BottomViewListener bvl = new BottomViewListener();
		playing_imgbtn_pre.setOnClickListener(bvl);
		playing_imgbtn_play.setOnClickListener(bvl);
		playing_imgbtn_next.setOnClickListener(bvl);
		playing_imgbtn_rule.setOnClickListener(bvl);
		// 初始化开始暂停按钮服务中播放器是暂停的状态的话，按钮为开始按钮
		if (mService.isPause()) {
			playing_imgbtn_play.setBackgroundResource(R.drawable.play);
		} else {
			playing_imgbtn_play.setBackgroundResource(R.drawable.pause);
		}
		// 通过数据库判断当前歌曲是否已经加入我的收藏
		if (Utils.getCollectMusicById(this, currentMusic) != 0) {
			playing_imgbtn_collect
					.setBackgroundResource(R.drawable.actioninfo_haveaddedfavorite);
		} else {
			playing_imgbtn_collect
					.setBackgroundResource(R.drawable.actioninfo_addfavorite);
		}

		// 判断sp里面存储的播放方式,然后显示对应的图标
		sp = getSharedPreferences("playing_info", MODE_PRIVATE);
		int currentrule = sp.getInt("playing_rule", Iinfo.PLAY_RULE_ORDER);
		switch (currentrule) {
		case Iinfo.PLAY_RULE_ORDER:
			playing_imgbtn_rule
					.setBackgroundResource(R.drawable.bt_widget_mode_order);
			break;
		case Iinfo.PLAY_RULE_SINGLE:
			playing_imgbtn_rule
					.setBackgroundResource(R.drawable.bt_widget_mode_singlecycle);
			break;
		case Iinfo.PLAY_RULE_RANDOM:
			playing_imgbtn_rule
					.setBackgroundResource(R.drawable.bt_widget_mode_shuffle);
			break;
		}
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case UPDATE_STARTTIME_AND_SEEKBOR:
					setStartTextAndSeekBarAndLRC();
					break;
				default:
					break;
				}
			};

		};

	}

	// 注册三个广播,改变控件的信息
	private void registercurrentReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.yzqmusicplayer.activity.changepauseToplayButton");
		filter.addAction("com.yzqmusicplayer.activity.changeplayTopauseButton");
		// 改变歌曲歌手
		filter.addAction("com.yzqmusicplayer.activity.changeSongAndSinger");
		currentReceiver = new CurrentMusicReceiver();
		registerReceiver(currentReceiver, filter);
	}

	private void initSeekBar() {
		mSeekbar.setMax(currentMusic.getDuration());
		// 设置当前进度条
		mSeekbar.setProgress(currentPosition = mService.getCurrentPosition());
		// 初始化歌词
		initLrcThread(currentPosition);
	}

	//根据当前歌曲的进度启一个子线程跑时间，一段时间刷新一个空间显示
	private void initLrcThread(int currentPosition) {
		LrcUtil lrcUtil = null;
		try {
			lrcUtil = new LrcUtil(getAssets().open(Trans2PinYin.trans2PinYin(currentMusic.getTitle())+".lrc"));
			playing_text_lrc.init(Trans2PinYin.trans2PinYin(currentMusic.getTitle())+".lrc");
		} catch (IOException e) {
			ToastInfo(R.string.notfind_lrc);
		}
		if(lrcUtil==null)
			return;
		final List<Integer> lrctime = lrcUtil.getTimes();
		int position = 0;
		for (int i = 0; i < lrctime.size(); i++) {
			if (currentPosition < lrctime.get(i)) {
				position = 0;
				break;
			} else if (currentPosition > lrctime.get(i)
					&& currentPosition < lrctime.get(i + 1)) {
				position = i;
				break;
			}
		}
		final int p = position;
		//找到对应位置的歌词
		playing_text_lrc.changeIndex(p);
		// 起一个子线程进行歌词显示
		new Thread() {
			int i = p;

			public void run() {
				while (!mService.isPause()) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							playing_text_lrc.invalidate();
						}
					});
					try {
						if (i == 0)
							Thread.sleep(lrctime.get(i));
						else {
							Thread.sleep(lrctime.get(i + 1) - lrctime.get(i));
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;
					if (i+1 >= lrctime.size())
						break;
				}
				;
			}
		}.start();
		
	}

	// 每隔一秒做一个任务
	private void startprogress() {
		Timer timer = new Timer();
		if (timertask != null) {
			timertask.cancel();
			timertask = null;
		}
		timertask = new TimerTask() {
			@Override
			public void run() {
				if (!mService.isPause()) {
					handler.sendEmptyMessage(UPDATE_STARTTIME_AND_SEEKBOR);
				} else {
					// 上面虽然暂停了，但是线程还在跑
					System.out.println("暂停了但是我还在跑" + Thread.currentThread());
				}
			}
		};
		// 每500毫秒执行一次,防止有歌词短,直接被跳过,即显示不出来
		timer.schedule(timertask, 0, Iinfo.PLAY_TIMERTASK_SEEKBARTIME);
	}

	private void endprogressTimer() {
		if (timertask != null) {
			timertask.cancel();
			timertask = null;
		}
	}

	private void setStartTextAndSeekBarAndLRC() {
		currentPosition += Iinfo.PLAY_TIMERTASK_SEEKBARTIME;
		mSeekbar.setProgress(currentPosition);
		playing_text_starttime.setText(setStartTimeByPosition());
		// 开始歌词同步
		// startLrc();
		// 超出了最大值，不再循环
		if (currentMusic.getDuration() <= currentPosition) {
			endprogressTimer();
		}
	}

	private String setStartTimeByPosition() {
		int time = currentPosition / 1000;
		int min = time / 60;
		int second = time - min * 60;
		String minStr = min < 10 ? ("0" + min) : (min + "");
		String sedStr = second < 10 ? ("0" + second) : (second + "");
		return minStr + ":" + sedStr;
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(0, R.anim.out_from_right);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(currentReceiver);
	}

	// 监听那个按钮移动的位置
	class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			currentPosition = progress;
			playing_text_starttime.setText(setStartTimeByPosition());

		}

		// 按下的时候暂停播放
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mService.playToPause();
		}

		// 放开按钮的时候seekto播放，待测试
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// 定位，注意里面的是毫秒
			System.out.println(currentPosition);
			mService.seekTo(currentPosition);
			mService.pauseToplay();
			handler.removeMessages(1);
			startprogress();
			// 开始播放
		}

	}

	class TopViewListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playing_imgbtn_back:
				Intent intent = new Intent();
				intent.setClass(PlayingActivity.this, MainActivity.class);
				startActivity(intent);
				overridePendingTransition(0, R.anim.out_from_right);
				finish();
				break;
			case R.id.playing_imgbtn_info:
				MainActivity.showDetailDialog(currentMusic,
						PlayingActivity.this);
				break;
			case R.id.playing_imgbtn_collect:
				if (Utils.getCollectMusicById(PlayingActivity.this,
						currentMusic) == 0) {
					ToastInfo(R.string.add_collect);
					Utils.insertCollection(PlayingActivity.this, currentMusic);
					playing_imgbtn_collect
							.setBackgroundResource(R.drawable.actioninfo_haveaddedfavorite);
				} else {
					ToastInfo(R.string.cancel_collect);
					playing_imgbtn_collect
							.setBackgroundResource(R.drawable.actioninfo_addfavorite);
					Utils.deleteCollectionById(PlayingActivity.this,
							currentMusic);
				}
				sendBroadcast(new Intent(
						"com.yzq.musicplayer.notifyCollectDataChanged"));
			}
		}
	}

	class BottomViewListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playing_imgbtn_pre:
				// 改变音乐如果有两首歌以上，放前面一首，如果只有一首歌，那么就只放当然的这一首
				if (preMusic > 0) {
					preMusic--;
				}
				// preMusic=(preMusic==0)?0:(preMusic-1);
				System.out.println(preMusic);
				mService.play(mService.record.get(preMusic));
				// 发送广播改变主界面的数值
				// Intent preIntent=new
				// Intent("com.yzqmusicplayer.activity.playPremusic");
				// preIntent.putExtra("premusic",
				// mService.record.get(preMusic));
				// sendBroadcast(preIntent);
				break;
			case R.id.playing_imgbtn_play:
				// 判断开始暂停，按钮的改变在服务中发送广播
				if (mService.isPause()) {
					mService.pauseToplay();
				} else {
					mService.playToPause();
				}
				break;
			case R.id.playing_imgbtn_next:
				// 播放下一首歌
				sendBroadcast(new Intent(
						"com.yzqmusicplayer.activity.playnextmusic"));
				break;
			// 弹出一个popwindow,进行操作,然后存储
			case R.id.playing_imgbtn_rule:
				View root = PlayingActivity.this.getLayoutInflater().inflate(
						R.layout.playing_rule, null);
				final PopupWindow window = new PopupWindow(root, 200, 200);
				window.setFocusable(true);// 设置焦点
				window.setOutsideTouchable(true);// 设置以外的焦点
				window.update();// 刷新
				window.setBackgroundDrawable(new BitmapDrawable());
				window.showAsDropDown(v);
				window.showAtLocation(findViewById(R.id.playing_imgbtn_rule),
						Gravity.LEFT, 20, 10);
				LinearLayout playing_rule_order = (LinearLayout) root
						.findViewById(R.id.playing_rule_order);
				LinearLayout playing_rule_random = (LinearLayout) root
						.findViewById(R.id.playing_rule_random);
				LinearLayout playing_rule_single = (LinearLayout) root
						.findViewById(R.id.playing_rule_single);
				playing_rule_order.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						window.dismiss();
						// 改变对应背景图片然后存储对应信息
						sp.edit().putInt("playing_rule", Iinfo.PLAY_RULE_ORDER)
								.commit();
						playing_imgbtn_rule
								.setBackgroundResource(R.drawable.bt_widget_mode_order);
					}
				});
				playing_rule_random.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						window.dismiss();
						sp.edit()
								.putInt("playing_rule", Iinfo.PLAY_RULE_RANDOM)
								.commit();
						playing_imgbtn_rule
								.setBackgroundResource(R.drawable.bt_widget_mode_shuffle);
					}
				});
				playing_rule_single.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						window.dismiss();
						sp.edit()
								.putInt("playing_rule", Iinfo.PLAY_RULE_SINGLE)
								.commit();
						playing_imgbtn_rule
								.setBackgroundResource(R.drawable.bt_widget_mode_singlecycle);
					}
				});
				break;
			}
		}

	}

	class CurrentMusicReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("com.yzqmusicplayer.activity.changeSongAndSinger".equals(intent
					.getAction())) {
				// 改变音乐
				currentMusic = mService.record.get(mService.record.size() - 1);
				playing_text_song.setText(currentMusic.getTitle());
				playing_text_singer.setText(currentMusic.getArtist());
				playing_text_endtime.setText(currentMusic.getStringDuration());
				endprogressTimer();
				initSeekBar();
				startprogress();
			}
			if ("com.yzqmusicplayer.activity.changepauseToplayButton"
					.equals(intent.getAction())) {
				playing_imgbtn_play.setBackgroundResource(R.drawable.pausebtn);
				initLrcThread(currentPosition);
			}
			if ("com.yzqmusicplayer.activity.changeplayTopauseButton"
					.equals(intent.getAction())) {
				playing_imgbtn_play.setBackgroundResource(R.drawable.playbtn);
			}
		}
	}

	private void ToastInfo(int info) {
		Toast.makeText(PlayingActivity.this, info, Toast.LENGTH_SHORT).show();
	}
}