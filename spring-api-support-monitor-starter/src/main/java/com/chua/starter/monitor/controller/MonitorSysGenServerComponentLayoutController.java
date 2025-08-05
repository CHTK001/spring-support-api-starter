package com.chua.starter.monitor.controller;

import com.chua.starter.monitor.entity.MonitorSysGenServerComponentLayout;
import com.chua.starter.monitor.service.MonitorSysGenServerComponentLayoutService;
import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器组件布局配置控制器
 *
 * @author CH
 * @since 2025/01/06
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server/component/layout")
@RequiredArgsConstructor
@Api(tags = "服务器组件布局配置管理")
@Tag(name = "服务器组件布局配置管理", description = "服务器组件布局配置相关接口")
public class MonitorSysGenServerComponentLayoutController {

    private final MonitorSysGenServerComponentLayoutService layoutService;

    @GetMapping("/server/{serverId}")
    @ApiOperation("根据服务器ID获取组件布局列表")
    @Operation(summary = "根据服务器ID获取组件布局列表", description = "获取指定服务器的所有组件布局配置")
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> getLayoutsByServerId(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return layoutService.getLayoutsByServerId(serverId);
    }

    @GetMapping("/server/{serverId}/enabled")
    @ApiOperation("根据服务器ID获取启用的组件布局列表")
    @Operation(summary = "根据服务器ID获取启用的组件布局列表", description = "获取指定服务器的启用状态组件布局配置")
    public ReturnResult<List<MonitorSysGenServerComponentLayout>> getEnabledLayoutsByServerId(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return layoutService.getEnabledLayoutsByServerId(serverId);
    }

    @GetMapping("/server/{serverId}/component/{componentId}")
    @ApiOperation("根据服务器ID和组件ID获取布局配置")
    @Operation(summary = "根据服务器ID和组件ID获取布局配置", description = "获取指定服务器和组件的布局配置")
    public ReturnResult<MonitorSysGenServerComponentLayout> getLayoutByServerIdAndComponentId(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @ApiParam(value = "组件ID", required = true) 
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        return layoutService.getLayoutByServerIdAndComponentId(serverId, componentId);
    }

    @PostMapping
    @ApiOperation("保存组件布局配置")
    @Operation(summary = "保存组件布局配置", description = "创建新的组件布局配置")
    public ReturnResult<MonitorSysGenServerComponentLayout> saveLayout(
            @ApiParam(value = "布局配置", required = true) 
            @Parameter(description = "布局配置", required = true)
            @RequestBody MonitorSysGenServerComponentLayout layout) {
        return layoutService.saveLayout(layout);
    }

    @PutMapping
    @ApiOperation("更新组件布局配置")
    @Operation(summary = "更新组件布局配置", description = "更新现有的组件布局配置")
    public ReturnResult<Boolean> updateLayout(
            @ApiParam(value = "布局配置", required = true) 
            @Parameter(description = "布局配置", required = true)
            @RequestBody MonitorSysGenServerComponentLayout layout) {
        return layoutService.updateLayout(layout);
    }

    @DeleteMapping("/{layoutId}")
    @ApiOperation("删除组件布局配置")
    @Operation(summary = "删除组件布局配置", description = "根据布局ID删除组件布局配置")
    public ReturnResult<Boolean> deleteLayout(
            @ApiParam(value = "布局ID", required = true) 
            @Parameter(description = "布局ID", required = true)
            @PathVariable Long layoutId) {
        return layoutService.deleteLayout(layoutId);
    }

    @DeleteMapping("/component/{componentId}")
    @ApiOperation("根据组件ID删除布局配置")
    @Operation(summary = "根据组件ID删除布局配置", description = "删除指定组件的所有布局配置")
    public ReturnResult<Boolean> deleteLayoutByComponentId(
            @ApiParam(value = "组件ID", required = true) 
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        return layoutService.deleteLayoutByComponentId(componentId);
    }

    @DeleteMapping("/server/{serverId}")
    @ApiOperation("根据服务器ID删除所有布局配置")
    @Operation(summary = "根据服务器ID删除所有布局配置", description = "删除指定服务器的所有布局配置")
    public ReturnResult<Boolean> deleteLayoutsByServerId(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return layoutService.deleteLayoutsByServerId(serverId);
    }

    @PostMapping("/batch/position")
    @ApiOperation("批量更新组件布局位置信息")
    @Operation(summary = "批量更新组件布局位置信息", description = "批量更新多个组件的布局位置信息")
    public ReturnResult<Boolean> batchUpdatePosition(
            @ApiParam(value = "布局列表", required = true) 
            @Parameter(description = "布局列表", required = true)
            @RequestBody List<MonitorSysGenServerComponentLayout> layouts) {
        return layoutService.batchUpdatePosition(layouts);
    }

    @PostMapping("/create")
    @ApiOperation("创建组件布局配置")
    @Operation(summary = "创建组件布局配置", description = "为指定服务器和组件创建布局配置")
    public ReturnResult<MonitorSysGenServerComponentLayout> createLayout(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @RequestParam Integer serverId,
            @ApiParam(value = "组件ID", required = true) 
            @Parameter(description = "组件ID", required = true)
            @RequestParam Long componentId,
            @ApiParam(value = "X坐标") 
            @Parameter(description = "X坐标")
            @RequestParam(required = false) Integer x,
            @ApiParam(value = "Y坐标") 
            @Parameter(description = "Y坐标")
            @RequestParam(required = false) Integer y,
            @ApiParam(value = "宽度") 
            @Parameter(description = "宽度")
            @RequestParam(required = false) Integer w,
            @ApiParam(value = "高度") 
            @Parameter(description = "高度")
            @RequestParam(required = false) Integer h) {
        return layoutService.createLayout(serverId, componentId, x, y, w, h);
    }

    @PutMapping("/{layoutId}/position")
    @ApiOperation("更新组件布局位置")
    @Operation(summary = "更新组件布局位置", description = "更新指定布局的位置信息")
    public ReturnResult<Boolean> updateLayoutPosition(
            @ApiParam(value = "布局ID", required = true) 
            @Parameter(description = "布局ID", required = true)
            @PathVariable Long layoutId,
            @ApiParam(value = "X坐标") 
            @Parameter(description = "X坐标")
            @RequestParam(required = false) Integer x,
            @ApiParam(value = "Y坐标") 
            @Parameter(description = "Y坐标")
            @RequestParam(required = false) Integer y,
            @ApiParam(value = "宽度") 
            @Parameter(description = "宽度")
            @RequestParam(required = false) Integer w,
            @ApiParam(value = "高度") 
            @Parameter(description = "高度")
            @RequestParam(required = false) Integer h) {
        return layoutService.updateLayoutPosition(layoutId, x, y, w, h);
    }

    @PutMapping("/{layoutId}/status")
    @ApiOperation("启用/禁用组件布局")
    @Operation(summary = "启用/禁用组件布局", description = "设置指定布局的启用状态")
    public ReturnResult<Boolean> setLayoutEnabled(
            @ApiParam(value = "布局ID", required = true) 
            @Parameter(description = "布局ID", required = true)
            @PathVariable Long layoutId,
            @ApiParam(value = "是否启用", required = true) 
            @Parameter(description = "是否启用", required = true)
            @RequestParam Boolean enabled) {
        return layoutService.setLayoutEnabled(layoutId, enabled);
    }

    @GetMapping("/server/{serverId}/statistics")
    @ApiOperation("获取服务器组件布局统计信息")
    @Operation(summary = "获取服务器组件布局统计信息", description = "获取指定服务器的布局统计信息")
    public ReturnResult<Map<String, Object>> getLayoutStatistics(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return layoutService.getLayoutStatistics(serverId);
    }

    @PostMapping("/copy")
    @ApiOperation("复制布局配置到其他服务器")
    @Operation(summary = "复制布局配置到其他服务器", description = "将源服务器的布局配置复制到目标服务器")
    public ReturnResult<Boolean> copyLayoutsToServer(
            @ApiParam(value = "源服务器ID", required = true) 
            @Parameter(description = "源服务器ID", required = true)
            @RequestParam Integer sourceServerId,
            @ApiParam(value = "目标服务器ID", required = true) 
            @Parameter(description = "目标服务器ID", required = true)
            @RequestParam Integer targetServerId) {
        return layoutService.copyLayoutsToServer(sourceServerId, targetServerId);
    }

    @PostMapping("/server/{serverId}/reset")
    @ApiOperation("重置服务器布局配置")
    @Operation(summary = "重置服务器布局配置", description = "删除指定服务器的所有布局配置")
    public ReturnResult<Boolean> resetServerLayouts(
            @ApiParam(value = "服务器ID", required = true) 
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return layoutService.resetServerLayouts(serverId);
    }
}
