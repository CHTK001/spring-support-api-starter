package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupStep implements TaskStep {

    private final MonitorSysGenServerMetricsService metricsService;
    private final com.chua.starter.monitor.service.DelayedUpdateService delayedUpdateService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            Integer retentionDays = setting.getMonitorSysGenServerSettingMetricsRetentionDays();
            if (retentionDays == null || retentionDays <= 0) {
                retentionDays = 30;
            }
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            // 改为延迟清理：按 serverId 合并，最终采用全局最小的过期时间执行一次清理
            delayedUpdateService.submitMetricsCleanup(server.getMonitorSysGenServerId(), expireTime);
        } catch (Exception e) {
            log.error("日志清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}

