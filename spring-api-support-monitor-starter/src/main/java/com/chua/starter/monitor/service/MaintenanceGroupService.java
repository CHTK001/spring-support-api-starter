package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceGroupRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceGroupResponse;

import java.util.List;

/**
 * 维护组服务
 *
 * @author CH
 */
public interface MaintenanceGroupService {

    /**
     * 创建维护组
     *
     * @param request 请求参数
     * @return 创建结果
     */
    ReturnResult<MaintenanceGroupResponse> createMaintenanceGroup(MaintenanceGroupRequest request);

    /**
     * 更新维护组
     *
     * @param request 请求参数
     * @return 更新结果
     */
    ReturnResult<MaintenanceGroupResponse> updateMaintenanceGroup(MaintenanceGroupRequest request);

    /**
     * 删除维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteMaintenanceGroup(Long maintenanceGroupId);

    /**
     * 获取维护组详情
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护组详情
     */
    ReturnResult<MaintenanceGroupResponse> getMaintenanceGroup(Long maintenanceGroupId);

    /**
     * 获取维护组列表
     *
     * @return 维护组列表
     */
    ReturnResult<List<MaintenanceGroupResponse>> listMaintenanceGroups();

    /**
     * 启用或停用维护组
     *
     * @param maintenanceGroupId 维护组ID
     * @param enabled            是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> enableMaintenanceGroup(Long maintenanceGroupId, Boolean enabled);
}