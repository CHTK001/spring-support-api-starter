package com.chua.starter.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.job.support.store.JobLogEntity;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;

import java.util.List;

/**
* 调度平台任务服务。
*
* <p>平台基于 job-starter 新架构 (SCHEDULER 角色) 提供统一任务管理能力,
* 直接读写 {@code sys_job / sys_job_log} 表, 由 作业-启动 的 铅 循环扫描调度。</p>
*
* @since 4.0.0.42
* @author CH
 */
public interface SchedulerJobPlatformService {

    /**
    * 分页查询任务。
    *
    * @param pageNum        页码
    * @param pageSize       每页大小
    * @param jobName        任务名模糊查询
    * @param triggerStatus  启用状态
    * @param group          任务分组
    * @return 任务分页
     */
    Page<JobEntity> pageJobs(int pageNum, int pageSize, String jobName, Integer triggerStatus, String group);

    /**
    * 查询任务详情。
    *
    * @param jobId 任务 标识
    * @return 任务实体
     */
    JobEntity getJob(Long jobId);

    /**
    * 按任务编号查询任务详情。
    *
    * @param jobNo 任务编号
    * @return 任务实体
     */
    JobEntity getJobByNo(String jobNo);

    /**
    * 创建任务。
    *
    * @param job 任务实体
    * @return 创建后的任务
     */
    JobEntity createJob(JobEntity job);

    /**
    * 更新任务。
    *
    * @param jobId 任务 标识
    * @param job   任务实体
    * @return 是否更新成功
     */
    boolean updateJob(Long jobId, JobEntity job);

    /**
    * 按任务编号更新任务。
    *
    * @param jobNo 任务编号
    * @param job   任务实体
    * @return 是否更新成功
     */
    boolean updateJobByNo(String jobNo, JobEntity job);

    /**
    * 删除任务 (逻辑删除)。
    *
    * @param jobId 任务 标识
    * @return 是否删除成功
     */
    boolean deleteJob(Long jobId);

    /**
    * 按任务编号删除任务 (逻辑删除)。
    *
    * @param jobNo 任务编号
    * @return 是否删除成功
     */
    boolean deleteJobByNo(String jobNo);

    /**
    * 恢复已删除任务。
    *
    * @param jobId 任务 标识
    * @return 是否恢复成功
     */
    boolean restoreJob(Long jobId);

    /**
    * 按任务编号恢复已删除任务。
    *
    * @param jobNo 任务编号
    * @return 是否恢复成功
     */
    boolean restoreJobByNo(String jobNo);

    /**
    * 启动任务。
    *
    * @param jobId 任务 标识
    * @return 是否启动成功
     */
    boolean startJob(Long jobId);

    /**
    * 按任务编号启动任务。
    *
    * @param jobNo 任务编号
    * @return 是否启动成功
     */
    boolean startJobByNo(String jobNo);

    /**
    * 停止任务。
    *
    * @param jobId 任务 标识
    * @return 是否停止成功
     */
    boolean stopJob(Long jobId);

    /**
    * 按任务编号停止任务。
    *
    * @param jobNo 任务编号
    * @return 是否停止成功
     */
    boolean stopJobByNo(String jobNo);

    /**
    * 立即执行任务。
    *
    * @param jobId 任务 标识
    * @param param 执行参数
    * @return 是否触发成功
     */
    boolean triggerJob(Long jobId, String param);

    /**
    * 按任务编号立即执行任务。
    *
    * @param jobNo 任务编号
    * @param param 执行参数
    * @return 是否触发成功
     */
    boolean triggerJobByNo(String jobNo, String param);

    /**
    * 清理指定天数前的日志记录和对应的日志文件。
    *
    * @param retentionDays 保留天数
    * @return 清理的日志记录数
     */
    int cleanupLogs(int retentionDays);

    /**
    * 分页查询任务日志。
    *
    * @param pageNum      页码
    * @param pageSize     每页大小
    * @param beanName     处理器 名称模糊查询
    * @param executeCode  执行状态码
    * @param group        任务分组
    * @return 日志分页
     */
    Page<JobLogEntity> pageLogs(int pageNum, int pageSize, String beanName, String executeCode, String group);

    /**
    * 查询平台任务视图。
    *
    * @param group          任务分组
    * @param includeDeleted 是否包含已删除
    * @return 任务视图列表
     */
    List<SchedulerTaskVO> listTasks(String group, boolean includeDeleted);

    /**
    * 更新平台任务。
    *
    * @param taskKey 任务标识 (编号/执行 Bean/名称)
    * @param request 更新请求
    * @return 更新后的任务视图
     */
    SchedulerTaskVO updateTask(String taskKey, SchedulerTaskUpdateRequest request);

    /**
    * 触发平台任务。
    *
    * @param taskKey 任务标识 (编号/执行 Bean/名称)
    * @return 触发后的任务视图
     */
    SchedulerTaskVO triggerTask(String taskKey);
}
