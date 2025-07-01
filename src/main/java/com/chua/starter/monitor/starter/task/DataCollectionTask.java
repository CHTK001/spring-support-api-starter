package com.chua.starter.monitor.starter.task;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerDataReportService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据采集定时任务
 * 负责定时采集服务器指标数据
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionTask {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final MonitorSysGenServerDataReportService dataReportService;

    /**
     * 数据采集任务
     * 每30秒执行一次，采集启用监控的服务器指标数据
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void collectServerMetrics() {
        try {
            log.debug("开始执行数据采集任务");

            // 获取所有启用的服务器
            List<MonitorSysGenServer> servers = serverService.list();
            if (servers == null || servers.isEmpty()) {
                log.debug("没有找到服务器配置，跳过数据采集");
                return;
            }

            int totalServers = servers.size();
            int collectedCount = 0;
            int skippedCount = 0;
            int errorCount = 0;

            for (MonitorSysGenServer server : servers) {
                try {
                    // 检查服务器是否启用监控
                    if (!isMonitoringEnabled(server)) {
                        skippedCount++;
                        log.debug("服务器 {} 未启用监控，跳过数据采集", server.getMonitorSysGenServerName());
                        continue;
                    }

                    // 根据服务器配置的上报方式进行数据采集
                    boolean success = collectServerData(server);
                    if (success) {
                        collectedCount++;
                        log.debug("服务器 {} 数据采集成功", server.getMonitorSysGenServerName());
                    } else {
                        errorCount++;
                        log.warn("服务器 {} 数据采集失败", server.getMonitorSysGenServerName());
                    }

                } catch (Exception e) {
                    errorCount++;
                    log.error("服务器 {} 数据采集异常: {}", 
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }

            log.debug("数据采集任务完成 - 总数: {}, 成功: {}, 跳过: {}, 失败: {}", 
                    totalServers, collectedCount, skippedCount, errorCount);

        } catch (Exception e) {
            log.error("数据采集任务执行失败", e);
        }
    }

    /**
     * 检查服务器是否启用监控
     */
    private boolean isMonitoringEnabled(MonitorSysGenServer server) {
        try {
            // 检查服务器状态
            if (server.getMonitorSysGenServerStatus() == null || server.getMonitorSysGenServerStatus() != 1) {
                return false;
            }

            // 获取服务器设置
            ReturnResult<MonitorSysGenServerSetting> settingResult = 
                    serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            
            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器 {} 配置失败，跳过数据采集", server.getMonitorSysGenServerName());
                return false;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            
            // 检查是否启用监控
            if (setting.getMonitorSysGenServerSettingMonitorEnabled() == null ||
                setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
                return false;
            }

            // 检查是否启用数据上报
            if (setting.getMonitorSysGenServerSettingReportEnabled() == null ||
                setting.getMonitorSysGenServerSettingReportEnabled() != 1) {
                return false;
            }

            // 检查数据上报方式
            String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
            if (reportMethod == null || "NONE".equals(reportMethod)) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("检查服务器 {} 监控状态失败: {}", 
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 采集服务器数据
     */
    private boolean collectServerData(MonitorSysGenServer server) {
        try {
            // 获取服务器设置以确定上报方式
            ReturnResult<MonitorSysGenServerSetting> settingResult = 
                    serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return false;
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();

            // 根据上报方式调用相应的处理方法
            switch (reportMethod.toUpperCase()) {
                case "LOCAL":
                    return handleLocalCollection(server);
                case "API":
                    // API上报由客户端主动推送，这里不需要主动采集
                    log.debug("服务器 {} 使用API上报方式，跳过主动采集", server.getMonitorSysGenServerName());
                    return true;
                case "PROMETHEUS":
                    return handlePrometheusCollection(server);
                default:
                    log.warn("服务器 {} 使用未知的上报方式: {}", server.getMonitorSysGenServerName(), reportMethod);
                    return false;
            }

        } catch (Exception e) {
            log.error("采集服务器 {} 数据失败: {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理本地数据采集
     */
    private boolean handleLocalCollection(MonitorSysGenServer server) {
        try {
            ReturnResult<?> result = dataReportService.handleLocalReport(server);
            if (result.isOk()) {
                log.debug("本地服务器 {} 数据采集成功", server.getMonitorSysGenServerName());
                return true;
            } else {
                log.warn("本地服务器 {} 数据采集失败: {}", server.getMonitorSysGenServerName(), result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("本地服务器 {} 数据采集异常: {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理Prometheus数据采集
     */
    private boolean handlePrometheusCollection(MonitorSysGenServer server) {
        try {
            ReturnResult<?> result = dataReportService.handlePrometheusReport(server);
            if (result.isOk()) {
                log.debug("Prometheus服务器 {} 数据采集成功", server.getMonitorSysGenServerName());
                return true;
            } else {
                log.warn("Prometheus服务器 {} 数据采集失败: {}", server.getMonitorSysGenServerName(), result.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("Prometheus服务器 {} 数据采集异常: {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
            return false;
        }
    }
}
