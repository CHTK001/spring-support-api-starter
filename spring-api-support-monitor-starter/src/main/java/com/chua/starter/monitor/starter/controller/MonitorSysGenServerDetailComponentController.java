package com.chua.starter.monitor.starter.controller;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServerDetailComponent;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerDetailComponentService;
import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器详情页组件配置控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/gen/server/detail/component")
@Tag(name = "服务器详情页组件管理")
@RequiredArgsConstructor
public class MonitorSysGenServerDetailComponentController {

    private final MonitorSysGenServerDetailComponentService componentService;

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID
     * @return 组件列表
     */
    @GetMapping("list")
    @Operation(summary = "获取服务器组件列表")
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerId(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.getComponentsByServerId(serverId);
    }

    /**
     * 根据服务器ID和组件类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param componentType 组件类型
     * @return 组件列表
     */
    @GetMapping("list/type")
    @Operation(summary = "根据类型获取服务器组件列表")
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndType(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId,
            @Parameter(name = "组件类型") @RequestParam String componentType) {
        return componentService.getComponentsByServerIdAndType(serverId, componentType);
    }

    /**
     * 根据服务器ID和表达式类型获取组件列表
     *
     * @param serverId 服务器ID
     * @param expressionType 表达式类型
     * @return 组件列表
     */
    @GetMapping("list/expression")
    @Operation(summary = "根据表达式类型获取服务器组件列表")
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentsByServerIdAndExpressionType(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId,
            @Parameter(name = "表达式类型") @RequestParam String expressionType) {
        return componentService.getComponentsByServerIdAndExpressionType(serverId, expressionType);
    }

    /**
     * 获取启用的组件列表
     *
     * @param serverId 服务器ID
     * @return 启用的组件列表
     */
    @GetMapping("list/enabled")
    @Operation(summary = "获取启用的服务器组件列表")
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getEnabledComponentsByServerId(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.getEnabledComponentsByServerId(serverId);
    }

    /**
     * 保存或更新组件
     *
     * @param component 组件信息
     * @return 操作结果
     */
    @PostMapping("save")
    @Operation(summary = "保存或更新组件")
    public ReturnResult<MonitorSysGenServerDetailComponent> saveOrUpdateComponent(
            @RequestBody MonitorSysGenServerDetailComponent component) {
        return componentService.saveOrUpdateComponent(component);
    }

    /**
     * 批量更新组件位置
     *
     * @param components 组件列表
     * @return 操作结果
     */
    @PostMapping("batch/position")
    @Operation(summary = "批量更新组件位置")
    public ReturnResult<Boolean> batchUpdateComponentPosition(
            @RequestBody List<MonitorSysGenServerDetailComponent> components) {
        return componentService.batchUpdateComponentPosition(components);
    }

    /**
     * 删除组件
     *
     * @param componentId 组件ID
     * @return 操作结果
     */
    @DeleteMapping("delete")
    @Operation(summary = "删除组件")
    public ReturnResult<Boolean> deleteComponent(
            @Parameter(name = "组件ID") @RequestParam Long componentId) {
        return componentService.deleteComponent(componentId);
    }

    /**
     * 根据服务器ID删除所有组件
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @DeleteMapping("delete/server")
    @Operation(summary = "删除服务器所有组件")
    public ReturnResult<Boolean> deleteComponentsByServerId(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.deleteComponentsByServerId(serverId);
    }

    /**
     * 获取组件模板列表
     *
     * @return 模板列表
     */
    @GetMapping("templates")
    @Operation(summary = "获取组件模板列表")
    public ReturnResult<List<MonitorSysGenServerDetailComponent>> getComponentTemplates() {
        return componentService.getComponentTemplates();
    }

    /**
     * 为服务器初始化默认组件
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @PostMapping("init")
    @Operation(summary = "为服务器初始化默认组件")
    public ReturnResult<Boolean> initDefaultComponentsForServer(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.initDefaultComponentsForServer(serverId);
    }

    /**
     * 复制模板组件到指定服务器
     *
     * @param serverId 目标服务器ID
     * @param templateIds 模板ID列表
     * @return 操作结果
     */
    @PostMapping("copy/templates")
    @Operation(summary = "复制模板组件到服务器")
    public ReturnResult<Boolean> copyTemplatesForServer(
            @Parameter(name = "目标服务器ID") @RequestParam Integer serverId,
            @Parameter(name = "模板ID列表") @RequestBody List<Long> templateIds) {
        return componentService.copyTemplatesForServer(serverId, templateIds);
    }

    /**
     * 获取服务器组件统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    @GetMapping("statistics")
    @Operation(summary = "获取服务器组件统计信息")
    public ReturnResult<Map<String, Object>> getComponentStatistics(
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.getComponentStatistics(serverId);
    }

    /**
     * 执行组件查询
     *
     * @param componentId 组件ID
     * @param timeRange 时间范围参数
     * @return 查询结果
     */
    @PostMapping("query")
    @Operation(summary = "执行组件查询")
    public ReturnResult<Object> executeComponentQuery(
            @Parameter(name = "组件ID") @RequestParam Long componentId,
            @RequestBody Map<String, Object> timeRange) {
        return componentService.executeComponentQuery(componentId, timeRange);
    }

    /**
     * 验证组件表达式
     *
     * @param expressionType 表达式类型
     * @param expression 表达式内容
     * @param serverId 服务器ID
     * @return 验证结果
     */
    @PostMapping("validate")
    @Operation(summary = "验证组件表达式")
    public ReturnResult<Boolean> validateComponentExpression(
            @Parameter(name = "表达式类型") @RequestParam String expressionType,
            @Parameter(name = "表达式内容") @RequestParam String expression,
            @Parameter(name = "服务器ID") @RequestParam Integer serverId) {
        return componentService.validateComponentExpression(expressionType, expression, serverId);
    }

    /**
     * 克隆组件
     *
     * @param sourceComponentId 源组件ID
     * @param targetServerId 目标服务器ID
     * @return 克隆结果
     */
    @PostMapping("clone")
    @Operation(summary = "克隆组件")
    public ReturnResult<MonitorSysGenServerDetailComponent> cloneComponent(
            @Parameter(name = "源组件ID") @RequestParam Long sourceComponentId,
            @Parameter(name = "目标服务器ID") @RequestParam Integer targetServerId) {
        return componentService.cloneComponent(sourceComponentId, targetServerId);
    }

    /**
     * 启用/禁用组件
     *
     * @param componentId 组件ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PostMapping("toggle")
    @Operation(summary = "启用/禁用组件")
    public ReturnResult<Boolean> toggleComponentEnabled(
            @Parameter(name = "组件ID") @RequestParam Long componentId,
            @Parameter(name = "是否启用") @RequestParam Boolean enabled) {
        return componentService.toggleComponentEnabled(componentId, enabled);
    }
}
