package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监控配置控制器
 * @author CH
 * @since 2024/12/27
 */
@RestController
@RequestMapping("/v1/monitor/config")
@RequiredArgsConstructor
@Tag(name = "监控配置管理", description = "监控配置管理相关接口")
public class MonitorConfigController {

    private final MonitorConfigService monitorConfigService;

    /**
     * 获取监控阈值配置
     */
    @GetMapping("/thresholds")
    @Operation(summary = "获取监控阈值配置")
    public ReturnResult<Map<String, Object>> getMonitorThresholds() {
        return ReturnResult.ok(monitorConfigService.getMonitorThresholds());
    }

    /**
     * 保存监控阈值配置
     */
    @PostMapping("/thresholds")
    @Operation(summary = "保存监控阈值配置")
    public ReturnResult<Boolean> saveMonitorThresholds(@RequestBody Map<String, Object> thresholds) {
        monitorConfigService.saveMonitorThresholds(thresholds);
        return ReturnResult.ok(true);
    }

    /**
     * 获取全局监控配置
     */
    @GetMapping("/global")
    @Operation(summary = "获取全局监控配置")
    public ReturnResult<Map<String, Object>> getGlobalMonitorConfig() {
        return ReturnResult.ok(monitorConfigService.getGlobalMonitorConfig());
    }

    /**
     * 保存全局监控配置
     */
    @PostMapping("/global")
    @Operation(summary = "保存全局监控配置")
    public ReturnResult<Boolean> saveGlobalMonitorConfig(@RequestBody Map<String, Object> config) {
        monitorConfigService.saveGlobalMonitorConfig(config);
        return ReturnResult.ok(true);
    }

    /**
     * 重置监控配置为默认值
     */
    @PostMapping("/reset")
    @Operation(summary = "重置监控配置为默认值")
    public ReturnResult<Boolean> resetMonitorConfig() {
        monitorConfigService.resetMonitorConfig();
        return ReturnResult.ok(true);
    }

    /**
     * 测试告警配置
     */
    @PostMapping("/test-alert")
    @Operation(summary = "测试告警配置")
    public ReturnResult<Boolean> testAlertConfig(@RequestBody Map<String, Object> testData) {
        return ReturnResult.ok(monitorConfigService.testAlertConfig(testData));
    }
}
