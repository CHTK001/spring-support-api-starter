package com.chua.starter.monitor.controller.data;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.service.MonitorSysGenServerDataReportService;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.UnifiedAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据上报控制器
 * <p>
 * 处理客户端的数据上报请求
 * </p>
 * <p>
 * <b>注意：此控制器已废弃，请使用节点方式上报</b>
 * </p>
 * <ul>
 *   <li>节点上报通过 SyncClient 连接到 SyncServer</li>
 *   <li>使用 Topic: monitor/device/metrics 上报设备指标</li>
 *   <li>服务端通过 SyncConfiguration 处理上报数据</li>
 * </ul>
 *
 * @author CH
 * @since 2024/12/25
 * @deprecated 使用节点方式上报替代，参考 {@link com.chua.starter.monitor.configuration.SyncConfiguration}
 */
@Slf4j
@RestController
@RequestMapping("/data-report")
@Api(tags = "数据上报管理（已废弃）")
@RequiredArgsConstructor
@Deprecated
public class DataReportController {

    private final MonitorSysGenServerDataReportService dataReportService;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final UnifiedAlertService unifiedAlertService;

    /**
     * 客户端数据上报接口
     * <p>
     * <b>已废弃：请使用节点方式上报</b>
     * </p>
     *
     * @param serverId   服务器ID
     * @param reportData 上报的数据
     * @return 处理结果
     * @deprecated 使用节点方式上报替代
     */
    @Deprecated
    @PostMapping("/submit/{serverId}")
    @ApiOperation("客户端数据上报（已废弃）")
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
            
            // 检查服务器是否启用节点上报
            try {
                ReturnResult<MonitorSysGenServerSetting> settingResult =
                    serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());

                if (!settingResult.isOk() || settingResult.getData() == null) {
                    return ReturnResult.error("获取服务器配置失败");
                }

                MonitorSysGenServerSetting setting = settingResult.getData();
                if (!"NODE".equals(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                    return ReturnResult.error("服务器未启用节点上报模式");
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
                    // 使用统一告警服务检测和推送API上报数据的告警
                    ReturnResult<Boolean> alertResult =
                        unifiedAlertService.checkAndPushApiReportAlerts(serverId, reportData);

                    if (alertResult.isOk() && alertResult.getData() != null && alertResult.getData()) {
                        log.info("API上报数据检测到告警并已推送: serverId={}", serverId);
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

}
