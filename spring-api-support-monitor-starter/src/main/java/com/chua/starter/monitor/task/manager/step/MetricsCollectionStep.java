package com.chua.starter.monitor.task.manager.step;

import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.ServerMetricsWebSocketService;
import com.chua.starter.monitor.task.manager.TaskStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 指标收集步骤
 * <p>
 * 负责收集服务器的性能指标数据
 *
 * @author CH
 * @since 2025/09/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCollectionStep implements TaskStep {

    private final ServerMetricsWebSocketService serverMetricsWebSocketService;

    /**
     * 执行指标收集任务
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     */
    @Override
    public void run(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        try {
            serverMetricsWebSocketService.pushSingleServerMetricsByConfig(server, setting);
        } catch (Exception e) {
            log.error("收集推送指标数据失败: serverId={}", server.getMonitorSysGenServerId(), e);
        }
    }
}