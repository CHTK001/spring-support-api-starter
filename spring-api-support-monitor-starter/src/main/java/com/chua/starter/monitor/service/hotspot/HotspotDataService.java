package com.chua.starter.monitor.service.hotspot;

import com.chua.starter.monitor.pojo.HotspotReportData;

/**
 * Hotspot 数据处理服务接口
 * 处理 Hotspot Agent 上报的监控数据
 *
 * @author CH
 * @since 2024/12/11
 * @version 4.0.0.34
 */
public interface HotspotDataService {

    /**
     * 处理上报数据
     *
     * @param reportData 上报数据
     * @return 处理结果
     */
    boolean handleReport(HotspotReportData reportData);

    /**
     * 处理链路追踪数据
     *
     * @param appName 应用名称
     * @param traceData 链路追踪数据
     * @return 处理结果
     */
    boolean handleTraceData(String appName, HotspotReportData.TraceData traceData);

    /**
     * 处理 SQL 监控数据
     *
     * @param appName 应用名称
     * @param sqlData SQL 数据
     * @return 处理结果
     */
    boolean handleSqlData(String appName, HotspotReportData.SqlData sqlData);

    /**
     * 处理日志监控数据
     *
     * @param appName 应用名称
     * @param logData 日志数据
     * @return 处理结果
     */
    boolean handleLogData(String appName, HotspotReportData.LogData logData);

    /**
     * 处理异常监控数据
     *
     * @param appName 应用名称
     * @param exceptionData 异常数据
     * @return 处理结果
     */
    boolean handleExceptionData(String appName, HotspotReportData.ExceptionData exceptionData);

    /**
     * 处理服务实例数据
     *
     * @param appName 应用名称
     * @param serverData 服务数据
     * @return 处理结果
     */
    boolean handleServerData(String appName, HotspotReportData.ServerData serverData);
}
