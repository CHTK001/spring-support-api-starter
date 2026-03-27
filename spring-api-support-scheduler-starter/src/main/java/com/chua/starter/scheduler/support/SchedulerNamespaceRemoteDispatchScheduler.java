package com.chua.starter.scheduler.support;

import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.scheduler.JobDispatchModeEnum;
import com.chua.starter.job.support.scheduler.JobHelper;
import com.chua.starter.job.support.scheduler.MisfireStrategyEnum;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 命名空间远程分发轮询器。
 * <p>
 * 这是调度中心真正承担“类似 XXL-Job admin”的部分：定时扫描已配置远程执行器
 * 的命名空间任务表，命中后先推进下一次触发时间，再把本次触发推送到远程业务服务。
 * </p>
 * <p>
 * 当前实现使用单节点本地锁避免同一实例内重入；跨实例则依赖
 * {@code job_trigger_status + job_trigger_next_time} 的 CAS 更新抢占同一条任务，
 * 避免多节点重复下发。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerNamespaceRemoteDispatchScheduler {

    private static final int STATUS_STOP = 0;
    private static final int STATUS_RUNNING = 1;

    private final SchedulerJobPlatformProperties platformProperties;
    private final SchedulerNamespaceTableResolver namespaceTableResolver;
    private final SchedulerRemoteExecutorDispatcher remoteExecutorDispatcher;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final BeanPropertyRowMapper<SysJob> jobRowMapper = BeanPropertyRowMapper.newInstance(SysJob.class);
    private final Map<String, ReentrantLock> namespaceLocks = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${plugin.scheduler.job-platform.dispatch-interval-ms:1000}")
    public void dispatchDueJobs() {
        if (!platformProperties.isRemoteDispatchEnabled() || platformProperties.getNamespaces() == null) {
            return;
        }

        for (String namespace : namespaceTableResolver.namespaces()) {
            if (!remoteExecutorDispatcher.hasExecutors(namespace)) {
                continue;
            }
            dispatchNamespace(namespace);
        }
    }

    private void dispatchNamespace(String namespace) {
        ReentrantLock lock = namespaceLocks.computeIfAbsent(namespace.toLowerCase(), key -> new ReentrantLock());
        if (!lock.tryLock()) {
            return;
        }
        try {
            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
            List<SysJob> dueJobs = loadDueJobs(tables);
            for (SysJob job : dueJobs) {
                processJob(namespace, tables, job);
            }
        } finally {
            lock.unlock();
        }
    }

    private List<SysJob> loadDueJobs(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", STATUS_RUNNING);
        parameters.addValue("dispatchMode", JobDispatchModeEnum.REMOTE.name());
        parameters.addValue("now", System.currentTimeMillis());
        parameters.addValue("limit", Math.max(1, platformProperties.getDispatchBatchSize()));
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM " + tables.jobTable()
                        + " WHERE job_trigger_status = :status"
                        + " AND job_dispatch_mode = :dispatchMode"
                        + " AND job_trigger_next_time > 0"
                        + " AND job_trigger_next_time <= :now"
                        + " ORDER BY job_trigger_next_time ASC, job_id ASC LIMIT :limit",
                parameters,
                jobRowMapper
        );
    }

    private void processJob(String namespace,
                            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables,
                            SysJob job) {
        if (!canDispatch(job)) {
            log.warn("[Scheduler] 跳过远程分发，任务不满足远程执行条件: namespace={}, jobNo={}, jobId={}, dispatchMode={}, glueType={}, handler={}",
                    namespace, job.getJobNo(), job.getJobId(), job.getJobDispatchMode(), job.getJobGlueType(), job.getJobExecuteBean());
            return;
        }

        DispatchPlan plan;
        try {
            plan = buildPlan(job);
        } catch (Exception e) {
            log.error("[Scheduler] 计算远程任务下次触发时间失败: namespace={}, jobNo={}, jobId={}, error={}",
                    namespace, job.getJobNo(), job.getJobId(), e.getMessage(), e);
            return;
        }

        if (!claimJob(tables.jobTable(), job, plan)) {
            return;
        }

        if (!plan.shouldDispatch()) {
            return;
        }

        try {
            remoteExecutorDispatcher.dispatch(namespace, job, job.getJobExecuteParam());
        } catch (Exception e) {
            rollbackClaim(tables.jobTable(), job, plan);
            log.error("[Scheduler] 远程任务下发失败，已回滚触发时间: namespace={}, jobNo={}, jobId={}, error={}",
                    namespace, job.getJobNo(), job.getJobId(), e.getMessage(), e);
        }
    }

    private DispatchPlan buildPlan(SysJob job) throws Exception {
        long now = System.currentTimeMillis();
        long currentNextTime = safeLong(job.getJobTriggerNextTime());
        long currentLastTime = safeLong(job.getJobTriggerLastTime());
        boolean misfire = currentNextTime > 0 && now > currentNextTime + JobHelper.PRE_READ_MS;
        MisfireStrategyEnum misfireStrategy = MisfireStrategyEnum.match(
                job.getJobExecuteMisfireStrategy(),
                MisfireStrategyEnum.DO_NOTHING
        );
        boolean shouldDispatch = !misfire || MisfireStrategyEnum.FIRE_ONCE_NOW == misfireStrategy;

        Date nextValidTime = JobHelper.generateNextValidTime(job, new Date());
        if (nextValidTime == null) {
            return new DispatchPlan(currentLastTime, currentNextTime, STATUS_RUNNING, 0L, 0L, STATUS_STOP, shouldDispatch);
        }
        return new DispatchPlan(
                currentLastTime,
                currentNextTime,
                STATUS_RUNNING,
                currentNextTime,
                nextValidTime.getTime(),
                STATUS_RUNNING,
                shouldDispatch
        );
    }

    private boolean claimJob(String tableName, SysJob job, DispatchPlan plan) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getJobId());
        parameters.addValue("expectedStatus", plan.previousStatus());
        parameters.addValue("expectedNextTime", plan.previousNextTime());
        parameters.addValue("nextStatus", plan.nextStatus());
        parameters.addValue("nextLastTime", plan.nextLastTime());
        parameters.addValue("nextNextTime", plan.nextNextTime());
        int updated = namedParameterJdbcTemplate.update(
                "UPDATE " + tableName
                        + " SET job_trigger_status = :nextStatus,"
                        + " job_trigger_last_time = :nextLastTime,"
                        + " job_trigger_next_time = :nextNextTime"
                        + " WHERE job_id = :jobId"
                        + " AND job_trigger_status = :expectedStatus"
                        + " AND job_trigger_next_time = :expectedNextTime",
                parameters
        );
        return updated > 0;
    }

    private void rollbackClaim(String tableName, SysJob job, DispatchPlan plan) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getJobId());
        parameters.addValue("currentStatus", plan.nextStatus());
        parameters.addValue("currentLastTime", plan.nextLastTime());
        parameters.addValue("currentNextTime", plan.nextNextTime());
        parameters.addValue("rollbackStatus", plan.previousStatus());
        parameters.addValue("rollbackLastTime", plan.previousLastTime());
        parameters.addValue("rollbackNextTime", plan.previousNextTime());
        namedParameterJdbcTemplate.update(
                "UPDATE " + tableName
                        + " SET job_trigger_status = :rollbackStatus,"
                        + " job_trigger_last_time = :rollbackLastTime,"
                        + " job_trigger_next_time = :rollbackNextTime"
                        + " WHERE job_id = :jobId"
                        + " AND job_trigger_status = :currentStatus"
                        + " AND job_trigger_last_time = :currentLastTime"
                        + " AND job_trigger_next_time = :currentNextTime",
                parameters
        );
    }

    private boolean canDispatch(SysJob job) {
        return job != null
                && JobDispatchModeEnum.REMOTE == JobDispatchModeEnum.match(job.getJobDispatchMode())
                && StringUtils.hasText(job.getJobExecuteBean())
                && (!StringUtils.hasText(job.getJobGlueType()) || "BEAN".equalsIgnoreCase(job.getJobGlueType()));
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 一次触发推进前后的状态快照。
     */
    private record DispatchPlan(long previousLastTime,
                                long previousNextTime,
                                int previousStatus,
                                long nextLastTime,
                                long nextNextTime,
                                int nextStatus,
                                boolean shouldDispatch) {
    }
}
