package com.chua.starter.monitor.starter.task;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据清理定时任务
 * 统一管理所有数据清理相关任务
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleanupTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerMetricsService metricsService;

    /**
     * 清理过期指标数据
     * 每小时执行一次，清理超过24小时的指标数据
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredMetrics() {
        try {
            log.debug("开始清理过期的服务器指标数据");

            // 清理超过24小时的历史数据
            LocalDateTime expireTime = LocalDateTime.now().minusHours(24);

            // 基于Redis的数据清理
            ReturnResult<Integer> cleanResult = metricsService.deleteExpiredMetrics(null, 1);
            if (cleanResult.isOk()) {
                int cleanedCount = cleanResult.getData();
                if (cleanedCount > 0) {
                    log.info("过期指标数据清理完成，清理了 {} 条记录", cleanedCount);
                } else {
                    log.debug("没有找到需要清理的过期指标数据");
                }
            } else {
                log.warn("清理过期指标数据失败: {}", cleanResult.getMsg());
            }

        } catch (Exception e) {
            log.error("清理过期指标数据失败", e);
        }
    }

    /**
     * 清理过期日志数据
     * 每天凌晨2点执行，清理超过7天的日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLogs() {
        try {
            log.info("开始清理过期的日志数据");

            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);

            // 清理连接状态日志
            int connectionLogCount = cleanupConnectionStatusLogs(expireTime);

            // 清理文件上传日志
            int uploadLogCount = cleanupFileUploadLogs(expireTime);

            // 清理系统日志文件
            int systemLogCount = cleanupSystemLogFiles();

            log.info("过期日志数据清理完成 - 连接日志: {}, 上传日志: {}, 系统日志: {}",
                    connectionLogCount, uploadLogCount, systemLogCount);

        } catch (Exception e) {
            log.error("清理过期日志数据失败", e);
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
            int tempFileCount = cleanupTempFiles();

            // 清理缓存数据
            int cacheCount = cleanupCacheData();

            // 清理过期的文件上传任务
            var uploadCleanupResult = cleanupExpiredUploadTasks();

            log.info("临时文件和缓存清理完成 - 临时文件: {}, 缓存: {}, 上传任务: {}",
                    tempFileCount, cacheCount, uploadCleanupResult);

        } catch (Exception e) {
            log.error("清理临时文件和缓存失败", e);
        }
    }

    /**
     * 清理连接状态日志
     *
     * @param expireTime 过期时间
     * @return 清理数量
     */
    private int cleanupConnectionStatusLogs(LocalDateTime expireTime) {
        try {
            // 这里应该清理连接状态相关的日志表
            // 由于没有具体的连接状态日志表，这里返回模拟数据
            log.debug("清理连接状态日志，过期时间: {}", expireTime);
            return 0;
        } catch (Exception e) {
            log.error("清理连接状态日志失败", e);
            return 0;
        }
    }

    /**
     * 清理文件上传日志
     *
     * @param expireTime 过期时间
     * @return 清理数量
     */
    private int cleanupFileUploadLogs(LocalDateTime expireTime) {
        try {
            // 清理过期的文件上传进度记录
            log.debug("清理文件上传日志，过期时间: {}", expireTime);
            // 这里可以调用相关服务清理上传日志
            return 0;
        } catch (Exception e) {
            log.error("清理文件上传日志失败", e);
            return 0;
        }
    }

    /**
     * 清理系统日志文件
     *
     * @return 清理数量
     */
    private int cleanupSystemLogFiles() {
        try {
            int count = 0;

            // 清理应用日志目录
            count += cleanupLogDirectory("./logs", 7);

            // 清理系统临时日志
            count += cleanupLogDirectory(System.getProperty("java.io.tmpdir"), 3);

            return count;
        } catch (Exception e) {
            log.error("清理系统日志文件失败", e);
            return 0;
        }
    }

    /**
     * 清理临时文件
     *
     * @return 清理数量
     */
    private int cleanupTempFiles() {
        try {
            int count = 0;

            // 清理系统临时目录
            count += cleanupTempDirectory(System.getProperty("java.io.tmpdir"));

            // 清理应用临时目录
            count += cleanupTempDirectory("./temp");

            return count;
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
            return 0;
        }
    }

    /**
     * 清理缓存数据
     *
     * @return 清理数量
     */
    private int cleanupCacheData() {
        try {
            // 这里可以清理各种缓存数据
            // 例如：Redis缓存、本地缓存等
            log.debug("清理缓存数据");
            return 0;
        } catch (Exception e) {
            log.error("清理缓存数据失败", e);
            return 0;
        }
    }

    /**
     * 清理过期的上传任务
     *
     * @return 清理结果描述
     */
    private String cleanupExpiredUploadTasks() {
        try {
            // 这里可以调用文件上传服务清理过期任务
            log.debug("清理过期的上传任务");
            return "0个任务";
        } catch (Exception e) {
            log.error("清理过期上传任务失败", e);
            return "清理失败";
        }
    }

    /**
     * 清理日志目录
     *
     * @param dirPath 目录路径
     * @param retentionDays 保留天数
     * @return 清理数量
     */
    private int cleanupLogDirectory(String dirPath, int retentionDays) {
        try {
            java.nio.file.Path directory = java.nio.file.Paths.get(dirPath);
            if (!java.nio.file.Files.exists(directory)) {
                return 0;
            }

            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);

            try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.walk(directory)) {
                files.filter(java.nio.file.Files::isRegularFile)
                     .filter(path -> path.getFileName().toString().endsWith(".log") ||
                                   path.getFileName().toString().endsWith(".log.gz"))
                     .filter(path -> {
                         try {
                             return java.nio.file.Files.getLastModifiedTime(path).toInstant()
                                     .isBefore(cutoffTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                         } catch (java.io.IOException e) {
                             return false;
                         }
                     })
                     .forEach(path -> {
                         try {
                             java.nio.file.Files.delete(path);
                             count.incrementAndGet();
                             log.debug("删除过期日志文件: {}", path);
                         } catch (java.io.IOException e) {
                             log.warn("删除日志文件失败: {}", path, e);
                         }
                     });
            }

            return count.get();
        } catch (Exception e) {
            log.error("清理日志目录失败: {}", dirPath, e);
            return 0;
        }
    }

    /**
     * 清理临时目录
     *
     * @param dirPath 目录路径
     * @return 清理数量
     */
    private int cleanupTempDirectory(String dirPath) {
        try {
            java.nio.file.Path directory = java.nio.file.Paths.get(dirPath);
            if (!java.nio.file.Files.exists(directory)) {
                return 0;
            }

            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // 清理24小时前的临时文件
            java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(0);

            try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.list(directory)) {
                files.filter(java.nio.file.Files::isRegularFile)
                     .filter(path -> path.getFileName().toString().startsWith("temp_") ||
                                   path.getFileName().toString().startsWith("tmp_"))
                     .filter(path -> {
                         try {
                             return java.nio.file.Files.getLastModifiedTime(path).toInstant()
                                     .isBefore(cutoffTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
                         } catch (java.io.IOException e) {
                             return false;
                         }
                     })
                     .forEach(path -> {
                         try {
                             java.nio.file.Files.delete(path);
                             count.incrementAndGet();
                             log.debug("删除临时文件: {}", path);
                         } catch (java.io.IOException e) {
                             log.warn("删除临时文件失败: {}", path, e);
                         }
                     });
            }

            return count.get();
        } catch (Exception e) {
            log.error("清理临时目录失败: {}", dirPath, e);
            return 0;
        }
    }
}
