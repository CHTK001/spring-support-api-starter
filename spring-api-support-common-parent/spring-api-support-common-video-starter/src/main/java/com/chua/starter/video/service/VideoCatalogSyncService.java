package com.chua.starter.video.service;

import com.chua.common.support.datasearch.video.model.VideoInfoResult;
import com.chua.starter.video.properties.VideoProperties;
import com.chua.starter.video.repository.VideoIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频目录同步服务。
 *
 * <p>把资源站（MacCMS）的最新目录增量落进本地索引 {@code video_info}，
 * 让列表/筛选/分页不再依赖每次请求直连第三方：</p>
 * <ol>
 *   <li>{@link #sync()}：拉取最新 N 页并 upsert，按 originId 去重；</li>
 *   <li>{@link #refreshDetails()}：刷新连载剧集的详情快照
 *       （集数与网盘地址会随更新变化）。</li>
 * </ol>
 *
 * <p>由 {@code VideoCatalogSyncJob} 以 job-starter 的 cron 驱动，也可通过
 * {@code POST /video/api/config/catalog/sync} 手动触发。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class VideoCatalogSyncService {

    /**
     * 视频平台服务（提供资源站读取入口）。
     */
    private final VideoPlatformService platform;

    /**
     * 本地索引。
     */
    private final VideoIndex repository;

    /**
     * 视频配置。
     */
    private final VideoProperties properties;

    /**
     * 本地索引是否可用。
     *
     * @return 可用返回 true
     */
    public boolean available() {
        return repository != null && repository.available();
    }

    /**
     * 增量同步：拉取最新若干页并落库。
     *
     * @return 同步结果摘要
     */
    public Map<String, Object> sync() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", available());
        if (!available()) {
            result.put("message", "本地索引不可用（未引入数据库或 MyBatis-Plus）");
            return result;
        }
        int pages = Math.max(1, properties.getCatalogSyncPages());
        Map<String, VideoInfoResult> collected = new LinkedHashMap<>();
        for (int page = 1; page <= pages; page++) {
            List<VideoInfoResult> items = platform.fetchCatalogPage(page);
            for (VideoInfoResult item : items) {
                String key = item == null ? null
                        : (item.getVideoOriginId() == null || item.getVideoOriginId().isBlank()
                        ? String.valueOf(item.getVideoId()) : item.getVideoOriginId());
                if (key != null && !key.isBlank()) {
                    collected.putIfAbsent(key, item);
                }
            }
        }
        int changed = repository.upsertAll(new ArrayList<>(collected.values()));
        result.put("pages", pages);
        result.put("fetched", collected.size());
        result.put("upserted", changed);
        result.put("total", repository.countAll());
        log.info("[Video] 目录同步完成: pages={} fetched={} upserted={} total={}",
                pages, collected.size(), changed, result.get("total"));
        return result;
    }

    /**
     * 刷新连载剧集详情（集数、网盘地址会变化）。
     *
     * @return 刷新结果摘要
     */
    public Map<String, Object> refreshDetails() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", available());
        if (!available()) {
            result.put("message", "本地索引不可用（未引入数据库或 MyBatis-Plus）");
            return result;
        }
        int limit = Math.max(1, properties.getCatalogDetailRefreshLimit());
        List<String> ids = repository.pendingSeriesIds(limit);
        List<VideoInfoResult> refreshed = new ArrayList<>(ids.size());
        for (String id : ids) {
            VideoInfoResult detail = platform.fetchCatalogDetail(id);
            if (detail != null) {
                refreshed.add(detail);
            }
        }
        int changed = repository.upsertAll(refreshed);
        result.put("requested", ids.size());
        result.put("refreshed", changed);
        log.info("[Video] 连载详情刷新完成: requested={} refreshed={}", ids.size(), changed);
        return result;
    }
}
