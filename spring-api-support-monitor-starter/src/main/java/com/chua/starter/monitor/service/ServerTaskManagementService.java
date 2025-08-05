package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;

import java.util.Map;

/**
 * 服务器任务管理服务接口
 * 提供服务器任务的创建、更新、删除等管理功能
 *
 * @author CH
 * @since 2024/12/28
 */
public interface ServerTaskManagementService {

    /**
     * 为服务器创建任务
     *
     * @param serverId 服务器ID
     * @return 创建结果
     */
    ReturnResult<Boolean> createServerTasks(Integer serverId);

    /**
     * 更新服务器任务配置
     *
     * @param serverId 服务器ID
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServerTasks(Integer serverId);

    /**
     * 移除服务器任务
     *
     * @param serverId 服务器ID
     * @return 移除结果
     */
    ReturnResult<Boolean> removeServerTasks(Integer serverId);

    /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getTaskStatistics();

    /**
     * 重新初始化所有服务器任务
     *
     * @return 初始化结果
     */
    ReturnResult<Boolean> reinitializeAllTasks();

    /**
     * 检查任务健康状态
     *
     * @return 健康状态
     */
    ReturnResult<Map<String, Object>> checkTaskHealth();
}
