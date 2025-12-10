package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.server.ServerMetricsWebSocketService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据推送步骤
 * <p>
 * 负责将服务器指标数据推送到WebSocket客户端
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPushStep implements TaskStep {

    private final ServerMetricsWebSocketService serverMetricsWebSocketService;

    /**
     * 执行数据推送任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            serverMetricsWebSocketService.pushSingleServerMetricsByConfig(server, setting);
        } catch (Exception e) {
            log.error("数据推送失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}