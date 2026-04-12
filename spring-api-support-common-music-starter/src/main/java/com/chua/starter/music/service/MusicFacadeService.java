package com.chua.starter.music.service;

import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistCategoryCatalog;
import com.chua.starter.music.model.MusicPlaylistCategoryResult;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicPlaylistSummary;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;
import com.chua.starter.music.model.MusicTrackSummary;
import com.chua.starter.music.spi.MusicSourceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicFacadeService {

    private final MusicSourceRegistry musicSourceRegistry;

    public List<MusicSourceOption> listSources() {
        return musicSourceRegistry.listSources();
    }

    public MusicOverview getOverview(String source) {
        MusicSourceProvider provider = musicSourceRegistry.requireProvider(source);
        MusicOverview overview = provider.getOverview();
        overview.setSources(listSources());
        overview.setDefaultSource(musicSourceRegistry.resolveSource(source));
        return overview;
    }

    public MusicSearchResult search(String source, String keyword, int page, int pageSize) {
        if (musicSourceRegistry.isAggregateSource(source)) {
            List<MusicTrackSummary> mergedTracks = new ArrayList<>();
            long total = 0L;
            for (MusicSourceProvider provider : musicSourceRegistry.listProviders()) {
                MusicSearchResult result = provider.search(keyword, 1, pageSize);
                if (result == null) {
                    continue;
                }
                total += result.getTotal() == null ? 0L : result.getTotal();
                if (result.getTracks() != null) {
                    mergedTracks.addAll(result.getTracks());
                }
            }
            int fromIndex = Math.max(0, (page - 1) * pageSize);
            int toIndex = Math.min(mergedTracks.size(), fromIndex + pageSize);
            List<MusicTrackSummary> tracks = fromIndex >= mergedTracks.size()
                    ? List.of()
                    : mergedTracks.subList(fromIndex, toIndex);
            return MusicSearchResult.builder()
                    .source("all")
                    .keyword(keyword)
                    .page(page)
                    .pageSize(pageSize)
                    .total(total)
                    .tracks(tracks)
                    .build();
        }
        return musicSourceRegistry.requireProvider(source).search(keyword, page, pageSize);
    }

    public MusicPlaylistSearchResult searchPlaylists(String source, String keyword, int page, int pageSize) {
        if (musicSourceRegistry.isAggregateSource(source)) {
            List<MusicPlaylistSummary> mergedPlaylists = new ArrayList<>();
            long total = 0L;
            for (MusicSourceProvider provider : musicSourceRegistry.listProviders()) {
                MusicPlaylistSearchResult result = provider.searchPlaylists(keyword, 1, pageSize);
                if (result == null) {
                    continue;
                }
                total += result.getTotal() == null ? 0L : result.getTotal();
                if (result.getPlaylists() != null) {
                    mergedPlaylists.addAll(result.getPlaylists());
                }
            }
            int fromIndex = Math.max(0, (page - 1) * pageSize);
            int toIndex = Math.min(mergedPlaylists.size(), fromIndex + pageSize);
            List<MusicPlaylistSummary> playlists = fromIndex >= mergedPlaylists.size()
                    ? List.of()
                    : mergedPlaylists.subList(fromIndex, toIndex);
            return MusicPlaylistSearchResult.builder()
                    .source("all")
                    .keyword(keyword)
                    .page(page)
                    .pageSize(pageSize)
                    .total(total)
                    .playlists(playlists)
                    .build();
        }
        return musicSourceRegistry.requireProvider(source).searchPlaylists(keyword, page, pageSize);
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
}
