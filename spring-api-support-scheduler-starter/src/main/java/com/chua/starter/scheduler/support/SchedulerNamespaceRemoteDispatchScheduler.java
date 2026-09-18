package com.chua.starter.scheduler.support;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.reflection.ReflectUtils;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * 命名空间远程分发轮询器。
 * <p>
 * 这是调度中心真正承担“类似 XXL-作业 admin”的部分：定时扫描已配置远程执行器
 * 的命名空间任务表，命中后先推进下一次触发时间，再把本次触发推送到远程业务服务。
 * </p>
 * <p>
 * 当前实现使用单节点本地锁避免同一实例内重入；跨实例则依赖
 * {@code sys_job_trigger_status + sys_job_trigger_next_time} 的 CAS 更新抢占同一条任务，
 * 避免多节点重复下发。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerNamespaceRemoteDispatchScheduler {

    /**
     * 状态：停止
     */
    private static final int STATUS_STOP = 0;

    /**
     * 状态：运行
     */
    private static final int STATUS_RUNNING = 1;

    /**
     * 预读时间窗（毫秒），对齐 JobProperties.ClusterConfig.DEFAULT_PRE_READ_MS
     */
    private static final long PRE_READ_MS = 5000L;

    /**
     * 发现服务任务缺省间隔（毫秒）
     */
    private static final long DISCOVERY_DEFAULT_INTERVAL_MS = 30000L;

    /**
     * 调度平台属性
     */
    private final SchedulerJobPlatformProperties platformProperties;

    /**
     * 命名空间表解析器
     */
    private final SchedulerNamespaceTableResolver namespaceTableResolver;

    /**
     * 远程执行器分发器
     */
    private final SchedulerRemoteExecutorDispatcher remoteExecutorDispatcher;

    /**
     * 名称参数 JDBC 模板
     */
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Bean 工厂
     */
    private final ListableBeanFactory beanFactory;

    /**
     * 作业 row 映射器
     */
    private final BeanPropertyRowMapper<JobEntity> jobRowMapper = BeanPropertyRowMapper.newInstance(JobEntity.class);

    /**
     * 命名空间锁
     */
    private final Map<String, ReentrantLock> namespaceLocks = new ConcurrentHashMap<>();

    /**
     * 发现服务下一个触发时间
     */
    private final Map<String, Long> discoveryNextTriggerTimes = new ConcurrentHashMap<>();

    /**
     * 命名空间空闲状态
     */
    private final Map<String, IdleState> namespaceIdleStates = new ConcurrentHashMap<>();

    /**
     * 发现服务类
     */
    private static final String DISCOVERY_SERVICE_CLASS = "com.chua.starter.discovery.support.service.DiscoveryService";

    /**
     * SQL 安全标识符
     */
    private static final Pattern SAFE_SQL_IDENTIFIER = Pattern.compile("^[a-zA-Z0-9_.]+$");

    /**
     * 分发到期任务
     */
    @Scheduled(fixedDelayString = "${plugin.scheduler.job-platform.dispatch-interval-ms:1000}")
    public void dispatchDueJobs() {
        if (!platformProperties.isRemoteDispatchEnabled() || platformProperties.getNamespaces() == null) {
            return;
        }

        for (String namespace : namespaceTableResolver.namespaces()) {
            if (!remoteExecutorDispatcher.hasExecutors(namespace)) {
                continue;
            }
            if (shouldSkipDueToIdle(namespace)) {
                continue;
            }
            dispatchNamespace(namespace);
        }
    }

    /**
     * 空闲回退：连续多次空轮询后跳过部分调度周期，降低无任务时的 DB 压力。
     *
     * @param namespace 命名空间
     * @return 应跳过则返回 true
     */
    private boolean shouldSkipDueToIdle(String namespace) {
        IdleState state = namespaceIdleStates.get(namespace.toLowerCase(Locale.ROOT));
        if (state == null || state.consecutiveEmptyCycles < 3) {
            return false;
        }
        int cycles = state.consecutiveEmptyCycles;
        if (cycles >= 30) {
            // 连续 30 次空 → 每 10 次只执行 1 次
            return (state.totalCycles % 10) != 0;
        }
        if (cycles >= 10) {
            // 连续 10 次空 → 每 5 次只执行 1 次
            return (state.totalCycles % 5) != 0;
        }
        // 连续 3 次空 → 每 2 次只执行 1 次
        return (state.totalCycles % 2) != 0;
    }

    /**
     * 判断指定命名空间是否处于集群模式。
     *
     * @param namespace 命名空间
     * @return 集群模式返回 true
     */
    public boolean isClusterMode(String namespace) {
        if (!StringUtils.hasText(namespace) || platformProperties.getNamespaces() == null) {
            return false;
        }
        SchedulerJobPlatformProperties.Namespace nsProps = platformProperties.getNamespaces().entrySet().stream()
                .filter(e -> namespace.equalsIgnoreCase(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        if (nsProps == null) {
            return false;
        }
        return "CLUSTER".equalsIgnoreCase(nsProps.getMode());
    }

    /**
     * 列出指定命名空间下发现服务中的任务（仅用于展示，不入库）。
     *
     * @param namespace 命名空间
     * @return 发现任务列表
     */
    public List<JobEntity> listDiscoveryTasks(String namespace) {
        Object discoveryService = resolveDiscoveryService();
        if (discoveryService == null) {
            return List.of();
        }

        SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
        if (nsProps == null) {
            return List.of();
        }

        SchedulerJobPlatformProperties.Discovery discoveryProps = nsProps.getDiscovery();
        if (discoveryProps != null && !discoveryProps.isEnabled()) {
            return List.of();
        }

        String discoveryNsPath = resolveDiscoveryNamespacePath(namespace, discoveryProps);
        if (!StringUtils.hasText(discoveryNsPath)) {
            return List.of();
        }

        Set<Discovery> discoveries = loadDiscoveryInstances(discoveryService, discoveryProps, discoveryNsPath);
        if (discoveries == null || discoveries.isEmpty()) {
            return List.of();
        }

        // 加载已禁用的发现服务处理器
        Set<String> disabledHandlers = loadDiscoveryDisabledHandlers(namespace);

        List<JobEntity> result = new ArrayList<>();
        for (Discovery discovery : discoveries) {
            if (discovery == null) {
                continue;
            }

            Map<String, String> metadata = discovery.getMetadata();
            if (metadata == null || !metadata.containsKey("jobs")) {
                continue;
            }

            String jobsValue = metadata.get("jobs");
            if (!StringUtils.hasText(jobsValue)) {
                continue;
            }

            String[] handlerNames = jobsValue.split(",");
            Map<String, Map<String, String>> jobConfigs = parseJobConfigs(metadata.get("job_configs"));

            for (String handlerName : handlerNames) {
                handlerName = handlerName.trim();
                if (!StringUtils.hasText(handlerName)) {
                    continue;
                }

                // 跳过已禁用的处理器
                if (disabledHandlers.contains(handlerName)) {
                    continue;
                }

                Map<String, String> config = (jobConfigs != null) ? jobConfigs.get(handlerName) : null;
                result.add(createDiscoveryJob(handlerName, config));
            }
        }
        return result;
    }

    /**
     * 禁用发现服务任务。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void disableDiscoveryTask(String namespace, String handlerName) {
        upsertDiscoveryControlRecord(namespace, handlerName, STATUS_STOP, 0);
        log.info("[Scheduler] 发现服务任务已禁用: namespace={}, handler={}", namespace, handlerName);
    }

    /**
     * 启用发现服务任务。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void enableDiscoveryTask(String namespace, String handlerName) {
        upsertDiscoveryControlRecord(namespace, handlerName, STATUS_RUNNING, 0);
        log.info("[Scheduler] 发现服务任务已启用: namespace={}, handler={}", namespace, handlerName);
    }

    /**
     * 删除发现服务任务（逻辑删除）。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscoveryTask(String namespace, String handlerName) {
        upsertDiscoveryControlRecord(namespace, handlerName, STATUS_STOP, 1);
        log.info("[Scheduler] 发现服务任务已删除: namespace={}, handler={}", namespace, handlerName);
    }

    /**
     * 恢复已删除的发现服务任务。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreDiscoveryTask(String namespace, String handlerName) {
        upsertDiscoveryControlRecord(namespace, handlerName, STATUS_RUNNING, 0);
        log.info("[Scheduler] 发现服务任务已恢复: namespace={}, handler={}", namespace, handlerName);
    }

    /**
     * 查询发现服务任务在 DB 中的控制记录状态。
     * 返回空表示没有控制记录（默认启用）。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     * @return 发现作业控制记录
     */
    public JobEntity findDiscoveryControlRecord(String namespace, String handlerName) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        String table = safeIdentifier(tables.jobTable());
        String discJobNo = discoveryJobNo(handlerName);
        MapSqlParameterSource params = new MapSqlParameterSource("jobNo", discJobNo);
        List<JobEntity> records = namedParameterJdbcTemplate.query(
                "SELECT * FROM " + table + " WHERE sys_job_no = :jobNo LIMIT 1",
                params,
                jobRowMapper
        );
        return records.isEmpty() ? null : records.get(0);
    }

    /**
     * 生成发现服务任务编号。
     *
     * @param handlerName 处理器名称
     * @return 任务编号
     */
    private static String discoveryJobNo(String handlerName) {
        return "disc:" + handlerName;
    }

    /**
     * 加载命名空间中已被禁用的发现服务处理器名称集合。
     *
     * @param namespace 命名空间
     * @return 已禁用处理器集合
     */
    private Set<String> loadDiscoveryDisabledHandlers(String namespace) {
        try {
            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
            String table = safeIdentifier(tables.jobTable());
            MapSqlParameterSource params = new MapSqlParameterSource();
            List<String> handlers = namedParameterJdbcTemplate.query(
                    "SELECT sys_job_execute_bean FROM " + table
                            + " WHERE sys_job_no LIKE 'disc:%'"
                            + " AND (sys_job_trigger_status = 0 OR sys_job_is_deleted = 1)",
                    params,
                    (rs, rowNum) -> rs.getString("sys_job_execute_bean")
            );
            Set<String> result = new HashSet<>();
            for (String h : handlers) {
                if (StringUtils.hasText(h)) {
                    result.add(h.trim());
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("[Scheduler] 加载发现服务禁用列表失败: namespace={}", namespace, e);
            return Set.of();
        }
    }

    /**
     * 插入或更新发现服务任务的控制记录。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     * @param status      状态
     * @param deleted     删除标记
     */
    private void upsertDiscoveryControlRecord(String namespace, String handlerName, int status, int deleted) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        String table = safeIdentifier(tables.jobTable());
        String discJobNo = discoveryJobNo(handlerName);

        // 先检查是否已有记录
        MapSqlParameterSource checkParams = new MapSqlParameterSource("jobNo", discJobNo);
        Integer exists = namedParameterJdbcTemplate.query(
                "SELECT COUNT(1) FROM " + table + " WHERE sys_job_no = :jobNo",
                checkParams,
                (rs) -> rs.next() ? rs.getInt(1) : 0
        );

        if (exists != null && exists > 0) {
            // 更新已有记录
            MapSqlParameterSource updateParams = new MapSqlParameterSource();
            updateParams.addValue("jobNo", discJobNo);
            updateParams.addValue("status", status);
            updateParams.addValue("deleted", deleted);
            namedParameterJdbcTemplate.update(
                    "UPDATE " + table
                            + " SET sys_job_trigger_status = :status, sys_job_is_deleted = :deleted"
                            + " WHERE sys_job_no = :jobNo",
                    updateParams
            );
        } else {
            // 插入新记录
            long jobId = Objects.hash("disc", handlerName) & 0x7FFFFFFF;
            MapSqlParameterSource insertParams = new MapSqlParameterSource();
            insertParams.addValue("jobId", jobId);
            insertParams.addValue("jobNo", discJobNo);
            insertParams.addValue("jobName", handlerName);
            insertParams.addValue("jobScheduleType", "CRON");
            insertParams.addValue("jobScheduleTime", "");
            insertParams.addValue("jobGlueType", "BEAN");
            insertParams.addValue("jobDispatchMode", "REMOTE");
            insertParams.addValue("jobExecuteBean", handlerName);
            insertParams.addValue("jobTriggerStatus", status);
            insertParams.addValue("isDeleted", deleted);
            insertParams.addValue("jobTriggerNextTime", 0L);
            insertParams.addValue("jobTriggerLastTime", 0L);

            namedParameterJdbcTemplate.update(
                    "INSERT INTO " + table + " ("
                            + "sys_job_id, sys_job_no, sys_job_name, sys_job_schedule_type, sys_job_schedule_time,"
                            + "sys_job_glue_type, sys_job_dispatch_mode, sys_job_execute_bean,"
                            + "sys_job_trigger_status, sys_job_is_deleted, sys_job_trigger_next_time, sys_job_trigger_last_time"
                            + ") VALUES ("
                            + ":jobId, :jobNo, :jobName, :jobScheduleType, :jobScheduleTime,"
                            + ":jobGlueType, :jobDispatchMode, :jobExecuteBean,"
                            + ":jobTriggerStatus, :isDeleted, :jobTriggerNextTime, :jobTriggerLastTime"
                            + ")",
                    insertParams
            );
        }
    }

    /**
     * 分发命名空间任务。
     *
     * @param namespace 命名空间
     */
    private void dispatchNamespace(String namespace) {
        ReentrantLock lock = namespaceLocks.computeIfAbsent(namespace.toLowerCase(Locale.ROOT), key -> new ReentrantLock());
        if (!lock.tryLock()) {
            return;
        }
        boolean clusterMode = isClusterMode(namespace);
        String nsKey = namespace.toLowerCase(Locale.ROOT);
        try {
            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
            SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
            List<JobEntity> dueJobs = loadDueJobs(tables, clusterMode, nsProps);
            IdleState state = namespaceIdleStates.computeIfAbsent(nsKey, k -> new IdleState());
            if (dueJobs.isEmpty()) {
                state.consecutiveEmptyCycles++;
                state.totalCycles++;
            } else {
                state.consecutiveEmptyCycles = 0;
                state.totalCycles = 0;
            }
            for (JobEntity job : dueJobs) {
                processJob(namespace, tables, job, clusterMode);
            }
        } finally {
            lock.unlock();
        }

        // 从发现服务中获取任务并调度
        dispatchDiscoveryTasks(namespace);
    }

    /**
     * 加载到期任务。
     *
     * @param tables      命名空间表
     * @param clusterMode 是否集群模式
     * @param nsProps     命名空间配置
     * @return 到期任务列表
     */
    private List<JobEntity> loadDueJobs(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables,
                                         boolean clusterMode,
                                         SchedulerJobPlatformProperties.Namespace nsProps) {
        String table = safeIdentifier(tables.jobTable());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", STATUS_RUNNING);
        parameters.addValue("now", System.currentTimeMillis());
        parameters.addValue("limit", Math.max(1, platformProperties.getDispatchBatchSize()));

        String group = resolveGroup(nsProps);
        String groupFilter = "";
        if (StringUtils.hasText(group)) {
            parameters.addValue("group", group);
            groupFilter = " AND (sys_job_group IS NULL OR sys_job_group = :group)";
        }

        String sql;
        if (clusterMode) {
            // 集群模式：拾取所有到期任务（包括本地和远程）
            sql = "SELECT * FROM " + table
                    + " WHERE sys_job_trigger_status = :status"
                    + " AND sys_job_trigger_next_time > 0"
                    + " AND sys_job_trigger_next_time <= :now"
                    + groupFilter
                    + " ORDER BY sys_job_trigger_next_time ASC, sys_job_id ASC LIMIT :limit";
        } else {
            // 单机模式：仅拾取远程类型任务
            parameters.addValue("dispatchMode", "REMOTE");
            sql = "SELECT * FROM " + table
                    + " WHERE sys_job_trigger_status = :status"
                    + " AND sys_job_dispatch_mode = :dispatchMode"
                    + " AND sys_job_trigger_next_time > 0"
                    + " AND sys_job_trigger_next_time <= :now"
                    + groupFilter
                    + " ORDER BY sys_job_trigger_next_time ASC, sys_job_id ASC LIMIT :limit";
        }
        return namedParameterJdbcTemplate.query(sql, parameters, jobRowMapper);
    }

    /**
     * 解析命名空间分组。
     *
     * @param nsProps 命名空间配置
     * @return 分组值，未配置返回 空
     */
    private String resolveGroup(SchedulerJobPlatformProperties.Namespace nsProps) {
        if (nsProps == null) {
            return null;
        }
        return StringUtils.hasText(nsProps.getGroup()) ? nsProps.getGroup().trim() : null;
    }

    /**
     * 处理单个任务。
     *
     * @param namespace 命名空间
     * @param tables    命名空间表
     * @param job       任务
     * @param clusterMode 是否集群模式
     */
    private void processJob(String namespace,
                            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables,
                            JobEntity job,
                            boolean clusterMode) {
        if (!canDispatch(job, clusterMode)) {
            log.warn("[Scheduler] 跳过远程分发，任务不满足远程执行条件: namespace={}, jobNo={}, jobId={}, dispatchMode={}, glueType={}, handler={}",
                    namespace, job.getSysJobNo(), job.getSysJobId(), job.getSysJobDispatchMode(), job.getSysJobGlueType(), job.getSysJobExecuteBean());
            return;
        }

        DispatchPlan plan;
        try {
            plan = buildPlan(job);
        } catch (Exception e) {
            log.error("[Scheduler] 计算远程任务下次触发时间失败: namespace={}, jobNo={}, jobId={}, error={}",
                    namespace, job.getSysJobNo(), job.getSysJobId(), e.getMessage(), e);
            return;
        }

        if (!claimJob(tables.jobTable(), job, plan)) {
            return;
        }

        if (!plan.shouldDispatch()) {
            return;
        }

        try {
            remoteExecutorDispatcher.dispatch(namespace, job, job.getSysJobExecuteParam(), clusterMode);
        } catch (Exception e) {
            rollbackClaim(tables.jobTable(), job, plan);
            log.error("[Scheduler] 远程任务下发失败，已回滚触发时间: namespace={}, jobNo={}, jobId={}, error={}",
                    namespace, job.getSysJobNo(), job.getSysJobId(), e.getMessage(), e);
        }
    }

    /**
     * 构建分发计划。
     *
     * @param job 任务
     * @return 分发计划
     */
    private DispatchPlan buildPlan(JobEntity job) {
        long now = System.currentTimeMillis();
        long currentNextTime = safeLong(job.getSysJobTriggerNextTime());
        long currentLastTime = safeLong(job.getSysJobTriggerLastTime());
        boolean misfire = currentNextTime > 0 && now > currentNextTime + PRE_READ_MS;
        String misfireStrategy = matchMisfireStrategy(job.getSysJobExecuteMisfireStrategy(), "DO_NOTHING");
        boolean shouldDispatch = !misfire || "FIRE_ONCE_NOW".equals(misfireStrategy);

        Date nextValidTime = SchedulerNamespaceTableResolver.generateNextValidTime(job, new Date());
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

    /**
     * 匹配错失触发策略。
     *
     * @param value     策略值
     * @param fallback  缺省策略
     * @return 匹配后的策略
     */
    private static String matchMisfireStrategy(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("DO_NOTHING".equals(normalized) || "FIRE_ONCE_NOW".equals(normalized)) {
            return normalized;
        }
        return fallback;
    }

    /**
     * 抢占任务。
     *
     * @param tableName 表名
     * @param job       任务
     * @param plan      分发计划
     * @return 抢占成功返回 true
     */
    private boolean claimJob(String tableName, JobEntity job, DispatchPlan plan) {
        String table = safeIdentifier(tableName);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getSysJobId());
        parameters.addValue("expectedStatus", plan.previousStatus());
        parameters.addValue("expectedNextTime", plan.previousNextTime());
        parameters.addValue("nextStatus", plan.nextStatus());
        parameters.addValue("nextLastTime", plan.nextLastTime());
        parameters.addValue("nextNextTime", plan.nextNextTime());
        int updated = namedParameterJdbcTemplate.update(
                "UPDATE " + table
                        + " SET sys_job_trigger_status = :nextStatus,"
                        + " sys_job_trigger_last_time = :nextLastTime,"
                        + " sys_job_trigger_next_time = :nextNextTime"
                        + " WHERE sys_job_id = :jobId"
                        + " AND sys_job_trigger_status = :expectedStatus"
                        + " AND sys_job_trigger_next_time = :expectedNextTime",
                parameters
        );
        return updated > 0;
    }

    /**
     * 回滚抢占。
     *
     * @param tableName 表名
     * @param job       任务
     * @param plan      分发计划
     */
    private void rollbackClaim(String tableName, JobEntity job, DispatchPlan plan) {
        String table = safeIdentifier(tableName);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getSysJobId());
        parameters.addValue("currentStatus", plan.nextStatus());
        parameters.addValue("currentLastTime", plan.nextLastTime());
        parameters.addValue("currentNextTime", plan.nextNextTime());
        parameters.addValue("rollbackStatus", plan.previousStatus());
        parameters.addValue("rollbackLastTime", plan.previousLastTime());
        parameters.addValue("rollbackNextTime", plan.previousNextTime());
        namedParameterJdbcTemplate.update(
                "UPDATE " + table
                        + " SET sys_job_trigger_status = :rollbackStatus,"
                        + " sys_job_trigger_last_time = :rollbackLastTime,"
                        + " sys_job_trigger_next_time = :rollbackNextTime"
                        + " WHERE sys_job_id = :jobId"
                        + " AND sys_job_trigger_status = :currentStatus"
                        + " AND sys_job_trigger_last_time = :currentLastTime"
                        + " AND sys_job_trigger_next_time = :currentNextTime",
                parameters
        );
    }

    /**
     * 判断任务能否分发。
     *
     * @param job         任务
     * @param clusterMode 是否集群模式
     * @return 可分发返回 true
     */
    private boolean canDispatch(JobEntity job, boolean clusterMode) {
        if (job == null || !StringUtils.hasText(job.getSysJobExecuteBean())) {
            return false;
        }
        if (StringUtils.hasText(job.getSysJobGlueType()) && !"BEAN".equalsIgnoreCase(job.getSysJobGlueType())) {
            return false;
        }
        if (clusterMode) {
            // 集群模式：所有任务都可以远程下发
            return true;
        }
        // 单机模式：仅远程类型任务
        return "REMOTE".equalsIgnoreCase(job.getSysJobDispatchMode());
    }

    /**
     * 安全转换长整型。
     *
     * @param value 值
     * @return 非空值，空值返回 0
     */
    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 校验并返回可用于 SQL 拼接的标识符（表名/列名）。
     *
     * @param identifier 标识符
     * @return 合法的 SQL 标识符
     */
    private String safeIdentifier(String identifier) {
        if (identifier == null || !SAFE_SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("非法的 SQL 标识符: " + identifier);
        }
        return identifier;
    }

    // ==================== 发现服务任务调度 ====================

    /**
     * 从发现服务中获取任务元数据并调度到期任务。
     *
     * @param namespace 命名空间
     */
    private void dispatchDiscoveryTasks(String namespace) {
        if (!platformProperties.isRemoteDispatchEnabled()) {
            return;
        }

        Object discoveryService = resolveDiscoveryService();
        if (discoveryService == null) {
            return;
        }

        SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
        if (nsProps == null) {
            return;
        }

        SchedulerJobPlatformProperties.Discovery discoveryProps = nsProps.getDiscovery();
        if (discoveryProps != null && !discoveryProps.isEnabled()) {
            return;
        }

        String discoveryNsPath = resolveDiscoveryNamespacePath(namespace, discoveryProps);
        if (!StringUtils.hasText(discoveryNsPath)) {
            return;
        }

        Set<Discovery> discoveries = loadDiscoveryInstances(discoveryService, discoveryProps, discoveryNsPath);
        if (discoveries == null || discoveries.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        // 加载已禁用的发现服务处理器
        Set<String> disabledHandlers = loadDiscoveryDisabledHandlers(namespace);

        for (Discovery discovery : discoveries) {
            if (discovery == null) {
                continue;
            }

            Map<String, String> metadata = discovery.getMetadata();
            if (metadata == null || !metadata.containsKey("jobs")) {
                continue;
            }

            String jobsValue = metadata.get("jobs");
            if (!StringUtils.hasText(jobsValue)) {
                continue;
            }

            String[] handlerNames = jobsValue.split(",");
            Map<String, Map<String, String>> jobConfigs = parseJobConfigs(metadata.get("job_configs"));

            for (String handlerName : handlerNames) {
                handlerName = handlerName.trim();
                if (!StringUtils.hasText(handlerName)) {
                    continue;
                }

                // 跳过已禁用的处理器
                if (disabledHandlers.contains(handlerName)) {
                    discoveryNextTriggerTimes.remove(discoveryCacheKey(namespace, handlerName));
                    continue;
                }

                String cacheKey = discoveryCacheKey(namespace, handlerName);
                Long scheduledTime = discoveryNextTriggerTimes.get(cacheKey);
                if (scheduledTime != null && now < scheduledTime) {
                    continue;
                }

                // 到期，执行调度
                Map<String, String> config = (jobConfigs != null) ? jobConfigs.get(handlerName) : null;

                try {
                    JobEntity transientJob = createDiscoveryJob(handlerName, config);
                    remoteExecutorDispatcher.dispatch(namespace, transientJob, transientJob.getSysJobExecuteParam());
                } catch (Exception e) {
                    log.warn("[Scheduler] 发现服务任务下发失败: namespace={}, handler={}", namespace, handlerName, e);
                }

                // 计算下次触发时间
                long nextTime = computeDiscoveryNextTime(handlerName, config, now);
                discoveryNextTriggerTimes.put(cacheKey, nextTime);
            }
        }
    }

    /**
     * 生成发现服务缓存键。
     *
     * @param namespace   命名空间
     * @param handlerName 处理器名称
     * @return 缓存键
     */
    private static String discoveryCacheKey(String namespace, String handlerName) {
        return namespace + ":" + handlerName;
    }

    /**
     * 通过反射获取发现服务 Bean。
     *
     * @return 发现服务实例，不存在返回 空
     */
    private Object resolveDiscoveryService() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SchedulerNamespaceRemoteDispatchScheduler.class.getClassLoader();
        }
        if (!ClassUtils.isPresent(DISCOVERY_SERVICE_CLASS, classLoader)) {
            return null;
        }
        try {
            Class<?> discoveryServiceType = ClassUtils.forName(DISCOVERY_SERVICE_CLASS, classLoader);
            Map<String, ?> beans = beanFactory.getBeansOfType(discoveryServiceType);
            if (beans.isEmpty()) {
                return null;
            }
            return beans.values().iterator().next();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通过反射加载发现服务实例列表。
     *
     * @param discoveryService 发现服务
     * @param discoveryProps   发现配置
     * @param discoveryNamespace 发现命名空间
     * @return 发现实例集合
     */
    @SuppressWarnings({"unchecked", "java:S1186"})
    private Set<Discovery> loadDiscoveryInstances(Object discoveryService,
                                                    SchedulerJobPlatformProperties.Discovery discoveryProps,
                                                    String discoveryNamespace) {
        try {
            String discoveryProtocol = (discoveryProps == null) ? null : trimToNull(discoveryProps.getProtocol());
 // 服务实例来自外部模块，方法调用统一经 ReflectUtils（getDiscoveryAll 返回 Set<Discovery>）
            Object result;
            if (StringUtils.hasText(discoveryProtocol)) {
                result = ReflectUtils.invoke(discoveryService, "getDiscoveryAll", Set.class,
                        new Class<?>[]{String.class, String.class}, discoveryProtocol, discoveryNamespace);
            } else {
                result = ReflectUtils.invoke(discoveryService, "getDiscoveryAll", Set.class,
                        new Class<?>[]{String.class}, discoveryNamespace);
            }
            if (result == null) {
                return Set.of();
            }
            return (Set<Discovery>) result;
        } catch (Exception e) {
            return Set.of();
        }
    }

    /**
     * 解析发现命名空间路径。
     *
     * @param namespace      命名空间
     * @param discoveryProps 发现配置
     * @return 发现命名空间路径
     */
    private String resolveDiscoveryNamespacePath(String namespace,
                                                  SchedulerJobPlatformProperties.Discovery discoveryProps) {
        if (discoveryProps != null && StringUtils.hasText(discoveryProps.getNamespace())) {
            return discoveryProps.getNamespace().trim();
        }
        return StringUtils.hasText(namespace) ? "/job-executor/" + namespace.trim() : null;
    }

    /**
     * 解析命名空间配置。
     *
     * @param namespace 命名空间
     * @return 命名空间配置，未配置返回 空
     */
    private SchedulerJobPlatformProperties.Namespace resolveNamespaceProperties(String namespace) {
        if (!StringUtils.hasText(namespace) || platformProperties.getNamespaces() == null) {
            return null;
        }
        return platformProperties.getNamespaces().entrySet().stream()
                .filter(e -> namespace.equalsIgnoreCase(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * 解析作业配置元数据 JSON 为映射。
     *
     * @param jobConfigsJson 作业配置 JSON
     * @return 作业配置映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> parseJobConfigs(String jobConfigsJson) {
        if (!StringUtils.hasText(jobConfigsJson)) {
            return Map.of();
        }
        try {
            Map<String, Object> parsed = Json.fromJson(jobConfigsJson);
            if (parsed == null || parsed.isEmpty()) {
                return Map.of();
            }

            Map<String, Map<String, String>> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : parsed.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> configMap = (Map<String, Object>) entry.getValue();
                    Map<String, String> stringConfig = new HashMap<>();
                    for (Map.Entry<String, Object> configEntry : configMap.entrySet()) {
                        if (configEntry.getValue() != null) {
                            stringConfig.put(configEntry.getKey(), configEntry.getValue().toString());
                        }
                    }
                    result.put(entry.getKey(), stringConfig);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("[Scheduler] 解析发现任务配置失败: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 计算发现服务任务的下次触发时间。
     *
     * @param handlerName 处理器名称
     * @param config      配置
     * @param now         当前时间
     * @return 下次触发时间
     */
    private long computeDiscoveryNextTime(String handlerName, Map<String, String> config, long now) {
        if (config == null) {
            return now + DISCOVERY_DEFAULT_INTERVAL_MS;
        }

        String scheduleType = config.getOrDefault("scheduleType", "cron");
        String scheduleTime = config.get("scheduleTime");

        if (!StringUtils.hasText(scheduleTime)) {
            return now + DISCOVERY_DEFAULT_INTERVAL_MS;
        }

        try {
            JobEntity template = new JobEntity();
            template.setSysJobScheduleType(scheduleType);
            template.setSysJobScheduleTime(scheduleTime);
            template.setSysJobTriggerLastTime(now);
            template.setSysJobTriggerNextTime(now);

            Date next = SchedulerNamespaceTableResolver.generateNextValidTime(template, new Date(now));
            if (next != null) {
                return next.getTime();
            }
        } catch (Exception e) {
            log.debug("[Scheduler] 计算发现任务下次触发时间失败: handler={}, type={}, time={}",
                    handlerName, scheduleType, scheduleTime);
        }
        return now + DISCOVERY_DEFAULT_INTERVAL_MS;
    }

    /**
     * 为发现服务来源的任务创建临时任务对象。
     *
     * @param handlerName 处理器名称
     * @param config      执行配置
     * @return 发现任务对象
     */
    private JobEntity createDiscoveryJob(String handlerName, Map<String, String> config) {
        JobEntity job = new JobEntity();
        job.setSysJobId((long) (Objects.hash("disc", handlerName) & 0x7FFFFFFF));
        job.setSysJobNo("disc:" + handlerName);
        job.setSysJobName(handlerName);
        job.setSysJobExecuteBean(handlerName);
        job.setSysJobGlueType("BEAN");
        job.setSysJobDispatchMode("REMOTE");
        job.setSysJobTriggerStatus(STATUS_RUNNING);
        job.setSysJobTriggerNextTime(System.currentTimeMillis());

        if (config != null) {
            job.setSysJobScheduleType(config.getOrDefault("scheduleType", "cron"));
            job.setSysJobScheduleTime(config.get("scheduleTime"));

            if (config.containsKey("executeTimeout")) {
                try {
                    job.setSysJobExecuteTimeout(Integer.parseInt(config.get("executeTimeout")));
                } catch (NumberFormatException ignored) {
                    // 忽略无效超时配置
                }
            }
            if (config.containsKey("param")) {
                job.setSysJobExecuteParam(config.get("param"));
            }
            job.setSysJobExecuteMisfireStrategy(config.getOrDefault("misfireStrategy", "DO_NOTHING"));
        }

        return job;
    }

    /**
     * 去除空白，空白文本转换为 空。
     *
     * @param value 文本值
     * @return 去除空白后的文本，空白返回 空
     */
    private String trimToNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    /**
     * 一次触发推进前后的状态快照。
     *
     * @since 4.0.0
     * @author CH
     */
    private record DispatchPlan(long previousLastTime,
                                long previousNextTime,
                                int previousStatus,
                                long nextLastTime,
                                long nextNextTime,
                                int nextStatus,
                                boolean shouldDispatch) {
    }

    /**
     * 命名空间空闲状态，用于控制空轮询时的调度频率。
     *
     * @since 4.0.0
     * @author CH
     */
    private static class IdleState {
        /**
         * 连续空循环计数
         */
        int consecutiveEmptyCycles;

        /**
         * 总循环计数
         */
        int totalCycles;
    }
}
