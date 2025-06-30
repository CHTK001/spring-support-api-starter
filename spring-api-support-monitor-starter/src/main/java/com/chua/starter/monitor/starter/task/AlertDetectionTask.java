package com.chua.starter.monitor.starter.task;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.service.AlertDetectionService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerAlertHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 告警检测定时任务
 * 定期检查Redis中存储的指标数据并触发相应告警
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertDetectionTask {

    private final AlertDetectionService alertDetectionService;
    private final MonitorSysGenServerAlertHistoryService alertHistoryService;

    /**
     * 批量检测所有服务器的告警
     * 每2分钟执行一次
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void detectAllServersAlerts() {
        try {
            log.debug("开始批量检测所有服务器告警");
            
            ReturnResult<List<MonitorSysGenServerAlertHistory>> result = 
                alertDetectionService.detectAllServersAlerts();
            
            if (result.isOk() && result.getData() != null) {
                int alertCount = result.getData().size();
                if (alertCount > 0) {
                    log.info("批量告警检测完成，检测到 {} 个新告警", alertCount);
                    
                    // 记录告警详情
                    for (MonitorSysGenServerAlertHistory alert : result.getData()) {
                        log.warn("定时任务检测到告警: serverId={}, type={}, level={}, currentValue={}, threshold={}", 
                            alert.getMonitorSysGenServerAlertHistoryServerId(),
                            alert.getMonitorSysGenServerAlertHistoryType(), 
                            alert.getMonitorSysGenServerAlertHistoryLevel(),
                            alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                            alert.getMonitorSysGenServerAlertHistoryThresholdValue());
                    }
                } else {
                    log.debug("批量告警检测完成，未检测到新告警");
                }
            } else {
                log.warn("批量告警检测失败: {}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("批量告警检测任务失败", e);
        }
    }

    /**
     * 自动恢复过期的活跃告警
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void autoRecoverExpiredAlerts() {
        try {
            log.debug("开始自动恢复过期的活跃告警");
            
            // 恢复超过30分钟的活跃告警
            ReturnResult<Integer> result = alertHistoryService.autoRecoverExpiredAlerts(30);
            
            if (result.isOk()) {
                int recoveredCount = result.getData();
                if (recoveredCount > 0) {
                    log.info("自动恢复过期告警完成，恢复了 {} 个告警", recoveredCount);
                } else {
                    log.debug("自动恢复过期告警完成，没有需要恢复的告警");
                }
            } else {
                log.warn("自动恢复过期告警失败: {}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("自动恢复过期告警任务失败", e);
        }
    }

    /**
     * 清理过期的告警记录
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredAlerts() {
        try {
            log.info("开始清理过期的告警记录");
            
            // 清理超过30天的告警记录
            ReturnResult<Integer> result = alertDetectionService.cleanupExpiredAlerts(30);
            
            if (result.isOk()) {
                int cleanedCount = result.getData();
                log.info("清理过期告警记录完成，清理了 {} 条记录", cleanedCount);
            } else {
                log.warn("清理过期告警记录失败: {}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("清理过期告警记录任务失败", e);
        }
    }

    /**
     * 处理未发送通知的告警
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 1分钟
    public void processUnsentNotificationAlerts() {
        try {
            log.debug("开始处理未发送通知的告警");
            
            // 获取未发送通知的告警，限制100条
            ReturnResult<List<MonitorSysGenServerAlertHistory>> result = 
                alertHistoryService.getUnsentNotificationAlerts(100);
            
            if (result.isOk() && result.getData() != null) {
                List<MonitorSysGenServerAlertHistory> unsentAlerts = result.getData();
                if (!unsentAlerts.isEmpty()) {
                    log.info("发现 {} 个未发送通知的告警", unsentAlerts.size());
                    
                    // 这里可以集成各种通知方式：邮件、短信、钉钉、微信等
                    for (MonitorSysGenServerAlertHistory alert : unsentAlerts) {
                        try {
                            // 模拟发送通知
                            boolean notificationSent = sendAlertNotification(alert);
                            
                            if (notificationSent) {
                                // 更新通知状态
                                alertHistoryService.updateNotificationStatus(
                                        Converter.createInteger(alert.getMonitorSysGenServerAlertHistoryId()),
                                    true, 
                                    "SYSTEM", 
                                    "system@monitor.com");
                                
                                log.debug("告警通知发送成功: alertId={}, type={}", 
                                    alert.getMonitorSysGenServerAlertHistoryId(),
                                    alert.getMonitorSysGenServerAlertHistoryType());
                            }
                        } catch (Exception e) {
                            log.error("发送告警通知失败: alertId={}", 
                                alert.getMonitorSysGenServerAlertHistoryId(), e);
                        }
                    }
                } else {
                    log.debug("没有未发送通知的告警");
                }
            } else {
                log.warn("获取未发送通知的告警失败: {}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("处理未发送通知的告警任务失败", e);
        }
    }

    /**
     * 告警统计和报告
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void generateAlertStatistics() {
        try {
            log.debug("开始生成告警统计报告");
            
            // 这里可以生成告警统计数据，用于监控大屏或报表
            // 例如：每小时的告警数量、告警类型分布、告警级别分布等
            
            log.debug("告警统计报告生成完成");
            
        } catch (Exception e) {
            log.error("生成告警统计报告任务失败", e);
        }
    }

    /**
     * 发送告警通知
     * 这里是一个简化的实现，实际应该集成各种通知渠道
     *
     * @param alert 告警记录
     * @return 是否发送成功
     */
    private boolean sendAlertNotification(MonitorSysGenServerAlertHistory alert) {
        try {
            // 这里可以根据告警级别和类型选择不同的通知方式
            // 例如：CRITICAL级别的告警发送短信和邮件，WARNING级别的只发送邮件
            
            String notificationContent = String.format(
                "告警通知：%s\n" +
                "服务器ID：%d\n" +
                "告警类型：%s\n" +
                "告警级别：%s\n" +
                "当前值：%s%s\n" +
                "阈值：%s%s\n" +
                "触发时间：%s",
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryThresholdValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryTriggerTime()
            );
            
            log.info("模拟发送告警通知: {}", notificationContent);
            
            // 模拟发送成功
            return true;
            
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
            return false;
        }
    }
}
