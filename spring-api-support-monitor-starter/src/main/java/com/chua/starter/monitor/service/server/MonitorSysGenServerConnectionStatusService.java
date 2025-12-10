package com.chua.starter.monitor.service.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionCheckResult;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionHealthReport;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionStatistics;

import java.util.List;

/**
 * 服务器连接状态管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerConnectionStatusService {

    /**
     * 测试单个服务器连接
     *
     * @param serverId 服务器ID
     * @return 连接测试结果
     */
    ReturnResult<MonitorSysGenServerConnectionStatus> testServerConnection(Integer serverId);

    /**
     * 批量测试服务器连接
     *
     * @param serverIds 服务器ID列表
     * @return 批量连接测试结果
     */
    ReturnResult<List<MonitorSysGenServerConnectionStatus>> batchTestServerConnection(List<Integer> serverIds);

    /**
     * 获取所有服务器连接状态
     *
     * @return 所有服务器连接状态
     */
    ReturnResult<List<MonitorSysGenServerConnectionStatus>> getAllServerConnectionStatus();

    /**
     * 根据服务器ID获取连接状态
     *
     * @param serverId 服务器ID
     * @return 服务器连接状态
     */
    ReturnResult<MonitorSysGenServerConnectionStatus> getServerConnectionStatus(Integer serverId);

    /**
     * 根据连接状态获取服务器列表
     *
     * @param connectionStatus 连接状态
     * @return 服务器连接状态列表
     */
    ReturnResult<List<MonitorSysGenServerConnectionStatus>> getServersByConnectionStatus(Integer connectionStatus);

    /**
     * 更新服务器连接状态
     *
     * @param serverId         服务器ID
     * @param connectionStatus 连接状态
     * @param connectionError  连接错误信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServerConnectionStatus(Integer serverId, Integer connectionStatus, String connectionError);

    /**
     * 批量更新服务器连接状态
     *
     * @param serverIds        服务器ID列表
     * @param connectionStatus 连接状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateServerConnectionStatus(List<Integer> serverIds, Integer connectionStatus);

    /**
     * 获取连接状态统计
     *
     * @return 连接状态统计
     */
    ReturnResult<MonitorSysGenServerConnectionStatistics> getConnectionStatusStatistics();

    /**
     * 获取长时间未连接的服务器
     *
     * @param minutes 分钟数
     * @return 长时间未连接的服务器列表
     */
    ReturnResult<List<MonitorSysGenServerConnectionStatus>> getLongTimeNoConnectServers(Integer minutes);

    /**
     * 检查所有服务器连接状态
     *
     * @return 检查结果
     */
    ReturnResult<MonitorSysGenServerConnectionCheckResult> checkAllServerConnections();

    /**
     * 获取服务器连接健康度报告
     *
     * @return 健康度报告
     */
    ReturnResult<MonitorSysGenServerConnectionHealthReport> getServerConnectionHealthReport();
}
