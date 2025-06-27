package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.service.ServerMetricsWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据下发定时任务
 * 统一管理所有WebSocket数据推送任务
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPushTask {

    private final ServerMetricsWebSocketService serverMetricsWebSocketService;

    /**
     * 推送实时指标数据
     * 每30秒执行一次
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void pushRealTimeMetrics() {
        try {
            log.debug("开始推送实时指标数据");
            serverMetricsWebSocketService.pushRealTimeMetrics();
        } catch (Exception e) {
            log.error("推送实时指标数据任务失败", e);
        }
    }

    /**
     * 推送服务器状态汇总
     * 每分钟执行一次，延迟30秒启动
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000) // 1分钟，延迟30秒
    public void pushServerStatusSummary() {
        try {
            log.debug("开始推送服务器状态汇总");
            serverMetricsWebSocketService.pushServerStatusSummary();
        } catch (Exception e) {
            log.error("推送服务器状态汇总任务失败", e);
        }
    }

    /**
     * 推送性能趋势数据
     * 每5分钟执行一次，延迟1分钟启动
     */
    @Scheduled(fixedRate = 300000, initialDelay = 60000) // 5分钟，延迟1分钟
    public void pushPerformanceTrends() {
        try {
            log.debug("开始推送性能趋势数据");
            serverMetricsWebSocketService.pushPerformanceTrends();
        } catch (Exception e) {
            log.error("推送性能趋势数据任务失败", e);
        }
    }

    /**
     * 推送告警信息
     * 每2分钟执行一次，延迟90秒启动
     */
    @Scheduled(fixedRate = 120000, initialDelay = 90000) // 2分钟，延迟90秒
    public void pushAlertMessages() {
        try {
            log.debug("开始推送告警信息");
            serverMetricsWebSocketService.pushAlertMessages();
        } catch (Exception e) {
            log.error("推送告警信息任务失败", e);
        }
    }

    /**
     * 推送连接状态统计
     * 每30秒执行一次，延迟10秒启动
     */
    @Scheduled(fixedRate = 30000, initialDelay = 10000) // 30秒，延迟10秒
    public void pushConnectionStatistics() {
        try {
            log.debug("开始推送连接状态统计");
            serverMetricsWebSocketService.pushConnectionStatistics();
        } catch (Exception e) {
            log.error("推送连接状态统计任务失败", e);
        }
    }

    /**
     * 推送健康状态报告
     * 每5分钟执行一次，延迟2分钟启动
     */
    @Scheduled(fixedRate = 300000, initialDelay = 120000) // 5分钟，延迟2分钟
    public void pushHealthStatus() {
        try {
            log.debug("开始推送健康状态报告");
            serverMetricsWebSocketService.pushHealthStatus();
        } catch (Exception e) {
            log.error("推送健康状态报告任务失败", e);
        }
    }

    /**
     * 推送每日健康报告
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void pushDailyHealthReport() {
        try {
            log.info("开始推送每日健康报告");
            serverMetricsWebSocketService.pushDailyHealthReport();
        } catch (Exception e) {
            log.error("推送每日健康报告任务失败", e);
        }
    }
}
