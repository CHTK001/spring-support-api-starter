package com.chua.starter.music.service;

import com.chua.starter.music.model.MusicOverview;
import com.chua.starter.music.model.MusicPlaylistDetail;
import com.chua.starter.music.model.MusicPlaylistSearchResult;
import com.chua.starter.music.model.MusicPlaylistSummary;
import com.chua.starter.music.model.MusicSearchResult;
import com.chua.starter.music.model.MusicSourceOption;
import com.chua.starter.music.model.MusicTrackDetail;
import com.chua.starter.music.model.MusicTrackSummary;
import com.chua.starter.music.spi.MusicSourceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class MusicFacadeServiceTest {

    @Test
    void aggregateTrackSearchShouldIgnoreProviderFailure() {
        MusicSourceProvider failingProvider = provider("tx",
                overview("tx"),
                () -> {
                    throw new IllegalStateException("provider offline");
                },
                () -> playlists("tx"),
                playlistDetail("tx-playlist", "tx"),
                trackDetail("tx-track", "tx"));
        MusicSourceProvider successProvider = provider("demo",
                overview("demo"),
                () -> tracks("demo", "demo-track", "Demo Track"),
                () -> playlists("demo", "demo-playlist", "Demo Playlist"),
                playlistDetail("demo-playlist", "demo"),
                trackDetail("demo-track", "demo"));

        MusicFacadeService service = new MusicFacadeService(new StubRegistry(List.of(failingProvider, successProvider)));

        MusicSearchResult result = service.search("all", "demo", 1, 10);

        assertThat(result.getSource()).isEqualTo("all");
        assertThat(result.getTracks()).hasSize(1);
        assertThat(result.getTracks().get(0).getSource()).isEqualTo("demo");
        assertThat(result.getTracks().get(0).getTrackId()).isEqualTo("demo-track");
    }

    @Test
    void aggregatePlaylistSearchShouldIgnoreProviderFailure() {
        MusicSourceProvider failingProvider = provider("tx",
                overview("tx"),
                () -> tracks("tx", "tx-track", "Tx Track"),
                () -> {
                    throw new IllegalStateException("playlist endpoint blocked");
                },
                playlistDetail("tx-playlist", "tx"),
                trackDetail("tx-track", "tx"));
        MusicSourceProvider successProvider = provider("git",
                overview("git"),
                () -> tracks("git", "git-track", "Git Track"),
                () -> playlists("git", "git-playlist", "Git Playlist"),
                playlistDetail("git-playlist", "git"),
                trackDetail("git-track", "git"));

        MusicFacadeService service = new MusicFacadeService(new StubRegistry(List.of(failingProvider, successProvider)));

        MusicPlaylistSearchResult result = service.searchPlaylists("all", "git", 1, 10);

        assertThat(result.getSource()).isEqualTo("all");
        assertThat(result.getPlaylists()).hasSize(1);
        assertThat(result.getPlaylists().get(0).getSource()).isEqualTo("git");
        assertThat(result.getPlaylists().get(0).getPlaylistId()).isEqualTo("git-playlist");
    }

    @Test
    void overviewShouldFallBackToNextAvailableProviderWhenDefaultSourceFails() {
        MusicSourceProvider failingDefault = provider("tx",
                () -> {
                    throw new IllegalStateException("overview timeout");
                },
                () -> tracks("tx", "tx-track", "Tx Track"),
                () -> playlists("tx", "tx-playlist", "Tx Playlist"),
                playlistDetail("tx-playlist", "tx"),
                trackDetail("tx-track", "tx"));
        MusicSourceProvider fallbackProvider = provider("kw",
                overview("kw"),
                () -> tracks("kw", "kw-track", "Kw Track"),
                () -> playlists("kw", "kw-playlist", "Kw Playlist"),
                playlistDetail("kw-playlist", "kw"),
                trackDetail("kw-track", "kw"));

        MusicFacadeService service = new MusicFacadeService(new StubRegistry(List.of(failingDefault, fallbackProvider)));

        MusicOverview overview = service.getOverview(null);

        assertThat(overview.getDefaultSource()).isEqualTo("kw");
        assertThat(overview.getHotKeywords()).containsExactly("kw-hot");
        assertThat(overview.getFeaturedPlaylists()).extracting(MusicPlaylistSummary::getSource).containsExactly("kw");
        assertThat(overview.getSources()).extracting(MusicSourceOption::getCode).containsExactly("tx", "kw");
    }

    private static MusicSourceProvider provider(String code,
                                                Supplier<MusicOverview> overviewSupplier,
                                                Supplier<MusicSearchResult> searchSupplier,
                                                Supplier<MusicPlaylistSearchResult> playlistSearchSupplier,
                                                MusicPlaylistDetail playlistDetail,
                                                MusicTrackDetail trackDetail) {
        return new MusicSourceProvider() {
            @Override
            public MusicSourceOption getSource() {
                return MusicSourceOption.builder()
                        .code(code)
                        .name(code)
                        .enabled(Boolean.TRUE)
                        .build();
            }

            @Override
            public MusicOverview getOverview() {
                return overviewSupplier.get();
            }

            @Override
            public MusicSearchResult search(String keyword, int page, int pageSize) {
                return searchSupplier.get();
            }

            @Override
            public MusicPlaylistSearchResult searchPlaylists(String keyword, int page, int pageSize) {
                return playlistSearchSupplier.get();
            }

            @Override
            public MusicPlaylistDetail getPlaylistDetail(String playlistId) {
                return playlistDetail;
            }

            @Override
            public MusicTrackDetail getTrackDetail(String trackId) {
                return trackDetail;
            }
        };
    }

    private static MusicSourceProvider provider(String code,
                                                MusicOverview overview,
                                                Supplier<MusicSearchResult> searchSupplier,
                                                Supplier<MusicPlaylistSearchResult> playlistSearchSupplier,
                                                MusicPlaylistDetail playlistDetail,
                                                MusicTrackDetail trackDetail) {
        return provider(code, () -> overview, searchSupplier, playlistSearchSupplier, playlistDetail, trackDetail);
    }

    private static MusicOverview overview(String source) {
        return MusicOverview.builder()
                .hotKeywords(List.of(source + "-hot"))
                .featuredPlaylists(List.of(MusicPlaylistSummary.builder()
                        .playlistId(source + "-playlist")
                        .source(source)
                        .title(source + " Playlist")
                        .author(source + " Author")
                        .trackCount(1)
                        .build()))
                .build();
    }

    private static MusicSearchResult tracks(String source, String trackId, String title) {
        return MusicSearchResult.builder()
                .source(source)
                .keyword(title)
                .page(1)
                .pageSize(10)
                .total(1L)
                .tracks(List.of(MusicTrackSummary.builder()
                        .trackId(trackId)
                        .source(source)
                        .title(title)
                        .artist(source + " Artist")
                        .album(source + " Album")
                        .durationSeconds(180)
                        .build()))
                .build();
    }

    private static MusicPlaylistSearchResult playlists(String source) {
        return playlists(source, source + "-playlist", source + " Playlist");
    }

    private static MusicPlaylistSearchResult playlists(String source, String playlistId, String title) {
        return MusicPlaylistSearchResult.builder()
                .source(source)
                .keyword(title)
                .page(1)
                .pageSize(10)
                .total(1L)
                .playlists(List.of(MusicPlaylistSummary.builder()
                        .playlistId(playlistId)
                        .source(source)
                        .title(title)
                        .author(source + " Author")
                        .trackCount(1)
                        .build()))
                .build();
    }

    private static MusicPlaylistDetail playlistDetail(String playlistId, String source) {
        return MusicPlaylistDetail.builder()
                .playlistId(playlistId)
                .source(source)
                .title(source + " Playlist")
                .author(source + " Author")
                .trackCount(1)
                .tracks(List.of(MusicTrackSummary.builder()
                        .trackId(source + "-track")
                        .source(source)
                        .title(source + " Track")
                        .artist(source + " Artist")
                        .album(source + " Album")
                        .durationSeconds(180)
                        .build()))
                .build();
    }

    private static MusicTrackDetail trackDetail(String trackId, String source) {
        return MusicTrackDetail.builder()
                .trackId(trackId)
                .source(source)
                .title(source + " Track")
                .artist(source + " Artist")
                .album(source + " Album")
                .streamUrl("https://example.test/" + source + "/" + trackId + ".mp3")
                .durationSeconds(180)
                .build();
    }

    private static final class StubRegistry extends MusicSourceRegistry {
        private static final ObjectProvider<MusicSourceAvailabilityService> EMPTY_AVAILABILITY =
                new ObjectProvider<>() {
                    @Override
                    public MusicSourceAvailabilityService getObject(Object... args) {
                        return null;
                    }

                    @Override
                    public MusicSourceAvailabilityService getIfAvailable() {
                        return null;
                    }

                    @Override
                    public MusicSourceAvailabilityService getIfUnique() {
                        return null;
                    }

                    @Override
                    public MusicSourceAvailabilityService getObject() {
                        return null;
                    }

                    @Override
                    public Iterator<MusicSourceAvailabilityService> iterator() {
                        return List.<MusicSourceAvailabilityService>of().iterator();
                    }
                };
        private final List<MusicSourceProvider> providers;

        private StubRegistry(List<MusicSourceProvider> providers) {
            super(EMPTY_AVAILABILITY);
            this.providers = providers;
        }

        @Override
        public List<MusicSourceOption> listSources() {
            return providers.stream().map(MusicSourceProvider::getSource).toList();
        }

        @Override
        public List<MusicSourceProvider> listProviders() {
            return providers;
        }

        @Override
        public MusicSourceProvider requireProvider(String source) {
            String actualSource = resolveSource(source);
            return providers.stream()
                    .filter(provider -> provider.getSource().getCode().equals(actualSource))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing provider: " + actualSource));
        }
    }
}
