package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerFileUploadQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器文件上传任务队列Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenServerFileUploadQueueMapper extends BaseMapper<MonitorSysGenServerFileUploadQueue> {

    /**
     * 获取队列统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getQueueStatistics();

    /**
     * 获取服务器队列统计信息
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    Map<String, Object> getServerQueueStatistics(@Param("serverId") Integer serverId);

    /**
     * 查询超时的处理中队列项
     *
     * @param timeoutTime 超时时间
     * @return 超时队列项列表
     */
    List<MonitorSysGenServerFileUploadQueue> selectTimeoutProcessingItems(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 重置超时的处理中队列项
     *
     * @param timeoutTime 超时时间
     * @return 重置数量
     */
    int resetTimeoutProcessingItems(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 清理已完成的队列项
     *
     * @param expiredTime 过期时间
     * @return 清理数量
     */
    int cleanupCompletedItems(@Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 获取队列长度
     *
     * @return 队列长度
     */
    Long getQueueLength();

    /**
     * 获取指定服务器的队列长度
     *
     * @param serverId 服务器ID
     * @return 队列长度
     */
    Long getServerQueueLength(@Param("serverId") Integer serverId);

    /**
     * 清空队列
     *
     * @return 清空数量
     */
    int clearQueue();

    /**
     * 清空指定服务器的队列
     *
     * @param serverId 服务器ID
     * @return 清空数量
     */
    int clearServerQueue(@Param("serverId") Integer serverId);

    /**
     * 查询可重试的失败队列项
     *
     * @param maxRetryCount 最大重试次数
     * @return 可重试队列项列表
     */
    List<MonitorSysGenServerFileUploadQueue> selectRetryableItems(@Param("maxRetryCount") Integer maxRetryCount);

    /**
     * 批量更新队列项状态
     *
     * @param queueIds 队列ID列表
     * @param status   新状态
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("queueIds") List<Long> queueIds, @Param("status") String status);

    /**
     * 获取队列项详细信息（包含任务信息）
     *
     * @param queueId 队列ID
     * @return 详细信息
     */
    Map<String, Object> getQueueItemDetails(@Param("queueId") Long queueId);
}
