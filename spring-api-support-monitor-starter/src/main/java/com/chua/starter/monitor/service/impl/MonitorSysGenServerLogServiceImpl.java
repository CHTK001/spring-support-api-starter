package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLog;
import com.chua.starter.monitor.mapper.MonitorSysGenServerLogMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器日志管理服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
public class MonitorSysGenServerLogServiceImpl extends ServiceImpl<MonitorSysGenServerLogMapper, MonitorSysGenServerLog>
        implements MonitorSysGenServerLogService {

    @Override
    public ReturnResult<IPage<MonitorSysGenServerLog>> getLogPage(Page<MonitorSysGenServerLog> page, Integer serverId,
            String level, String source, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            IPage<MonitorSysGenServerLog> result = baseMapper.selectLogPage(page, serverId, level, source, keyword,
                    startTime, endTime);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("分页查询服务器日志失败", e);
            return ReturnResult.error("查询日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLog>> getLogsByServerAndTime(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            List<MonitorSysGenServerLog> logs = baseMapper.selectLogsByServerAndTime(serverId, startTime, endTime);
            return ReturnResult.success(logs);
        } catch (Exception e) {
            log.error("根据服务器ID和时间范围查询日志失败: serverId={}", serverId, e);
            return ReturnResult.error("查询日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLog>> getRecentErrorLogs(Integer serverId, Integer limit) {
        try {
            List<MonitorSysGenServerLog> errorLogs = baseMapper.getRecentErrorLogs(serverId, limit);
            return ReturnResult.success(errorLogs);
        } catch (Exception e) {
            log.error("获取最近错误日志失败: serverId={}", serverId, e);
            return ReturnResult.error("获取错误日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLog>> getRealTimeLogs(Integer serverId, LocalDateTime lastTime,
            Integer limit) {
        try {
            List<MonitorSysGenServerLog> realTimeLogs = baseMapper.getRealTimeLogs(serverId, lastTime, limit);
            return ReturnResult.success(realTimeLogs);
        } catch (Exception e) {
            log.error("获取实时日志失败: serverId={}", serverId, e);
            return ReturnResult.error("获取实时日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<IPage<MonitorSysGenServerLog>> searchLogContent(Page<MonitorSysGenServerLog> page,
            Integer serverId, String keyword, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            IPage<MonitorSysGenServerLog> result = baseMapper.searchLogContent(page, serverId, keyword, startTime,
                    endTime);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("搜索日志内容失败: serverId={}, keyword={}", serverId, keyword, e);
            return ReturnResult.error("搜索日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> batchSaveLogs(List<MonitorSysGenServerLog> logs) {
        try {
            if (logs == null || logs.isEmpty()) {
                return ReturnResult.success(true);
            }

            // 设置默认值
            LocalDateTime now = LocalDateTime.now();
            for (MonitorSysGenServerLog log : logs) {
                if (log.getMonitorSysGenServerLogCreateTime() == null) {
                    log.setMonitorSysGenServerLogCreateTime(now);
                }
                if (log.getMonitorSysGenServerLogStatus() == null) {
                    log.setMonitorSysGenServerLogStatus(1);
                }
            }

            boolean result = saveBatch(logs);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("批量保存日志失败", e);
            return ReturnResult.error("批量保存日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Integer> deleteExpiredLogs(Integer serverId, LocalDateTime expiredTime) {
        try {
            int deletedCount = baseMapper.deleteExpiredLogs(serverId, expiredTime);
            log.info("删除服务器 {} 的过期日志 {} 条", serverId, deletedCount);
            return ReturnResult.success(deletedCount);
        } catch (Exception e) {
            log.error("删除过期日志失败: serverId={}", serverId, e);
            return ReturnResult.error("删除过期日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Integer> batchDeleteExpiredLogs(LocalDateTime expiredTime) {
        try {
            int deletedCount = baseMapper.batchDeleteExpiredLogs(expiredTime);
            log.info("批量删除过期日志 {} 条", deletedCount);
            return ReturnResult.success(deletedCount);
        } catch (Exception e) {
            log.error("批量删除过期日志失败", e);
            return ReturnResult.error("批量删除过期日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLogLevelStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            List<Map<String, Object>> statistics = baseMapper.getLogLevelStatistics(serverId, startTime, endTime);

            Map<String, Object> result = new HashMap<>();
            Map<String, Long> levelCounts = new HashMap<>();
            long totalCount = 0;

            for (Map<String, Object> stat : statistics) {
                String level = (String) stat.get("level");
                Long count = (Long) stat.get("count");
                levelCounts.put(level, count);
                totalCount += count;
            }

            result.put("levelCounts", levelCounts);
            result.put("totalCount", totalCount);
            result.put("serverId", serverId);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取日志级别统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志级别统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLogSourceStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            List<Map<String, Object>> statistics = baseMapper.getLogSourceStatistics(serverId, startTime, endTime);

            Map<String, Object> result = new HashMap<>();
            Map<String, Long> sourceCounts = new HashMap<>();
            long totalCount = 0;

            for (Map<String, Object> stat : statistics) {
                String source = (String) stat.get("source");
                Long count = (Long) stat.get("count");
                sourceCounts.put(source, count);
                totalCount += count;
            }

            result.put("sourceCounts", sourceCounts);
            result.put("totalCount", totalCount);
            result.put("serverId", serverId);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取日志来源统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志来源统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getLogTimeDistribution(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            List<Map<String, Object>> distribution = baseMapper.getLogTimeDistribution(serverId, startTime, endTime);
            return ReturnResult.success(distribution);
        } catch (Exception e) {
            log.error("获取日志时间分布统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志时间分布统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getExceptionStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = baseMapper.getExceptionStatistics(serverId, startTime, endTime);
            statistics.put("serverId", serverId);
            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("获取异常日志统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取异常日志统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLogSizeStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            Long totalSize = baseMapper.getLogSizeStatistics(serverId, startTime, endTime);

            Map<String, Object> result = new HashMap<>();
            result.put("totalSizeBytes", totalSize);
            result.put("totalSizeMB", totalSize / (1024.0 * 1024.0));
            result.put("totalSizeGB", totalSize / (1024.0 * 1024.0 * 1024.0));
            result.put("serverId", serverId);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取日志大小统计失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志大小统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> generateLogReport(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            Map<String, Object> report = new HashMap<>();

            // 基本信息
            report.put("serverId", serverId);
            report.put("startTime", startTime);
            report.put("endTime", endTime);
            report.put("generateTime", LocalDateTime.now());

            // 日志级别统计
            ReturnResult<Map<String, Object>> levelStatsResult = getLogLevelStatistics(serverId, startTime, endTime);
            if (levelStatsResult != null && levelStatsResult.getData() != null) {
                report.put("levelStatistics", levelStatsResult.getData());
            }

            // 日志来源统计
            ReturnResult<Map<String, Object>> sourceStatsResult = getLogSourceStatistics(serverId, startTime, endTime);
            if (sourceStatsResult != null && sourceStatsResult.getData() != null) {
                report.put("sourceStatistics", sourceStatsResult.getData());
            }

            // 异常统计
            ReturnResult<Map<String, Object>> exceptionStatsResult = getExceptionStatistics(serverId, startTime,
                    endTime);
            if (exceptionStatsResult != null && exceptionStatsResult.getData() != null) {
                report.put("exceptionStatistics", exceptionStatsResult.getData());
            }

            // 日志大小统计
            ReturnResult<Map<String, Object>> sizeStatsResult = getLogSizeStatistics(serverId, startTime, endTime);
            if (sizeStatsResult != null && sizeStatsResult.getData() != null) {
                report.put("sizeStatistics", sizeStatsResult.getData());
            }

            // 时间分布
            ReturnResult<List<Map<String, Object>>> timeDistributionResult = getLogTimeDistribution(serverId, startTime,
                    endTime);
            if (timeDistributionResult != null && timeDistributionResult.getData() != null) {
                report.put("timeDistribution", timeDistributionResult.getData());
            }

            // 最近错误日志
            ReturnResult<List<MonitorSysGenServerLog>> recentErrorsResult = getRecentErrorLogs(serverId, 10);
            if (recentErrorsResult != null && recentErrorsResult.getData() != null) {
                report.put("recentErrors", recentErrorsResult.getData());
            }

            return ReturnResult.success(report);
        } catch (Exception e) {
            log.error("生成日志报告失败: serverId={}", serverId, e);
            return ReturnResult.error("生成日志报告失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportLogs(Integer serverId, String level, String source, LocalDateTime startTime,
            LocalDateTime endTime, String format) {
        try {
            // TODO: 实现日志导出功能
            // 这里可以根据format参数导出为CSV、JSON、TXT等格式

            log.info("导出日志: serverId={}, level={}, source={}, format={}", serverId, level, source, format);

            // 暂时返回成功消息
            return ReturnResult.success("日志导出功能正在开发中");
        } catch (Exception e) {
            log.error("导出日志失败: serverId={}", serverId, e);
            return ReturnResult.error("导出日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> cleanupLogs(Integer serverId) {
        try {
            // TODO: 实现日志清理功能
            Map<String, Object> result = new HashMap<>();
            result.put("serverId", serverId);
            result.put("cleanupTime", LocalDateTime.now());
            result.put("message", "日志清理功能正在开发中");

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("清理日志失败: serverId={}", serverId, e);
            return ReturnResult.error("清理日志失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getLogCollectionStatus(Integer serverId) {
        try {
            // TODO: 实现获取日志收集状态功能
            Map<String, Object> status = new HashMap<>();
            status.put("serverId", serverId);
            status.put("status", "running");
            status.put("lastCollectionTime", LocalDateTime.now());

            return ReturnResult.success(status);
        } catch (Exception e) {
            log.error("获取日志收集状态失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志收集状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startLogCollection(Integer serverId) {
        try {
            // TODO: 实现启动日志收集功能
            log.info("启动日志收集: serverId={}", serverId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("启动日志收集失败: serverId={}", serverId, e);
            return ReturnResult.error("启动日志收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopLogCollection(Integer serverId) {
        try {
            // TODO: 实现停止日志收集功能
            log.info("停止日志收集: serverId={}", serverId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("停止日志收集失败: serverId={}", serverId, e);
            return ReturnResult.error("停止日志收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> restartLogCollection(Integer serverId) {
        try {
            // TODO: 实现重启日志收集功能
            log.info("重启日志收集: serverId={}", serverId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("重启日志收集失败: serverId={}", serverId, e);
            return ReturnResult.error("重启日志收集失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getServerLogStatistics(Integer serverId) {
        try {
            log.info("获取服务器日志统计信息: serverId={}", serverId);

            Map<String, Object> statistics = new HashMap<>();

            // 总日志数
            long total = baseMapper.countByServerId(serverId);
            statistics.put("total", total);

            // 各级别日志数量
            long infoCount = baseMapper.countByServerIdAndLevel(serverId, "INFO");
            statistics.put("infoCount", infoCount);

            long warnCount = baseMapper.countByServerIdAndLevel(serverId, "WARN");
            statistics.put("warnCount", warnCount);

            long errorCount = baseMapper.countByServerIdAndLevel(serverId, "ERROR");
            statistics.put("errorCount", errorCount);

            // 今日日志数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            long todayCount = baseMapper.countByServerIdAndTimeRange(serverId, todayStart, LocalDateTime.now());
            statistics.put("todayCount", todayCount);

            // 最近1小时日志数
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long lastHourCount = baseMapper.countByServerIdAndTimeRange(serverId, oneHourAgo, LocalDateTime.now());
            statistics.put("lastHourCount", lastHourCount);

            log.info("获取服务器日志统计信息完成: serverId={}, statistics={}", serverId, statistics);
            return ReturnResult.success(statistics);

        } catch (Exception e) {
            log.error("获取服务器日志统计信息失败: serverId={}", serverId, e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Integer> cleanupServerLogs(Integer serverId, LocalDateTime beforeDate) {
        try {
            log.info("清理服务器日志: serverId={}, beforeDate={}", serverId, beforeDate);

            if (beforeDate == null) {
                // 默认清理30天前的日志
                beforeDate = LocalDateTime.now().minusDays(30);
            }

            int deletedCount = baseMapper.deleteByServerIdAndBeforeDate(serverId, beforeDate);

            log.info("清理服务器日志完成: serverId={}, 清理记录数={}", serverId, deletedCount);
            return ReturnResult.success(deletedCount);

        } catch (Exception e) {
            log.error("清理服务器日志失败: serverId={}", serverId, e);
            return ReturnResult.error("清理日志失败: " + e.getMessage());
        }
    }
}
