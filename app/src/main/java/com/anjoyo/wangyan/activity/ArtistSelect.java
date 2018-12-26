package com.anjoyo.wangyan.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.anjoyo.wangyan.constant.DbFinal;
import com.anjoyo.wangyan.constant.PlayerFinal;
import com.anjoyo.wangyan.db.MusicDBHelper;
import com.anjoyo.wangyan.ui.R;

/**
 * 本地歌曲列表界面
 */
public class ArtistSelect extends Activity {
	// 歌曲显示listview
	private ListView lvMusic;
	// 无歌曲时显示的图片
	private ImageView nothingImg;
	// 歌曲adapter
	private SimpleAdapter adapter;
	// 标题栏处的按钮
	private ImageButton back, scan;
	// 查询本地数据库得到的cursor
	private Cursor curLocal;
	// 数据库工具类
	private MusicDBHelper localDbHelper;
	// 用于传参数给service
	private ArrayList<Map<String, String>> data;

	// 匿名内部类handler用于listview显示
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			curLocal = (Cursor) msg.obj;
			// 扫描到歌曲，图片设置为gone，listview显示
			if (curLocal.getCount() != 0) {
				nothingImg.setVisibility(View.GONE);
				lvMusic.setVisibility(View.VISIBLE);
				data = getListFromArtist(curLocal);
				adapter = new SimpleAdapter(ArtistSelect.this, data,
						R.layout.artist_list_item,
						new String[] { "artist" },
						new int[] { R.id.artist_select_tv });
				lvMusic.setAdapter(adapter);
				// 关闭cursor和数据库
				curLocal.close();
				localDbHelper.close();
			} else {
				nothingImg.setVisibility(View.VISIBLE);
				lvMusic.setVisibility(View.GONE);
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local_music);
		initListView();
		initButton();
		setListViewClick();
	}

	/**
	 * 将得到的cursor转换成list
	 * 
	 * @param curLocal
	 * @return ArrayList<Map<String, String>>
	 */
	private ArrayList<Map<String, String>> getListFromArtist(Cursor curLocal) {
		// TODO Auto-generated method stub
		if (curLocal.getCount() != 0) {
			curLocal.moveToFirst();
			data = new ArrayList<Map<String, String>>();
			do {
				Map<String, String> map = new HashMap<String, String>();
				map.put("artist", curLocal.getString(curLocal
						.getColumnIndex(DbFinal.ARTIST_LOCAL_ARTIST)));
				data.add(map);
				curLocal.moveToNext();
			} while (!curLocal.isAfterLast());
			return data;
		}
		return null;
	}

	/**
	 * listView监听事件
	 */
	private void setListViewClick() {
		// TODO Auto-generated method stub
		lvMusic.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ArtistSelect.this,
						ArtistLocalMusic.class);
				intent.putExtra(PlayerFinal.LOCAL_ARTIST, data.get(position)
						.get("artist"));
				startActivity(intent);
			}
		});
	}

	/**
	 * 刚进入时得到listview控件，开线程查询数据库
	 */
	private void initListView() {
		// TODO Auto-generated method stub
		nothingImg = (ImageView) findViewById(R.id.local_nothing_img);
		nothingImg.setImageResource(R.drawable.error_nosimilar_artist);
		lvMusic = (ListView) findViewById(R.id.local_listview);
		localDbHelper = new MusicDBHelper(ArtistSelect.this);
		new Thread() {
			public void run() {
				curLocal = localDbHelper.queryArtistByID();
				handler.sendMessage(handler.obtainMessage(0, curLocal));
			};
		}.start();

	}

	/**
	 * 设置按钮点击事件
	 */
	private void initButton() {
		// TODO Auto-generated method stub
		back = (ImageButton) findViewById(R.id.local_actionbar_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		scan = (ImageButton) findViewById(R.id.local_actionbar_scan);
		scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}

		});
		TextView title = (TextView) findViewById(R.id.local_actionbar_title);
		title.setText("歌手");
		RelativeLayout miniPlayer = (RelativeLayout) findViewById(R.id.local_miniplayer_layout);
		miniPlayer.setVisibility(View.GONE);
		scan.setVisibility(View.INVISIBLE);
	}

}
