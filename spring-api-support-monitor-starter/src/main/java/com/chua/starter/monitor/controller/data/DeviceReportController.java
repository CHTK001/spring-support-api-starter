package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.service.MonitorSysGenServerDeviceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备数据查询接口
 * <p>
 * 设备数据上报已迁移到 SyncProtocol 方式
 * </p>
 * 
 * @author CH
 * @since 2024/12/19
 */
@RestController
@RequestMapping("api/v1/monitor/device")
@Tag(name = "设备数据查询接口")
@RequiredArgsConstructor
@Slf4j
public class DeviceReportController {

    private final MonitorSysGenServerDeviceReportService deviceReportService;

    /**
     * 获取设备上报统计信息
     *
     * @param deviceId 设备ID
     * @return 统计信息
     */
    @Operation(summary = "获取设备上报统计信息")
    @GetMapping("stats")
    public ReturnResult<Map<String, Object>> getDeviceStats(
            @Parameter(name = "设备ID") @RequestParam(required = false) String deviceId) {
        try {
            if (StringUtils.isEmpty(deviceId)) {
                return deviceReportService.getDeviceReportStatistics();
            } else {
                return deviceReportService.getDeviceReportHistory(deviceId, 100);
            }
        } catch (Exception e) {
            log.error("获取设备统计信息异常", e);
            return ReturnResult.error("获取设备统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备列表
     *
     * @return 设备列表
     */
    @Operation(summary = "获取设备列表")
    @GetMapping("list")
    public ReturnResult<Map<String, Object>> getDeviceList() {
        try {
            return deviceReportService.getActiveDevices(24);
        } catch (Exception e) {
            log.error("获取设备列表异常", e);
            return ReturnResult.error("获取设备列表失败: " + e.getMessage());
        }
    }
}
