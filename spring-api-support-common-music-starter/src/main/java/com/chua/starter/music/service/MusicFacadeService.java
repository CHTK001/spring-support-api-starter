package com.chua.starter.music.service;

import com.chua.common.support.music.model.MusicOverview;
import com.chua.common.support.music.model.MusicPlaylistCategoryCatalog;
import com.chua.common.support.music.model.MusicPlaylistCategoryResult;
import com.chua.common.support.music.model.MusicPlaylistDetail;
import com.chua.common.support.music.model.MusicPlaylistSearchResult;
import com.chua.common.support.music.model.MusicPlaylistSummary;
import com.chua.common.support.music.model.MusicSearchResult;
import com.chua.common.support.music.model.MusicSourceOption;
import com.chua.common.support.music.model.MusicTrackDetail;
import com.chua.common.support.music.model.MusicTrackSummary;
import com.chua.common.support.music.spi.MusicSourceProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.chua.common.support.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicFacadeService {

    private final MusicSourceRegistry musicSourceRegistry;

    public List<MusicSourceOption> listSources() {
        return musicSourceRegistry.listSources();
    }

    public MusicOverview getOverview(String source) {
        if (!musicSourceRegistry.isAggregateSource(source)) {
            return enrichOverview(musicSourceRegistry.requireProvider(source).getOverview(),
                    musicSourceRegistry.resolveSource(source));
        }
        for (MusicSourceProvider provider : musicSourceRegistry.listProviders()) {
            MusicOverview overview = invokeSafely(provider, "overview", provider::getOverview);
            if (hasOverviewContent(overview)) {
                return enrichOverview(overview, provider.getSource().getCode());
            }
        }
        return enrichOverview(MusicOverview.builder()
                .hotKeywords(List.of())
                .featuredPlaylists(List.of())
                .build(), musicSourceRegistry.resolveSource(source));
    }

    public MusicSearchResult search(String source, String keyword, int page, int pageSize) {
        int actualPage = normalizePage(page);
        int actualPageSize = normalizePageSize(pageSize);
        if (musicSourceRegistry.isAggregateSource(source)) {
            Map<String, MusicTrackSummary> mergedTracks = new LinkedHashMap<>();
            long total = 0L;
            for (MusicSourceProvider provider : musicSourceRegistry.listProviders()) {
                MusicSearchResult result = invokeSafely(provider, "search",
                        () -> provider.search(keyword, 1, actualPageSize));
                if (result == null) {
                    continue;
                }
                total += result.getTotal() == null ? 0L : result.getTotal();
                if (result.getTracks() != null) {
                    for (MusicTrackSummary track : result.getTracks()) {
                        if (track == null) {
                            continue;
                        }
                        mergedTracks.putIfAbsent(trackKey(track), track);
                    }
                }
            }
            List<MusicTrackSummary> tracks = pageOf(new ArrayList<>(mergedTracks.values()), actualPage, actualPageSize);
            return MusicSearchResult.builder()
                    .source("all")
                    .keyword(keyword)
                    .page(actualPage)
                    .pageSize(actualPageSize)
                    .total(total)
                    .tracks(tracks)
                    .build();
        }
        return musicSourceRegistry.requireProvider(source).search(keyword, actualPage, actualPageSize);
    }

    public MusicPlaylistSearchResult searchPlaylists(String source, String keyword, int page, int pageSize) {
        int actualPage = normalizePage(page);
        int actualPageSize = normalizePageSize(pageSize);
        if (musicSourceRegistry.isAggregateSource(source)) {
            Map<String, MusicPlaylistSummary> mergedPlaylists = new LinkedHashMap<>();
            long total = 0L;
            for (MusicSourceProvider provider : musicSourceRegistry.listProviders()) {
                MusicPlaylistSearchResult result = invokeSafely(provider, "searchPlaylists",
                        () -> provider.searchPlaylists(keyword, 1, actualPageSize));
                if (result == null) {
                    continue;
                }
                total += result.getTotal() == null ? 0L : result.getTotal();
                if (result.getPlaylists() != null) {
                    for (MusicPlaylistSummary playlist : result.getPlaylists()) {
                        if (playlist == null) {
                            continue;
                        }
                        mergedPlaylists.putIfAbsent(playlistKey(playlist), playlist);
                    }
                }
            }
            List<MusicPlaylistSummary> playlists = pageOf(new ArrayList<>(mergedPlaylists.values()), actualPage, actualPageSize);
            return MusicPlaylistSearchResult.builder()
                    .source("all")
                    .keyword(keyword)
                    .page(actualPage)
                    .pageSize(actualPageSize)
                    .total(total)
                    .playlists(playlists)
                    .build();
        }
        return musicSourceRegistry.requireProvider(source).searchPlaylists(keyword, actualPage, actualPageSize);
    }

    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog(String source) {
        return musicSourceRegistry.requireProvider(source).getPlaylistCategoryCatalog();
    }

    public MusicPlaylistCategoryResult getCategoryPlaylists(String source, String tagId, int page, int pageSize) {
        return musicSourceRegistry.requireProvider(source).getCategoryPlaylists(tagId, page, pageSize);
    }

    public MusicPlaylistDetail getPlaylistDetail(String source, String playlistId) {
        return musicSourceRegistry.requireProvider(source).getPlaylistDetail(playlistId);
    }

    public MusicTrackDetail getTrackDetail(String source, String trackId) {
        return musicSourceRegistry.requireProvider(source).getTrackDetail(trackId);
    }

    private MusicOverview enrichOverview(MusicOverview overview, String defaultSource) {
        MusicOverview target = overview == null ? MusicOverview.builder()
                .hotKeywords(List.of())
                .featuredPlaylists(List.of())
                .build() : overview;
        target.setSources(listSources());
        target.setDefaultSource(defaultSource);
        return target;
    }

    private boolean hasOverviewContent(MusicOverview overview) {
        return overview != null
                && ((overview.getHotKeywords() != null && !overview.getHotKeywords().isEmpty())
                || (overview.getFeaturedPlaylists() != null && !overview.getFeaturedPlaylists().isEmpty()));
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), 100);
    }

    private <T> List<T> pageOf(List<T> items, int page, int pageSize) {
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(items.size(), fromIndex + pageSize);
        if (fromIndex >= items.size()) {
            return List.of();
        }
        return items.subList(fromIndex, toIndex);
    }

    private <T> T invokeSafely(MusicSourceProvider provider, String operation, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            log.warn("Music provider call failed, source={}, operation={}, message={}",
                    providerCode(provider), operation, ex.getMessage(), ex);
            return null;
        }
    }

    private String providerCode(MusicSourceProvider provider) {
        if (provider == null || provider.getSource() == null || !StringUtils.hasText(provider.getSource().getCode())) {
            return "unknown";
        }
        return provider.getSource().getCode();
    }

    private String trackKey(MusicTrackSummary track) {
        String source = StringUtils.hasText(track.getSource()) ? track.getSource() : "unknown";
        String trackId = StringUtils.hasText(track.getTrackId()) ? track.getTrackId()
                : "%s|%s|%s".formatted(track.getTitle(), track.getArtist(), track.getAlbum());
        return source + ":" + trackId;
    }

    private String playlistKey(MusicPlaylistSummary playlist) {
        String source = StringUtils.hasText(playlist.getSource()) ? playlist.getSource() : "unknown";
        String playlistId = StringUtils.hasText(playlist.getPlaylistId()) ? playlist.getPlaylistId()
                : "%s|%s".formatted(playlist.getTitle(), playlist.getAuthor());
        return source + ":" + playlistId;
    }
}
