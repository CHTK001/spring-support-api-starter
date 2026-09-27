package com.chua.starter.video.job;

import com.chua.starter.job.support.annotation.Job;
import com.chua.starter.video.setting.VideoConfigSyncService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 视频/直播配置同步任务。
 *
 * <p>由 job-starter（{@code @Job} 注解）扫描注册并按 cron 触发，调用
 * {@link VideoConfigSyncService#sync()} 拉取远程配置快照并热加载。</p>
 *
 * <p>默认每 10 分钟执行一次；可在 job-starter 治理端调整调度时间，
 * 或通过 {@code spring.video.config-sync-enabled=false} 关闭任务注册。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class VideoConfigSyncJob {

    /**
     * 配置同步服务。
     */
    private final VideoConfigSyncService syncService;

    /**
     * 构造配置同步任务。
     *
     * @param syncService 配置同步服务
     */
    public VideoConfigSyncJob(VideoConfigSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * 任务执行入口（无参）。
     */
    @Job(value = "video.config.sync", desc = "视频/直播配置同步（拉取远程快照并热加载）",
            cron = "0 0/10 * * * ?")
    public void execute() {
        try {
            Map<String, Object> result = syncService.sync();
            log.info("[Video][job] 视频/直播配置同步完成: {}", result);
        } catch (Exception e) {
            log.warn("[Video][job] 视频/直播配置同步失败: {}", e.getMessage());
        }
    }
}
