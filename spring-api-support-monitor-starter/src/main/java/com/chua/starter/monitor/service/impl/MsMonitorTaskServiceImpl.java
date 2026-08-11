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
 * @author CH
 * @since 2026-08-11
 */
@Slf4j
@Service
public class MsMonitorTaskServiceImpl extends ServiceImpl<MsMonitorTaskMapper, MsMonitorTask>
        implements MsMonitorTaskService {

    @Override
    public List<MsMonitorTask> listByMonitorId(Long monitorId) {
        return list(Wrappers.<MsMonitorTask>lambdaQuery()
                .eq(MsMonitorTask::getMonitorId, monitorId));
    }

    @Override
    public Boolean updateTaskEnabled(Long taskId, Integer enabledFlag) {
        return update(Wrappers.<MsMonitorTask>lambdaUpdate()
                .eq(MsMonitorTask::getId, taskId)
                .set(MsMonitorTask::getTaskEnabled, enabledFlag)
                .set(MsMonitorTask::getTaskStatus,
                        enabledFlag != null && enabledFlag == 1 ? "RUNNING" : "STOPPED"));
    }
}
