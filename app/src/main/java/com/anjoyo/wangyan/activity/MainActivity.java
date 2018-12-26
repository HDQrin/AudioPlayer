package com.anjoyo.wangyan.activity;

/*
* 我的音乐主页面
* */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.customview.MyHSV;
import com.anjoyo.wangyan.db.MusicDBHelper;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.service.OnPlayerStateChangeListener;
import com.anjoyo.wangyan.service.PlayerService;
import com.anjoyo.wangyan.ui.R;
import com.anjoyo.wangyan.util.ToastShow;
/**
 * 
 * 主界面，有侧滑菜单
 *
 */
public class MainActivity extends Activity {
	/** 自定义滚动条 **/
	public static MyHSV myHSV;
	/** 滚动条的子布局对象 **/
	private LinearLayout myHSV_Linear;
	/** 第二页布局视图对象 **/
	private View mainView, onlineMusicView, aboutView;
	private TextView txtArg;
	/** View数组，加入子视图 **/
	private View[] mainView_Children;
	/** 保存左边布局的宽度 */
	private int leftWidth;
	/** 保存右边布局的宽度 */
	private int rightWidth;

	/** 滚动参数 **/
	private boolean flagMove = false;
	public static int offset = 0;

	private ScrollView slideMenu;
	private ImageButton actionbar_menu, pop_menu;

	// 侧滑菜单跳转view
	private LinearLayout myMusic, onlineMusic, setting, setting_set, quit;
	private RelativeLayout miniPlayer;
	// 我的音乐界面点击跳转view
	private LinearLayout localMusic, folder, artist, album, download, favor,
			playlist;
	private TextView tv_localMusic, tv_folder, tv_artist, tv_album,
			tv_download, tv_favor, tv_playlist;
	// miniplayer中的控件
	private TextView title_mini, artist_mini;
	private ImageView album_mini;
	private ImageButton play, next;

	// 保存设置信息
	private SharedPreferences sharedpre;
	private SharedPreferences.Editor sharedpre_editor;
	private Boolean flagWiFi = false, flagGes = false;
	private ToggleButton wifiToggle;
	private ToggleButton gesToggle;

	// 用于开启服务
	private Intent service;
	// 歌曲播放状态改变的监听器
	// 监听器改变UI
	private OnPlayerStateChangeListener stateChangeListener;
	// 网络歌曲列表
	private ArrayList<MusicInfo> musicList;
	private ListView onlineLv;
	private View bottom;
	private Handler onlineHandler;
	private int curPage = 1;
	private int totalPage = 0;
	private int firstItem, lastItem;
	private Runnable runnable;
	private LinearLayout online_loading;
	private ImageView online_error_img;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.slide_menu_main);
		
		initView();
		loadSetting();
//		setToggle();
		setSlideMenu();
		setMainView(mainView);
		// 启动service
		service = new Intent(this, PlayerService.class);
		startService(service);
		// 监听器改变UI
		stateChangeListener = new OnPlayerStateChangeListener() {

			@Override
			public void onStateChange(int state, int mode,
					List<MusicInfo> musicList, int position) {
				// TODO Auto-generated method stub
				setMainView(mainView);
				// 更改当前界面UI
				if (musicList != null) {
					Log.e(PlayerFinal.TAG, "回调主界面当前UI！！！！！！！！！！");
					if (title_mini == null) {
						Log.e(PlayerFinal.TAG, "找不到title");
					}
					title_mini.setText(musicList.get(position).getTitle());
					Log.e(PlayerFinal.TAG, musicList.get(position).getTitle()
							+ "!!!!!");
					artist_mini.setText(musicList.get(position).getArtist());
					if (musicList.get(position).getAlbum_img_path() != null) {
						Uri uri = Uri.parse(musicList.get(position)
								.getAlbum_img_path());
						album_mini.setImageURI(uri);
					}
				} else {
					title_mini.setText("欢迎来到我的音乐");
					artist_mini.setText("让音乐跟我走");
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
	 * 设置setting按钮监听事件
	 */
	/*private void setToggle() {
		// TODO Auto-generated method stub
		wifiToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				flagWiFi = isChecked;
				saveSetting(flagWiFi, flagGes);
			}
		});
		gesToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				flagGes = isChecked;
				saveSetting(flagWiFi, flagGes);
			}
		});
	}
*/
	/**
	 * 读取设置信息
	 */
	private void loadSetting() {
		// TODO Auto-generated method stub
		sharedpre = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//		flagWiFi = sharedpre.getBoolean("wifi", false);
//		flagGes = sharedpre.getBoolean("gesture", false);
		/*wifiToggle = (ToggleButton) this.findViewById(R.id.setting_wifi_toggle);
		gesToggle = (ToggleButton) this
				.findViewById(R.id.setting_gesture_toggle);*/
		/*wifiToggle.setChecked(flagWiFi);
		gesToggle.setChecked(flagGes);*/
	}

	/**
	 * 保存设置信息
	 */
//	private void saveSetting(Boolean flag_wifi, Boolean flag_ges) {
//		// TODO Auto-generated method stub
//		sharedpre = getSharedPreferences("setting", Activity.MODE_PRIVATE);
//		sharedpre_editor = sharedpre.edit();
//		sharedpre_editor.putBoolean("wifi", flag_wifi);
//		sharedpre_editor.putBoolean("gesture", flag_ges);
//		sharedpre_editor.commit();
//	}

	/**
	 * 设置侧滑菜单监听事件
	 */
	private void setSlideMenu() {
		// TODO Auto-generated method stub
		myMusic = (LinearLayout) this.findViewById(R.id.slide_menu_my_music);
		myMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int width = mainView_Children[0].getMeasuredWidth();
				moveScrollList(width);
				onGlobalLayout(mainView);
				setMainView(mainView);
			}
		});
//		onlineMusic = (LinearLayout) this
//				.findViewById(R.id.slide_menu_online_music);
//		onlineMusic.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				int width = mainView_Children[0].getMeasuredWidth();
//				moveScrollList(width);
//				onGlobalLayout(onlineMusicView);
//				setOnlineMusic(onlineMusicView);
//			}
//		});
		/*aboutView = (LinearLayout) this.findViewById(R.id.slide_menu_about);
		aboutView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int width = mainView_Children[0].getMeasuredWidth();
				moveScrollList(width);
				initView();
				onGlobalLayout(aboutView);
				setAbout(aboutView);
			}
		});*/
		/*setting = (LinearLayout) this.findViewById(R.id.slide_menu_setting);
		setting_set = (LinearLayout) this
				.findViewById(R.id.slide_menu_setting_set);
		setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (setting_set.getVisibility() == View.VISIBLE) {
					setting_set.setVisibility(View.GONE);
				} else {
					setting_set.setVisibility(View.VISIBLE);
				}
			}
		});*/
		quit = (LinearLayout) this.findViewById(R.id.slide_menu_quit);
		quit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});

	}

	private void initView() {
		// TODO Auto-generated method stub
		// 自定义
		myHSV = (MyHSV) findViewById(R.id.main_myHSV);
		// 第二布局中的linearlaout
		myHSV_Linear = (LinearLayout) findViewById(R.id.main_myHSV_linear);
		// 第一布局的linearlayout
		slideMenu = (ScrollView) findViewById(R.id.main_scrollview);
		// 动态生成一个TextView
		txtArg = new TextView(this);
		// 第二布局
		mainView = LayoutInflater.from(this).inflate(R.layout.activity_main,
				null);
//		onlineMusicView = LayoutInflater.from(this).inflate(R.layout.online_music, null);
//		aboutView = LayoutInflater.from(this).inflate(R.layout.about, null);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		onGlobalLayout(mainView);
	}

	private void onGlobalLayout(View v) {
		// TODO Auto-generated method stub
		mainView_Children = new View[] { txtArg, v };
		leftWidth = slideMenu.getMeasuredWidth();
		final int w = myHSV.getMeasuredWidth();
		final int h = myHSV.getMeasuredHeight();
		rightWidth = w - leftWidth;// 获得剩余部分的宽度
		System.out.println("leftWidth------" + leftWidth + "w----" + w
				+ "  ----h----" + h);
		myHSV_Linear.removeAllViews();
		int[] dims = new int[2];
		for (int i = 0; i < mainView_Children.length; i++) {
			getViewSize(i, w, h, dims);
			myHSV_Linear.addView(mainView_Children[i], dims[0], dims[1]);
		}

		myHSV.setBtnWith(rightWidth);
		myHSV.setAppWidth(w);
	}

	/**
	 * 获取各个View视图的宽高
	 */
	public void getViewSize(int idx, int w, int h, int[] dims) {
		dims[0] = w;
		dims[1] = h;
		final int menuIdx = 0;
		if (idx == menuIdx) {
			dims[0] = w - rightWidth;
		}
		System.out.println("idx---" + idx + "------w---" + dims[0]
				+ "------h----" + dims[1]);

	}

	/**
	 * 顶部按钮左右移动
	 * 
	 * @param width
	 */
	public void moveScrollList(int width) {
		int menuWidth = width;

		if (flagMove) {
			// Scroll to 0 to reveal menu
			offset = 0;
			myHSV.smoothScrollTo(offset, 0);
		} else {
			// Scroll to menuWidth so menu isn't on screen.
			offset = menuWidth;
			myHSV.smoothScrollTo(offset, 0);
		}
		flagMove = !flagMove;
	}

	/**
	 * 设置按键监听事件
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			int width = mainView_Children[0].getMeasuredWidth();
			moveScrollList(width);
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 设置主界面监听事件
	 * 
	 * @param v
	 */
	private void setMainView(View v) {
		// TODO Auto-generated method stub

		actionbar_menu = (ImageButton) v.findViewById(R.id.main_actionbar_menu);
		pop_menu = (ImageButton) v.findViewById(R.id.main_actionbar_scan);
		actionbar_menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int width = mainView_Children[0].getMeasuredWidth();
				moveScrollList(width);
			}
		});
		pop_menu.setVisibility(View.INVISIBLE);
		pop_menu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		localMusic = (LinearLayout) v.findViewById(R.id.main_local_music_item);
		folder = (LinearLayout) v.findViewById(R.id.main_folder_item);
		artist = (LinearLayout) v.findViewById(R.id.main_artist_item);
		album = (LinearLayout) v.findViewById(R.id.main_album_item);
//		download = (LinearLayout) v.findViewById(R.id.main_download_item);
//		download.setVisibility(View.GONE);
//		favor = (LinearLayout) v.findViewById(R.id.main_favor_item);
		playlist = (LinearLayout) v.findViewById(R.id.main_playlist_item);
		// 得到miniplayer上的控件
		title_mini = (TextView) v.findViewById(R.id.main_miniplayer_song);
		artist_mini = (TextView) v.findViewById(R.id.main_miniplayer_artist);
		album_mini = (ImageView) v.findViewById(R.id.main_miniplayer_album);
		play = (ImageButton) v.findViewById(R.id.main_miniplayer_play);
		next = (ImageButton) v.findViewById(R.id.main_miniplayer_next);
		miniPlayer = (RelativeLayout) v
				.findViewById(R.id.main_miniplayer_layout);

		tv_localMusic = (TextView) v.findViewById(R.id.main_local_music_text);
		tv_folder = (TextView) v.findViewById(R.id.main_folder_text);
		tv_artist = (TextView) v.findViewById(R.id.main_artist_text);
		tv_album = (TextView) v.findViewById(R.id.main_album_text);
//		tv_download = (TextView) v.findViewById(R.id.main_download_text);
//		tv_favor = (TextView) v.findViewById(R.id.main_favor_text);
		tv_playlist = (TextView) v.findViewById(R.id.main_playlist_text);
		int[] data = initData();
		tv_localMusic.setText(data[0] + "首");
//		tv_favor.setText(data[1] + "首");
		localMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, LocalMusic.class);
				startActivity(intent);
			}
		});
		folder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		artist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						ArtistSelect.class);
				startActivity(intent);
			}
		});
		album.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, AlbumSelect.class);
				startActivity(intent);
			}
		});
		/*download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});*/
		/*favor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, FavorMusic.class);
				startActivity(intent);
			}
		});*/
		playlist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		miniPlayer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
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
	 * 查询数据库，用于我的音乐界面，UI显示数据
	 * 
	 * @return int[]
	 */
	private int[] initData() {
		// TODO Auto-generated method stub
		MusicDBHelper localDbHelper = new MusicDBHelper(MainActivity.this);
		Cursor curLocal = localDbHelper.queryLocalByID();
		Cursor curFav = localDbHelper.queryFavByID();
		Cursor curArtist = localDbHelper.queryArtistByID();
		Cursor curAlbum = localDbHelper.queryAlbumByID();
		int[] dataSum = new int[] { curLocal.getCount(),
				curArtist.getCount(), curAlbum.getCount() };
		curLocal.close();
		curFav.close();
		curArtist.close();
		curAlbum.close();
		localDbHelper.close();
		return dataSum;
	}

	/**
	 * 设置在线音乐界面
	 * 
	 * @param v
	 */
//	private void setOnlineMusic(View v) {
//		actionbar_menu = (ImageButton) v
//				.findViewById(R.id.online_actionbar_menu);
//		actionbar_menu.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				int width = mainView_Children[0].getMeasuredWidth();
//				moveScrollList(width);
//			}
//		});
//		onlineLv = (ListView) v.findViewById(R.id.online_listview);
//		online_error_img = (ImageView) v
//				.findViewById(R.id.online_network_error);
//		online_loading = (LinearLayout) v.findViewById(R.id.online_loading);
//		// 设置加载图片旋转动画
//		RotateAnimation anim = new RotateAnimation(0, 360,
//				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
//				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//		anim.setDuration(1000);
//		anim.setInterpolator(new LinearInterpolator());
//		anim.setRepeatCount(-1);
//		ImageView loadImg = (ImageView) v.findViewById(R.id.online_loading_img);
//		loadImg.startAnimation(anim);
//
//		bottom = LayoutInflater.from(this).inflate(
//				R.layout.online_listview_end, null);
//		// onlineLv.addHeaderView(bottom);
//		onlineLv.addFooterView(bottom);
//		onlineLv.setOnScrollListener(new OnScrollListener() {
//
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				// TODO Auto-generated method stub
//				if (lastItem == 10) {// head 11
//					curPage++;
//					// 开启线程，加载数据
//					new Thread(runnable).start();
//				}
//				// if (firstItem == 0) {
//				// if (curPage > 1) {
//				// curPage--;
//				// }
//				// new Thread(runnable).start();
//				// }
//
//			}
//
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				// TODO Auto-generated method stub
//				firstItem = firstVisibleItem;
//				lastItem = totalItemCount - 1;// 因为加上了页脚
//			}
//		});
//		onlineHandler = new Handler() {
//			public void handleMessage(android.os.Message msg) {
//				switch (msg.what) {
//				case 0:
//					musicList = (ArrayList<MusicInfo>) msg.obj;
//					online_loading.setVisibility(View.GONE);
//					onlineLv.setVisibility(View.VISIBLE);
//					if (musicList != null) {
//						adapter = new OnlineListAdapter(MainActivity.this,
//								musicList);
//						onlineLv.setAdapter(adapter);
//						adapter.notifyDataSetChanged();
//					} else {
//						ToastShow.toastShow(MainActivity.this, "未获取到网络歌曲信息");
//					}
//					break;
//				case 1:
////					ToastShow.toastShow(MainActivity.this, "开始下载");
//					break;
//				case 2:
////					ToastShow.toastShow(MainActivity.this, "下载完毕");
//					break;
//				case 3:
//					ToastShow.toastShow(MainActivity.this, "请连接网络");
//					online_loading.setVisibility(View.GONE);
//					online_error_img.setVisibility(View.VISIBLE);
//					break;
//				case 4:
//					// 下载网络歌曲歌词
//
//					break;
//				case 5:
//					ToastShow.toastShow(MainActivity.this, "请开启wifi");
//					online_loading.setVisibility(View.GONE);
//					online_error_img.setVisibility(View.VISIBLE);
//					break;
//				}
//
//			};
//		};
//		runnable = new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				Looper.prepare();
//				// 判断网络仅限wifi时，wifi可用，执行联网获取歌曲列表
//				// 未设置仅限wifi时，只要网络可用就执行联网操作
//				if ((flagWiFi && NetWorkHelper.isWifi(getApplicationContext()))
//						|| (!flagWiFi && NetWorkHelper
//								.isNetWorkAvaliable(getApplicationContext()))) {
//					ParseHelper helper = new ParseHelper();
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					totalPage = helper.getMusicTotalPage();
//					Log.e(PlayerFinal.TAG, "curPage----->" + curPage
//							+ "totalPage--->" + totalPage);
//					if (curPage <= totalPage) {
//						musicList = helper.getMusicListByPage(curPage);
//					}
//					onlineHandler.sendMessage(onlineHandler.obtainMessage(0,
//							musicList));
//				} else if (!NetWorkHelper
//						.isNetWorkAvaliable(getApplicationContext())) {
//					// 网络不可用时，发消息，提示用户网络不可用
//					onlineHandler.sendMessage(onlineHandler.obtainMessage(3));
//				} else if ((flagWiFi && !NetWorkHelper
//						.isWifi(getApplicationContext()))) {
//					// 网络不可用时，发消息，提示用户网络不可用
//					onlineHandler.sendMessage(onlineHandler.obtainMessage(5));
//				}
//				Looper.loop();
//			}
//		};
//		new Thread(runnable).start();
//		onlineLv.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setAction(PlayerService.ACTION_PLAY_ITEM);
//				intent.putParcelableArrayListExtra(PlayerFinal.PLAYER_LIST,
//						musicList);
//				intent.putExtra(PlayerFinal.PLAYER_POSITION, arg2);
//				// 用于判断歌曲是网络还是本地
//				intent.putExtra(PlayerFinal.PLAYER_WHERE, "internet");
//				sendBroadcast(intent);
//				// 下载歌词
//				String HttpUrl = ParseHelper.IP + "/res/music/"
//						+ musicList.get(arg2).getLyric_file_name();
//				Log.e(PlayerFinal.TAG, "lrcPath------->" + HttpUrl);
//				ThreadPoolUtilsImg
//						.execute(new LrcDownThread(HttpUrl, onlineHandler,
//								musicList.get(arg2).getLyric_file_name()));
//			}
//		});
//	}

	/**
	 * 设置关于页面
	 * 00
	 * @param v
	 */
//	private void setAbout(View v) {
//		actionbar_menu = (ImageButton) v
//				.findViewById(R.id.about_actionbar_menu);
//		actionbar_menu.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				int width = mainView_Children[0].getMeasuredWidth();
//				moveScrollList(width);
//			}
//		});
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 改变当前界面UI
		int[] data = initData();
		tv_localMusic.setText(data[0] + "首");
//		tv_favor.setText(data[1] + "首");
		tv_artist.setText(data[1] + "个歌手");
		tv_album.setText(data[2] + "张专辑");
		// 注册播放状态改变的监听器
		PlayerService.registerStateChangeListener(stateChangeListener);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 解除注册状态改变监听器
		PlayerService.unRegisterStateChangeListener(stateChangeListener);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stopService(service);
		super.onDestroy();
	}
}
