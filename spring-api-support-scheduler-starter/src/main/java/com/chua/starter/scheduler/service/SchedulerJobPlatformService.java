package com.chua.starter.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.entity.SysJobLog;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;

import java.util.List;

/**
 * 调度平台任务服务。
 */
public interface SchedulerJobPlatformService {

    List<String> namespaces();

    Page<SysJob> pageJobs(String namespace, int pageNum, int pageSize, String jobName, Integer triggerStatus);

    SysJob getJob(String namespace, Integer jobId);

    SysJob getJobByNo(String namespace, String jobNo);

    SysJob createJob(String namespace, SysJob job);

    boolean updateJob(String namespace, Integer jobId, SysJob job);

    boolean updateJobByNo(String namespace, String jobNo, SysJob job);

    boolean deleteJob(String namespace, Integer jobId);

    boolean deleteJobByNo(String namespace, String jobNo);

    boolean restoreJob(String namespace, Integer jobId);

    boolean restoreJobByNo(String namespace, String jobNo);

    boolean restoreJob(String namespace, Integer jobId);

    boolean restoreJobByNo(String namespace, String jobNo);

    boolean startJob(String namespace, Integer jobId);

    boolean startJobByNo(String namespace, String jobNo);

    boolean stopJob(String namespace, Integer jobId);

    boolean stopJobByNo(String namespace, String jobNo);

    boolean triggerJob(String namespace, Integer jobId, String param);

    boolean triggerJobByNo(String namespace, String jobNo, String param);

    Page<SysJobLog> pageLogs(String namespace, int pageNum, int pageSize, String beanName, String executeCode);

    List<SchedulerTaskVO> listTasks(String namespace);

    SchedulerTaskVO updateTask(String namespace, String taskKey, SchedulerTaskUpdateRequest request);

    SchedulerTaskVO triggerTask(String namespace, String taskKey);
}
