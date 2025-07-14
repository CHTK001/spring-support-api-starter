package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerGroup;

import java.util.List;

/**
 * 服务器分组管理服务接口
 *
 * @author CH
 * @since 2025/01/11
 */
public interface MonitorSysGenServerGroupService extends IService<MonitorSysGenServerGroup> {

    /**
     * 分页查询服务器分组
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenServerGroup> pageFor(IPage<MonitorSysGenServerGroup> page, MonitorSysGenServerGroup entity);

    /**
     * 分页查询服务器分组（包含服务器数量）
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    ReturnResult<IPage<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> pageForWithServerCount(
            IPage<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount> page,
            MonitorSysGenServerGroup entity);

    /**
     * 获取启用的分组列表
     *
     * @return 启用的分组列表
     */
    ReturnResult<List<MonitorSysGenServerGroup>> getEnabledGroups();

    /**
     * 获取启用的分组列表（包含服务器数量）
     *
     * @return 启用的分组列表
     */
    ReturnResult<List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> getEnabledGroupsWithServerCount();

    /**
     * 获取所有分组列表
     *
     * @return 所有分组列表
     */
    ReturnResult<List<MonitorSysGenServerGroup>> getAllGroups();

    /**
     * 获取所有分组列表（包含服务器数量）
     *
     * @return 所有分组列表
     */
    ReturnResult<List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> getAllGroupsWithServerCount();

    /**
     * 根据ID获取分组信息
     *
     * @param groupId 分组ID
     * @return 分组信息
     */
    ReturnResult<MonitorSysGenServerGroup> getGroupById(Integer groupId);

    /**
     * 创建分组
     *
     * @param group 分组信息
     * @return 创建结果
     */
    ReturnResult<Boolean> createGroup(MonitorSysGenServerGroup group);

    /**
     * 更新分组
     *
     * @param group 分组信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateGroup(MonitorSysGenServerGroup group);

    /**
     * 删除分组
     *
     * @param groupId 分组ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteGroup(Integer groupId);

    /**
     * 批量删除分组
     *
     * @param groupIds 分组ID列表
     * @return 删除结果
     */
    ReturnResult<Boolean> batchDeleteGroups(List<Integer> groupIds);

    /**
     * 检查分组名称是否存在
     *
     * @param groupName 分组名称
     * @param excludeId 排除的分组ID
     * @return 是否存在
     */
    ReturnResult<Boolean> checkGroupNameExists(String groupName, Integer excludeId);

    /**
     * 获取默认分组
     *
     * @return 默认分组
     */
    ReturnResult<MonitorSysGenServerGroup> getDefaultGroup();

    /**
     * 复制分组
     *
     * @param sourceGroupId 源分组ID
     * @param newGroupName  新分组名称
     * @return 复制结果
     */
    ReturnResult<Boolean> copyGroup(Integer sourceGroupId, String newGroupName);

    /**
     * 获取分组统计信息
     *
     * @return 分组统计信息
     */
    ReturnResult<java.util.Map<String, Object>> getGroupStatistics();

    /**
     * 根据分组ID列表获取分组信息
     *
     * @param groupIds 分组ID列表
     * @return 分组列表
     */
    ReturnResult<List<MonitorSysGenServerGroup>> getGroupsByIds(List<Integer> groupIds);

    /**
     * 获取分组及其服务器数量
     *
     * @return 分组及服务器数量信息
     */
    ReturnResult<List<java.util.Map<String, Object>>> getGroupsWithServerCount();

    /**
     * 移动服务器到指定分组
     *
     * @param serverIds 服务器ID列表
     * @param groupId   目标分组ID
     * @return 操作结果
     */
    ReturnResult<Boolean> moveServersToGroup(List<Integer> serverIds, Integer groupId);

    /**
     * 获取分组下的服务器列表
     *
     * @param groupId 分组ID
     * @return 服务器列表
     */
    ReturnResult<List<com.chua.starter.monitor.starter.entity.MonitorSysGenServer>> getServersByGroupId(
            Integer groupId);

    /**
     * 批量设置服务器分组
     *
     * @param serverGroupMappings 服务器分组映射列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchSetServerGroups(List<java.util.Map<String, Object>> serverGroupMappings);

    /**
     * 获取详细的分组统计信息
     *
     * @return 详细统计信息
     */
    ReturnResult<java.util.Map<String, Object>> getDetailedGroupStatistics();

    /**
     * 获取分组及其服务器详细信息
     *
     * @param includeServers 是否包含服务器列表
     * @return 分组详细信息列表
     */
    ReturnResult<List<java.util.Map<String, Object>>> getGroupsWithDetails(Boolean includeServers);

    /**
     * 根据分组ID获取分组详细信息
     *
     * @param groupId        分组ID
     * @param includeServers 是否包含服务器列表
     * @return 分组详细信息
     */
    ReturnResult<java.util.Map<String, Object>> getGroupDetailById(Integer groupId, Boolean includeServers);
}
