package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerDataReportService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.service.AlertDetectionService;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * 数据上报控制器
 * 处理客户端的数据上报请求
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@RestController
@RequestMapping("/api/data-report")
@Api(tags = "数据上报管理")
@RequiredArgsConstructor
public class DataReportController {

    private final MonitorSysGenServerDataReportService dataReportService;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final AlertDetectionService alertDetectionService;

    /**
     * 客户端数据上报接口
     * 
     * @param serverId 服务器ID
     * @param reportData 上报的数据
     * @return 处理结果
     */
    @PostMapping("/submit/{serverId}")
    @ApiOperation("客户端数据上报")
    public ReturnResult<MonitorSysGenServerMetrics> submitData(
            @ApiParam("服务器ID") @PathVariable Integer serverId,
            @ApiParam("上报数据") @RequestBody Map<String, Object> reportData) {
        
        try {
            log.debug("收到数据上报请求: serverId={}", serverId);
            
            // 查找服务器配置
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }
            
            // 检查服务器是否启用API上报
            try {
                ReturnResult<MonitorSysGenServerSetting> settingResult =
                    serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                if (!settingResult.isOk() || settingResult.getData() == null) {
                    return ReturnResult.error("获取服务器配置失败");
                }

                MonitorSysGenServerSetting setting = settingResult.getData();
                if (!"API".equals(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                    return ReturnResult.error("服务器未启用API上报模式");
                }
            } catch (Exception e) {
                log.error("获取服务器配置失败: serverId={}", serverId, e);
                return ReturnResult.error("获取服务器配置失败");
            }
            
            // 处理API上报数据
            ReturnResult<MonitorSysGenServerMetrics> result = dataReportService.handleApiReport(server, reportData);

            // 如果数据处理成功，进行告警检测
            if (result.isOk() && result.getData() != null) {
                try {
                    // 检测API上报数据的告警
                    ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult =
                        alertDetectionService.detectApiReportAlerts(serverId, reportData);

                    if (alertResult.isOk() && alertResult.getData() != null && !alertResult.getData().isEmpty()) {
                        log.info("API上报数据检测到 {} 个告警: serverId={}",
                            alertResult.getData().size(), serverId);

                        // 这里可以添加告警通知逻辑
                        for (MonitorSysGenServerAlertHistory alert : alertResult.getData()) {
                            log.warn("触发告警: serverId={}, type={}, level={}, currentValue={}, threshold={}",
                                serverId, alert.getMonitorSysGenServerAlertHistoryType(),
                                alert.getMonitorSysGenServerAlertHistoryLevel(),
                                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                                alert.getMonitorSysGenServerAlertHistoryThresholdValue());
                        }
                    }
                } catch (Exception e) {
                    // 告警检测失败不影响数据上报的成功
                    log.error("API上报数据告警检测失败: serverId={}", serverId, e);
                }
            }

            return result;
            
        } catch (Exception e) {
            log.error("处理数据上报请求失败: serverId={}", serverId, e);
            return ReturnResult.error("数据上报处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器数据收集状态
     * 
     * @param serverId 服务器ID
     * @return 收集状态
     */
    @GetMapping("/status/{serverId}")
    @ApiOperation("获取数据收集状态")
    public ReturnResult<Map<String, Object>> getCollectionStatus(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {
        
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }
            
            return serverService.getMonitoringStatus(serverId.toString());
            
        } catch (Exception e) {
            log.error("获取数据收集状态失败: serverId={}", serverId, e);
            return ReturnResult.error("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动数据收集
     * 
     * @param serverId 服务器ID
     * @return 启动结果
     */
    @PostMapping("/start/{serverId}")
    @ApiOperation("启动数据收集")
    public ReturnResult<String> startCollection(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {

        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            return dataReportService.startDataCollection(server);
            
        } catch (Exception e) {
            log.error("启动数据收集失败: serverId={}", serverId, e);
            return ReturnResult.error("启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止数据收集
     * 
     * @param serverId 服务器ID
     * @return 停止结果
     */
    @PostMapping("/stop/{serverId}")
    @ApiOperation("停止数据收集")
    public ReturnResult<String> stopCollection(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {

        try {
            return dataReportService.stopDataCollection(serverId);
            
        } catch (Exception e) {
            log.error("停止数据收集失败: serverId={}", serverId, e);
            return ReturnResult.error("停止失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发本地数据收集
     * 
     * @param serverId 服务器ID
     * @return 收集结果
     */
    @PostMapping("/collect-local/{serverId}")
    @ApiOperation("手动触发本地数据收集")
    public ReturnResult<MonitorSysGenServerMetrics> collectLocalData(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {
        
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }
            
            // 检查服务器是否启用本地收集模式
            String reportMethod = getServerReportMethod(server);
            if (!"LOCAL".equals(reportMethod)) {
                return ReturnResult.error("服务器未启用本地收集模式");
            }
            
            return dataReportService.handleLocalReport(server);
            
        } catch (Exception e) {
            log.error("手动收集本地数据失败: serverId={}", serverId, e);
            return ReturnResult.error("收集失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发Prometheus数据查询
     * 
     * @param serverId 服务器ID
     * @return 查询结果
     */
    @PostMapping("/query-prometheus/{serverId}")
    @ApiOperation("手动触发Prometheus数据查询")
    public ReturnResult<Map<String, Object>> queryPrometheusData(
            @ApiParam("服务器ID") @PathVariable Integer serverId) {
        
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器配置不存在");
            }
            
            // 检查服务器是否启用Prometheus查询模式
            String reportMethod = getServerReportMethod(server);
            if (!"PROMETHEUS".equals(reportMethod)) {
                return ReturnResult.error("服务器未启用Prometheus查询模式");
            }
            
            return dataReportService.handlePrometheusQuery(server);
            
        } catch (Exception e) {
            log.error("查询Prometheus数据失败: serverId={}", serverId, e);
            return ReturnResult.error("查询失败: " + e.getMessage());
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
