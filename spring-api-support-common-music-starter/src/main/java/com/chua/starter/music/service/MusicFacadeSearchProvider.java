package com.chua.starter.music.service;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.search.MusicWebSearchProvider;
import com.chua.common.support.search.SearchResultFormatter;
import com.chua.data.support.music.model.MusicSearchResult;
import com.chua.data.support.music.model.MusicTrackSummary;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐搜索 SPI 提供者。
 * <p>
 * 实现 {@link MusicWebSearchProvider}，委托 {@link MusicFacadeService} 执行搜索，
 * 将结构化的 {@link MusicSearchResult} 格式化为文本条目。
 * <p>
 * Spring 启动时自动注册到 SPI 系统，供 {@code DefaultMusicWebSearch} 聚合。
 *
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MusicFacadeSearchProvider implements MusicWebSearchProvider {

    /** Musicfacade服务 */
    private final MusicFacadeService musicFacadeService;

    @PostConstruct
    /** 注册 */
    public void register() {
        ServiceProvider.of(MusicWebSearchProvider.class).register("music-facade", this);
        log.debug("[Music] MusicFacadeSearchProvider registered to SPI");
    }

    @Override
    /** 搜索 */
    public List<String> search(String query, int maxResults) {
        try {
            MusicSearchResult result = musicFacadeService.search("all", query, 1, maxResults);
            if (result == null || result.getTracks() == null || result.getTracks().isEmpty()) {
                return List.of();
            }

            List<String> entries = new ArrayList<>();
            for (MusicTrackSummary track : result.getTracks()) {
                if (track == null) continue;
                StringBuilder sb = new StringBuilder();
                sb.append(SearchResultFormatter.formatField("Title", track.getTitle()));
                sb.append(", ");
                sb.append(SearchResultFormatter.formatField("Artist", track.getArtist()));
                if (track.getAlbum() != null && !track.getAlbum().isBlank()) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Album", track.getAlbum()));
                }
                if (track.getDurationSeconds() != null && track.getDurationSeconds() > 0) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Duration", track.getDurationSeconds() + "s"));
                }
                if (track.getSource() != null && !track.getSource().isBlank()) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Source", track.getSource()));
                }
                entries.add(sb.toString());
            }
            return entries;
        } catch (Exception e) {
            log.warn("[MusicFacadeSearchProvider] search failed: query={}, error={}", query, e.getMessage());
            return List.of();
        }
    }
}
