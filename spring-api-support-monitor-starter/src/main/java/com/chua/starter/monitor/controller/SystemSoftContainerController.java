package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftContainerStats;
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

    /**
     * 分页查询容器列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 分页容器列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询容器列表", description = "分页获取容器列表")
    public ReturnResult<IPage<SystemSoftContainer>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status,
            @Parameter(description = "容器名称") @RequestParam(required = false) String containerName) {

        try {
            log.debug("分页查询容器列表, page: {}, size: {}", page, size);

            IPage<SystemSoftContainer> pageResult = systemSoftContainerService.page(new Page<>(page, size));
            log.debug("分页查询容器列表成功, count: {}", pageResult.getRecords().size());
            return ReturnResult.ok(pageResult);

        } catch (Exception e) {
            log.error("分页查询容器列表失败", e);
            return ReturnResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件查询容器列表
     *
     * @param serverId      服务器ID
     * @param softId        软件ID
     * @param status        容器状态
     * @param containerName 容器名称
     * @return 容器列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询容器列表", description = "根据条件查询容器列表")
    public ReturnResult<List<SystemSoftContainer>> list(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "软件ID") @RequestParam(required = false) Integer softId,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status,
            @Parameter(description = "容器名称") @RequestParam(required = false) String containerName) {
        return systemSoftContainerService.selectContainerListForContainer(serverId, softId, status, containerName);
    }

    /**
     * 根据容器ID获取容器详细信息
     *
     * @param id 容器ID
     * @return 容器详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取容器详情", description = "根据容器ID获取容器详细信息")
    public ReturnResult<SystemSoftContainer> getById(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerDetails(id);
    }

    /**
     * 新增容器记录信息
     *
     * @param systemSoftContainer 容器信息
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增容器记录", description = "新增容器记录信息")
    public ReturnResult<Boolean> add(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        systemSoftContainer.setSystemSoftContainerId(null);
        boolean result = systemSoftContainerService.saveOrUpdate(systemSoftContainer);
        return ReturnResult.of(result, result ? "新增成功" : "新增失败");
    }

    /**
     * 更新容器记录信息
     *
     * @param systemSoftContainer 容器信息
     * @return 更新结果
     */
    @PutMapping
    @Operation(summary = "更新容器记录", description = "更新容器记录信息")
    public ReturnResult<Boolean> update(@Parameter(description = "容器信息") @RequestBody @Validated SystemSoftContainer systemSoftContainer) {
        if (systemSoftContainer.getSystemSoftContainerId() == null) {
            return ReturnResult.error("更新操作必须提供容器ID");
        }
        boolean result = systemSoftContainerService.saveOrUpdate(systemSoftContainer);
        return ReturnResult.of(result, result ? "更新成功" : "更新失败");
    }

    /**
     * 启动指定的容器
     *
     * @param id 容器ID
     * @return 启动结果
     */
    @PostMapping("/{id}/start")
    @Operation(summary = "启动容器", description = "启动指定的容器")
    public ReturnResult<Boolean> startContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.startContainerForContainer(id);
    }

    /**
     * 停止指定的容器
     *
     * @param id 容器ID
     * @return 停止结果
     */
    @PostMapping("/{id}/stop")
    @Operation(summary = "停止容器", description = "停止指定的容器")
    public ReturnResult<Boolean> stopContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.stopContainer(id);
    }

    /**
     * 重启指定的容器
     *
     * @param id 容器ID
     * @return 重启结果
     */
    @PostMapping("/{id}/restart")
    @Operation(summary = "重启容器", description = "重启指定的容器")
    public ReturnResult<Boolean> restartContainer(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.restartContainer(id);
    }

    /**
     * 从服务器中删除容器（物理删除）
     *
     * @param id    容器ID
     * @param force 是否强制删除
     * @return 删除结果
     */
    @DeleteMapping("/{id}/container")
    @Operation(summary = "删除容器", description = "从服务器中删除容器（物理删除）")
    public ReturnResult<Boolean> removeContainer(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {
        return systemSoftContainerService.removeContainer(id, force);
    }

    /**
     * 获取容器的运行日志
     *
     * @param id    容器ID
     * @param lines 日志行数
     * @return 容器日志
     */
    @GetMapping("/{id}/logs")
    @Operation(summary = "获取容器日志", description = "获取容器的运行日志")
    public ReturnResult<String> getContainerLogs(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {
        return systemSoftContainerService.getContainerLogs(id, lines);
    }

    /**
     * 启动容器日志的实时推送
     *
     * @param id    容器ID
     * @param lines 日志行数
     * @return 启动结果
     */
    @PostMapping("/{id}/logs/start")
    @Operation(summary = "启动容器日志推送", description = "启动容器日志的实时推送")
    public ReturnResult<Boolean> startContainerLog(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {
        return systemSoftContainerService.startContainerLog(id, lines);
    }

    /**
     * 停止容器日志的实时推送
     *
     * @param id 容器ID
     * @return 停止结果
     */
    @PostMapping("/{id}/logs/stop")
    @Operation(summary = "停止容器日志推送", description = "停止容器日志的实时推送")
    public ReturnResult<Boolean> stopContainerLog(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.stopContainerLog(id);
    }

    /**
     * 在指定容器中执行命令
     *
     * @param id      容器ID
     * @param command 要执行的命令
     * @return 执行结果
     */
    @PostMapping("/{id}/exec")
    @Operation(summary = "在容器中执行命令", description = "在指定容器中执行命令")
    public ReturnResult<String> execCommand(
            @Parameter(description = "容器ID") @PathVariable Integer id,
            @Parameter(description = "要执行的命令") @RequestParam String command) {
        return systemSoftContainerService.execCommand(id, command);
    }

    /**
     * 获取容器的运行统计信息
     *
     * @param id 容器ID
     * @return 容器统计信息
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "获取容器统计", description = "获取容器的运行统计信息")
    public ReturnResult<SystemSoftContainerStats> getContainerStats(@Parameter(description = "容器ID") @PathVariable Integer id) {
        return systemSoftContainerService.getContainerStats(id);
    }

    /**
     * 同步指定服务器的容器状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    @GetMapping("/sync")
    @Operation(summary = "同步容器状态", description = "同步指定服务器的容器状态")
    public ReturnResult<Integer> syncContainerStatus(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId) {
        return systemSoftContainerService.syncContainerStatus(serverId);
    }
}