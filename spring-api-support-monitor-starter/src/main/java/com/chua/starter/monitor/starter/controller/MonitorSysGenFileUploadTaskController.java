package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;
import com.chua.starter.monitor.starter.service.MonitorSysGenFileUploadTaskService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件上传任务管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("/v1/file-upload/tasks")
@Api(tags = "文件上传任务管理")
@Tag(name = "文件上传任务管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenFileUploadTaskController {

    private final MonitorSysGenFileUploadTaskService taskService;

    /**
     * 分页查询文件上传任务
     *
     * @param current    当前页
     * @param size       页大小
     * @param taskName   任务名称
     * @param status     任务状态
     * @param mode       上传模式
     * @param type       上传类型
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询文件上传任务")
    @ApiOperation("分页查询文件上传任务")
    public ReturnResult<IPage<MonitorSysGenFileUploadTask>> getTaskPage(
            @RequestParam(value = "current", defaultValue = "1") @Parameter(description = "当前页") @ApiParam("当前页") Long current,
            @RequestParam(value = "size", defaultValue = "20") @Parameter(description = "页大小") @ApiParam("页大小") Long size,
            @RequestParam(value = "taskName", required = false) @Parameter(description = "任务名称") @ApiParam("任务名称") String taskName,
            @RequestParam(value = "status", required = false) @Parameter(description = "任务状态") @ApiParam("任务状态") String status,
            @RequestParam(value = "mode", required = false) @Parameter(description = "上传模式") @ApiParam("上传模式") String mode,
            @RequestParam(value = "type", required = false) @Parameter(description = "上传类型") @ApiParam("上传类型") String type,
            @RequestParam(value = "createUser", required = false) @Parameter(description = "创建用户") @ApiParam("创建用户") String createUser,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<MonitorSysGenFileUploadTask> page = new Page<>(current, size);
        return taskService.getTaskPage(page, taskName, status, mode, type, createUser, startTime, endTime);
    }

    /**
     * 创建文件上传任务
     *
     * @param task 上传任务
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建文件上传任务")
    @ApiOperation("创建文件上传任务")
    public ReturnResult<MonitorSysGenFileUploadTask> createUploadTask(
            @RequestBody @Parameter(description = "上传任务") @ApiParam("上传任务") MonitorSysGenFileUploadTask task) {

        return taskService.createUploadTask(task);
    }

    /**
     * 批量创建文件上传任务
     *
     * @param tasks 上传任务列表
     * @return 创建结果
     */
    @PostMapping("/batch/create")
    @Operation(summary = "批量创建文件上传任务")
    @ApiOperation("批量创建文件上传任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> createBatchUploadTasks(
            @RequestBody @Parameter(description = "上传任务列表") @ApiParam("上传任务列表") List<MonitorSysGenFileUploadTask> tasks) {

        return taskService.createBatchUploadTasks(tasks);
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
     * 暂停上传任务
     *
     * @param taskId 任务ID
     * @return 暂停结果
     */
    @PostMapping("/{taskId}/pause")
    @Operation(summary = "暂停上传任务")
    @ApiOperation("暂停上传任务")
    public ReturnResult<Boolean> pauseUploadTask(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return taskService.pauseUploadTask(taskId);
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
     * 批量操作任务
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch/operate")
    @Operation(summary = "批量操作任务")
    @ApiOperation("批量操作任务")
    public ReturnResult<Map<String, Object>> batchOperateTasks(
            @RequestBody @Parameter(description = "批量操作请求") @ApiParam("批量操作请求") BatchOperateRequest request) {

        return taskService.batchOperateTasks(request.getTaskIds(), request.getOperation());
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
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByStatus(
            @PathVariable("status") @Parameter(description = "任务状态") @ApiParam("任务状态") String status) {

        return taskService.getTasksByStatus(status);
    }

    /**
     * 查询待处理的定时任务
     *
     * @return 待处理的定时任务列表
     */
    @GetMapping("/scheduled/pending")
    @Operation(summary = "查询待处理的定时任务")
    @ApiOperation("查询待处理的定时任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getPendingScheduledTasks() {
        return taskService.getPendingScheduledTasks();
    }

    /**
     * 查询可重试的失败任务
     *
     * @return 可重试的失败任务列表
     */
    @GetMapping("/retryable")
    @Operation(summary = "查询可重试的失败任务")
    @ApiOperation("查询可重试的失败任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getRetryableTasks() {
        return taskService.getRetryableTasks();
    }

    /**
     * 查询超时的处理中任务
     *
     * @param timeoutMinutes 超时分钟数
     * @return 超时的处理中任务列表
     */
    @GetMapping("/timeout")
    @Operation(summary = "查询超时的处理中任务")
    @ApiOperation("查询超时的处理中任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTimeoutTasks(
            @RequestParam(value = "timeoutMinutes", defaultValue = "30") @Parameter(description = "超时分钟数") @ApiParam("超时分钟数") Integer timeoutMinutes) {

        return taskService.getTimeoutTasks(timeoutMinutes);
    }

    /**
     * 获取任务统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计信息")
    @ApiOperation("获取任务统计信息")
    public ReturnResult<Map<String, Object>> getTaskStatistics(
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return taskService.getTaskStatistics(startTime, endTime);
    }

    /**
     * 根据用户查询任务统计
     *
     * @param createUser 创建用户
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 用户任务统计
     */
    @GetMapping("/statistics/user")
    @Operation(summary = "根据用户查询任务统计")
    @ApiOperation("根据用户查询任务统计")
    public ReturnResult<Map<String, Object>> getUserTaskStatistics(
            @RequestParam("createUser") @Parameter(description = "创建用户") @ApiParam("创建用户") String createUser,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return taskService.getUserTaskStatistics(createUser, startTime, endTime);
    }

    /**
     * 查询热门文件类型
     *
     * @param limit 限制数量
     * @return 热门文件类型统计
     */
    @GetMapping("/statistics/file-types")
    @Operation(summary = "查询热门文件类型")
    @ApiOperation("查询热门文件类型")
    public ReturnResult<List<Map<String, Object>>> getPopularFileTypes(
            @RequestParam(value = "limit", defaultValue = "10") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return taskService.getPopularFileTypes(limit);
    }

    /**
     * 查询大文件任务
     *
     * @param minSizeMB 最小文件大小(MB)
     * @param limit     限制数量
     * @return 大文件任务列表
     */
    @GetMapping("/large-files")
    @Operation(summary = "查询大文件任务")
    @ApiOperation("查询大文件任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getLargeFileTasks(
            @RequestParam(value = "minSizeMB", defaultValue = "100") @Parameter(description = "最小文件大小(MB)") @ApiParam("最小文件大小(MB)") Integer minSizeMB,
            @RequestParam(value = "limit", defaultValue = "20") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return taskService.getLargeFileTasks(minSizeMB, limit);
    }

    /**
     * 查询最近的任务
     *
     * @param limit 限制数量
     * @return 最近的任务列表
     */
    @GetMapping("/recent")
    @Operation(summary = "查询最近的任务")
    @ApiOperation("查询最近的任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getRecentTasks(
            @RequestParam(value = "limit", defaultValue = "20") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return taskService.getRecentTasks(limit);
    }

    /**
     * 根据文件MD5查询任务
     *
     * @param fileMd5 文件MD5
     * @return 任务列表
     */
    @GetMapping("/md5/{fileMd5}")
    @Operation(summary = "根据文件MD5查询任务")
    @ApiOperation("根据文件MD5查询任务")
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByFileMd5(
            @PathVariable("fileMd5") @Parameter(description = "文件MD5") @ApiParam("文件MD5") String fileMd5) {

        return taskService.getTasksByFileMd5(fileMd5);
    }

    /**
     * 查询队列中的任务数量
     *
     * @return 队列中的任务数量
     */
    @GetMapping("/queue/count")
    @Operation(summary = "查询队列中的任务数量")
    @ApiOperation("查询队列中的任务数量")
    public ReturnResult<Long> getQueueTaskCount() {
        return taskService.getQueueTaskCount();
    }

    /**
     * 查询今日任务统计
     *
     * @return 今日任务统计
     */
    @GetMapping("/statistics/today")
    @Operation(summary = "查询今日任务统计")
    @ApiOperation("查询今日任务统计")
    public ReturnResult<Map<String, Object>> getTodayTaskStatistics() {
        return taskService.getTodayTaskStatistics();
    }

    /**
     * 验证任务参数
     *
     * @param task 上传任务
     * @return 验证结果
     */
    @PostMapping("/validate")
    @Operation(summary = "验证任务参数")
    @ApiOperation("验证任务参数")
    public ReturnResult<Map<String, Object>> validateTask(
            @RequestBody @Parameter(description = "上传任务") @ApiParam("上传任务") MonitorSysGenFileUploadTask task) {

        return taskService.validateTask(task);
    }

    /**
     * 复制任务
     *
     * @param taskId 源任务ID
     * @return 复制结果
     */
    @PostMapping("/{taskId}/duplicate")
    @Operation(summary = "复制任务")
    @ApiOperation("复制任务")
    public ReturnResult<MonitorSysGenFileUploadTask> duplicateTask(
            @PathVariable("taskId") @Parameter(description = "源任务ID") @ApiParam("源任务ID") Long taskId) {

        return taskService.duplicateTask(taskId);
    }

    /**
     * 获取任务执行报告
     *
     * @param taskId 任务ID
     * @return 执行报告
     */
    @GetMapping("/{taskId}/report")
    @Operation(summary = "获取任务执行报告")
    @ApiOperation("获取任务执行报告")
    public ReturnResult<Map<String, Object>> getTaskExecutionReport(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return taskService.getTaskExecutionReport(taskId);
    }

    /**
     * 获取任务队列状态
     *
     * @return 队列状态
     */
    @GetMapping("/queue/status")
    @Operation(summary = "获取任务队列状态")
    @ApiOperation("获取任务队列状态")
    public ReturnResult<Map<String, Object>> getTaskQueueStatus() {
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
    public ReturnResult<Map<String, Object>> optimizeTaskQueue() {
        return taskService.optimizeTaskQueue();
    }

    /**
     * 删除过期任务
     *
     * @param days 过期天数
     * @return 删除结果
     */
    @DeleteMapping("/expired")
    @Operation(summary = "删除过期任务")
    @ApiOperation("删除过期任务")
    public ReturnResult<Integer> deleteExpiredTasks(
            @RequestParam(value = "days", defaultValue = "30") @Parameter(description = "过期天数") @ApiParam("过期天数") Integer days) {

        LocalDateTime expiredTime = LocalDateTime.now().minusDays(days);
        return taskService.deleteExpiredTasks(expiredTime);
    }

    /**
     * 批量操作请求对象
     */
    public static class BatchOperateRequest {
        private List<Long> taskIds;
        private String operation;

        public List<Long> getTaskIds() {
            return taskIds;
        }

        public void setTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }
    }
}
