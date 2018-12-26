package com.anjoyo.wangyan.service;

import java.util.List;

import com.anjoyo.wangyan.entity.MusicInfo;

public interface OnPlayerStateChangeListener {
	void onStateChange(int state, int mode, List<MusicInfo> musicList,
			int position);
}
