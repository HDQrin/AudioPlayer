package com.anjoyo.wangyan.activity;

/*
* 播放歌曲（带歌词）页面
* */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.anjoyo.wangyan.adapter.PlayListAdapter;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.lyric.DefaultLrcBuilder;
import com.anjoyo.wangyan.lyric.ILrcBuilder;
import com.anjoyo.wangyan.lyric.LrcRow;
import com.anjoyo.wangyan.lyric.LrcView;
import com.anjoyo.wangyan.service.OnModeChangeListener;
import com.anjoyo.wangyan.service.OnPlayerStateChangeListener;
import com.anjoyo.wangyan.service.OnSeekChangeListener;
import com.anjoyo.wangyan.service.PlayerService;
import com.anjoyo.wangyan.ui.R;
import com.anjoyo.wangyan.util.ToastShow;
/**
 * 歌曲播放，显示歌词界面
 *
 */
public class PlayerAndLyric extends Activity {
	// 标题栏按钮
	private ImageButton back, list;
	// 标题栏歌手和歌曲
	private TextView title, artist;
	// 播放，上一首，下一首，播放状态改变，音量改变按钮
	private ImageButton play, pre, next, mode_btn, volume;
	// 播放进度条
	private SeekBar seekBar;
	// seekbar时间
	private TextView time_position, duration;

	// 歌词
	private LrcView mLrcView;
	// 播放列表
	private ListView playQueue;
	// 播放列表
	private ArrayList<MusicInfo> musicList;

	// 回调函数更新UI
	private OnPlayerStateChangeListener stateChangeListener;
	private OnSeekChangeListener seekChangeListener;
	private OnModeChangeListener modeChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_lyric);
		initMiniPlayer();
		initActionBar();
		setSeekBar();
		//lrcView = (LyricView) findViewById(R.id.lyric_view);
		playQueue = (ListView) findViewById(R.id.player_queue);
		mLrcView = (LrcView) findViewById(R.id.lyric_view);
		// 播放模式改变监听事件
		modeChangeListener = new OnModeChangeListener() {

			@Override
			public void onModeChange(int mode) {
				// TODO Auto-generated method stub
				switch (mode) {
				case PlayerFinal.MODE_SINGLE:
					mode_btn.setImageResource(R.drawable.player_mode_single);
					ToastShow.toastShow(PlayerAndLyric.this, "单曲播放");
					break;
				case PlayerFinal.MODE_LOOP:
					mode_btn.setImageResource(R.drawable.player_mode_loop);
					mode = PlayerFinal.MODE_LOOP;
					ToastShow.toastShow(PlayerAndLyric.this, "循环播放");
					break;
				case PlayerFinal.MODE_RANDOM:
					mode_btn.setImageResource(R.drawable.player_mode_random);
					ToastShow.toastShow(PlayerAndLyric.this, "随机播放");
					break;
				case PlayerFinal.MODE_ORDER:
					mode_btn.setImageResource(R.drawable.player_mode_order);
					ToastShow.toastShow(PlayerAndLyric.this, "顺序播放");
					break;
				}
			}
		};
		// 进度条监听
		seekChangeListener = new OnSeekChangeListener() {

			@Override
			public void onSeekChange(int progress, int max, String time,
					String duration) {
				// TODO Auto-generated method stub
				seekBar.setMax(max);
				seekBar.setProgress(progress);
				time_position.setText(time);
				                                                                                                                                                                                                          mLrcView.seekLrcToTime(progress);
			}
		};
		// 播放状态改变监听
		stateChangeListener = new OnPlayerStateChangeListener() {

			@Override
			public void onStateChange(int state, int mode,
					List<MusicInfo> musicList, int position) {
				// TODO Auto-generated method stub
				if (musicList != null) {
					PlayListAdapter adapter = new PlayListAdapter(
							PlayerAndLyric.this,
							(ArrayList<MusicInfo>) musicList);
					adapter.setPlayPosition(position);
					playQueue.setAdapter(adapter);
					PlayerAndLyric.this.musicList = (ArrayList<MusicInfo>) musicList;
					setListViewClick();
					String lrcName = musicList.get(position)
							.getLyric_file_name();
					Log.e(PlayerFinal.TAG, "lrcName----->" + lrcName);
					String lrcPath = Environment.getExternalStorageDirectory()
							.getPath() + "/Music/" + lrcName;
					Log.e(PlayerFinal.TAG, "lrcPath------>" + lrcPath);
					String lrcStr = getFromLrcFile(lrcPath);
					// 解析歌词
					ILrcBuilder builder = new DefaultLrcBuilder();
					List<LrcRow> rows = builder.getLrcRows(lrcStr);
					// 设置歌词
					mLrcView.setLrc(rows);
					// 显示当前播放歌曲信息
					title.setText(musicList.get(position).getTitle());
					artist.setText(musicList.get(position).getArtist());
					long l = musicList.get(position).getDuration();
					float longF = (float) l / 1000.0f / 60.0f;
					String longStr = Float.toString(longF);
					String dur[] = longStr.split("\\.");
					duration.setText(dur[0] + ":" + dur[1].substring(0, 2));
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
				} else {
					title.setText("我的音乐");
					artist.setText(" ");
				}

			}
			// lrc字符串拼接
			private String getFromLrcFile(String lrcPath) {
				// TODO Auto-generated method stub

				try {
					InputStreamReader inputReader = new InputStreamReader(
							new FileInputStream(lrcPath));
					BufferedReader bufReader = new BufferedReader(inputReader);
					String line = "";
					String Result = "";
					while ((line = bufReader.readLine()) != null) {
						if (line.trim().equals(""))
							continue;
						Result += line + "\r\n";
					}
					return Result;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "";

			}
		};
	}
	/**
	 * 设置seekbar
	 */
	private void setSeekBar() {
		// TODO Auto-generated method stub
		time_position = (TextView) findViewById(R.id.player_seekbar_time);
		duration = (TextView) findViewById(R.id.player_seekbar_duration);
		seekBar = (SeekBar) findViewById(R.id.player_seekbar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					// 进度条改变，发送广播，回调改变播放时间
					Intent intent = new Intent(PlayerService.ACTION_SEEKBAR);
					intent.putExtra(PlayerFinal.SEEKBAR_PROGRESS, progress);
					sendBroadcast(intent);
				}
			}
		});
	}
	/**
	 * 设置标题栏
	 */
	private void initActionBar() {
		// TODO Auto-generated method stub
		back = (ImageButton) findViewById(R.id.player_actionbar_back);
		list = (ImageButton) findViewById(R.id.player_actionbar_list);
		title = (TextView) findViewById(R.id.player_actionbar_song);
		artist = (TextView) findViewById(R.id.player_actionbar_artist);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (playQueue.getVisibility() == View.VISIBLE) {
					playQueue.setVisibility(View.GONE);
					mLrcView.setVisibility(View.VISIBLE);
				} else {
					playQueue.setVisibility(View.VISIBLE);
					mLrcView.setVisibility(View.GONE);
				}

			}
		});
	}
	
	/**
	 * 设置底部MiniPlayer
	 */
	private void initMiniPlayer() {
		// TODO Auto-generated method stub
		play = (ImageButton) findViewById(R.id.player_play);
		pre = (ImageButton) findViewById(R.id.player_pre);
		next = (ImageButton) findViewById(R.id.player_next);
		mode_btn = (ImageButton) findViewById(R.id.player_mode);
//		volume = (ImageButton) findViewById(R.id.player_volume);
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_BUTTON);
				sendBroadcast(intent);
			}
		});
		pre.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_PREVIOUS);
				sendBroadcast(intent);
			}
		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_PLAY_NEXT);
				sendBroadcast(intent);
			}
		});
		mode_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(PlayerService.ACTION_MODE);
				sendBroadcast(intent);

			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PlayerService.registerStateChangeListener(stateChangeListener);
		PlayerService.registerSeekChangeListener(seekChangeListener);
		PlayerService.registerModeChangeListener(modeChangeListener);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		PlayerService.unRegisterStateChangeListener(stateChangeListener);
		PlayerService.unRegisterSeekChangeListener(seekChangeListener);
		PlayerService.unRegisterModeChangeListener(modeChangeListener);
	}
	/**
	 * listview点击事件
	 */
	private void setListViewClick() {
		// TODO Auto-generated method stub
		playQueue.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				int position = arg2;
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
}
