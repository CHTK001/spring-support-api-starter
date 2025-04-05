package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MaintenanceTask;
import com.chua.starter.monitor.pojo.request.MaintenanceTaskRequest;

import java.util.List;

/**
 * 维护任务服务
 *
 * @author CH
 */
public interface MaintenanceTaskService {

    /**
     * 创建维护任务
     *
     * @param request 请求参数
     * @return 创建结果
     */
    ReturnResult<MaintenanceTask> createMaintenanceTask(MaintenanceTaskRequest request);

    /**
     * 获取维护任务详情
     *
     * @param maintenanceTaskId 维护任务ID
     * @return 维护任务详情
     */
    ReturnResult<MaintenanceTask> getMaintenanceTask(Integer maintenanceTaskId);

    /**
     * 获取维护任务列表
     *
     * @param request 请求参数
     * @return 维护任务列表
     */
    ReturnResult<List<MaintenanceTask>> listMaintenanceTasks(MaintenanceTaskRequest request);

    /**
     * 执行维护任务
     *
     * @param maintenanceTaskId 维护任务ID
     * @return 执行结果
     */
    ReturnResult<Boolean> executeMaintenanceTask(Integer maintenanceTaskId);

    /**
     * 取消维护任务
     *
     * @param maintenanceTaskId 维护任务ID
     * @return 取消结果
     */
    ReturnResult<Boolean> cancelMaintenanceTask(Integer maintenanceTaskId);

    /**
     * 获取维护任务执行日志
     *
     * @param maintenanceTaskId 维护任务ID
     * @return 执行日志
     */
    ReturnResult<String> getMaintenanceTaskLog(Integer maintenanceTaskId);
}