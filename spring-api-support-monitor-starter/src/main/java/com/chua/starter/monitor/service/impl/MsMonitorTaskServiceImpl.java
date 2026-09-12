package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.entity.MsMonitorTask;
import com.chua.starter.monitor.mapper.MsMonitorTaskMapper;
import com.chua.starter.monitor.service.MsMonitorTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 采集任务服务实现，提供采集任务基础 CRUD 与启用/停用能力。
 *
 * @since 2026-08-11
 * @author CH
 */
@Slf4j
@Service
public class MsMonitorTaskServiceImpl extends ServiceImpl<MsMonitorTaskMapper, MsMonitorTask>
        implements MsMonitorTaskService {

    /**
     * 任务运行状态：运行中
     */
    private static final String STATUS_RUNNING = "RUNNING";

    /**
     * 任务运行状态：已停止
     */
    private static final String STATUS_STOPPED = "STOPPED";

    /**
     * 启用标记，值为 1 表示启用
     */
    private static final int ENABLED_FLAG = 1;

    /**
      * 根据监控资产标识查询关联的采集任务列表。
     *
     * @param monitorId 监控资产标识
     * @return 采集任务列表
     */
    @Override
    public List<MsMonitorTask> listByMonitorId(Long monitorId) {
        return list(Wrappers.<MsMonitorTask>lambdaQuery()
                .eq(MsMonitorTask::getMonitorId, monitorId));
    }

    /**
     * 启用或停用采集任务，并同步更新运行状态。
     *
     * @param taskId      任务标识
     * @param enabledFlag 启用状态（1启用/0停用）
     * @return 是否更新成功
     */
    @Override
    public Boolean updateTaskEnabled(Long taskId, Integer enabledFlag) {
        return update(Wrappers.<MsMonitorTask>lambdaUpdate()
                .eq(MsMonitorTask::getId, taskId)
                .set(MsMonitorTask::getTaskEnabled, enabledFlag)
                .set(MsMonitorTask::getTaskStatus,
                        enabledFlag != null && enabledFlag == ENABLED_FLAG ? STATUS_RUNNING : STATUS_STOPPED));
    }
}
