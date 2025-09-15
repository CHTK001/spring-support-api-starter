package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.pojo.request.UpdateContainerRequest;
import com.github.dockerjava.api.model.Statistics;

import java.util.List;
import java.util.Map;

/**
 * 系统软件容器管理服务接口
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
public interface SystemSoftContainerService extends IService<SystemSoftContainer> {

    /**
     * 分页查询容器列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param serverId 服务器ID（可选）
     * @param status   容器状态（可选）
     * @param keyword  关键词（可选）
     * @return 分页结果
     */
    IPage<SystemSoftContainer> getContainerPage(Long current, Long size, Integer serverId, String status, String keyword);

    /**
     * 根据软件ID查询容器列表
     *
     * @param softId 软件ID
     * @return 容器列表
     */
    List<SystemSoftContainer> getContainersBySoftId(Integer softId);

    /**
     * 根据服务器ID查询容器列表
     *
     * @param serverId 服务器ID
     * @return 容器列表
     */
    List<SystemSoftContainer> getContainersByServerId(Integer serverId);

    /**
     * 创建容器
     *
     * @param softId        软件ID
     * @param serverId      服务器ID
     * @param containerName 容器名称
     * @param imageTag      镜像标签
     * @param ports         端口映射（格式：hostPort:containerPort）
     * @param envVars       环境变量（JSON格式）
     * @param volumes       数据卷映射（JSON格式）
     * @param command       启动命令（可选）
     * @param args          启动参数（可选）
     * @return 是否创建成功
     */
    boolean createContainer(Integer softId, Integer serverId, String containerName, String imageTag,
                            String ports, String envVars, String volumes, String command, String args);

    /**
     * 启动容器
     *
     * @param containerId 容器ID
     * @return 是否启动成功
     */
    boolean startContainer(Integer containerId);

    /**
     * 停止容器
     *
     * @param containerId 容器ID
     * @return 是否停止成功
     */
    boolean stopContainer(Integer containerId);

    /**
     * 重启容器
     *
     * @param containerId 容器ID
     * @return 是否重启成功
     */
    boolean restartContainer(Integer containerId);

    /**
     * 删除容器
     *
     * @param containerId 容器ID
     * @param force       是否强制删除
     * @return 是否删除成功
     */
    boolean removeContainer(Integer containerId, boolean force);

    /**
     * 获取容器日志
     *
     * @param containerId 容器ID
     * @param lines       日志行数
     * @return 日志内容
     */
    String getContainerLogs(Integer containerId, Integer lines);

    /**
     * 获取容器统计信息
     *
     * @param containerId 容器ID
     * @return 统计信息
     */
    Statistics getContainerStats(Integer containerId);

    /**
     * 同步容器状态
     * 从Docker API获取最新状态并更新数据库
     *
     * @param serverId 服务器ID（可选，为空则同步所有服务器）
     * @return 同步的容器数量
     */
    int syncContainerStatus(Integer serverId);

    /**
     * 同步单个容器状态
     * 从Docker API获取指定容器的最新状态并更新数据库
     *
     * @param containerId 容器ID
     * @return 更新后的容器信息，如果容器不存在或同步失败则返回null
     */
    SystemSoftContainer syncSingleContainerStatus(Integer containerId);

    /**
     * 批量操作容器
     *
     * @param containerIds 容器ID列表
     * @param operation    操作类型（start/stop/restart/remove）
     * @return 操作结果
     */
    Map<String, Object> batchOperateContainers(List<Integer> containerIds, String operation);

    /**
     * 检查容器名称是否存在
     *
     * @param containerName 容器名称
     * @param serverId      服务器ID
     * @param excludeId     排除的容器ID（用于更新时检查）
     * @return 是否存在
     */
    boolean checkContainerNameExists(String containerName, Integer serverId, Integer excludeId);

    /**
     * 检查端口是否被占用
     *
     * @param hostPort  主机端口
     * @param serverId  服务器ID
     * @param excludeId 排除的容器ID（用于更新时检查）
     * @return 是否被占用
     */
    boolean checkPortExists(Integer hostPort, Integer serverId, Integer excludeId);

    /**
     * 获取容器状态统计
     *
     * @param serverId 服务器ID（可选）
     * @return 状态统计
     */
    Map<String, Long> getContainerStatusStats(Integer serverId);

    /**
     * 获取运行中的容器列表
     *
     * @return 运行中的容器列表
     */
    List<SystemSoftContainer> getRunningContainers();

    /**
     * 获取异常容器列表
     *
     * @return 异常容器列表
     */
    List<SystemSoftContainer> getAbnormalContainers();

    /**
     * 更新容器配置
     *
     * @param containerId 容器ID
     * @param request     更新请求
     * @return 是否更新成功
     */
    boolean updateContainer(Integer containerId, UpdateContainerRequest request);

    /**
     * 根据条件查询容器列表
     *
     * @param queryParams 查询参数Map，支持的参数包括：
     *                    - serverId: 服务器ID
     *                    - softId: 软件ID
     *                    - containerStatus: 容器状态
     *                    - containerName: 容器名称（模糊查询）
     *                    - imageId: 镜像ID
     *                    - hostPort: 主机端口
     * @return 容器列表
     */
    List<SystemSoftContainer> listByCondition(Map<String, Object> queryParams);
}