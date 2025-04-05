package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceGroup;
import com.chua.starter.monitor.entity.MaintenanceHost;
import com.chua.starter.monitor.entity.MaintenanceScript;
import com.chua.starter.monitor.mapper.MaintenanceGroupMapper;
import com.chua.starter.monitor.mapper.MaintenanceHostMapper;
import com.chua.starter.monitor.mapper.MaintenanceScriptMapper;
import com.chua.starter.monitor.pojo.request.MaintenanceGroupRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceGroupResponse;
import com.chua.starter.monitor.service.MaintenanceGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 维护组服务实现类
 * 提供维护组的增删改查等功能
 *
 * @author CH
 */
@Service
@RequiredArgsConstructor
public class MaintenanceGroupServiceImpl implements MaintenanceGroupService {

    /**
     * 维护组数据访问对象
     */
    private final MaintenanceGroupMapper maintenanceGroupMapper;

    /**
     * 维护主机数据访问对象
     */
    private final MaintenanceHostMapper maintenanceHostMapper;

    /**
     * 维护脚本数据访问对象
     */
    private final MaintenanceScriptMapper maintenanceScriptMapper;

    /**
     * 创建维护组
     *
     * @param request 请求参数
     * @return 创建结果
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> createMaintenanceGroup(MaintenanceGroupRequest request) {
        try {
            MaintenanceGroup maintenanceGroup = new MaintenanceGroup();
            BeanUtils.copyProperties(request, maintenanceGroup);
            maintenanceGroup.setMaintenanceGroupCreateTime(LocalDateTime.now());
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());
            maintenanceGroup.setMaintenanceGroupEnabled(true);

            maintenanceGroupMapper.insert(maintenanceGroup);

            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("创建维护组失败: " + e.getMessage());
        }
    }

    /**
     * 更新维护组
     *
     * @param request 请求参数
     * @return 更新结果
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> updateMaintenanceGroup(MaintenanceGroupRequest request) {
        try {
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(request.getMaintenanceGroupId());
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }

            BeanUtils.copyProperties(request, maintenanceGroup);
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());

            maintenanceGroupMapper.updateById(maintenanceGroup);

            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("更新维护组失败: " + e.getMessage());
        }
    }

    /**
     * 删除维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 删除结果
     */
    @Override
    public ReturnResult<Boolean> deleteMaintenanceGroup(Long maintenanceGroupId) {
        try {
            maintenanceGroupMapper.deleteById(maintenanceGroupId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error("删除维护组失败: " + e.getMessage());
        }
    }

    /**
     * 获取维护组详情
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组详情
     */
    @Override
    public ReturnResult<MaintenanceGroupResponse> getMaintenanceGroup(Long maintenanceGroupId) {
        try {
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(maintenanceGroupId);
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }

            MaintenanceGroupResponse response = new MaintenanceGroupResponse();
            BeanUtils.copyProperties(maintenanceGroup, response);
            
            // 获取主机数量
            Integer hostCount = maintenanceHostMapper.countHostsByGroupId(maintenanceGroupId);
            response.setHostCount(hostCount);
            
            // 获取脚本数量
            Integer scriptCount = maintenanceScriptMapper.countScriptsByGroupId(maintenanceGroupId);
            response.setScriptCount(scriptCount);

            return ReturnResult.success(response);
        } catch (Exception e) {
            return ReturnResult.error("获取维护组失败: " + e.getMessage());
        }
    }

    /**
     * 获取维护组列表
     *
     * @return 维护组列表
     */
    @Override
    public ReturnResult<List<MaintenanceGroupResponse>> listMaintenanceGroups() {
        try {
            List<MaintenanceGroup> maintenanceGroups = maintenanceGroupMapper.selectList(null);

            List<MaintenanceGroupResponse> responses = maintenanceGroups.stream()
                    .map(group -> {
                        MaintenanceGroupResponse response = new MaintenanceGroupResponse();
                        BeanUtils.copyProperties(group, response);
                        
                        // 获取主机数量
                        Integer hostCount = maintenanceHostMapper.countHostsByGroupId(group.getMaintenanceGroupId());
                        response.setHostCount(hostCount);
                        
                        // 获取脚本数量
                        Integer scriptCount = maintenanceScriptMapper.countScriptsByGroupId(group.getMaintenanceGroupId());
                        response.setScriptCount(scriptCount);
                        
                        return response;
                    })
                    .collect(Collectors.toList());

            return ReturnResult.success(responses);
        } catch (Exception e) {
            return ReturnResult.error("获取维护组列表失败: " + e.getMessage());
        }
    }

    /**
     * 启用或停用维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @param enabled            是否启用
     * @return 操作结果
     */
    @Override
    public ReturnResult<Boolean> enableMaintenanceGroup(Long maintenanceGroupId, Boolean enabled) {
        try {
            MaintenanceGroup maintenanceGroup = maintenanceGroupMapper.selectById(maintenanceGroupId);
            if (maintenanceGroup == null) {
                return ReturnResult.error("维护组不存在");
            }

            maintenanceGroup.setMaintenanceGroupEnabled(enabled);
            maintenanceGroup.setMaintenanceGroupUpdateTime(LocalDateTime.now());

            maintenanceGroupMapper.updateById(maintenanceGroup);

            return ReturnResult.success(true);
        } catch (Exception e) {
            return ReturnResult.error((enabled ? "启用" : "停用") + "维护组失败: " + e.getMessage());
        }
    }
} 