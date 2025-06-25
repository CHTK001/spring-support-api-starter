package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.ServerFileUploadRequest;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadTask;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskStatistics;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadTaskQueueStatus;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadQueueOptimizationResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadBatchResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器文件上传控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server/file-upload")
@RequiredArgsConstructor
@Api(tags = "服务器文件上传管理")
@Tag(name = "服务器文件上传管理", description = "服务器文件上传任务管理接口")
public class MonitorSysGenServerFileUploadController {

    private final MonitorSysGenServerFileUploadTaskService taskService;

    /**
     * 创建上传任务
     *
     * @param file    上传文件
     * @param request 上传请求
     * @return 创建结果
     */
    @PostMapping("/upload")
    @Operation(summary = "创建上传任务")
    @ApiOperation("创建上传任务")
    public ReturnResult<MonitorSysGenServerFileUploadTask> createUploadTask(
            @RequestParam("file") @Parameter(description = "上传文件") @ApiParam("上传文件") MultipartFile file,
            @ModelAttribute @Parameter(description = "上传请求") @ApiParam("上传请求") ServerFileUploadRequest request) {
        
        return taskService.createUploadTask(file, request);
    }

    /**
     * 分页查询上传任务
     *
     * @param current    当前页
     * @param size       页大小
     * @param serverId   服务器ID
     * @param taskName   任务名称
     * @param status     任务状态
     * @param mode       上传模式
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询上传任务")
    @ApiOperation("分页查询上传任务")
    public ReturnResult<IPage<MonitorSysGenServerFileUploadTask>> getTaskPage(
            @RequestParam(defaultValue = "1") @Parameter(description = "当前页") @ApiParam("当前页") Long current,
            @RequestParam(defaultValue = "10") @Parameter(description = "页大小") @ApiParam("页大小") Long size,
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(required = false) @Parameter(description = "任务名称") @ApiParam("任务名称") String taskName,
            @RequestParam(required = false) @Parameter(description = "任务状态") @ApiParam("任务状态") String status,
            @RequestParam(required = false) @Parameter(description = "上传模式") @ApiParam("上传模式") String mode,
            @RequestParam(required = false) @Parameter(description = "创建用户") @ApiParam("创建用户") String createUser,
            @RequestParam(required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<MonitorSysGenServerFileUploadTask> page = new Page<>(current, size);
        return taskService.getTaskPage(page, serverId, taskName, status, mode, createUser, startTime, endTime);
    }

    /**
     * 根据ID查询任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "根据ID查询任务")
    @ApiOperation("根据ID查询任务")
    public ReturnResult<MonitorSysGenServerFileUploadTask> getTaskById(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        
        return taskService.getTaskById(taskId);
    }

    /**
     * 根据服务器ID查询当前活跃任务
     *
     * @param serverId 服务器ID
     * @return 活跃任务
     */
    @GetMapping("/server/{serverId}/active")
    @Operation(summary = "查询服务器活跃任务")
    @ApiOperation("查询服务器活跃任务")
    public ReturnResult<MonitorSysGenServerFileUploadTask> getActiveTaskByServerId(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        
        return taskService.getActiveTaskByServerId(serverId);
    }

    /**
     * 启动上传任务
     *
     * @param taskId 任务ID
     * @return 启动结果
     */
    @PostMapping("/{taskId}/start")
    @Operation(summary = "启动上传任务")
    @ApiOperation("启动上传任务")
    public ReturnResult<Boolean> startUploadTask(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        
        return taskService.startUploadTask(taskId);
    }

    /**
     * 取消上传任务
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消上传任务")
    @ApiOperation("取消上传任务")
    public ReturnResult<Boolean> cancelUploadTask(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        
        return taskService.cancelUploadTask(taskId);
    }

    /**
     * 重试上传任务
     *
     * @param taskId 任务ID
     * @return 重试结果
     */
    @PostMapping("/{taskId}/retry")
    @Operation(summary = "重试上传任务")
    @ApiOperation("重试上传任务")
    public ReturnResult<Boolean> retryUploadTask(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        
        return taskService.retryUploadTask(taskId);
    }

    /**
     * 删除上传任务
     *
     * @param taskId 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除上传任务")
    @ApiOperation("删除上传任务")
    public ReturnResult<Boolean> deleteUploadTask(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        
        return taskService.deleteUploadTask(taskId);
    }

    /**
     * 根据状态查询任务
     *
     * @param status 任务状态
     * @return 任务列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询任务")
    @ApiOperation("根据状态查询任务")
    public ReturnResult<List<MonitorSysGenServerFileUploadTask>> getTasksByStatus(
            @PathVariable("status") @Parameter(description = "任务状态") @ApiParam("任务状态") String status) {
        
        return taskService.getTasksByStatus(status);
    }

    /**
     * 查询待处理的定时任务
     *
     * @return 定时任务列表
     */
    @GetMapping("/scheduled/pending")
    @Operation(summary = "查询待处理的定时任务")
    @ApiOperation("查询待处理的定时任务")
    public ReturnResult<List<MonitorSysGenServerFileUploadTask>> getPendingScheduledTasks() {
        return taskService.getPendingScheduledTasks();
    }

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试任务列表
     */
    @GetMapping("/retryable")
    @Operation(summary = "查询可重试的失败任务")
    @ApiOperation("查询可重试的失败任务")
    public ReturnResult<List<MonitorSysGenServerFileUploadTask>> getRetryableTasks() {
        return taskService.getRetryableTasks();
    }

    /**
     * 获取任务统计信息
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计信息")
    @ApiOperation("获取任务统计信息")
    public ReturnResult<MonitorSysGenServerFileUploadTaskStatistics> getTaskStatistics(
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(required = false) @Parameter(description = "开始时间") @ApiParam("开始时间")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @Parameter(description = "结束时间") @ApiParam("结束时间")
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        return taskService.getTaskStatistics(serverId, startTime, endTime);
    }

    /**
     * 验证服务器是否支持上传
     *
     * @param serverId 服务器ID
     * @return 验证结果
     */
    @GetMapping("/server/{serverId}/validate")
    @Operation(summary = "验证服务器是否支持上传")
    @ApiOperation("验证服务器是否支持上传")
    public ReturnResult<Boolean> validateServerForUpload(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        
        return taskService.validateServerForUpload(serverId);
    }

    /**
     * 获取任务队列状态
     *
     * @return 队列状态
     */
    @GetMapping("/queue/status")
    @Operation(summary = "获取任务队列状态")
    @ApiOperation("获取任务队列状态")
    public ReturnResult<MonitorSysGenServerFileUploadTaskQueueStatus> getTaskQueueStatus() {
        return taskService.getTaskQueueStatus();
    }

    /**
     * 优化任务队列
     *
     * @return 优化结果
     */
    @PostMapping("/queue/optimize")
    @Operation(summary = "优化任务队列")
    @ApiOperation("优化任务队列")
    public ReturnResult<MonitorSysGenServerFileUploadQueueOptimizationResult> optimizeTaskQueue() {
        return taskService.optimizeTaskQueue();
    }

    /**
     * 批量取消任务
     *
     * @param taskIds 任务ID列表
     * @return 取消结果
     */
    @PostMapping("/batch/cancel")
    @Operation(summary = "批量取消任务")
    @ApiOperation("批量取消任务")
    public ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchCancelTasks(
            @RequestBody @Parameter(description = "任务ID列表") @ApiParam("任务ID列表") List<Long> taskIds) {
        
        return taskService.batchCancelTasks(taskIds);
    }

    /**
     * 批量重试任务
     *
     * @param taskIds 任务ID列表
     * @return 重试结果
     */
    @PostMapping("/batch/retry")
    @Operation(summary = "批量重试任务")
    @ApiOperation("批量重试任务")
    public ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchRetryTasks(
            @RequestBody @Parameter(description = "任务ID列表") @ApiParam("任务ID列表") List<Long> taskIds) {
        
        return taskService.batchRetryTasks(taskIds);
    }
}
