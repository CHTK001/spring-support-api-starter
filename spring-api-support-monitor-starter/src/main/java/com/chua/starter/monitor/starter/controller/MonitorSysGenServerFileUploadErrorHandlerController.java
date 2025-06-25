package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadErrorHandlerService;
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
import java.util.Map;

/**
 * 服务器文件上传错误处理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server/file-upload/error")
@RequiredArgsConstructor
@Api(tags = "服务器文件上传错误处理")
@Tag(name = "服务器文件上传错误处理", description = "服务器文件上传错误处理相关接口")
public class MonitorSysGenServerFileUploadErrorHandlerController {

    private final MonitorSysGenServerFileUploadErrorHandlerService errorHandlerService;

    /**
     * 获取常见错误列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取常见错误列表")
    @ApiOperation("获取常见错误列表")
    public ReturnResult<List<Map<String, Object>>> getErrorList(
            @RequestParam(defaultValue = "20") @Parameter(description = "返回数量限制") @ApiParam("返回数量限制") Integer limit) {
        return errorHandlerService.getCommonErrors(limit);
    }

    /**
     * 诊断上传问题
     */
    @GetMapping("/{taskId}/diagnose")
    @Operation(summary = "诊断上传问题")
    @ApiOperation("诊断上传问题")
    public ReturnResult<Map<String, Object>> diagnoseUploadIssue(
            @PathVariable @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        return errorHandlerService.diagnoseUploadIssue(taskId);
    }

    /**
     * 自动修复常见问题
     */
    @PostMapping("/{taskId}/auto-fix")
    @Operation(summary = "自动修复常见问题")
    @ApiOperation("自动修复常见问题")
    public ReturnResult<Map<String, Object>> autoFixCommonIssues(
            @PathVariable @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        return errorHandlerService.autoFixCommonIssues(taskId);
    }

    /**
     * 执行重试策略
     */
    @PostMapping("/retry-strategy/{taskId}")
    @Operation(summary = "执行重试策略")
    @ApiOperation("执行重试策略")
    public ReturnResult<Map<String, Object>> executeRetryStrategy(
            @PathVariable @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        return errorHandlerService.executeRetryStrategy(taskId);
    }

    /**
     * 获取错误统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取错误统计")
    @ApiOperation("获取错误统计")
    public ReturnResult<Map<String, Object>> getErrorStatistics(
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(defaultValue = "7") @Parameter(description = "统计天数") @ApiParam("统计天数") Integer days) {
        return errorHandlerService.getErrorStatistics(serverId, days);
    }

    /**
     * 获取错误趋势分析
     */
    @GetMapping("/trend")
    @Operation(summary = "获取错误趋势分析")
    @ApiOperation("获取错误趋势分析")
    public ReturnResult<Map<String, Object>> getErrorTrendAnalysis(
            @RequestParam(defaultValue = "7") @Parameter(description = "分析天数") @ApiParam("分析天数") Integer days) {
        return errorHandlerService.getErrorTrendAnalysis(days);
    }

    /**
     * 获取错误模式分析
     */
    @GetMapping("/pattern-analysis")
    @Operation(summary = "获取错误模式分析")
    @ApiOperation("获取错误模式分析")
    public ReturnResult<Map<String, Object>> getErrorPatternAnalysis(
            @RequestParam(defaultValue = "7") @Parameter(description = "分析天数") @ApiParam("分析天数") Integer days) {
        return errorHandlerService.getErrorPatternAnalysis(days);
    }

    /**
     * 预测上传失败风险
     */
    @GetMapping("/{taskId}/predict-risk")
    @Operation(summary = "预测上传失败风险")
    @ApiOperation("预测上传失败风险")
    public ReturnResult<Map<String, Object>> predictUploadFailureRisk(
            @PathVariable @Parameter(description = "任务ID") @ApiParam("任务ID") Long taskId) {
        return errorHandlerService.predictUploadFailureRisk(taskId);
    }

    /**
     * 获取错误解决方案
     */
    @GetMapping("/solution/{errorCode}")
    @Operation(summary = "获取错误解决方案")
    @ApiOperation("获取错误解决方案")
    public ReturnResult<Map<String, Object>> getErrorSolution(
            @PathVariable @Parameter(description = "错误代码") @ApiParam("错误代码") String errorCode) {
        return errorHandlerService.getErrorSolution(errorCode);
    }

    /**
     * 监控系统健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "监控系统健康状态")
    @ApiOperation("监控系统健康状态")
    public ReturnResult<Map<String, Object>> monitorSystemHealth() {
        return errorHandlerService.monitorSystemHealth();
    }

    /**
     * 生成错误报告
     */
    @GetMapping("/report")
    @Operation(summary = "生成错误报告")
    @ApiOperation("生成错误报告")
    public ReturnResult<Map<String, Object>> generateErrorReport(
            @RequestParam @Parameter(description = "开始时间") @ApiParam("开始时间") String startTime,
            @RequestParam @Parameter(description = "结束时间") @ApiParam("结束时间") String endTime) {
        return errorHandlerService.generateErrorReport(startTime, endTime);
    }

    /**
     * 清理错误日志
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理错误日志")
    @ApiOperation("清理错误日志")
    public ReturnResult<Map<String, Object>> cleanupErrorLogs(
            @RequestParam(defaultValue = "30") @Parameter(description = "保留天数") @ApiParam("保留天数") Integer retentionDays) {
        return errorHandlerService.cleanupErrorLogs(retentionDays);
    }

    /**
     * 获取错误告警配置
     */
    @GetMapping("/alert-configs")
    @Operation(summary = "获取错误告警配置")
    @ApiOperation("获取错误告警配置")
    public ReturnResult<List<Map<String, Object>>> getErrorAlertConfigs() {
        return errorHandlerService.getErrorAlertConfigs();
    }

    /**
     * 检查错误告警
     */
    @GetMapping("/check-alerts")
    @Operation(summary = "检查错误告警")
    @ApiOperation("检查错误告警")
    public ReturnResult<List<Map<String, Object>>> checkErrorAlerts() {
        return errorHandlerService.checkErrorAlerts();
    }

    /**
     * 获取系统恢复建议
     */
    @GetMapping("/recovery-recommendations")
    @Operation(summary = "获取系统恢复建议")
    @ApiOperation("获取系统恢复建议")
    public ReturnResult<List<Map<String, Object>>> getSystemRecoveryRecommendations() {
        return errorHandlerService.getSystemRecoveryRecommendations();
    }

    /**
     * 执行系统自愈
     */
    @PostMapping("/self-healing")
    @Operation(summary = "执行系统自愈")
    @ApiOperation("执行系统自愈")
    public ReturnResult<Map<String, Object>> executeSelfHealing() {
        return errorHandlerService.executeSelfHealing();
    }

    /**
     * 优化错误处理策略
     */
    @PostMapping("/optimize-strategy")
    @Operation(summary = "优化错误处理策略")
    @ApiOperation("优化错误处理策略")
    public ReturnResult<Map<String, Object>> optimizeErrorHandlingStrategy() {
        return errorHandlerService.optimizeErrorHandlingStrategy();
    }

    /**
     * 测试错误处理机制
     */
    @PostMapping("/test-mechanism")
    @Operation(summary = "测试错误处理机制")
    @ApiOperation("测试错误处理机制")
    public ReturnResult<Map<String, Object>> testErrorHandlingMechanism(
            @RequestParam @Parameter(description = "错误类型") @ApiParam("错误类型") String errorType) {
        return errorHandlerService.testErrorHandlingMechanism(errorType);
    }
}
