package com.chua.starter.scheduler.support;

import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.text.json.Json;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.scheduler.JobDispatchModeEnum;
import com.chua.starter.job.support.scheduler.JobHelper;
import com.chua.starter.job.support.scheduler.MisfireStrategyEnum;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final ListableBeanFactory beanFactory;
    private final BeanPropertyRowMapper<SysJob> jobRowMapper = BeanPropertyRowMapper.newInstance(SysJob.class);
    private final Map<String, ReentrantLock> namespaceLocks = new ConcurrentHashMap<>();
    private final Map<String, Long> discoveryNextTriggerTimes = new ConcurrentHashMap<>();

    private static final String DISCOVERY_SERVICE_CLASS = "com.chua.starter.discovery.support.service.DiscoveryService";

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

    /**
     * 判断指定命名空间是否处于集群模式。
     * <p>
     * 集群模式下，调度中心统一拾取所有到期任务（包括 LOCAL 和 REMOTE），
     * 推送到远程执行器执行，本地 job-starter 不再自行触发。
     * </p>
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

    private void dispatchNamespace(String namespace) {
        ReentrantLock lock = namespaceLocks.computeIfAbsent(namespace.toLowerCase(), key -> new ReentrantLock());
        if (!lock.tryLock()) {
            return;
        }
        boolean clusterMode = isClusterMode(namespace);
        try {
            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
            List<SysJob> dueJobs = loadDueJobs(tables, clusterMode);
            for (SysJob job : dueJobs) {
                processJob(namespace, tables, job, clusterMode);
            }
        } finally {
            lock.unlock();
        }

        // 从发现服务中获取任务并调度
        dispatchDiscoveryTasks(namespace);
    }

    private List<SysJob> loadDueJobs(SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables,
                                     boolean clusterMode) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", STATUS_RUNNING);
        parameters.addValue("now", System.currentTimeMillis());
        parameters.addValue("limit", Math.max(1, platformProperties.getDispatchBatchSize()));

        String sql;
        if (clusterMode) {
            // 集群模式：拾取所有到期任务（包括 LOCAL 和 REMOTE）
            sql = "SELECT * FROM " + tables.jobTable()
                    + " WHERE job_trigger_status = :status"
                    + " AND job_trigger_next_time > 0"
                    + " AND job_trigger_next_time <= :now"
                    + " ORDER BY job_trigger_next_time ASC, job_id ASC LIMIT :limit";
        } else {
            // 单机模式：仅拾取 REMOTE 类型任务
            parameters.addValue("dispatchMode", JobDispatchModeEnum.REMOTE.name());
            sql = "SELECT * FROM " + tables.jobTable()
                    + " WHERE job_trigger_status = :status"
                    + " AND job_dispatch_mode = :dispatchMode"
                    + " AND job_trigger_next_time > 0"
                    + " AND job_trigger_next_time <= :now"
                    + " ORDER BY job_trigger_next_time ASC, job_id ASC LIMIT :limit";
        }
        return namedParameterJdbcTemplate.query(sql, parameters, jobRowMapper);
    }

    private void processJob(String namespace,
                            SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables,
                            SysJob job,
                            boolean clusterMode) {
        if (!canDispatch(job, clusterMode)) {
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

    private boolean canDispatch(SysJob job, boolean clusterMode) {
        if (job == null || !StringUtils.hasText(job.getJobExecuteBean())) {
            return false;
        }
        if (StringUtils.hasText(job.getJobGlueType()) && !"BEAN".equalsIgnoreCase(job.getJobGlueType())) {
            return false;
        }
        if (clusterMode) {
            // 集群模式：所有任务都可以远程下发
            return true;
        }
        // 单机模式：仅 REMOTE 类型任务
        return JobDispatchModeEnum.REMOTE == JobDispatchModeEnum.match(job.getJobDispatchMode());
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    // ==================== 发现服务任务调度 ====================

    /**
     * 从发现服务中获取任务元数据并调度到期任务。
     * <p>
     * 通过反射访问 DiscoveryService（避免编译期强依赖），读取各注册客户端的
     * {@code jobs} 和 {@code job_configs} 元数据，在内存中跟踪每个 handler
     * 的下次触发时间，到期后通过远程执行器分发器下发。
     * </p>
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

        for (Discovery discovery : discoveries) {
            if (discovery == null) continue;

            Map<String, String> metadata = discovery.getMetadata();
            if (metadata == null || !metadata.containsKey("jobs")) continue;

            String jobsValue = metadata.get("jobs");
            if (!StringUtils.hasText(jobsValue)) continue;

            String[] handlerNames = jobsValue.split(",");
            Map<String, Map<String, String>> jobConfigs = parseJobConfigs(metadata.get("job_configs"));

            for (String handlerName : handlerNames) {
                handlerName = handlerName.trim();
                if (!StringUtils.hasText(handlerName)) continue;

                String cacheKey = discoveryCacheKey(namespace, handlerName);
                Long scheduledTime = discoveryNextTriggerTimes.get(cacheKey);
                if (scheduledTime != null && now < scheduledTime) continue;

                // 到期，执行调度
                Map<String, String> config = (jobConfigs != null) ? jobConfigs.get(handlerName) : null;

                try {
                    SysJob transientJob = createDiscoveryJob(handlerName, config);
                    remoteExecutorDispatcher.dispatch(namespace, transientJob, transientJob.getJobExecuteParam());
                } catch (Exception e) {
                    log.warn("[Scheduler] 发现服务任务下发失败: namespace={}, handler={}", namespace, handlerName, e);
                }

                // 计算下次触发时间
                long nextTime = computeDiscoveryNextTime(handlerName, config, now);
                discoveryNextTriggerTimes.put(cacheKey, nextTime);
            }
        }
    }

    private static String discoveryCacheKey(String namespace, String handlerName) {
        return namespace + ":" + handlerName;
    }

    /**
     * 通过反射获取 DiscoveryService Bean。
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
     * 通过反射加载 Discovery 实例列表。
     */
    @SuppressWarnings("unchecked")
    private Set<Discovery> loadDiscoveryInstances(Object discoveryService,
                                                   SchedulerJobPlatformProperties.Discovery discoveryProps,
                                                   String discoveryNamespace) {
        try {
            String discoveryProtocol = (discoveryProps == null) ? null : trimToNull(discoveryProps.getProtocol());
            if (StringUtils.hasText(discoveryProtocol)) {
                Method method = discoveryService.getClass().getMethod("getDiscoveryAll", String.class, String.class);
                return (Set<Discovery>) method.invoke(discoveryService, discoveryProtocol, discoveryNamespace);
            }
            Method method = discoveryService.getClass().getMethod("getDiscoveryAll", String.class);
            return (Set<Discovery>) method.invoke(discoveryService, discoveryNamespace);
        } catch (Exception e) {
            return Set.of();
        }
    }

    /**
     * 解析发现命名空间路径。
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
     * 解析 job_configs 元数据 JSON 为 Map。
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
     */
    private long computeDiscoveryNextTime(String handlerName, Map<String, String> config, long now) {
        if (config == null) {
            return now + 30000L;
        }

        String scheduleType = config.getOrDefault("scheduleType", "cron");
        String scheduleTime = config.get("scheduleTime");

        if (!StringUtils.hasText(scheduleTime)) {
            return now + 30000L;
        }

        try {
            SysJob template = new SysJob();
            template.setJobScheduleType(scheduleType);
            template.setJobScheduleTime(scheduleTime);
            template.setJobTriggerLastTime(now);
            template.setJobTriggerNextTime(now);

            Date next = JobHelper.generateNextValidTime(template, new Date(now));
            if (next != null) {
                return next.getTime();
            }
        } catch (Exception e) {
            log.debug("[Scheduler] 计算发现任务下次触发时间失败: handler={}, type={}, time={}",
                    handlerName, scheduleType, scheduleTime);
        }
        return now + 30000L;
    }

    /**
     * 为发现服务来源的任务创建临时 SysJob 对象。
     * <p>
     * 该对象仅用于传递 handler 名称和执行配置到远程执行器分发器，
     * 不会持久化到数据库。
     * </p>
     */
    private SysJob createDiscoveryJob(String handlerName, Map<String, String> config) {
        SysJob job = new SysJob();
        job.setJobId(Objects.hash("disc", handlerName) & 0x7FFFFFFF);
        job.setJobNo("disc:" + handlerName);
        job.setJobName(handlerName);
        job.setJobExecuteBean(handlerName);
        job.setJobGlueType("BEAN");
        job.setJobDispatchMode(JobDispatchModeEnum.REMOTE.name());
        job.setJobTriggerStatus(1);
        job.setJobTriggerNextTime(System.currentTimeMillis());

        if (config != null) {
            job.setJobScheduleType(config.getOrDefault("scheduleType", "cron"));
            job.setJobScheduleTime(config.get("scheduleTime"));

            if (config.containsKey("executeTimeout")) {
                try {
                    job.setJobExecuteTimeout(Integer.parseInt(config.get("executeTimeout")));
                } catch (NumberFormatException ignored) {
                }
            }
            if (config.containsKey("param")) {
                job.setJobExecuteParam(config.get("param"));
            }
            job.setJobExecuteMisfireStrategy(config.getOrDefault("misfireStrategy", "DO_NOTHING"));
        }

        return job;
    }

    private String trimToNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
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
