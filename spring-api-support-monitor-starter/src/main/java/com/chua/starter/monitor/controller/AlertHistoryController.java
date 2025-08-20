package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertStatus;
import com.chua.starter.monitor.service.MonitorSysGenServerAlertHistoryService;
import com.chua.starter.mybatis.entity.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警历史管理控制器
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@RestController
@RequestMapping("/alert-history")
@Api(tags = "告警历史管理")
@RequiredArgsConstructor
@Validated
public class AlertHistoryController {

    private final MonitorSysGenServerAlertHistoryService alertHistoryService;

    /**
     * 分页查询告警历史
     */
    @ApiOperation("分页查询告警历史")
    @PostMapping("/page")
    public ReturnPageResult<MonitorSysGenServerAlertHistory> pageAlerts(
            @RequestBody @Valid Query<MonitorSysGenServerAlertHistory> query) {

        try {
            return alertHistoryService.pageAlerts(query);
        } catch (Exception e) {
            log.error("分页查询告警历史失败", e);
            return ReturnPageResult.error("分页查询告警历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警详情
     */
    @ApiOperation("获取告警详情")
    @GetMapping("/{alertId}")
    public ReturnResult<MonitorSysGenServerAlertHistory> getAlertDetail(
            @ApiParam("告警ID") @PathVariable @NotNull Long alertId) {

        try {
            return alertHistoryService.getAlertDetail(alertId);
        } catch (Exception e) {
            log.error("获取告警详情失败: alertId={}", alertId, e);
            return ReturnResult.error("获取告警详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新告警状态
     */
    @ApiOperation("更新告警状态")
    @PutMapping("/{alertId}/status")
    public ReturnResult<Boolean> updateAlertStatus(
            @ApiParam("告警ID") @PathVariable @NotNull Long alertId,
            @ApiParam("新状态") @RequestParam @NotNull AlertStatus status) {

        try {
            return alertHistoryService.updateAlertStatus(alertId, status);
        } catch (Exception e) {
            log.error("更新告警状态失败: alertId={}, status={}", alertId, status, e);
            return ReturnResult.error("更新告警状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新告警状态
     */
    @ApiOperation("批量更新告警状态")
    @PutMapping("/batch/status")
    public ReturnResult<Integer> batchUpdateAlertStatus(
            @ApiParam("告警ID列表") @RequestParam List<Long> alertIds,
            @ApiParam("新状态") @RequestParam @NotNull AlertStatus status) {

        try {
            return alertHistoryService.batchUpdateAlertStatus(alertIds, status);
        } catch (Exception e) {
            log.error("批量更新告警状态失败: alertIds={}, status={}", alertIds, status, e);
            return ReturnResult.error("批量更新告警状态失败: " + e.getMessage());
        }
    }

    /**
     * 恢复告警
     */
    @ApiOperation("恢复告警")
    @PutMapping("/{alertId}/recover")
    public ReturnResult<Boolean> recoverAlert(
            @ApiParam("告警ID") @PathVariable @NotNull Integer alertId) {

        try {
            return alertHistoryService.recoverAlert(alertId);
        } catch (Exception e) {
            log.error("恢复告警失败: alertId={}", alertId, e);
            return ReturnResult.error("恢复告警失败: " + e.getMessage());
        }
    }

    /**
     * 忽略告警
     */
    @ApiOperation("忽略告警")
    @PutMapping("/{alertId}/ignore")
    public ReturnResult<Boolean> ignoreAlert(
            @ApiParam("告警ID") @PathVariable @NotNull Long alertId,
            @ApiParam("忽略原因") @RequestParam(required = false) String reason) {

        try {
            return alertHistoryService.ignoreAlert(alertId, reason);
        } catch (Exception e) {
            log.error("忽略告警失败: alertId={}", alertId, e);
            return ReturnResult.error("忽略告警失败: " + e.getMessage());
        }
    }

    /**
     * 设置告警静默
     */
    @ApiOperation("设置告警静默")
    @PutMapping("/{alertId}/silence")
    public ReturnResult<Boolean> silenceAlert(
            @ApiParam("告警ID") @PathVariable @NotNull Integer alertId,
            @ApiParam("静默时长(分钟)") @RequestParam @NotNull Integer silenceMinutes) {

        try {
            return alertHistoryService.silenceAlert(alertId, silenceMinutes);
        } catch (Exception e) {
            log.error("设置告警静默失败: alertId={}, silenceMinutes={}", alertId, silenceMinutes, e);
            return ReturnResult.error("设置告警静默失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器活跃告警
     */
    @ApiOperation("获取服务器活跃告警")
    @GetMapping("/server/{serverId}/active")
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getActiveAlerts(
            @ApiParam("服务器ID") @PathVariable @NotNull Integer serverId) {

        try {
            return alertHistoryService.getActiveAlertsByServerId(serverId);
        } catch (Exception e) {
            log.error("获取服务器活跃告警失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器活跃告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警统计信息
     */
    @ApiOperation("获取告警统计信息")
    @GetMapping("/statistics")
    public ReturnResult<Map<String, Object>> getAlertStatistics(
            @ApiParam("服务器ID") @RequestParam(required = false) Integer serverId,
            @ApiParam("统计天数") @RequestParam(defaultValue = "7") Integer days) {

        try {
            return alertHistoryService.getAlertStatistics(serverId, days);
        } catch (Exception e) {
            log.error("获取告警统计信息失败: serverId={}, days={}", serverId, days, e);
            return ReturnResult.error("获取告警统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取告警趋势数据
     */
    @ApiOperation("获取告警趋势数据")
    @GetMapping("/trends")
    public ReturnResult<List<Map<String, Object>>> getAlertTrends(
            @ApiParam("服务器ID") @RequestParam(required = false) Integer serverId,
            @ApiParam("统计天数") @RequestParam(defaultValue = "7") Integer days) {

        try {
            // 暂时返回空数据，等待实现
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("获取告警趋势数据失败: serverId={}, days={}", serverId, days, e);
            return ReturnResult.error("获取告警趋势数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出告警记录
     */
    @ApiOperation("导出告警记录")
    @PostMapping("/export")
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> exportAlerts(
            @RequestBody @Valid Query<MonitorSysGenServerAlertHistory> query) {

        try {
            // 暂时返回空数据，等待实现
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("导出告警记录失败", e);
            return ReturnResult.error("导出告警记录失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除告警记录
     */
    @ApiOperation("批量删除告警记录")
    @DeleteMapping("/batch")
    public ReturnResult<Integer> batchDeleteAlerts(
            @ApiParam("告警ID列表") @RequestParam List<Integer> alertIds) {

        try {
            // 使用物理删除
            boolean result = alertHistoryService.removeByIds(alertIds);
            return ReturnResult.success(result ? alertIds.size() : 0);
        } catch (Exception e) {
            log.error("批量删除告警记录失败: alertIds={}", alertIds, e);
            return ReturnResult.error("批量删除告警记录失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期告警记录
     */
    @ApiOperation("清理过期告警记录")
    @DeleteMapping("/cleanup")
    public ReturnResult<Integer> cleanupExpiredAlerts(
            @ApiParam("过期天数") @RequestParam(defaultValue = "30") Integer expireDays) {

        try {
            return alertHistoryService.cleanupExpiredAlerts(expireDays);
        } catch (Exception e) {
            log.error("清理过期告警记录失败: expireDays={}", expireDays, e);
            return ReturnResult.error("清理过期告警记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取时间范围内的告警
     */
    @ApiOperation("获取时间范围内的告警")
    @GetMapping("/server/{serverId}/range")
    public ReturnResult<List<MonitorSysGenServerAlertHistory>> getAlertsByTimeRange(
            @ApiParam("服务器ID") @PathVariable @NotNull Integer serverId,
            @ApiParam("开始时间") @RequestParam @NotNull LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam @NotNull LocalDateTime endTime) {

        try {
            // 暂时返回空数据，等待实现
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("获取时间范围内的告警失败: serverId={}, startTime={}, endTime={}",
                    serverId, startTime, endTime, e);
            return ReturnResult.error("获取时间范围内的告警失败: " + e.getMessage());
        }
    }
}
