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
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Spi("kg")
public class KugouMusicSourceProvider extends AbstractHttpMusicSourceProvider {

    private static final Pattern GLOBAL_DATA_PATTERN = Pattern.compile("global\\.data = (\\[.+?]);", Pattern.DOTALL);
    private static final Pattern GLOBAL_NAME_PATTERN = Pattern.compile("name:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern GLOBAL_PIC_PATTERN = Pattern.compile("pic:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("<li id=\"singer_text\">创建人：<a[^>]*title=\"([^\"]*)\"");
    private static final Pattern DESC_PATTERN = Pattern.compile("<div class=\"pc_specail_text pc_singer_tab_content\" id=\"specailIntroduceWrap\">([\\s\\S]*?)</div>");
    private static final String CATEGORY_INFO_URL = "http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_smarty=1&";

    @Override
    public MusicSourceOption getSource() {
        return MusicSourceOption.builder()
                .code("kg")
                .name("酷狗音乐")
                .description("CeruMusic JS SDK - Kugou Music")
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
        String url = "https://songsearch.kugou.com/song_search_v2?keyword=" + encode(keyword)
                + "&page=" + page
                + "&pagesize=" + pageSize
                + "&userid=0&clientver=&platform=WebFilter&filter=2&iscorrection=1&privilege_filter=0&area_code=1";
        JsonNode root = getJson(url);
        List<MusicTrackSummary> tracks = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (JsonNode item : elements(path(root, "data", "lists"))) {
            collectSearchTrack(item, tracks, seen);
            for (JsonNode child : elements(item.path("Grp"))) {
                collectSearchTrack(child, tracks, seen);
            }
        }
        return searchResult(keyword, page, pageSize, longValue(root, "data", "total"), tracks);
    }

    @Override
    public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
        String url = "http://msearchretry.kugou.com/api/v3/search/special?keyword=" + encode(keyword)
                + "&page=" + page
                + "&pagesize=" + pageSize
                + "&showtype=10&filter=0&version=7910&sver=2";
        JsonNode root = getJson(url);
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(path(root, "data", "info"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(item, "specialid"))
                    .source(getSource().getCode())
                    .title(text(item, "specialname"))
                    .description(text(item, "intro"))
                    .coverUrl(normalizeImage(text(item, "imgurl")))
                    .author(text(item, "nickname"))
                    .trackCount(integer(item, "songcount"))
                    .accentColor("linear-gradient(135deg, #2a1f14 0%, #a46b41 100%)")
                    .playCount(parseCount(firstNonBlank(text(item, "playcount"), text(item, "total_play_count"))))
                    .build());
        }
        return MusicPlaylistSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(root, "data", "total"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog() {
        JsonNode root = getJson(CATEGORY_INFO_URL);
        List<MusicPlaylistCategory> hotTags = new ArrayList<>();
        JsonNode hotTagNode = path(root, "data", "hotTag", "data");
        if (hotTagNode != null && hotTagNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = hotTagNode.fields();
            while (iterator.hasNext()) {
                JsonNode item = iterator.next().getValue();
                hotTags.add(MusicPlaylistCategory.builder()
                        .tagId(text(item, "special_id"))
                        .name(text(item, "special_name"))
                        .hot(true)
                        .build());
            }
        }

        List<MusicPlaylistCategoryGroup> groups = new ArrayList<>();
        JsonNode tagIds = path(root, "data", "tagids");
        if (tagIds != null && tagIds.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> iterator = tagIds.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                List<MusicPlaylistCategory> tags = new ArrayList<>();
                for (JsonNode item : elements(entry.getValue().path("data"))) {
                    tags.add(MusicPlaylistCategory.builder()
                            .tagId(text(item, "id"))
                            .name(text(item, "name"))
                            .hot(false)
                            .build());
                }
                groups.add(MusicPlaylistCategoryGroup.builder()
                        .groupId(String.valueOf(longValue(entry.getValue(), "id")))
                        .name(entry.getKey())
                        .tags(tags)
                        .build());
            }
        }
        return MusicPlaylistCategoryCatalog.builder()
                .source(getSource().getCode())
                .hotTags(hotTags)
                .groups(groups)
                .build();
    }

    @Override
    public MusicPlaylistCategoryResult getCategoryPlaylists(String tagId, int page, int pageSize) {
        String actualTagId = tagId == null ? "" : tagId.trim();
        String listUrl = "http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_ajax=1&cdn=cdn&t=5&c="
                + encode(actualTagId) + "&p=" + page;
        JsonNode listRoot = getJson(listUrl);
        JsonNode infoRoot = getJson(CATEGORY_INFO_URL + (StringUtils.hasText(actualTagId) ? "c=" + encode(actualTagId) : ""));
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(path(listRoot, "special_db"))) {
            playlists.add(toPlaylistSummary(item));
        }
        return MusicPlaylistCategoryResult.builder()
                .source(getSource().getCode())
                .tagId(tagId)
                .categoryName(StringUtils.hasText(actualTagId) ? "分类歌单" : "推荐")
                .page(page)
                .pageSize(pageSize)
                .total(longValue(path(infoRoot, "data", "params"), "total"))
                .playlists(limitPlaylists(playlists, pageSize))
                .build();
    }

    @Override
    public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
        String html = getText("http://www2.kugou.kugou.com/yueku/v9/special/single/" + encode(playlistId) + "-5-9999.html",
                builder -> builder.userAgent(DESKTOP_UA));
        JsonNode tracksNode = extractGlobalData(html);
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(tracksNode)) {
            tracks.add(MusicTrackSummary.builder()
                    .trackId(buildTrackId(text(item, "audio_id"), text(item, "hash"), text(item, "album_id")))
                    .source(getSource().getCode())
                    .title(text(item, "songname"))
                    .artist(firstNonBlank(text(item, "singername"), joinAuthors(item.path("authors"))))
                    .album(text(item, "album_name"))
                    .coverUrl(firstNonBlank(normalizeImage(text(item, "trans_param", "union_cover")), normalizeImage(text(item, "img"))))
                    .durationSeconds(parseDurationValue(item.path("duration")))
                    .build());
        }
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(extractString(html, GLOBAL_NAME_PATTERN))
                .description(extractDescription(html))
                .coverUrl(normalizeImage(extractString(html, GLOBAL_PIC_PATTERN)))
                .author(extractAuthor(html))
                .trackCount(tracks.size())
                .tracks(tracks)
                .build();
    }

    @Override
    public MusicTrackDetail getTrackDetail(String trackId) {
        TrackIdentity identity = TrackIdentity.parse(trackId);
        String url = "http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash=" + encode(identity.hash)
                + "&album_id=" + encode(identity.albumId);
        JsonNode info = getJson(url, builder -> builder.userAgent(MOBILE_UA).referer("https://m.kugou.com/"));
        String lyrics = fetchLyrics(
                firstNonBlank(text(info, "songName"), text(info, "fileName")),
                identity.hash,
                resolveLyricDuration(info)
        );
        return MusicTrackDetail.builder()
                .trackId(trackId)
                .source(getSource().getCode())
                .title(firstNonBlank(text(info, "songName"), text(info, "fileName")))
                .artist(firstNonBlank(text(info, "author_name"), text(info, "singerName")))
                .album(firstNonBlank(text(info, "album_name"), text(info, "albumName")))
                .coverUrl(firstNonBlank(
                        normalizeImage(text(info, "album_img")),
                        normalizeImage(text(info, "imgUrl")),
                        normalizeImage(text(info, "trans_param", "union_cover"))
                ))
                .durationSeconds(parseDurationValue(path(info, "extra", "128timelength")))
                .streamUrl(text(info, "url"))
                .lyrics(lyrics)
                .build();
    }

    private void collectSearchTrack(JsonNode item, List<MusicTrackSummary> tracks, Set<String> seen) {
        String hash = text(item, "FileHash");
        String audioId = text(item, "Audioid");
        String uniqueKey = audioId + "__" + hash;
        if (!StringUtils.hasText(hash) || !seen.add(uniqueKey)) {
            return;
        }
        tracks.add(MusicTrackSummary.builder()
                .trackId(buildTrackId(audioId, hash, text(item, "AlbumID")))
                .source(getSource().getCode())
                .title(text(item, "SongName"))
                .artist(firstNonBlank(joinArtists(item.path("Singers")), text(item, "SingerName")))
                .album(text(item, "AlbumName"))
                .coverUrl(normalizeImage(text(item, "Image")))
                .durationSeconds(parseDurationValue(item.path("Duration")))
                .build());
    }

    private MusicPlaylistSummary toPlaylistSummary(JsonNode item) {
        return MusicPlaylistSummary.builder()
                .playlistId(text(item, "specialid"))
                .source(getSource().getCode())
                .title(text(item, "specialname"))
                .description(text(item, "intro"))
                .coverUrl(normalizeImage(firstNonBlank(text(item, "img"), text(item, "imgurl"))))
                .author(text(item, "nickname"))
                .trackCount(integer(item, "song_count"))
                .accentColor("linear-gradient(135deg, #2a1f14 0%, #a46b41 100%)")
                .playCount(parseCount(firstNonBlank(text(item, "total_play_count"), text(item, "play_count"))))
                .build();
    }

    private List<MusicPlaylistSummary> limitPlaylists(List<MusicPlaylistSummary> playlists, int pageSize) {
        if (playlists.size() <= pageSize) {
            return playlists;
        }
        return new ArrayList<>(playlists.subList(0, pageSize));
    }

    private JsonNode extractGlobalData(String html) {
        Matcher matcher = GLOBAL_DATA_PATTERN.matcher(html);
        if (!matcher.find()) {
            return MAPPER.createArrayNode();
        }
        try {
            return MAPPER.readTree(matcher.group(1));
        } catch (Exception e) {
            throw new IllegalStateException("解析酷狗歌单详情失败", e);
        }
    }

    private String extractString(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return decodeJsString(matcher.group(1));
    }

    private String extractAuthor(String html) {
        Matcher matcher = AUTHOR_PATTERN.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return HtmlUtils.htmlUnescape(matcher.group(1));
    }

    private String extractDescription(String html) {
        Matcher matcher = DESC_PATTERN.matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return HtmlUtils.htmlUnescape(matcher.group(1).replaceAll("<[^>]+>", "").trim());
    }

    private String decodeJsString(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        try {
            return MAPPER.readValue("\"" + raw + "\"", String.class);
        } catch (Exception e) {
            return raw;
        }
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

    private String joinAuthors(JsonNode node) {
        List<String> names = new ArrayList<>();
        for (JsonNode item : elements(node)) {
            String name = text(item, "singername");
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return String.join(" / ", names);
    }

    private String normalizeImage(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replace("{size}", "400");
    }

    private String buildTrackId(String audioId, String hash, String albumId) {
        return firstNonBlank(audioId, "0") + "__" + firstNonBlank(hash, "") + "__" + firstNonBlank(albumId, "0");
    }

    private int resolveLyricDuration(JsonNode info) {
        int duration = parseDurationValue(path(info, "extra", "128timelength"));
        if (duration > 0) {
            return duration * 1000;
        }
        int timeLength = integer(info, "timeLength");
        if (timeLength > 0) {
            return timeLength;
        }
        return 0;
    }

    private String fetchLyrics(String keyword, String hash, int durationMillis) {
        if (!StringUtils.hasText(hash) || durationMillis <= 0) {
            return "";
        }
        JsonNode search = getJson("http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword="
                + encode(keyword) + "&hash=" + encode(hash) + "&timelength=" + durationMillis + "&lrctxt=1",
                builder -> builder
                        .header("KG-RC", "1")
                        .header("KG-THash", "expand_search_manager.cpp:852736169:451")
                        .userAgent("KuGou2012-9020-ExpandSearchManager"));
        JsonNode candidate = path(search, "candidates").path(0);
        String id = text(candidate, "id");
        String accessKey = text(candidate, "accesskey");
        if (!StringUtils.hasText(id) || !StringUtils.hasText(accessKey)) {
            return "";
        }
        JsonNode lyric = getJson("http://lyrics.kugou.com/download?ver=1&client=pc&id=" + encode(id)
                        + "&accesskey=" + encode(accessKey) + "&fmt=lrc&charset=utf8",
                builder -> builder
                        .header("KG-RC", "1")
                        .header("KG-THash", "expand_search_manager.cpp:852736169:451")
                        .userAgent("KuGou2012-9020-ExpandSearchManager"));
        String content = text(lyric, "content");
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
    }

    private Long parseCount(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.replace(",", "").trim();
        try {
            if (normalized.endsWith("万")) {
                double number = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return Math.round(number * 10_000D);
            }
            if (normalized.endsWith("亿")) {
                double number = Double.parseDouble(normalized.substring(0, normalized.length() - 1));
                return Math.round(number * 100_000_000D);
            }
            return Long.parseLong(normalized);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int parseDurationValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return 0;
        }
        long value = node.asLong(0L);
        if (value <= 0L) {
            String textValue = node.asText("");
            if (!StringUtils.hasText(textValue)) {
                return 0;
            }
            try {
                value = Long.parseLong(textValue);
            } catch (Exception e) {
                return 0;
            }
        }
        return value >= 1000L ? (int) Math.round(value / 1000D) : (int) value;
    }

    private static final class TrackIdentity {
        private final String hash;
        private final String albumId;

        private TrackIdentity(String hash, String albumId) {
            this.hash = hash;
            this.albumId = albumId;
        }

        private static TrackIdentity parse(String trackId) {
            String[] parts = trackId == null ? new String[0] : trackId.split("__");
            String hash = parts.length >= 2 ? parts[1] : trackId;
            String albumId = parts.length >= 3 ? parts[2] : "0";
            return new TrackIdentity(hash, albumId);
        }
    }
}
