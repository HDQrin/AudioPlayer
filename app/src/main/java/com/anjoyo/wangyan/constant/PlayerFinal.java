package com.anjoyo.wangyan.constant;

/**
 * 播放歌曲常量类
 */
public class PlayerFinal {
	public static final String TAG = "MyDuomi";
	public static final String STATE = "state";// 状态
	public static final int STATE_WAIT = 0;// 等待状态
	public static final int STATE_PLAY = 1;// 播放状态
	public static final int STATE_PAUSE = 2;// 暂停状态
	public static final int STATE_STOP = 3;// 停止状态
	public static final int STATE_CONTINUE = 4;// 继续播放状态
	public static final int STATE_PRE = 5;// 上一首
	public static final int STATE_NEXT = 6;// 下一首

	public static final String PLAYER_WHERE = "where";// 用于传参数
	public static final String PLAYER_POSITION = "position";// 用于传参数
	public static final String PLAYER_LIST = "musicList";
	public static final String SEEKBAR_PROGRESS = "progress";
	public static final String LOCAL_ARTIST = "artist";
	public static final String LOCAL_ALBUM = "album";

	public static final int MODE_RANDOM = 0;// 随机播放
	public static final int MODE_SINGLE = 1;// 单曲循环
	public static final int MODE_ORDER = 2;// 顺序播放
	public static final int MODE_LOOP = 3;// 循环播放

}
