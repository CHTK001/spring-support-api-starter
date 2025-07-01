package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标数据服务接口
 *
 * @author CH
 * @since 2024/12/18
 */
public interface MonitorSysGenServerMetricsService {

    /**
     * 分页查询服务器指标数据
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenServerMetrics> pageFor(Page<MonitorSysGenServerMetrics> page, MonitorSysGenServerMetrics entity);

    /**
     * 保存服务器指标数据
     *
     * @param metrics 指标数据
     * @return 保存结果
     */
    ReturnResult<Boolean> saveMetrics(MonitorSysGenServerMetrics metrics);

    /**
     * 批量保存服务器指标数据
     *
     * @param metricsList 指标数据列表
     * @return 保存结果
     */
    ReturnResult<Boolean> batchSaveMetrics(List<MonitorSysGenServerMetrics> metricsList);

    /**
     * 获取服务器最新指标数据
     *
     * @param serverId 服务器ID
     * @return 最新指标数据
     */
    ReturnResult<MonitorSysGenServerMetrics> getLatestMetrics(Integer serverId);

    /**
     * 获取服务器最新指标数据（直接返回对象）
     *
     * @param serverId 服务器ID
     * @return 最新指标数据，如果不存在返回null
     */
    MonitorSysGenServerMetrics getLatestMetricsDirectly(Integer serverId);

    /**
     * 获取指定时间范围内的指标数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 指标数据列表
     */
    List<MonitorSysGenServerMetrics> getMetricsByTimeRange(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器指标历史数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据
     */
    ReturnResult<List<MonitorSysGenServerMetrics>> getHistoryMetrics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器指标图表数据
     *
     * @param serverId 服务器ID
     * @param metricType 指标类型 (cpu/memory/disk/network)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔 (5m/15m/1h/1d)
     * @return 图表数据
     */
    ReturnResult<Map<String, Object>> getMetricsChartData(Integer serverId, String metricType, 
                                                          LocalDateTime startTime, LocalDateTime endTime, String interval);

    /**
     * 获取多服务器对比图表数据
     *
     * @param serverIds 服务器ID列表
     * @param metricType 指标类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔
     * @return 对比图表数据
     */
    ReturnResult<Map<String, Object>> getCompareChartData(List<Integer> serverIds, String metricType,
                                                          LocalDateTime startTime, LocalDateTime endTime, String interval);

    /**
     * 获取服务器指标统计数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    ReturnResult<Map<String, Object>> getMetricsStatistics(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取服务器告警数据
     *
     * @param serverId 服务器ID
     * @param thresholds 告警阈值配置
     * @return 告警数据
     */
    ReturnResult<List<Map<String, Object>>> getAlertData(Integer serverId, Map<String, Object> thresholds);

    /**
     * 删除过期指标数据
     *
     * @param serverId 服务器ID
     * @param retentionDays 保留天数
     * @return 删除结果
     */
    ReturnResult<Integer> deleteExpiredMetrics(Integer serverId, Integer retentionDays);

    /**
     * 批量删除过期指标数据
     *
     * @return 删除结果
     */
    ReturnResult<Map<String, Object>> batchDeleteExpiredMetrics();

    /**
     * 获取服务器性能报告
     *
     * @param serverId 服务器ID
     * @param reportType 报告类型 (daily/weekly/monthly)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 性能报告
     */
    ReturnResult<Map<String, Object>> getPerformanceReport(Integer serverId, String reportType,
                                                           LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 导出指标数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param format 导出格式 (csv/excel/json)
     * @return 导出数据
     */
    ReturnResult<byte[]> exportMetricsData(Integer serverId, LocalDateTime startTime, LocalDateTime endTime, String format);

    /**
     * 获取实时指标数据
     *
     * @param serverId 服务器ID
     * @return 实时指标数据
     */
    ReturnResult<Map<String, Object>> getRealTimeMetrics(Integer serverId);

    /**
     * 获取服务器健康状态
     *
     * @param serverId 服务器ID
     * @return 健康状态
     */
    ReturnResult<Map<String, Object>> getServerHealthStatus(Integer serverId);

    /**
     * 获取服务器容量预测
     *
     * @param serverId 服务器ID
     * @param predictDays 预测天数
     * @return 容量预测数据
     */
    ReturnResult<Map<String, Object>> getCapacityPrediction(Integer serverId, Integer predictDays);

    /**
     * 获取服务器指标趋势分析
     *
     * @param serverId 服务器ID
     * @param metricType 指标类型
     * @param days 分析天数
     * @return 趋势分析数据
     */
    ReturnResult<Map<String, Object>> getTrendAnalysis(Integer serverId, String metricType, Integer days);

    /**
     * 获取服务器异常检测结果
     *
     * @param serverId 服务器ID
     * @param sensitivity 敏感度 (low/medium/high)
     * @return 异常检测结果
     */
    ReturnResult<List<Map<String, Object>>> getAnomalyDetection(Integer serverId, String sensitivity);

    /**
     * 清理过期指标数据
     *
     * @param expireTime 过期时间
     * @return 清理的记录数
     */
    ReturnResult<Integer> cleanupExpiredMetrics(LocalDateTime expireTime);

    /**
     * 优化指标数据表
     *
     * @return 优化结果
     */
    ReturnResult<Void> optimizeTables();
}
