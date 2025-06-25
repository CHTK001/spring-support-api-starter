package com.chua.starter.monitor.starter.task;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 系统清理定时任务
 * 统一管理各种清理任务，避免重复
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemCleanupTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;

    /**
     * 清理过期指标数据
     * 每小时执行一次，清理超过24小时的指标数据
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredMetrics() {
        try {
            log.info("开始清理过期的服务器指标数据");

            // 清理超过24小时的历史数据
            LocalDateTime expireTime = LocalDateTime.now().minusHours(24);

            ReturnResult<Integer> result = metricsService.cleanupExpiredMetrics(expireTime);
            if (result != null && result.isOk()) {
                log.info("过期指标数据清理完成，清理了 {} 条记录", result.getData());
            }

        } catch (Exception e) {
            log.error("清理过期指标数据失败", e);
        }
    }

    /**
     * 清理过期连接状态数据
     * 每天凌晨2点执行，清理超过7天的连接状态历史数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredConnectionStatus() {
        try {
            log.info("开始清理过期的连接状态数据");

            // 清理超过7天的连接状态历史数据
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);

            ReturnResult<Integer> result = serverService.cleanupExpiredConnectionStatus(expireTime);
            if (result != null && result.isOk()) {
                log.info("过期连接状态数据清理完成，清理了 {} 条记录", result.getData());
            }

        } catch (Exception e) {
            log.error("清理过期连接状态数据失败", e);
        }
    }

    /**
     * 清理临时文件和缓存
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTempFilesAndCache() {
        try {
            log.info("开始清理临时文件和缓存");

            // 清理临时文件
            ReturnResult<Integer> tempFileResult = serverService.cleanupTempFiles();
            if (tempFileResult != null && tempFileResult.isOk()) {
                log.info("临时文件清理完成，清理了 {} 个文件", tempFileResult.getData());
            }

            // 清理缓存
            ReturnResult<Void> cacheResult = serverService.cleanupCache();
            if (cacheResult != null && cacheResult.isOk()) {
                log.info("缓存清理完成");
            }

        } catch (Exception e) {
            log.error("清理临时文件和缓存失败", e);
        }
    }

    /**
     * 数据库优化任务
     * 每周日凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void optimizeDatabase() {
        try {
            log.info("开始执行数据库优化任务");

            // 优化指标表
            ReturnResult<Void> metricsOptResult = metricsService.optimizeTables();
            if (metricsOptResult != null && metricsOptResult.isOk()) {
                log.info("指标表优化完成");
            }

            // 优化服务器表
            ReturnResult<Void> serverOptResult = serverService.optimizeTables();
            if (serverOptResult != null && serverOptResult.isOk()) {
                log.info("服务器表优化完成");
            }

            log.info("数据库优化任务完成");

        } catch (Exception e) {
            log.error("数据库优化任务失败", e);
        }
    }

    /**
     * 系统健康检查
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void systemHealthCheck() {
        try {
            log.debug("开始系统健康检查");

            // 检查数据库连接
            ReturnResult<Void> dbHealthResult = serverService.checkDatabaseHealth();
            if (dbHealthResult != null && !dbHealthResult.isOk()) {
                log.warn("数据库健康检查失败: {}", dbHealthResult.getMsg());
            }

            // 检查磁盘空间
            ReturnResult<Void> diskHealthResult = serverService.checkDiskSpace();
            if (diskHealthResult != null && !diskHealthResult.isOk()) {
                log.warn("磁盘空间检查失败: {}", diskHealthResult.getMsg());
            }

            // 检查内存使用情况
            ReturnResult<Void> memoryHealthResult = serverService.checkMemoryUsage();
            if (memoryHealthResult != null && !memoryHealthResult.isOk()) {
                log.warn("内存使用检查失败: {}", memoryHealthResult.getMsg());
            }

            log.debug("系统健康检查完成");

        } catch (Exception e) {
            log.error("系统健康检查失败", e);
        }
    }
}
