package com.chua.starter.music.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.data.support.music.model.MusicOverview;
import com.chua.data.support.music.model.MusicPlaylistCategoryCatalog;
import com.chua.data.support.music.model.MusicPlaylistCategoryResult;
import com.chua.data.support.music.model.MusicPlaylistDetail;
import com.chua.data.support.music.model.MusicPlaylistSearchResult;
import com.chua.data.support.music.model.MusicSearchResult;
import com.chua.data.support.music.model.MusicSourceOption;
import com.chua.data.support.music.model.MusicTrackDetail;
import com.chua.starter.music.service.MusicFacadeService;
import com.chua.starter.music.service.MusicStreamProxyService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.util.StringUtils;

import java.util.List;
/** @作者 CH */

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/music")
@Tag(name = "音乐播放器")
@Api(tags = "音乐播放器")
public class MusicController {

    /** Musicfacade服务 */
    private final MusicFacadeService musicFacadeService;
    /** Music流代理服务 */
    private final MusicStreamProxyService musicStreamProxyService;

    @GetMapping("/sources")
    @Operation(summary = "获取音源列表")
    /**
    * 列表源
    *
    * @return 列表源的结果
     */
    public ReturnResult<List<MusicSourceOption>> listSources() {
        return ReturnResult.ok(musicFacadeService.listSources());
    }

    @GetMapping("/overview")
    @Operation(summary = "获取音乐首页概览")
    /**
    * Overview
    *
    * @param false false
    * @return overview的结果
     */
    public ReturnResult<MusicOverview> overview(@RequestParam(required = false) String source) {
        return ReturnResult.ok(musicFacadeService.getOverview(source));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索歌曲")
    /**
    * 搜索
    * @param source 源
    * @param keyword keyword
    * @param page page
    * @param pageSize page大小
     */
    public ReturnResult<MusicSearchResult> search(
            @RequestParam(required = false) String source,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.search(source, keyword, page, pageSize));
    }

    @GetMapping("/search/playlists")
    @Operation(summary = "搜索歌单")
    /**
    * 搜索Playlists
    * @param source 源
    * @param keyword keyword
    * @param page page
    * @param pageSize page大小
     */
    public ReturnResult<MusicPlaylistSearchResult> searchPlaylists(
            @RequestParam(required = false) String source,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.searchPlaylists(source, keyword, page, pageSize));
    }

    @GetMapping("/playlist/categories")
    @Operation(summary = "获取歌单分类")
    /**
    * playlist分类
    * @param source 源
     */
    public ReturnResult<MusicPlaylistCategoryCatalog> playlistCategories(
            @RequestParam(required = false) String source) {
        return ReturnResult.ok(musicFacadeService.getPlaylistCategoryCatalog(source));
    }

    @GetMapping("/playlist/category")
    @Operation(summary = "按分类获取歌单")
    /**
    * 分类playlists
    * @param source 源
    * @param tagId 标签id
    * @param page page
    * @param pageSize page大小
     */
    public ReturnResult<MusicPlaylistCategoryResult> categoryPlaylists(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String tagId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.getCategoryPlaylists(source, tagId, page, pageSize));
    }

    @GetMapping("/playlist/{source}/{playlistId}")
    @Operation(summary = "获取歌单详情")
    /**
    * Playlist详情
    * @param source 源
    * @param playlistId playlistid
     */
    public ReturnResult<MusicPlaylistDetail> playlistDetail(
            @PathVariable String source,
            @PathVariable String playlistId) {
        return ReturnResult.ok(musicFacadeService.getPlaylistDetail(source, playlistId));
    }

    @GetMapping("/track/{source}/{trackId}")
    @Operation(summary = "获取歌曲详情")
    /**
    * Track详情
    * @param source 源
    * @param trackId trackid
     */
    public ReturnResult<MusicTrackDetail> trackDetail(
            @PathVariable String source,
            @PathVariable String trackId) {
        return ReturnResult.ok(rewriteTrackStreamUrl(musicFacadeService.getTrackDetail(source, trackId)));
    }

    @GetMapping("/track/{source}/{trackId}/stream")
    @Operation(summary = "代理歌曲音频流")
    /**
    * 流式输出Track
    * @param source 源
    * @param trackId trackid
    * @param rangeHeader 范围头部
     */
    public ResponseEntity<StreamingResponseBody> streamTrack(
            @PathVariable String source,
            @PathVariable String trackId,
            @RequestHeader(name = HttpHeaders.RANGE, required = false) String rangeHeader) {
        MusicTrackDetail trackDetail = musicFacadeService.getTrackDetail(source, trackId);
        return musicStreamProxyService.proxy(trackDetail, rangeHeader);
    }

    /**
    * rewritetrack流url
    *
    * @param trackDetail trackDetail
    * @return rewritetrack流url的结果
     */
    private MusicTrackDetail rewriteTrackStreamUrl(MusicTrackDetail trackDetail) {
        String streamUrl = trackDetail.getStreamUrl();
        if (StringUtils.hasText(streamUrl)) {
            streamUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/v1/music/track/{source}/{trackId}/stream")
                    .buildAndExpand(trackDetail.getSource(), trackDetail.getTrackId())
                    .toUriString();
        }
        return MusicTrackDetail.builder()
                .trackId(trackDetail.getTrackId())
                .source(trackDetail.getSource())
                .title(trackDetail.getTitle())
                .artist(trackDetail.getArtist())
                .album(trackDetail.getAlbum())
                .coverUrl(trackDetail.getCoverUrl())
                .streamUrl(streamUrl)
                .durationSeconds(trackDetail.getDurationSeconds())
                .lyrics(trackDetail.getLyrics())
                .playCount(trackDetail.getPlayCount())
                .commentCount(trackDetail.getCommentCount())
                .comments(trackDetail.getComments())
                .build();
    }
}

