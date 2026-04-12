package com.chua.starter.music.spi.impl;

import com.chua.common.support.core.annotation.Spi;
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
import com.chua.starter.music.spi.support.MiguSignSupport;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Spi("mg")
public class MiguMusicSourceProvider extends AbstractHttpMusicSourceProvider {

    @Override
    public MusicSourceOption getSource() {
        return MusicSourceOption.builder()
                .code("mg")
                .name("咪咕音乐")
                .description("CeruMusic JS SDK - Migu Music")
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
        JsonNode root = getJson("https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=0&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize="
                + pageSize + "&text=" + encode(keyword) + "&pageNo=" + page + "&sort=0&sid=USS",
                builder -> applySearchHeaders(builder, keyword));
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode group : elements(path(root, "songResultData", "resultList"))) {
            for (JsonNode item : elements(group)) {
                String copyrightId = text(item, "copyrightId");
                if (!StringUtils.hasText(copyrightId)) {
                    continue;
                }
                tracks.add(MusicTrackSummary.builder()
                        .trackId(copyrightId)
                        .source(getSource().getCode())
                        .title(text(item, "name"))
                        .artist(joinSinger(item.path("singerList")))
                        .album(text(item, "album"))
                        .coverUrl(resolveImage(item))
                        .durationSeconds(integer(item, "duration"))
                        .build());
            }
        }
        return searchResult(keyword, page, pageSize, longValue(root, "songResultData", "totalCount"), tracks);
    }

    @Override
    public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
        JsonNode root = getJson("https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A0%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22bestShow%22%3A0%2C%22songlist%22%3A1%2C%22lyricSong%22%3A0%7D&pageSize="
                + pageSize + "&text=" + encode(keyword) + "&pageNo=" + page + "&sort=0&sid=USS",
                builder -> applySearchHeaders(builder, keyword));
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(path(root, "songListResultData", "result"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(item, "id"))
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .description("")
                    .coverUrl(text(item, "musicListPicUrl"))
                    .author(text(item, "userName"))
                    .trackCount(integer(item, "musicNum"))
                    .accentColor("linear-gradient(135deg, #1f3a2a 0%, #71b57c 100%)")
                    .build());
        }
        return MusicPlaylistSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(root, "songListResultData", "totalCount"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog() {
        JsonNode root = getJson("https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-taglist/release",
                builder -> applyDefaultHeaders(builder));
        List<MusicPlaylistCategory> hotTags = new ArrayList<>();
        for (JsonNode item : elements(path(root, "data").path(0).path("content"))) {
            hotTags.add(MusicPlaylistCategory.builder()
                    .tagId(item.path("texts").path(1).asText(""))
                    .name(item.path("texts").path(0).asText(""))
                    .hot(true)
                    .build());
        }
        List<MusicPlaylistCategoryGroup> groups = new ArrayList<>();
        List<JsonNode> groupsNode = elements(path(root, "data"));
        for (int index = 1; index < groupsNode.size(); index++) {
            JsonNode item = groupsNode.get(index);
            List<MusicPlaylistCategory> tags = new ArrayList<>();
            for (JsonNode tag : elements(item.path("content"))) {
                tags.add(MusicPlaylistCategory.builder()
                        .tagId(tag.path("texts").path(1).asText(""))
                        .name(tag.path("texts").path(0).asText(""))
                        .hot(false)
                        .build());
            }
            groups.add(MusicPlaylistCategoryGroup.builder()
                    .groupId(String.valueOf(index))
                    .name(text(item, "header", "title"))
                    .tags(tags)
                    .build());
        }
        return MusicPlaylistCategoryCatalog.builder()
                .source(getSource().getCode())
                .hotTags(hotTags)
                .groups(groups)
                .build();
    }

    @Override
    public MusicPlaylistCategoryResult getCategoryPlaylists(String tagId, int page, int pageSize) {
        String url;
        if (!StringUtils.hasText(tagId)) {
            url = "https://app.c.nf.migu.cn/pc/bmw/page-data/playlist-square-recommend/v1.0?templateVersion=2&pageNo=" + page;
        } else {
            url = "https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-listbytag/release?pageNumber=" + page
                    + "&templateVersion=2&tagId=" + encode(tagId);
        }
        JsonNode root = getJson(url, this::applyDefaultHeaders);
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        JsonNode source = path(root, "data", "contents");
        if (source == null || source.isMissingNode() || source.isNull()) {
            source = path(root, "data", "contentItemList").path(1).path("itemList");
        }
        collectPlaylists(source, playlists);
        return MusicPlaylistCategoryResult.builder()
                .source(getSource().getCode())
                .tagId(tagId)
                .categoryName(StringUtils.hasText(tagId) ? "分类歌单" : "推荐")
                .page(page)
                .pageSize(pageSize)
                .total((long) playlists.size())
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
        JsonNode detailRoot = getJson("https://c.musicapp.migu.cn/MIGUM3.0/resource/playlist/v2.0?playlistId=" + encode(playlistId),
                this::applyDefaultHeaders);
        JsonNode songsRoot = getJson("https://app.c.nf.migu.cn/MIGUM3.0/resource/playlist/song/v2.0?pageNo=1&pageSize=200&playlistId=" + encode(playlistId),
                this::applyDefaultHeaders);
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(path(songsRoot, "data", "songList"))) {
            String copyrightId = text(item, "copyrightId");
            if (!StringUtils.hasText(copyrightId)) {
                continue;
            }
            tracks.add(MusicTrackSummary.builder()
                    .trackId(copyrightId)
                    .source(getSource().getCode())
                    .title(text(item, "songName"))
                    .artist(joinSinger(item.path("singerList")))
                    .album(text(item, "album"))
                    .coverUrl(resolveImage(item))
                    .durationSeconds(integer(item, "duration"))
                    .build());
        }
        JsonNode detail = path(detailRoot, "data");
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(text(detail, "title"))
                .description(text(detail, "summary"))
                .coverUrl(path(detail, "imgItem").path("img").asText(""))
                .author(text(detail, "ownerName"))
                .trackCount(tracks.size())
                .tracks(tracks)
                .build();
    }

    @Override
    public MusicTrackDetail getTrackDetail(String trackId) {
        JsonNode infoRoot = postForm("https://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do?resourceType=2",
                Map.of("resourceId", trackId), builder -> applyDefaultHeaders(builder));
        JsonNode item = path(infoRoot, "resource").path(0);
        String streamUrl = fetchStreamUrl(trackId);
        String lyrics = fetchLyrics(trackId);
        return MusicTrackDetail.builder()
                .trackId(trackId)
                .source(getSource().getCode())
                .title(text(item, "songName"))
                .artist(joinSinger(item.path("artists")))
                .album(text(item, "album"))
                .coverUrl(resolveAlbumImage(item.path("albumImgs")))
                .durationSeconds(parseLength(text(item, "length")))
                .streamUrl(streamUrl)
                .lyrics(lyrics)
                .build();
    }

    private void applySearchHeaders(com.chua.common.support.network.http.HttpClientBuilder builder, String keyword) {
        applyDefaultHeaders(builder);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        MiguSignSupport.headers(keyword, timestamp).forEach(builder::header);
        builder.userAgent(MOBILE_UA);
    }

    private void applyDefaultHeaders(com.chua.common.support.network.http.HttpClientBuilder builder) {
        builder.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile Safari/604.1");
        builder.referer("https://m.music.migu.cn/");
    }

    private void collectPlaylists(JsonNode node, List<MusicPlaylistSummary> playlists) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : elements(node)) {
                collectPlaylists(item, playlists);
            }
            return;
        }
        if ("2021".equals(text(node, "resType"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(node, "resId"))
                    .source(getSource().getCode())
                    .title(firstNonBlank(text(node, "txt"), text(node, "title")))
                    .description(firstNonBlank(text(node, "txt2"), text(node, "summary")))
                    .coverUrl(firstNonBlank(text(node, "img"), text(node, "imageUrl")))
                    .author("")
                    .trackCount(integer(node, "musicNum"))
                    .accentColor("linear-gradient(135deg, #1f3a2a 0%, #71b57c 100%)")
                    .build());
        }
        collectPlaylists(node.path("contents"), playlists);
        collectPlaylists(node.path("itemList"), playlists);
    }

    private String resolveImage(JsonNode item) {
        String value = firstNonBlank(text(item, "img3"), text(item, "img2"), text(item, "img1"));
        if (value.startsWith("/")) {
            return "https://d.musicapp.migu.cn" + value;
        }
        return value;
    }

    private String resolveAlbumImage(JsonNode albumImgs) {
        JsonNode first = albumImgs.path(0);
        return first.path("img").asText("");
    }

    private String joinSinger(JsonNode singers) {
        List<String> names = new ArrayList<>();
        for (JsonNode item : elements(singers)) {
            String name = firstNonBlank(text(item, "name"), text(item, "singerName"));
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return String.join("、", names);
    }

    private int parseLength(String value) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return 0;
        }
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String fetchStreamUrl(String copyrightId) {
        JsonNode root = getJson("https://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/listen-url?netType=01&resourceType=2&songId="
                + encode(copyrightId) + "&toneFlag=HQ", this::applyDefaultHeaders);
        String directUrl = text(root, "data", "url");
        if (StringUtils.hasText(directUrl)) {
            return directUrl;
        }
        JsonNode formats = path(root, "data", "newRateFormats");
        if (formats != null && formats.isArray()) {
            for (JsonNode item : elements(formats)) {
                String value = item.path("url").asText("");
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    private String fetchLyrics(String copyrightId) {
        JsonNode root = getJson("https://music.migu.cn/v3/api/music/audioPlayer/getLyric?copyrightId=" + encode(copyrightId),
                builder -> builder.referer("https://music.migu.cn/v3/music/player/audio?from=migu"));
        return text(root, "lyric");
    }
}
