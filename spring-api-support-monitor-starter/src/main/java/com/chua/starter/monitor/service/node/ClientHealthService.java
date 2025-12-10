package com.chua.starter.monitor.service.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.ClientHealthReport;
import com.chua.starter.monitor.pojo.node.ClientHealthStatus;

import java.util.List;
import java.util.Map;

/**
 * 客户端健康状态服务接口
 * 
 * @author CH
 * @since 2024/12/13
 */
public interface ClientHealthService {

    /**
     * 上报客户端健康状态
     * 
     * @param report 健康状态报告
     * @return 上报结果
     */
    ReturnResult<Void> reportHealth(ClientHealthReport report);

    /**
     * 检查客户端是否健康
     * 
     * @param clientIp 客户端IP
     * @param clientPort 客户端端口
     * @return 是否健康
     */
    ReturnResult<Boolean> isClientHealthy(String clientIp, Integer clientPort);

    /**
     * 获取客户端健康状态
     * 
     * @param clientIp 客户端IP
     * @param clientPort 客户端端口
     * @return 健康状态
     */
    ReturnResult<ClientHealthStatus> getClientHealth(String clientIp, Integer clientPort);

    /**
     * 获取所有健康的客户端
     * 
     * @return 健康客户端列表
     */
    ReturnResult<List<ClientHealthStatus>> getAllHealthyClients();

    /**
     * 清理过期的健康状态记录
     * 
     * @return 清理结果
     */
    ReturnResult<Integer> cleanupExpiredHealthRecords();

    /**
     * 获取健康状态统计信息
     * 
     * @return 统计信息
     */
    ReturnResult<Object> getHealthStatistics();

    /**
     * 检查指定服务器的客户端健康状态
     *
     * @param serverId 服务器ID
     * @return 是否健康
     */
    ReturnResult<Boolean> isServerClientHealthy(Integer serverId);

    /**
     * 批量检查客户端在线状态
     *
     * @param clientAddresses 客户端地址列表
     * @return 在线状态映射
     */
    ReturnResult<Map<String, Boolean>> batchCheckOnlineStatus(List<String> clientAddresses);

    /**
     * 获取服务器客户端健康状态
     *
     * @param serverId 服务器ID
     * @return 客户端健康状态
     */
    ReturnResult<ClientHealthStatus> getServerClientHealth(Integer serverId);
}
