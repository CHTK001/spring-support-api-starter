package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;

import java.util.Map;

/**
 * 服务器数据上报服务接口
 * 
 * @author CH
 * @since 2024/12/25
 */
public interface MonitorSysGenServerDataReportService {

    /**
     * 处理本地上报数据
     * 本地服务器直接获取系统指标数据
     * 
     * @param server 服务器配置
     * @return 处理结果
     */
    ReturnResult<MonitorSysGenServerMetrics> handleLocalReport(MonitorSysGenServer server);

    /**
     * 处理API上报数据
     * 远程服务器通过API接口上报数据
     * 
     * @param server 服务器配置
     * @param reportData 上报的数据
     * @return 处理结果
     */
    ReturnResult<MonitorSysGenServerMetrics> handleApiReport(MonitorSysGenServer server, Map<String, Object> reportData);

    /**
     * 处理Prometheus上报数据
     * 从Prometheus服务器获取指标数据
     * 
     * @param server 服务器配置
     * @return 处理结果
     */
    ReturnResult<Map<String, Object>> handlePrometheusReport(MonitorSysGenServer server);

    /**
     * 获取服务器上报统计信息
     * 
     * @param serverId 服务器ID
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getServerReportStatistics(Integer serverId);

    /**
     * 获取所有服务器上报概览
     * 
     * @return 上报概览
     */
    ReturnResult<Map<String, Object>> getServerReportOverview();

    /**
     * 清理过期的服务器指标数据
     * 
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ReturnResult<String> cleanupExpiredServerMetrics(Integer retentionDays);

    /**
     * 获取服务器上报历史
     * 
     * @param serverId 服务器ID
     * @param limit 限制数量
     * @return 上报历史
     */
    ReturnResult<Map<String, Object>> getServerReportHistory(Integer serverId, Integer limit);

    /**
     * 重置服务器上报统计
     * 
     * @param serverId 服务器ID
     * @return 重置结果
     */
    ReturnResult<String> resetServerReportStats(Integer serverId);

    /**
     * 获取服务器上报错误统计
     * 
     * @param serverId 服务器ID
     * @return 错误统计
     */
    ReturnResult<Map<String, Object>> getServerReportErrorStats(Integer serverId);

    /**
     * 批量处理服务器数据上报
     *
     * @return 处理结果
     */
    ReturnResult<Map<String, Object>> handleBatchServerReport();

    /**
     * 启动数据收集
     *
     * @param server 服务器配置
     * @return 启动结果
     */
    ReturnResult<String> startDataCollection(MonitorSysGenServer server);

    /**
     * 停止数据收集
     *
     * @param serverId 服务器ID
     * @return 停止结果
     */
    ReturnResult<String> stopDataCollection(Integer serverId);

    /**
     * 处理Prometheus查询
     *
     * @param server 服务器配置
     * @return 查询结果
     */
    ReturnResult<Map<String, Object>> handlePrometheusQuery(MonitorSysGenServer server);
}
