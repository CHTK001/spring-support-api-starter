package com.chua.starter.monitor.controller.skywalking;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;
import com.chua.starter.monitor.pojo.skywalking.*;
import com.chua.starter.monitor.service.skywalking.MonitorSkywalkingConfigService;
import com.chua.starter.monitor.service.skywalking.SkywalkingGraphQLService;
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

/**
 * SkyWalking 数据查询控制器
 *
 * @author CH
 * @since 2024-12-18
 */
@RestController
@RequestMapping("/v1/skywalking/data")
@Api(tags = "SkyWalking 数据查询")
@Tag(name = "SkyWalking 数据查询")
@Slf4j
@RequiredArgsConstructor
public class MonitorSkywalkingDataController {

    private final MonitorSkywalkingConfigService skywalkingConfigService;
    private final SkywalkingGraphQLService skywalkingGraphQLService;

    /**
     * 获取配置
     */
    private MonitorSkywalkingConfig getConfig(Integer configId) {
        MonitorSkywalkingConfig config = skywalkingConfigService.getById(configId);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在");
        }
        if (!config.isEnabled()) {
            throw new IllegalArgumentException("配置已禁用");
        }
        return config;
    }

    /**
     * 获取所有层
     */
    @GetMapping("/layers")
    @Operation(summary = "获取所有层")
    @ApiOperation("获取所有层")
    public ReturnResult<List<String>> listLayers(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            List<String> layers = skywalkingGraphQLService.listLayers(config);
            return ReturnResult.ok(layers);
        } catch (Exception e) {
            log.error("获取层列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取服务列表
     */
    @GetMapping("/services")
    @Operation(summary = "获取服务列表")
    @ApiOperation("获取服务列表")
    public ReturnResult<List<SkywalkingServiceInfo>> listServices(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step,
            @RequestParam(value = "layer", required = false) @Parameter(description = "层") @ApiParam("层") String layer) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            condition.setLayer(layer);
            List<SkywalkingServiceInfo> services = skywalkingGraphQLService.listServices(config, condition);
            return ReturnResult.ok(services);
        } catch (Exception e) {
            log.error("获取服务列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 搜索服务
     */
    @GetMapping("/services/search")
    @Operation(summary = "搜索服务")
    @ApiOperation("搜索服务")
    public ReturnResult<List<SkywalkingServiceInfo>> searchServices(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "关键词") @ApiParam("关键词") String keyword) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            condition.setKeyword(keyword);
            List<SkywalkingServiceInfo> services = skywalkingGraphQLService.searchServices(config, condition);
            return ReturnResult.ok(services);
        } catch (Exception e) {
            log.error("搜索服务失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取服务实例列表
     */
    @GetMapping("/services/{serviceId}/instances")
    @Operation(summary = "获取服务实例列表")
    @ApiOperation("获取服务实例列表")
    public ReturnResult<List<SkywalkingServiceInfo.Instance>> listInstances(
            @PathVariable("serviceId") @Parameter(description = "服务ID") @ApiParam("服务ID") String serviceId,
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setServiceId(serviceId);
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            List<SkywalkingServiceInfo.Instance> instances = skywalkingGraphQLService.listInstances(config, condition);
            return ReturnResult.ok(instances);
        } catch (Exception e) {
            log.error("获取服务实例列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取端点列表
     */
    @GetMapping("/services/{serviceId}/endpoints")
    @Operation(summary = "获取端点列表")
    @ApiOperation("获取端点列表")
    public ReturnResult<List<SkywalkingServiceInfo.Endpoint>> findEndpoints(
            @PathVariable("serviceId") @Parameter(description = "服务ID") @ApiParam("服务ID") String serviceId,
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "关键词") @ApiParam("关键词") String keyword,
            @RequestParam(value = "limit", defaultValue = "100") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setServiceId(serviceId);
            condition.setKeyword(keyword);
            condition.setPageSize(limit);
            List<SkywalkingServiceInfo.Endpoint> endpoints = skywalkingGraphQLService.findEndpoints(config, condition);
            return ReturnResult.ok(endpoints);
        } catch (Exception e) {
            log.error("获取端点列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 查询链路追踪列表
     */
    @GetMapping("/traces")
    @Operation(summary = "查询链路追踪列表")
    @ApiOperation("查询链路追踪列表")
    public ReturnResult<SkywalkingTrace.Brief> queryBasicTraces(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step,
            @RequestParam(value = "serviceId", required = false) @Parameter(description = "服务ID") @ApiParam("服务ID") String serviceId,
            @RequestParam(value = "serviceInstanceId", required = false) @Parameter(description = "服务实例ID") @ApiParam("服务实例ID") String serviceInstanceId,
            @RequestParam(value = "endpointId", required = false) @Parameter(description = "端点ID") @ApiParam("端点ID") String endpointId,
            @RequestParam(value = "endpointName", required = false) @Parameter(description = "端点名称") @ApiParam("端点名称") String endpointName,
            @RequestParam(value = "traceId", required = false) @Parameter(description = "Trace ID") @ApiParam("Trace ID") String traceId,
            @RequestParam(value = "traceState", defaultValue = "ALL") @Parameter(description = "Trace 状态") @ApiParam("Trace 状态") String traceState,
            @RequestParam(value = "queryOrder", defaultValue = "BY_START_TIME") @Parameter(description = "排序") @ApiParam("排序") String queryOrder,
            @RequestParam(value = "minTraceDuration", required = false) @Parameter(description = "最小持续时间") @ApiParam("最小持续时间") Integer minTraceDuration,
            @RequestParam(value = "maxTraceDuration", required = false) @Parameter(description = "最大持续时间") @ApiParam("最大持续时间") Integer maxTraceDuration,
            @RequestParam(value = "pageNum", defaultValue = "1") @Parameter(description = "页码") @ApiParam("页码") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") @Parameter(description = "每页大小") @ApiParam("每页大小") Integer pageSize) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            condition.setServiceId(serviceId);
            condition.setServiceInstanceId(serviceInstanceId);
            condition.setEndpointId(endpointId);
            condition.setEndpointName(endpointName);
            condition.setTraceId(traceId);
            condition.setTraceState(traceState);
            condition.setQueryOrder(queryOrder);
            condition.setMinTraceDuration(minTraceDuration);
            condition.setMaxTraceDuration(maxTraceDuration);
            condition.setPageNum(pageNum);
            condition.setPageSize(pageSize);
            SkywalkingTrace.Brief traces = skywalkingGraphQLService.queryBasicTraces(config, condition);
            return ReturnResult.ok(traces);
        } catch (Exception e) {
            log.error("查询链路追踪列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 查询链路详情
     */
    @GetMapping("/traces/{traceId}")
    @Operation(summary = "查询链路详情")
    @ApiOperation("查询链路详情")
    public ReturnResult<SkywalkingTrace> queryTrace(
            @PathVariable("traceId") @Parameter(description = "Trace ID") @ApiParam("Trace ID") String traceId,
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingTrace trace = skywalkingGraphQLService.queryTrace(config, traceId);
            return ReturnResult.ok(trace);
        } catch (Exception e) {
            log.error("查询链路详情失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取全局拓扑
     */
    @GetMapping("/topology/global")
    @Operation(summary = "获取全局拓扑")
    @ApiOperation("获取全局拓扑")
    public ReturnResult<SkywalkingTopology> getGlobalTopology(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step,
            @RequestParam(value = "layer", required = false) @Parameter(description = "层") @ApiParam("层") String layer) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            condition.setLayer(layer);
            SkywalkingTopology topology = skywalkingGraphQLService.getGlobalTopology(config, condition);
            return ReturnResult.ok(topology);
        } catch (Exception e) {
            log.error("获取全局拓扑失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取服务拓扑
     */
    @GetMapping("/topology/service/{serviceId}")
    @Operation(summary = "获取服务拓扑")
    @ApiOperation("获取服务拓扑")
    public ReturnResult<SkywalkingTopology> getServiceTopology(
            @PathVariable("serviceId") @Parameter(description = "服务ID") @ApiParam("服务ID") String serviceId,
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setServiceId(serviceId);
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            SkywalkingTopology topology = skywalkingGraphQLService.getServiceTopology(config, condition);
            return ReturnResult.ok(topology);
        } catch (Exception e) {
            log.error("获取服务拓扑失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }

    /**
     * 获取告警列表
     */
    @GetMapping("/alarms")
    @Operation(summary = "获取告警列表")
    @ApiOperation("获取告警列表")
    public ReturnResult<SkywalkingAlarm> getAlarms(
            @RequestParam("configId") @Parameter(description = "配置ID") @ApiParam("配置ID") Integer configId,
            @RequestParam("startTime") @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam("endTime") @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime,
            @RequestParam(value = "step", defaultValue = "MINUTE") @Parameter(description = "步长") @ApiParam("步长") String step,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "关键词") @ApiParam("关键词") String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") @Parameter(description = "页码") @ApiParam("页码") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") @Parameter(description = "每页大小") @ApiParam("每页大小") Integer pageSize) {
        try {
            MonitorSkywalkingConfig config = getConfig(configId);
            SkywalkingQueryCondition condition = new SkywalkingQueryCondition();
            condition.setStartTime(startTime);
            condition.setEndTime(endTime);
            condition.setStep(step);
            condition.setKeyword(keyword);
            condition.setPageNum(pageNum);
            condition.setPageSize(pageSize);
            SkywalkingAlarm alarms = skywalkingGraphQLService.getAlarms(config, condition);
            return ReturnResult.ok(alarms);
        } catch (Exception e) {
            log.error("获取告警列表失败: {}", e.getMessage(), e);
            return ReturnResult.illegal(e.getMessage());
        }
    }
}
