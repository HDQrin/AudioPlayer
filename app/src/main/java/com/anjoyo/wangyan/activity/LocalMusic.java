package com.anjoyo.wangyan.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anjoyo.wangyan.adapter.LocalListAdapter;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.db.MusicDBHelper;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.service.OnPlayerStateChangeListener;
import com.anjoyo.wangyan.service.PlayerService;
import com.anjoyo.wangyan.ui.R;
import com.anjoyo.wangyan.util.ToastShow;

/**
 * 本地歌曲列表界面
 * 
 */
public class LocalMusic extends Activity {
	// 歌曲显示listview
	private ListView lvMusic;
	// 无歌曲时显示的图片
	private ImageView nothingImg;
	// 歌曲adapter
	private LocalListAdapter lvMusicAdapter;
	// 标题栏处的按钮
	private ImageButton back, scan;
	// 查询本地数据库得到的cursor
	private Cursor curLocal;
	// 正在扫描对话框
	private Dialog scanDialog;
	// 循环线程
	private ScanThread scanThread;
	// 数据库
	private MusicDBHelper localDbHelper;
	// 播放音乐
	private ArrayList<MusicInfo> musicList;

	// miniplayer专辑图片，歌曲名，歌手名
	private ImageView album;
	private TextView title;
	private TextView artist;
	// miniplayer播放，下一首按钮
	private ImageView play, next;
	// miniplayer跳转到播放显示歌词页面
	private RelativeLayout miniPlayer;

	// 播放状态改变监听器
	private OnPlayerStateChangeListener changeListener;
	private Boolean flag = false;

	// 匿名内部类handler用于listview显示
	private Handler handler = new Handler() {
		public  void handleMessage(Message msg) {
			// 根据what判断进行不同的操作
			switch (msg.what) {
			case 0:
				break;
			case 1:
				curLocal = (Cursor) msg.obj;
				// 扫描完对话框关闭
				scanDialog.dismiss();
				Log.e(PlayerFinal.TAG,
						"curLocal.getCount()" + curLocal.getCount());
				if (curLocal.getCount() != 0) {
					nothingImg.setVisibility(View.GONE);
					lvMusic.setVisibility(View.VISIBLE);
					lvMusicAdapter = new LocalListAdapter(LocalMusic.this,
							curLocal);
					lvMusic.setAdapter(lvMusicAdapter);
					musicList = (ArrayList<MusicInfo>) localDbHelper
							.getMusicListFromLocal(curLocal);
				} else {
					nothingImg.setVisibility(View.VISIBLE);
					lvMusic.setVisibility(View.GONE);
				}
				break;
			case 2:
				curLocal = (Cursor) msg.obj;
				View randomPlay = getLayoutInflater().inflate(
						R.layout.local_music_headview, null, true);
				lvMusic.addHeaderView(randomPlay);
				// 初次进入时加headview
				// 扫描到歌曲，图片设置为gone，listview显示
				if (curLocal.getCount() != 0) {
					nothingImg.setVisibility(View.GONE);
					lvMusic.setVisibility(View.VISIBLE);
					lvMusicAdapter = new LocalListAdapter(LocalMusic.this,
							curLocal);
					lvMusic.setAdapter(lvMusicAdapter);
					musicList = (ArrayList<MusicInfo>) localDbHelper
							.getMusicListFromLocal(curLocal);
				} else {
					nothingImg.setVisibility(View.VISIBLE);
					lvMusic.setVisibility(View.GONE);
					ToastShow.toastShow(LocalMusic.this, "本地还未添加歌曲");
				}
				break;
			case 3:
				scanDialog.dismiss();
				nothingImg.setVisibility(View.VISIBLE);
				lvMusic.setVisibility(View.GONE);
				break;
			}
			if (lvMusicAdapter != null) {
				// 注册状态改变监听事件
				PlayerService.registerStateChangeListener(changeListener);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_music);
		initListView();
		initButton();
		setListViewClick();
		initMiniPlayer();
		// 状态改变监听事件,改变UI
		changeListener = new OnPlayerStateChangeListener() {

			@Override
			public void onStateChange(int state, int mode,
					List<MusicInfo> musicList, int position) {
				
				if (musicList != null) {
					title.setText(musicList.get(position).getTitle());
					artist.setText(musicList.get(position).getArtist());
					if (musicList.get(position).getAlbum_img_path() != null) {
						Uri uri = Uri.parse(musicList.get(position)
								.getAlbum_img_path());
						album.setImageURI(uri);
					}
					Log.e(PlayerFinal.TAG, "空指针！！！！！！" + lvMusicAdapter);
					if (lvMusicAdapter != null) {
						lvMusicAdapter.setPlayPosition(position);
						lvMusicAdapter.notifyDataSetChanged();
					}
				} else {
					title.setText("我的音乐");
					artist.setText("演唱者");
				}
				switch (state) {
				case PlayerFinal.STATE_PLAY:
					play.setImageResource(R.drawable.player_pause);
					break;
				case PlayerFinal.STATE_CONTINUE:
					play.setImageResource(R.drawable.player_pause);
					break;
				case PlayerFinal.STATE_PAUSE:
					play.setImageResource(R.drawable.player_play);
					break;
				case PlayerFinal.STATE_STOP:
					play.setImageResource(R.drawable.player_play);
					break;
				}
			}
		};
	}

	/**
	 * 初始化miniplayer中控件，以及设置监听事件
	 */
	private void initMiniPlayer() {
		
		album = (ImageView) findViewById(R.id.local_miniplayer_album);
		title = (TextView) findViewById(R.id.local_miniplayer_song);
		artist = (TextView) findViewById(R.id.local_miniplayer_artist);
		play = (ImageView) findViewById(R.id.local_miniplayer_play);
		next = (ImageView) findViewById(R.id.local_miniplayer_next);
		miniPlayer = (RelativeLayout) findViewById(R.id.local_miniplayer_layout);
		miniPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// 跳转到播放界面
				Intent intent = new Intent(LocalMusic.this,
						PlayerAndLyric.class);
				startActivity(intent);
			}
		});
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// 发送广播给service
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_BUTTON);
				sendBroadcast(intent);
			}
		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// 发送广播给service
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_NEXT);
				sendBroadcast(intent);
			}
		});
	}

	/**
	 * listView监听事件
	 */
	private void setListViewClick() {
		
		lvMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int arg2,
					long arg3) {
				
				// 点击item传一个musicList和当前点击位置给service
				// 发送相应广播给service
				int position = arg2 - 1; // 加了headview
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_ITEM);
				intent.putParcelableArrayListExtra(PlayerFinal.PLAYER_LIST,
						musicList);
				intent.putExtra(PlayerFinal.PLAYER_WHERE, "local");
				Log.e(PlayerFinal.TAG, "arg2------------------" + arg2);
				// 不能点击headview
				if (arg2 == 0) {
					Log.e(PlayerFinal.TAG, "0000------------------" + arg2);
					Random ran = new Random();
					int random = ran.nextInt(musicList.size());
					while (random == 0) {
						random = ran.nextInt(musicList.size());
						if (random != 0) {
							break;
						}
					}
					intent.putExtra(PlayerFinal.PLAYER_POSITION, random);
					sendBroadcast(intent);
				} else {
					intent.putExtra(PlayerFinal.PLAYER_POSITION, position);
					sendBroadcast(intent);
				}
			}
		});
	}

	/**
	 * 刚进入时查询本地数据库得到数据给listview
	 */
	private void initListView() {
		
		nothingImg = (ImageView) findViewById(R.id.local_nothing_img);
		lvMusic = (ListView) findViewById(R.id.local_listview);
		localDbHelper = new MusicDBHelper(LocalMusic.this);
		new Thread() {
			public void run() {
				curLocal = localDbHelper.queryLocalByID();
				handler.sendMessage(handler.obtainMessage(2, curLocal));
			};
		}.start();

	}

	/**
	 * 设置按钮点击事件
	 */
	private void initButton() {
		
		back = (ImageButton) findViewById(R.id.local_actionbar_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		scan = (ImageButton) findViewById(R.id.local_actionbar_scan);
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				nothingImg.setVisibility(View.GONE);
				lvMusic.setVisibility(View.VISIBLE);
				scanDialog();
				// 点击扫描按钮开启扫描线程
				scanThread = new ScanThread();
				scanThread.start();
			}

		});
	}

	/**
	 * 扫描对话框
	 */
	private void scanDialog() {
		
		Builder builder = new AlertDialog.Builder(LocalMusic.this);
		builder.setCancelable(true);
		View dialogView = LayoutInflater.from(LocalMusic.this).inflate(
				R.layout.scan_dialog, null);
		builder.setView(dialogView);
		RotateAnimation anim = new RotateAnimation(0, 360,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(1000);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(-1);
		ImageView dialogImg = (ImageView) dialogView
				.findViewById(R.id.scan_img);
		dialogImg.startAnimation(anim);
		builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				handler.sendMessage(handler.obtainMessage(3));
			}
		});
		scanDialog = builder.create();
		scanDialog.show();
	}

	private class ScanThread extends Thread {
		@Override
		public void run() {
			
			// while (isRun) {
			// Looper.prepare();
			ContentResolver conRes = getContentResolver();
			Cursor cur = conRes.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
					MediaStore.Audio.Media.SIZE + ">?",
					new String[] { "2097152" }, MediaStore.Audio.Media.TITLE
							+ " asc");
			Log.e(PlayerFinal.TAG, "查询到多媒体文件一共" + cur.getCount());
			int titleIndex = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int artistIndex = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
			int albumIndex = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
			int pathIndex = cur.getColumnIndex(MediaStore.Audio.Media.DATA);
			int durationIndex = cur
					.getColumnIndex(MediaStore.Audio.Media.DURATION);
			int sizeIndex = cur.getColumnIndex(MediaStore.Audio.Media.SIZE);
			int fileNameIndex = cur
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
			localDbHelper = new MusicDBHelper(LocalMusic.this);
			localDbHelper.clearLocal();
			if (cur.getCount() != 0) {
				cur.moveToFirst();
				do {
					MusicInfo music = new MusicInfo();
					music.setTitle(cur.getString(titleIndex));
					music.setArtist(cur.getString(artistIndex));
					music.setAlbum(cur.getString(albumIndex));
					music.setPath(cur.getString(pathIndex));
					music.setDuration(cur.getLong(durationIndex));
					music.setSize(cur.getLong(sizeIndex));
					String fileName = cur.getString(fileNameIndex);
					String lrcName = fileName.replace(".mp3", ".lrc");
					music.setLyric_file_name(lrcName);
					Long i = localDbHelper.insertLocal(music);
					Log.i(PlayerFinal.TAG, "insert---->" + i);
					localDbHelper.insertArtist(music);
					localDbHelper.insertAlbum(music);
					cur.moveToNext();
				} while (!cur.isAfterLast());
				curLocal = localDbHelper.queryLocalByID();
				Log.e(PlayerFinal.TAG, "curLocal" + curLocal.getCount());
				handler.sendMessage(handler.obtainMessage(1, curLocal));
			} else {
				Log.e(PlayerFinal.TAG, "本地没有多媒体文件" + cur.getCount());
				handler.sendMessage(handler.obtainMessage(3));
			}
			// Looper.loop();
		}
	}

	// }

	@Override
	public void onResume() {
		super.onResume();
		if (flag) {
			// 注册状态改变监听事件
			PlayerService.registerStateChangeListener(changeListener);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// 解除注册状态改变监听事件
		PlayerService.unRegisterStateChangeListener(changeListener);
		flag = true;
	}

	@Override
	protected void onDestroy() {
		
		// 关闭cursor和数据库
		curLocal.close();
		localDbHelper.close();
		super.onDestroy();
	}
}
