package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerGroup;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerGroupMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerGroupService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务器分组管理服务实现类
 *
 * @author CH
 * @since 2025/01/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenServerGroupServiceImpl
        extends ServiceImpl<MonitorSysGenServerGroupMapper, MonitorSysGenServerGroup>
        implements MonitorSysGenServerGroupService {

    private final MonitorSysGenServerService serverService;

    @Override
    public IPage<MonitorSysGenServerGroup> pageFor(IPage<MonitorSysGenServerGroup> page,
            MonitorSysGenServerGroup entity) {
        LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();

        // 按分组名称模糊查询
        if (StringUtils.isNotBlank(entity.getMonitorSysGenServerGroupName())) {
            wrapper.like(MonitorSysGenServerGroup::getMonitorSysGenServerGroupName,
                    entity.getMonitorSysGenServerGroupName());
        }

        // 按状态查询
        if (entity.getMonitorSysGenServerGroupStatus() != null) {
            wrapper.eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupStatus,
                    entity.getMonitorSysGenServerGroupStatus());
        }

        // 按排序号升序排列
        wrapper.orderByAsc(MonitorSysGenServerGroup::getMonitorSysGenServerGroupSort)
                .orderByDesc(MonitorSysGenServerGroup::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public ReturnResult<IPage<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> pageForWithServerCount(
            IPage<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount> page,
            MonitorSysGenServerGroup entity) {
        try {
            IPage<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount> result = baseMapper
                    .selectPageWithServerCount(page, entity);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("分页查询服务器分组（包含服务器数量）失败", e);
            return ReturnResult.error("分页查询服务器分组（包含服务器数量）失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerGroup>> getEnabledGroups() {
        try {
            LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupStatus, 1)
                    .orderByAsc(MonitorSysGenServerGroup::getMonitorSysGenServerGroupSort)
                    .orderByDesc(MonitorSysGenServerGroup::getCreateTime);

            List<MonitorSysGenServerGroup> groups = baseMapper.selectList(wrapper);
            return ReturnResult.ok(groups);
        } catch (Exception e) {
            log.error("获取启用分组列表失败", e);
            return ReturnResult.error("获取启用分组列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> getEnabledGroupsWithServerCount() {
        try {
            List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount> groups = baseMapper
                    .selectByStatusWithServerCount(1);
            return ReturnResult.ok(groups);
        } catch (Exception e) {
            log.error("获取启用分组列表（包含服务器数量）失败", e);
            return ReturnResult.error("获取启用分组列表（包含服务器数量）失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerGroup>> getAllGroups() {
        try {
            LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(MonitorSysGenServerGroup::getMonitorSysGenServerGroupSort)
                    .orderByDesc(MonitorSysGenServerGroup::getCreateTime);

            List<MonitorSysGenServerGroup> groups = baseMapper.selectList(wrapper);
            return ReturnResult.ok(groups);
        } catch (Exception e) {
            log.error("获取所有分组列表失败", e);
            return ReturnResult.error("获取所有分组列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount>> getAllGroupsWithServerCount() {
        try {
            List<com.chua.starter.monitor.starter.pojo.MonitorSysGenServerGroupWithCount> groups = baseMapper
                    .selectAllWithServerCount();
            return ReturnResult.ok(groups);
        } catch (Exception e) {
            log.error("获取所有分组列表（包含服务器数量）失败", e);
            return ReturnResult.error("获取所有分组列表（包含服务器数量）失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerGroup> getGroupById(Integer groupId) {
        try {
            if (groupId == null) {
                return ReturnResult.error("分组ID不能为空");
            }

            MonitorSysGenServerGroup group = baseMapper.selectById(groupId);
            if (group == null) {
                return ReturnResult.error("分组不存在");
            }

            return ReturnResult.ok(group);
        } catch (Exception e) {
            log.error("获取分组信息失败: groupId={}", groupId, e);
            return ReturnResult.error("获取分组信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> createGroup(MonitorSysGenServerGroup group) {
        try {
            // 参数验证
            if (group == null) {
                return ReturnResult.error("分组信息不能为空");
            }

            if (StringUtils.isBlank(group.getMonitorSysGenServerGroupName())) {
                return ReturnResult.error("分组名称不能为空");
            }

            // 检查分组名称是否已存在
            ReturnResult<Boolean> checkResult = checkGroupNameExists(group.getMonitorSysGenServerGroupName(), null);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            if (checkResult.getData()) {
                return ReturnResult.error("分组名称已存在");
            }

            // 设置默认值
            if (group.getMonitorSysGenServerGroupStatus() == null) {
                group.setMonitorSysGenServerGroupStatus(1); // 默认启用
            }

            if (group.getMonitorSysGenServerGroupSort() == null) {
                // 获取最大排序号并加1
                Integer maxSort = getMaxSort();
                group.setMonitorSysGenServerGroupSort(maxSort + 1);
            }

            if (group.getMonitorSysGenServerGroupIsDefault() == null) {
                group.setMonitorSysGenServerGroupIsDefault(0); // 默认非默认分组
            }

            // 如果设置为默认分组，需要取消其他分组的默认状态
            if (Integer.valueOf(1).equals(group.getMonitorSysGenServerGroupIsDefault())) {
                clearDefaultGroups();
            }

            boolean result = save(group);
            if (result) {
                log.info("创建分组成功: groupName={}, groupId={}", group.getMonitorSysGenServerGroupName(),
                        group.getMonitorSysGenServerGroupId());
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("创建分组失败");
            }

        } catch (Exception e) {
            log.error("创建分组失败: groupName={}", group != null ? group.getMonitorSysGenServerGroupName() : "null", e);
            return ReturnResult.error("创建分组失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> updateGroup(MonitorSysGenServerGroup group) {
        try {
            // 参数验证
            if (group == null || group.getMonitorSysGenServerGroupId() == null) {
                return ReturnResult.error("分组信息不完整");
            }

            if (StringUtils.isBlank(group.getMonitorSysGenServerGroupName())) {
                return ReturnResult.error("分组名称不能为空");
            }

            // 检查分组是否存在
            MonitorSysGenServerGroup existingGroup = baseMapper.selectById(group.getMonitorSysGenServerGroupId());
            if (existingGroup == null) {
                return ReturnResult.error("分组不存在");
            }

            // 检查分组名称是否已存在（排除当前分组）
            ReturnResult<Boolean> checkResult = checkGroupNameExists(group.getMonitorSysGenServerGroupName(),
                    group.getMonitorSysGenServerGroupId());
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            if (checkResult.getData()) {
                return ReturnResult.error("分组名称已存在");
            }

            // 如果设置为默认分组，需要取消其他分组的默认状态
            if (Integer.valueOf(1).equals(group.getMonitorSysGenServerGroupIsDefault())) {
                clearDefaultGroups();
            }

            boolean result = updateById(group);
            if (result) {
                log.info("更新分组成功: groupName={}, groupId={}", group.getMonitorSysGenServerGroupName(),
                        group.getMonitorSysGenServerGroupId());
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("更新分组失败");
            }

        } catch (Exception e) {
            log.error("更新分组失败: groupId={}", group != null ? group.getMonitorSysGenServerGroupId() : "null", e);
            return ReturnResult.error("更新分组失败: " + e.getMessage());
        }
    }

    /**
     * 获取最大排序号
     */
    private Integer getMaxSort() {
        LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MonitorSysGenServerGroup::getMonitorSysGenServerGroupSort)
                .orderByDesc(MonitorSysGenServerGroup::getMonitorSysGenServerGroupSort).last("LIMIT 1");

        MonitorSysGenServerGroup group = baseMapper.selectOne(wrapper);
        return group != null && group.getMonitorSysGenServerGroupSort() != null
                ? group.getMonitorSysGenServerGroupSort()
                : 0;
    }

    /**
     * 清除所有分组的默认状态
     */
    private void clearDefaultGroups() {
        LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupIsDefault, 1);

        MonitorSysGenServerGroup updateGroup = new MonitorSysGenServerGroup();
        updateGroup.setMonitorSysGenServerGroupIsDefault(0);

        baseMapper.update(updateGroup, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteGroup(Integer groupId) {
        try {
            if (groupId == null) {
                return ReturnResult.error("分组ID不能为空");
            }

            // 检查分组是否存在
            MonitorSysGenServerGroup group = baseMapper.selectById(groupId);
            if (group == null) {
                return ReturnResult.error("分组不存在");
            }

            // 检查是否为默认分组
            if (Integer.valueOf(1).equals(group.getMonitorSysGenServerGroupIsDefault())) {
                return ReturnResult.error("默认分组不能删除");
            }

            // 检查分组下是否有服务器
            Integer serverCount = baseMapper.countServersByGroupId(groupId);
            if (serverCount != null && serverCount > 0) {
                return ReturnResult.error("分组下还有服务器，不能删除");
            }

            boolean result = removeById(groupId);
            if (result) {
                log.info("删除分组成功: groupId={}, groupName={}", groupId, group.getMonitorSysGenServerGroupName());
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("删除分组失败");
            }

        } catch (Exception e) {
            log.error("删除分组失败: groupId={}", groupId, e);
            return ReturnResult.error("删除分组失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchDeleteGroups(List<Integer> groupIds) {
        try {
            if (groupIds == null || groupIds.isEmpty()) {
                return ReturnResult.error("分组ID列表不能为空");
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (Integer groupId : groupIds) {
                ReturnResult<Boolean> result = deleteGroup(groupId);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    errorMessages.append("分组ID ").append(groupId).append(": ").append(result.getMsg()).append("; ");
                }
            }

            if (failCount == 0) {
                log.info("批量删除分组成功: 成功{}个", successCount);
                return ReturnResult.ok(true);
            } else if (successCount == 0) {
                return ReturnResult.error("批量删除分组失败: " + errorMessages.toString());
            } else {
                return ReturnResult
                        .error("批量删除分组部分成功: 成功" + successCount + "个，失败" + failCount + "个。" + errorMessages.toString());
            }

        } catch (Exception e) {
            log.error("批量删除分组失败", e);
            return ReturnResult.error("批量删除分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkGroupNameExists(String groupName, Integer excludeId) {
        try {
            if (StringUtils.isBlank(groupName)) {
                return ReturnResult.error("分组名称不能为空");
            }

            LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupName, groupName);

            if (excludeId != null) {
                wrapper.ne(MonitorSysGenServerGroup::getMonitorSysGenServerGroupId, excludeId);
            }

            long count = baseMapper.selectCount(wrapper);
            return ReturnResult.ok(count > 0);

        } catch (Exception e) {
            log.error("检查分组名称是否存在失败: groupName={}, excludeId={}", groupName, excludeId, e);
            return ReturnResult.error("检查分组名称是否存在失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerGroup> getDefaultGroup() {
        try {
            LambdaQueryWrapper<MonitorSysGenServerGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupIsDefault, 1)
                    .eq(MonitorSysGenServerGroup::getMonitorSysGenServerGroupStatus, 1);

            MonitorSysGenServerGroup defaultGroup = baseMapper.selectOne(wrapper);
            return ReturnResult.ok(defaultGroup);

        } catch (Exception e) {
            log.error("获取默认分组失败", e);
            return ReturnResult.error("获取默认分组失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> copyGroup(Integer sourceGroupId, String newGroupName) {
        try {
            if (sourceGroupId == null) {
                return ReturnResult.error("源分组ID不能为空");
            }

            if (StringUtils.isBlank(newGroupName)) {
                return ReturnResult.error("新分组名称不能为空");
            }

            // 检查源分组是否存在
            MonitorSysGenServerGroup sourceGroup = baseMapper.selectById(sourceGroupId);
            if (sourceGroup == null) {
                return ReturnResult.error("源分组不存在");
            }

            // 检查新分组名称是否已存在
            ReturnResult<Boolean> checkResult = checkGroupNameExists(newGroupName, null);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            if (checkResult.getData()) {
                return ReturnResult.error("新分组名称已存在");
            }

            // 创建新分组
            MonitorSysGenServerGroup newGroup = new MonitorSysGenServerGroup();
            newGroup.setMonitorSysGenServerGroupName(newGroupName);
            newGroup.setMonitorSysGenServerGroupDesc(sourceGroup.getMonitorSysGenServerGroupDesc());
            newGroup.setMonitorSysGenServerGroupColor(sourceGroup.getMonitorSysGenServerGroupColor());
            newGroup.setMonitorSysGenServerGroupIcon(sourceGroup.getMonitorSysGenServerGroupIcon());
            newGroup.setMonitorSysGenServerGroupStatus(1); // 默认启用
            newGroup.setMonitorSysGenServerGroupIsDefault(0); // 非默认分组
            newGroup.setMonitorSysGenServerGroupRemark("复制自: " + sourceGroup.getMonitorSysGenServerGroupName());

            // 设置排序号
            Integer maxSort = getMaxSort();
            newGroup.setMonitorSysGenServerGroupSort(maxSort + 1);

            boolean result = save(newGroup);
            if (result) {
                log.info("复制分组成功: sourceGroupId={}, newGroupName={}, newGroupId={}", sourceGroupId, newGroupName,
                        newGroup.getMonitorSysGenServerGroupId());
                return ReturnResult.ok(true);
            } else {
                return ReturnResult.error("复制分组失败");
            }

        } catch (Exception e) {
            log.error("复制分组失败: sourceGroupId={}, newGroupName={}", sourceGroupId, newGroupName, e);
            return ReturnResult.error("复制分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<java.util.Map<String, Object>> getGroupStatistics() {
        try {
            List<java.util.Map<String, Object>> statistics = baseMapper.getGroupStatistics();
            if (statistics != null && !statistics.isEmpty()) {
                return ReturnResult.ok(statistics.get(0));
            } else {
                // 返回默认统计信息
                java.util.Map<String, Object> defaultStats = new java.util.HashMap<>();
                defaultStats.put("total_groups", 0);
                defaultStats.put("enabled_groups", 0);
                defaultStats.put("default_groups", 0);
                defaultStats.put("avg_sort", 0.0);
                return ReturnResult.ok(defaultStats);
            }
        } catch (Exception e) {
            log.error("获取分组统计信息失败", e);
            return ReturnResult.error("获取分组统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerGroup>> getGroupsByIds(List<Integer> groupIds) {
        try {
            if (groupIds == null || groupIds.isEmpty()) {
                return ReturnResult.error("分组ID列表不能为空");
            }

            List<MonitorSysGenServerGroup> groups = baseMapper.selectByIds(groupIds);
            return ReturnResult.ok(groups);

        } catch (Exception e) {
            log.error("根据ID列表获取分组信息失败: groupIds={}", groupIds, e);
            return ReturnResult.error("根据ID列表获取分组信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<java.util.Map<String, Object>>> getGroupsWithServerCount() {
        try {
            List<java.util.Map<String, Object>> groupsWithCount = baseMapper.selectGroupsWithServerCount();
            return ReturnResult.ok(groupsWithCount);

        } catch (Exception e) {
            log.error("获取分组及服务器数量信息失败", e);
            return ReturnResult.error("获取分组及服务器数量信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> moveServersToGroup(List<Integer> serverIds, Integer groupId) {
        try {
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }

            if (groupId != null) {
                // 检查目标分组是否存在且启用
                MonitorSysGenServerGroup targetGroup = baseMapper.selectById(groupId);
                if (targetGroup == null) {
                    return ReturnResult.error("目标分组不存在");
                }
                if (!Integer.valueOf(1).equals(targetGroup.getMonitorSysGenServerGroupStatus())) {
                    return ReturnResult.error("目标分组未启用");
                }
            }

            // 调用服务器服务来更新服务器的分组
            return serverService.batchUpdateServerGroup(serverIds, groupId);

        } catch (Exception e) {
            log.error("移动服务器到分组失败: serverIds={}, groupId={}", serverIds, groupId, e);
            return ReturnResult.error("移动服务器到分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<com.chua.starter.monitor.starter.entity.MonitorSysGenServer>> getServersByGroupId(
            Integer groupId) {
        try {
            if (groupId == null) {
                return ReturnResult.error("分组ID不能为空");
            }

            // 调用服务器服务来获取分组下的服务器列表
            return serverService.getServersByGroupId(groupId);

        } catch (Exception e) {
            log.error("获取分组下服务器列表失败: groupId={}", groupId, e);
            return ReturnResult.error("获取分组下服务器列表失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchSetServerGroups(List<java.util.Map<String, Object>> serverGroupMappings) {
        try {
            if (serverGroupMappings == null || serverGroupMappings.isEmpty()) {
                return ReturnResult.error("服务器分组映射列表不能为空");
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (java.util.Map<String, Object> mapping : serverGroupMappings) {
                Object serverIdObj = mapping.get("serverId");
                Object groupIdObj = mapping.get("groupId");

                if (serverIdObj == null) {
                    failCount++;
                    errorMessages.append("服务器ID为空; ");
                    continue;
                }

                Integer serverId = Integer.valueOf(serverIdObj.toString());
                Integer groupId = groupIdObj != null ? Integer.valueOf(groupIdObj.toString()) : null;

                // 调用服务器服务来更新单个服务器的分组
                ReturnResult<Boolean> result = serverService.batchUpdateServerGroup(List.of(serverId), groupId);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    errorMessages.append("服务器ID ").append(serverId).append(": ").append(result.getMsg()).append("; ");
                }
            }

            if (failCount == 0) {
                log.info("批量设置服务器分组成功: 成功{}个", successCount);
                return ReturnResult.ok(true);
            } else if (successCount == 0) {
                return ReturnResult.error("批量设置服务器分组失败: " + errorMessages.toString());
            } else {
                return ReturnResult.error(
                        "批量设置服务器分组部分成功: 成功" + successCount + "个，失败" + failCount + "个。" + errorMessages.toString());
            }

        } catch (Exception e) {
            log.error("批量设置服务器分组失败", e);
            return ReturnResult.error("批量设置服务器分组失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<java.util.Map<String, Object>> getDetailedGroupStatistics() {
        try {
            // 获取基础统计信息
            ReturnResult<java.util.Map<String, Object>> basicStats = getGroupStatistics();
            if (!basicStats.isSuccess()) {
                return basicStats;
            }

            java.util.Map<String, Object> detailedStats = new java.util.HashMap<>(basicStats.getData());

            // 获取服务器统计信息
            ReturnResult<com.chua.starter.monitor.starter.pojo.ServerStatisticsDTO> serverStats = serverService
                    .getServerStatistics();
            if (serverStats.isSuccess() && serverStats.getData() != null) {
                detailedStats.put("total_servers", serverStats.getData().getTotalServers());
                detailedStats.put("online_servers", serverStats.getData().getOnlineServers());
                detailedStats.put("offline_servers", serverStats.getData().getOfflineServers());
            }

            // 获取未分组服务器数量
            ReturnResult<List<com.chua.starter.monitor.starter.entity.MonitorSysGenServer>> ungroupedServers = serverService
                    .getUngroupedServers();
            if (ungroupedServers.isSuccess()) {
                detailedStats.put("ungrouped_servers", ungroupedServers.getData().size());
            }

            return ReturnResult.ok(detailedStats);

        } catch (Exception e) {
            log.error("获取详细分组统计信息失败", e);
            return ReturnResult.error("获取详细分组统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<java.util.Map<String, Object>>> getGroupsWithDetails(Boolean includeServers) {
        try {
            // 获取分组及服务器数量信息
            List<java.util.Map<String, Object>> groupsWithCount = baseMapper.selectGroupsWithServerCount();

            if (Boolean.TRUE.equals(includeServers)) {
                // 为每个分组添加服务器列表
                for (java.util.Map<String, Object> group : groupsWithCount) {
                    Integer groupId = (Integer) group.get("groupId");
                    if (groupId != null) {
                        ReturnResult<List<com.chua.starter.monitor.starter.entity.MonitorSysGenServer>> serversResult = serverService
                                .getServersByGroupId(groupId);
                        if (serversResult.isSuccess()) {
                            group.put("servers", serversResult.getData());
                        }
                    }
                }
            }

            return ReturnResult.ok(groupsWithCount);

        } catch (Exception e) {
            log.error("获取分组详细信息失败", e);
            return ReturnResult.error("获取分组详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<java.util.Map<String, Object>> getGroupDetailById(Integer groupId, Boolean includeServers) {
        try {
            if (groupId == null) {
                return ReturnResult.error("分组ID不能为空");
            }

            // 获取分组基本信息
            MonitorSysGenServerGroup group = baseMapper.selectById(groupId);
            if (group == null) {
                return ReturnResult.error("分组不存在");
            }

            java.util.Map<String, Object> groupDetail = new java.util.HashMap<>();
            groupDetail.put("groupId", group.getMonitorSysGenServerGroupId());
            groupDetail.put("groupName", group.getMonitorSysGenServerGroupName());
            groupDetail.put("groupDesc", group.getMonitorSysGenServerGroupDesc());
            groupDetail.put("groupColor", group.getMonitorSysGenServerGroupColor());
            groupDetail.put("groupIcon", group.getMonitorSysGenServerGroupIcon());
            groupDetail.put("groupStatus", group.getMonitorSysGenServerGroupStatus());
            groupDetail.put("isDefault", group.getMonitorSysGenServerGroupIsDefault());
            groupDetail.put("groupSort", group.getMonitorSysGenServerGroupSort());
            groupDetail.put("groupRemark", group.getMonitorSysGenServerGroupRemark());
            groupDetail.put("createTime", group.getCreateTime());
            groupDetail.put("updateTime", group.getUpdateTime());

            // 获取服务器数量
            Integer serverCount = baseMapper.countServersByGroupId(groupId);
            groupDetail.put("serverCount", serverCount != null ? serverCount : 0);

            // 如果需要包含服务器列表
            if (Boolean.TRUE.equals(includeServers)) {
                ReturnResult<List<com.chua.starter.monitor.starter.entity.MonitorSysGenServer>> serversResult = serverService
                        .getServersByGroupId(groupId);
                if (serversResult.isSuccess()) {
                    groupDetail.put("servers", serversResult.getData());

                    // 计算在线服务器数量
                    long onlineCount = serversResult.getData().stream().filter(
                            server -> Integer.valueOf(1).equals(server.getMonitorSysGenServerConnectionStatus()))
                            .count();
                    groupDetail.put("onlineServerCount", (int) onlineCount);
                }
            }

            return ReturnResult.ok(groupDetail);

        } catch (Exception e) {
            log.error("获取分组详细信息失败: groupId={}", groupId, e);
            return ReturnResult.error("获取分组详细信息失败: " + e.getMessage());
        }
    }
}
