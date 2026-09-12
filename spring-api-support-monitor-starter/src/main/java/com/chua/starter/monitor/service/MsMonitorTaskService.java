package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.entity.MsMonitorTask;

import java.util.List;

/**
 * 采集任务服务接口，提供采集任务基础 CRUD 与启用/停用能力。
 *
 * @since 2026-08-11
 * @author CH
 */
public interface MsMonitorTaskService extends IService<MsMonitorTask> {

    /**
      * 根据监控资产标识查询关联的采集任务列表。
     *
     * @param monitorId 监控资产标识
     * @return 采集任务列表
     */
    List<MsMonitorTask> listByMonitorId(Long monitorId);

    /**
     * 启用或停用采集任务，并同步更新运行状态。
     *
     * @param taskId      任务标识
     * @param enabledFlag 启用状态（1启用/0停用）
     * @return 是否更新成功
     */
    Boolean updateTaskEnabled(Long taskId, Integer enabledFlag);
}
