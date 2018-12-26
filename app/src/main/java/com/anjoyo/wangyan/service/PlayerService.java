package com.anjoyo.wangyan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.anjoyo.wangyan.activity.MainActivity;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.ui.R;
import com.anjoyo.wangyan.util.PlayerHelper;

/**
 * 歌曲播放service
 * 
 */
public class PlayerService extends Service implements Runnable {

	// 点击了播放/暂停键的时候发这个action的广播
	public static final String ACTION_PLAY_BUTTON = "com.wangyan.service.ACTION_PLAY_BUTTON";
	// 在点击了播放列表的时候发这个action的广播
	public static final String ACTION_PLAY_ITEM = "com.wangyan.service.ACTION_PLAY_ITEM";
	// 在点击了播放列表的时候发这个action的广播
	public static final String ACTION_PLAY_INTERNET = "com.wangyan.service.ACTION_PLAY_INTERNET";
	// 定义上一首，下一首action
	public static final String ACTION_PLAY_PREVIOUS = "com.wangyan.service.ACTION_PLAY_PREVIOUS";
	public static final String ACTION_PLAY_NEXT = "com.wangyan.service.ACTION_PLAY_NEXT";
	// 更改播放模式mode的action
	public static final String ACTION_MODE = "com.wangyan.service.ACTION_MODE";
	// seekbar进度更改的action
	public static final String ACTION_SEEKBAR = "com.wangyan.service.ACTION_SEEKBAR";

	// 当前音乐播放状态，默认为等待
	public static int state = PlayerFinal.STATE_WAIT;
	// 当前音乐循环模式，默认为随机
	public static int mode = PlayerFinal.MODE_RANDOM;
	// 表示播放状态是否改变，进度条是否改变，播放模式时候改变
	public static boolean stateChange, seekChange, modeChange;
	// 常驻线程是否运行
	public static Boolean isRun = true;
	// 播放歌曲帮助类
	public static PlayerHelper player;
	// 当前播放列表
	public static List<MusicInfo> serviceMusicList;
	// 当前播放歌曲位置
	public static int servicePosition = 0;

	// 用一个List保存 客户注册的监听----此监听用于回调更新客户的ui，状态改变监听
	private static List<OnPlayerStateChangeListener> stateListeners = new ArrayList<OnPlayerStateChangeListener>();
	// 用于seekbar进度改变监听
	private static List<OnSeekChangeListener> seekListenerList = new ArrayList<OnSeekChangeListener>();
	// 用于播放模式改变监听
	private static List<OnModeChangeListener> modeListenerList = new ArrayList<OnModeChangeListener>();

	// 当前歌曲播放进度
	private static int progress = 0;
	// 当前歌曲进度条最大值
	private static int max = 0;
	// 当前播放的时间
	private static String time = "0:00";
	// 当前歌曲播放的时长
	private static String duration = "0:00";

	// 通知栏管理
	private NotificationManager notiManager;
	// 通知栏
	private Notification notifi;
	private String notifiTitle;
	private String notifiArtist;

	private String where;

	// handler匿名内部类，用于监听器遍历回调
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 对List中的所有监听器遍历回调，根据what值判断回调哪个监听器
			switch (msg.what) {
			case 0:
				for (OnPlayerStateChangeListener listener : stateListeners) {
					listener.onStateChange(state, mode, serviceMusicList,
							servicePosition);
				}
				break;
			case 1:
				for (OnSeekChangeListener listener : seekListenerList) {
					listener.onSeekChange(progress, max, time, duration);
				}
				break;
			case 2:
				for (OnModeChangeListener listener : modeListenerList) {
					listener.onModeChange(mode);
				}
				break;
			}

		};
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		// 注册广播，并添加Action
		PlayerReceiver receiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_PLAY_ITEM);
		filter.addAction(ACTION_PLAY_BUTTON);
		filter.addAction(ACTION_PLAY_PREVIOUS);
		filter.addAction(ACTION_PLAY_NEXT);
		filter.addAction(ACTION_MODE);
		filter.addAction(ACTION_SEEKBAR);
		registerReceiver(receiver, filter);
		// new歌曲播放类
		player = new PlayerHelper();
		// 开启常驻线程
		new Thread(this).start();
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * notification设置弹出
	 * 
	 * @param title
	 * @param artist
	 */
	private void showNotification(String title, String artist) {
		// TODO Auto-generated method stub
		notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifi = new Notification();
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notifi.contentIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notifi.flags |= Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONGOING_EVENT;
		notifi.icon = R.drawable.notification;
		notifi.tickerText = "正在播放：" + title;
		notifi.contentView = new RemoteViews(getPackageName(),
				R.layout.notification);
		notifi.contentView.setTextViewText(R.id.notifi_song, title);
		notifi.contentView.setTextViewText(R.id.notifi_artist, artist);
		notiManager.notify(0, notifi);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// 常驻线程，死循环
		while (isRun) {
			// 判断如果state改变，player播放类执行不同的方法
			if (stateChange) {
				Log.e(PlayerFinal.TAG, "stateChange=" + state);
				switch (state) {
				case PlayerFinal.STATE_WAIT:
					break;
				case PlayerFinal.STATE_PLAY:
					if (where.equals("local")) {
						player.play(serviceMusicList.get(servicePosition)
								.getPath());
					} else if (where.equals("internet")) {
						Uri uri = Uri.parse(serviceMusicList.get(
								servicePosition).getPath());
						player.playInternet(getApplicationContext(), uri);
					}
					// 播放状态要动seekbar
					seekChange = true;
					break;
				case PlayerFinal.STATE_PAUSE:
					player.pause();
					break;
				case PlayerFinal.STATE_CONTINUE:
					player.continuePlay();
					// 播放状态要动seekbar
					seekChange = true;
					break;
				case PlayerFinal.STATE_STOP:
					player.stop();
					break;
				}
				// state改变为false
				stateChange = false;
				// 向handler发送一条消息，通知handler执行回调函数
				handler.sendEmptyMessage(0);

				notifiTitle = serviceMusicList.get(servicePosition).getTitle();
				notifiArtist = serviceMusicList.get(servicePosition)
						.getArtist();
				showNotification(notifiTitle, notifiArtist);
			}
			if (player.isPlaying()) {
				seekChange = true;
			} else {
				seekChange = false;
			}
			// 如果进度改变执行以下
			if (seekChange) {
				// 得到当前播放时间，int，毫秒单位，也是进度条的当前进度
				progress = player.getPlayCurrentTime();
				// 得到歌曲播放总时长，为进度条的最大值
				max = player.getPlayDuration();
				// 当前播放时间改变单位为分
				float floatTime = (float) progress / 1000.0f / 60.0f;
				// 当前播放时间转换为字符类型
				String timeStr = Float.toString(floatTime);
				// 根据小数点切分
				String timeSub[] = timeStr.split("\\.");
				// 初始值为0.0，在后边补0
				if (timeSub[1].length() < 2) {
					timeSub[1] = timeSub[1] + "0";
				} else {
					// 截取小数点后两位
					timeSub[1] = timeSub[1].substring(0, 2);
				}
				// 拼接得到当前播放时间，用于UI界面显示
				time = timeSub[0] + ":" + timeSub[1];
				// seekChange改回false
				seekChange = false;
				try {
					// 等1s发送消息
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 发送相应消息给handler
				handler.sendEmptyMessage(1);
			}
			// 如果歌曲播放模式改变，发送消息给handler，modeChange改回false
			if (modeChange) {
				handler.sendEmptyMessage(2);
				modeChange = false;
			}
		}
	}

	/**
	 * 定义一个广播，用于接收客户发来的播放音乐的广播
	 */
	public class PlayerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(PlayerFinal.TAG, "action===============" + action);
			// 如果收到的是点击播放列表时发送的广播
			if (action.equals(ACTION_PLAY_ITEM)) {
				where = intent.getStringExtra(PlayerFinal.PLAYER_WHERE);
				// 得到当前页面传过来的播放列表
				serviceMusicList = intent
						.getParcelableArrayListExtra(PlayerFinal.PLAYER_LIST);
				// 得到当前页面点击的item的position
				servicePosition = intent.getIntExtra(
						PlayerFinal.PLAYER_POSITION, 0);
				// state改变为play，播放歌曲
				state = PlayerFinal.STATE_PLAY;
				// state改变
				stateChange = true;
			} else if (action.equals(ACTION_PLAY_BUTTON)) {
				if (serviceMusicList != null) {
					// 如果接收的是点击暂停/播放键时的广播
					// 根据当前状态点击后，进行相应状态改变
					switch (state) {
					case PlayerFinal.STATE_PLAY:
					case PlayerFinal.STATE_CONTINUE:
						state = PlayerFinal.STATE_PAUSE;
						break;
					case PlayerFinal.STATE_PAUSE:
						state = PlayerFinal.STATE_CONTINUE;
						break;
					case PlayerFinal.STATE_STOP:
						state = PlayerFinal.STATE_PLAY;
						break;
					}
					// state改变
					stateChange = true;
				}
			} else if (action.equals(ACTION_PLAY_PREVIOUS)) {
				if (serviceMusicList != null) {
					// 点击上一首按钮，如果当前位置为0，退回歌曲列表最后一首
					if (servicePosition == 0) {
						servicePosition = serviceMusicList.size() - 1;
					} else {
						servicePosition--;
					}
					// state改变
					state = PlayerFinal.STATE_PLAY;
					stateChange = true;
				}
			} else if (action.equals(ACTION_PLAY_NEXT)) {
				if (serviceMusicList != null) {
					// 点击下一首，根据播放模式不同，下一首位置不同
					switch (mode) {
					case PlayerFinal.MODE_SINGLE:
						state = PlayerFinal.STATE_PLAY;
						break;
					case PlayerFinal.MODE_LOOP:
						if (servicePosition == serviceMusicList.size() - 1) {
							servicePosition = 0;
						} else {
							servicePosition++;
						}
						state = PlayerFinal.STATE_PLAY;
						break;
					case PlayerFinal.MODE_RANDOM:
						Random random = new Random();
						int p = servicePosition;
						while (true) {
							servicePosition = random.nextInt(serviceMusicList
									.size());
							Log.d(PlayerFinal.TAG, "p" + p + ":random"
									+ servicePosition);
							if (p != servicePosition) {
								state = PlayerFinal.STATE_PLAY;
								break;
							}
						}
						break;
					case PlayerFinal.MODE_ORDER:
						if (servicePosition == serviceMusicList.size() - 1) {
							state = PlayerFinal.STATE_STOP;
						} else {
							servicePosition++;
							state = PlayerFinal.STATE_PLAY;
						}
						break;
					}
					// state改变
					stateChange = true;
				}
			} else if (action.equals(ACTION_MODE)) {
				Log.i(PlayerFinal.TAG, "收到播放模式更改广播，当前播放模式为" + mode);
				switch (mode) {
				// 根据当前mode，做出mode的更改
				case PlayerFinal.MODE_SINGLE:
					mode = PlayerFinal.MODE_ORDER;
					break;
				case PlayerFinal.MODE_LOOP:
					mode = PlayerFinal.MODE_RANDOM;
					break;
				case PlayerFinal.MODE_RANDOM:
					mode = PlayerFinal.MODE_SINGLE;
					break;
				case PlayerFinal.MODE_ORDER:
					mode = PlayerFinal.MODE_LOOP;
					break;
				}
				// 播放模式改变
				modeChange = true;
			} else if (action.equals(ACTION_SEEKBAR)) {
				// seekbar发送的广播
				// 得到传过来的当前进度条进度，更改歌曲播放位置
				int seekTime = intent.getIntExtra(PlayerFinal.SEEKBAR_PROGRESS,
						progress);
				Log.i(PlayerFinal.TAG, "接收到seekbar广播了" + "seekTime" + seekTime);
				player.seekToMusic(seekTime);
				// 进度条改变
				seekChange = true;
			}
		}
	}

	/**
	 * 向service注册一个监听器，用于监听播放状态的改变
	 * 
	 * @param listener
	 */
	public static void registerStateChangeListener(
			OnPlayerStateChangeListener listener) {
		listener.onStateChange(state, mode, serviceMusicList, servicePosition);
		stateListeners.add(listener);
		Log.e(PlayerFinal.TAG, "注册stateChange的监听，当前一共有" + stateListeners.size()
				+ "个");
	}

	/**
	 * 向service注册一个监听器，用于监听seekbar改变
	 * 
	 * @param seekListener
	 */
	public static void registerSeekChangeListener(
			OnSeekChangeListener seekListener) {
		seekListener.onSeekChange(progress, max, time, duration);
		seekListenerList.add(seekListener);
		Log.d(PlayerFinal.TAG,
				"注册seekChange的监听，当前一共有" + seekListenerList.size() + "个");
	}

	/**
	 * 向service注册一个监听器，用于监听mode的改变
	 * 
	 * @param modeListener
	 */
	public static void registerModeChangeListener(
			OnModeChangeListener modeListener) {
		modeListener.onModeChange(mode);
		modeListenerList.add(modeListener);
		Log.d(PlayerFinal.TAG, "注册ModeChange，当前一共有" + modeListenerList.size()
				+ "个");
	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param statelistener
	 */
	public static void unRegisterStateChangeListener(
			OnPlayerStateChangeListener statelistener) {
		stateListeners.remove(statelistener);
		Log.d(PlayerFinal.TAG, "解除注册listener，当前一共有" + stateListeners.size()
				+ "个");

	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param seekListener
	 */
	public static void unRegisterSeekChangeListener(
			OnSeekChangeListener seekListener) {
		seekListenerList.remove(seekListener);
		Log.d(PlayerFinal.TAG, "解除注册seekChange，当前一共有" + stateListeners.size()
				+ "个");

	}

	/**
	 * 解除之前注册的监听器
	 * 
	 * @param modeListener
	 */
	public static void unRegisterModeChangeListener(
			OnModeChangeListener modeListener) {
		modeListenerList.remove(modeListener);
		Log.d(PlayerFinal.TAG, "解除注册modeChange，当前一共有" + stateListeners.size()
				+ "个");

	}

	/**
	 * 结束service之前，取消通知栏
	 */
	@Override
	public boolean stopService(Intent name) {
		// TODO Auto-generated method stub
		notiManager.cancelAll();
		return super.stopService(name);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
