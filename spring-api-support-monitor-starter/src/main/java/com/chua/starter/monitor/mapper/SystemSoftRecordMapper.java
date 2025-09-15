package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemSoftRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统软件操作记录Mapper接口
 *
 * @author CH
 * @since 2025/01/15
 */
@Mapper
public interface SystemSoftRecordMapper extends BaseMapper<SystemSoftRecord> {

    /**
     * 根据软件ID查询操作记录列表
     *
     * @param softId 软件ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID查询操作记录列表
     *
     * @param serverId 服务器ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据操作类型查询操作记录列表
     *
     * @param operationType 操作类型
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationType(@Param("operationType") String operationType);

    /**
     * 根据操作状态查询操作记录列表
     *
     * @param operationStatus 操作状态
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationStatus(@Param("operationStatus") String operationStatus);

    /**
     * 根据软件ID和操作类型查询操作记录列表
     *
     * @param softId        软件ID
     * @param operationType 操作类型
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectBySoftIdAndOperationType(@Param("softId") Integer softId,
                                                          @Param("operationType") String operationType);

    /**
     * 根据服务器ID和操作类型查询操作记录列表
     *
     * @param serverId      服务器ID
     * @param operationType 操作类型
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByServerIdAndOperationType(@Param("serverId") Integer serverId,
                                                            @Param("operationType") String operationType);

    /**
     * 根据软件ID和操作状态查询操作记录列表
     *
     * @param softId          软件ID
     * @param operationStatus 操作状态
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectBySoftIdAndOperationStatus(@Param("softId") Integer softId,
                                                            @Param("operationStatus") String operationStatus);

    /**
     * 根据操作用户查询操作记录列表
     *
     * @param operationUser 操作用户
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationUser(@Param("operationUser") String operationUser);

    /**
     * 根据容器ID查询操作记录列表
     *
     * @param containerId 容器ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByContainerId(@Param("containerId") Integer containerId);

    /**
     * 根据镜像ID查询操作记录列表
     *
     * @param imageId 镜像ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByImageId(@Param("imageId") Integer imageId);

    /**
     * 根据软件ID统计操作记录数量
     *
     * @param softId 软件ID
     * @return 操作记录数量
     */
    Long countBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID统计操作记录数量
     *
     * @param serverId 服务器ID
     * @return 操作记录数量
     */
    Long countByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据操作类型统计操作记录数量
     *
     * @param operationType 操作类型
     * @return 操作记录数量
     */
    Long countByOperationType(@Param("operationType") String operationType);

    /**
     * 根据操作状态统计操作记录数量
     *
     * @param operationStatus 操作状态
     * @return 操作记录数量
     */
    Long countByOperationStatus(@Param("operationStatus") String operationStatus);

    /**
     * 根据操作用户统计操作记录数量
     *
     * @param operationUser 操作用户
     * @return 操作记录数量
     */
    Long countByOperationUser(@Param("operationUser") String operationUser);

    /**
     * 根据软件ID删除操作记录
     *
     * @param softId 软件ID
     * @return 删除的记录数
     */
    Integer deleteBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID删除操作记录
     *
     * @param serverId 服务器ID
     * @return 删除的记录数
     */
    Integer deleteByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据容器ID删除操作记录
     *
     * @param containerId 容器ID
     * @return 删除的记录数
     */
    Integer deleteByContainerId(@Param("containerId") Integer containerId);

    /**
     * 根据镜像ID删除操作记录
     *
     * @param imageId 镜像ID
     * @return 删除的记录数
     */
    Integer deleteByImageId(@Param("imageId") Integer imageId);

    /**
     * 根据关键字搜索操作记录
     *
     * @param keyword 关键字
     * @return 操作记录列表
     */
    List<SystemSoftRecord> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取失败的操作记录列表
     *
     * @return 失败的操作记录列表
     */
    List<SystemSoftRecord> selectFailedRecords();

    /**
     * 获取成功的操作记录列表
     *
     * @return 成功的操作记录列表
     */
    List<SystemSoftRecord> selectSuccessRecords();

    /**
     * 获取进行中的操作记录列表
     *
     * @return 进行中的操作记录列表
     */
    List<SystemSoftRecord> selectRunningRecords();

    /**
     * 根据操作时间范围查询操作记录列表
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationTimeRange(@Param("startTime") String startTime,
                                                      @Param("endTime") String endTime);

    /**
     * 获取最新的操作记录列表
     *
     * @param limit 限制数量
     * @return 最新操作记录列表
     */
    List<SystemSoftRecord> selectLatestRecords(@Param("limit") Integer limit);

    /**
     * 根据软件ID获取最新的操作记录
     *
     * @param softId 软件ID
     * @return 最新操作记录
     */
    SystemSoftRecord selectLatestBySoftId(@Param("softId") Integer softId);

    /**
     * 根据服务器ID获取最新的操作记录
     *
     * @param serverId 服务器ID
     * @return 最新操作记录
     */
    SystemSoftRecord selectLatestByServerId(@Param("serverId") Integer serverId);

    /**
     * 根据操作耗时范围查询操作记录列表
     *
     * @param minDuration 最小耗时(毫秒)
     * @param maxDuration 最大耗时(毫秒)
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByDurationRange(@Param("minDuration") Long minDuration,
                                                 @Param("maxDuration") Long maxDuration);

    /**
     * 统计各操作类型的数量
     *
     * @return 操作类型统计结果
     */
    List<SystemSoftRecord> countGroupByOperationType();

    /**
     * 统计各操作状态的数量
     *
     * @return 操作状态统计结果
     */
    List<SystemSoftRecord> countGroupByOperationStatus();
}