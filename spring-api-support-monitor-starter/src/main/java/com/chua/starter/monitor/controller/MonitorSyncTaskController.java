package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSyncConnection;
import com.chua.starter.monitor.entity.MonitorSyncNode;
import com.chua.starter.monitor.entity.MonitorSyncTask;
import com.chua.starter.monitor.entity.MonitorSyncTaskLog;
import com.chua.starter.monitor.pojo.sync.SyncTaskDesign;
import com.chua.starter.monitor.pojo.sync.SyncTaskQuery;
import com.chua.starter.monitor.pojo.sync.SyncTaskStatistics;
import com.chua.starter.monitor.service.sync.MonitorSyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步任务管理 Controller
 *
 * @author CH
 * @since 2024/12/19
 */
@RestController
@RequestMapping("/v1/sync/task")
@Tag(name = "同步任务管理")
@RequiredArgsConstructor
public class MonitorSyncTaskController {

    private final MonitorSyncTaskService taskService;

    /**
     * 分页查询任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询任务列表")
    public ReturnResult<Page<MonitorSyncTask>> list(SyncTaskQuery query) {
        return taskService.pageList(query);
    }

    /**
     * 创建任务
     */
    @PostMapping("/create")
    @Operation(summary = "创建任务")
    public ReturnResult<MonitorSyncTask> create(@RequestBody MonitorSyncTask task) {
        return taskService.createTask(task);
    }

    /**
     * 更新任务
     */
    @PutMapping("/update")
    @Operation(summary = "更新任务")
    public ReturnResult<Boolean> update(@RequestBody MonitorSyncTask task) {
        return taskService.updateTask(task);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/delete/{taskId}")
    @Operation(summary = "删除任务")
    public ReturnResult<Boolean> delete(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.deleteTask(taskId);
    }

    /**
     * 获取任务设计数据
     */
    @GetMapping("/design/{taskId}")
    @Operation(summary = "获取任务设计数据")
    public ReturnResult<SyncTaskDesign> getDesign(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.getTaskDesign(taskId);
    }

    /**
     * 保存任务设计
     */
    @PostMapping("/design/{taskId}")
    @Operation(summary = "保存任务设计")
    public ReturnResult<Boolean> saveDesign(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody SyncTaskDesign design) {
        return taskService.saveTaskDesign(taskId, design);
    }

    /**
     * 启动任务
     */
    @PostMapping("/start/{taskId}")
    @Operation(summary = "启动任务")
    public ReturnResult<Boolean> start(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.startTask(taskId);
    }

    /**
     * 停止任务
     */
    @PostMapping("/stop/{taskId}")
    @Operation(summary = "停止任务")
    public ReturnResult<Boolean> stop(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.stopTask(taskId);
    }

    /**
     * 手动执行一次
     */
    @PostMapping("/execute/{taskId}")
    @Operation(summary = "手动执行一次")
    public ReturnResult<Long> executeOnce(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.executeOnce(taskId);
    }

    /**
     * 获取任务执行日志
     */
    @GetMapping("/logs/{taskId}")
    @Operation(summary = "获取任务执行日志")
    public ReturnResult<Page<MonitorSyncTaskLog>> getLogs(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        return taskService.getTaskLogs(taskId, page, size);
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/log/{logId}")
    @Operation(summary = "获取日志详情")
    public ReturnResult<MonitorSyncTaskLog> getLogDetail(
            @Parameter(description = "日志ID") @PathVariable Long logId) {
        return taskService.getLogDetail(logId);
    }

    /**
     * 验证任务设计
     */
    @PostMapping("/validate")
    @Operation(summary = "验证任务设计")
    public ReturnResult<Boolean> validateDesign(@RequestBody SyncTaskDesign design) {
        return taskService.validateDesign(design);
    }

    /**
     * 复制任务
     */
    @PostMapping("/copy/{taskId}")
    @Operation(summary = "复制任务")
    public ReturnResult<MonitorSyncTask> copyTask(
            @Parameter(description = "原任务ID") @PathVariable Long taskId,
            @Parameter(description = "新任务名称") @RequestParam String newName) {
        return taskService.copyTask(taskId, newName);
    }

    /**
     * 获取任务节点列表
     */
    @GetMapping("/nodes/{taskId}")
    @Operation(summary = "获取任务节点列表")
    public ReturnResult<List<MonitorSyncNode>> getNodes(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.getTaskNodes(taskId);
    }

    /**
     * 获取任务连线列表
     */
    @GetMapping("/connections/{taskId}")
    @Operation(summary = "获取任务连线列表")
    public ReturnResult<List<MonitorSyncConnection>> getConnections(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        return taskService.getTaskConnections(taskId);
    }

    /**
     * 获取全局执行统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取全局执行统计")
    public ReturnResult<SyncTaskStatistics> getStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "统计粒度: hour/day") @RequestParam(required = false, defaultValue = "day") String granularity) {
        return taskService.getStatistics(startTime, endTime, granularity);
    }

    /**
     * 获取指定任务的执行统计
     */
    @GetMapping("/statistics/{taskId}")
    @Operation(summary = "获取指定任务的执行统计")
    public ReturnResult<SyncTaskStatistics> getTaskStatistics(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "统计粒度: hour/day") @RequestParam(required = false, defaultValue = "day") String granularity) {
        return taskService.getTaskStatistics(taskId, startTime, endTime, granularity);
    }
}
