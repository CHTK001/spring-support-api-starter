package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器延迟服务接口
 * 负责管理服务器延迟数据的缓存、存储和推送
 *
 * @author CH
 * @since 2024/12/26
 */
public interface ServerLatencyService {

    /**
     * 更新服务器延迟到Redis缓存
     *
     * @param serverId 服务器ID
     * @param latency  延迟时间(毫秒)
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServerLatencyToRedis(Integer serverId, Long latency);

    /**
     * 从Redis获取服务器当前延迟
     *
     * @param serverId 服务器ID
     * @return 延迟时间(毫秒)，null表示未检测
     */
    ReturnResult<Long> getServerLatencyFromRedis(Integer serverId);

    /**
     * 批量获取服务器延迟
     *
     * @param serverIds 服务器ID列表
     * @return 服务器延迟映射
     */
    ReturnResult<Map<Integer, Long>> getBatchServerLatency(List<Integer> serverIds);

    /**
     * 推送延迟数据到前端
     *
     * @param serverId 服务器ID
     * @param latency  延迟时间
     * @return 推送结果
     */
    ReturnResult<Boolean> pushLatencyToFrontend(Integer serverId, Long latency);

    /**
     * 批量推送延迟数据到前端
     *
     * @param latencyData 延迟数据映射
     * @return 推送结果
     */
    ReturnResult<Boolean> batchPushLatencyToFrontend(Map<Integer, Long> latencyData);

    /**
     * 保存延迟数据到数据库
     * 每5分钟调用一次
     *
     * @return 保存结果
     */
    ReturnResult<Integer> saveLatencyToDatabase();

    /**
     * 获取服务器延迟历史数据
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 历史延迟数据
     */
    ReturnResult<List<Map<String, Object>>> getServerLatencyHistory(Integer serverId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期的延迟数据
     *
     * @param expireDays 过期天数
     * @return 清理结果
     */
    ReturnResult<Integer> cleanupExpiredLatencyData(Integer expireDays);

    /**
     * 获取延迟统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getLatencyStatistics();

    /**
     * 检查延迟告警
     *
     * @return 告警信息
     */
    ReturnResult<List<Map<String, Object>>> checkLatencyAlerts();
}
