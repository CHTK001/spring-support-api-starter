package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标数据控制器
 *
 * @author CH
 * @since 2024/12/18
 */
@RestController
@RequestMapping("v1/gen/server/metrics")
@Tag(name = "服务器指标数据")
@RequiredArgsConstructor
public class MonitorSysGenServerMetricsController {

    private final MonitorSysGenServerMetricsService monitorSysGenServerMetricsService;

    /**
     * 分页查询指标数据
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询指标数据")
    @GetMapping("page")
    public ReturnPageResult<MonitorSysGenServerMetrics> page(Query<MonitorSysGenServerMetrics> page, MonitorSysGenServerMetrics entity) {
        return PageResultUtils.ok(monitorSysGenServerMetricsService.pageFor(page.createPage(), entity));
    }

    /**
     * 获取服务器最新指标数据
     *
     * @param serverId 服务器ID
     * @return 最新指标数据
     */
    @Operation(summary = "获取服务器最新指标数据")
    @GetMapping("latest")
    public ReturnResult<MonitorSysGenServerMetrics> getLatestMetrics(@Parameter(name = "服务器ID") Integer serverId) {
        return monitorSysGenServerMetricsService.getLatestMetrics(serverId);
    }

    /**
     * 获取服务器指标历史数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据
     */
    @Operation(summary = "获取服务器指标历史数据")
    @GetMapping("history")
    public ReturnResult<List<MonitorSysGenServerMetrics>> getHistoryMetrics(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(name = "结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return monitorSysGenServerMetricsService.getHistoryMetrics(serverId, startTime, endTime);
    }

    /**
     * 获取服务器指标图表数据
     *
     * @param serverId 服务器ID
     * @param metricType 指标类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔
     * @return 图表数据
     */
    @Operation(summary = "获取服务器指标图表数据")
    @GetMapping("chart")
    public ReturnResult<Map<String, Object>> getMetricsChartData(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "指标类型") String metricType,
            @Parameter(name = "开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(name = "结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(name = "时间间隔") String interval) {
        return monitorSysGenServerMetricsService.getMetricsChartData(serverId, metricType, startTime, endTime, interval);
    }

    /**
     * 获取多服务器对比图表数据
     *
     * @param request 对比请求
     * @return 对比图表数据
     */
    @Operation(summary = "获取多服务器对比图表数据")
    @PostMapping("compare")
    public ReturnResult<Map<String, Object>> getCompareChartData(@RequestBody CompareChartRequest request) {
        return monitorSysGenServerMetricsService.getCompareChartData(
                request.getServerIds(), 
                request.getMetricType(), 
                request.getStartTime(), 
                request.getEndTime(), 
                request.getInterval());
    }

    /**
     * 获取服务器指标统计数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @Operation(summary = "获取服务器指标统计数据")
    @GetMapping("statistics")
    public ReturnResult<Map<String, Object>> getMetricsStatistics(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(name = "结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return monitorSysGenServerMetricsService.getMetricsStatistics(serverId, startTime, endTime);
    }

    /**
     * 获取服务器告警数据
     *
     * @param serverId 服务器ID
     * @param request 告警阈值配置
     * @return 告警数据
     */
    @Operation(summary = "获取服务器告警数据")
    @PostMapping("alerts")
    public ReturnResult<List<Map<String, Object>>> getAlertData(
            @Parameter(name = "服务器ID") Integer serverId,
            @RequestBody Map<String, Object> request) {
        return monitorSysGenServerMetricsService.getAlertData(serverId, request);
    }

    /**
     * 删除过期指标数据
     *
     * @param serverId 服务器ID
     * @param retentionDays 保留天数
     * @return 删除结果
     */
    @Operation(summary = "删除过期指标数据")
    @DeleteMapping("expired")
    public ReturnResult<Integer> deleteExpiredMetrics(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "保留天数") Integer retentionDays) {
        return monitorSysGenServerMetricsService.deleteExpiredMetrics(serverId, retentionDays);
    }

    /**
     * 批量删除过期指标数据
     *
     * @return 删除结果
     */
    @Operation(summary = "批量删除过期指标数据")
    @DeleteMapping("batch-expired")
    public ReturnResult<Map<String, Object>> batchDeleteExpiredMetrics() {
        return monitorSysGenServerMetricsService.batchDeleteExpiredMetrics();
    }

    /**
     * 获取服务器性能报告
     *
     * @param serverId 服务器ID
     * @param reportType 报告类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告
     */
    @Operation(summary = "获取服务器性能报告")
    @GetMapping("report")
    public ReturnResult<Map<String, Object>> getPerformanceReport(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "报告类型") String reportType,
            @Parameter(name = "开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(name = "结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return monitorSysGenServerMetricsService.getPerformanceReport(serverId, reportType, startTime, endTime);
    }

    /**
     * 导出指标数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param format 导出格式
     * @return 导出数据
     */
    @Operation(summary = "导出指标数据")
    @GetMapping("export")
    public ReturnResult<byte[]> exportMetricsData(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "开始时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(name = "结束时间") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(name = "导出格式") String format) {
        return monitorSysGenServerMetricsService.exportMetricsData(serverId, startTime, endTime, format);
    }

    /**
     * 获取实时指标数据
     *
     * @param serverId 服务器ID
     * @return 实时指标数据
     */
    @Operation(summary = "获取实时指标数据")
    @GetMapping("realtime")
    public ReturnResult<Map<String, Object>> getRealTimeMetrics(@Parameter(name = "服务器ID") Integer serverId) {
        return monitorSysGenServerMetricsService.getRealTimeMetrics(serverId);
    }

    /**
     * 获取服务器健康状态
     *
     * @param serverId 服务器ID
     * @return 健康状态
     */
    @Operation(summary = "获取服务器健康状态")
    @GetMapping("health")
    public ReturnResult<Map<String, Object>> getServerHealthStatus(@Parameter(name = "服务器ID") Integer serverId) {
        return monitorSysGenServerMetricsService.getServerHealthStatus(serverId);
    }

    /**
     * 获取服务器容量预测
     *
     * @param serverId 服务器ID
     * @param predictDays 预测天数
     * @return 容量预测数据
     */
    @Operation(summary = "获取服务器容量预测")
    @GetMapping("capacity-prediction")
    public ReturnResult<Map<String, Object>> getCapacityPrediction(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "预测天数") Integer predictDays) {
        return monitorSysGenServerMetricsService.getCapacityPrediction(serverId, predictDays);
    }

    /**
     * 获取服务器指标趋势分析
     *
     * @param serverId 服务器ID
     * @param metricType 指标类型
     * @param days 分析天数
     * @return 趋势分析数据
     */
    @Operation(summary = "获取服务器指标趋势分析")
    @GetMapping("trend-analysis")
    public ReturnResult<Map<String, Object>> getTrendAnalysis(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "指标类型") String metricType,
            @Parameter(name = "分析天数") Integer days) {
        return monitorSysGenServerMetricsService.getTrendAnalysis(serverId, metricType, days);
    }

    /**
     * 获取服务器异常检测结果
     *
     * @param serverId 服务器ID
     * @param sensitivity 敏感度
     * @return 异常检测结果
     */
    @Operation(summary = "获取服务器异常检测结果")
    @GetMapping("anomaly-detection")
    public ReturnResult<List<Map<String, Object>>> getAnomalyDetection(
            @Parameter(name = "服务器ID") Integer serverId,
            @Parameter(name = "敏感度") String sensitivity) {
        return monitorSysGenServerMetricsService.getAnomalyDetection(serverId, sensitivity);
    }

    /**
     * 对比图表请求
     */
    public static class CompareChartRequest {
        private List<Integer> serverIds;
        private String metricType;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String interval;

        public List<Integer> getServerIds() {
            return serverIds;
        }

        public void setServerIds(List<Integer> serverIds) {
            this.serverIds = serverIds;
        }

        public String getMetricType() {
            return metricType;
        }

        public void setMetricType(String metricType) {
            this.metricType = metricType;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }
    }
}
