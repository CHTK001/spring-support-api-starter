package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.ServerLatencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 服务器延迟控制器
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@RestController
@RequestMapping("server/latency")
@RequiredArgsConstructor
@Tag(name = "服务器延迟管理", description = "服务器延迟数据查询和管理接口")
public class ServerLatencyController {

    private final ServerLatencyService serverLatencyService;

    /**
     * 获取服务器当前延迟
     *
     * @param serverId 服务器ID
     * @return 延迟数据
     */
    @GetMapping("/{serverId}")
    @Operation(summary = "获取服务器当前延迟", description = "获取指定服务器的当前延迟时间")
    public ReturnResult<Long> getServerLatency(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId) {
        try {
            return serverLatencyService.getServerLatencyFromRedis(serverId);
        } catch (Exception e) {
            log.error("获取服务器延迟失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器延迟失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取服务器延迟
     *
     * @param serverIds 服务器ID列表
     * @return 延迟数据映射
     */
    @PostMapping("/batch")
    @Operation(summary = "批量获取服务器延迟", description = "批量获取多个服务器的当前延迟时间")
    public ReturnResult<Map<Integer, Long>> getBatchServerLatency(
            @Parameter(description = "服务器ID列表", required = true)
            @RequestBody List<Integer> serverIds) {
        try {
            return serverLatencyService.getBatchServerLatency(serverIds);
        } catch (Exception e) {
            log.error("批量获取服务器延迟失败", e);
            return ReturnResult.error("批量获取服务器延迟失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器延迟历史数据
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @param endTime   结束时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @return 历史延迟数据
     */
    @GetMapping("/{serverId}/history")
    @Operation(summary = "获取服务器延迟历史", description = "获取指定时间范围内的服务器延迟历史数据")
    public ReturnResult<List<Map<String, Object>>> getServerLatencyHistory(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @Parameter(description = "开始时间", required = true, example = "2024-12-26 00:00:00")
            @RequestParam String startTime,
            @Parameter(description = "结束时间", required = true, example = "2024-12-26 23:59:59")
            @RequestParam String endTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);

            return serverLatencyService.getServerLatencyHistory(serverId, start, end);
        } catch (Exception e) {
            log.error("获取服务器延迟历史失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器延迟历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取延迟统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取延迟统计信息", description = "获取所有服务器的延迟统计信息")
    public ReturnResult<Map<String, Object>> getLatencyStatistics() {
        try {
            return serverLatencyService.getLatencyStatistics();
        } catch (Exception e) {
            log.error("获取延迟统计信息失败", e);
            return ReturnResult.error("获取延迟统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查延迟告警
     *
     * @return 告警信息
     */
    @GetMapping("/alerts")
    @Operation(summary = "检查延迟告警", description = "检查当前存在延迟异常的服务器")
    public ReturnResult<List<Map<String, Object>>> checkLatencyAlerts() {
        try {
            return serverLatencyService.checkLatencyAlerts();
        } catch (Exception e) {
            log.error("检查延迟告警失败", e);
            return ReturnResult.error("检查延迟告警失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发延迟数据保存
     *
     * @return 保存结果
     */
    @PostMapping("/save")
    @Operation(summary = "手动保存延迟数据", description = "手动触发将Redis中的延迟数据保存到数据库")
    public ReturnResult<Integer> saveLatencyToDatabase() {
        try {
            return serverLatencyService.saveLatencyToDatabase();
        } catch (Exception e) {
            log.error("手动保存延迟数据失败", e);
            return ReturnResult.error("手动保存延迟数据失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期延迟数据
     *
     * @param expireDays 过期天数，默认30天
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期延迟数据", description = "清理指定天数之前的延迟历史数据")
    public ReturnResult<Integer> cleanupExpiredLatencyData(
            @Parameter(description = "过期天数", example = "30")
            @RequestParam(defaultValue = "30") Integer expireDays) {
        try {
            return serverLatencyService.cleanupExpiredLatencyData(expireDays);
        } catch (Exception e) {
            log.error("清理过期延迟数据失败", e);
            return ReturnResult.error("清理过期延迟数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动更新服务器延迟
     *
     * @param serverId 服务器ID
     * @param latency  延迟时间
     * @return 更新结果
     */
    @PutMapping("/{serverId}")
    @Operation(summary = "手动更新服务器延迟", description = "手动更新指定服务器的延迟时间")
    public ReturnResult<Boolean> updateServerLatency(
            @Parameter(description = "服务器ID", required = true)
            @PathVariable Integer serverId,
            @Parameter(description = "延迟时间(毫秒)", required = true)
            @RequestParam Long latency) {
        try {
            var updateResult = serverLatencyService.updateServerLatencyToRedis(serverId, latency);
            if (updateResult.isSuccess() && updateResult.getData()) {
                // 推送到前端
                serverLatencyService.pushLatencyToFrontend(serverId, latency);
            }
            return updateResult;
        } catch (Exception e) {
            log.error("手动更新服务器延迟失败: serverId={}, latency={}", serverId, latency, e);
            return ReturnResult.error("手动更新服务器延迟失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器延迟状态分布
     *
     * @return 状态分布数据
     */
    @GetMapping("/status-distribution")
    @Operation(summary = "获取延迟状态分布", description = "获取服务器延迟状态的分布统计")
    public ReturnResult<Map<String, Object>> getLatencyStatusDistribution() {
        try {
            var statisticsResult = serverLatencyService.getLatencyStatistics();
            if (statisticsResult.isSuccess()) {
                Map<String, Object> statistics = statisticsResult.getData();
                
                // 构建状态分布数据
                Map<String, Object> distribution = Map.of(
                    "normal", statistics.getOrDefault("normalCount", 0),
                    "high", statistics.getOrDefault("highCount", 0),
                    "abnormal", statistics.getOrDefault("abnormalCount", 0),
                    "total", statistics.getOrDefault("totalServers", 0),
                    "timestamp", System.currentTimeMillis()
                );
                
                return ReturnResult.ok(distribution);
            } else {
                return ReturnResult.error(statisticsResult.getMsg());
            }
        } catch (Exception e) {
            log.error("获取延迟状态分布失败", e);
            return ReturnResult.error("获取延迟状态分布失败: " + e.getMessage());
        }
    }
}
