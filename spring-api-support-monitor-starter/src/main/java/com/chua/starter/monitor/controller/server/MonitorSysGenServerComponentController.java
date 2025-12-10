package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerComponent;
import com.chua.starter.monitor.service.MonitorSysGenServerComponentService;
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

    /**
     * 根据服务器ID获取组件列表
     *
     * @param serverId 服务器ID，必填参数，用于标识特定的服务器
     * @return 返回指定服务器的所有组件配置列表，包含组件的详细信息
     * @throws IllegalArgumentException 当服务器ID为空或无效时抛出
     * @since 2025/01/03
     */
    @ApiOperation("根据服务器ID获取组件列表")
    @Operation(summary = "根据服务器ID获取组件列表", description = "获取指定服务器的所有组件配置")
    @GetMapping("/list/{serverId}")
    public ReturnResult<List<MonitorSysGenServerComponent>> getComponentsByServerId(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        log.info("开始获取服务器组件列表, serverId: {}", serverId);
        try {
            ReturnResult<List<MonitorSysGenServerComponent>> result = componentService.getComponentsByServerId(serverId);
            log.info("获取服务器组件列表成功, serverId: {}, 组件数量: {}", serverId,
                    result.isSuccess() && result.getData() != null ? result.getData().size() : 0);
            return result;
        } catch (Exception e) {
            log.error("获取服务器组件列表失败, serverId: {}", serverId, e);
            throw e;
        }
    }

    /**
     * 根据组件ID获取组件信息
     *
     * @param componentId 组件ID，必填参数，用于标识特定的组件
     * @return 返回指定组件的详细信息，包含组件的所有配置属性
     * @throws IllegalArgumentException 当组件ID为空或无效时抛出
     * @since 2025/01/03
     */
    @ApiOperation("根据组件ID获取组件信息")
    @Operation(summary = "根据组件ID获取组件信息", description = "获取指定组件的详细信息")
    @GetMapping("/{componentId}")
    public ReturnResult<MonitorSysGenServerComponent> getComponentById(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        log.info("开始获取组件信息, componentId: {}", componentId);
        try {
            ReturnResult<MonitorSysGenServerComponent> result = componentService.getComponentById(componentId);
            log.info("获取组件信息成功, componentId: {}, 组件名称: {}", componentId,
                    result.isSuccess() && result.getData() != null ? result.getData().getMonitorSysGenServerComponentName() : "未知");
            return result;
        } catch (Exception e) {
            log.error("获取组件信息失败, componentId: {}", componentId, e);
            throw e;
        }
    }

    /**
     * 更新组件信息
     *
     * @param componentId 组件ID，必填参数，用于标识要更新的组件
     * @param component   组件信息对象，包含要更新的组件配置数据
     * @return 返回更新操作的结果，true表示更新成功，false表示更新失败
     * @throws IllegalArgumentException 当组件ID或组件信息为空时抛出
     * @since 2025/01/03
     */
    @ApiOperation("更新组件信息")
    @Operation(summary = "更新组件信息", description = "更新指定组件的配置信息")
    @PutMapping("/{componentId}")
    public ReturnResult<Boolean> updateComponent(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @RequestBody MonitorSysGenServerComponent component) {
        log.info("开始更新组件信息, componentId: {}, componentName: {}", componentId,
                component != null ? component.getMonitorSysGenServerComponentName() : "未知");
        try {
            if (component == null) {
                log.warn("更新组件信息失败, 组件信息为空, componentId: {}", componentId);
                return ReturnResult.error("组件信息不能为空");
            }
            component.setMonitorSysGenServerComponentId(componentId);
            ReturnResult<Boolean> result = componentService.updateComponent(component);
            log.info("更新组件信息成功, componentId: {}, 更新结果: {}", componentId, result.isSuccess());
            return result;
        } catch (Exception e) {
            log.error("更新组件信息失败, componentId: {}", componentId, e);
            throw e;
        }
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

    /**
     * 查询组件数据（支持时间范围）
     *
     * @param componentId 组件ID，必填参数，用于标识要查询数据的组件
     * @param startTime   开始时间（Unix时间戳，秒），可选参数，默认为当前时间前30分钟
     * @param endTime     结束时间（Unix时间戳，秒），可选参数，默认为当前时间
     * @param step        数据采样步长（秒），默认为60秒，用于控制数据点的密度
     * @return 返回组件在指定时间范围内的监控数据，数据格式根据组件类型而定
     * @throws IllegalArgumentException 当组件ID无效或时间参数不合理时抛出
     * @since 2025/01/03
     */
    @ApiOperation("查询组件数据（支持时间范围）")
    @Operation(summary = "查询组件数据", description = "根据时间范围查询组件的监控数据")
    @GetMapping("/{componentId}/data")
    public ReturnResult<Object> getComponentData(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId,
            @ApiParam(value = "开始时间（时间戳，秒）")
            @Parameter(description = "开始时间（时间戳，秒）")
            @RequestParam(required = false) Long startTime,
            @ApiParam(value = "结束时间（时间戳，秒）")
            @Parameter(description = "结束时间（时间戳，秒）")
            @RequestParam(required = false) Long endTime,
            @ApiParam(value = "步长（秒）", defaultValue = "60")
            @Parameter(description = "步长（秒）")
            @RequestParam(defaultValue = "60") Integer step) {
        log.info("开始查询组件数据, componentId: {}, startTime: {}, endTime: {}, step: {}",
                componentId, startTime, endTime, step);
        try {
            ReturnResult<Object> result = componentService.getComponentData(componentId, startTime, endTime, step);
            log.info("查询组件数据成功, componentId: {}, 查询结果: {}", componentId, result.isSuccess());
            return result;
        } catch (Exception e) {
            log.error("查询组件数据失败, componentId: {}, startTime: {}, endTime: {}, step: {}",
                    componentId, startTime, endTime, step, e);
            throw e;
        }
    }

    /**
     * 批量查询组件数据
     *
     * @param serverId  服务器ID，必填参数，用于标识要查询组件数据的服务器
     * @param startTime 开始时间（Unix时间戳，秒），可选参数，默认为当前时间前30分钟
     * @param endTime   结束时间（Unix时间戳，秒），可选参数，默认为当前时间
     * @param step      数据采样步长（秒），默认为60秒，用于控制数据点的密度
     * @return 返回指定服务器所有启用组件在指定时间范围内的监控数据集合
     * @throws IllegalArgumentException 当服务器ID无效或时间参数不合理时抛出
     * @since 2025/01/03
     */
    @ApiOperation("批量查询组件数据")
    @Operation(summary = "批量查询组件数据", description = "批量查询指定服务器所有组件的监控数据")
    @GetMapping("/batch/{serverId}/data")
    public ReturnResult<Map<String, Object>> getBatchComponentData(
            @ApiParam(value = "服务器ID", required = true)
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @ApiParam(value = "开始时间（时间戳，秒）")
            @Parameter(description = "开始时间（时间戳，秒）")
            @RequestParam(required = false) Long startTime,
            @ApiParam(value = "结束时间（时间戳，秒）")
            @Parameter(description = "结束时间（时间戳，秒）")
            @RequestParam(required = false) Long endTime,
            @ApiParam(value = "步长（秒）", defaultValue = "60")
            @Parameter(description = "步长（秒）")
            @RequestParam(defaultValue = "60") Integer step) {
        log.info("开始批量查询组件数据, serverId: {}, startTime: {}, endTime: {}, step: {}",
                serverId, startTime, endTime, step);
        try {
            ReturnResult<Map<String, Object>> result = componentService.getBatchComponentData(serverId, startTime, endTime, step);
            log.info("批量查询组件数据成功, serverId: {}, 查询结果: {}", serverId, result.isSuccess());
            return result;
        } catch (Exception e) {
            log.error("批量查询组件数据失败, serverId: {}, startTime: {}, endTime: {}, step: {}",
                    serverId, startTime, endTime, step, e);
            throw e;
        }
    }

    @ApiOperation("获取组件实时数据")
    @Operation(summary = "获取组件实时数据", description = "获取组件的实时监控数据")
    @GetMapping("/{componentId}/data/realtime")
    public ReturnResult<Object> getComponentRealtimeData(
            @ApiParam(value = "组件ID", required = true)
            @Parameter(description = "组件ID", required = true)
            @PathVariable Long componentId) {
        return componentService.getComponentRealtimeData(componentId);
    }



}
