package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.entity.MonitorSysGenServerSettingHistory;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingHistoryService;
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
 * 服务器配置变更历史Controller
 *
 * @author CH
 * @since 2024/12/27
 */
@Slf4j
@RestController
@RequestMapping("/v1/monitor/server/setting/history")
@RequiredArgsConstructor
@Tag(name = "服务器配置变更历史管理", description = "服务器配置变更历史相关接口")
public class MonitorSysGenServerSettingHistoryController {

    private final MonitorSysGenServerSettingHistoryService historyService;

    /**
     * 根据服务器ID查询历史记录
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID查询历史记录")
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerId(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") Integer limit) {
        return historyService.getHistoryByServerId(serverId, limit);
    }

    /**
     * 根据服务器ID和时间范围查询历史记录
     */
    @GetMapping("/server/{serverId}/range")
    @Operation(summary = "根据服务器ID和时间范围查询历史记录")
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByServerIdAndTimeRange(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return historyService.getHistoryByServerIdAndTimeRange(serverId, startTime, endTime);
    }

    /**
     * 根据变更类型查询历史记录
     */
    @GetMapping("/type/{changeType}")
    @Operation(summary = "根据变更类型查询历史记录")
    public ReturnResult<List<MonitorSysGenServerSettingHistory>> getHistoryByChangeType(
            @Parameter(description = "变更类型") @PathVariable String changeType,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "20") Integer limit) {
        return historyService.getHistoryByChangeType(changeType, limit);
    }

    /**
     * 获取历史记录统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取历史记录统计信息")
    public ReturnResult<Map<String, Object>> getHistoryStatistics(
            @Parameter(description = "服务器ID（可选）") @RequestParam(required = false) Integer serverId) {
        return historyService.getHistoryStatistics(serverId);
    }

    /**
     * 获取历史记录详情
     */
    @GetMapping("/{historyId}/detail")
    @Operation(summary = "获取历史记录详情")
    public ReturnResult<Map<String, Object>> getHistoryDetail(
            @Parameter(description = "历史记录ID") @PathVariable Long historyId) {
        return historyService.getHistoryDetail(historyId);
    }

    /**
     * 恢复历史配置
     */
    @PostMapping("/{historyId}/restore")
    @Operation(summary = "恢复历史配置")
    public ReturnResult<MonitorSysGenServerSetting> restoreFromHistory(
            @Parameter(description = "历史记录ID") @PathVariable Long historyId,
            @Parameter(description = "操作用户") @RequestParam(defaultValue = "system") String user) {
        return historyService.restoreFromHistory(historyId, user);
    }

    /**
     * 比较两个历史记录的差异
     */
    @GetMapping("/compare")
    @Operation(summary = "比较两个历史记录的差异")
    public ReturnResult<Map<String, Object>> compareHistory(
            @Parameter(description = "历史记录ID1") @RequestParam Long historyId1,
            @Parameter(description = "历史记录ID2") @RequestParam Long historyId2) {
        return historyService.compareHistory(historyId1, historyId2);
    }

    /**
     * 获取最近的配置变更
     */
    @GetMapping("/server/{serverId}/recent")
    @Operation(summary = "获取最近的配置变更")
    public ReturnResult<List<Map<String, Object>>> getRecentChanges(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        return historyService.getRecentChanges(serverId, limit);
    }

    /**
     * 获取配置变更摘要
     */
    @GetMapping("/server/{serverId}/summary")
    @Operation(summary = "获取配置变更摘要")
    public ReturnResult<Map<String, Object>> getChangeSummary(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return historyService.getChangeSummary(serverId, days);
    }

    /**
     * 获取配置变更趋势
     */
    @GetMapping("/server/{serverId}/trend")
    @Operation(summary = "获取配置变更趋势")
    public ReturnResult<List<Map<String, Object>>> getChangeTrend(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return historyService.getChangeTrend(serverId, days);
    }

    /**
     * 导出历史记录
     */
    @GetMapping("/server/{serverId}/export")
    @Operation(summary = "导出历史记录")
    public ReturnResult<String> exportHistory(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "json") String format) {
        return historyService.exportHistory(serverId, startTime, endTime, format);
    }

    /**
     * 删除指定时间之前的历史记录
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "删除指定时间之前的历史记录")
    public ReturnResult<Integer> deleteHistoryBeforeTime(
            @Parameter(description = "时间点") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        return historyService.deleteHistoryBeforeTime(beforeTime);
    }

    /**
     * 根据服务器ID删除历史记录
     */
    @DeleteMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID删除历史记录")
    public ReturnResult<Integer> deleteHistoryByServerId(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        return historyService.deleteHistoryByServerId(serverId);
    }

    /**
     * 记录配置变更历史
     */
    @PostMapping("/record")
    @Operation(summary = "记录配置变更历史")
    public ReturnResult<Boolean> recordHistory(
            @Parameter(description = "服务器ID") @RequestParam Integer serverId,
            @Parameter(description = "配置ID") @RequestParam Long settingId,
            @Parameter(description = "变更类型") @RequestParam String changeType,
            @Parameter(description = "变更描述") @RequestParam String description,
            @Parameter(description = "变更用户") @RequestParam(defaultValue = "system") String user,
            @Parameter(description = "配置快照") @RequestParam(required = false) String settingSnapshot,
            @Parameter(description = "变更字段") @RequestParam(required = false) String changedFields) {

        MonitorSysGenServerSettingHistory.ChangeType type;
        try {
            type = MonitorSysGenServerSettingHistory.ChangeType.valueOf(changeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReturnResult.error("无效的变更类型: " + changeType);
        }

        return historyService.recordHistory(serverId, settingId, type, description, user, settingSnapshot,
                changedFields);
    }

    /**
     * 检查配置是否有变更
     */
    @PostMapping("/server/{serverId}/check-changes")
    @Operation(summary = "检查配置是否有变更")
    public ReturnResult<Boolean> hasChanges(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId,
            @Parameter(description = "新配置") @RequestBody MonitorSysGenServerSetting newSetting) {
        return historyService.hasChanges(serverId, newSetting);
    }
}
