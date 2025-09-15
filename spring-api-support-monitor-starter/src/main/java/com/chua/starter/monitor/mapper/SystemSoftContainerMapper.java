package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件容器信息Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftContainerMapper extends BaseMapper<SystemSoftContainer> {

    /**
     * 根据软件ID查询容器列表
     *
     * @param softId 软件ID
     * @return 容器列表
     */
    List<SystemSoftContainer> selectBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID查询容器列表
     *
     * @param serverId 服务器ID
     * @return 容器列表
     */
    List<SystemSoftContainer> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据Docker容器ID查询容器信息
     *
     * @param dockerContainerId Docker容器ID
     * @return 容器信息
     */
    SystemSoftContainer selectByDockerContainerId(@Param("dockerContainerId") String dockerContainerId);

    /**
     * 根据容器名称查询容器信息
     *
     * @param containerName 容器名称
     * @return 容器信息
     */
    SystemSoftContainer selectByContainerName(@Param("containerName") String containerName);

    /**
     * 根据软件ID和服务器ID查询容器列表
     *
     * @param softId   软件ID
     * @param serverId 服务器ID
     * @return 容器列表
     */
    List<SystemSoftContainer> selectBySoftIdAndServerId(@Param("softId") Integer softId,
                                                        @Param("serverId") Integer serverId);

    /**
     * 根据容器状态查询容器列表
     *
     * @param containerStatus 容器状态
     * @return 容器列表
     */
    List<SystemSoftContainer> selectByContainerStatus(@Param("containerStatus") String containerStatus);

    /**
     * 根据服务器ID和状态查询容器列表
     *
     * @param serverId        服务器ID
     * @param containerStatus 容器状态
     * @return 容器列表
     */
    List<SystemSoftContainer> selectByServerIdAndStatus(@Param("serverId") Integer serverId,
                                                        @Param("containerStatus") String containerStatus);

    /**
     * 根据镜像ID查询容器列表
     *
     * @param imageId 镜像ID
     * @return 容器列表
     */
    List<SystemSoftContainer> selectByImageId(@Param("imageId") String imageId);

    /**
     * 根据端口映射查询容器列表
     *
     * @param hostPort 主机端口
     * @return 容器列表
     */
    List<SystemSoftContainer> selectByHostPort(@Param("hostPort") Integer hostPort);

    /**
     * 根据软件ID统计容器数量
     *
     * @param softId 软件ID
     * @return 容器数量
     */
    Long countBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID统计容器数量
     *
     * @param serverId 服务器ID
     * @return 容器数量
     */
    Long countByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据容器状态统计容器数量
     *
     * @param containerStatus 容器状态
     * @return 容器数量
     */
    Long countByContainerStatus(@Param("containerStatus") String containerStatus);

    /**
     * 根据服务器ID和状态统计容器数量
     *
     * @param serverId        服务器ID
     * @param containerStatus 容器状态
     * @return 容器数量
     */
    Long countByServerIdAndStatus(@Param("serverId") Integer serverId,
                                  @Param("containerStatus") String containerStatus);

    /**
     * 根据状态统计容器数量（按状态分组）
     *
     * @param serverId 服务器ID（可选）
     * @return 状态统计结果列表，每个元素包含status和count字段
     */
    List<java.util.Map<String, Object>> countByStatus(@Param("serverId") Integer serverId);

    /**
     * 批量更新容器状态
     *
     * @param containerIds    容器ID列表
     * @param containerStatus 新状态
     * @return 更新的记录数
     */
    Integer batchUpdateContainerStatus(@Param("containerIds") List<Integer> containerIds,
                                       @Param("containerStatus") String containerStatus);

    /**
     * 根据软件ID删除容器
     *
     * @param softId 软件ID
     * @return 删除的记录数
     */
    Integer deleteBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID删除容器
     *
     * @param serverId 服务器ID
     * @return 删除的记录数
     */
    Integer deleteByServerId(@Param("serverId") Integer serverId);

    /**
     * 检查容器名称是否存在
     *
     * @param containerName 容器名称
     * @param serverId      服务器ID
     * @param excludeId     排除的ID
     * @return 存在的数量
     */
    Long checkContainerNameExists(@Param("containerName") String containerName,
                                  @Param("serverId") Integer serverId,
                                  @Param("excludeId") Integer excludeId);

    /**
     * 检查端口是否被占用
     *
     * @param hostPort  主机端口
     * @param serverId  服务器ID
     * @param excludeId 排除的ID
     * @return 占用的数量
     */
    Long checkPortExists(@Param("hostPort") Integer hostPort,
                         @Param("serverId") Integer serverId,
                         @Param("excludeId") Integer excludeId);

    /**
     * 根据关键字搜索容器
     *
     * @param keyword 关键字
     * @return 容器信息列表
     */
    List<SystemSoftContainer> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取运行中的容器列表
     *
     * @return 运行中的容器列表
     */
    List<SystemSoftContainer> selectRunningContainers();

    /**
     * 获取异常状态的容器列表
     *
     * @return 异常状态的容器列表
     */
    List<SystemSoftContainer> selectAbnormalContainers();

    /**
     * 根据创建时间范围查询容器列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 容器信息列表
     */
    List<SystemSoftContainer> selectByCreateTimeRange(@Param("startTime") String startTime,
                                                      @Param("endTime") String endTime);
}