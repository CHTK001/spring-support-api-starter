package com.chua.starter.music.spi;

import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;

/**
 * 音乐音源 SPI。
 */
public interface MusicSourceProvider {

    /**
     * 当前音源基础信息。
     */
    MusicSourceOption getSource();

    /**
     * 首页概览。
     */
    MusicOverview getOverview();

    /**
     * 搜索歌曲。
     */
    MusicSearchResult search(String keyword, int page, int pageSize);

    /**
     * 搜索歌单。
     */
    MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize);

    /**
     * 读取歌单详情。
     */
    MusicPlaylistDetail getPlaylistDetail(String playlistId);

    /**
     * 读取歌曲播放详情。
     */
    MusicTrackDetail getTrackDetail(String trackId);
}
