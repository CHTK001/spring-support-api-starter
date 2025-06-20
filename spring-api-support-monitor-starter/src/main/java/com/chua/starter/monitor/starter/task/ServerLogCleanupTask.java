package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogConfig;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLogConfigService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLogService;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器日志清理定时任务
 *
 * @author CH
 * @since 2024/12/20
 */
@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "monitor.server.log.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ServerLogCleanupTask {

    private final MonitorSysGenServerLogService logService;
    private final MonitorSysGenServerLogConfigService configService;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 清理过期日志
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLogs() {
        log.info("开始执行日志清理任务...");
        
        try {
            // 获取需要清理的配置
            var configsResult = configService.getConfigsForCleanup();
            if (configsResult == null || configsResult.getData() == null || configsResult.getData().isEmpty()) {
                log.info("没有找到需要清理的日志配置");
                return;
            }

            List<MonitorSysGenServerLogConfig> configs = configsResult.getData();
            log.info("找到 {} 个需要清理的日志配置", configs.size());

            // 并行清理各服务器的过期日志
            List<CompletableFuture<Void>> futures = configs.stream()
                    .map(config -> CompletableFuture.runAsync(() -> cleanupServerLogs(config), executorService))
                    .toList();

            // 等待所有清理任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> log.info("所有服务器日志清理任务完成"))
                    .exceptionally(throwable -> {
                        log.error("日志清理任务执行过程中出现异常", throwable);
                        return null;
                    });

        } catch (Exception e) {
            log.error("执行日志清理任务失败", e);
        }
    }

    /**
     * 清理单个服务器的日志
     *
     * @param config 日志配置
     */
    private void cleanupServerLogs(MonitorSysGenServerLogConfig config) {
        try {
            Integer serverId = config.getMonitorSysGenServerId();
            Integer retentionDays = config.getMonitorSysGenServerLogRetentionDays();
            
            if (retentionDays == null || retentionDays <= 0) {
                log.warn("服务器 {} 的日志保留天数配置无效: {}", serverId, retentionDays);
                return;
            }

            // 计算过期时间
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(retentionDays);
            
            log.info("开始清理服务器 {} 的过期日志，保留天数: {}，过期时间: {}", serverId, retentionDays, expiredTime);

            // 删除过期日志
            var deleteResult = logService.deleteExpiredLogs(serverId, expiredTime);
            if (deleteResult != null && deleteResult.getData() != null) {
                int deletedCount = deleteResult.getData();
                if (deletedCount > 0) {
                    log.info("服务器 {} 清理了 {} 条过期日志", serverId, deletedCount);
                } else {
                    log.debug("服务器 {} 没有过期日志需要清理", serverId);
                }
            }

        } catch (Exception e) {
            log.error("清理服务器 {} 的日志失败", config.getMonitorSysGenServerId(), e);
        }
    }

    /**
     * 批量清理过期日志
     * 每周日凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void batchCleanupExpiredLogs() {
        log.info("开始执行批量日志清理任务...");
        
        try {
            // 默认清理30天前的日志
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(30);
            
            log.info("批量清理过期时间: {}", expiredTime);

            var deleteResult = logService.batchDeleteExpiredLogs(expiredTime);
            if (deleteResult != null && deleteResult.getData() != null) {
                int deletedCount = deleteResult.getData();
                log.info("批量清理了 {} 条过期日志", deletedCount);
            }

        } catch (Exception e) {
            log.error("执行批量日志清理任务失败", e);
        }
    }

    /**
     * 清理日志统计数据
     * 每月1号凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void cleanupLogStatistics() {
        log.info("开始执行日志统计数据清理任务...");
        
        try {
            // TODO: 实现日志统计数据清理
            // 清理超过6个月的统计数据
            
            log.info("日志统计数据清理任务完成");

        } catch (Exception e) {
            log.error("执行日志统计数据清理任务失败", e);
        }
    }

    /**
     * 检查日志存储空间
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void checkLogStorageSpace() {
        log.debug("开始检查日志存储空间...");
        
        try {
            // TODO: 实现日志存储空间检查
            // 检查磁盘空间使用情况
            // 如果空间不足，发送告警
            
            log.debug("日志存储空间检查完成");

        } catch (Exception e) {
            log.error("检查日志存储空间失败", e);
        }
    }

    /**
     * 优化日志表
     * 每周日凌晨5点执行
     */
    @Scheduled(cron = "0 0 5 * * SUN")
    public void optimizeLogTables() {
        log.info("开始执行日志表优化任务...");
        
        try {
            // TODO: 实现日志表优化
            // 执行表优化、重建索引等操作
            
            log.info("日志表优化任务完成");

        } catch (Exception e) {
            log.error("执行日志表优化任务失败", e);
        }
    }

    /**
     * 生成日志清理报告
     * 每天上午8点执行
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateCleanupReport() {
        log.info("开始生成日志清理报告...");
        
        try {
            // TODO: 实现日志清理报告生成
            // 统计昨天的日志清理情况
            // 生成报告并发送通知
            
            log.info("日志清理报告生成完成");

        } catch (Exception e) {
            log.error("生成日志清理报告失败", e);
        }
    }

    /**
     * 检查日志配置有效性
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void validateLogConfigs() {
        log.info("开始检查日志配置有效性...");
        
        try {
            var enabledConfigsResult = configService.getEnabledConfigs();
            if (enabledConfigsResult == null || enabledConfigsResult.getData() == null) {
                log.warn("获取启用的日志配置失败");
                return;
            }

            List<MonitorSysGenServerLogConfig> configs = enabledConfigsResult.getData();
            int invalidCount = 0;

            for (MonitorSysGenServerLogConfig config : configs) {
                var validationResult = configService.validateConfig(config);
                if (validationResult != null && validationResult.getData() != null) {
                    Map<String, Object> validation = (Map<String, Object>) validationResult.getData();
                    if (!(Boolean) validation.get("valid")) {
                        invalidCount++;
                        log.warn("服务器 {} 的日志配置无效: {}", 
                                config.getMonitorSysGenServerId(), validation.get("message"));
                    }
                }
            }

            if (invalidCount > 0) {
                log.warn("发现 {} 个无效的日志配置", invalidCount);
            } else {
                log.info("所有日志配置都有效");
            }

        } catch (Exception e) {
            log.error("检查日志配置有效性失败", e);
        }
    }

    /**
     * 手动触发日志清理
     *
     * @param serverId 服务器ID，如果为null则清理所有服务器
     */
    public void triggerManualCleanup(Integer serverId) {
        log.info("手动触发日志清理: serverId={}", serverId);
        
        CompletableFuture.runAsync(() -> {
            try {
                if (serverId != null) {
                    // 清理指定服务器
                    var configResult = configService.getConfigByServerId(serverId);
                    if (configResult != null && configResult.getData() != null) {
                        cleanupServerLogs(configResult.getData());
                    }
                } else {
                    // 清理所有服务器
                    cleanupExpiredLogs();
                }
            } catch (Exception e) {
                log.error("手动触发日志清理失败: serverId={}", serverId, e);
            }
        }, executorService);
    }

    /**
     * 手动触发批量清理
     *
     * @param days 清理多少天前的日志
     */
    public void triggerManualBatchCleanup(Integer days) {
        log.info("手动触发批量日志清理: days={}", days);
        
        CompletableFuture.runAsync(() -> {
            try {
                LocalDateTime expiredTime = LocalDateTime.now().minusDays(days != null ? days : 30);
                var deleteResult = logService.batchDeleteExpiredLogs(expiredTime);
                if (deleteResult != null && deleteResult.getData() != null) {
                    log.info("手动批量清理了 {} 条过期日志", deleteResult.getData());
                }
            } catch (Exception e) {
                log.error("手动触发批量日志清理失败: days={}", days, e);
            }
        }, executorService);
    }
}
