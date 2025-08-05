package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

import com.chua.starter.monitor.starter.enums.AlertLevel;
import com.chua.starter.monitor.starter.enums.AlertStatus;
import com.chua.starter.monitor.starter.enums.AlertType;
import com.chua.starter.monitor.starter.service.AlertDetectionService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerAlertHistoryService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;

import com.chua.starter.redis.support.service.TimeSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警检测服务实现类
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertDetectionServiceImpl implements AlertDetectionService {

    private final MonitorSysGenServerAlertHistoryService alertHistoryService;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final TimeSeriesService timeSeriesService;


    // 告警时间窗口(分钟)
    private static final int DEFAULT_ALERT_WINDOW_MINUTES = 5;


    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> detectMetricsAlerts(
            MonitorSysGenServer server,
            MonitorSysGenServerSetting setting,
            MonitorSysGenServerMetrics metrics) {

        if(null == metrics) {
            return ReturnResult.success(Collections.emptyList());
        }

        try {
            // 直接使用服务器设置中的告警配置
            log.debug("使用服务器设置进行告警检测: serverId={}", server.getMonitorSysGenServerId());

            List<MonitorSysGenServerAlertHistory> alerts = new ArrayList<>();
            Integer serverId = server.getMonitorSysGenServerId();

            // 检测CPU告警
            BigDecimal cpuThreshold = setting.getMonitorSysGenServerSettingCpuAlertThreshold();

            if (metrics.getMonitorSysGenServerMetricsCpuUsage() != null && cpuThreshold != null) {
                BigDecimal cpuUsage = metrics.getMonitorSysGenServerMetricsCpuUsage();

                if (cpuUsage.compareTo(cpuThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.CPU, cpuUsage, cpuThreshold, "%");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            // 检测内存告警
            BigDecimal memoryThreshold = setting.getMonitorSysGenServerSettingMemoryAlertThreshold();

            if (metrics.getMonitorSysGenServerMetricsMemoryUsage() != null && memoryThreshold != null) {
                BigDecimal memoryUsage = metrics.getMonitorSysGenServerMetricsMemoryUsage();

                if (memoryUsage.compareTo(memoryThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.MEMORY, memoryUsage, memoryThreshold, "%");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            // 检测磁盘告警
            BigDecimal diskThreshold = setting.getMonitorSysGenServerSettingDiskAlertThreshold();

            if (metrics.getMonitorSysGenServerMetricsDiskUsage() != null && diskThreshold != null) {
                BigDecimal diskUsage = metrics.getMonitorSysGenServerMetricsDiskUsage();

                if (diskUsage.compareTo(diskThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.DISK, diskUsage, diskThreshold, "%");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            // 检测网络告警（新增）
            BigDecimal networkThreshold = setting.getMonitorSysGenServerSettingNetworkAlertThreshold();

            if (networkThreshold != null &&
                (metrics.getMonitorSysGenServerMetricsNetworkIn() != null ||
                 metrics.getMonitorSysGenServerMetricsNetworkOut() != null)) {

                // 计算网络总流量（入流量 + 出流量），转换为Mbps
                long networkIn = metrics.getMonitorSysGenServerMetricsNetworkIn() != null ?
                    metrics.getMonitorSysGenServerMetricsNetworkIn() : 0L;
                long networkOut = metrics.getMonitorSysGenServerMetricsNetworkOut() != null ?
                    metrics.getMonitorSysGenServerMetricsNetworkOut() : 0L;

                // 假设这是每秒的流量，转换为Mbps (bytes/s -> Mbps)
                BigDecimal totalNetworkMbps = new BigDecimal((networkIn + networkOut) * 8.0 / 1024 / 1024);

                if (totalNetworkMbps.compareTo(networkThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.NETWORK, totalNetworkMbps, networkThreshold, "Mbps");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("检测服务器指标告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("检测服务器指标告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerAlertHistory> detectSingleAlert(
            Integer serverId, 
            AlertType alertType, 
            BigDecimal currentValue, 
            BigDecimal thresholdValue, 
            String unit) {
        
        try {
            // 计算告警级别
            AlertLevel alertLevel = calculateAlertLevel(alertType, currentValue, thresholdValue);
            
            // 生成告警指纹
            String fingerprint = generateAlertFingerprint(serverId, alertType, alertLevel);
            
            // 检查是否重复告警
            if (isDuplicateAlert(fingerprint, DEFAULT_ALERT_WINDOW_MINUTES)) {
                log.debug("重复告警，跳过: serverId={}, alertType={}, fingerprint={}", 
                    serverId, alertType, fingerprint);
                return ReturnResult.success(null);
            }
            
            // 检查是否在静默期
            if (isAlertSilenced(fingerprint)) {
                log.debug("告警在静默期，跳过: serverId={}, alertType={}, fingerprint={}", 
                    serverId, alertType, fingerprint);
                return ReturnResult.success(null);
            }

            // 创建告警记录
            MonitorSysGenServerAlertHistory alert = new MonitorSysGenServerAlertHistory();
            alert.setMonitorSysGenServerAlertHistoryServerId(serverId);
            alert.setMonitorSysGenServerAlertHistoryType(alertType);
            alert.setMonitorSysGenServerAlertHistoryLevel(alertLevel);
            alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
            alert.setMonitorSysGenServerAlertHistoryTitle(generateAlertTitle(alertType, alertLevel));
            alert.setMonitorSysGenServerAlertHistoryContent(generateAlertContent(alertType, currentValue, thresholdValue, unit));
            alert.setMonitorSysGenServerAlertHistoryCurrentValue(currentValue);
            alert.setMonitorSysGenServerAlertHistoryThresholdValue(thresholdValue);
            alert.setMonitorSysGenServerAlertHistoryUnit(unit);
            alert.setMonitorSysGenServerAlertHistoryTriggerTime(LocalDateTime.now());
            alert.setMonitorSysGenServerAlertHistoryFingerprint(fingerprint);
            alert.setMonitorSysGenServerAlertHistoryNotificationSent(false);

            // 保存告警记录
            ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
            if (!saveResult.isOk()) {
                return ReturnResult.error("保存告警记录失败: " + saveResult.getMsg());
            }

            log.info("检测到新告警: serverId={}, alertType={}, level={}, currentValue={}, threshold={}", 
                serverId, alertType, alertLevel, currentValue, thresholdValue);

            return ReturnResult.success(alert);

        } catch (Exception e) {
            log.error("检测单个告警失败: serverId={}, alertType={}", serverId, alertType, e);
            return ReturnResult.error("检测单个告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> detectApiReportAlerts(
            Integer serverId, 
            Map<String, Object> reportData) {
        
        try {
            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            List<MonitorSysGenServerAlertHistory> alerts = new ArrayList<>();

            // 检测CPU告警
            Object cpuUsageObj = reportData.get("cpuUsage");
            if (cpuUsageObj != null && setting.getMonitorSysGenServerSettingCpuAlertThreshold() != null) {
                BigDecimal cpuUsage = new BigDecimal(cpuUsageObj.toString());
                BigDecimal cpuThreshold = setting.getMonitorSysGenServerSettingCpuAlertThreshold();
                
                if (cpuUsage.compareTo(cpuThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.CPU, cpuUsage, cpuThreshold, "%");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            // 检测内存告警
            Object memoryUsageObj = reportData.get("memoryUsage");
            if (memoryUsageObj != null && setting.getMonitorSysGenServerSettingMemoryAlertThreshold() != null) {
                BigDecimal memoryUsage = new BigDecimal(memoryUsageObj.toString());
                BigDecimal memoryThreshold = setting.getMonitorSysGenServerSettingMemoryAlertThreshold();
                
                if (memoryUsage.compareTo(memoryThreshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.MEMORY, memoryUsage, memoryThreshold, "%");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }

            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("检测API上报数据告警失败: serverId={}", serverId, e);
            return ReturnResult.error("检测API上报数据告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> detectRedisMetricsAlerts(Integer serverId) {
        try {
            // 从Redis获取最新指标数据
            String redisKey = "server:metrics:" + serverId;
            Map<String, String> metricsData = timeSeriesService.hGet(redisKey);

            if (metricsData == null || metricsData.isEmpty()) {
                log.debug("Redis中没有找到服务器指标数据: serverId={}", serverId);
                return ReturnResult.success(new ArrayList<>());
            }

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 直接使用服务器设置中的告警配置
            List<MonitorSysGenServerAlertHistory> alerts = new ArrayList<>();

            // 检测各项指标，使用服务器设置中的告警阈值
            BigDecimal cpuThreshold = setting.getMonitorSysGenServerSettingCpuAlertThreshold();
            checkMetricAlert(serverId, metricsData, "cpuUsage", AlertType.CPU, cpuThreshold, "%", alerts);

            BigDecimal memoryThreshold = setting.getMonitorSysGenServerSettingMemoryAlertThreshold();
            checkMetricAlert(serverId, metricsData, "memoryUsage", AlertType.MEMORY, memoryThreshold, "%", alerts);

            BigDecimal diskThreshold = setting.getMonitorSysGenServerSettingDiskAlertThreshold();
            checkMetricAlert(serverId, metricsData, "diskUsage", AlertType.DISK, diskThreshold, "%", alerts);

            // 新增网络告警检测
            BigDecimal networkThreshold = setting.getMonitorSysGenServerSettingNetworkAlertThreshold();
            if (networkThreshold != null) {
                checkNetworkAlert(serverId, metricsData, networkThreshold, alerts);
            }

            return ReturnResult.success(alerts);

        } catch (Exception e) {
            log.error("检测Redis指标数据告警失败: serverId={}", serverId, e);
            return ReturnResult.error("检测Redis指标数据告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> detectAllServersAlerts() {
        try {
            List<MonitorSysGenServerAlertHistory> allAlerts = new ArrayList<>();

            // 获取所有启用的服务器
            List<MonitorSysGenServer> servers = serverService.list(
                new LambdaQueryWrapper<MonitorSysGenServer>()
                    .eq(MonitorSysGenServer::getMonitorSysGenServerStatus, 1)
            );

            for (MonitorSysGenServer server : servers) {
                try {
                    ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult =
                        detectRedisMetricsAlerts(server.getMonitorSysGenServerId());

                    if (alertResult.isOk() && alertResult.getData() != null) {
                        allAlerts.addAll(alertResult.getData());
                    }
                } catch (Exception e) {
                    log.error("检测服务器告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
                }
            }

            log.info("批量检测告警完成，共检测到 {} 个告警", allAlerts.size());
            return ReturnResult.success(allAlerts);

        } catch (Exception e) {
            log.error("批量检测所有服务器告警失败", e);
            return ReturnResult.error("批量检测所有服务器告警失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isDuplicateAlert(String fingerprint, Integer timeWindowMinutes) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);

            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryFingerprint, fingerprint)
                       .ge(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime, startTime)
                       .in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus,
                           AlertStatus.TRIGGERED, AlertStatus.ACTIVE);

            return alertHistoryService.count(queryWrapper) > 0;

        } catch (Exception e) {
            log.error("检查重复告警失败: fingerprint={}", fingerprint, e);
            return false;
        }
    }

    @Override
    public String generateAlertFingerprint(Integer serverId, AlertType alertType, AlertLevel alertLevel) {
        String source = serverId + ":" + alertType.getCode() + ":" + alertLevel.getCode();
        return DigestUtils.md5Hex(source);
    }

    @Override
    public AlertLevel calculateAlertLevel(AlertType alertType, BigDecimal currentValue, BigDecimal thresholdValue) {
        if (currentValue == null || thresholdValue == null) {
            return AlertLevel.WARNING;
        }

        BigDecimal ratio = currentValue.divide(thresholdValue, 2, RoundingMode.HALF_UP);

        if (ratio.compareTo(new BigDecimal("2.0")) >= 0) {
            return AlertLevel.CRITICAL;
        } else if (ratio.compareTo(new BigDecimal("1.5")) >= 0) {
            return AlertLevel.ERROR;
        } else if (ratio.compareTo(new BigDecimal("1.0")) >= 0) {
            return AlertLevel.WARNING;
        } else {
            return AlertLevel.INFO;
        }
    }

    @Override
    public boolean isAlertSilenced(String fingerprint) {
        try {
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryFingerprint, fingerprint)
                       .eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus, AlertStatus.SILENCED)
                       .gt(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistorySilenceUntil, LocalDateTime.now())
                       .orderByDesc(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryTriggerTime)
                       .last("LIMIT 1");

            return alertHistoryService.count(queryWrapper) > 0;

        } catch (Exception e) {
            log.error("检查告警静默状态失败: fingerprint={}", fingerprint, e);
            return false;
        }
    }



    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> recoverAlerts(Integer serverId, AlertType alertType) {
        try {
            // 查找指定服务器和类型的活跃告警
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryServerId, serverId)
                       .eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryType, alertType)
                       .in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus,
                           AlertStatus.TRIGGERED, AlertStatus.ACTIVE);

            List<MonitorSysGenServerAlertHistory> activeAlerts = alertHistoryService.list(queryWrapper);

            if (!activeAlerts.isEmpty()) {
                List<Long> alertIds = activeAlerts.stream()
                    .map(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId)
                    .collect(Collectors.toList());

                ReturnResult<Integer> updateResult = alertHistoryService.batchUpdateAlertStatus(alertIds, AlertStatus.RECOVERED);
                if (updateResult.isOk()) {
                    log.info("批量恢复告警成功: serverId={}, alertType={}, count={}",
                        serverId, alertType, updateResult.getData());
                }
            }

            return ReturnResult.success(activeAlerts);
        } catch (Exception e) {
            log.error("恢复告警失败: serverId={}, alertType={}", serverId, alertType, e);
            return ReturnResult.error("恢复告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> recoverAllServerAlerts(Integer serverId) {
        try {
            // 查找指定服务器的所有活跃告警
            LambdaQueryWrapper<MonitorSysGenServerAlertHistory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryServerId, serverId)
                       .in(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryStatus,
                           AlertStatus.TRIGGERED, AlertStatus.ACTIVE);

            List<MonitorSysGenServerAlertHistory> activeAlerts = alertHistoryService.list(queryWrapper);

            if (!activeAlerts.isEmpty()) {
                List<Long> alertIds = activeAlerts.stream()
                    .map(MonitorSysGenServerAlertHistory::getMonitorSysGenServerAlertHistoryId)
                    .collect(Collectors.toList());

                ReturnResult<Integer> updateResult = alertHistoryService.batchUpdateAlertStatus(alertIds, AlertStatus.RECOVERED);
                if (updateResult.isOk()) {
                    log.info("批量恢复服务器所有告警成功: serverId={}, count={}",
                        serverId, updateResult.getData());
                }
            }

            return ReturnResult.success(activeAlerts);
        } catch (Exception e) {
            log.error("批量恢复服务器告警失败: serverId={}", serverId, e);
            return ReturnResult.error("批量恢复服务器告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerAlertHistory> detectConnectionAlert(
            Integer serverId, Boolean isOnline, Long responseTime) {

        try {
            // 检测连接状态告警
            if (!isOnline) {
                // 服务器离线告警
                MonitorSysGenServerAlertHistory alert = new MonitorSysGenServerAlertHistory();
                alert.setMonitorSysGenServerAlertHistoryServerId(serverId);
                alert.setMonitorSysGenServerAlertHistoryType(AlertType.CONNECTION);
                alert.setMonitorSysGenServerAlertHistoryLevel(AlertLevel.CRITICAL);
                alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
                alert.setMonitorSysGenServerAlertHistoryTitle("服务器连接异常");
                alert.setMonitorSysGenServerAlertHistoryContent("服务器无法连接，可能已离线");
                alert.setMonitorSysGenServerAlertHistoryTriggerTime(LocalDateTime.now());
                alert.setMonitorSysGenServerAlertHistoryFingerprint(
                    generateAlertFingerprint(serverId, AlertType.CONNECTION, AlertLevel.CRITICAL));
                alert.setMonitorSysGenServerAlertHistoryNotificationSent(false);

                // 检查是否重复告警
                if (!isDuplicateAlert(alert.getMonitorSysGenServerAlertHistoryFingerprint(), DEFAULT_ALERT_WINDOW_MINUTES)) {
                    ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
                    if (saveResult.isOk()) {
                        log.warn("检测到服务器连接告警: serverId={}", serverId);
                        return ReturnResult.success(alert);
                    }
                }
            }

            // 检测响应时间告警
            if (responseTime != null && responseTime > 5000) { // 响应时间超过5秒
                AlertLevel level = responseTime > 10000 ? AlertLevel.ERROR : AlertLevel.WARNING;

                MonitorSysGenServerAlertHistory alert = new MonitorSysGenServerAlertHistory();
                alert.setMonitorSysGenServerAlertHistoryServerId(serverId);
                alert.setMonitorSysGenServerAlertHistoryType(AlertType.RESPONSE_TIME);
                alert.setMonitorSysGenServerAlertHistoryLevel(level);
                alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
                alert.setMonitorSysGenServerAlertHistoryTitle("服务器响应时间异常");
                alert.setMonitorSysGenServerAlertHistoryContent(
                    String.format("服务器响应时间为 %dms，超过正常范围", responseTime));
                alert.setMonitorSysGenServerAlertHistoryCurrentValue(new BigDecimal(responseTime));
                alert.setMonitorSysGenServerAlertHistoryThresholdValue(new BigDecimal(5000));
                alert.setMonitorSysGenServerAlertHistoryUnit("ms");
                alert.setMonitorSysGenServerAlertHistoryTriggerTime(LocalDateTime.now());
                alert.setMonitorSysGenServerAlertHistoryFingerprint(
                    generateAlertFingerprint(serverId, AlertType.RESPONSE_TIME, level));
                alert.setMonitorSysGenServerAlertHistoryNotificationSent(false);

                // 检查是否重复告警
                if (!isDuplicateAlert(alert.getMonitorSysGenServerAlertHistoryFingerprint(), DEFAULT_ALERT_WINDOW_MINUTES)) {
                    ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
                    if (saveResult.isOk()) {
                        log.warn("检测到服务器响应时间告警: serverId={}, responseTime={}ms", serverId, responseTime);
                        return ReturnResult.success(alert);
                    }
                }
            }

            return ReturnResult.success(null);
        } catch (Exception e) {
            log.error("检测连接状态告警失败: serverId={}", serverId, e);
            return ReturnResult.error("检测连接状态告警失败: " + e.getMessage());
        }
    }





    @Override
    public ReturnResult<Integer> cleanupExpiredAlerts(Integer expireDays) {
        try {
            return alertHistoryService.cleanupExpiredAlerts(expireDays);
        } catch (Exception e) {
            log.error("清理过期告警记录失败: expireDays={}", expireDays, e);
            return ReturnResult.error("清理过期告警记录失败: " + e.getMessage());
        }
    }

    /**
     * 检查单个指标告警
     */
    private void checkMetricAlert(Integer serverId, Map<String, String> metricsData,
                                 String metricKey, AlertType alertType,
                                 BigDecimal threshold, String unit,
                                 List<MonitorSysGenServerAlertHistory> alerts) {

        String valueStr = metricsData.get(metricKey);
        if (StringUtils.isNotBlank(valueStr) && threshold != null) {
            try {
                BigDecimal currentValue = new BigDecimal(valueStr);
                if (currentValue.compareTo(threshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, alertType, currentValue, threshold, unit);
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("指标值格式错误: serverId={}, metricKey={}, value={}", serverId, metricKey, valueStr);
            }
        }
    }

    /**
     * 检测网络告警
     */
    private void checkNetworkAlert(Integer serverId, Map<String, String> metricsData,
                                  BigDecimal threshold, List<MonitorSysGenServerAlertHistory> alerts) {
        try {
            String networkInStr = metricsData.get("networkIn");
            String networkOutStr = metricsData.get("networkOut");

            if (StringUtils.isNotBlank(networkInStr) || StringUtils.isNotBlank(networkOutStr)) {
                long networkIn = StringUtils.isNotBlank(networkInStr) ? Long.parseLong(networkInStr) : 0L;
                long networkOut = StringUtils.isNotBlank(networkOutStr) ? Long.parseLong(networkOutStr) : 0L;

                // 计算总网络流量并转换为Mbps (假设输入是bytes/s)
                BigDecimal totalNetworkMbps = new BigDecimal((networkIn + networkOut) * 8.0 / 1024 / 1024);

                if (totalNetworkMbps.compareTo(threshold) > 0) {
                    ReturnResult<MonitorSysGenServerAlertHistory> alertResult = detectSingleAlert(
                        serverId, AlertType.NETWORK, totalNetworkMbps, threshold, "Mbps");
                    if (alertResult.isOk() && alertResult.getData() != null) {
                        alerts.add(alertResult.getData());
                    }
                }
            }
        } catch (NumberFormatException e) {
            log.warn("网络指标值格式错误: serverId={}", serverId, e);
        }
    }

    /**
     * 生成告警标题
     */
    @Override
    public String generateAlertTitle(AlertType alertType, AlertLevel alertLevel) {
        return String.format("[%s] %s告警", alertLevel.getDescription(), alertType.getDescription());
    }

    /**
     * 生成告警内容
     */
    @Override
    public String generateAlertContent(AlertType alertType, BigDecimal currentValue,
                                       BigDecimal thresholdValue, String unit) {
        return String.format("%s当前值为 %s%s，超过阈值 %s%s",
            alertType.getDescription(), currentValue, unit, thresholdValue, unit);
    }
}
