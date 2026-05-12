package com.chua.starter.video.scheduler;

import com.chua.starter.video.service.VideoSyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 视频同步定时任务（@Scheduled 回退方案）
 * <p>当 plugin.job.enable 未设置或为 false 时生效</p>
 *
 * @author CH
 * @since 2024/6/21
 */
@SpringBootConfiguration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnMissingBean(VideoSyncJob.class)
public class VideoSyncScheduler {

    private final VideoSyncTaskService videoSyncTaskService;

    /**
     * 定时同步视频信息
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncVideoInfo() {
        log.info("开始执行视频同步定时任务");
        try {
            int count = videoSyncTaskService.syncAllEnabledConfigs();
            log.info("视频同步定时任务执行完成，共同步{}个视频", count);
        } catch (Exception e) {
            log.error("视频同步定时任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 手动触发同步任务
     *
     * @return 同步的视频数量
     */
    public int manualSync() {
        log.info("手动触发视频同步任务");
        try {
            int count = videoSyncTaskService.syncAllEnabledConfigs();
            log.info("手动视频同步任务执行完成，共同步{}个视频", count);
            return count;
        } catch (Exception e) {
            log.error("手动视频同步任务执行失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}
