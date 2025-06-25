package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.DeviceMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerDeviceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备数据上报接口
 * 
 * @author CH
 * @since 2024/12/19
 */
@RestController
@RequestMapping("api/v1/monitor/device")
@Tag(name = "设备数据上报接口")
@RequiredArgsConstructor
@Slf4j
public class DeviceReportController {

    private final MonitorSysGenServerDeviceReportService deviceReportService;

    /**
     * 接收设备指标数据上报
     *
     * @param metrics 设备指标数据
     * @return 处理结果
     */
    @Operation(summary = "接收设备指标数据上报")
    @PostMapping("metrics")
    public ReturnResult<Map<String, Object>> receiveDeviceMetrics(@RequestBody DeviceMetrics metrics) {
        try {
            log.debug("接收到设备指标数据上报: deviceId={}, ipAddress={}", 
                    metrics.getDeviceId(), metrics.getIpAddress());
            
            if (StringUtils.isEmpty(metrics.getIpAddress())) {
                return ReturnResult.error("设备IP地址不能为空");
            }
            
            // 处理设备指标数据
            ReturnResult<String> result = deviceReportService.handleDeviceMetrics(metrics);
            
            if (result.isOk()) {
                log.debug("设备指标数据处理成功: deviceId={}, result={}",
                        metrics.getDeviceId(), result.getData());
            } else {
                log.warn("设备指标数据处理失败: deviceId={}, error={}",
                        metrics.getDeviceId(), result.getMsg());
            }

            // 转换返回值类型
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", result.getData());
            responseData.put("deviceId", metrics.getDeviceId());
            if (result.isOk()) {
                return ReturnResult.success(responseData);
            } else {
                return ReturnResult.error(result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("处理设备指标数据异常: deviceId={}", metrics.getDeviceId(), e);
            return ReturnResult.error("处理设备指标数据失败: " + e.getMessage());
        }
    }

    /**
     * 接收设备基本信息上报
     *
     * @param deviceInfo 设备基本信息
     * @return 处理结果
     */
    @Operation(summary = "接收设备基本信息上报")
    @PostMapping("info")
    public ReturnResult<Map<String, Object>> receiveDeviceInfo(@RequestBody DeviceMetrics deviceInfo) {
        try {
            log.debug("接收到设备基本信息上报: deviceId={}, ipAddress={}", 
                    deviceInfo.getDeviceId(), deviceInfo.getIpAddress());
            
            if (StringUtils.isEmpty(deviceInfo.getIpAddress())) {
                return ReturnResult.error("设备IP地址不能为空");
            }
            
            // 处理设备基本信息（使用Map格式）
            Map<String, Object> deviceInfoMap = convertDeviceMetricsToMap(deviceInfo);
            ReturnResult<String> result = deviceReportService.handleDeviceMetrics(deviceInfoMap);
            
            if (result.isOk()) {
                log.info("设备基本信息处理成功: deviceId={}, result={}",
                        deviceInfo.getDeviceId(), result.getData());
            } else {
                log.warn("设备基本信息处理失败: deviceId={}, error={}",
                        deviceInfo.getDeviceId(), result.getMsg());
            }

            // 转换返回值类型
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", result.getData());
            responseData.put("deviceId", deviceInfo.getDeviceId());
            if (result.isOk()) {
                return ReturnResult.success(responseData);
            } else {
                return ReturnResult.error(result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("处理设备基本信息异常: deviceId={}", deviceInfo.getDeviceId(), e);
            return ReturnResult.error("处理设备基本信息失败: " + e.getMessage());
        }
    }

    /**
     * 设备心跳检测
     *
     * @param pingData 心跳数据
     * @return 处理结果
     */
    @Operation(summary = "设备心跳检测")
    @PostMapping("ping")
    public ReturnResult<Map<String, Object>> devicePing(@RequestBody Map<String, Object> pingData) {
        try {
            String deviceId = (String) pingData.get("deviceId");
            Long timestamp = (Long) pingData.get("timestamp");
            
            log.debug("接收到设备心跳: deviceId={}, timestamp={}", deviceId, timestamp);
            
            if (StringUtils.isEmpty(deviceId)) {
                return ReturnResult.error("设备ID不能为空");
            }
            
            // 处理设备心跳（简化实现）
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("timestamp", timestamp);
            result.put("status", "success");
            
            log.debug("设备心跳处理成功: deviceId={}", deviceId);

            return ReturnResult.success(result);
            
        } catch (Exception e) {
            log.error("处理设备心跳异常", e);
            return ReturnResult.error("处理设备心跳失败: " + e.getMessage());
        }
    }

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

    /**
     * 将DeviceMetrics转换为Map
     */
    private Map<String, Object> convertDeviceMetricsToMap(DeviceMetrics metrics) {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", metrics.getDeviceId());
        map.put("deviceName", metrics.getDeviceName());
        map.put("deviceIp", metrics.getIpAddress());
        map.put("cpuUsage", metrics.getCpuUsage());
        map.put("memoryUsage", metrics.getMemoryUsage());
        map.put("diskUsage", metrics.getDiskUsage());
        map.put("networkIn", metrics.getNetworkInBytes());
        map.put("networkOut", metrics.getNetworkOutBytes());
        map.put("online", metrics.getOnline());
        map.put("collectTime", metrics.getCollectTime());
        return map;
    }
}
