package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.service.SystemSoftContainerService;
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
 * 系统软件容器管理控制器
 *
 * @author CH
 * @since 2025/01/15
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/system-soft-container")
@RequiredArgsConstructor
@Tag(name = "系统软件容器管理", description = "系统软件容器管理相关接口")
public class SystemSoftContainerController {

    private final SystemSoftContainerService systemSoftContainerService;

    @GetMapping("/list")
    @Operation(summary = "查询容器列表", description = "根据条件查询容器列表")
    public ReturnResult<List<SystemSoftContainer>> list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status,
            @Parameter(description = "容器名称") @RequestParam(required = false) String containerName) {
        return systemSoftContainerService.selectContainerList(serverId, softId, status, containerName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取容器详情", description = "根据容器ID获取容器详细信息")
    public ReturnResult<SystemSoftContainer> getById(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerDetails(id);
    }

    @PostMapping
    @Operation(summary = "新增容器记录", description = "新增容器记录信息")
    public ReturnResult<SystemSoftContainer> add(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        systemSoftContainer.setSystemSoftContainerId(null); // 确保是新增操作
        return systemSoftContainerService.saveOrUpdate(systemSoftContainer);
    }

    @PutMapping
    @Operation(summary = "更新容器记录", description = "更新容器记录信息")
    public ReturnResult<SystemSoftContainer> update(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        if (systemSoftContainer.getSystemSoftContainerId() == null) {
            return ReturnResult.error("更新操作必须提供容器ID");
        }
        return systemSoftContainerService.saveOrUpdate(systemSoftContainer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除容器记录", description = "删除容器记录（不删除实际容器）")
    public ReturnResult<Boolean> delete(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.removeById(id);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除容器记录", description = "批量删除容器记录")
    public ReturnResult<Boolean> deleteBatch(@Parameter(description = "容器ID列表") @RequestBody List<Integer> ids) {
        return systemSoftContainerService.removeByIds(ids);
    }

    @PostMapping("/create")
    @Operation(summary = "创建容器", description = "创建新的Docker容器")
    public ReturnResult<SystemSoftContainer> createContainer(
            @Parameter(description = "软件ID") @RequestParam Integer softId,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "容器名称") @RequestParam String containerName,
            @Parameter(description = "镜像标签") @RequestParam(defaultValue = "latest") String imageTag,
            @Parameter(description = "创建参数") @RequestBody(required = false) Map<String, Object> params) {
        return systemSoftContainerService.createContainer(softId, serverId, containerName, imageTag, params);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动容器", description = "启动指定的容器")
    public ReturnResult<Boolean> startContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.startContainer(id);
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "停止容器", description = "停止指定的容器")
    public ReturnResult<Boolean> stopContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.stopContainer(id);
    }

    @PostMapping("/{id}/restart")
    @Operation(summary = "重启容器", description = "重启指定的容器")
    public ReturnResult<Boolean> restartContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.restartContainer(id);
    }

    @DeleteMapping("/{id}/container")
    @Operation(summary = "删除容器", description = "从服务器中删除容器（物理删除）")
    public ReturnResult<Boolean> removeContainer(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftContainerService.removeContainer(id, force);
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "获取容器日志", description = "获取容器的运行日志")
    public ReturnResult<String> getContainerLogs(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {
        return systemSoftContainerService.getContainerLogs(id, lines);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取容器统计", description = "获取容器的运行统计信息")
    public ReturnResult<Map<String, Object>> getContainerStats(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerStats(id);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步容器状态", description = "同步指定服务器的容器状态")
    public ReturnResult<Integer> syncContainerStatus(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId) {
        return systemSoftContainerService.syncContainerStatus(serverId);
    }

    @PostMapping("/batch-operation")
    @Operation(summary = "批量操作容器", description = "批量执行容器操作")
    public ReturnResult<Map<String, Object>> batchOperateContainers(
            @Parameter(description = "容器ID列表") @RequestParam List<Integer> containerIds,
            @Parameter(description = "操作类型") @RequestParam String operation,
            @Parameter(description = "操作参数") @RequestBody(required = false) Map<String, Object> params) {
        return systemSoftContainerService.batchOperateContainers(containerIds, operation, params);
    }

    @GetMapping("/check-name")
    @Operation(summary = "检查容器名称", description = "检查容器名称是否已存在")
    public ReturnResult<Boolean> checkContainerNameExists(
            @Parameter(description = "容器名称") @RequestParam String containerName,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "排除的容器ID") @RequestParam(required = false) Integer excludeId) {
        return systemSoftContainerService.checkContainerNameExists(containerName, serverId, excludeId);
    }

    @GetMapping("/check-port")
    @Operation(summary = "检查端口占用", description = "检查端口是否被占用")
    public ReturnResult<Boolean> checkPortExists(
            @Parameter(description = "主机端口") @RequestParam Integer hostPort,
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "排除的容器ID") @RequestParam(required = false) Integer excludeId) {
        return systemSoftContainerService.checkPortExists(hostPort, serverId, excludeId);
    }

    @GetMapping("/software/{softId}")
    @Operation(summary = "按软件获取容器", description = "根据软件ID获取容器列表")
    public ReturnResult<List<SystemSoftContainer>> getContainersBySoftId(@Parameter(description = "软件ID") @PathVariable Integer softId) {
        return systemSoftContainerService.getContainersBySoftId(softId);
    }

    @GetMapping("/server/{serverId}")
    @Operation(summary = "按服务器获取容器", description = "根据服务器ID获取容器列表")
    public ReturnResult<List<SystemSoftContainer>> getContainersByServerId(@Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return systemSoftContainerService.getContainersByServerId(serverId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取容器统计", description = "获取容器状态统计信息")
    public ReturnResult<Map<String, Object>> getContainerStatusStats(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId) {
        return systemSoftContainerService.getContainerStatusStats(serverId);
    }

    @GetMapping("/running")
    @Operation(summary = "获取运行中容器", description = "获取运行中的容器列表")
    public ReturnResult<List<SystemSoftContainer>> getRunningContainers() {
        return systemSoftContainerService.getRunningContainers();
    }

    @GetMapping("/abnormal")
    @Operation(summary = "获取异常容器", description = "获取异常状态的容器列表")
    public ReturnResult<List<SystemSoftContainer>> getAbnormalContainers() {
        return systemSoftContainerService.getAbnormalContainers();
    }

    @PutMapping("/{id}/config")
    @Operation(summary = "更新容器配置", description = "更新容器的配置信息")
    public ReturnResult<Boolean> updateContainerConfig(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "新配置信息") @RequestBody Map<String, Object> config) {
        return systemSoftContainerService.updateContainerConfig(id, config);
    }
}