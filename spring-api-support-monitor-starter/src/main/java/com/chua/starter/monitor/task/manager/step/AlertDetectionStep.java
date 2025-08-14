package com.chua.starter.monitor.task.manager.step;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertDetectionStep implements TaskStep {

    private final UnifiedAlertService unifiedAlertService;

    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            ReturnResult<Boolean> rr = unifiedAlertService.checkAndPushServerAlerts(server, null, setting);
            if (!rr.isOk()) {
                log.warn("告警检测失败: serverId={}, msg={}", server.getMonitorSysGenServerId(), rr.getMsg());
            }
        } catch (Exception e) {
            log.error("检测推送告警失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}

