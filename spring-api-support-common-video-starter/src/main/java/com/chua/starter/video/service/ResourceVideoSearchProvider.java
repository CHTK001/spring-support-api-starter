package com.chua.starter.video.service;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.search.SearchResultFormatter;
import com.chua.common.support.search.VideoWebSearchProvider;
import com.chua.common.support.video.model.VideoInfoResult;
import com.chua.common.support.video.model.VideoSearch;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频搜索 SPI 提供者。
 * <p>
 * 实现 {@link VideoWebSearchProvider}，委托 {@link ResourceSearchManager} 执行搜索，
 * 将结构化的视频信息格式化为文本条目。
 * <p>
 * Spring 启动时自动注册到 SPI 系统，供 {@code DefaultVideoWebSearch} 聚合。
 *
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceVideoSearchProvider implements VideoWebSearchProvider {

    private final ResourceSearchManager resourceSearchManager;

    @PostConstruct
    public void register() {
        ServiceProvider.of(VideoWebSearchProvider.class).register("resource-video", this);
        log.debug("[Video] ResourceVideoSearchProvider registered to SPI");
    }

    @Override
    public List<String> search(String query, int maxResults) {
        try {
            VideoSearch videoSearch = new VideoSearch();
            videoSearch.setKeyword(query);
            videoSearch.setPage(1);
            videoSearch.setPageSize(maxResults);

            ReturnPageResult<VideoInfoResult> result = resourceSearchManager.searchFromAllResources(videoSearch);
            if (result == null || result.data() == null
                    || result.data().getData() == null
                    || result.data().getData().isEmpty()) {
                return List.of();
            }

            List<String> entries = new ArrayList<>();
            for (VideoInfoResult video : result.data().getData()) {
                if (video == null) continue;
                StringBuilder sb = new StringBuilder();
                sb.append(SearchResultFormatter.formatField("Title", video.getVideoTitle()));
                sb.append(", ");
                sb.append(SearchResultFormatter.formatField("Name", video.getVideoName()));
                if (video.getVideoYear() != null && video.getVideoYear() > 0) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Year", video.getVideoYear()));
                }
                if (video.getVideoScore() != null) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Score", video.getVideoScore()));
                }
                if (video.getTags() != null && !video.getTags().isBlank()) {
                    sb.append(", ");
                    sb.append(SearchResultFormatter.formatField("Tags", video.getTags()));
                }
                entries.add(sb.toString());
            }
            return entries;
        } catch (Exception e) {
            log.warn("[ResourceVideoSearchProvider] search failed: query={}, error={}", query, e.getMessage());
            return List.of();
        }
    }
}
