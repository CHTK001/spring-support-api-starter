package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorJob;
import com.chua.starter.monitor.job.JobConfig;
import com.chua.starter.monitor.job.JobHelper;
import com.chua.starter.monitor.job.SchedulerTypeEnum;
import com.chua.starter.monitor.mapper.MonitorJobMapper;
import com.chua.starter.monitor.service.MonitorJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
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
        
        // 清除调度任务缓存
        clearScheduleJobCache();
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
        
        // 清除调度任务缓存
        clearScheduleJobCache();
        return ReturnResult.SUCCESS;
    }
    
    // ==================== 覆写父类方法，在增删改时清除缓存 ====================
    
    @Override
    public boolean save(MonitorJob entity) {
        boolean result = super.save(entity);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    @Override
    public boolean saveBatch(Collection<MonitorJob> entityList) {
        boolean result = super.saveBatch(entityList);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    @Override
    public boolean updateById(MonitorJob entity) {
        boolean result = super.updateById(entity);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    @Override
    public boolean updateBatchById(Collection<MonitorJob> entityList) {
        boolean result = super.updateBatchById(entityList);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    @Override
    public boolean removeByIds(Collection<?> idList) {
        boolean result = super.removeByIds(idList);
        if (result) {
            clearScheduleJobCache();
        }
        return result;
    }
    
    /**
     * 清除调度任务查询缓存
     */
    private void clearScheduleJobCache() {
        try {
            JobConfig.getInstance().clearScheduleJobCache();
            log.debug("已清除调度任务查询缓存");
        } catch (Exception e) {
            log.warn("清除调度任务缓存失败: {}", e.getMessage());
        }
    }
}
