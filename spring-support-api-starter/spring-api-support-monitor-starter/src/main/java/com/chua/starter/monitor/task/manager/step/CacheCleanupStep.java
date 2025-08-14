package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleanupStep implements TaskStep {

    private final MonitorSysGenServerService serverService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            serverService.cleanupTempFiles();
            serverService.cleanupCache();
        } catch (Exception e) {
            log.error("缓存清理失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}

