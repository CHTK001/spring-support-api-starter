package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.pojo.*;
import com.chua.common.support.lang.code.ReturnResult;

/**
 * 服务器延迟检测服务接口
 *
 * @author CH
 * @since 2024/12/25
 */
public interface MonitorSysGenServerLatencyDetectionService {

    /**
     * 检测服务器延迟
     *
     * @param server 服务器配置
     * @return 检测结果，包含延迟时间等信息
     */
    ReturnResult<LatencyDetectionResult> detectLatency(MonitorSysGenServer server);

    /**
     * 批量检测服务器延迟
     * 只检测在线的服务器
     *
     * @return 检测结果
     */
    ReturnResult<LatencyBatchDetectionResult> batchDetectLatency();

    /**
     * 异步检测服务器延迟
     * 
     * @param server 服务器配置
     */
    void detectLatencyAsync(MonitorSysGenServer server);



    /**
     * 获取服务器延迟统计信息
     *
     * @param serverId 服务器ID
     * @return 延迟统计信息
     */
    ReturnResult<LatencyStatistics> getLatencyStatistics(Integer serverId);

    /**
     * 获取所有服务器延迟概览
     *
     * @return 延迟概览
     */
    ReturnResult<LatencyOverview> getLatencyOverview();

    /**
     * 重置服务器延迟统计
     * 
     * @param serverId 服务器ID
     * @return 重置结果
     */
    ReturnResult<Boolean> resetLatencyStatistics(Integer serverId);

    /**
     * 检查延迟告警
     *
     * @return 告警信息
     */
    ReturnResult<LatencyAlerts> checkLatencyAlerts();
}
