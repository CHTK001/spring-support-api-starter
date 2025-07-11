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
     * 获取启用的分组列表
     *
     * @return 启用的分组列表
     */
    ReturnResult<List<MonitorSysGenServerGroup>> getEnabledGroups();

    /**
     * 获取所有分组列表
     *
     * @return 所有分组列表
     */
    ReturnResult<List<MonitorSysGenServerGroup>> getAllGroups();

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
     * 更新分组状态
     *
     * @param groupId 分组ID
     * @param status  状态 0:禁用 1:启用
     * @return 更新结果
     */
    ReturnResult<Boolean> updateGroupStatus(Integer groupId, Integer status);

    /**
     * 更新分组排序
     *
     * @param groupId 分组ID
     * @param sort    排序号
     * @return 更新结果
     */
    ReturnResult<Boolean> updateGroupSort(Integer groupId, Integer sort);

    /**
     * 批量更新分组排序
     *
     * @param sortData 排序数据
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateGroupSort(List<MonitorSysGenServerGroup> sortData);

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
     * 设置默认分组
     *
     * @param groupId 分组ID
     * @return 设置结果
     */
    ReturnResult<Boolean> setDefaultGroup(Integer groupId);

    /**
     * 获取分组下的服务器数量
     *
     * @param groupId 分组ID
     * @return 服务器数量
     */
    ReturnResult<Integer> getServerCountByGroup(Integer groupId);

    /**
     * 复制分组
     *
     * @param sourceGroupId 源分组ID
     * @param newGroupName  新分组名称
     * @return 复制结果
     */
    ReturnResult<Boolean> copyGroup(Integer sourceGroupId, String newGroupName);
}
