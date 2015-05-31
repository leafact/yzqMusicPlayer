package com.yzqmusicplayer.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DialogRecognitionListener;
import com.example.yzqmusicplayer.R;
import com.yzqmusicplayer.Logic.MyLocalMusicListViewAdapter;
import com.yzqmusicplayer.Logic.MyMusicQueueListViewAdapter;
import com.yzqmusicplayer.Logic.MyViewPagerAdapter;
import com.yzqmusicplayer.entity.Music;
import com.yzqmusicplayer.service.PlayerService;
import com.yzqmusicplayer.service.PlayerService.MyBinder;
import com.yzqmusicplayer.util.Iinfo;
import com.yzqmusicplayer.util.SlidingMenu;
import com.yzqmusicplayer.util.Utils;

public class MainActivity extends Activity {

	private ViewPager viewPager;
	private View viewPager1, viewPager2, viewPager3;
	// 显示顶部的三个
	private TextView text_MainLocal, text_MainOnline, text_MainQueue;
	// 显示底部播放器的歌曲名和歌手名
	private TextView text_mainBottomsong, text_mainBottomsinger;
	private ImageButton img_MusicPlaying, img_MusicNext;
	// 本地音樂中正在播放的音樂的位置
	public int musicPosition;
	// 游标的动画
	private ImageView cursor;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	// 主界面底部的播放器
	private LinearLayout mainBottom;
	// 本地音乐的数据
	private List<Music> localMusicListData;
	// 收藏列表
	private List<Music> collectMusicListData;
	// 播放列表的数据
	private List<Music> musicQueueListData;
	// 本地音乐盒播放列表的音乐的ListView
	private ListView localMusicListView, collcectMusicListView,
			musicQueueListView;
	// 本地音乐的适配器
	private MyLocalMusicListViewAdapter localMusicListViewAdapter,
			collcectMusicListViewAdapter;
	// 播放列表的适配器
	private MyMusicQueueListViewAdapter musicQueueListViewAdapter;
	// 广播改变音乐
	private MusicReceiver musicReceiver;
	//判断来电去电前播放器的状态
	private boolean isPlayBeforePhoneCall = false;

	public static PlayerService mService;
	private ServiceConnection conn;
	// context menu中的点击事件对应的ID
	private static final int MENU_ID_DETAIL = 0x113;
	private static final int MENU_ID_ADD_MY_COLLECT = 0x111;
	private static final int MENU_ID_DELETE = 0x112;
	private Music mSelectedMusic;
	private SlidingMenu left_Menu;
	private ImageView background_img;

	private TextView searchText;
	// 语音识别对应的dialog
	private BaiduASRDigitalDialog mASRDigitaldialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, LoadingActivity.class));
		setContentView(R.layout.activity_main);
		init();
		Utils.showNotify(this);
	}

	private void init() {
		initImageCursor();
		initMenu();
		initViewPager();
		initView();
		initService();
		initContextMenu();
		registerReceiver();
	}

	private void initContextMenu() {
		// context设置对应的listview
		this.registerForContextMenu(localMusicListView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		mSelectedMusic = localMusicListData
				.get(((AdapterContextMenuInfo) menuInfo).position);
		menu.add(Menu.NONE, MENU_ID_DETAIL, Menu.NONE, R.string.menu_detail)
				.setEnabled(true);
		if (Utils.getCollectMusicById(this, mSelectedMusic) == 0) {
			menu.add(Menu.NONE, MENU_ID_ADD_MY_COLLECT, Menu.NONE,
					R.string.menu_add).setEnabled(true);
		}
		menu.add(Menu.NONE, MENU_ID_DELETE, Menu.NONE, R.string.menu_del)
				.setEnabled(true);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// 当contextMenu中的元素被选中的时候
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_DETAIL:
			showDetailDialog(mSelectedMusic, this);
			break;
		case MENU_ID_DELETE:
			showDeleteDialog(mSelectedMusic, this);
			break;
		case MENU_ID_ADD_MY_COLLECT:
			Utils.insertCollection(this, mSelectedMusic);
			collectMusicListData.clear();
			collectMusicListData.addAll(Utils.getCollectMusic(this));
			collcectMusicListViewAdapter.notifyDataSetChanged();
			break;
		}
		return super.onContextItemSelected(item);
	}

	// 初始化cursor动画
	private void initImageCursor() {
		cursor = (ImageView) findViewById(R.id.imageCursor);
		bmpW = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_cursor).getWidth();// 获取图片宽度
		// 获取屏幕分辨率
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置

	}

	private void initMenu() {
		left_Menu = (SlidingMenu) findViewById(R.id.id_menu);
		background_img = (ImageView) findViewById(R.id.background_img);
		left_Menu.setBackground(background_img);
		Button voice = (Button) findViewById(R.id.yuyinshibie_btn);
		LeftMenuListener lml = new LeftMenuListener();
		voice.setOnClickListener(lml);

	}

	// 初始化ViewPager
	private void initViewPager() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager1 = LayoutInflater.from(this).inflate(R.layout.viewpager1,
				null);
		viewPager2 = LayoutInflater.from(this).inflate(R.layout.viewpager2,
				null);
		viewPager3 = LayoutInflater.from(this).inflate(R.layout.viewpager3,
				null);
		List<View> pagerList = new ArrayList<View>();
		searchText = (TextView) viewPager1.findViewById(R.id.localsearchtext);
		SearchTextWatcher stw = new SearchTextWatcher();
		searchText.addTextChangedListener(stw);
		pagerList.add(viewPager1);
		pagerList.add(viewPager2);
		pagerList.add(viewPager3);
		// 为Viewpager添加适配器/
		MyViewPagerAdapter pagerAdapter = new MyViewPagerAdapter(pagerList);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量

			int two = one * 2;// 页卡1 -> 页卡3 偏移量

			@Override
			public void onPageSelected(int arg0) {
				Animation animation = null;
				switch (arg0) {
				case 0:
					text_MainLocal.setBackgroundResource(R.color.selectedcolor);
					text_MainOnline.setBackgroundResource(R.color.defaultcolor);
					text_MainQueue.setBackgroundResource(R.color.defaultcolor);
					if (currIndex == 1) {
						animation = new TranslateAnimation(one, 0, 0, 0);
					} else if (currIndex == 2) {
						animation = new TranslateAnimation(two, 0, 0, 0);
					}
					break;
				case 1:
					text_MainLocal.setBackgroundResource(R.color.defaultcolor);
					text_MainOnline
							.setBackgroundResource(R.color.selectedcolor);
					text_MainQueue.setBackgroundResource(R.color.defaultcolor);

					if (currIndex == 0) {
						animation = new TranslateAnimation(0, one, 0, 0);
					} else if (currIndex == 2) {
						animation = new TranslateAnimation(two, one, 0, 0);
					}
					break;
				case 2:
					text_MainLocal.setBackgroundResource(R.color.defaultcolor);
					text_MainOnline.setBackgroundResource(R.color.defaultcolor);
					text_MainQueue.setBackgroundResource(R.color.selectedcolor);
					if (currIndex == 0) {
						animation = new TranslateAnimation(0, two, 0, 0);
					} else if (currIndex == 1) {
						animation = new TranslateAnimation(one, two, 0, 0);
					}
					break;
				}

				currIndex = arg0;
				// 每次都要刷新一下left_menu中的page的数值,方便判断是否时间要向下传递
				left_Menu.notifyPage(currIndex);
				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(300);
				cursor.startAnimation(animation);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	// 初始化
	private void initView() {
		text_MainLocal = (TextView) findViewById(R.id.img_mainLocal);
		text_MainOnline = (TextView) findViewById(R.id.img_mainOnline);
		text_MainQueue = (TextView) findViewById(R.id.img_mainQueue);
		mainBottom = (LinearLayout) findViewById(R.id.mainBottom);
		MainListener sl = new MainListener();
		// 上面三个的监听
		text_MainLocal.setOnClickListener(sl);
		text_MainOnline.setOnClickListener(sl);
		text_MainQueue.setOnClickListener(sl);
		// 播放器整个的监听
		mainBottom.setOnClickListener(sl);
		// 图片的初始化
		text_mainBottomsong = (TextView) findViewById(R.id.text_mainBottomsong);
		text_mainBottomsinger = (TextView) findViewById(R.id.text_mainBottomsinger);
		img_MusicPlaying = (ImageButton) findViewById(R.id.img_musicplaying);
		// img_MusicPlaying.setTag(R.drawable.play);
		img_MusicNext = (ImageButton) findViewById(R.id.img_musicnext);
		MusicListener ml = new MusicListener();
		img_MusicPlaying.setOnClickListener(ml);
		img_MusicNext.setOnClickListener(ml);
		// updatalocalMusicListData();
		// 自定义适配器并且本地音乐加载
		localMusicListView = (ListView) viewPager1
				.findViewById(R.id.localmusiclistView);
		localMusicListData = Utils.JudgeMusic(this);
		localMusicListViewAdapter = new MyLocalMusicListViewAdapter(this,
				localMusicListData);
		localMusicListView.setAdapter(localMusicListViewAdapter);
		// 设置监听事件
		localMusicListViewListener ll = new localMusicListViewListener();
		localMusicListView.setOnItemClickListener(ll);
		// 我的收藏的音乐的初始化
		collcectMusicListView = (ListView) viewPager2
				.findViewById(R.id.collectmusiclistView);
		collectMusicListData = Utils.getCollectMusic(this);
		collcectMusicListViewAdapter = new MyLocalMusicListViewAdapter(this,
				collectMusicListData);
		collcectMusicListView.setAdapter(collcectMusicListViewAdapter);
		CollectMusicListViewListener cm = new CollectMusicListViewListener();
		collcectMusicListView.setOnItemClickListener(cm);
		// 自定义适配器并且播放队列加载
		musicQueueListData = new ArrayList<Music>();
		musicQueueListView = (ListView) viewPager3
				.findViewById(R.id.musicqueuelistView);
		musicQueueListViewAdapter = new MyMusicQueueListViewAdapter(this,
				musicQueueListData);
		musicQueueListView.setAdapter(musicQueueListViewAdapter);
		MusicQueueListViewListener mq = new MusicQueueListViewListener();
		musicQueueListView.setOnItemClickListener(mq);
	}

	private void initService() {
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = ((MyBinder) service).getService();
			}
		};
		Intent intent = new Intent("com.yzqmusicplayer.musicservice");
		// 如果没有启动service,绑定过程中会自动启动service,而却同一个service可以绑定多个服务连接
		MainActivity.this.bindService(intent, conn, BIND_AUTO_CREATE);
	}

	private void updatalocalMusicListData() {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 点击back键，后台运行，不退出;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		}
		// 其他情况下调用父类的方法
		return super.onKeyDown(keyCode, event);
	}

	// 写个ToastInfo方便下次 调用
	private void ToastInfo(String info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}

	// 动态注册广播
	private void registerReceiver() {
		musicReceiver = new MusicReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.yzqmusicplayer.activity.addmusic");
		filter.addAction("com.yzqmusicplayer.activity.playnextmusic");
		filter.addAction("com.yzqmusicplayer.activity.playsinglemusic");
		filter.addAction("com.yzqmusicplayer.activity.playrandommusic");
		filter.addAction("com.yzqmusicplayer.activity.changepauseToplayButton");
		filter.addAction("com.yzqmusicplayer.activity.changeplayTopauseButton");
		// 改变歌曲和歌手
		filter.addAction("com.yzqmusicplayer.activity.changeSongAndSinger");
		// 当电话状态改变的时候
		filter.addAction("android.intent.action.PHONE_STATE");
		// 当打电话的时候，去电也会发出android.intent.action.PHONE_STATE广播，这里不再特别加入去电广播。
		// filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		// 我的收藏
		filter.addAction("com.yzq.musicplayer.notifyCollectDataChanged");
		registerReceiver(musicReceiver, filter);
	}

	// 解注册广播
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(musicReceiver);
		unbindService(conn);
		if (mASRDigitaldialog != null) {
			mASRDigitaldialog.dismiss();
		}
	}

	// 用来给查看详细信息调用的方法
	public static void showDetailDialog(Music music, Context context) {
		if (music == null) {
			return;
		}
		StringBuffer information = new StringBuffer();
		information.append("\n").append("  歌曲标题:  " + music.getTitle() + "\n")
				.append("  专辑名:  " + music.getAlbum() + "\n")
				.append("  歌手名:  " + music.getArtist() + "\n")
				.append("  歌曲时长:  " + music.getStringDuration() + "\n")
				.append("  歌曲大小:" + music.getStringSize() + "\n")
				.append("  歌曲位置:" + music.getUrl() + "\n");
		;
		AlertDialog builder = new AlertDialog.Builder(context,
				AlertDialog.THEME_HOLO_LIGHT).setTitle(music.getTitle())
				.setMessage(information).create();
		builder.show();
		// 设置builder大小
		WindowManager.LayoutParams params = builder.getWindow().getAttributes();
		params.width = 700;
		params.height = LayoutParams.WRAP_CONTENT;
		builder.getWindow().setAttributes(params);
	}

	// 用来给查看删除调用的方法
	private void showDeleteDialog(final Music music, final Context context) {
		if (music == null) {
			return;
		}
		// 设置AlertDialog的主题
		AlertDialog dialog = new AlertDialog.Builder(context,
				AlertDialog.THEME_HOLO_LIGHT)
				.setTitle("删除歌曲：" + music.getTitle())
				.setIcon(R.drawable.ic_launcher).setMessage("确认删除吗？")
				// 相当于点击取消按钮
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				})
				// 相当于点击确认按钮
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// ============删除并且刷新数据，为啥数据刷新不起来了，这是什么鬼？===========================================
						Utils.DeleteMusicById(context, music.getId());
						localMusicListData.clear();
						localMusicListData.addAll(Utils.JudgeMusic(context));
						localMusicListViewAdapter.notifyDataSetChanged();

					}
				}).create();
		dialog.show();
	}

	// 创建语音识别对话框并且设置回调
	private void showASRDigitalDialog() {
		Bundle params = new Bundle();
		// 输入PARAM_API_KEY，和PARAM_SECRET_KEY
		params.putString(BaiduASRDigitalDialog.PARAM_API_KEY,
				"xN5tQuvWaQGzjCXLOModiZKa");
		params.putString(BaiduASRDigitalDialog.PARAM_SECRET_KEY,
				"iVR578Qqn8bNIVjwKt5t5BhuKKGYKcAt");
		params.putInt(BaiduASRDigitalDialog.PARAM_DIALOG_THEME,
				BaiduASRDigitalDialog.THEME_BLUE_LIGHTBG);
		mASRDigitaldialog = new BaiduASRDigitalDialog(this, params);
		mASRDigitaldialog
				.setDialogRecognitionListener(new DialogRecognitionListener() {

					@Override
					public void onResults(Bundle results) {
						ArrayList<String> rs = results != null ? results
								.getStringArrayList(RESULTS_RECOGNITION) : null;
						if (rs != null && rs.size() != 0) {
							searchText.setText(rs.get(0).subSequence(0,
									rs.get(0).length() - 1));
						}
					}
				});
		mASRDigitaldialog.show();

	}

	class LeftMenuListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.yuyinshibie_btn:
				left_Menu.closeMenu();
				showASRDigitalDialog();
				break;

			default:
				break;
			}
		}
	}

	class MainListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.img_mainLocal:
				viewPager.setCurrentItem(0);
				text_MainLocal.setBackgroundResource(R.color.selectedcolor);
				text_MainOnline.setBackgroundResource(R.color.defaultcolor);
				text_MainQueue.setBackgroundResource(R.color.defaultcolor);
				// ToastInfo("本地音乐O(∩_∩)O");
				break;
			case R.id.img_mainOnline:
				viewPager.setCurrentItem(1);
				text_MainLocal.setBackgroundResource(R.color.defaultcolor);
				text_MainOnline.setBackgroundResource(R.color.selectedcolor);
				text_MainQueue.setBackgroundResource(R.color.defaultcolor);
				// ToastInfo("在线音乐O(∩_∩)O");
				break;
			case R.id.img_mainQueue:
				viewPager.setCurrentItem(2);
				text_MainLocal.setBackgroundResource(R.color.defaultcolor);
				text_MainOnline.setBackgroundResource(R.color.defaultcolor);
				text_MainQueue.setBackgroundResource(R.color.selectedcolor);
				// ToastInfo("播放列表O(∩_∩)O");
				break;

			case R.id.mainBottom:
				// 底部进行跳转时候进行传值,通过判断服务中音乐是否开启进行跳转
				if (mService.isPlayerExist()) {
					Intent intent = new Intent();
					intent.setAction("com.yzqmusicplayer.activity.playing");
					startActivity(intent);
				}
			default:
				break;
			}
		}
	}

	class MusicListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.img_musicplaying:
				if (!mService.isPlayerExist()) {
					ToastInfo("未找到音乐");
					// 判断开始暂停，按钮的改变在服务中发送广播
				} else if (mService.isPause()) {
					// 从暂停到开始
					mService.pauseToplay();
				} else {
					// 从开始到暂停
					mService.playToPause();
				}
				break;
			case R.id.img_musicnext:
				// 这里需要判断是播放方式，如果是单曲循环，按下一首歌，是下一首的单曲循环,随机就是下一首随机播放
				SharedPreferences sp = getSharedPreferences("playing_info",
						MODE_PRIVATE);
				int currentrule = sp.getInt("playing_rule",
						Iinfo.PLAY_RULE_ORDER);
				switch (currentrule) {
				case Iinfo.PLAY_RULE_ORDER:
				case Iinfo.PLAY_RULE_SINGLE:
					playNextMusic();
					break;
				case Iinfo.PLAY_RULE_RANDOM:
					playRandomMusic();
					break;
				default:
					break;
				}
				break;
			}
		}
	}

	class localMusicListViewListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			musicPosition = position;
			setMusicMainBottom();
		}
	}

	class CollectMusicListViewListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			musicPosition = position;
			mService.play(collectMusicListData.get(musicPosition));
		}
	}

	class MusicQueueListViewListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			musicPosition = position;
			mService.play(musicQueueListData.get(musicPosition));
			musicQueueListData.remove(position);
			musicQueueListViewAdapter.notifyDataSetChanged();
		}
	}

	// 查询的内容
	class SearchTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!s.toString().trim().isEmpty()) {
				List<Music> tempList = new ArrayList<Music>();
				for (Music temp : localMusicListData) {
					if (temp.getTitle().contains(s)
							|| temp.getArtist().contains(s)) {
						tempList.add(temp);
					}
				}
				localMusicListData.clear();
				localMusicListData.addAll(tempList);
				localMusicListViewAdapter.notifyDataSetChanged();
				return;
			}
			localMusicListData.clear();
			localMusicListData.addAll(Utils.JudgeMusic(MainActivity.this));
			localMusicListViewAdapter.notifyDataSetChanged();
		}
	}

	class MusicReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 等待从当地音乐中排队过来的music
			if ("com.yzqmusicplayer.activity.addmusic".equals(intent
					.getAction())) {
				Music add = (Music) intent.getSerializableExtra("addmusic");
				boolean addFlag = true;
				// 判断是否重复，不重复再添加
				for (Music temp : musicQueueListData) {
					if (add.getTitle().equals(temp.getTitle())) {
						// 如果数组中存在，不在遍历，直接跳出
						addFlag = false;
						break;
					}
				}
				if (addFlag) {
					musicQueueListData.add(add);
					List<Music> temp = new ArrayList<Music>();
					temp.addAll(musicQueueListData);
					System.out.println(temp.size());
					musicQueueListData.clear();
					musicQueueListData.addAll(temp);
					System.out.println(musicQueueListData.size());
					musicQueueListViewAdapter.notifyDataSetChanged();
				} else {
					ToastInfo("已经在队列中");
				}
			}
			// 等待音乐播放结束，自动播放下一首
			if ("com.yzqmusicplayer.activity.playnextmusic".equals(intent
					.getAction())) {
				playNextMusic();
			} else if ("com.yzqmusicplayer.activity.playsinglemusic"
					.equals(intent.getAction())) {
				playSingleMusic();
			} else if ("com.yzqmusicplayer.activity.playrandommusic"
					.equals(intent.getAction())) {
				playRandomMusic();
			}
			// 改变按钮
			else if ("com.yzqmusicplayer.activity.changepauseToplayButton"
					.equals(intent.getAction())) {
				img_MusicPlaying.setBackgroundResource(R.drawable.pause);
			} else if ("com.yzqmusicplayer.activity.changeplayTopauseButton"
					.equals(intent.getAction())) {
				img_MusicPlaying.setBackgroundResource(R.drawable.play);
			}
			// 改变歌曲和歌手名
			else if ("com.yzqmusicplayer.activity.changeSongAndSinger"
					.equals(intent.getAction())) {
				Music currentMusic = (Music) intent
						.getSerializableExtra("currentMusic");
				text_mainBottomsong.setText(currentMusic.getTitle());
				text_mainBottomsinger.setText(currentMusic.getArtist());
			} else if ("com.yzq.musicplayer.notifyCollectDataChanged"
					.equals(intent.getAction())) {
				collectMusicListData.clear();
				collectMusicListData.addAll(Utils
						.getCollectMusic(MainActivity.this));
				collcectMusicListViewAdapter.notifyDataSetChanged();
			}
			// 注意：来电去电都会改变PHONE_STATE，只是ACTION_NEW_OUTGOING_CALL能判断是这是去电
			// 在这里完全可以用PHONE_STATE来改变音乐播放状态。
			// else
			// if(Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction()))
			// {
			// if(!mService.isPause())
			// {
			// mService.playToPause();
			// }
			// }
			else if ("android.intent.action.PHONE_STATE".equals(intent
					.getAction())) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			}

		}

		// 电话状态改变的监听
		PhoneStateListener listener = new PhoneStateListener() {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				// TODO Auto-generated method stub
				// state 当前状态 incomingNumber,貌似没有去电的API
				super.onCallStateChanged(state, incomingNumber);
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					System.out.println("挂断");
					//需要判断之前电话之前是否是播放的，是的话再继续播放，防止用户之前就暂停，接完电话就自动播放的bug
					if (mService.isPause()&&isPlayBeforePhoneCall) {
						mService.pauseToplay();
						isPlayBeforePhoneCall=false;
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					System.out.println("接听");
				case TelephonyManager.CALL_STATE_RINGING:
					// 输出来电号码
					System.out.println("响铃:来电号码" + incomingNumber);
					if (!mService.isPause()) {
						mService.playToPause();
						isPlayBeforePhoneCall=true;
					}
					break;
				}
			}
		};

	}

	// 播放下一首歌曲
	private void playNextMusic() {
		// 有播放队列的时候，冲播放队列中选择
		if (musicQueueListData.size() > 0) {

			mService.play(musicQueueListData.get(0));
			// 设置对应的显示的歌曲名和歌手
			musicQueueListData.remove(0);
			// 通知适配器改变播放列表
			musicQueueListViewAdapter.notifyDataSetChanged();
			if (PlayingActivity.preMusic > 0) {
				PlayingActivity.preMusic = mService.record.size() - 1;
			}
		} else {
			if (localMusicListData.size() > 0) {
				if (++musicPosition >= localMusicListData.size()) {
					musicPosition = 0;
				}
				mService.play(localMusicListData.get(musicPosition));
			}
		}
	}

	private void setMusicMainBottom() {
		// 按了前一首歌曲，再按下一首歌的時候從記錄的位置開始順序,記錄的按鈕寫的有問題======================================
		// if(PlayingActivity.preMusic>0&&PlayingActivity.preMusic<mService.record.size()-1)
		// {
		// mService.play(mService.record.get(++PlayingActivity.preMusic));
		// }
		// 也有問題==============================導致每次按上一曲播放的都是同一曲歌曲了
		if (PlayingActivity.preMusic > 0) {
			PlayingActivity.preMusic = mService.record.size() - 1;
		}
		mService.play(localMusicListData.get(musicPosition));
	}

	private void playRandomMusic() {
		if (musicQueueListData.size() > 0) {
			int randomNum = Utils.RandomNum(musicQueueListData.size());
			mService.play(musicQueueListData.get(randomNum));
			// 设置对应的显示的歌曲名和歌手
			musicQueueListData.remove(randomNum);
			// 通知适配器改变播放列表
			musicQueueListViewAdapter.notifyDataSetChanged();
		} else {
			if (localMusicListData.size() > 0) {
				int randomNum = Utils.RandomNum(localMusicListData.size());
				musicPosition = randomNum;
				mService.play(localMusicListData.get(musicPosition));
			}
		}
	}

	private void playSingleMusic() {
		mService.play(localMusicListData.get(musicPosition));
	}

}
