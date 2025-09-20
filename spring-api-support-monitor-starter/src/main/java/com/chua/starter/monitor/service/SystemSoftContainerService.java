package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftContainer;

import java.util.List;
import java.util.Map;

/**
 * 系统软件容器管理服务接口
 *
 * @author CH
 * @since 2025/01/15
 */
public interface SystemSoftContainerService {

    /**
     * 根据条件查询容器列表
     *
     * @param serverId      服务器ID
     * @param softId        软件ID
     * @param status        容器状态
     * @param containerName 容器名称
     * @return 容器列表
     */
    ReturnResult<List<SystemSoftContainer>> selectContainerList(Integer serverId, Integer softId, String status, String containerName);

    /**
     * 保存或更新容器信息
     *
     * @param systemSoftContainer 容器信息
     * @return 保存结果
     */
    ReturnResult<SystemSoftContainer> saveOrUpdate(SystemSoftContainer systemSoftContainer);

    /**
     * 根据ID删除容器记录
     *
     * @param containerId 容器ID
     * @return 删除结果
     */
    ReturnResult<Boolean> removeById(Integer containerId);

    /**
     * 批量删除容器记录
     *
     * @param containerIds 容器ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> removeByIds(List<Integer> containerIds);

    /**
     * 创建容器
     *
     * @param softId        软件ID
     * @param serverId      服务器ID
     * @param containerName 容器名称
     * @param imageTag      镜像标签
     * @param params        创建参数（端口映射、环境变量、数据卷等）
     * @return 创建结果
     */
    ReturnResult<SystemSoftContainer> createContainer(Integer softId, Integer serverId, String containerName, String imageTag, Map<String, Object> params);

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     * @return 启动结果
     */
    ReturnResult<Boolean> startContainer(Integer containerId);

    /**
     * 停止容器
     *
     * @param containerId 容器ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopContainer(Integer containerId);

    /**
     * 重启容器
     *
     * @param containerId 容器ID
     * @return 重启结果
     */
    ReturnResult<Boolean> restartContainer(Integer containerId);

    /**
     * 删除容器（从服务器中删除）
     *
     * @param containerId 容器ID
     * @param force       是否强制删除
     * @return 删除结果
     */
    ReturnResult<Boolean> removeContainer(Integer containerId, Boolean force);

    /**
     * 获取容器详情
     *
     * @param containerId 容器ID
     * @return 容器详情
     */
    ReturnResult<SystemSoftContainer> getContainerDetails(Integer containerId);

    /**
     * 获取容器日志
     *
     * @param containerId 容器ID
     * @param lines       日志行数（默认100行）
     * @return 容器日志
     */
    ReturnResult<String> getContainerLogs(Integer containerId, Integer lines);

    /**
     * 获取容器统计信息
     *
     * @param containerId 容器ID
     * @return 统计信息（CPU、内存、网络、磁盘等）
     */
    ReturnResult<Map<String, Object>> getContainerStats(Integer containerId);

    /**
     * 同步容器状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    ReturnResult<Integer> syncContainerStatus(Integer serverId);

    /**
     * 批量操作容器
     *
     * @param containerIds 容器ID列表
     * @param operation    操作类型（start/stop/restart/remove）
     * @param params       操作参数
     * @return 操作结果
     */
    ReturnResult<Map<String, Object>> batchOperateContainers(List<Integer> containerIds, String operation, Map<String, Object> params);

    /**
     * 检查容器名称是否存在
     *
     * @param containerName 容器名称
     * @param serverId      服务器ID
     * @param excludeId     排除的容器ID
     * @return 是否存在
     */
    ReturnResult<Boolean> checkContainerNameExists(String containerName, Integer serverId, Integer excludeId);

    /**
     * 检查端口是否被占用
     *
     * @param hostPort  主机端口
     * @param serverId  服务器ID
     * @param excludeId 排除的容器ID
     * @return 是否被占用
     */
    ReturnResult<Boolean> checkPortExists(Integer hostPort, Integer serverId, Integer excludeId);

    /**
     * 根据软件ID获取容器列表
     *
     * @param softId 软件ID
     * @return 容器列表
     */
    ReturnResult<List<SystemSoftContainer>> getContainersBySoftId(Integer softId);

    /**
     * 根据服务器ID获取容器列表
     *
     * @param serverId 服务器ID
     * @return 容器列表
     */
    ReturnResult<List<SystemSoftContainer>> getContainersByServerId(Integer serverId);

    /**
     * 获取容器状态统计
     *
     * @param serverId 服务器ID（可选）
     * @return 状态统计信息
     */
    ReturnResult<Map<String, Object>> getContainerStatusStats(Integer serverId);

    /**
     * 获取运行中的容器列表
     *
     * @return 运行中的容器列表
     */
    ReturnResult<List<SystemSoftContainer>> getRunningContainers();

    /**
     * 获取异常容器列表
     *
     * @return 异常容器列表
     */
    ReturnResult<List<SystemSoftContainer>> getAbnormalContainers();

    /**
     * 更新容器配置
     *
     * @param containerId 容器ID
     * @param config      新配置信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateContainerConfig(Integer containerId, Map<String, Object> config);
}