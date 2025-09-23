package com.chua.starter.monitor.controller;

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
    public Object list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status,
            @Parameter(description = "容器名称") @RequestParam(required = false) String containerName) {
        return systemSoftContainerService.selectContainerList(serverId, softId, status, containerName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取容器详情", description = "根据容器ID获取容器详细信息")
    public Object getById(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerDetails(id);
    }

    @PostMapping
    @Operation(summary = "新增容器记录", description = "新增容器记录信息")
    public Object add(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        systemSoftContainer.setSystemSoftContainerId(null);
        return systemSoftContainerService.saveOrUpdate(systemSoftContainer);
    }

    @PutMapping
    @Operation(summary = "更新容器记录", description = "更新容器记录信息")
    public Object update(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        if (systemSoftContainer.getSystemSoftContainerId() == null) {
            return "更新操作必须提供容器ID";
        }
        return systemSoftContainerService.saveOrUpdate(systemSoftContainer);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动容器", description = "启动指定的容器")
    public Object startContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.startContainer(id);
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "停止容器", description = "停止指定的容器")
    public Object stopContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.stopContainer(id);
    }

    @PostMapping("/{id}/restart")
    @Operation(summary = "重启容器", description = "重启指定的容器")
    public Object restartContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.restartContainer(id);
    }

    @DeleteMapping("/{id}/container")
    @Operation(summary = "删除容器", description = "从服务器中删除容器（物理删除）")
    public Object removeContainer(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftContainerService.removeContainer(id, force);
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "获取容器日志", description = "获取容器的运行日志")
    public Object getContainerLogs(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {
        return systemSoftContainerService.getContainerLogs(id, lines);
    }

    @PostMapping("/{id}/logs/start")
    @Operation(summary = "启动容器日志推送", description = "启动容器日志的实时推送")
    public Object startContainerLog(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {
        return systemSoftContainerService.startContainerLog(id, lines);
    }

    @PostMapping("/{id}/logs/stop")
    @Operation(summary = "停止容器日志推送", description = "停止容器日志的实时推送")
    public Object stopContainerLog(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.stopContainerLog(id);
    }

    @PostMapping("/{id}/exec")
    @Operation(summary = "在容器中执行命令", description = "在指定容器中执行命令")
    public Object execCommand(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "要执行的命令") @RequestParam String command) {
        return systemSoftContainerService.execCommand(id, command);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "获取容器统计", description = "获取容器的运行统计信息")
    public Object getContainerStats(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerStats(id);
    }

    @GetMapping("/sync")
    @Operation(summary = "同步容器状态", description = "同步指定服务器的容器状态")
    public Object syncContainerStatus(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId) {
        return systemSoftContainerService.syncContainerStatus(serverId);
    }
}