package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorJob;
import com.chua.starter.monitor.job.JobHelper;
import com.chua.starter.monitor.job.SchedulerTypeEnum;
import com.chua.starter.monitor.mapper.MonitorJobMapper;
import com.chua.starter.monitor.service.job.MonitorJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 监控任务服务实现类
 *
 * @author CH
 * @since 2024/03/08
 */
@Service
@Slf4j
public class MonitorJobServiceImpl extends ServiceImpl<MonitorJobMapper, MonitorJob> implements MonitorJobService {

    @Override
    public ReturnResult<String> stop(int jobId) {
        MonitorJob monitorJob = baseMapper.selectById(jobId);

        monitorJob.setJobTriggerStatus(0);
        monitorJob.setJobTriggerLastTime(0L);
        monitorJob.setJobTriggerNextTime(0L);

        monitorJob.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(monitorJob);
        return ReturnResult.SUCCESS;
    }

    @Override
    public ReturnResult<String> start(int jobId) {
        MonitorJob monitorJob = baseMapper.selectById(jobId);

        // valid
        SchedulerTypeEnum scheduleTypeEnum = SchedulerTypeEnum.match(monitorJob.getJobScheduleType(), SchedulerTypeEnum.NONE);
        if (SchedulerTypeEnum.NONE == scheduleTypeEnum) {
            return ReturnResult.illegal("限制启动");
        }

        // next trigger time (5s后生效，避开预读周期)
        long nextTriggerTime = 0;
        try {
            Date nextValidTime = JobHelper.generateNextValidTime(monitorJob, new Date(System.currentTimeMillis() + JobHelper.PRE_READ_MS));
            if (nextValidTime == null) {
                return ReturnResult.illegal("限制启动");
            }
            nextTriggerTime = nextValidTime.getTime();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ReturnResult.illegal("限制启动");
        }

        monitorJob.setJobTriggerStatus(1);
        monitorJob.setJobTriggerLastTime(0L);
        monitorJob.setJobTriggerNextTime(nextTriggerTime);

        monitorJob.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(monitorJob);
        return ReturnResult.SUCCESS;
    }
}
