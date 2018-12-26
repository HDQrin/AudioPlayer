package com.anjoyo.wangyan.lyric;

import java.util.List;


public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}
