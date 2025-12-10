package com.chua.starter.monitor.service.docker;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.entity.SystemSoftContainerStats;

import java.util.List;
import java.util.Map;

/**
 * 系统软件容器管理服务接口
 *
 * @author CH
 * @since 2025/01/15
 */
public interface SystemSoftContainerService extends IService<SystemSoftContainer> {

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
     * 根据条件查询容器列表(ForContainer)
     *
     * @param serverId      服务器ID
     * @param softId        软件ID
     * @param status        容器状态
     * @param containerName 容器名称
     * @return 容器列表
     */
    default ReturnResult<List<SystemSoftContainer>> selectContainerListForContainer(Integer serverId, Integer softId, String status, String containerName) {
        return selectContainerList(serverId, softId, status, containerName);
    }

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
    ReturnResult<Boolean> createContainer(Integer softId, Integer serverId, String containerName, String imageTag, Map<String, Object> params);

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     * @return 启动结果
     */
    ReturnResult<Boolean> startContainer(Integer containerId);

    /**
     * 启动容器(ForContainer)
     *
     * @param containerId 容器ID
     * @return 启动结果
     */
    default ReturnResult<Boolean> startContainerForContainer(Integer containerId) {
        return startContainer(containerId);
    }

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
    ReturnResult<SystemSoftContainerStats> getContainerStats(Integer containerId);

    /**
     * 同步容器状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    ReturnResult<Integer> syncContainerStatus(Integer serverId);

    /**
     * 启动容器日志推送
     *
     * @param containerId 容器ID
     * @param lines       日志行数（默认100行）
     * @return 启动结果
     */
    ReturnResult<Boolean> startContainerLog(Integer containerId, Integer lines);
    
    /**
     * 停止容器日志推送
     *
     * @param containerId 容器ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopContainerLog(Integer containerId);
    
    /**
     * 在容器中执行命令
     *
     * @param containerId 容器ID
     * @param command     要执行的命令
     * @return 执行结果
     */
    ReturnResult<String> execCommand(Integer containerId, String command);
}