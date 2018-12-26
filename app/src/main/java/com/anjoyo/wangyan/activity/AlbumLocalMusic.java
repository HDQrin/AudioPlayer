package com.anjoyo.wangyan.activity;

import java.util.ArrayList;
import java.util.List;

import com.anjoyo.wangyan.adapter.LocalListAdapter;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.db.MusicDBHelper;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.service.OnPlayerStateChangeListener;
import com.anjoyo.wangyan.service.PlayerService;
import com.anjoyo.wangyan.ui.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 专辑歌曲列表显示页
 */
public class AlbumLocalMusic extends Activity {
	// 传参过来的专辑名
	private String album_title;
	// 标题栏按钮
	private ImageButton back, scan;
	// 歌曲列表listview
	private ListView lvMusic;
	// 自定义adapter
	private LocalListAdapter adapter;
	// 实体类集合，用于传参数给service
	private ArrayList<MusicInfo> musicList;
	// 查询数据库返回的cur，用于自定义adapter
	private Cursor cur;
	// 数据库工具类实例
	private MusicDBHelper localDbHelper;
	// 主线程接收到查询结果，listview适配adapter
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			cur = (Cursor) msg.obj;
			adapter = new LocalListAdapter(AlbumLocalMusic.this, cur);
			lvMusic.setAdapter(adapter);
			// 注册状态改变监听事件
			PlayerService.registerStateChangeListener(changeListener);
		};
	};
	// service状态改变UI更新监听接口
	private OnPlayerStateChangeListener changeListener;
	// miniplayer的控件
	private ImageView album;
	private TextView title;
	private TextView artist;
	private ImageView play, next;
	private RelativeLayout miniPlayer;
	// 用于判断是否第一次进入
	private Boolean flag=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 加载xml布局
		setContentView(R.layout.local_music);
		// 接到intent传来的参数，专辑名
		Intent intent = getIntent();
		album_title = intent.getStringExtra(PlayerFinal.LOCAL_ALBUM);
		Log.e(PlayerFinal.TAG, "album--->" + album_title);
		// 初始化listview
		initListView();
		// 设置按钮点击事件
		initButton();
		// 设置底部miniplayer
		initMiniPlayer();
		// 接口实例化，重写方法
		changeListener = new OnPlayerStateChangeListener() {

			@Override
			public void onStateChange(int state, int mode,
					List<MusicInfo> musicList, int position) {
				// TODO Auto-generated method stub
				if (musicList != null) {
					title.setText(musicList.get(position).getTitle());
					artist.setText(musicList.get(position).getArtist());
					if (musicList.get(position).getAlbum_img_path() != null) {
						Uri uri = Uri.parse(musicList.get(position)
								.getAlbum_img_path());
						album.setImageURI(uri);
					}
					adapter.setPlayPosition(position);
					adapter.notifyDataSetChanged();
				} else {
					title.setText("欢迎来到我的音乐");
					artist.setText("让音乐跟我走");
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
		// TODO Auto-generated method stub
		album = (ImageView) findViewById(R.id.local_miniplayer_album);
		title = (TextView) findViewById(R.id.local_miniplayer_song);
		artist = (TextView) findViewById(R.id.local_miniplayer_artist);
		play = (ImageView) findViewById(R.id.local_miniplayer_play);
		next = (ImageView) findViewById(R.id.local_miniplayer_next);
		miniPlayer = (RelativeLayout) findViewById(R.id.local_miniplayer_layout);
		miniPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 跳转到播放界面
				Intent intent = new Intent(AlbumLocalMusic.this,
						PlayerAndLyric.class);
				startActivity(intent);
			}
		});
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送广播给service
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_BUTTON);
				sendBroadcast(intent);
			}
		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送广播给service
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_NEXT);
				sendBroadcast(intent);
			}
		});
	}

	/**
	 * 设置按钮点击事件
	 */
	private void initButton() {
		// TODO Auto-generated method stub
		TextView title = (TextView) findViewById(R.id.local_actionbar_title);
		title.setText(album_title);
		back = (ImageButton) findViewById(R.id.local_actionbar_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		scan = (ImageButton) findViewById(R.id.local_actionbar_scan);
		scan.setVisibility(View.INVISIBLE);

	}

	/**
	 * 刚进入时查询本地数据库得到数据给listview
	 */
	private void initListView() {
		// TODO Auto-generated method stub
		lvMusic = (ListView) findViewById(R.id.local_listview);
		new Thread() {
			public void run() {
				localDbHelper = new MusicDBHelper(AlbumLocalMusic.this);
				cur = localDbHelper.queryLocalByAlbum(album_title);
				musicList = localDbHelper.getMusicListFromLocal(cur);
				handler.sendMessage(handler.obtainMessage(0, cur));
			};
		}.start();
		lvMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				int position = arg2;
				// new一个Intent用于带参数跳转
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_ITEM);
				intent.putParcelableArrayListExtra(PlayerFinal.PLAYER_LIST,
						musicList);
				intent.putExtra(PlayerFinal.PLAYER_POSITION, position);
				intent.putExtra(PlayerFinal.PLAYER_WHERE, "local");
				sendBroadcast(intent);
			}
		});
	}

	@Override
	public void onResume() {
		if(flag){
			// 注册状态改变监听事件
			PlayerService.registerStateChangeListener(changeListener);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		// 解除注册状态改变监听事件
		PlayerService.unRegisterStateChangeListener(changeListener);
		flag = true;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// 关闭cursor和数据库
		cur.close();
		localDbHelper.close();
		super.onDestroy();
	}
}
