package com.chua.starter.video.scheduler;

import com.chua.starter.job.support.annotation.Job;
import com.chua.starter.video.service.VideoSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 视频同步任务（Job Starter 集成）
 * <p>当 plugin.job.enable=true 时生效，替代原 @Scheduled 方式</p>
 *
 * @author CH
 * @since 2024/6/21
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.job", name = "enable", havingValue = "true")
public class VideoSyncJob {

    private final VideoSyncTaskService videoSyncTaskService;

    /**
     * 定时同步视频信息
     */
    @Job(value = "videoSyncJob", desc = "视频源自动同步任务", scheduleType = "cron", scheduleTime = "0 0/1 * * * ?")
    public void syncVideo() {
        log.info("[Job] 开始执行视频同步定时任务");
        try {
            int count = videoSyncTaskService.syncAllEnabledConfigs();
            log.info("[Job] 视频同步定时任务执行完成，共同步{}个视频", count);
        } catch (Exception e) {
            log.error("[Job] 视频同步定时任务执行失败: {}", e.getMessage(), e);
        }
    }
}
