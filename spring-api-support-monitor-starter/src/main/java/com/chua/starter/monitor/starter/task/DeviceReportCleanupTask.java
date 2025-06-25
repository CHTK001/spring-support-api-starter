package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.service.MonitorSysGenServerDeviceReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备数据上报清理任务
 * 
 * @author CH
 * @since 2024/12/19
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceReportCleanupTask {

    private final MonitorSysGenServerDeviceReportService deviceReportService;

    /**
     * 清理设备统计缓存
     * 每小时执行一次，清理长时间未活跃的设备统计信息
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupDeviceStats() {
        try {
            log.debug("开始清理设备统计缓存...");
            
            // 获取设备统计信息
            var statsResult = deviceReportService.getDeviceReportStatistics();
            if (statsResult.isOk() && statsResult.getData() != null) {
                @SuppressWarnings("unchecked")
                var devices = (java.util.Map<String, Object>) statsResult.getData().get("devices");
                
                if (devices != null) {
                    long currentTime = System.currentTimeMillis();
                    long expireTime = 24 * 60 * 60 * 1000; // 24小时
                    
                    int cleanedCount = 0;
                    for (var entry : devices.entrySet()) {
                        @SuppressWarnings("unchecked")
                        var deviceStats = (java.util.Map<String, Object>) entry.getValue();
                        Long lastUpdateTime = (Long) deviceStats.get("lastUpdateTime");
                        
                        if (lastUpdateTime != null && (currentTime - lastUpdateTime) > expireTime) {
                            // 这里可以添加清理逻辑，目前只记录日志
                            log.info("发现长时间未活跃的设备: deviceId={}, lastUpdate={}", 
                                    entry.getKey(), new java.util.Date(lastUpdateTime));
                            cleanedCount++;
                        }
                    }
                    
                    if (cleanedCount > 0) {
                        log.info("设备统计缓存清理完成，发现 {} 个长时间未活跃的设备", cleanedCount);
                    } else {
                        log.debug("设备统计缓存清理完成，无需清理的设备");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("清理设备统计缓存失败", e);
        }
    }

    /**
     * 生成设备上报统计报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyDeviceReport() {
        try {
            log.info("开始生成每日设备上报统计报告...");
            
            // 获取设备列表
            var deviceListResult = deviceReportService.getActiveDevices(24);
            if (deviceListResult.isOk() && deviceListResult.getData() != null) {
                @SuppressWarnings("unchecked")
                var devices = (java.util.List<java.util.Map<String, Object>>) deviceListResult.getData().get("devices");
                Integer total = (Integer) deviceListResult.getData().get("total");
                
                if (devices != null && total != null) {
                    log.info("=== 每日设备上报统计报告 ===");
                    log.info("总设备数: {}", total);
                    
                    long totalMetrics = 0;
                    long totalInfo = 0;
                    long totalMatched = 0;
                    long totalUnmatched = 0;
                    int activeDevices = 0;
                    
                    long currentTime = System.currentTimeMillis();
                    long activeThreshold = 24 * 60 * 60 * 1000; // 24小时内有活动的设备
                    
                    for (var device : devices) {
                        String deviceId = (String) device.get("deviceId");
                        Long metricsCount = (Long) device.get("metricsCount");
                        Long infoCount = (Long) device.get("infoCount");
                        Long matchedCount = (Long) device.get("matchedCount");
                        Long unmatchedCount = (Long) device.get("unmatchedCount");
                        Long lastUpdateTime = (Long) device.get("lastUpdateTime");
                        
                        if (metricsCount != null) totalMetrics += metricsCount;
                        if (infoCount != null) totalInfo += infoCount;
                        if (matchedCount != null) totalMatched += matchedCount;
                        if (unmatchedCount != null) totalUnmatched += unmatchedCount;
                        
                        if (lastUpdateTime != null && (currentTime - lastUpdateTime) < activeThreshold) {
                            activeDevices++;
                        }
                        
                        log.info("设备 {}: 指标上报={}, 信息上报={}, 匹配={}, 未匹配={}", 
                                deviceId, metricsCount, infoCount, matchedCount, unmatchedCount);
                    }
                    
                    log.info("活跃设备数: {} / {}", activeDevices, total);
                    log.info("总指标上报次数: {}", totalMetrics);
                    log.info("总信息上报次数: {}", totalInfo);
                    log.info("总匹配次数: {}", totalMatched);
                    log.info("总未匹配次数: {}", totalUnmatched);
                    
                    if (totalMatched + totalUnmatched > 0) {
                        double matchRate = (double) totalMatched / (totalMatched + totalUnmatched) * 100;
                        log.info("匹配成功率: {:.2f}%", matchRate);
                    }
                    
                    log.info("=== 报告结束 ===");
                }
            }
            
        } catch (Exception e) {
            log.error("生成每日设备上报统计报告失败", e);
        }
    }

    /**
     * 检查设备上报健康状态
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void checkDeviceReportHealth() {
        try {
            log.debug("开始检查设备上报健康状态...");
            
            // 获取设备统计信息
            var statsResult = deviceReportService.getDeviceReportStatistics();
            if (statsResult.isOk() && statsResult.getData() != null) {
                @SuppressWarnings("unchecked")
                var devices = (java.util.Map<String, Object>) statsResult.getData().get("devices");
                
                if (devices != null) {
                    long currentTime = System.currentTimeMillis();
                    long warningThreshold = 60 * 60 * 1000; // 1小时未上报则警告
                    long errorThreshold = 6 * 60 * 60 * 1000; // 6小时未上报则错误
                    
                    int warningCount = 0;
                    int errorCount = 0;
                    
                    for (var entry : devices.entrySet()) {
                        @SuppressWarnings("unchecked")
                        var deviceStats = (java.util.Map<String, Object>) entry.getValue();
                        Long lastUpdateTime = (Long) deviceStats.get("lastUpdateTime");
                        
                        if (lastUpdateTime != null) {
                            long timeSinceLastUpdate = currentTime - lastUpdateTime;
                            
                            if (timeSinceLastUpdate > errorThreshold) {
                                log.error("设备长时间未上报数据: deviceId={}, 最后上报时间={}, 已超过{}小时", 
                                        entry.getKey(), new java.util.Date(lastUpdateTime), 
                                        timeSinceLastUpdate / (60 * 60 * 1000));
                                errorCount++;
                            } else if (timeSinceLastUpdate > warningThreshold) {
                                log.warn("设备上报数据延迟: deviceId={}, 最后上报时间={}, 已超过{}分钟", 
                                        entry.getKey(), new java.util.Date(lastUpdateTime), 
                                        timeSinceLastUpdate / (60 * 1000));
                                warningCount++;
                            }
                        }
                    }
                    
                    if (errorCount > 0 || warningCount > 0) {
                        log.info("设备上报健康检查完成: 错误设备={}, 警告设备={}, 总设备={}", 
                                errorCount, warningCount, devices.size());
                    } else {
                        log.debug("设备上报健康检查完成: 所有设备状态正常");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("检查设备上报健康状态失败", e);
        }
    }
}
