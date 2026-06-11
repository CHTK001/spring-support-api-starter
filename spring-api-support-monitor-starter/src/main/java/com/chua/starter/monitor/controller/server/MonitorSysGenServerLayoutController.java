package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLayout;
import com.chua.starter.monitor.service.server.MonitorSysGenServerLayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器布局配置控制器
 *
 * @author CH
 * @since 2025/01/03
 */
@RestController
@RequestMapping("v1/gen/server/layout")
@Tag(name = "服务器布局管理")
@RequiredArgsConstructor
@Slf4j
public class MonitorSysGenServerLayoutController {

    private final MonitorSysGenServerLayoutService layoutService;

    @GetMapping("/list/{serverId}")
    @Operation(summary = "获取服务器布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByServerId(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.getLayoutsByServerId(serverId);
    }

    @GetMapping("/default/{serverId}")
    @Operation(summary = "获取服务器默认布局")
    public ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.getDefaultLayout(serverId);
    }

    @PostMapping("/save")
    @Operation(summary = "保存服务器布局配置")
    public ReturnResult<MonitorSysGenServerLayout> saveLayout(
            @RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.saveLayout(layout);
    }

    @PutMapping("/update")
    @Operation(summary = "更新服务器布局配置")
    public ReturnResult<Boolean> updateLayout(
            @RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.updateLayout(layout);
    }

    @DeleteMapping("/delete/{layoutId}")
    @Operation(summary = "删除服务器布局配置")
    public ReturnResult<Boolean> deleteLayout(
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.deleteLayout(layoutId);
    }

    @PutMapping("/default/{serverId}/{layoutId}")
    @Operation(summary = "设置默认布局")
    public ReturnResult<Boolean> setDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.setDefaultLayout(serverId, layoutId);
    }

    @PostMapping("/generate/{serverId}")
    @Operation(summary = "从组件配置生成布局")
    public ReturnResult<MonitorSysGenServerLayout> generateLayoutFromComponents(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局名称") @RequestParam String layoutName) {
        return layoutService.generateLayoutFromComponents(serverId, layoutName);
    }

    @PostMapping("/apply/{serverId}/{layoutId}")
    @Operation(summary = "应用布局到服务器组件")
    public ReturnResult<Boolean> applyLayoutToComponents(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.applyLayoutToComponents(serverId, layoutId);
    }

    @PostMapping("/copy/{sourceLayoutId}/{targetServerId}")
    @Operation(summary = "复制布局到其他服务器")
    public ReturnResult<MonitorSysGenServerLayout> copyLayoutToServer(
            @Parameter(description = "源布局ID") @PathVariable Integer sourceLayoutId,
            @Parameter(description = "目标服务器ID") @PathVariable Integer targetServerId,
            @RequestParam String layoutName) {
        return layoutService.copyLayoutToServer(sourceLayoutId, targetServerId, layoutName);
    }

    @GetMapping("/detail/{layoutId}")
    @Operation(summary = "获取布局详情")
    public ReturnResult<MonitorSysGenServerLayout> getLayoutById(
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.getLayoutById(layoutId);
    }

    @GetMapping("/type/{layoutType}")
    @Operation(summary = "根据类型获取布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByType(
            @Parameter(description = "布局类型") @PathVariable String layoutType) {
        return layoutService.getLayoutsByType(layoutType);
    }

    @PostMapping("/create-default/{serverId}")
    @Operation(summary = "创建默认布局")
    public ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.createDefaultLayout(serverId);
    }

    @PostMapping("/component/config/{serverId}")
    @Operation(summary = "保存组件图表配置")
    public ReturnResult<Boolean> saveComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @RequestBody Map<String, Object> componentConfig) {
        return layoutService.saveComponentChartConfig(serverId, componentConfig);
    }

    @GetMapping("/component/config/{serverId}/{componentId}")
    @Operation(summary = "获取组件图表配置")
    public ReturnResult<Map<String, Object>> getComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "组件ID") @PathVariable String componentId) {
        return layoutService.getComponentChartConfig(serverId, componentId);
    }

    @PostMapping("/component/config/batch/{serverId}")
    @Operation(summary = "批量保存组件图表配置")
    public ReturnResult<Boolean> batchSaveComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @RequestBody List<Map<String, Object>> configs) {
        return layoutService.batchSaveComponentChartConfig(serverId, configs);
    }
}
