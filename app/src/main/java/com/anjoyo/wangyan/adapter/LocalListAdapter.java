package com.anjoyo.wangyan.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anjoyo.wangyan.constant.DbFinal;
import com.anjoyo.wangyan.db.MusicDBHelper;
import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.service.PlayerService;
import com.anjoyo.wangyan.ui.R;
import com.anjoyo.wangyan.util.ToastShow;
/**
 * 本地音乐自定义adapter
 */
public class LocalListAdapter extends BaseAdapter {
	// 上下文
	private Context context;
	// 数据源
	private Cursor cursor;
	// 用于判断checkbox
	private List<Boolean> popCheckStatus;
	// 正在播放的歌曲位置
	private int playPosition = -1;
	/**
	 * 构造方法
	 * @param context
	 * @param cursor
	 */

	public LocalListAdapter(Context context, Cursor cursor) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.cursor = cursor;
		if (cursor != null) {
			popCheckStatus = new ArrayList<Boolean>();
			for (int i = 0; i < cursor.getCount(); i++) {
				popCheckStatus.add(false);
			}
		}

	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cursor.getCount();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return cursor.moveToPosition(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.local_music_listview_item, null);
			holder.title = (TextView) convertView
					.findViewById(R.id.localmusic_listitem_song_text);
			holder.artist = (TextView) convertView
					.findViewById(R.id.localmusic_listitem_artist_text);
			holder.popCheck = (CheckBox) convertView
					.findViewById(R.id.localmusic_listitem_action);
			holder.popdown = (LinearLayout) convertView
					.findViewById(R.id.local_popdown);
			/*holder.favor = (Button) convertView
					.findViewById(R.id.local_popdown_favor);*/
			holder.detail = (Button) convertView
					.findViewById(R.id.local_popdown_detail);
			holder.ringtone = (Button) convertView
					.findViewById(R.id.local_popdown_ringtone);
			holder.delete = (Button) convertView
					.findViewById(R.id.local_popdown_del);
			holder.indicator = (ImageView) convertView
					.findViewById(R.id.localmusic_listitem_indicator);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == playPosition) {
			holder.indicator.setVisibility(View.VISIBLE);
		} else {
			holder.indicator.setVisibility(View.INVISIBLE);
		}
		cursor.moveToPosition(position);
		holder.title.setText(cursor.getString(cursor
				.getColumnIndex(DbFinal.LOCAL_TITLE)));
		holder.artist.setText(cursor.getString(cursor
				.getColumnIndex(DbFinal.LOCAL_ARTIST)));
		holder.popdown.setVisibility(View.GONE);
		if (popCheckStatus.get(position)) {
			holder.popdown.setVisibility(View.VISIBLE);
		} else {
			holder.popdown.setVisibility(View.GONE);
		}
		holder.popCheck.setTag(position);
//		holder.favor.setTag(position);
		holder.detail.setTag(position);
		holder.ringtone.setTag(position);
		holder.delete.setTag(position);
		holder.popCheck.setChecked(popCheckStatus.get(position));
		// ckeckbox为选中时，显示popdown
		holder.popCheck
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						popCheckStatus.set((Integer) buttonView.getTag(),
								isChecked);
						if (isChecked) {
							holder.popdown.setVisibility(View.VISIBLE);
						} else {
							holder.popdown.setVisibility(View.GONE);
						}
					}
				});
		// 设置我喜欢按钮点击事件
		/*holder.favor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("position~~~~~~" + v.getTag());
				cursor.moveToPosition((Integer) v.getTag());
				MusicInfo music = new MusicInfo();
				music.setId(cursor.getInt(cursor
						.getColumnIndex(DbFinal.LOCAL_ID)));
				MusicDBHelper dBHelper = new MusicDBHelper(context);
				// 插入我喜欢数据库的是本地音乐库的id
				Long i = dBHelper.insertFav(music);
				Log.e("MyDuomi", "i------>" + i);
				Log.e("MyDuomi",
						"ID------>"
								+ cursor.getInt(cursor
										.getColumnIndex(DbFinal.LOCAL_ID)));
				if (i == -1) {
					int j = dBHelper.delFav(music.getId());
					if (j != 0) {
						// 移出收藏数据库
						ToastShow.toastShow(context, "取消我喜欢");
					}
				} else {
					// 收藏成功
					ToastShow.toastShow(context, "我喜欢");
				}
			}
		});*/
		// 设置歌曲信息显示按钮
		holder.detail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int p = (Integer) v.getTag();
				MusicDBHelper helper = new MusicDBHelper(context);
				ArrayList<MusicInfo> musicList = helper
						.getMusicListFromLocal(cursor);
				MusicInfo m = musicList.get(p);
				String title = m.getTitle();
				String artist = m.getArtist();
				String album = m.getAlbum();
				String size = m.getSize() / 1024.0f / 1024.0f + "";
				size = size.substring(0, 3) + "M";
				String duration = m.getDuration() / 1000.0f / 60.0f + "";
				duration = duration.substring(0, 4) + "分";
				String path = m.getPath();
				ToastShow.toastShow(context, "歌曲名称：" + title + "\n歌手：" + artist
						+ "\n专辑：" + album + "\n歌曲大小：" + size + "\n歌曲时长："
						+ duration + "\n文件路径：" + path);
			}
		});
		// 设置铃声按钮监听事件
		holder.ringtone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int p = (Integer) v.getTag();
				MusicDBHelper helper = new MusicDBHelper(context);
				ArrayList<MusicInfo> musicList = helper
						.getMusicListFromLocal(cursor);
				MusicInfo m = musicList.get(p);
				String path = m.getPath();
				File f = new File(path);
				// 调用设置铃声方法
				setMyRingtone(f);
			}
		});
		// 删除按钮，弹出对话框
		holder.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int p = (Integer) v.getTag();
				delDialog(p);
			}
		});
		return convertView;
	}

	private class ViewHolder {
		TextView title;
		TextView artist;
		CheckBox popCheck;
		LinearLayout popdown;
		Button favor, detail, ringtone, delete;
		ImageView indicator;
	}

	// 删除对话框
	private void delDialog(int p) {
		// TODO Auto-generated method stub
		final int position = p;
		Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("删除确认");
		builder.setMessage("确定删除？");
		builder.setNegativeButton("取消", null);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				MusicDBHelper dBHelper = new MusicDBHelper(context);
				cursor.moveToPosition(position);
				String title = cursor.getString(cursor
						.getColumnIndex(DbFinal.LOCAL_TITLE));
				// 从本地音乐数据库删除
				int i = dBHelper.delLocal(title);
				if (0 == i) {
					ToastShow.toastShow(context, "删除失败");
				} else {
					ToastShow.toastShow(context, "删除成功");
				}
				cursor = dBHelper.queryLocalByID();
				if (cursor != null) {
					popCheckStatus = new ArrayList<Boolean>();
					for (int j = 0; j < cursor.getCount(); j++) {
						popCheckStatus.add(false);
					}
				}
				// 没有用？
				PlayerService.serviceMusicList = dBHelper
						.getMusicListFromLocal(cursor);
				notifyDataSetChanged();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
	}

	public void setPlayPosition(int playPosition) {
		this.playPosition = playPosition;
	}

	public int getPlayPosition() {
		return playPosition;
	}

	/**
	 * 设置铃声
	 * @param file
	 */
	private void setMyRingtone(File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(file
				.getAbsolutePath());
		Uri newUri = context.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(context,
				RingtoneManager.TYPE_RINGTONE, newUri);
		ToastShow.toastShow(context, "设置铃声成功");
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
		notifyDataSetChanged();
	}

}
