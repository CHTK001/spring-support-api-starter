package com.chua.starter.music.service;

import com.chua.data.support.music.model.MusicOverview;
import com.chua.data.support.music.model.MusicPlaylistCategoryCatalog;
import com.chua.data.support.music.model.MusicPlaylistCategoryResult;
import com.chua.data.support.music.model.MusicPlaylistDetail;
import com.chua.data.support.music.model.MusicPlaylistSearchResult;
import com.chua.data.support.music.model.MusicPlaylistSummary;
import com.chua.data.support.music.model.MusicSearchResult;
import com.chua.data.support.music.model.MusicSourceOption;
import com.chua.data.support.music.model.MusicTrackDetail;
import com.chua.data.support.music.model.MusicTrackSummary;
import com.chua.data.support.music.spi.MusicSourceProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.chua.common.support.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
/** @作者 CH */

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicFacadeService {

    /** Music来源注册表 */
    private final MusicSourceRegistry musicSourceRegistry;

    /**
    * 列表源
    *
    * @return 列表源的结果
     */
    public List<MusicSourceOption> listSources() {
        return musicSourceRegistry.listSources();
    }

    /**
    * 获取Overview
    *
    * @param source 源
    * @return 获取overview的结果
     */
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

    /**
    * 搜索
    *
    * @param source 源
    * @param keyword keyword
    * @param page page
    * @param pageSize page大小
    * @return 搜索的结果
     */
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

    /**
    * 搜索Playlists
    *
    * @param source 源
    * @param keyword keyword
    * @param page page
    * @param pageSize page大小
    * @return 搜索playlists的结果
     */
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

    /**
    * 获取playlist分类catalog
    *
    * @param source 源
    * @return 获取playlist分类catalog的结果
     */
    public MusicPlaylistCategoryCatalog getPlaylistCategoryCatalog(String source) {
        return musicSourceRegistry.requireProvider(source).getPlaylistCategoryCatalog();
    }

    /**
    * 获取分类playlists
    *
    * @param source 源
    * @param tagId 标签id
    * @param page page
    * @param pageSize page大小
    * @return 获取分类playlists的结果
     */
    public MusicPlaylistCategoryResult getCategoryPlaylists(String source, String tagId, int page, int pageSize) {
        return musicSourceRegistry.requireProvider(source).getCategoryPlaylists(tagId, page, pageSize);
    }

    /**
    * 获取playlistdetail
    *
    * @param source 源
    * @param playlistId playlistId
    * @return 获取playlistdetail的结果
     */
    public MusicPlaylistDetail getPlaylistDetail(String source, String playlistId) {
        return musicSourceRegistry.requireProvider(source).getPlaylistDetail(playlistId);
    }

    /**
    * 获取trackdetail
    *
    * @param source 源
    * @param trackId trackId
    * @return 获取trackdetail的结果
     */
    public MusicTrackDetail getTrackDetail(String source, String trackId) {
        return musicSourceRegistry.requireProvider(source).getTrackDetail(trackId);
    }

    /**
    * enrichoverview
    *
    * @param overview overview
    * @param defaultSource 默认源
    * @return enrichOverview的结果
     */
    private MusicOverview enrichOverview(MusicOverview overview, String defaultSource) {
        MusicOverview target = overview == null ? MusicOverview.builder()
                .hotKeywords(List.of())
                .featuredPlaylists(List.of())
                .build() : overview;
        target.setSources(listSources());
        target.setDefaultSource(defaultSource);
        return target;
    }

    /**
    * 是否拥有overview内容
    *
    * @param overview overview
    * @return 是否包含overview内容的结果
     */
    private boolean hasOverviewContent(MusicOverview overview) {
        return overview != null
                && ((overview.getHotKeywords() != null && !overview.getHotKeywords().isEmpty())
                || (overview.getFeaturedPlaylists() != null && !overview.getFeaturedPlaylists().isEmpty()));
    }

    /**
    * normalizepage
    *
    * @param page page
    * @return normalizePage的结果
     */
    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    /**
    * normalizepage获取大小
    *
    * @param pageSize page大小
    * @return normalizepage大小的结果
     */
    private int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), 100);
    }

    /**
    * page的
    *
    * @param items items
    * @param page page
    * @param pageSize page大小
    * @return page的的结果
     */
    private <T> List<T> pageOf(List<T> items, int page, int pageSize) {
        int fromIndex = Math.max(0, (page - 1) * pageSize);
        int toIndex = Math.min(items.size(), fromIndex + pageSize);
        if (fromIndex >= items.size()) {
            return List.of();
        }
        return items.subList(fromIndex, toIndex);
    }

    /**
    * 调用Safely
    *
    * @param provider 提供者
    * @param operation operation
    * @param supplier 供应商
    * @return invokeSafely的结果
     */
    private <T> T invokeSafely(MusicSourceProvider provider, String operation, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            log.warn("Music provider call failed, source={}, operation={}, message={}",
                    providerCode(provider), operation, ex.getMessage(), ex);
            return null;
        }
    }

    /**
    * 提供者编码
    *
    * @param provider 提供者
    * @return 提供者编码的结果
     */
    private String providerCode(MusicSourceProvider provider) {
        if (provider == null || provider.getSource() == null || !StringUtils.hasText(provider.getSource().getCode())) {
            return "unknown";
        }
        return provider.getSource().getCode();
    }

    /**
    * track键
    *
    * @param track track
    * @return track键的结果
     */
    private String trackKey(MusicTrackSummary track) {
        String source = StringUtils.hasText(track.getSource()) ? track.getSource() : "unknown";
        String trackId = StringUtils.hasText(track.getTrackId()) ? track.getTrackId()
                : "%s|%s|%s".formatted(track.getTitle(), track.getArtist(), track.getAlbum());
        return source + ":" + trackId;
    }

    /**
    * playlist键
    *
    * @param playlist playlist
    * @return playlist键的结果
     */
    private String playlistKey(MusicPlaylistSummary playlist) {
        String source = StringUtils.hasText(playlist.getSource()) ? playlist.getSource() : "unknown";
        String playlistId = StringUtils.hasText(playlist.getPlaylistId()) ? playlist.getPlaylistId()
                : "%s|%s".formatted(playlist.getTitle(), playlist.getAuthor());
        return source + ":" + playlistId;
    }
}

