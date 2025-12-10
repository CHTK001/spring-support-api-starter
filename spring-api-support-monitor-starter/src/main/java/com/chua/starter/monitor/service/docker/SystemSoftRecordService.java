package com.chua.starter.monitor.service.docker;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.SystemSoftRecord;

import java.util.List;

/**
 * 系统软件操作记录服务接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/15
 */
public interface SystemSoftRecordService extends IService<SystemSoftRecord> {

    /**
     * 分页查询操作记录列表
     *
     * @param page 分页参数
     * @param softId 软件ID（可选）
     * @param serverId 服务器ID（可选）
     * @param operationType 操作类型（可选）
     * @param operationStatus 操作状态（可选）
     * @param operationUser 操作用户（可选）
     * @return 分页结果
     */
    IPage<SystemSoftRecord> selectRecordPage(Page<SystemSoftRecord> page,
                                             Integer softId,
                                             Integer serverId,
                                             String operationType,
                                             Integer operationStatus,
                                             String operationUser);

    /**
     * 根据软件ID查询操作记录列表
     *
     * @param softId 软件ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectBySoftId(Integer softId);

    /**
     * 根据服务器ID查询操作记录列表
     *
     * @param serverId 服务器ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByServerId(Integer serverId);

    /**
     * 根据操作类型查询操作记录列表
     *
     * @param operationType 操作类型
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationType(String operationType);

    /**
     * 根据操作状态查询操作记录列表
     *
     * @param operationStatus 操作状态
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationStatus(Integer operationStatus);

    /**
     * 根据操作用户查询操作记录列表
     *
     * @param operationUser 操作用户
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByOperationUser(String operationUser);

    /**
     * 根据容器ID查询操作记录列表
     *
     * @param containerId 容器ID
     * @return 操作记录列表
     */
    List<SystemSoftRecord> selectByContainerId(String containerId);

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
     * 根据关键字搜索操作记录
     *
     * @param keyword 关键字
     * @return 操作记录列表
     */
    List<SystemSoftRecord> searchByKeyword(String keyword);

    /**
     * 根据软件ID获取最新的操作记录
     *
     * @param softId 软件ID
     * @return 最新操作记录
     */
    SystemSoftRecord selectLatestBySoftId(Integer softId);

    /**
     * 根据服务器ID获取最新的操作记录
     *
     * @param serverId 服务器ID
     * @return 最新操作记录
     */
    SystemSoftRecord selectLatestByServerId(Integer serverId);

    /**
     * 根据软件ID统计操作记录数量
     *
     * @param softId 软件ID
     * @return 操作记录数量
     */
    Long countBySoftId(Integer softId);

    /**
     * 根据服务器ID统计操作记录数量
     *
     * @param serverId 服务器ID
     * @return 操作记录数量
     */
    Long countByServerId(Integer serverId);

    /**
     * 根据操作类型统计操作记录数量
     *
     * @param operationType 操作类型
     * @return 操作记录数量
     */
    Long countByOperationType(String operationType);

    /**
     * 根据操作状态统计操作记录数量
     *
     * @param operationStatus 操作状态
     * @return 操作记录数量
     */
    Long countByOperationStatus(Integer operationStatus);

    /**
     * 根据操作用户统计操作记录数量
     *
     * @param operationUser 操作用户
     * @return 操作记录数量
     */
    Long countByOperationUser(String operationUser);
}