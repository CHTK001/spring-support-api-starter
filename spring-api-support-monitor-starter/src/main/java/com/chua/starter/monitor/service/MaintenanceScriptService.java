package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.request.MaintenanceScriptRequest;
import com.chua.starter.monitor.pojo.request.RunScriptRequest;
import com.chua.starter.monitor.pojo.response.MaintenanceScriptResponse;
import com.chua.starter.monitor.pojo.response.RunScriptResponse;

import java.util.List;

/**
 * 维护脚本服务
 *
 * @author CH
 */
public interface MaintenanceScriptService {

    /**
     * 创建维护脚本
     *
     * @param request 请求参数
     * @return 创建结果
     */
    ReturnResult<MaintenanceScriptResponse> createMaintenanceScript(MaintenanceScriptRequest request);

    /**
     * 更新维护脚本
     *
     * @param request 请求参数
     * @return 更新结果
     */
    ReturnResult<MaintenanceScriptResponse> updateMaintenanceScript(MaintenanceScriptRequest request);

    /**
     * 删除维护脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteMaintenanceScript(Long maintenanceScriptId);

    /**
     * 获取维护脚本详情
     *
     * @param maintenanceScriptId 脚本ID
     * @return 脚本详情
     */
    ReturnResult<MaintenanceScriptResponse> getMaintenanceScript(Long maintenanceScriptId);

    /**
     * 获取维护脚本列表
     *
     * @param maintenanceGroupId 维护组ID
     * @return 脚本列表
     */
    ReturnResult<List<MaintenanceScriptResponse>> listMaintenanceScripts(Long maintenanceGroupId);

    /**
     * 同步脚本到远程主机
     *
     * @param maintenanceScriptId 脚本ID
     * @return 同步结果
     */
    ReturnResult<Boolean> syncScript(Long maintenanceScriptId);

    /**
     * 运行脚本
     *
     * @param request 请求参数
     * @return 运行结果
     */
    ReturnResult<RunScriptResponse> runScript(RunScriptRequest request);

    /**
     * 启用或停用脚本
     *
     * @param maintenanceScriptId 脚本ID
     * @param status              状态 0-停用 1-启用
     * @return 操作结果
     */
    ReturnResult<Boolean> updateScriptStatus(Long maintenanceScriptId, Integer status);
}