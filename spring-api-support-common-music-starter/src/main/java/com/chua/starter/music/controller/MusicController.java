package com.chua.starter.music.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistCategoryCatalog;
import com.chua.starter.music.model.MusicPlaylistCategoryResult;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/music")
@Tag(name = "音乐播放器")
@Api(tags = "音乐播放器")
public class MusicController {

    private final MusicFacadeService musicFacadeService;
    private final MusicStreamProxyService musicStreamProxyService;

    @GetMapping("/sources")
    @Operation(summary = "获取音源列表")
    public ReturnResult<List<MusicSourceOption>> listSources() {
        return ReturnResult.ok(musicFacadeService.listSources());
    }

    @GetMapping("/overview")
    @Operation(summary = "获取音乐首页概览")
    public ReturnResult<MusicOverview> overview(@RequestParam(required = false) String source) {
        return ReturnResult.ok(musicFacadeService.getOverview(source));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索歌曲")
    public ReturnResult<MusicSearchResult> search(
            @RequestParam(required = false) String source,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.search(source, keyword, page, pageSize));
    }

    @GetMapping("/search/playlists")
    @Operation(summary = "搜索歌单")
    public ReturnResult<MusicPlaylistSearchResult> searchPlaylists(
            @RequestParam(required = false) String source,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.searchPlaylists(source, keyword, page, pageSize));
    }

    @GetMapping("/playlist/categories")
    @Operation(summary = "获取歌单分类")
    public ReturnResult<MusicPlaylistCategoryCatalog> playlistCategories(
            @RequestParam(required = false) String source) {
        return ReturnResult.ok(musicFacadeService.getPlaylistCategoryCatalog(source));
    }

    @GetMapping("/playlist/category")
    @Operation(summary = "按分类获取歌单")
    public ReturnResult<MusicPlaylistCategoryResult> categoryPlaylists(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String tagId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ReturnResult.ok(musicFacadeService.getCategoryPlaylists(source, tagId, page, pageSize));
    }

    @GetMapping("/playlist/{source}/{playlistId}")
    @Operation(summary = "获取歌单详情")
    public ReturnResult<MusicPlaylistDetail> playlistDetail(
            @PathVariable String source,
            @PathVariable String playlistId) {
        return ReturnResult.ok(musicFacadeService.getPlaylistDetail(source, playlistId));
    }

    @GetMapping("/track/{source}/{trackId}")
    @Operation(summary = "获取歌曲详情")
    public ReturnResult<MusicTrackDetail> trackDetail(
            @PathVariable String source,
            @PathVariable String trackId) {
        return ReturnResult.ok(rewriteTrackStreamUrl(musicFacadeService.getTrackDetail(source, trackId)));
    }

    @GetMapping("/track/{source}/{trackId}/stream")
    @Operation(summary = "代理歌曲音频流")
    public ResponseEntity<StreamingResponseBody> streamTrack(
            @PathVariable String source,
            @PathVariable String trackId,
            @RequestHeader(name = HttpHeaders.RANGE, required = false) String rangeHeader) {
        MusicTrackDetail trackDetail = musicFacadeService.getTrackDetail(source, trackId);
        return musicStreamProxyService.proxy(trackDetail, rangeHeader);
    }

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
