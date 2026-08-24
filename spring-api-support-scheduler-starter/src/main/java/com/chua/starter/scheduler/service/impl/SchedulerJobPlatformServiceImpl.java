package com.chua.starter.scheduler.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.task.scheduler.CronExpression;
import com.chua.starter.job.support.JobProperties;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.job.support.store.JobLogEntity;
import com.chua.starter.job.support.store.JobLogMapper;
import com.chua.starter.job.support.store.JobMapper;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.mapper.SchedulerJobMapper;
import com.chua.starter.scheduler.service.SchedulerJobPlatformService;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 调度平台任务服务实现。
 *
 * <p>平台基于 job-starter 新架构 (SCHEDULER 角色) 提供统一任务管理能力,
 * 通过 MyBatis-Plus 的 {@link JobMapper} / {@link JobLogMapper} 直接读写
 * {@code sys_job / sys_job_log} 表。任务扫描与远程派发由 job-starter 的
 * Leader 循环 + 长链 Sync 完成, 平台不再维护自研轮询/HTTP 派发。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SchedulerJobPlatformServiceImpl implements SchedulerJobPlatformService, SchedulingConfigurer {

    /**
     * 最大分页大小
     */
    private static final int MAX_PAGE_SIZE = 200;

    /**
     * 停止状态
     */
    private static final int STATUS_STOP = 0;

    /**
     * 运行状态
     */
    private static final int STATUS_RUNNING = 1;

    /**
     * 默认调度类型
     */
    private static final String DEFAULT_SCHEDULE_TYPE = "CRON";

    /**
     * 默认 Glue 类型
     */
    private static final String DEFAULT_GLUE_TYPE = "BEAN";

    /**
     * 默认错失触发策略
     */
    private static final String DEFAULT_MISFIRE_STRATEGY = "DO_NOTHING";

    /**
     * 任务 Mapper
     */
    private final JobMapper jobMapper;

    /**
     * 日志 Mapper
     */
    private final JobLogMapper jobLogMapper;

    /**
     * 任务配置
     */
    private final JobProperties jobProperties;

    /**
     * 调度任务 Mapper
     */
    private final SchedulerJobMapper schedulerJobMapper;

    /**
     * 分页查询任务。
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param jobName       任务名模糊查询
     * @param triggerStatus 启用状态
     * @param group         任务分组
     * @return 任务分页结果
     */
    @Override
    public Page<JobEntity> pageJobs(int pageNum, int pageSize, String jobName, Integer triggerStatus, String group) {
        int safePageNum = normalizePageNum(pageNum);
        int safePageSize = normalizePageSize(pageSize);
        String normalizedGroup = trimToNull(group);

        var wrapper = Wrappers.<JobEntity>lambdaQuery()
                .eq(JobEntity::getSysJobIsDeleted, 0)
                .and(StringUtils.hasText(normalizedGroup), w -> w
                        .isNull(JobEntity::getSysJobGroup)
                        .or()
                        .eq(JobEntity::getSysJobGroup, normalizedGroup))
                .like(StringUtils.hasText(trimToNull(jobName)), JobEntity::getSysJobName, trimToNull(jobName))
                .eq(triggerStatus != null, JobEntity::getSysJobTriggerStatus, triggerStatus)
                .orderByDesc(JobEntity::getSysJobId);
        return jobMapper.selectPage(new Page<>(safePageNum, safePageSize), wrapper);
    }

    /**
     * 查询任务详情。
     *
     * @param jobId 任务 ID
     * @return 任务实体
     */
    @Override
    public JobEntity getJob(Long jobId) {
        JobEntity job = jobMapper.selectById(requireJobId(jobId));
        if (job == null || Integer.valueOf(1).equals(job.getSysJobIsDeleted())) {
            throw new IllegalArgumentException("任务不存在: " + jobId);
        }
        return job;
    }

    /**
     * 按任务编号查询任务详情。
     *
     * @param jobNo 任务编号
     * @return 任务实体
     */
    @Override
    public JobEntity getJobByNo(String jobNo) {
        JobEntity job = findByNo(requireText(jobNo, "任务编号不能为空"));
        if (job == null) {
            throw new IllegalArgumentException("任务不存在: " + jobNo);
        }
        return job;
    }

    /**
     * 创建任务, 自动补齐默认值并做校验。
     *
     * @param job 任务实体
     * @return 创建后的任务实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobEntity createJob(JobEntity job) {
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        JobEntity target = new JobEntity();
        BeanUtils.copyProperties(job, target);
        applyDefaults(target);
        normalizeJob(target);
        validateJob(target);
        applyTriggerState(target, STATUS_RUNNING == safeStatus(target.getSysJobTriggerStatus()));

        jobMapper.insert(target);
        JobEntity saved = jobMapper.selectById(target.getSysJobId());
        if (saved == null) {
            throw new IllegalStateException("创建任务失败");
        }
        return saved;
    }

    /**
     * 更新任务, 按传入字段进行局部合并。
     *
     * @param jobId 任务 ID
     * @param job   任务实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(Long jobId, JobEntity job) {
        Long safeJobId = requireJobId(jobId);
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        JobEntity existing = getJob(safeJobId);
        mergeJobPatch(existing, job);
        normalizeJob(existing);
        validateJob(existing);
        applyTriggerState(existing, STATUS_RUNNING == safeStatus(existing.getSysJobTriggerStatus()));
        return jobMapper.updateById(existing) > 0;
    }

    /**
     * 按任务编号更新任务。
     *
     * @param jobNo 任务编号
     * @param job   任务实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobByNo(String jobNo, JobEntity job) {
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        JobEntity existing = getJobByNo(jobNo);
        mergeJobPatch(existing, job);
        normalizeJob(existing);
        validateJob(existing);
        applyTriggerState(existing, STATUS_RUNNING == safeStatus(existing.getSysJobTriggerStatus()));
        return jobMapper.updateById(existing) > 0;
    }

    /**
     * 删除任务 (逻辑删除), 先停止任务再置删除标记。
     *
     * @param jobId 任务 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long jobId) {
        Long safeJobId = requireJobId(jobId);
        stopJob(safeJobId);
        return jobMapper.update(null, Wrappers.<JobEntity>lambdaUpdate()
                .eq(JobEntity::getSysJobId, safeJobId)
                .set(JobEntity::getSysJobIsDeleted, 1)) > 0;
    }

    /**
     * 按任务编号删除任务 (逻辑删除)。
     *
     * @param jobNo 任务编号
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJobByNo(String jobNo) {
        String safeJobNo = requireText(jobNo, "任务编号不能为空");
        stopJobByNo(safeJobNo);
        return jobMapper.update(null, Wrappers.<JobEntity>lambdaUpdate()
                .eq(JobEntity::getSysJobNo, safeJobNo)
                .set(JobEntity::getSysJobIsDeleted, 1)) > 0;
    }

    /**
     * 恢复已删除任务。
     *
     * @param jobId 任务 ID
     * @return 是否恢复成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreJob(Long jobId) {
        log.info("恢复任务: jobId={}", jobId);
        return jobMapper.update(null, Wrappers.<JobEntity>lambdaUpdate()
                .eq(JobEntity::getSysJobId, requireJobId(jobId))
                .set(JobEntity::getSysJobIsDeleted, 0)) > 0;
    }

    /**
     * 按任务编号恢复已删除任务。
     *
     * @param jobNo 任务编号
     * @return 是否恢复成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreJobByNo(String jobNo) {
        log.info("恢复任务: jobNo={}", jobNo);
        return jobMapper.update(null, Wrappers.<JobEntity>lambdaUpdate()
                .eq(JobEntity::getSysJobNo, requireText(jobNo, "任务编号不能为空"))
                .set(JobEntity::getSysJobIsDeleted, 0)) > 0;
    }

    /**
     * 启动任务。
     *
     * @param jobId 任务 ID
     * @return 是否启动成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startJob(Long jobId) {
        JobEntity job = getJob(requireJobId(jobId));
        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, true);
        return jobMapper.updateById(job) > 0;
    }

    /**
     * 按任务编号启动任务。
     *
     * @param jobNo 任务编号
     * @return 是否启动成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startJobByNo(String jobNo) {
        JobEntity job = getJobByNo(jobNo);
        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, true);
        return jobMapper.updateById(job) > 0;
    }

    /**
     * 停止任务。
     *
     * @param jobId 任务 ID
     * @return 是否停止成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopJob(Long jobId) {
        JobEntity job = getJob(requireJobId(jobId));
        applyTriggerState(job, false);
        return jobMapper.updateById(job) > 0;
    }

    /**
     * 按任务编号停止任务。
     *
     * @param jobNo 任务编号
     * @return 是否停止成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopJobByNo(String jobNo) {
        JobEntity job = getJobByNo(jobNo);
        applyTriggerState(job, false);
        return jobMapper.updateById(job) > 0;
    }

    /**
     * 立即执行任务。
     *
     * @param jobId 任务 ID
     * @param param 执行参数
     * @return 是否触发成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerJob(Long jobId, String param) {
        JobEntity job = getJob(requireJobId(jobId));
        return trigger(job, param);
    }

    /**
     * 按任务编号立即执行任务。
     *
     * @param jobNo 任务编号
     * @param param 执行参数
     * @return 是否触发成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerJobByNo(String jobNo, String param) {
        JobEntity job = getJobByNo(jobNo);
        return trigger(job, param);
    }

    /**
     * 清理指定天数前的日志记录和对应的日志文件。
     *
     * @param retentionDays 保留天数
     * @return 清理的日志记录数
     */
    /**
     * 一天的毫秒数
     */
    private static final long MILLIS_PER_DAY = 86400000L;

    /**
     * 清理指定天数前的日志记录和对应的日志文件。
     *
     * @param retentionDays 保留天数
     * @return 清理的日志记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupLogs(int retentionDays) {
        if (retentionDays <= 0) {
            throw new IllegalArgumentException("保留天数必须大于0");
        }
        long cutoff = System.currentTimeMillis() - retentionDays * MILLIS_PER_DAY;

        // 1. 查出待删除的日志文件路径
        var files = jobLogMapper.selectList(Wrappers.<JobLogEntity>lambdaQuery()
                .select(JobLogEntity::getSysJobLogFilePath)
                .isNotNull(JobLogEntity::getSysJobLogFilePath)
                .le(JobLogEntity::getSysJobLogTriggerTime, new Date(cutoff))
                .last("AND sys_job_log_file_path != ''"));
        if (files != null && !files.isEmpty()) {
            Path base = Paths.get(jobProperties.getLog().getBasePath()).toAbsolutePath().normalize();
            for (JobLogEntity f : files) {
                if (f.getSysJobLogFilePath() == null || f.getSysJobLogFilePath().isBlank()) {
                    continue;
                }
                try {
                    Path file = base.resolve(f.getSysJobLogFilePath()).normalize();
                    if (file.startsWith(base) && Files.exists(file)) {
                        Files.delete(file);
                    }
                } catch (IOException e) {
                    log.warn("[job] 删除日志文件失败 path={} cause={}", f.getSysJobLogFilePath(), e.getMessage());
                }
            }
            // 清理空目录
            try (var stream = Files.walk(base)) {
                var dirs = stream.filter(Files::isDirectory)
                        .sorted((a, b) -> -a.compareTo(b))
                        .toList();
                for (Path dir : dirs) {
                    try (var list = Files.list(dir)) {
                        if (list.findAny().isEmpty()) {
                            Files.delete(dir);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("[job] 清理空目录失败 cause={}", e.getMessage());
            }
        }

        // 2. 删除 DB 记录
        int deleted = jobLogMapper.deleteByTriggerTimeBefore(new Date(cutoff));
        log.info("[job] 日志清理完成 retentionDays={} deleted={}", retentionDays, deleted);
        return deleted;
    }

    /**
     * 分页查询任务日志。
     *
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param beanName     handler 名称模糊查询
     * @param executeCode  执行状态码
     * @param group        任务分组
     * @return 日志分页结果
     */
    @Override
    public Page<JobLogEntity> pageLogs(int pageNum, int pageSize, String beanName, String executeCode, String group) {
        int safePageNum = normalizePageNum(pageNum);
        int safePageSize = normalizePageSize(pageSize);
        String normalizedBeanName = trimToNull(beanName);
        String normalizedExecuteCode = trimToNull(executeCode);
        String normalizedGroup = trimToNull(group);

        var wrapper = Wrappers.<JobLogEntity>lambdaQuery()
                .eq(JobLogEntity::getSysJobLogIsDeleted, 0)
                .like(StringUtils.hasText(normalizedBeanName), JobLogEntity::getSysJobLogTriggerBean, normalizedBeanName)
                .eq(StringUtils.hasText(normalizedExecuteCode), JobLogEntity::getSysJobLogExecuteCode,
                        normalizedExecuteCode == null ? null : normalizedExecuteCode.toUpperCase(java.util.Locale.ROOT))
                .and(StringUtils.hasText(normalizedGroup), w -> w
                        .isNull(JobLogEntity::getSysJobNo)
                        .or()
                        .in(JobLogEntity::getSysJobNo,
                                schedulerJobMapper.selectJobNosByGroup(normalizedGroup)))
                .orderByDesc(JobLogEntity::getSysJobLogId);
        return jobLogMapper.selectPage(new Page<>(safePageNum, safePageSize), wrapper);
    }

    /**
     * 查询平台任务视图列表。
     *
     * @param group          任务分组
     * @param includeDeleted 是否包含已删除
     * @return 任务视图列表
     */
    @Override
    public List<SchedulerTaskVO> listTasks(String group, boolean includeDeleted) {
        String normalizedGroup = trimToNull(group);
        List<JobEntity> jobs = jobMapper.selectList(Wrappers.<JobEntity>lambdaQuery()
                .eq(!includeDeleted, JobEntity::getSysJobIsDeleted, 0)
                .and(StringUtils.hasText(normalizedGroup), w -> w
                        .isNull(JobEntity::getSysJobGroup)
                        .or()
                        .eq(JobEntity::getSysJobGroup, normalizedGroup))
                .orderByDesc(JobEntity::getSysJobId));
        return jobs.stream().map(this::toTaskVO).toList();
    }

    /**
     * 更新平台任务。
     *
     * @param taskKey 任务标识 (编号/执行 Bean/名称)
     * @param request 更新请求
     * @return 更新后的任务视图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchedulerTaskVO updateTask(String taskKey, SchedulerTaskUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("任务更新内容不能为空");
        }

        JobEntity job = requireTask(taskKey);
        if (StringUtils.hasText(request.getCronExpression())) {
            job.setSysJobScheduleType(DEFAULT_SCHEDULE_TYPE);
            job.setSysJobScheduleTime(request.getCronExpression().trim());
        }
        if (request.getEnabled() != null) {
            job.setSysJobTriggerStatus(Boolean.TRUE.equals(request.getEnabled()) ? STATUS_RUNNING : STATUS_STOP);
        }

        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, STATUS_RUNNING == safeStatus(job.getSysJobTriggerStatus()));
        jobMapper.updateById(job);
        return toTaskVO(requireTask(taskKey));
    }

    /**
     * 触发平台任务。
     *
     * @param taskKey 任务标识 (编号/执行 Bean/名称)
     * @return 触发后的任务视图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchedulerTaskVO triggerTask(String taskKey) {
        JobEntity job = requireTask(taskKey);
        trigger(job, job.getSysJobExecuteParam());
        return toTaskVO(requireTask(taskKey));
    }

    /**
     * 触发任务, 将下次执行时间置为当前时间以便立即调度。
     *
     * @param job   任务实体
     * @param param 执行参数
     * @return 是否触发成功
     */
    private boolean trigger(JobEntity job, String param) {
        if (param != null) {
            job.setSysJobExecuteParam(param);
        }
        job.setSysJobTriggerNextTime(System.currentTimeMillis());
        if (job.getSysJobTriggerLastTime() == null) {
            job.setSysJobTriggerLastTime(0L);
        }
        return jobMapper.updateById(job) > 0;
    }

    /**
     * 将任务实体转换为平台任务视图。
     *
     * @param job 任务实体
     * @return 任务视图
     */
    private SchedulerTaskVO toTaskVO(JobEntity job) {
        SchedulerTaskVO vo = new SchedulerTaskVO();
        vo.setJobId(job.getSysJobId());
        vo.setJobNo(job.getSysJobNo());
        vo.setTaskKey(StringUtils.hasText(job.getSysJobExecuteBean())
                ? job.getSysJobExecuteBean()
                : job.getSysJobName());
        vo.setTaskName(job.getSysJobName());
        vo.setScheduleType(job.getSysJobScheduleType());
        vo.setCronExpression(job.getSysJobScheduleTime());
        vo.setEnabled(STATUS_RUNNING == safeStatus(job.getSysJobTriggerStatus()));
        vo.setDescription(job.getSysJobDesc());
        vo.setScheduled(STATUS_RUNNING == safeStatus(job.getSysJobTriggerStatus()));
        vo.setDeleted(Integer.valueOf(1).equals(job.getSysJobIsDeleted()));
        vo.setDispatchMode(job.getSysJobDispatchMode());
        vo.setRemoteExecutorAddress(job.getSysJobRemoteExecutorAddress());
        vo.setStorageMode(job.getSysJobStorageMode());
        vo.setNextExecutionTime(toLocalDateTime(job.getSysJobTriggerNextTime()));

        JobLogEntity latest = findLatestLog(job);
        if (latest != null) {
            vo.setLastStartedAt(toLocalDateTime(latest.getSysJobLogTriggerTime()));
            vo.setLastFinishedAt(toLocalDateTime(latest.getSysJobLogTriggerTime(), latest.getSysJobLogCost()));
            vo.setLastRunStatus(latest.getSysJobLogExecuteCode());
            vo.setLastRunMessage(latest.getSysJobLogTriggerMsg());
        }
        return vo;
    }

    /**
     * 查找任务最近一次执行日志。
     *
     * @param job 任务实体
     * @return 最近一次执行日志, 不存在返回 null
     */
    private JobLogEntity findLatestLog(JobEntity job) {
        if (StringUtils.hasText(trimToNull(job.getSysJobNo()))) {
            JobLogEntity byNo = findLatestLogByNo(job.getSysJobNo());
            if (byNo != null) {
                return byNo;
            }
        }
        String triggerBean = trimToNull(job.getSysJobExecuteBean());
        if (StringUtils.hasText(triggerBean)) {
            JobLogEntity byBean = findLatestLogByBean(triggerBean);
            if (byBean != null) {
                return byBean;
            }
        }
        return null;
    }

    /**
     * 按任务编号查找最近一次执行日志。
     *
     * @param jobNo 任务编号
     * @return 最近一次执行日志, 不存在返回 null
     */
    private JobLogEntity findLatestLogByNo(String jobNo) {
        return jobLogMapper.selectOne(Wrappers.<JobLogEntity>lambdaQuery()
                .eq(JobLogEntity::getSysJobNo, jobNo)
                .orderByDesc(JobLogEntity::getSysJobLogTriggerTime)
                .orderByDesc(JobLogEntity::getSysJobLogId)
                .last("LIMIT 1"));
    }

    /**
     * 按执行 Bean 名称查找最近一次执行日志。
     *
     * @param triggerBean 执行 Bean 名称
     * @return 最近一次执行日志, 不存在返回 null
     */
    private JobLogEntity findLatestLogByBean(String triggerBean) {
        return jobLogMapper.selectOne(Wrappers.<JobLogEntity>lambdaQuery()
                .eq(JobLogEntity::getSysJobLogTriggerBean, triggerBean)
                .orderByDesc(JobLogEntity::getSysJobLogTriggerTime)
                .orderByDesc(JobLogEntity::getSysJobLogId)
                .last("LIMIT 1"));
    }

    /**
     * 按任务编号查找未删除的任务。
     *
     * @param jobNo 任务编号
     * @return 任务实体, 不存在返回 null
     */
    private JobEntity findByNo(String jobNo) {
        return jobMapper.selectOne(Wrappers.<JobEntity>lambdaQuery()
                .eq(JobEntity::getSysJobNo, jobNo)
                .eq(JobEntity::getSysJobIsDeleted, 0)
                .last("LIMIT 1"));
    }

    /**
     * 按任务标识 (编号/执行 Bean/名称) 解析任务。
     *
     * @param taskKey 任务标识
     * @return 已存在的任务实体
     */
    private JobEntity requireTask(String taskKey) {
        String normalizedTaskKey = requireText(taskKey, "任务标识不能为空");

        JobEntity byNo = jobMapper.selectOne(Wrappers.<JobEntity>lambdaQuery()
                .eq(JobEntity::getSysJobNo, normalizedTaskKey)
                .eq(JobEntity::getSysJobIsDeleted, 0)
                .orderByDesc(JobEntity::getSysJobId)
                .last("LIMIT 1"));
        if (byNo != null) {
            return byNo;
        }

        JobEntity byBean = jobMapper.selectOne(Wrappers.<JobEntity>lambdaQuery()
                .eq(JobEntity::getSysJobExecuteBean, normalizedTaskKey)
                .eq(JobEntity::getSysJobIsDeleted, 0)
                .orderByDesc(JobEntity::getSysJobId)
                .last("LIMIT 1"));
        if (byBean != null) {
            return byBean;
        }

        JobEntity byName = jobMapper.selectOne(Wrappers.<JobEntity>lambdaQuery()
                .eq(JobEntity::getSysJobName, normalizedTaskKey)
                .eq(JobEntity::getSysJobIsDeleted, 0)
                .orderByDesc(JobEntity::getSysJobId)
                .last("LIMIT 1"));
        if (byName != null) {
            return byName;
        }
        throw new IllegalArgumentException("任务不存在: " + normalizedTaskKey);
    }

    /**
     * 将补丁字段局部合并到目标任务。
     *
     * @param target 目标任务实体
     * @param patch  补丁任务实体
     */
    private void mergeJobPatch(JobEntity target, JobEntity patch) {
        if (patch.getSysJobName() != null) {
            target.setSysJobName(patch.getSysJobName());
        }
        if (patch.getSysJobScheduleType() != null) {
            target.setSysJobScheduleType(patch.getSysJobScheduleType());
        }
        if (patch.getSysJobScheduleTime() != null) {
            target.setSysJobScheduleTime(patch.getSysJobScheduleTime());
        }
        if (patch.getSysJobAuthor() != null) {
            target.setSysJobAuthor(patch.getSysJobAuthor());
        }
        if (patch.getSysJobAlarmEmail() != null) {
            target.setSysJobAlarmEmail(patch.getSysJobAlarmEmail());
        }
        if (patch.getSysJobTriggerStatus() != null) {
            target.setSysJobTriggerStatus(patch.getSysJobTriggerStatus());
        }
        if (patch.getSysJobDesc() != null) {
            target.setSysJobDesc(patch.getSysJobDesc());
        }
        if (patch.getSysJobGlueUpdatetime() != null) {
            target.setSysJobGlueUpdatetime(patch.getSysJobGlueUpdatetime());
        }
        if (patch.getSysJobGlueSource() != null) {
            target.setSysJobGlueSource(patch.getSysJobGlueSource());
        }
        if (patch.getSysJobGlueType() != null) {
            target.setSysJobGlueType(patch.getSysJobGlueType());
        }
        if (patch.getSysJobDispatchMode() != null) {
            target.setSysJobDispatchMode(patch.getSysJobDispatchMode());
        }
        if (patch.getSysJobRemoteExecutorAddress() != null) {
            target.setSysJobRemoteExecutorAddress(patch.getSysJobRemoteExecutorAddress());
        }
        if (patch.getSysJobStorageMode() != null) {
            target.setSysJobStorageMode(patch.getSysJobStorageMode());
        }
        if (patch.getSysJobFailRetry() != null) {
            target.setSysJobFailRetry(patch.getSysJobFailRetry());
        }
        if (patch.getSysJobRetryInterval() != null) {
            target.setSysJobRetryInterval(patch.getSysJobRetryInterval());
        }
        if (patch.getSysJobExecuteTimeout() != null) {
            target.setSysJobExecuteTimeout(patch.getSysJobExecuteTimeout());
        }
        if (patch.getSysJobExecuteBean() != null) {
            target.setSysJobExecuteBean(patch.getSysJobExecuteBean());
        }
        if (patch.getSysJobExecuteParam() != null) {
            target.setSysJobExecuteParam(patch.getSysJobExecuteParam());
        }
        if (patch.getSysJobExceptionCallbackBean() != null) {
            target.setSysJobExceptionCallbackBean(patch.getSysJobExceptionCallbackBean());
        }
        if (patch.getSysJobRetryCallbackBean() != null) {
            target.setSysJobRetryCallbackBean(patch.getSysJobRetryCallbackBean());
        }
        if (patch.getSysJobExecuteMisfireStrategy() != null) {
            target.setSysJobExecuteMisfireStrategy(patch.getSysJobExecuteMisfireStrategy());
        }
        if (patch.getSysJobGroup() != null) {
            target.setSysJobGroup(patch.getSysJobGroup());
        }
    }

    /**
     * 为任务补齐默认值。
     *
     * @param job 任务实体
     */
    private void applyDefaults(JobEntity job) {
        if (!StringUtils.hasText(job.getSysJobNo())) {
            job.setSysJobNo(nextJobNo());
        }
        if (job.getSysJobScheduleType() == null) {
            job.setSysJobScheduleType(DEFAULT_SCHEDULE_TYPE);
        }
        if (job.getSysJobGlueType() == null) {
            job.setSysJobGlueType(DEFAULT_GLUE_TYPE);
        }
        if (job.getSysJobGlueUpdatetime() == null) {
            job.setSysJobGlueUpdatetime(new Date());
        }
        if (job.getSysJobTriggerStatus() == null) {
            job.setSysJobTriggerStatus(STATUS_STOP);
        }
        if (job.getSysJobFailRetry() == null) {
            job.setSysJobFailRetry(0);
        }
        if (job.getSysJobRetryInterval() == null) {
            job.setSysJobRetryInterval(0);
        }
        if (job.getSysJobExecuteTimeout() == null) {
            job.setSysJobExecuteTimeout(0);
        }
        if (job.getSysJobDispatchMode() == null) {
            job.setSysJobDispatchMode("LOCAL");
        }
        if (job.getSysJobStorageMode() == null) {
            job.setSysJobStorageMode("DATABASE");
        }
        if (job.getSysJobExecuteMisfireStrategy() == null) {
            job.setSysJobExecuteMisfireStrategy(DEFAULT_MISFIRE_STRATEGY);
        }
        if (job.getSysJobTriggerLastTime() == null) {
            job.setSysJobTriggerLastTime(0L);
        }
        if (job.getSysJobTriggerNextTime() == null) {
            job.setSysJobTriggerNextTime(0L);
        }
    }

    /**
     * 校验任务关键字段合法性。
     *
     * @param job 任务实体
     */
    private void validateJob(JobEntity job) {
        requireText(job.getSysJobNo(), "任务编号不能为空");
        requireText(job.getSysJobName(), "任务名称不能为空");
        if ("BEAN".equalsIgnoreCase(job.getSysJobGlueType())) {
            requireText(job.getSysJobExecuteBean(), "执行 Bean 名称不能为空");
        }
        if (!"NONE".equalsIgnoreCase(job.getSysJobScheduleType())) {
            requireText(job.getSysJobScheduleTime(), "调度配置不能为空");
        }
    }

    /**
     * 规范化任务字段, 去除空白并统一枚举字符串。
     *
     * @param job 任务实体
     */
    private void normalizeJob(JobEntity job) {
        applyDefaults(job);
        job.setSysJobNo(trimToNull(job.getSysJobNo()));
        job.setSysJobName(trimToNull(job.getSysJobName()));
        job.setSysJobExecuteBean(trimToNull(job.getSysJobExecuteBean()));
        job.setSysJobScheduleTime(trimToNull(job.getSysJobScheduleTime()));
        job.setSysJobAuthor(trimToNull(job.getSysJobAuthor()));
        job.setSysJobAlarmEmail(trimToNull(job.getSysJobAlarmEmail()));
        job.setSysJobDesc(trimToNull(job.getSysJobDesc()));
        job.setSysJobGlueSource(trimToNull(job.getSysJobGlueSource()));
        job.setSysJobGlueType(trimToNull(job.getSysJobGlueType()));
        job.setSysJobDispatchMode(normalizeDispatchMode(job.getSysJobDispatchMode()));
        job.setSysJobRemoteExecutorAddress(trimToNull(job.getSysJobRemoteExecutorAddress()));
        job.setSysJobStorageMode(normalizeStorageMode(job.getSysJobStorageMode()));
        job.setSysJobExceptionCallbackBean(trimToNull(job.getSysJobExceptionCallbackBean()));
        job.setSysJobRetryCallbackBean(trimToNull(job.getSysJobRetryCallbackBean()));
        job.setSysJobExecuteMisfireStrategy(trimToNull(job.getSysJobExecuteMisfireStrategy()));
        job.setSysJobScheduleType(normalizeScheduleType(job.getSysJobScheduleType()));
    }

    /**
     * 规范化调度类型, 支持 CRON / FIXED / FIXED_MS / DELAY / AT / NONE。
     *
     * @param scheduleType 原始调度类型
     * @return 规范化后的调度类型
     */
    private String normalizeScheduleType(String scheduleType) {
        if (!StringUtils.hasText(scheduleType)) {
            return DEFAULT_SCHEDULE_TYPE;
        }
        String upper = scheduleType.trim().toUpperCase(java.util.Locale.ROOT);
        if (!List.of("CRON", "FIXED", "FIXED_MS", "DELAY", "AT", "NONE").contains(upper)) {
            throw new IllegalArgumentException("不支持的调度类型: " + scheduleType);
        }
        return upper;
    }

    /**
     * 规范化分发模式。
     *
     * @param dispatchMode 原始分发模式
     * @return 规范化后的分发模式
     */
    private String normalizeDispatchMode(String dispatchMode) {
        String upper = trimToNull(dispatchMode);
        if (upper == null) {
            return "LOCAL";
        }
        upper = upper.toUpperCase(java.util.Locale.ROOT);
        if (!List.of("LOCAL", "REMOTE").contains(upper)) {
            throw new IllegalArgumentException("不支持的分发模式: " + dispatchMode);
        }
        return upper;
    }

    /**
     * 规范化日志存储模式。
     *
     * @param storageMode 原始存储模式
     * @return 规范化后的存储模式
     */
    private String normalizeStorageMode(String storageMode) {
        String upper = trimToNull(storageMode);
        if (upper == null) {
            return "DATABASE";
        }
        upper = upper.toUpperCase(java.util.Locale.ROOT);
        if (!List.of("DATABASE", "FILE").contains(upper)) {
            throw new IllegalArgumentException("不支持的日志存储模式: " + storageMode);
        }
        return upper;
    }

    /**
     * 应用触发器状态, 运行状态下计算下次触发时间。
     *
     * @param job     任务实体
     * @param running 是否运行
     */
    private void applyTriggerState(JobEntity job, boolean running) {
        String scheduleType = normalizeScheduleType(job.getSysJobScheduleType());
        if (running) {
            job.setSysJobTriggerStatus(STATUS_RUNNING);
            if ("NONE".equals(scheduleType)) {
                job.setSysJobTriggerNextTime(0L);
            } else {
                requireText(job.getSysJobScheduleTime(), "运行中的任务必须配置调度时间");
                job.setSysJobTriggerNextTime(calculateNextTriggerTime(scheduleType, job.getSysJobScheduleTime()));
            }
            if (job.getSysJobTriggerLastTime() == null) {
                job.setSysJobTriggerLastTime(0L);
            }
            return;
        }
        job.setSysJobTriggerStatus(STATUS_STOP);
        job.setSysJobTriggerLastTime(0L);
        job.setSysJobTriggerNextTime(0L);
    }

    /**
     * 计算下次触发时间。
     *
     * @param scheduleType 调度类型
     * @param scheduleTime 调度配置
     * @return 下次触发时间戳
     */
    private long calculateNextTriggerTime(String scheduleType, String scheduleTime) {
        try {
            if ("CRON".equals(scheduleType)) {
                CronExpression expression = new CronExpression(scheduleTime == null ? null : scheduleTime.trim());
                Date nextValidTime = expression.getNextValidTimeAfter(new Date());
                if (nextValidTime != null) {
                    return nextValidTime.getTime();
                }
            } else {
                long interval = parseInterval(scheduleTime);
                return System.currentTimeMillis() + interval;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的调度配置: type=" + scheduleType + ", time=" + scheduleTime, e);
        }
        throw new IllegalArgumentException("无效的调度配置: type=" + scheduleType + ", time=" + scheduleTime);
    }

    /**
     * 解析调度间隔 (毫秒)。
     *
     * @param scheduleTime 调度间隔配置
     * @return 调度间隔毫秒数
     */
    private long parseInterval(String scheduleTime) {
        if (!StringUtils.hasText(scheduleTime)) {
            throw new IllegalArgumentException("调度间隔不能为空");
        }
        try {
            return Long.parseLong(scheduleTime.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的调度间隔: " + scheduleTime, e);
        }
    }

    /**
     * 生成唯一任务编号。
     *
     * @return 任务编号
     */
    private String nextJobNo() {
        String ts = Long.toString(System.currentTimeMillis());
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        return "JOB-" + ts + "-" + rand;
    }

    /**
     * 时间戳毫秒数转换为本地时间。
     *
     * @param time 毫秒时间戳
     * @return 本地时间, 空值或非正数返回 null
     */
    private LocalDateTime toLocalDateTime(Long time) {
        if (time == null || time <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    /**
     * 日期转换为本地时间。
     *
     * @param date 日期
     * @return 本地时间, 空值返回 null
     */
    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 日期加耗时转换为本地时间。
     *
     * @param date 日期
     * @param cost 耗时毫秒数
     * @return 本地时间, 空值返回 null
     */
    private LocalDateTime toLocalDateTime(Date date, java.math.BigDecimal cost) {
        if (date == null) {
            return null;
        }
        long plus = cost == null ? 0L : cost.longValue();
        return LocalDateTime.ofInstant(date.toInstant().plusMillis(plus), ZoneId.systemDefault());
    }

    /**
     * 规范化页码, 最小为 1。
     *
     * @param pageNum 页码
     * @return 规范化后的页码
     */
    private int normalizePageNum(int pageNum) {
        return Math.max(pageNum, 1);
    }

    /**
     * 规范化每页大小, 限制在 1 到最大分页大小之间。
     *
     * @param pageSize 每页大小
     * @return 规范化后的每页大小
     */
    private int normalizePageSize(int pageSize) {
        int normalized = Math.max(pageSize, 1);
        return Math.min(normalized, MAX_PAGE_SIZE);
    }

    /**
     * 校验任务 ID 合法性。
     *
     * @param jobId 任务 ID
     * @return 合法任务 ID
     */
    private Long requireJobId(Long jobId) {
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        return jobId;
    }

    /**
     * 校验文本参数非空。
     *
     * @param value   文本值
     * @param message 空值时的异常消息
     * @return 去除空白后的文本
     */
    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    /**
     * 去除空白, 空白文本转换为 null。
     *
     * @param value 文本值
     * @return 去除空白后的文本, 空白返回 null
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 空值状态转换为停止状态。
     *
     * @param triggerStatus 触发器状态
     * @return 非空状态值, 空值返回停止状态
     */
    private int safeStatus(Integer triggerStatus) {
        return triggerStatus == null ? STATUS_STOP : triggerStatus;
    }

    /**
     * 注册定时清理任务 (动态按 {@code plugin.job.log.cleanup-cron} 调度)。
     *
     * <p>不使用 {@code @Scheduled} 注解, 因为本模块的 {@code ScheduledAnnotationScanner}
     * 不解析 Spring 占位符, 会按字面量解析 cron 导致启动失败。</p>
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        String cron = jobProperties.getLog().getCleanupCron();
        if (cron == null || cron.isBlank()) {
            cron = JobProperties.LogConfig.DEFAULT_CLEANUP_CRON;
        }
        taskRegistrar.addCronTask(this::cleanupIfEnabled, cron);
    }

    /**
     * 按配置执行清理 (仅 {@code cleanupEnabled=true} 时生效)。
     */
    private void cleanupIfEnabled() {
        if (!jobProperties.getLog().isCleanupEnabled()) {
            return;
        }
        int retentionDays = jobProperties.getLog().getCleanupRetentionDays();
        log.info("[job] 定时清理日志开始 retentionDays={}", retentionDays);
        try {
            int deleted = cleanupLogs(retentionDays);
            log.info("[job] 定时清理日志完成 deleted={}", deleted);
        } catch (Exception e) {
            log.error("[job] 定时清理日志异常", e);
        }
    }

    /**
     * 转义 SQL 单引号, 防止 SQL 注入。
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }
}
