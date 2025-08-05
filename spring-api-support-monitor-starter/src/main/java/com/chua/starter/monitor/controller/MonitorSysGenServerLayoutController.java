package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLayout;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLayoutService;
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

    /**
     * 根据服务器ID获取布局列表
     *
     * @param serverId 服务器ID
     * @return 布局列表
     */
    @GetMapping("/list/{serverId}")
    @Operation(summary = "获取服务器布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByServerId(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.getLayoutsByServerId(serverId);
    }

    /**
     * 获取服务器的默认布局
     *
     * @param serverId 服务器ID
     * @return 默认布局
     */
    @GetMapping("/default/{serverId}")
    @Operation(summary = "获取服务器默认布局")
    public ReturnResult<MonitorSysGenServerLayout> getDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.getDefaultLayout(serverId);
    }

    /**
     * 保存服务器布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    @PostMapping("/save")
    @Operation(summary = "保存服务器布局配置")
    public ReturnResult<MonitorSysGenServerLayout> saveLayout(
            @RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.saveLayout(layout);
    }

    /**
     * 更新服务器布局配置
     *
     * @param layout 布局配置
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新服务器布局配置")
    public ReturnResult<Boolean> updateLayout(
            @RequestBody MonitorSysGenServerLayout layout) {
        return layoutService.updateLayout(layout);
    }

    /**
     * 删除服务器布局配置
     *
     * @param layoutId 布局ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{layoutId}")
    @Operation(summary = "删除服务器布局配置")
    public ReturnResult<Boolean> deleteLayout(
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.deleteLayout(layoutId);
    }

    /**
     * 设置默认布局
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 操作结果
     */
    @PutMapping("/default/{serverId}/{layoutId}")
    @Operation(summary = "设置默认布局")
    public ReturnResult<Boolean> setDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.setDefaultLayout(serverId, layoutId);
    }

    /**
     * 从组件配置生成布局
     *
     * @param serverId 服务器ID
     * @param layoutName 布局名称
     * @return 操作结果
     */
    @PostMapping("/generate/{serverId}")
    @Operation(summary = "从组件配置生成布局")
    public ReturnResult<MonitorSysGenServerLayout> generateLayoutFromComponents(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局名称") @RequestParam String layoutName) {
        return layoutService.generateLayoutFromComponents(serverId, layoutName);
    }

    /**
     * 应用布局到服务器组件
     *
     * @param serverId 服务器ID
     * @param layoutId 布局ID
     * @return 操作结果
     */
    @PostMapping("/apply/{serverId}/{layoutId}")
    @Operation(summary = "应用布局到服务器组件")
    public ReturnResult<Boolean> applyLayoutToComponents(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.applyLayoutToComponents(serverId, layoutId);
    }

    /**
     * 复制布局到其他服务器
     *
     * @param sourceLayoutId 源布局ID
     * @param targetServerId 目标服务器ID
     * @param layoutName 新布局名称
     * @return 操作结果
     */
    @PostMapping("/copy/{sourceLayoutId}/{targetServerId}")
    @Operation(summary = "复制布局到其他服务器")
    public ReturnResult<MonitorSysGenServerLayout> copyLayoutToServer(
            @Parameter(description = "源布局ID") @PathVariable Integer sourceLayoutId,
            @Parameter(description = "目标服务器ID") @PathVariable Integer targetServerId,
            @Parameter(description = "新布局名称") @RequestParam String layoutName) {
        return layoutService.copyLayoutToServer(sourceLayoutId, targetServerId, layoutName);
    }

    /**
     * 获取布局详情
     *
     * @param layoutId 布局ID
     * @return 布局详情
     */
    @GetMapping("/detail/{layoutId}")
    @Operation(summary = "获取布局详情")
    public ReturnResult<MonitorSysGenServerLayout> getLayoutById(
            @Parameter(description = "布局ID") @PathVariable Integer layoutId) {
        return layoutService.getLayoutById(layoutId);
    }

    /**
     * 根据布局类型获取布局列表
     *
     * @param layoutType 布局类型
     * @return 布局列表
     */
    @GetMapping("/type/{layoutType}")
    @Operation(summary = "根据类型获取布局列表")
    public ReturnResult<List<MonitorSysGenServerLayout>> getLayoutsByType(
            @Parameter(description = "布局类型") @PathVariable String layoutType) {
        return layoutService.getLayoutsByType(layoutType);
    }

    /**
     * 创建默认布局
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @PostMapping("/create-default/{serverId}")
    @Operation(summary = "创建默认布局")
    public ReturnResult<MonitorSysGenServerLayout> createDefaultLayout(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return layoutService.createDefaultLayout(serverId);
    }

    /**
     * 保存单个组件的图表配置
     *
     * @param serverId 服务器ID
     * @param componentConfig 组件配置
     * @return 操作结果
     */
    @PostMapping("/component/config/{serverId}")
    @Operation(summary = "保存组件图表配置")
    public ReturnResult<Boolean> saveComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @RequestBody Map<String, Object> componentConfig) {
        return layoutService.saveComponentChartConfig(serverId, componentConfig);
    }

    /**
     * 获取组件的图表配置
     *
     * @param serverId 服务器ID
     * @param componentId 组件ID
     * @return 图表配置
     */
    @GetMapping("/component/config/{serverId}/{componentId}")
    @Operation(summary = "获取组件图表配置")
    public ReturnResult<Map<String, Object>> getComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "组件ID") @PathVariable String componentId) {
        return layoutService.getComponentChartConfig(serverId, componentId);
    }

    /**
     * 批量保存组件图表配置
     *
     * @param serverId 服务器ID
     * @param configs 组件配置列表
     * @return 操作结果
     */
    @PostMapping("/component/config/batch/{serverId}")
    @Operation(summary = "批量保存组件图表配置")
    public ReturnResult<Boolean> batchSaveComponentChartConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @RequestBody List<Map<String, Object>> configs) {
        return layoutService.batchSaveComponentChartConfig(serverId, configs);
    }
}
