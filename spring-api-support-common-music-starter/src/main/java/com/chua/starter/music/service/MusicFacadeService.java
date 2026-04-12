package com.chua.starter.music.service;

import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;
import com.chua.starter.music.spi.MusicSourceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return musicSourceRegistry.requireProvider(source).search(keyword, page, pageSize);
    }

    public MusicPlaylistSearchResult searchPlaylists(String source, String keyword, int page, int pageSize) {
        return musicSourceRegistry.requireProvider(source).searchPlaylists(keyword, page, pageSize);
    }

    public MusicPlaylistDetail getPlaylistDetail(String source, String playlistId) {
        return musicSourceRegistry.requireProvider(source).getPlaylistDetail(playlistId);
    }

    public MusicTrackDetail getTrackDetail(String source, String trackId) {
        return musicSourceRegistry.requireProvider(source).getTrackDetail(trackId);
    }
}
