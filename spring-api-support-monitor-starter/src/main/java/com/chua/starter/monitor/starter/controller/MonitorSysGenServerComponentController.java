package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerComponent;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerComponentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器组件配置控制器
 *
 * @author CH
 * @since 2025/01/03
 */
@Api(tags = "服务器组件配置管理")
@Tag(name = "服务器组件配置管理", description = "服务器组件配置相关接口")
@RestController
@RequestMapping("/v1/gen/server/component")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MonitorSysGenServerComponentController {

    private final MonitorSysGenServerComponentService componentService;

    @ApiOperation("根据服务器ID获取组件列表")
    @Operation(summary = "根据服务器ID获取组件列表", description = "获取指定服务器的所有组件配置")
    @GetMapping("/list/{serverId}")
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerId(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return componentService.getComponentsByServerId(serverId);
    }

    @ApiOperation("根据组件ID获取组件信息")
    @Operation(summary = "根据组件ID获取组件信息", description = "获取指定组件的详细信息")
    @GetMapping("/{componentId}")
    public ReturnResult<MonitorSysGenServerComponent> getComponentById(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        return componentService.getComponentById(componentId);
    }

    @ApiOperation("更新组件信息")
    @Operation(summary = "更新组件信息", description = "更新指定组件的配置信息")
    @PutMapping("/{componentId}")
    public ReturnResult<Boolean> updateComponent(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @RequestBody MonitorSysGenServerComponent component) {
        component.setMonitorSysGenServerComponentId(componentId);
        return componentService.updateComponent(component);
    }

    @ApiOperation("根据服务器ID和组件类型获取组件列表")
    @Operation(summary = "根据服务器ID和组件类型获取组件列表", description = "获取指定服务器指定类型的组件列表")
    @GetMapping("/list/{serverId}/type/{componentType}")
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndType(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @ApiParam(value = "组件类型", required = true)
            @Parameter(description = "组件类型", required = true)
            @PathVariable String componentType) {
        return componentService.getComponentsByServerIdAndType(serverId, componentType);
    }

    @ApiOperation("根据服务器ID和表达式类型获取组件列表")
    @Operation(summary = "根据服务器ID和表达式类型获取组件列表", description = "获取指定服务器指定表达式类型的组件列表")
    @GetMapping("/list/{serverId}/expression/{expressionType}")
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerIdAndExpressionType(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @ApiParam(value = "表达式类型", required = true)
            @Parameter(description = "表达式类型", required = true)
            @PathVariable String expressionType) {
        return componentService.getComponentsByServerIdAndExpressionType(serverId, expressionType);
    }

    @ApiOperation("获取启用的组件列表")
    @Operation(summary = "获取启用的组件列表", description = "获取指定服务器所有启用的组件")
    @GetMapping("/list/{serverId}/enabled")
    public ReturnResult<List<MonitorSysGenServerComponent>> getEnabledComponentsByServerId(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return componentService.getEnabledComponentsByServerId(serverId);
    }

    @ApiOperation("批量更新组件位置信息")
    @Operation(summary = "批量更新组件位置信息", description = "批量更新多个组件的位置信息")
    @PutMapping("/batch/position")
    public ReturnResult<Boolean> batchUpdatePosition(
            @RequestBody List<MonitorSysGenServerComponent> components) {
        return componentService.batchUpdatePosition(components);
    }

    @ApiOperation("为服务器初始化默认组件")
    @Operation(summary = "为服务器初始化默认组件", description = "为指定服务器初始化默认的组件配置")
    @PostMapping("/init/{serverId}")
    public ReturnResult<Boolean> initDefaultComponentsForServer(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return componentService.initDefaultComponentsForServer(serverId);
    }

    @ApiOperation("获取服务器组件统计信息")
    @Operation(summary = "获取服务器组件统计信息", description = "获取指定服务器的组件统计信息")
    @GetMapping("/statistics/{serverId}")
    public ReturnResult<Map<String, Object>> getComponentStatistics(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return componentService.getComponentStatistics(serverId);
    }

    @ApiOperation("执行组件查询")
    @Operation(summary = "执行组件查询", description = "执行指定组件的数据查询")
    @PostMapping("/query/{componentId}")
    public ReturnResult<Object> executeComponentQuery(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @RequestBody Map<String, Object> timeRange) {
        return componentService.executeComponentQuery(componentId, timeRange);
    }

    @ApiOperation("验证组件表达式")
    @Operation(summary = "验证组件表达式", description = "验证组件表达式的语法正确性")
    @PostMapping("/validate/{serverId}")
    public ReturnResult<Boolean> validateComponentExpression(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @RequestParam String expressionType,
            @RequestParam String expression) {
        return componentService.validateComponentExpression(expressionType, expression, serverId);
    }

    @ApiOperation("克隆组件")
    @Operation(summary = "克隆组件", description = "将组件克隆到指定服务器")
    @PostMapping("/clone/{sourceComponentId}/to/{targetServerId}")
    public ReturnResult<MonitorSysGenServerComponent> cloneComponent(
            @ApiParam(value = "源组件ID", required = true)
            @Parameter(description = "源组件ID", required = true)
            @PathVariable Long sourceComponentId,
            @ApiParam(value = "目标服务器ID", required = true)
            @Parameter(description = "目标服务器ID", required = true)
            @PathVariable Integer targetServerId) {
        return componentService.cloneComponent(sourceComponentId, targetServerId);
    }

    @ApiOperation("删除服务器的所有组件")
    @Operation(summary = "删除服务器的所有组件", description = "删除指定服务器的所有组件配置")
    @DeleteMapping("/server/{serverId}")
    public ReturnResult<Boolean> deleteComponentsByServerId(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        return componentService.deleteComponentsByServerId(serverId);
    }

    @ApiOperation("启用/禁用组件")
    @Operation(summary = "启用/禁用组件", description = "切换组件的启用状态")
    @PutMapping("/{componentId}/toggle")
    public ReturnResult<Boolean> toggleComponentEnabled(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @RequestParam Boolean enabled) {
        return componentService.toggleComponentEnabled(componentId, enabled);
    }

    @ApiOperation("更新组件排序")
    @Operation(summary = "更新组件排序", description = "更新组件的排序序号")
    @PutMapping("/{componentId}/sort")
    public ReturnResult<Boolean> updateComponentSortOrder(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @RequestParam Integer sortOrder) {
        return componentService.updateComponentSortOrder(componentId, sortOrder);
    }

    @ApiOperation("创建新组件")
    @Operation(summary = "创建新组件", description = "为指定服务器创建新的组件配置")
    @PostMapping("/create")
    public ReturnResult<Boolean> createComponent(@RequestBody MonitorSysGenServerComponent component) {
        return ReturnResult.ok(componentService.save(component));
    }

    @ApiOperation("删除组件")
    @Operation(summary = "删除组件", description = "删除指定的组件配置")
    @DeleteMapping("/{componentId}")
    public ReturnResult<Boolean> deleteComponent(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        return ReturnResult.ok(componentService.removeById(componentId));
    }
}
