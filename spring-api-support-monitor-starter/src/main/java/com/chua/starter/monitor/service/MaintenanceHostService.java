package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceHostRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceHostResponse;

import java.util.List;

/**
 * 维护主机服务接口
 * 提供维护主机的增删改查等功能
 *
 * @author CH
 */
public interface MaintenanceHostService {

    /**
     * 创建维护主机
     *
     * @param request 请求参数
     * @return 创建结果
     */
    ReturnResult<MaintenanceHostResponse> createMaintenanceHost(MaintenanceHostRequest request);

    /**
     * 更新维护主机
     *
     * @param request 请求参数
     * @return 更新结果
     */
    ReturnResult<MaintenanceHostResponse> updateMaintenanceHost(MaintenanceHostRequest request);

    /**
     * 删除维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteMaintenanceHost(Long maintenanceHostId);

    /**
     * 获取维护主机详情
     *
     * @param maintenanceHostId 维护主机ID
     * @return 维护主机详情
     */
    ReturnResult<MaintenanceHostResponse> getMaintenanceHost(Long maintenanceHostId);

    /**
     * 获取维护主机列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 维护主机列表
     */
    ReturnResult<List<MaintenanceHostResponse>> listMaintenanceHosts(Long maintenanceGroupId);

    /**
     * 启用或停用维护主机
     *
     * @param maintenanceHostId 维护主机ID
     * @param enabled           是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> enableMaintenanceHost(Long maintenanceHostId, Boolean enabled);

    /**
     * 测试主机连接
     *
     * @param maintenanceHostId 维护主机ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testConnection(Integer maintenanceHostId);
}