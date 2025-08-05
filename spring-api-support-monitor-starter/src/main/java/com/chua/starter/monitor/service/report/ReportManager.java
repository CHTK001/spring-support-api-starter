package com.chua.starter.monitor.service.report;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.report.ReportHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 上报管理器
 * 统一管理不同的上报处理器，根据服务器配置选择合适的处理器
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportManager {

    private final List<com.chua.starter.monitor.service.report.ReportHandler> reportHandlers;
    private final MonitorSysGenServerSettingService serverSettingService;

    /**
     * 根据服务器配置获取合适的上报处理器
     * 
     * @param server 服务器配置
     * @return 上报处理器
     */
    public Optional<com.chua.starter.monitor.service.report.ReportHandler> getReportHandler(MonitorSysGenServer server) {
        if (server == null) {
            log.warn("服务器配置为空");
            return Optional.empty();
        }

        // 从配置表获取上报方式
        String reportMethod = getServerReportMethod(server);
        if (reportMethod == null) {
            log.warn("服务器上报方式为空");
            return Optional.empty();
        }

        return reportHandlers.stream()
                .filter(handler -> handler.getReportType().equals(reportMethod))
                .filter(handler -> handler.supports(server))
                .min((h1, h2) -> Integer.compare(h1.getPriority(), h2.getPriority()));
    }

    /**
     * 根据上报类型获取处理器
     *
     * @param reportType 上报类型
     * @return 上报处理器
     */
    public Optional<com.chua.starter.monitor.service.report.ReportHandler> getReportHandlerByType(String reportType) {
        return reportHandlers.stream()
                .filter(handler -> handler.getReportType().equals(reportType))
                .findFirst();
    }



    /**
     * 处理数据上报
     * 
     * @param server 服务器配置
     * @param reportData 上报数据
     * @return 处理结果
     */
    public ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            Optional<com.chua.starter.monitor.service.report.ReportHandler> handlerOpt = getReportHandler(server);
            if (handlerOpt.isEmpty()) {
                String reportMethod = getServerReportMethod(server);
                log.warn("未找到支持的上报处理器: serverId={}, reportMethod={}",
                        server.getMonitorSysGenServerId(), reportMethod);
                return ReturnResult.error("不支持的上报方式: " + reportMethod);
            }

            com.chua.starter.monitor.service.report.ReportHandler handler = handlerOpt.get();
            log.debug("使用上报处理器: {} 处理服务器: {}", 
                    handler.getClass().getSimpleName(), server.getMonitorSysGenServerId());
            
            return handler.handleReport(server, reportData);
            
        } catch (Exception e) {
            log.error("处理数据上报失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("数据上报处理失败: " + e.getMessage());
        }
    }

    /**
     * 启动数据收集任务
     * 
     * @param server 服务器配置
     * @return 启动结果
     */
    public ReturnResult<Boolean> startDataCollection(MonitorSysGenServer server) {
        try {
            Optional<com.chua.starter.monitor.service.report.ReportHandler> handlerOpt = getReportHandler(server);
            if (handlerOpt.isEmpty()) {
                String reportMethod = getServerReportMethod(server);
                log.warn("未找到支持的上报处理器: serverId={}, reportMethod={}",
                        server.getMonitorSysGenServerId(), reportMethod);
                return ReturnResult.error("不支持的上报方式: " + reportMethod);
            }

            com.chua.starter.monitor.service.report.ReportHandler handler = handlerOpt.get();
            log.info("启动数据收集任务: serverId={}, handler={}", 
                    server.getMonitorSysGenServerId(), handler.getClass().getSimpleName());
            
            return handler.startDataCollection(server);
            
        } catch (Exception e) {
            log.error("启动数据收集任务失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("启动数据收集失败: " + e.getMessage());
        }
    }

    /**
     * 停止数据收集任务
     * 
     * @param serverId 服务器ID
     * @param reportType 上报类型
     * @return 停止结果
     */
    public ReturnResult<Boolean> stopDataCollection(Integer serverId, String reportType) {
        try {
            Optional<com.chua.starter.monitor.service.report.ReportHandler> handlerOpt = getReportHandlerByType(reportType);
            if (handlerOpt.isEmpty()) {
                log.warn("未找到指定类型的上报处理器: serverId={}, reportType={}", serverId, reportType);
                return ReturnResult.error("不支持的上报方式: " + reportType);
            }

            com.chua.starter.monitor.service.report.ReportHandler handler = handlerOpt.get();
            log.info("停止数据收集任务: serverId={}, handler={}", 
                    serverId, handler.getClass().getSimpleName());
            
            return handler.stopDataCollection(serverId);
            
        } catch (Exception e) {
            log.error("停止数据收集任务失败: serverId={}", serverId, e);
            return ReturnResult.error("停止数据收集失败: " + e.getMessage());
        }
    }

    /**
     * 停止所有数据收集任务
     * 
     * @param serverId 服务器ID
     * @return 停止结果
     */
    public ReturnResult<Boolean> stopAllDataCollection(Integer serverId) {
        try {
            boolean allSuccess = true;
            StringBuilder errorMessages = new StringBuilder();

            for (com.chua.starter.monitor.service.report.ReportHandler handler : reportHandlers) {
                try {
                    ReturnResult<Boolean> result = handler.stopDataCollection(serverId);
                    if (!result.isOk()) {
                        allSuccess = false;
                        errorMessages.append(handler.getReportType()).append(": ").append(result.getMsg()).append("; ");
                    }
                } catch (Exception e) {
                    allSuccess = false;
                    errorMessages.append(handler.getReportType()).append(": ").append(e.getMessage()).append("; ");
                    log.error("停止数据收集任务失败: serverId={}, handler={}", 
                            serverId, handler.getClass().getSimpleName(), e);
                }
            }

            if (allSuccess) {
                log.info("所有数据收集任务已停止: serverId={}", serverId);
                return ReturnResult.success(true);
            } else {
                log.warn("部分数据收集任务停止失败: serverId={}, errors={}", serverId, errorMessages);
                return ReturnResult.error("部分任务停止失败: " + errorMessages);
            }
            
        } catch (Exception e) {
            log.error("停止所有数据收集任务失败: serverId={}", serverId, e);
            return ReturnResult.error("停止所有数据收集失败: " + e.getMessage());
        }
    }

    /**
     * 验证服务器配置
     * 
     * @param server 服务器配置
     * @return 验证结果
     */
    public ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server) {
        try {
            Optional<com.chua.starter.monitor.service.report.ReportHandler> handlerOpt = getReportHandler(server);
            if (handlerOpt.isEmpty()) {
                String reportMethod = getServerReportMethod(server);
                return ReturnResult.error("不支持的上报方式: " + reportMethod);
            }

            com.chua.starter.monitor.service.report.ReportHandler handler = handlerOpt.get();
            return handler.validateServerConfig(server);
            
        } catch (Exception e) {
            log.error("验证服务器配置失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有支持的上报方式
     * 
     * @return 上报方式列表
     */
    public List<Map<String, Object>> getSupportedReportMethods() {
        return reportHandlers.stream()
                .map(handler -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", handler.getReportType());
                    map.put("description", handler.getDescription());
                    map.put("priority", handler.getPriority());
                    map.put("requiresScheduledTask", handler.requiresScheduledTask());
                    map.put("defaultInterval", handler.getDefaultMonitorInterval());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取服务器支持的上报方式
     * 
     * @param server 服务器配置
     * @return 支持的上报方式列表
     */
    public List<String> getServerSupportedReportMethods(MonitorSysGenServer server) {
        return reportHandlers.stream()
                .filter(handler -> {
                    try {
                        // 直接测试处理器是否支持该服务器
                        return handler.supports(server);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(ReportHandler::getReportType)
                .collect(Collectors.toList());
    }

    /**
     * 重新初始化服务器数据收集
     * 
     * @param server 服务器配置
     * @return 初始化结果
     */
    public ReturnResult<Boolean> reinitializeDataCollection(MonitorSysGenServer server) {
        try {
            // 先停止所有数据收集任务
            stopAllDataCollection(server.getMonitorSysGenServerId());
            
            // 等待一段时间确保任务完全停止
            Thread.sleep(1000);
            
            // 重新启动数据收集任务
            return startDataCollection(server);
            
        } catch (Exception e) {
            log.error("重新初始化数据收集失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("重新初始化失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器的数据上报方式
     */
    private String getServerReportMethod(MonitorSysGenServer server) {
        try {
            // 从配置表获取上报方式
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting setting = settingResult.getData();
                String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
                if (reportMethod != null && !reportMethod.trim().isEmpty()) {
                    return reportMethod;
                }
            }
        } catch (Exception e) {
            log.warn("获取服务器上报配置失败: serverId={}, error={}",
                server.getMonitorSysGenServerId(), e.getMessage());
        }

        // 默认返回NONE
        return "NONE";
    }
}
