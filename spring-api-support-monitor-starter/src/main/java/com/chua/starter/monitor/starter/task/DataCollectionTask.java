package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.report.ReportManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据收集定时任务
 * 负责启动和管理各服务器的数据收集任务
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionTask implements CommandLineRunner {

    private final MonitorSysGenServerService serverService;
    private final ReportManager reportManager;

    /**
     * 应用启动时初始化数据收集任务
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("初始化数据收集任务...");
        initializeDataCollection();
    }

    /**
     * 每5分钟检查一次服务器配置变更，重新初始化数据收集任务
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkAndUpdateDataCollection() {
        log.debug("检查数据收集任务配置变更...");
        initializeDataCollection();
    }

    /**
     * 初始化数据收集任务
     */
    private void initializeDataCollection() {
        try {
            // 获取所有启用监控的服务器
            List<MonitorSysGenServer> servers = serverService.list();
            
            for (MonitorSysGenServer server : servers) {
                // 只为启用监控且需要上报的服务器启动数据收集
                if (isMonitoringEnabled(server) && isReportingEnabled(server)) {
                    reportManager.startDataCollection(server);
                } else {
                    // 停止不需要监控的服务器的数据收集
                    String reportType = server.getMonitorSysGenServerDataReportMethod();
                    if (reportType != null && !"NONE".equals(reportType)) {
                        reportManager.stopDataCollection(server.getMonitorSysGenServerId(), reportType);
                    }
                }
            }
            
            log.debug("数据收集任务初始化完成");
        } catch (Exception e) {
            log.error("初始化数据收集任务失败", e);
        }
    }

    /**
     * 检查服务器是否启用监控
     */
    private boolean isMonitoringEnabled(MonitorSysGenServer server) {
        return server.getMonitorSysGenServerStatus() != null && 
               server.getMonitorSysGenServerStatus() == 1 &&
               server.getMonitorSysGenServerMonitorEnabled() != null && 
               server.getMonitorSysGenServerMonitorEnabled() == 1;
    }

    /**
     * 检查服务器是否启用数据上报
     */
    private boolean isReportingEnabled(MonitorSysGenServer server) {
        return server.getMonitorSysGenServerReportEnabled() != null && 
               server.getMonitorSysGenServerReportEnabled() == 1 &&
               server.getMonitorSysGenServerDataReportMethod() != null &&
               !"NONE".equals(server.getMonitorSysGenServerDataReportMethod());
    }
}
