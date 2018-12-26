package com.anjoyo.wangyan.entity;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Serializable, Parcelable {
	/**
	 * 歌曲实体类
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String artist;
	private String album;
	private String path;
	private Long duration;
	private Long size;
	private String album_img_path;
	private String lyric_file_name;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(this.title);
		dest.writeString(this.artist);
		dest.writeString(this.album);
		dest.writeString(this.path);
		dest.writeLong(this.duration);
		dest.writeLong(this.size);
		dest.writeString(this.album_img_path);
		dest.writeString(this.lyric_file_name);
	}

	public void setAlbum_img_path(String album_img_path) {
		this.album_img_path = album_img_path;
	}

	public String getAlbum_img_path() {
		return album_img_path;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setLyric_file_name(String lyric_file_name) {
		this.lyric_file_name = lyric_file_name;
	}

	public String getLyric_file_name() {
		return lyric_file_name;
	}

	public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {

		@Override
		public MusicInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			MusicInfo music = new MusicInfo();
			music.setTitle(source.readString());
			music.setArtist(source.readString());
			music.setAlbum(source.readString());
			music.setPath(source.readString());
			music.setDuration(source.readLong());
			music.setSize(source.readLong());
			music.setAlbum_img_path(source.readString());
			music.setLyric_file_name(source.readString());
			return music;
		}

		@Override
		public MusicInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new MusicInfo[size];
		}
	};

}