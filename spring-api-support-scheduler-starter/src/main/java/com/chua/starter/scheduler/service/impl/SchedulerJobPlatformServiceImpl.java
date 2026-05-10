package com.chua.starter.scheduler.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.job.support.JobNumberGenerator;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.entity.SysJobLog;
import com.chua.starter.job.support.scheduler.JobDispatchModeEnum;
import com.chua.starter.job.support.scheduler.JobHelper;
import com.chua.starter.job.support.scheduler.JobStorageModeEnum;
import com.chua.starter.job.support.scheduler.SchedulerTypeEnum;
import com.chua.starter.scheduler.dto.SchedulerTaskUpdateRequest;
import com.chua.starter.scheduler.service.SchedulerJobPlatformService;
import com.chua.starter.scheduler.support.SchedulerNamespaceTableResolver;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.chua.starter.scheduler.support.SchedulerRemoteExecutorDispatcher;
import com.chua.starter.scheduler.vo.SchedulerTaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 调度平台任务服务实现。
 * <p>
 * 平台管理侧直接面向命名空间对应的物理表执行 SQL，不再通过 ThreadLocal
 * 动态切换 Mapper 表名。这样每次请求的读写边界都落到显式表名上，更适合多命名空间
 * 的统一管理场景。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SchedulerJobPlatformServiceImpl implements SchedulerJobPlatformService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final int STATUS_STOP = 0;
    private static final int STATUS_RUNNING = 1;
    private static final String DEFAULT_SCHEDULE_TYPE = "CRON";
    private static final String DEFAULT_GLUE_TYPE = "BEAN";
    private static final String DEFAULT_MISFIRE_STRATEGY = "DO_NOTHING";

    private final SchedulerNamespaceTableResolver namespaceTableResolver;
    private final SchedulerRemoteExecutorDispatcher remoteExecutorDispatcher;
    private final SchedulerJobPlatformProperties platformProperties;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final BeanPropertyRowMapper<SysJob> jobRowMapper = BeanPropertyRowMapper.newInstance(SysJob.class);
    private final BeanPropertyRowMapper<SysJobLog> logRowMapper = BeanPropertyRowMapper.newInstance(SysJobLog.class);

    @Override
    public List<String> namespaces() {
        return namespaceTableResolver.namespaces();
    }

    @Override
    public Page<SysJob> pageJobs(String namespace, int pageNum, int pageSize, String jobName, Integer triggerStatus) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        int safePageNum = normalizePageNum(pageNum);
        int safePageSize = normalizePageSize(pageSize);
        String normalizedJobName = trimToNull(jobName);

        StringBuilder where = new StringBuilder(" FROM ").append(tables.jobTable()).append(" WHERE is_deleted = 0");
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (StringUtils.hasText(normalizedJobName)) {
            where.append(" AND job_name LIKE :jobName");
            parameters.addValue("jobName", "%" + normalizedJobName + "%");
        }
        if (triggerStatus != null) {
            where.append(" AND job_trigger_status = :triggerStatus");
            parameters.addValue("triggerStatus", triggerStatus);
        }

        long total = count("SELECT COUNT(1)" + where, parameters);
        parameters.addValue("limit", safePageSize);
        parameters.addValue("offset", (safePageNum - 1L) * safePageSize);
        List<SysJob> records = namedParameterJdbcTemplate.query(
                "SELECT *" + where + " ORDER BY job_id DESC LIMIT :limit OFFSET :offset",
                parameters,
                jobRowMapper
        );
        return toPage(safePageNum, safePageSize, total, records);
    }

    @Override
    public SysJob getJob(String namespace, Integer jobId) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        return requireJob(tables, requireJobId(jobId));
    }

    @Override
    public SysJob getJobByNo(String namespace, String jobNo) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        return requireJobByNo(tables, jobNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysJob createJob(String namespace, SysJob job) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        SysJob target = new SysJob();
        BeanUtils.copyProperties(job, target);
        applyDefaults(target);
        normalizeJob(target);
        validateJob(target);
        applyTriggerState(target, STATUS_RUNNING == safeStatus(target.getJobTriggerStatus()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(insertSql(tables.jobTable()),
                new BeanPropertySqlParameterSource(target),
                keyHolder,
                new String[]{"job_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建任务失败: 未返回任务ID");
        }
        return requireJob(tables, key.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJob(String namespace, Integer jobId, SysJob job) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        int safeJobId = requireJobId(jobId);
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        SysJob existing = requireJob(tables, safeJobId);
        mergeJobPatch(existing, job);
        normalizeJob(existing);
        validateJob(existing);

        if (STATUS_RUNNING == safeStatus(existing.getJobTriggerStatus())) {
            applyTriggerState(existing, true);
        } else {
            applyTriggerState(existing, false);
        }
        return updateJobRecord(tables.jobTable(), existing) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateJobByNo(String namespace, String jobNo, SysJob job) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        if (job == null) {
            throw new IllegalArgumentException("任务内容不能为空");
        }

        SysJob existing = requireJobByNo(tables, jobNo);
        mergeJobPatch(existing, job);
        normalizeJob(existing);
        validateJob(existing);

        if (STATUS_RUNNING == safeStatus(existing.getJobTriggerStatus())) {
            applyTriggerState(existing, true);
        } else {
            applyTriggerState(existing, false);
        }
        return updateJobRecord(tables.jobTable(), existing) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(String namespace, Integer jobId) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobId", requireJobId(jobId));
        // 逻辑删除：先停止任务，再设置 is_deleted = 1
        stopJob(namespace, jobId);
        return namedParameterJdbcTemplate.update(
                "UPDATE " + tables.jobTable() + " SET is_deleted = 1 WHERE job_id = :jobId",
                parameters
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJobByNo(String namespace, String jobNo) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        // 逻辑删除：先停止任务，再设置 is_deleted = 1
        stopJobByNo(namespace, jobNo);
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobNo", requireText(jobNo, "任务编号不能为空"));
        return namedParameterJdbcTemplate.update(
                "UPDATE " + tables.jobTable() + " SET is_deleted = 1 WHERE job_no = :jobNo",
                parameters
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreJob(String namespace, Integer jobId) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        log.info("恢复任务: namespace={}, jobId={}", namespace, jobId);
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobId", requireJobId(jobId));
        return namedParameterJdbcTemplate.update(
                "UPDATE " + tables.jobTable() + " SET is_deleted = 0 WHERE job_id = :jobId",
                parameters
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreJobByNo(String namespace, String jobNo) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        log.info("恢复任务: namespace={}, jobNo={}", namespace, jobNo);
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobNo", requireText(jobNo, "任务编号不能为空"));
        return namedParameterJdbcTemplate.update(
                "UPDATE " + tables.jobTable() + " SET is_deleted = 0 WHERE job_no = :jobNo",
                parameters
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startJob(String namespace, Integer jobId) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJob(tables, requireJobId(jobId));
        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, true);
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startJobByNo(String namespace, String jobNo) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJobByNo(tables, jobNo);
        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, true);
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopJob(String namespace, Integer jobId) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJob(tables, requireJobId(jobId));
        applyTriggerState(job, false);
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean stopJobByNo(String namespace, String jobNo) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJobByNo(tables, jobNo);
        applyTriggerState(job, false);
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerJob(String namespace, Integer jobId, String param) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJob(tables, requireJobId(jobId));

        if (isRemoteDispatchJob(job)) {
            remoteExecutorDispatcher.dispatch(namespace, job, param);
            return true;
        }

        if (param != null) {
            job.setJobExecuteParam(param);
        }
        job.setJobTriggerNextTime(System.currentTimeMillis());
        if (job.getJobTriggerLastTime() == null) {
            job.setJobTriggerLastTime(0L);
        }
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean triggerJobByNo(String namespace, String jobNo, String param) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireJobByNo(tables, jobNo);

        if (isRemoteDispatchJob(job)) {
            remoteExecutorDispatcher.dispatch(namespace, job, param);
            return true;
        }

        if (param != null) {
            job.setJobExecuteParam(param);
        }
        job.setJobTriggerNextTime(System.currentTimeMillis());
        if (job.getJobTriggerLastTime() == null) {
            job.setJobTriggerLastTime(0L);
        }
        return updateJobRecord(tables.jobTable(), job) > 0;
    }

    @Override
    public Page<SysJobLog> pageLogs(String namespace, int pageNum, int pageSize, String beanName, String executeCode) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        int safePageNum = normalizePageNum(pageNum);
        int safePageSize = normalizePageSize(pageSize);
        String normalizedBeanName = trimToNull(beanName);
        String normalizedExecuteCode = trimToNull(executeCode);
        if (StringUtils.hasText(normalizedExecuteCode)) {
            normalizedExecuteCode = normalizedExecuteCode.toUpperCase(Locale.ROOT);
        }

        StringBuilder where = new StringBuilder(" FROM ").append(tables.logTable()).append(" WHERE 1=1");
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (StringUtils.hasText(normalizedBeanName)) {
            where.append(" AND job_log_trigger_bean LIKE :beanName");
            parameters.addValue("beanName", "%" + normalizedBeanName + "%");
        }
        if (StringUtils.hasText(normalizedExecuteCode)) {
            where.append(" AND job_log_execute_code = :executeCode");
            parameters.addValue("executeCode", normalizedExecuteCode);
        }

        long total = count("SELECT COUNT(1)" + where, parameters);
        parameters.addValue("limit", safePageSize);
        parameters.addValue("offset", (safePageNum - 1L) * safePageSize);
        List<SysJobLog> records = namedParameterJdbcTemplate.query(
                "SELECT *" + where + " ORDER BY job_log_id DESC LIMIT :limit OFFSET :offset",
                parameters,
                logRowMapper
        );
        return toPage(safePageNum, safePageSize, total, records);
    }

    @Override
    public List<SchedulerTaskVO> listTasks(String namespace) {
        return listTasks(namespace, false);
    }

    @Override
    public List<SchedulerTaskVO> listTasks(String namespace, boolean includeDeleted) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        String sql = includeDeleted
                ? "SELECT * FROM " + tables.jobTable() + " ORDER BY job_id DESC"
                : "SELECT * FROM " + tables.jobTable() + " WHERE is_deleted = 0 ORDER BY job_id DESC";
        List<SysJob> jobs = namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource(),
                jobRowMapper
        );
        return jobs.stream()
                .map(job -> toTaskVO(tables, job))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchedulerTaskVO updateTask(String namespace, String taskKey, SchedulerTaskUpdateRequest request) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        if (request == null) {
            throw new IllegalArgumentException("任务更新内容不能为空");
        }

        SysJob job = requireTask(tables, taskKey);
        if (StringUtils.hasText(request.getCronExpression())) {
            job.setJobScheduleType(DEFAULT_SCHEDULE_TYPE);
            job.setJobScheduleTime(request.getCronExpression().trim());
        }
        if (request.getEnabled() != null) {
            job.setJobTriggerStatus(Boolean.TRUE.equals(request.getEnabled()) ? STATUS_RUNNING : STATUS_STOP);
        }

        normalizeJob(job);
        validateJob(job);
        applyTriggerState(job, STATUS_RUNNING == safeStatus(job.getJobTriggerStatus()));
        updateJobRecord(tables.jobTable(), job);
        return toTaskVO(tables, requireTask(tables, taskKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchedulerTaskVO triggerTask(String namespace, String taskKey) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        SysJob job = requireTask(tables, taskKey);
        if (isRemoteDispatchJob(job)) {
            remoteExecutorDispatcher.dispatch(namespace, job, job.getJobExecuteParam());
            return toTaskVO(tables, requireTask(tables, taskKey));
        }
        job.setJobTriggerNextTime(System.currentTimeMillis());
        if (job.getJobTriggerLastTime() == null) {
            job.setJobTriggerLastTime(0L);
        }
        updateJobRecord(tables.jobTable(), job);
        return toTaskVO(tables, requireTask(tables, taskKey));
    }

    private SchedulerTaskVO toTaskVO(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, SysJob job) {
        SchedulerTaskVO vo = new SchedulerTaskVO();
        vo.setJobId(job.getJobId());
        vo.setJobNo(job.getJobNo());
        vo.setTaskKey(StringUtils.hasText(job.getJobExecuteBean()) ? job.getJobExecuteBean() : job.getJobName());
        vo.setTaskName(job.getJobName());
        vo.setScheduleType(job.getJobScheduleType());
        vo.setCronExpression(job.getJobScheduleTime());
        vo.setEnabled(STATUS_RUNNING == safeStatus(job.getJobTriggerStatus()));
        vo.setDescription(job.getJobDesc());
        vo.setScheduled(STATUS_RUNNING == safeStatus(job.getJobTriggerStatus()));
        vo.setDeleted(Integer.valueOf(1).equals(job.getIsDeleted()));
        vo.setDispatchMode(job.getJobDispatchMode());
        vo.setRemoteExecutorAddress(job.getJobRemoteExecutorAddress());
        vo.setStorageMode(job.getJobStorageMode());
        vo.setNextExecutionTime(toLocalDateTime(job.getJobTriggerNextTime()));

        SysJobLog latest = findLatestLog(tables, job);
        if (latest != null) {
            vo.setLastStartedAt(toLocalDateTime(latest.getJobLogTriggerTime()));
            vo.setLastFinishedAt(toLocalDateTime(latest.getJobLogTriggerTime(), latest.getJobLogCost()));
            vo.setLastRunStatus(latest.getJobLogExecuteCode());
            vo.setLastRunMessage(latest.getJobLogTriggerMsg());
        }
        return vo;
    }

    private SysJob requireJob(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, int jobId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobId", jobId);
        List<SysJob> records = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable() + " WHERE job_id = :jobId AND is_deleted = 0 LIMIT 1",
                parameters,
                jobRowMapper
        );
        if (records.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + jobId);
        }
        return records.get(0);
    }

    private SysJob requireJobByNo(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, String jobNo) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobNo", requireText(jobNo, "任务编号不能为空"));
        List<SysJob> records = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable() + " WHERE job_no = :jobNo AND is_deleted = 0 LIMIT 1",
                parameters,
                jobRowMapper
        );
        if (records.isEmpty()) {
            throw new IllegalArgumentException("任务不存在: " + jobNo);
        }
        return records.get(0);
    }

    private SysJob requireTask(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, String taskKey) {
        String normalizedTaskKey = requireText(taskKey, "任务标识不能为空");
        MapSqlParameterSource parameters = new MapSqlParameterSource("taskKey", normalizedTaskKey);

        List<SysJob> byNo = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable() + " WHERE job_no = :taskKey AND is_deleted = 0 ORDER BY job_id DESC LIMIT 1",
                parameters,
                jobRowMapper
        );
        if (!byNo.isEmpty()) {
            return byNo.get(0);
        }

        List<SysJob> byBean = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable() + " WHERE job_execute_bean = :taskKey AND is_deleted = 0 ORDER BY job_id DESC LIMIT 1",
                parameters,
                jobRowMapper
        );
        if (!byBean.isEmpty()) {
            return byBean.get(0);
        }

        List<SysJob> byName = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable() + " WHERE job_name = :taskKey AND is_deleted = 0 ORDER BY job_id DESC LIMIT 1",
                parameters,
                jobRowMapper
        );
        if (!byName.isEmpty()) {
            return byName.get(0);
        }
        throw new IllegalArgumentException("任务不存在: " + normalizedTaskKey);
    }

    private SysJobLog findLatestLog(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, SysJob job) {
        SysJobLog latest = findLatestLogByJobNo(tables, trimToNull(job.getJobNo()));
        if (latest != null) {
            return latest;
        }
        latest = findLatestLogByTriggerBean(tables, trimToNull(job.getJobExecuteBean()));
        if (latest != null) {
            return latest;
        }
        String fallbackTriggerBean = trimToNull(job.getJobName());
        if (StringUtils.hasText(fallbackTriggerBean)
                && !fallbackTriggerBean.equals(trimToNull(job.getJobExecuteBean()))) {
            return findLatestLogByTriggerBean(tables, fallbackTriggerBean);
        }
        return null;
    }

    private SysJobLog findLatestLogByJobNo(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, String jobNo) {
        if (!StringUtils.hasText(jobNo)) {
            return null;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource("jobNo", jobNo);
        List<SysJobLog> logs = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.logTable()
                        + " WHERE job_no = :jobNo"
                        + " ORDER BY job_log_trigger_time DESC, job_log_id DESC LIMIT 1",
                parameters,
                logRowMapper
        );
        return logs.isEmpty() ? null : logs.get(0);
    }

    private SysJobLog findLatestLogByTriggerBean(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables, String triggerBean) {
        if (!StringUtils.hasText(triggerBean)) {
            return null;
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource("triggerBean", triggerBean);
        List<SysJobLog> logs = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.logTable()
                        + " WHERE job_log_trigger_bean = :triggerBean"
                        + " ORDER BY job_log_trigger_time DESC, job_log_id DESC LIMIT 1",
                parameters,
                logRowMapper
        );
        return logs.isEmpty() ? null : logs.get(0);
    }

    private int updateJobRecord(String tableName, SysJob job) {
        return namedParameterJdbcTemplate.update(
                updateSql(tableName),
                new BeanPropertySqlParameterSource(job)
        );
    }

    private String insertSql(String tableName) {
        return "INSERT INTO " + tableName + " ("
                + "job_no, job_name, job_schedule_type, job_schedule_time, job_author, job_alarm_email, "
                + "job_trigger_status, job_desc, job_glue_updatetime, job_glue_source, job_glue_type, "
                + "job_dispatch_mode, job_remote_executor_address, job_storage_mode, "
                + "job_fail_retry, job_retry_interval, job_execute_timeout, job_execute_bean, job_execute_param, "
                + "job_exception_callback_bean, job_retry_callback_bean, "
                + "job_execute_misfire_strategy, job_trigger_last_time, job_trigger_next_time"
                + ") VALUES ("
                + ":jobNo, :jobName, :jobScheduleType, :jobScheduleTime, :jobAuthor, :jobAlarmEmail, "
                + ":jobTriggerStatus, :jobDesc, :jobGlueUpdatetime, :jobGlueSource, :jobGlueType, "
                + ":jobDispatchMode, :jobRemoteExecutorAddress, :jobStorageMode, "
                + ":jobFailRetry, :jobRetryInterval, :jobExecuteTimeout, :jobExecuteBean, :jobExecuteParam, "
                + ":jobExceptionCallbackBean, :jobRetryCallbackBean, "
                + ":jobExecuteMisfireStrategy, :jobTriggerLastTime, :jobTriggerNextTime"
                + ")";
    }

    private String updateSql(String tableName) {
        return "UPDATE " + tableName + " SET "
                + "job_no = :jobNo, "
                + "job_name = :jobName, "
                + "job_schedule_type = :jobScheduleType, "
                + "job_schedule_time = :jobScheduleTime, "
                + "job_author = :jobAuthor, "
                + "job_alarm_email = :jobAlarmEmail, "
                + "job_trigger_status = :jobTriggerStatus, "
                + "job_desc = :jobDesc, "
                + "job_glue_updatetime = :jobGlueUpdatetime, "
                + "job_glue_source = :jobGlueSource, "
                + "job_glue_type = :jobGlueType, "
                + "job_dispatch_mode = :jobDispatchMode, "
                + "job_remote_executor_address = :jobRemoteExecutorAddress, "
                + "job_storage_mode = :jobStorageMode, "
                + "job_fail_retry = :jobFailRetry, "
                + "job_retry_interval = :jobRetryInterval, "
                + "job_execute_timeout = :jobExecuteTimeout, "
                + "job_execute_bean = :jobExecuteBean, "
                + "job_execute_param = :jobExecuteParam, "
                + "job_exception_callback_bean = :jobExceptionCallbackBean, "
                + "job_retry_callback_bean = :jobRetryCallbackBean, "
                + "job_execute_misfire_strategy = :jobExecuteMisfireStrategy, "
                + "job_trigger_last_time = :jobTriggerLastTime, "
                + "job_trigger_next_time = :jobTriggerNextTime "
                + "WHERE job_id = :jobId";
    }

    private void mergeJobPatch(SysJob target, SysJob patch) {
        if (patch.getJobName() != null) {
            target.setJobName(patch.getJobName());
        }
        if (patch.getJobScheduleType() != null) {
            target.setJobScheduleType(patch.getJobScheduleType());
        }
        if (patch.getJobScheduleTime() != null) {
            target.setJobScheduleTime(patch.getJobScheduleTime());
        }
        if (patch.getJobAuthor() != null) {
            target.setJobAuthor(patch.getJobAuthor());
        }
        if (patch.getJobAlarmEmail() != null) {
            target.setJobAlarmEmail(patch.getJobAlarmEmail());
        }
        if (patch.getJobTriggerStatus() != null) {
            target.setJobTriggerStatus(patch.getJobTriggerStatus());
        }
        if (patch.getJobDesc() != null) {
            target.setJobDesc(patch.getJobDesc());
        }
        if (patch.getJobGlueUpdatetime() != null) {
            target.setJobGlueUpdatetime(patch.getJobGlueUpdatetime());
        }
        if (patch.getJobGlueSource() != null) {
            target.setJobGlueSource(patch.getJobGlueSource());
        }
        if (patch.getJobGlueType() != null) {
            target.setJobGlueType(patch.getJobGlueType());
        }
        if (patch.getJobDispatchMode() != null) {
            target.setJobDispatchMode(patch.getJobDispatchMode());
        }
        if (patch.getJobRemoteExecutorAddress() != null) {
            target.setJobRemoteExecutorAddress(patch.getJobRemoteExecutorAddress());
        }
        if (patch.getJobStorageMode() != null) {
            target.setJobStorageMode(patch.getJobStorageMode());
        }
        if (patch.getJobFailRetry() != null) {
            target.setJobFailRetry(patch.getJobFailRetry());
        }
        if (patch.getJobRetryInterval() != null) {
            target.setJobRetryInterval(patch.getJobRetryInterval());
        }
        if (patch.getJobExecuteTimeout() != null) {
            target.setJobExecuteTimeout(patch.getJobExecuteTimeout());
        }
        if (patch.getJobExecuteBean() != null) {
            target.setJobExecuteBean(patch.getJobExecuteBean());
        }
        if (patch.getJobExecuteParam() != null) {
            target.setJobExecuteParam(patch.getJobExecuteParam());
        }
        if (patch.getJobExceptionCallbackBean() != null) {
            target.setJobExceptionCallbackBean(patch.getJobExceptionCallbackBean());
        }
        if (patch.getJobRetryCallbackBean() != null) {
            target.setJobRetryCallbackBean(patch.getJobRetryCallbackBean());
        }
        if (patch.getJobExecuteMisfireStrategy() != null) {
            target.setJobExecuteMisfireStrategy(patch.getJobExecuteMisfireStrategy());
        }
    }

    private void applyDefaults(SysJob job) {
        if (!StringUtils.hasText(job.getJobNo())) {
            job.setJobNo(JobNumberGenerator.nextJobNo());
        }
        if (job.getJobScheduleType() == null) {
            job.setJobScheduleType(DEFAULT_SCHEDULE_TYPE);
        }
        if (job.getJobGlueType() == null) {
            job.setJobGlueType(DEFAULT_GLUE_TYPE);
        }
        if (job.getJobGlueUpdatetime() == null) {
            job.setJobGlueUpdatetime(new Date());
        }
        if (job.getJobTriggerStatus() == null) {
            job.setJobTriggerStatus(STATUS_STOP);
        }
        if (job.getJobFailRetry() == null) {
            job.setJobFailRetry(0);
        }
        if (job.getJobRetryInterval() == null) {
            job.setJobRetryInterval(0);
        }
        if (job.getJobExecuteTimeout() == null) {
            job.setJobExecuteTimeout(0);
        }
        if (job.getJobDispatchMode() == null) {
            job.setJobDispatchMode(JobDispatchModeEnum.LOCAL.name());
        }
        if (job.getJobStorageMode() == null) {
            job.setJobStorageMode(JobStorageModeEnum.DATABASE.name());
        }
        if (job.getJobExecuteMisfireStrategy() == null) {
            job.setJobExecuteMisfireStrategy(DEFAULT_MISFIRE_STRATEGY);
        }
        if (job.getJobTriggerLastTime() == null) {
            job.setJobTriggerLastTime(0L);
        }
        if (job.getJobTriggerNextTime() == null) {
            job.setJobTriggerNextTime(0L);
        }
    }

    private void validateJob(SysJob job) {
        requireText(job.getJobNo(), "任务编号不能为空");
        requireText(job.getJobName(), "任务名称不能为空");
        if ("BEAN".equalsIgnoreCase(job.getJobGlueType())) {
            requireText(job.getJobExecuteBean(), "执行 Bean 名称不能为空");
        }
        if (SchedulerTypeEnum.NONE != SchedulerTypeEnum.match(job.getJobScheduleType(), SchedulerTypeEnum.CRON)) {
            requireText(job.getJobScheduleTime(), "调度配置不能为空");
        }
    }

    private void normalizeJob(SysJob job) {
        applyDefaults(job);
        job.setJobNo(trimToNull(job.getJobNo()));
        job.setJobName(trimToNull(job.getJobName()));
        job.setJobExecuteBean(trimToNull(job.getJobExecuteBean()));
        job.setJobScheduleTime(trimToNull(job.getJobScheduleTime()));
        job.setJobAuthor(trimToNull(job.getJobAuthor()));
        job.setJobAlarmEmail(trimToNull(job.getJobAlarmEmail()));
        job.setJobDesc(trimToNull(job.getJobDesc()));
        job.setJobGlueSource(trimToNull(job.getJobGlueSource()));
        job.setJobGlueType(trimToNull(job.getJobGlueType()));
        job.setJobDispatchMode(normalizeDispatchMode(job.getJobDispatchMode()));
        job.setJobRemoteExecutorAddress(trimToNull(job.getJobRemoteExecutorAddress()));
        job.setJobStorageMode(normalizeStorageMode(job.getJobStorageMode()));
        job.setJobExceptionCallbackBean(trimToNull(job.getJobExceptionCallbackBean()));
        job.setJobRetryCallbackBean(trimToNull(job.getJobRetryCallbackBean()));
        job.setJobExecuteMisfireStrategy(trimToNull(job.getJobExecuteMisfireStrategy()));
        job.setJobScheduleType(normalizeScheduleType(job.getJobScheduleType()));
    }

    private String normalizeScheduleType(String scheduleType) {
        if (!StringUtils.hasText(scheduleType)) {
            return DEFAULT_SCHEDULE_TYPE;
        }
        SchedulerTypeEnum matched = SchedulerTypeEnum.match(scheduleType, null);
        if (matched == null) {
            throw new IllegalArgumentException("不支持的调度类型: " + scheduleType);
        }
        return matched.name();
    }

    private String normalizeDispatchMode(String dispatchMode) {
        return JobDispatchModeEnum.match(dispatchMode).name();
    }

    private String normalizeStorageMode(String storageMode) {
        return JobStorageModeEnum.match(storageMode).name();
    }

    private void applyTriggerState(SysJob job, boolean running) {
        String scheduleType = normalizeScheduleType(job.getJobScheduleType());
        if (running) {
            job.setJobTriggerStatus(STATUS_RUNNING);
            if (SchedulerTypeEnum.NONE == SchedulerTypeEnum.match(scheduleType, SchedulerTypeEnum.CRON)) {
                job.setJobTriggerNextTime(0L);
            } else {
                requireText(job.getJobScheduleTime(), "运行中的任务必须配置调度时间");
                job.setJobTriggerNextTime(calculateNextTriggerTime(scheduleType, job.getJobScheduleTime()));
            }
            if (job.getJobTriggerLastTime() == null) {
                job.setJobTriggerLastTime(0L);
            }
            return;
        }
        job.setJobTriggerStatus(STATUS_STOP);
        job.setJobTriggerLastTime(0L);
        job.setJobTriggerNextTime(0L);
    }

    private long calculateNextTriggerTime(String scheduleType, String scheduleTime) {
        try {
            SysJob job = new SysJob();
            job.setJobScheduleType(normalizeScheduleType(scheduleType));
            job.setJobScheduleTime(scheduleTime == null ? null : scheduleTime.trim());
            Date nextValidTime = JobHelper.generateNextValidTime(job, new Date());
            if (nextValidTime != null) {
                return nextValidTime.getTime();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的调度配置: type=" + scheduleType + ", time=" + scheduleTime, e);
        }
        throw new IllegalArgumentException("无效的调度配置: type=" + scheduleType + ", time=" + scheduleTime);
    }

    private boolean isRemoteDispatchJob(SysJob job) {
        return job != null && JobDispatchModeEnum.REMOTE == JobDispatchModeEnum.match(job.getJobDispatchMode());
    }

    private <T> Page<T> toPage(int pageNum, int pageSize, long total, List<T> records) {
        Page<T> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    private long count(String sql, MapSqlParameterSource parameters) {
        Long total = namedParameterJdbcTemplate.queryForObject(sql, parameters, Long.class);
        return total == null ? 0L : total;
    }

    private LocalDateTime toLocalDateTime(Long time) {
        if (time == null || time <= 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private LocalDateTime toLocalDateTime(java.util.Date date, BigDecimal cost) {
        if (date == null) {
            return null;
        }
        long plus = cost == null ? 0L : cost.longValue();
        return LocalDateTime.ofInstant(date.toInstant().plusMillis(plus), ZoneId.systemDefault());
    }

    private int normalizePageNum(int pageNum) {
        return Math.max(pageNum, 1);
    }

    private int normalizePageSize(int pageSize) {
        int normalized = Math.max(pageSize, 1);
        return Math.min(normalized, MAX_PAGE_SIZE);
    }

    private int requireJobId(Integer jobId) {
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        return jobId;
    }

    private String requireText(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private int safeStatus(Integer triggerStatus) {
        return triggerStatus == null ? STATUS_STOP : triggerStatus;
    }

    @Override
    public Object getNamespaceConfig(String namespace) {
        boolean hasRemote = remoteExecutorDispatcher.hasExecutors(namespace);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("remoteDispatchEnabled", platformProperties.isRemoteDispatchEnabled());
        config.put("hasRemoteExecutors", hasRemote);

        // 调度模式
        String mode = "STANDALONE";
        SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
        if (nsProps != null && StringUtils.hasText(nsProps.getMode())) {
            mode = nsProps.getMode().toUpperCase();
        }
        config.put("schedulerMode", mode);

        // 数据源信息
        String executorMode = "DISCOVERY";
        if (nsProps != null && StringUtils.hasText(nsProps.getExecutorMode())) {
            executorMode = nsProps.getExecutorMode().toUpperCase();
        }
        config.put("dataSource", executorMode);

        if (hasRemote) {
            List<?> executors = remoteExecutorDispatcher.resolveExecutors(namespace, null);
            List<Map<String, Object>> executorList = new ArrayList<>();
            for (Object ex : executors) {
                SchedulerJobPlatformProperties.Executor e = (SchedulerJobPlatformProperties.Executor) ex;
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", e.getName());
                map.put("baseUrl", e.getBaseUrl());
                executorList.add(map);
            }
            config.put("executors", executorList);
        } else {
            config.put("executors", List.of());
        }
        return config;
    }

    private SchedulerJobPlatformProperties.Namespace resolveNamespaceProperties(String namespace) {
        if (!StringUtils.hasText(namespace) || platformProperties.getNamespaces() == null) {
            return null;
        }
        return platformProperties.getNamespaces().entrySet().stream()
                .filter(entry -> namespace.equalsIgnoreCase(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }
}
