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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Spi("kw")
public class KuwoMusicSourceProvider extends AbstractHttpMusicSourceProvider {

    @Override
    public MusicSourceOption getSource() {
        return MusicSourceOption.builder()
                .code("kw")
                .name("酷我音乐")
                .description("CeruMusic JS SDK - Kuwo Music")
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
        String url = "http://search.kuwo.cn/r.s?client=kt&all=" + encode(keyword)
                + "&pn=" + Math.max(0, page - 1)
                + "&rn=" + pageSize
                + "&uid=794762570&ver=kwplayer_ar_9.2.2.1&vipver=1&show_copyright_off=1&newver=1&ft=music&cluster=0&strategy=2012&encoding=utf8&rformat=json&vermerge=1&mobi=1&issubtitle=1";
        JsonNode root = getJson(url);
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(root.path("abslist"))) {
            String musicRid = text(item, "MUSICRID").replace("MUSIC_", "");
            if (!StringUtils.hasText(musicRid)) {
                continue;
            }
            tracks.add(MusicTrackSummary.builder()
                    .trackId(musicRid)
                    .source(getSource().getCode())
                    .title(text(item, "SONGNAME"))
                    .artist(text(item, "ARTIST"))
                    .album(text(item, "ALBUM"))
                    .coverUrl("")
                    .durationSeconds(parseDuration(text(item, "DURATION")))
                    .build());
        }
        return searchResult(keyword, page, pageSize, longValue(root, "TOTAL"), tracks);
    }

    @Override
    public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
        String url = "http://search.kuwo.cn/r.s?all=" + encode(keyword)
                + "&pn=" + Math.max(0, page - 1)
                + "&rn=" + pageSize
                + "&rformat=json&encoding=utf8&ver=mbox&vipver=MUSIC_8.7.7.0_BCS37&plat=pc&devid=28156413&ft=playlist&pay=0&needliveshow=0";
        JsonNode root = getJson(url);
        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        for (JsonNode item : elements(root.path("abslist"))) {
            playlists.add(MusicPlaylistSummary.builder()
                    .playlistId(text(item, "playlistid"))
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .description(text(item, "intro"))
                    .coverUrl(text(item, "pic"))
                    .author(text(item, "nickname"))
                    .trackCount(integer(item, "songnum"))
                    .accentColor("linear-gradient(135deg, #163042 0%, #4f8db5 100%)")
                    .build());
        }
        return MusicPlaylistSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(root, "TOTAL"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog() {
        JsonNode tagRoot = getJson("http://wapi.kuwo.cn/api/pc/classify/playlist/getTagList?cmd=rcm_keyword_playlist&user=0&prod=kwplayer_pc_9.0.5.0&vipver=9.0.5.0&source=kwplayer_pc_9.0.5.0&loginUid=0&loginSid=0&appUid=76039576");
        JsonNode hotRoot = getJson("http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmTagList?loginUid=0&loginSid=0&appUid=76039576");

        List<MusicPlaylistCategory> hotTags = new ArrayList<>();
        for (JsonNode item : elements(path(hotRoot, "data").path(0).path("data"))) {
            hotTags.add(MusicPlaylistCategory.builder()
                    .tagId(text(item, "id") + "-" + text(item, "digest"))
                    .name(text(item, "name"))
                    .hot(true)
                    .build());
        }

        List<MusicPlaylistCategoryGroup> groups = new ArrayList<>();
        for (JsonNode group : elements(tagRoot.path("data"))) {
            List<MusicPlaylistCategory> tags = new ArrayList<>();
            for (JsonNode item : elements(group.path("data"))) {
                tags.add(MusicPlaylistCategory.builder()
                        .tagId(text(item, "id") + "-" + text(item, "digest"))
                        .name(text(item, "name"))
                        .hot(false)
                        .build());
            }
            groups.add(MusicPlaylistCategoryGroup.builder()
                    .groupId(text(group, "id"))
                    .name(text(group, "name"))
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
        JsonNode root;
        String categoryName = "热门";
        if (!StringUtils.hasText(tagId)) {
            root = getJson("http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmPlayList?loginUid=0&loginSid=0&appUid=76039576&&pn="
                    + page + "&rn=" + pageSize + "&order=hot");
        } else {
            String[] parts = tagId.split("-");
            String id = parts[0];
            String digest = parts.length > 1 ? parts[1] : "";
            if ("10000".equals(digest)) {
                root = getJson("http://wapi.kuwo.cn/api/pc/classify/playlist/getTagPlayList?loginUid=0&loginSid=0&appUid=76039576&pn="
                        + page + "&id=" + id + "&rn=" + pageSize);
            } else {
                root = getJson("http://mobileinterfaces.kuwo.cn/er.s?type=get_pc_qz_data&f=web&id=" + id + "&prod=pc");
            }
            categoryName = "分类歌单";
        }

        List<MusicPlaylistSummary> playlists = new ArrayList<>();
        JsonNode data = path(root, "data");
        if (data.isArray()) {
            for (JsonNode block : elements(data)) {
                for (JsonNode item : elements(block.path("list"))) {
                    playlists.add(toPlaylist(item));
                }
            }
        } else {
            for (JsonNode item : elements(path(data, "data"))) {
                playlists.add(toPlaylist(item));
            }
        }

        return MusicPlaylistCategoryResult.builder()
                .source(getSource().getCode())
                .tagId(tagId)
                .categoryName(categoryName)
                .page(page)
                .pageSize(pageSize)
                .total(longValue(data, "total"))
                .playlists(playlists)
                .build();
    }

    @Override
    public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
        String actualId = playlistId;
        if (actualId.startsWith("digest-")) {
            String[] parts = actualId.split("__");
            actualId = parts.length > 1 ? parts[1] : actualId.replace("digest-", "");
        }
        JsonNode root = getJson("http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=" + actualId
                + "&pn=0&rn=1000&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1");
        List<MusicTrackSummary> tracks = new ArrayList<>();
        for (JsonNode item : elements(root.path("musiclist"))) {
            String trackId = text(item, "id");
            if (!StringUtils.hasText(trackId)) {
                continue;
            }
            tracks.add(MusicTrackSummary.builder()
                    .trackId(trackId)
                    .source(getSource().getCode())
                    .title(text(item, "name"))
                    .artist(text(item, "artist"))
                    .album(text(item, "album"))
                    .coverUrl("")
                    .durationSeconds(parseDuration(text(item, "duration")))
                    .build());
        }
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(text(root, "title"))
                .description(text(root, "info"))
                .coverUrl(text(root, "pic"))
                .author(text(root, "uname"))
                .trackCount(tracks.size())
                .tracks(tracks)
                .build();
    }

    @Override
    public MusicTrackDetail getTrackDetail(String trackId) {
        JsonNode infoRoot = getJson("http://www.kuwo.cn/api/www/music/musicInfo?mid=" + trackId,
                builder -> builder.header("csrf", UUID.randomUUID().toString()).referer("http://www.kuwo.cn/"));
        JsonNode info = path(infoRoot, "data");
        String playUrl = getText("https://antiserver.kuwo.cn/anti.s?type=convert_url3&rid=MUSIC_" + trackId + "&format=mp3&response=url");
        String lyrics = buildLyrics(getJson("http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=" + trackId));
        return MusicTrackDetail.builder()
                .trackId(trackId)
                .source(getSource().getCode())
                .title(text(info, "name"))
                .artist(text(info, "artist"))
                .album(text(info, "album"))
                .coverUrl(text(info, "pic"))
                .durationSeconds(integer(info, "duration"))
                .streamUrl(playUrl.trim())
                .lyrics(lyrics)
                .build();
    }

    private MusicPlaylistSummary toPlaylist(JsonNode item) {
        String id = text(item, "id");
        String digest = text(item, "digest");
        String playlistId = StringUtils.hasText(digest) && StringUtils.hasText(id)
                ? "digest-" + digest + "__" + id
                : id;
        return MusicPlaylistSummary.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(text(item, "name"))
                .description(text(item, "desc"))
                .coverUrl(text(item, "img"))
                .author(text(item, "uname"))
                .trackCount(integer(item, "total"))
                .accentColor("linear-gradient(135deg, #163042 0%, #4f8db5 100%)")
                .build();
    }

    private int parseDuration(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private String buildLyrics(JsonNode root) {
        StringBuilder builder = new StringBuilder();
        for (JsonNode item : elements(path(root, "data", "lrclist"))) {
            String time = item.path("time").asText("");
            if (time.contains(".")) {
                String[] parts = time.split("\\.");
                time = parts[0] + "." + parts[1];
            } else {
                time = time + ".00";
            }
            builder.append("[").append(time).append("]").append(item.path("lineLyric").asText("")).append("\n");
        }
        return builder.toString().trim();
    }
}
