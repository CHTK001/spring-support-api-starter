package com.chua.starter.music.spi.impl;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.music.model.MusicComment;
import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistCategory;
import com.chua.starter.music.model.MusicPlaylistCategoryCatalog;
import com.chua.starter.music.model.MusicPlaylistCategoryGroup;
import com.chua.starter.music.model.MusicPlaylistCategoryResult;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicPlaylistSummary;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;
import com.chua.starter.music.model.MusicTrackSummary;
import com.chua.starter.music.spi.support.AbstractHttpMusicSourceProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Spi("wy")
public class NeteaseMusicSourceProvider extends AbstractHttpMusicSourceProvider {

    private static final Map<Integer, String> CATEGORY_NAMES = new LinkedHashMap<>();

    static {
        CATEGORY_NAMES.put(0, "语种");
        CATEGORY_NAMES.put(1, "风格");
        CATEGORY_NAMES.put(2, "场景");
        CATEGORY_NAMES.put(3, "情感");
        CATEGORY_NAMES.put(4, "主题");
    }

    @Override
    public MusicSourceOption getSource() {
        return MusicSourceOption.builder()
                .code("wy")
                .name("网易云音乐")
                .description("CeruMusic JS SDK - Netease Music")
                .enabled(Boolean.TRUE)
                .build();
    }

    @Override
    public MusicOverview getOverview() {
        MusicPlaylistCategoryCatalog catalog = getPlaylistCategoryCatalog();
        List<String> hotKeywords = new ArrayList<>();
        catalog.getHotTags().forEach(item -> hotKeywords.add(item.getName()));
        return overviewOf(hotKeywords, getCategoryPlaylists("", 1, 12).getPlaylists());
    }

    @Override
    public MusicSearchResult search(String keyword, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        JsonNode root = getJson("https://music.163.com/api/search/get/web?type=1&s=" + encode(keyword)
                + "&offset=" + offset + "&limit=" + pageSize, this::applyDefaultHeaders);
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(path(root, "result", "songs"))) {
            tracks.add(MusicTrackSummary.builder()
                    .trackId(text(item, "id"))
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .artist(joinArtists(item.path("artists")))
                    .album(text(item, "album", "name"))
                    .coverUrl(firstNonBlank(text(item, "album", "picUrl"), text(item, "al", "picUrl")))
                    .durationSeconds(parseMilliseconds(item.path("duration")))
                    .build());
        }
        return searchResult(keyword, page, pageSize, longValue(root, "result", "songCount"), tracks);
    }

    @Override
    public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        JsonNode root = getJson("https://music.163.com/api/search/get/web?type=1000&s=" + encode(keyword)
                + "&offset=" + offset + "&limit=" + pageSize, this::applyDefaultHeaders);
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(path(root, "result", "playlists"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(item, "id"))
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .description(text(item, "description"))
                    .coverUrl(text(item, "coverImgUrl"))
                    .author(text(item, "creator", "nickname"))
                    .trackCount(integer(item, "trackCount"))
                    .accentColor("linear-gradient(135deg, #3b1018 0%, #d1445b 100%)")
                    .playCount(longValue(item, "playCount"))
                    .build());
        }
        return MusicPlaylistSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(root, "result", "playlistCount"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog() {
        JsonNode root = getJson("https://music.163.com/api/playlist/hottags", this::applyDefaultHeaders);
        List<MusicPlaylistCategory> hotTags = new ArrayList<>();
        Map<Integer, List<MusicPlaylistCategory>> grouped = new LinkedHashMap<>();
        for (JsonNode item : elements(path(root, "tags"))) {
            MusicPlaylistCategory tag = MusicPlaylistCategory.builder()
                    .tagId(text(item, "name"))
                    .name(text(item, "name"))
                    .hot(true)
                    .build();
            hotTags.add(tag);
            int category = integer(item, "category");
            grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(tag);
        }

        List<MusicPlaylistCategoryGroup> groups = new ArrayList<>();
        grouped.forEach((category, tags) -> groups.add(MusicPlaylistCategoryGroup.builder()
                .groupId(String.valueOf(category))
                .name(CATEGORY_NAMES.getOrDefault(category, "分类"))
                .tags(tags)
                .build()));

        return MusicPlaylistCategoryCatalog.builder()
                .source(getSource().getCode())
                .hotTags(hotTags)
                .groups(groups)
                .build();
    }

    @Override
    public MusicPlaylistCategoryResult getCategoryPlaylists(String tagId, int page, int pageSize) {
        String category = StringUtils.hasText(tagId) ? tagId : "全部";
        int offset = Math.max(0, page - 1) * pageSize;
        JsonNode root = getJson("https://music.163.com/api/playlist/list?cat=" + encode(category)
                + "&order=hot&offset=" + offset + "&limit=" + pageSize, this::applyDefaultHeaders);
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(path(root, "playlists"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(item, "id"))
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .description(text(item, "description"))
                    .coverUrl(text(item, "coverImgUrl"))
                    .author(text(item, "creator", "nickname"))
                    .trackCount(integer(item, "trackCount"))
                    .accentColor("linear-gradient(135deg, #3b1018 0%, #d1445b 100%)")
                    .playCount(longValue(item, "playCount"))
                    .commentCount(longValue(item, "commentCount"))
                    .build());
        }
        return MusicPlaylistCategoryResult.builder()
                .source(getSource().getCode())
                .tagId(tagId)
                .categoryName(category)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(root, "total"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
        JsonNode root = getJson("https://music.163.com/api/playlist/detail?id=" + encode(playlistId), this::applyDefaultHeaders);
        JsonNode playlist = path(root, "playlist");
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(playlist.path("tracks"))) {
            tracks.add(toTrackSummary(item));
        }
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(text(playlist, "name"))
                .description(text(playlist, "description"))
                .coverUrl(text(playlist, "coverImgUrl"))
                .author(text(playlist, "creator", "nickname"))
                .trackCount(integer(playlist, "trackCount"))
                .tracks(tracks)
                .build();
    }

    @Override
    public MusicTrackDetail getTrackDetail(String trackId) {
        JsonNode detailRoot = getJson("https://music.163.com/api/song/detail/?id=" + encode(trackId)
                + "&ids=" + encode("[" + trackId + "]"), this::applyDefaultHeaders);
        JsonNode song = path(detailRoot, "songs").path(0);
        JsonNode lyricRoot = getJson("https://music.163.com/api/song/lyric?id=" + encode(trackId)
                + "&lv=-1&kv=-1&tv=-1", this::applyDefaultHeaders);
        JsonNode streamRoot = getJson("https://music.163.com/api/song/enhance/player/url?id=" + encode(trackId)
                + "&ids=" + encode("[" + trackId + "]") + "&br=320000", this::applyDefaultHeaders);
        JsonNode commentRoot = getJson("https://music.163.com/api/v1/resource/comments/R_SO_4_" + encode(trackId)
                + "?limit=20&offset=0", this::applyDefaultHeaders);
        List<MusicComment> comments = new ArrayList<>();
        JsonNode commentList = path(commentRoot, "comments");
        if (commentList == null || !commentList.isArray() || commentList.isEmpty()) {
            commentList = path(commentRoot, "hotComments");
        }
        for (JsonNode item : elements(commentList)) {
            comments.add(MusicComment.builder()
                    .commentId(text(item, "commentId"))
                    .author(text(item, "user", "nickname"))
                    .avatar(text(item, "user", "avatarUrl"))
                    .content(text(item, "content"))
                    .likedCount(longValue(item, "likedCount"))
                    .time(firstNonBlank(text(item, "timeStr"), text(item, "time")))
                    .build());
        }
        return MusicTrackDetail.builder()
                .trackId(trackId)
                .source(getSource().getCode())
                .title(text(song, "name"))
                .artist(firstNonBlank(joinArtists(song.path("artists")), joinArtists(song.path("ar"))))
                .album(firstNonBlank(text(song, "album", "name"), text(song, "al", "name")))
                .coverUrl(firstNonBlank(text(song, "album", "picUrl"), text(song, "al", "picUrl")))
                .durationSeconds(parseMilliseconds(firstNonBlank(text(song, "duration"), text(song, "dt"))))
                .streamUrl(text(path(streamRoot, "data").path(0), "url"))
                .lyrics(firstNonBlank(text(lyricRoot, "lrc", "lyric"), text(lyricRoot, "klyric", "lyric")))
                .commentCount(longValue(commentRoot, "total"))
                .comments(comments)
                .build();
    }

    private void applyDefaultHeaders(com.chua.common.support.network.http.HttpClientBuilder builder) {
        builder.userAgent(DESKTOP_UA)
                .referer("https://music.163.com/")
                .origin("https://music.163.com");
    }

    private MusicTrackSummary toTrackSummary(JsonNode item) {
        return MusicTrackSummary.builder()
                .trackId(text(item, "id"))
                .source(getSource().getCode())
                .title(text(item, "name"))
                .artist(firstNonBlank(joinArtists(item.path("ar")), joinArtists(item.path("artists"))))
                .album(firstNonBlank(text(item, "al", "name"), text(item, "album", "name")))
                .coverUrl(firstNonBlank(text(item, "al", "picUrl"), text(item, "album", "picUrl")))
                .durationSeconds(parseMilliseconds(firstNonBlank(text(item, "dt"), text(item, "duration"))))
                .build();
    }

    private String joinArtists(JsonNode node) {
        List<String> names = new ArrayList<>();
        for (JsonNode item : elements(node)) {
            String name = text(item, "name");
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return String.join(" / ", names);
    }

    private int parseMilliseconds(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0;
        }
        return parseMilliseconds(node.asText(""));
    }

    private int parseMilliseconds(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        try {
            long milliseconds = Long.parseLong(value);
            return milliseconds > 1000L ? (int) Math.round(milliseconds / 1000D) : (int) milliseconds;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
