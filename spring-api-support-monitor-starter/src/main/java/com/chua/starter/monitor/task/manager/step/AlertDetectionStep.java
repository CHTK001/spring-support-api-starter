package com.chua.starter.monitor.task.manager.step;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.UnifiedAlertService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 告警检测步骤
 * <p>
 * 负责检测服务器的告警条件并推送告警信息
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertDetectionStep implements TaskStep {

    private final UnifiedAlertService unifiedAlertService;

    /**
     * 执行告警检测任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
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