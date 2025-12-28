package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemSoftOperationLog;

import java.util.List;

/**
 * 系统软件操作日志服务接口
 *
 * @author CH
 * @since 2025/12/28
 */
public interface SystemSoftOperationLogService extends IService<SystemSoftOperationLog> {

    /**
     * 记录操作开始日志
     *
     * @param operationId 操作ID
     * @param operationType 操作类型
     * @param operationTarget 操作目标
     * @param containerId 容器ID
     * @param imageId 镜像ID
     * @param softId 软件ID
     * @param serverId 服务器ID
     * @param recordId 记录ID
     * @param operator 操作者
     * @return 日志记录
     */
    SystemSoftOperationLog logStart(String operationId, String operationType, String operationTarget,
                                     Integer containerId, Integer imageId, Integer softId, 
                                     Integer serverId, Integer recordId, String operator);

    /**
     * 记录操作进度日志
     *
     * @param operationId 操作ID
     * @param message 日志消息
     * @param progress 进度
     * @param dockerStatus Docker状态
     * @param dockerProgress Docker进度
     * @return 日志记录
     */
    SystemSoftOperationLog logProgress(String operationId, String message, Integer progress,
                                        String dockerStatus, String dockerProgress);

    /**
     * 记录操作完成日志
     *
     * @param operationId 操作ID
     * @param message 日志消息
     * @param success 是否成功
     * @return 日志记录
     */
    SystemSoftOperationLog logComplete(String operationId, String message, boolean success);

    /**
     * 记录操作错误日志
     *
     * @param operationId 操作ID
     * @param message 日志消息
     * @param errorMessage 错误消息
     * @param errorStack 错误堆栈
     * @return 日志记录
     */
    SystemSoftOperationLog logError(String operationId, String message, String errorMessage, String errorStack);

    /**
     * 根据操作ID查询日志列表
     *
     * @param operationId 操作ID
     * @return 日志列表
     */
    List<SystemSoftOperationLog> getByOperationId(String operationId);

    /**
     * 根据容器ID查询最近的操作日志
     *
     * @param containerId 容器ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<SystemSoftOperationLog> getRecentByContainerId(Integer containerId, int limit);

    /**
     * 根据服务器ID查询最近的操作日志
     *
     * @param serverId 服务器ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<SystemSoftOperationLog> getRecentByServerId(Integer serverId, int limit);

    /**
     * 分页查询操作日志
     *
     * @param page 当前页
     * @param size 每页大小
     * @param operationType 操作类型
     * @param serverId 服务器ID
     * @param containerId 容器ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<SystemSoftOperationLog> pageQuery(int page, int size, String operationType, 
                                             Integer serverId, Integer containerId, String status);
}
