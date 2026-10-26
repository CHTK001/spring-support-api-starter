package com.chua.starter.video.job;

import com.chua.starter.job.support.annotation.Job;
import com.chua.starter.video.service.VideoCatalogSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 视频目录同步任务。
 *
 * <p>由 job-starter 扫描 {@code @Job} 注册并按 cron 触发：</p>
 * <ul>
 *   <li>{@code video.catalog.sync}：每 5 分钟把资源站最新目录增量落进
 *       {@code video_info}，列表/筛选/分页随后读本地库；</li>
 *   <li>{@code video.catalog.detail}：每 6 小时刷新连载剧集详情，
 *       集数与网盘地址会随更新变化。</li>
 * </ul>
 *
 * <p>开关：{@code spring.video.catalog-sync-enabled=false} 关闭任务注册。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class VideoCatalogSyncJob {

    /**
     * 目录同步服务。
     */
    private final VideoCatalogSyncService syncService;

    /**
     * 增量同步入口。
     */
    @Job(value = "video.catalog.sync", desc = "视频目录增量同步（资源站 → video_info）",
            cron = "0 0/5 * * * ?")
    public void sync() {
        try {
            Map<String, Object> result = syncService.sync();
            log.info("[Video][job] 目录同步完成: {}", result);
        } catch (Exception e) {
            log.warn("[Video][job] 目录同步失败: {}", e.getMessage());
        }
    }

    /**
     * 连载详情刷新入口。
     */
    @Job(value = "video.catalog.detail", desc = "连载剧集详情刷新（集数/网盘地址）",
            cron = "0 0 0/6 * * ?")
    public void refreshDetails() {
        try {
            Map<String, Object> result = syncService.refreshDetails();
            log.info("[Video][job] 连载详情刷新完成: {}", result);
        } catch (Exception e) {
            log.warn("[Video][job] 连载详情刷新失败: {}", e.getMessage());
        }
    }
}
