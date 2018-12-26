package com.anjoyo.wangyan.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anjoyo.wangyan.entity.MusicInfo;
import com.anjoyo.wangyan.ui.R;

/**
 * 播放界面，播放列表显示
 */
public class PlayListAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<MusicInfo> musicList;

	private int playPosition = -1;

	public PlayListAdapter(Context context, ArrayList<MusicInfo> musicList) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.musicList = musicList;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musicList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return musicList.get(arg0);
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
					R.layout.player_music_listview_item, null);
			holder.title = (TextView) convertView
					.findViewById(R.id.player_listitem_song_text);
			holder.artist = (TextView) convertView
					.findViewById(R.id.player_listitem_artist_text);
			convertView.setTag(holder);
			holder.title.setTag(position);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.title.setText(musicList.get(position).getTitle());
		holder.artist.setText(musicList.get(position).getArtist());
		int p = (Integer) holder.title.getTag();
		if (playPosition == p) {
			holder.title.setTextColor(context.getResources().getColor(
					R.color.lightgreen));
			holder.artist.setTextColor(context.getResources().getColor(
					R.color.lightgreen));
		}
		return convertView;
	}

	public void setPlayPosition(int playPosition) {
		this.playPosition = playPosition;
	}

	public int getPlayPosition() {
		return playPosition;
	}

	private class ViewHolder {
		TextView title;
		TextView artist;
	}
}
