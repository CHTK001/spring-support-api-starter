package com.chua.starter.monitor.controller.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerLog;
import com.chua.starter.monitor.service.server.MonitorSysGenServerLogService;
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
 * 服务器日志管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("/v1/server/logs")
@Api(tags = "服务器日志管理")
@Tag(name = "服务器日志管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerLogController {

    private final MonitorSysGenServerLogService logService;

    /**
     * 分页查询服务器日志
     *
     * @param current   当前页
     * @param size      页大小
     * @param serverId  服务器ID
     * @param level     日志级别
     * @param source    日志来源
     * @param keyword   关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询服务器日志")
    @ApiOperation("分页查询服务器日志")
    public ReturnResult<IPage<MonitorSysGenServerLog>> getLogPage(
            @RequestParam(value = "current", defaultValue = "1") @Parameter(description = "当前页") @ApiParam("当前页") Long current,
            @RequestParam(value = "size", defaultValue = "20") @Parameter(description = "页大小") @ApiParam("页大小") Long size,
            @RequestParam(value = "serverId", required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "level", required = false) @Parameter(description = "日志级别") @ApiParam("日志级别") String level,
            @RequestParam(value = "source", required = false) @Parameter(description = "日志来源") @ApiParam("日志来源") String source,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "关键词") @ApiParam("关键词") String keyword,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<MonitorSysGenServerLog> page = new Page<>(current, size);
        return logService.getLogPage(page, serverId, level, source, keyword, startTime, endTime);
    }

    /**
     * 根据服务器ID和时间范围查询日志
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID和时间范围查询日志")
    @ApiOperation("根据服务器ID和时间范围查询日志")
    public ReturnResult<List<MonitorSysGenServerLog>> getLogsByServerAndTime(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getLogsByServerAndTime(serverId, startTime, endTime);
    }

    /**
     * 获取最近的错误日志
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 错误日志列表
     */
    @GetMapping("/server/{serverId}/errors")
    @Operation(summary = "获取最近的错误日志")
    @ApiOperation("获取最近的错误日志")
    public ReturnResult<List<MonitorSysGenServerLog>> getRecentErrorLogs(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "limit", defaultValue = "50") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        return logService.getRecentErrorLogs(serverId, limit);
    }

    /**
     * 获取实时日志
     *
     * @param serverId 服务器ID
     * @param lastTime 最后时间
     * @param limit    限制数量
     * @return 实时日志列表
     */
    @GetMapping("/server/{serverId}/realtime")
    @Operation(summary = "获取实时日志")
    @ApiOperation("获取实时日志")
    public ReturnResult<List<MonitorSysGenServerLog>> getRealTimeLogs(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "lastTime", required = false) @Parameter(description = "最后时间") @ApiParam("最后时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastTime,
            @RequestParam(value = "limit", defaultValue = "100") @Parameter(description = "限制数量") @ApiParam("限制数量") Integer limit) {

        if (lastTime == null) {
            lastTime = LocalDateTime.now().minusMinutes(5);
        }

        return logService.getRealTimeLogs(serverId, lastTime, limit);
    }

    /**
     * 搜索日志内容
     *
     * @param current   当前页
     * @param size      页大小
     * @param serverId  服务器ID
     * @param keyword   关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索日志内容")
    @ApiOperation("搜索日志内容")
    public ReturnResult<IPage<MonitorSysGenServerLog>> searchLogContent(
            @RequestParam(value = "current", defaultValue = "1") @Parameter(description = "当前页") @ApiParam("当前页") Long current,
            @RequestParam(value = "size", defaultValue = "20") @Parameter(description = "页大小") @ApiParam("页大小") Long size,
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam("keyword") @Parameter(description = "关键词") @ApiParam("关键词") String keyword,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Page<MonitorSysGenServerLog> page = new Page<>(current, size);
        return logService.searchLogContent(page, serverId, keyword, startTime, endTime);
    }

    /**
     * 批量保存日志
     *
     * @param logs 日志列表
     * @return 保存结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量保存日志")
    @ApiOperation("批量保存日志")
    public ReturnResult<Boolean> batchSaveLogs(
            @RequestBody @Parameter(description = "日志列表") @ApiParam("日志列表") List<MonitorSysGenServerLog> logs) {

        return logService.batchSaveLogs(logs);
    }

    /**
     * 删除过期日志
     *
     * @param serverId    服务器ID
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    @DeleteMapping("/server/{serverId}/expired")
    @Operation(summary = "删除过期日志")
    @ApiOperation("删除过期日志")
    public ReturnResult<Integer> deleteExpiredLogs(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam("expiredTime") @Parameter(description = "过期时间") @ApiParam("过期时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expiredTime) {

        return logService.deleteExpiredLogs(serverId, expiredTime);
    }

    /**
     * 批量删除过期日志
     *
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    @DeleteMapping("/expired")
    @Operation(summary = "批量删除过期日志")
    @ApiOperation("批量删除过期日志")
    public ReturnResult<Integer> batchDeleteExpiredLogs(
            @RequestParam("expiredTime") @Parameter(description = "过期时间") @ApiParam("过期时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expiredTime) {

        return logService.batchDeleteExpiredLogs(expiredTime);
    }

    /**
     * 获取服务器日志统计信息（简化版本，用于对话框显示）
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取服务器日志统计信息")
    @ApiOperation("获取服务器日志统计信息")
    public ReturnResult<Map<String, Object>> getServerLogStatistics(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        log.info("获取服务器日志统计信息: serverId={}", serverId);
        return logService.getServerLogStatistics(serverId);
    }

    /**
     * 获取日志级别统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics/level")
    @Operation(summary = "获取日志级别统计")
    @ApiOperation("获取日志级别统计")
    public ReturnResult<Map<String, Object>> getLogLevelStatistics(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getLogLevelStatistics(serverId, startTime, endTime);
    }

    /**
     * 获取日志来源统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics/source")
    @Operation(summary = "获取日志来源统计")
    @ApiOperation("获取日志来源统计")
    public ReturnResult<Map<String, Object>> getLogSourceStatistics(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getLogSourceStatistics(serverId, startTime, endTime);
    }

    /**
     * 获取日志时间分布统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间分布统计
     */
    @GetMapping("/statistics/time-distribution")
    @Operation(summary = "获取日志时间分布统计")
    @ApiOperation("获取日志时间分布统计")
    public ReturnResult<List<Map<String, Object>>> getLogTimeDistribution(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getLogTimeDistribution(serverId, startTime, endTime);
    }

    /**
     * 获取异常日志统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 异常统计
     */
    @GetMapping("/statistics/exception")
    @Operation(summary = "获取异常日志统计")
    @ApiOperation("获取异常日志统计")
    public ReturnResult<Map<String, Object>> getExceptionStatistics(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getExceptionStatistics(serverId, startTime, endTime);
    }

    /**
     * 获取日志大小统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志大小统计
     */
    @GetMapping("/statistics/size")
    @Operation(summary = "获取日志大小统计")
    @ApiOperation("获取日志大小统计")
    public ReturnResult<Map<String, Object>> getLogSizeStatistics(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.getLogSizeStatistics(serverId, startTime, endTime);
    }

    /**
     * 生成日志报告
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志报告
     */
    @GetMapping("/report")
    @Operation(summary = "生成日志报告")
    @ApiOperation("生成日志报告")
    public ReturnResult<Map<String, Object>> generateLogReport(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "startTime", required = false) @Parameter(description = "开始时间") @ApiParam("开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @Parameter(description = "结束时间") @ApiParam("结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return logService.generateLogReport(serverId, startTime, endTime);
    }

    /**
     * 启动日志收集
     *
     * @param serverId 服务器ID
     * @return 启动结果
     */
    @PostMapping("/collection/{serverId}/start")
    @Operation(summary = "启动日志收集")
    @ApiOperation("启动日志收集")
    public ReturnResult<Boolean> startLogCollection(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return logService.startLogCollection(serverId);
    }

    /**
     * 停止日志收集
     *
     * @param serverId 服务器ID
     * @return 停止结果
     */
    @PostMapping("/collection/{serverId}/stop")
    @Operation(summary = "停止日志收集")
    @ApiOperation("停止日志收集")
    public ReturnResult<Boolean> stopLogCollection(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return logService.stopLogCollection(serverId);
    }

    /**
     * 重启日志收集
     *
     * @param serverId 服务器ID
     * @return 重启结果
     */
    @PostMapping("/collection/{serverId}/restart")
    @Operation(summary = "重启日志收集")
    @ApiOperation("重启日志收集")
    public ReturnResult<Boolean> restartLogCollection(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return logService.restartLogCollection(serverId);
    }

    /**
     * 获取日志收集状态
     *
     * @param serverId 服务器ID
     * @return 收集状态
     */
    @GetMapping("/collection/{serverId}/status")
    @Operation(summary = "获取日志收集状态")
    @ApiOperation("获取日志收集状态")
    public ReturnResult<Map<String, Object>> getLogCollectionStatus(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return logService.getLogCollectionStatus(serverId);
    }

    /**
     * 清理服务器日志
     *
     * @param serverId   服务器ID
     * @param beforeDate 清理此日期之前的日志
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理服务器日志")
    @ApiOperation("清理服务器日志")
    public ReturnResult<Integer> cleanupServerLogs(
            @RequestParam("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(value = "beforeDate", required = false) @Parameter(description = "清理此日期之前的日志") @ApiParam("清理此日期之前的日志") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeDate) {

        log.info("清理服务器日志: serverId={}, beforeDate={}", serverId, beforeDate);
        return logService.cleanupServerLogs(serverId, beforeDate);
    }
}
