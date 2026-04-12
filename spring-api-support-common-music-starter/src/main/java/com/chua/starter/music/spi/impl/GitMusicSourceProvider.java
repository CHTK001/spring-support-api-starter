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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Spi("git")
public class GitMusicSourceProvider extends AbstractHttpMusicSourceProvider {

    private static final String DEFAULT_RAW_URL = "https://raw.gitcode.com/ikun_0014/music/master/audio_database.json";
    private static final String RAW_URL_PROPERTY = "music.git.raw-url";
    private static final String RAW_URL_ENV = "MUSIC_GIT_RAW_URL";
    private static final long CACHE_TTL_MILLIS = 5 * 60 * 1000L;
    private static final AtomicReference<CatalogCache> CACHE = new AtomicReference<>();

    @Override
    public MusicSourceOption getSource() {
        return MusicSourceOption.builder()
                .code("git")
                .name("GitCode 曲库")
                .description("公开 GitCode 曲库清单")
                .enabled(Boolean.TRUE)
                .build();
    }

    @Override
    public MusicOverview getOverview() {
        CatalogCache cache = loadCache();
        List<String> hotKeywords = cache.playlists.stream()
                .map(MusicPlaylistSummary::getTitle)
                .limit(8)
                .collect(Collectors.toList());
        return overviewOf(hotKeywords, cache.playlists.stream().limit(8).collect(Collectors.toList()));
    }

    @Override
    public MusicSearchResult search(String keyword, int page, int pageSize) {
        String actualKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        List<MusicTrackSummary> tracks = loadCache().tracks.stream()
                .filter(item -> matches(item, actualKeyword))
                .map(this::toTrackSummary)
                .collect(Collectors.toList());
        return searchResult(keyword, page, pageSize, tracks.size(), pageOf(tracks, page, pageSize));
    }

    @Override
    public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
        String actualKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        List<MusicPlaylistSummary> playlists = loadCache().playlists.stream()
                .filter(item -> item.getTitle().toLowerCase().contains(actualKeyword)
                        || item.getDescription().toLowerCase().contains(actualKeyword)
                        || item.getAuthor().toLowerCase().contains(actualKeyword))
                .collect(Collectors.toList());
        return MusicPlaylistSearchResult.builder()
                .source(getSource().getCode())
                .keyword(keyword)
                .page(page)
                .pageSize(pageSize)
                .total((long) playlists.size())
                .playlists(pageOf(playlists, page, pageSize))
                .build();
    }

    @Override
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog() {
        CatalogCache cache = loadCache();
        List<MusicPlaylistCategory> hotTags = new ArrayList<>();
        hotTags.add(MusicPlaylistCategory.builder().tagId("all").name("全部").hot(true).build());
        for (MusicPlaylistSummary item : cache.playlists.stream().skip(1).limit(7).collect(Collectors.toList())) {
            hotTags.add(MusicPlaylistCategory.builder()
                    .tagId(item.getPlaylistId())
                    .name(item.getTitle())
                    .hot(true)
                    .build());
        }
        return MusicPlaylistCategoryCatalog.builder()
                .source(getSource().getCode())
                .hotTags(hotTags)
                .groups(List.of(MusicPlaylistCategoryGroup.builder()
                        .groupId("library")
                        .name("曲库")
                        .tags(hotTags)
                        .build()))
                .build();
    }

    @Override
    public MusicPlaylistCategoryResult getCategoryPlaylists(String tagId, int page, int pageSize) {
        CatalogCache cache = loadCache();
        List<MusicPlaylistSummary> playlists;
        if (!StringUtils.hasText(tagId) || "all".equalsIgnoreCase(tagId)) {
            playlists = cache.playlists;
        } else {
            playlists = cache.playlists.stream()
                    .filter(item -> item.getPlaylistId().equals(tagId))
                    .collect(Collectors.toList());
        }
        return MusicPlaylistCategoryResult.builder()
                .source(getSource().getCode())
                .tagId(tagId)
                .categoryName(StringUtils.hasText(tagId) ? tagId : "全部")
                .page(page)
                .pageSize(pageSize)
                .total((long) playlists.size())
                .playlists(pageOf(playlists, page, pageSize))
                .build();
    }

    @Override
    public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
        CatalogCache cache = loadCache();
        GitPlaylist playlist = cache.playlistMap.get(playlistId);
        if (playlist == null) {
            throw new IllegalArgumentException("未找到 GitCode 歌单: " + playlistId);
        }
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(playlist.summary.getTitle())
                .description(playlist.summary.getDescription())
                .coverUrl(playlist.summary.getCoverUrl())
                .author(playlist.summary.getAuthor())
                .trackCount(playlist.summary.getTrackCount())
                .tracks(playlist.tracks.stream().map(this::toTrackSummary).collect(Collectors.toList()))
                .build();
    }

    @Override
    public MusicTrackDetail getTrackDetail(String trackId) {
        GitTrack track = loadCache().trackMap.get(trackId);
        if (track == null) {
            throw new IllegalArgumentException("未找到 GitCode 歌曲: " + trackId);
        }
        return MusicTrackDetail.builder()
                .trackId(track.trackId)
                .source(getSource().getCode())
                .title(track.title)
                .artist(track.artist)
                .album(track.album)
                .coverUrl(track.coverUrl)
                .streamUrl(track.streamUrl)
                .durationSeconds(track.durationSeconds)
                .lyrics(track.lyrics)
                .build();
    }

    private CatalogCache loadCache() {
        CatalogCache current = CACHE.get();
        long now = System.currentTimeMillis();
        if (current != null && now - current.loadedAt < CACHE_TTL_MILLIS) {
            return current;
        }
        synchronized (CACHE) {
            current = CACHE.get();
            if (current != null && now - current.loadedAt < CACHE_TTL_MILLIS) {
                return current;
            }
            JsonNode root = getJson(resolveRawUrl());
            List<GitTrack> tracks = new ArrayList<>();
            for (JsonNode item : elements(root)) {
                GitTrack track = toTrack(item);
                if (track != null) {
                    tracks.add(track);
                }
            }
            CatalogCache rebuilt = buildCache(tracks, now);
            CACHE.set(rebuilt);
            return rebuilt;
        }
    }

    private CatalogCache buildCache(List<GitTrack> tracks, long loadedAt) {
        Map<String, GitTrack> trackMap = new LinkedHashMap<>();
        tracks.forEach(item -> trackMap.put(item.trackId, item));

        Map<String, List<GitTrack>> byArtist = tracks.stream()
                .filter(item -> StringUtils.hasText(item.artist))
                .collect(Collectors.groupingBy(item -> item.artist, LinkedHashMap::new, Collectors.toList()));

        List<MusicPlaylistSummary> playlistSummaries = new ArrayList<>();
        Map<String, GitPlaylist> playlistMap = new LinkedHashMap<>();

        GitPlaylist full = buildPlaylist("git-main", "下架歌曲收藏", "GitCode 公开曲库聚合歌单", "GitCode",
                tracks.stream().limit(500).collect(Collectors.toList()));
        playlistSummaries.add(full.summary);
        playlistMap.put(full.summary.getPlaylistId(), full);

        byArtist.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 2)
                .sorted(Comparator.<Map.Entry<String, List<GitTrack>>>comparingInt(entry -> entry.getValue().size()).reversed())
                .limit(8)
                .forEach(entry -> {
                    String playlistId = "artist-" + safeId(entry.getKey());
                    GitPlaylist playlist = buildPlaylist(
                            playlistId,
                            entry.getKey(),
                            "GitCode 曲库中的歌手合集",
                            entry.getKey(),
                            entry.getValue()
                    );
                    playlistSummaries.add(playlist.summary);
                    playlistMap.put(playlistId, playlist);
                });

        return new CatalogCache(loadedAt, tracks, trackMap, playlistSummaries, playlistMap);
    }

    private GitPlaylist buildPlaylist(String playlistId, String title, String description, String author, List<GitTrack> tracks) {
        String coverUrl = tracks.isEmpty() ? "" : tracks.get(0).coverUrl;
        MusicPlaylistSummary summary = MusicPlaylistSummary.builder()
                .playlistId(playlistId)
                .source(getSource().getCode())
                .title(title)
                .description(description)
                .coverUrl(coverUrl)
                .author(author)
                .trackCount(tracks.size())
                .accentColor("linear-gradient(135deg, #162033 0%, #4d6df0 100%)")
                .build();
        return new GitPlaylist(summary, tracks);
    }

    private GitTrack toTrack(JsonNode item) {
        String relativePath = text(item, "relative_path");
        String title = firstNonBlank(text(item, "title"), fileNameWithoutExtension(text(item, "filename")));
        String artist = firstNonBlank(text(item, "artist"), "未知歌手");
        String album = firstNonBlank(text(item, "album"), "未知专辑");
        String streamUrl = text(item, "download_url");
        if (!StringUtils.hasText(relativePath) || !StringUtils.hasText(streamUrl)) {
            return null;
        }
        String trackId = firstNonBlank(text(item, "hash"), safeId(relativePath));
        return new GitTrack(
                trackId,
                title,
                artist,
                album,
                firstNonBlank(text(item, "img"), ""),
                integer(item, "duration"),
                text(item, "lyrics"),
                streamUrl
        );
    }

    private MusicTrackSummary toTrackSummary(GitTrack item) {
        return MusicTrackSummary.builder()
                .trackId(item.trackId)
                .source(getSource().getCode())
                .title(item.title)
                .artist(item.artist)
                .album(item.album)
                .coverUrl(item.coverUrl)
                .durationSeconds(item.durationSeconds)
                .build();
    }

    private boolean matches(GitTrack track, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return track.title.toLowerCase().contains(keyword)
                || track.artist.toLowerCase().contains(keyword)
                || track.album.toLowerCase().contains(keyword);
    }

    private <T> List<T> pageOf(List<T> items, int page, int pageSize) {
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(items.size(), fromIndex + pageSize);
        if (fromIndex >= items.size()) {
            return List.of();
        }
        return items.subList(fromIndex, toIndex);
    }

    private String resolveRawUrl() {
        String configured = System.getProperty(RAW_URL_PROPERTY);
        if (!StringUtils.hasText(configured)) {
            configured = System.getenv(RAW_URL_ENV);
        }
        return StringUtils.hasText(configured) ? configured.trim() : DEFAULT_RAW_URL;
    }

    private String safeId(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String fileNameWithoutExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "未知歌曲";
        }
        int index = filename.lastIndexOf('.');
        return index > 0 ? filename.substring(0, index) : filename;
    }

    private static final class CatalogCache {
        private final long loadedAt;
        private final List<GitTrack> tracks;
        private final Map<String, GitTrack> trackMap;
        private final List<MusicPlaylistSummary> playlists;
        private final Map<String, GitPlaylist> playlistMap;

        private CatalogCache(long loadedAt,
                             List<GitTrack> tracks,
                             Map<String, GitTrack> trackMap,
                             List<MusicPlaylistSummary> playlists,
                             Map<String, GitPlaylist> playlistMap) {
            this.loadedAt = loadedAt;
            this.tracks = tracks;
            this.trackMap = trackMap;
            this.playlists = playlists;
            this.playlistMap = playlistMap;
        }
    }

    private static final class GitPlaylist {
        private final MusicPlaylistSummary summary;
        private final List<GitTrack> tracks;

        private GitPlaylist(MusicPlaylistSummary summary, List<GitTrack> tracks) {
            this.summary = summary;
            this.tracks = tracks;
        }
    }

    private static final class GitTrack {
        private final String trackId;
        private final String title;
        private final String artist;
        private final String album;
        private final String coverUrl;
        private final int durationSeconds;
        private final String lyrics;
        private final String streamUrl;

        private GitTrack(String trackId,
                         String title,
                         String artist,
                         String album,
                         String coverUrl,
                         int durationSeconds,
                         String lyrics,
                         String streamUrl) {
            this.trackId = trackId;
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.coverUrl = coverUrl;
            this.durationSeconds = durationSeconds;
            this.lyrics = lyrics;
            this.streamUrl = streamUrl;
        }
    }
}
