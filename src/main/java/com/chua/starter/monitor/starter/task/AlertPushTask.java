package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.service.UnifiedAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 告警推送定时任务
 * 使用统一告警服务推送告警信息，只推送启用告警功能的服务器的告警信息
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertPushTask {

    private final UnifiedAlertService unifiedAlertService;

    /**
     * 推送所有服务器的告警信息
     * 每2分钟执行一次，使用统一告警服务
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void pushAllServerAlerts() {
        try {
            log.debug("开始推送所有服务器告警信息");
            
            // 使用统一告警服务推送所有服务器的告警信息
            // 只推送启用告警功能的服务器的告警信息
            unifiedAlertService.pushAllServerAlerts();
            
            log.debug("所有服务器告警信息推送完成");
            
        } catch (Exception e) {
            log.error("推送所有服务器告警信息异常", e);
        }
    }
}
