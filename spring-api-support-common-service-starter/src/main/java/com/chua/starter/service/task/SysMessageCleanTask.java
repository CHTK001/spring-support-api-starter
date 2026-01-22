package com.chua.starter.service.task;

import com.chua.common.support.core.utils.NumberUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.SysMessageHistoryService;
import com.chua.starter.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 系统消息清理定时任务
 * 定时清理历史消息，保留天数通过系统设置配置
 * 
 * 配置项：
 * - 设置组: message
 * - 设置名: HistoryRetentionDays
 * - 设置值: 保留天数（默认30天）
 *
 * @author CH
 * @since 2024/12/04
 */
@SpringBootConfiguration
@RequiredArgsConstructor
@Slf4j
public class SysMessageCleanTask {

    private final SysMessageHistoryService messageHistoryService;
    private final SysMessageService messageService;

    /**
     * 默认保留天数
     */
    private static final int DEFAULT_RETENTION_DAYS = 30;

    /**
     * 设置组名称
     */
    private static final String SETTING_GROUP = "message";

    /**
     * 历史消息保留天数设置名
     */
    private static final String SETTING_HISTORY_RETENTION_DAYS = "HistoryRetentionDays";

    /**
     * 定时清理历史消息
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanHistoryMessages() {
        log.info("开始执行历史消息清理定时任务");
        try {
            int retentionDays = getRetentionDays();
            int count = messageHistoryService.cleanOldHistory(retentionDays);
            log.info("历史消息清理任务执行完成，清理了{}条超过{}天的历史消息", count, retentionDays);
        } catch (Exception e) {
            log.error("历史消息清理任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 定时清理过期消息
     * 每天凌晨3点30分执行
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanExpiredMessages() {
        log.info("开始执行过期消息清理定时任务");
        try {
            int count = messageService.cleanExpiredMessages();
            log.info("过期消息清理任务执行完成，清理了{}条过期消息", count);
        } catch (Exception e) {
            log.error("过期消息清理任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取历史消息保留天数
     * 从系统设置中读取，如果未配置则使用默认值
     *
     * @return 保留天数
     */
    private int getRetentionDays() {
        try {
            String value = GlobalSettingFactory.getInstance().get(SETTING_GROUP, SETTING_HISTORY_RETENTION_DAYS);
            if (value != null && !value.isEmpty()) {
                int days = NumberUtils.toInt(value, DEFAULT_RETENTION_DAYS);
                return days > 0 ? days : DEFAULT_RETENTION_DAYS;
            }
        } catch (Exception e) {
            log.warn("读取历史消息保留天数配置失败，使用默认值{}天: {}", DEFAULT_RETENTION_DAYS, e.getMessage());
        }
        return DEFAULT_RETENTION_DAYS;
    }

    /**
     * 手动触发历史消息清理
     *
     * @param days 清理超过指定天数的历史消息，如果为null则使用配置值
     * @return 清理的消息数量
     */
    public int manualCleanHistory(Integer days) {
        log.info("手动触发历史消息清理任务");
        try {
            int retentionDays = days != null && days > 0 ? days : getRetentionDays();
            int count = messageHistoryService.cleanOldHistory(retentionDays);
            log.info("手动历史消息清理任务执行完成，清理了{}条超过{}天的历史消息", count, retentionDays);
            return count;
        } catch (Exception e) {
            log.error("手动历史消息清理任务执行失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 手动触发过期消息清理
     *
     * @return 清理的消息数量
     */
    public int manualCleanExpired() {
        log.info("手动触发过期消息清理任务");
        try {
            int count = messageService.cleanExpiredMessages();
            log.info("手动过期消息清理任务执行完成，清理了{}条过期消息", count);
            return count;
        } catch (Exception e) {
            log.error("手动过期消息清理任务执行失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}
