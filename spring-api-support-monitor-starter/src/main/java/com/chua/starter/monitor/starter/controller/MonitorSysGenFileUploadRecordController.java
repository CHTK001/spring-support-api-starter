package com.chua.starter.monitor.starter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadRecord;
import com.chua.starter.monitor.starter.service.MonitorSysGenFileUploadRecordService;
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
 * 文件上传记录管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("/v1/file-upload/records")
@Api(tags = "文件上传记录管理")
@Tag(name = "文件上传记录管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenFileUploadRecordController {

    private final MonitorSysGenFileUploadRecordService recordService;

    /**
     * 分页查询文件上传记录
     *
     * @param current   当前页
     * @param size      页大小
     * @param taskId    任务ID
     * @param serverId  服务器ID
     * @param status    上传状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询文件上传记录")
    @ApiOperation("分页查询文件上传记录")
    public ReturnResult<IPage<MonitorSysGenFileUploadRecord>> getRecordPage(
            @RequestParam(value = "current", defaultValue = "1") @Parameter(description = "当前页") @ApiParam("当前页") Long current,
            @RequestParam(value = "size", defaultValue = "20") @Parameter(description = "页大小") @ApiParam("页大小") Long size,
            @RequestParam(value = "taskId", required = false) @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId,
            @RequestParam(value = "serverId", required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "status", required = false) @Parameter(description = "上传状态") @ApiParam("上传状态") String status,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<MonitorSysGenFileUploadRecord> page = new Page<>(current, size);
        return recordService.getRecordPage(page, taskId, serverId, status, startTime, endTime);
    }

    /**
     * 根据任务ID查询上传记录
     *
     * @param taskId 任务ID
     * @return 上传记录列表
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "根据任务ID查询上传记录")
    @ApiOperation("根据任务ID查询上传记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByTaskId(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return recordService.getRecordsByTaskId(taskId);
    }

    /**
     * 根据服务器ID查询上传记录
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 上传记录列表
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID查询上传记录")
    @ApiOperation("根据服务器ID查询上传记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByServerId(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "limit", defaultValue = "50") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return recordService.getRecordsByServerId(serverId, limit);
    }

    /**
     * 根据状态查询上传记录
     *
     * @param status 上传状态
     * @return 上传记录列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询上传记录")
    @ApiOperation("根据状态查询上传记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getRecordsByStatus(
            @PathVariable("status") @Parameter(description = "上传状态") @ApiParam("上传状态") String status) {

        return recordService.getRecordsByStatus(status);
    }

    /**
     * 查询正在上传的记录
     *
     * @return 正在上传的记录列表
     */
    @GetMapping("/uploading")
    @Operation(summary = "查询正在上传的记录")
    @ApiOperation("查询正在上传的记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getUploadingRecords() {
        return recordService.getUploadingRecords();
    }

    /**
     * 查询失败的上传记录
     *
     * @param limit 限制数量
     * @return 失败的上传记录列表
     */
    @GetMapping("/failed")
    @Operation(summary = "查询失败的上传记录")
    @ApiOperation("查询失败的上传记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getFailedRecords(
            @RequestParam(value = "limit", defaultValue = "50") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return recordService.getFailedRecords(limit);
    }

    /**
     * 创建上传记录
     *
     * @param record 上传记录
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建上传记录")
    @ApiOperation("创建上传记录")
    public ReturnResult<MonitorSysGenFileUploadRecord> createUploadRecord(
            @RequestBody @Parameter(description = "上传记录") @ApiParam("上传记录") MonitorSysGenFileUploadRecord record) {

        return recordService.createUploadRecord(record);
    }

    /**
     * 批量创建上传记录
     *
     * @param records 上传记录列表
     * @return 创建结果
     */
    @PostMapping("/batch/create")
    @Operation(summary = "批量创建上传记录")
    @ApiOperation("批量创建上传记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> createBatchUploadRecords(
            @RequestBody @Parameter(description = "上传记录列表") @ApiParam("上传记录列表") List<MonitorSysGenFileUploadRecord> records) {

        return recordService.createBatchUploadRecords(records);
    }

    /**
     * 更新上传进度
     *
     * @param recordId         记录ID
     * @param progress         进度
     * @param transferredBytes 已传输字节数
     * @param speed            上传速度
     * @return 更新结果
     */
    @PutMapping("/{recordId}/progress")
    @Operation(summary = "更新上传进度")
    @ApiOperation("更新上传进度")
    public ReturnResult<Boolean> updateUploadProgress(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId,
            @RequestParam("progress") @Parameter(description = "进度") @ApiParam("进度") Integer progress,
            @RequestParam("transferredBytes") @Parameter(description = "已传输字节数") @ApiParam("已传输字节数") Long transferredBytes,
            @RequestParam("speed") @Parameter(description = "上传速度") @ApiParam("上传速度") Long speed) {

        return recordService.updateUploadProgress(recordId, progress, transferredBytes, speed);
    }

    /**
     * 更新上传状态
     *
     * @param recordId     记录ID
     * @param status       状态
     * @param errorMessage 错误信息
     * @param errorCode    错误代码
     * @return 更新结果
     */
    @PutMapping("/{recordId}/status")
    @Operation(summary = "更新上传状态")
    @ApiOperation("更新上传状态")
    public ReturnResult<Boolean> updateUploadStatus(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId,
            @RequestParam("status") @Parameter(description = "状态") @ApiParam("状态") String status,
            @RequestParam(value = "errorMessage", required = false) @Parameter(description = "错误信息") @ApiParam("错误信息") String errorMessage,
            @RequestParam(value = "errorCode", required = false) @Parameter(description = "错误代码") @ApiParam("错误代码") String errorCode) {

        LocalDateTime endTime = null;
        Long durationMs = null;
        
        // 如果是完成或失败状态，设置结束时间
        if ("COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status)) {
            endTime = LocalDateTime.now();
            // TODO: 计算实际耗时
        }

        return recordService.updateUploadStatus(recordId, status, errorMessage, errorCode, endTime, durationMs);
    }

    /**
     * 更新验证状态
     *
     * @param recordId           记录ID
     * @param verificationStatus 验证状态
     * @param remoteMd5          远程文件MD5
     * @return 更新结果
     */
    @PutMapping("/{recordId}/verification")
    @Operation(summary = "更新验证状态")
    @ApiOperation("更新验证状态")
    public ReturnResult<Boolean> updateVerificationStatus(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId,
            @RequestParam("verificationStatus") @Parameter(description = "验证状态") @ApiParam("验证状态") String verificationStatus,
            @RequestParam(value = "remoteMd5", required = false) @Parameter(description = "远程文件MD5") @ApiParam("远程文件MD5") String remoteMd5) {

        return recordService.updateVerificationStatus(recordId, verificationStatus, remoteMd5);
    }

    /**
     * 取消上传记录
     *
     * @param recordId 记录ID
     * @return 取消结果
     */
    @PostMapping("/{recordId}/cancel")
    @Operation(summary = "取消上传记录")
    @ApiOperation("取消上传记录")
    public ReturnResult<Boolean> cancelUploadRecord(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId) {

        return recordService.cancelUploadRecord(recordId);
    }

    /**
     * 重试上传记录
     *
     * @param recordId 记录ID
     * @return 重试结果
     */
    @PostMapping("/{recordId}/retry")
    @Operation(summary = "重试上传记录")
    @ApiOperation("重试上传记录")
    public ReturnResult<Boolean> retryUploadRecord(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId) {

        return recordService.retryUploadRecord(recordId);
    }

    /**
     * 验证文件完整性
     *
     * @param recordId 记录ID
     * @return 验证结果
     */
    @PostMapping("/{recordId}/verify")
    @Operation(summary = "验证文件完整性")
    @ApiOperation("验证文件完整性")
    public ReturnResult<Boolean> verifyFileIntegrity(
            @PathVariable("recordId") @Parameter(description = "记录ID") @ApiParam("记录ID") Long recordId) {

        return recordService.verifyFileIntegrity(recordId);
    }

    /**
     * 获取任务的上传统计
     *
     * @param taskId 任务ID
     * @return 上传统计
     */
    @GetMapping("/statistics/task/{taskId}")
    @Operation(summary = "获取任务的上传统计")
    @ApiOperation("获取任务的上传统计")
    public ReturnResult<Map<String, Object>> getTaskUploadStatistics(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return recordService.getTaskUploadStatistics(taskId);
    }

    /**
     * 获取服务器上传统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 服务器上传统计
     */
    @GetMapping("/statistics/server/{serverId}")
    @Operation(summary = "获取服务器上传统计")
    @ApiOperation("获取服务器上传统计")
    public ReturnResult<Map<String, Object>> getServerUploadStatistics(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
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

        return recordService.getServerUploadStatistics(serverId, startTime, endTime);
    }

    /**
     * 获取上传速度排行
     *
     * @param limit 限制数量
     * @return 上传速度排行
     */
    @GetMapping("/statistics/speed-ranking")
    @Operation(summary = "获取上传速度排行")
    @ApiOperation("获取上传速度排行")
    public ReturnResult<List<Map<String, Object>>> getUploadSpeedRanking(
            @RequestParam(value = "limit", defaultValue = "10") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return recordService.getUploadSpeedRanking(limit);
    }

    /**
     * 获取上传时间分布
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 上传时间分布
     */
    @GetMapping("/statistics/time-distribution")
    @Operation(summary = "获取上传时间分布")
    @ApiOperation("获取上传时间分布")
    public ReturnResult<List<Map<String, Object>>> getUploadTimeDistribution(
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return recordService.getUploadTimeDistribution(startTime, endTime);
    }

    /**
     * 查询长时间上传的记录
     *
     * @param hours 小时数
     * @return 长时间上传的记录列表
     */
    @GetMapping("/long-time-uploading")
    @Operation(summary = "查询长时间上传的记录")
    @ApiOperation("查询长时间上传的记录")
    public ReturnResult<List<MonitorSysGenFileUploadRecord>> getLongTimeUploadingRecords(
            @RequestParam(value = "hours", defaultValue = "2") @Parameter(description = "小时数") @ApiParam("小时数") Integer hours) {

        return recordService.getLongTimeUploadingRecords(hours);
    }

    /**
     * 删除过期记录
     *
     * @param days 过期天数
     * @return 删除结果
     */
    @DeleteMapping("/expired")
    @Operation(summary = "删除过期记录")
    @ApiOperation("删除过期记录")
    public ReturnResult<Integer> deleteExpiredRecords(
            @RequestParam(value = "days", defaultValue = "30") @Parameter(description = "过期天数") @ApiParam("过期天数") Integer days) {

        LocalDateTime expiredTime = LocalDateTime.now().minusDays(days);
        return recordService.deleteExpiredRecords(expiredTime);
    }

    /**
     * 获取上传进度详情
     *
     * @param taskId 任务ID
     * @return 上传进度详情
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取上传进度详情")
    @ApiOperation("获取上传进度详情")
    public ReturnResult<Map<String, Object>> getUploadProgressDetails(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return recordService.getUploadProgressDetails(taskId);
    }

    /**
     * 生成上传报告
     *
     * @param taskId 任务ID
     * @return 上传报告
     */
    @GetMapping("/report/{taskId}")
    @Operation(summary = "生成上传报告")
    @ApiOperation("生成上传报告")
    public ReturnResult<Map<String, Object>> generateUploadReport(
            @PathVariable("taskId") @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {

        return recordService.generateUploadReport(taskId);
    }
}
