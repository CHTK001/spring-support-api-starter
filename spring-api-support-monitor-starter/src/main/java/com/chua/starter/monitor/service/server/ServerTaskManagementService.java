package com.chua.starter.monitor.service.server;

import com.chua.common.support.lang.code.ReturnResult;

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

}
