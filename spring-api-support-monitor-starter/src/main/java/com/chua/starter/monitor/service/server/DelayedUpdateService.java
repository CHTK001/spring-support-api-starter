package com.chua.starter.monitor.service.server;

/**
 * 延迟更新服务
 *
 * 将处理任务中的数据库更新改为先写入内存缓冲，
 * 按 serverId + 类型 只保留最新的一条，定时批量刷新到数据库，
 * 以降低数据库写压力、避免抖动。
 *
 * 说明：
 * - 当前支持的类型：在线状态、健康状态
 * - 如需扩展类型，可在实现类中新增对应的 Map 与 flush 逻辑
 *
 * @author CH
 */
public interface DelayedUpdateService {

    /**
     * 提交在线状态（延迟刷新）
     * @param serverId 服务器ID
     * @param online 是否在线
     */
    void submitOnlineStatus(Integer serverId, boolean online);

    /**
     * 提交健康状态（延迟刷新）
     * @param serverId 服务器ID
     * @param healthy 是否健康
     */
    void submitHealthStatus(Integer serverId, boolean healthy);

    /**
     * 提交连接状态（延迟刷新）
     * @param serverId 服务器ID
     * @param connectionStatus 连接状态（0/1/2/3）
     * @param errorMessage 错误信息，可为空
     */
    void submitConnectionStatus(Integer serverId, Integer connectionStatus, String errorMessage);

    /**
     * 提交指标清理（延迟刷新）
     * 注意：LogCleanupStep 原实现为全局清理，这里按 serverId 维度缓冲，刷盘时合并为一次全局清理，采用所有请求中的最小 expireTime，避免重复多次删除。
     * @param serverId 服务器ID
     * @param expireTime 过期时间点，删除此时间点之前的数据
     */
    void submitMetricsCleanup(Integer serverId, java.time.LocalDateTime expireTime);
}

