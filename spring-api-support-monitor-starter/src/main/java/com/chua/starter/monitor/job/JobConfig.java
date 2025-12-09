package com.chua.starter.monitor.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.report.client.starter.job.TriggerParam;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.entity.MonitorJob;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.job.lock.JobLock;
import com.chua.starter.monitor.job.lock.RedisLock;
import com.chua.starter.monitor.job.route.ExecutorRouter;
import com.chua.starter.monitor.properties.ReportJobProperties;
import com.chua.starter.monitor.sender.MonitorSender;
import com.chua.starter.monitor.service.MonitorAppService;
import com.chua.starter.monitor.service.MonitorJobLogService;
import com.chua.starter.monitor.service.MonitorJobService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作业配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/03/08
 */
public class JobConfig {

    private static final JobConfig INSTANCE = new JobConfig();
    
    /**
     * 调度任务查询缓存
     * key: 缓存键 (时间戳_数量)
     * value: 缓存数据包装对象
     */
    private final ConcurrentHashMap<String, ScheduleJobCacheEntry> scheduleJobCache = new ConcurrentHashMap<>();
    
    /**
     * 缓存过期时间（毫秒），默认3秒
     */
    private static final long CACHE_EXPIRE_MS = 3000L;
    
    /**
     * 缓存版本号，用于快速失效缓存
     */
    private final AtomicLong cacheVersion = new AtomicLong(0);
    private ReportJobProperties reportJobProperties;
    private ApplicationContext applicationContext;
    private MonitorSender monitorSender;
    private MonitorJobService monitorJobService;
    private MonitorJobLogService monitorJobLogService;
    private RedissonClient redissonClient;
    private MonitorAppService monitorAppService;
    private SyncServer syncServer;

    public static JobConfig getInstance() {
        return INSTANCE;
    }


    public void register(ReportJobProperties reportJobProperties) {
        this.reportJobProperties = reportJobProperties;
    }

    /**
     * 获取快速最大触发池
     *
     * @return int
     */
    public int getTriggerPoolFastMax() {
        return null == reportJobProperties ? 200 : reportJobProperties.getTriggerPoolFastMax();
    }

    /**
     * 获取触发池慢速最大值
     *
     * @return int
     */
    public int getTriggerPoolSlowMax() {
        return null == reportJobProperties ? 100 : reportJobProperties.getTriggerPoolSlowMax();
    }

    /**
     * 通过作业ID加载监控作业。
     *
     * @param jobId 作业ID
     * @return 返回MonitorJob对象，如果找不到则返回null。
     */
    public MonitorJob loadById(int jobId) {
        return monitorJobService.getById(jobId);
    }

    /**
     * 注册应用上下文。
     *
     * @param applicationContext 应用上下文实例
     */
    public void register(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 从应用上下文中获取各种服务的实例
        this.monitorJobService = applicationContext.getBean(MonitorJobService.class);
        this.monitorSender = applicationContext.getBean(MonitorSender.class);
        this.monitorJobLogService = applicationContext.getBean(MonitorJobLogService.class);
        this.redissonClient = applicationContext.getBean(RedissonClient.class);
        this.monitorAppService = applicationContext.getBean(MonitorAppService.class);
        // SyncServer 可能未启用
        try {
            this.syncServer = applicationContext.getBean(SyncServer.class);
        } catch (Exception ignored) {
        }
    }

    /**
     * 保存监控作业日志。
     *
     * @param jobLog 监控作业日志对象
     */
    public void saveLog(MonitorJobLog jobLog) {
        monitorJobLogService.save(jobLog);
    }

    /**
     * 更新监控作业日志。
     *
     * @param jobLog 监控作业日志对象
     */
    public void updateLog(MonitorJobLog jobLog) {
        monitorJobLogService.updateById(jobLog);
    }

    /**
     * 根据触发参数和监控作业信息获取执行地址列表。
     * <p>
     * 从 SyncServer 在线客户端中获取
     * </p>
     *
     * @param triggerParam 触发参数
     * @param jobInfo      监控作业信息
     * @return 返回执行地址列表，如果没有可用的执行地址则返回null。
     */
    public Set<Discovery> getAddress(TriggerParam triggerParam, MonitorJob jobInfo) {
        if (syncServer == null) {
            return Collections.emptySet();
        }

        ReturnResult<MonitorApplication> monitorApplication = monitorAppService.getByCacheId(jobInfo.getMonitorId());
        if (monitorApplication.isFailure()) {
            return Collections.emptySet();
        }
        MonitorApplication application = monitorApplication.getData();
        String targetAppName = application.getMonitorApplicationName();
        String targetProfile = jobInfo.getJobApplicationActive();

        if (targetAppName == null) {
            return Collections.emptySet();
        }

        // 从 SyncServer 获取在线客户端
        Set<Discovery> onlineClients = new HashSet<>();
        for (Map.Entry<String, ClientInfo> entry : syncServer.getAllClients().entrySet()) {
            ClientInfo clientInfo = entry.getValue();
            
            // 匹配应用名
            if (!targetAppName.equals(clientInfo.getClientApplicationName())) {
                continue;
            }
            
            // 匹配环境 (如果配置了)
            if (targetProfile != null && !targetProfile.isEmpty()) {
                String clientProfile = clientInfo.getClientApplicationActive();
                if (clientProfile != null && !targetProfile.equals(clientProfile)) {
                    continue;
                }
            }

            // 构建 Discovery 对象（使用 Builder 模式）
            Map<String, String> discoveryMetadata = new HashMap<>();
            discoveryMetadata.put("applicationName", clientInfo.getClientApplicationName());
            discoveryMetadata.put("applicationActive", clientInfo.getClientApplicationActive());
            if (clientInfo.getClientMetadata() != null) {
                clientInfo.getClientMetadata().forEach((k, v) -> discoveryMetadata.put(k, String.valueOf(v)));
            }
            Discovery discovery = Discovery.builder()
                    .host(clientInfo.getClientIpAddress())
                    .port(clientInfo.getClientPort())
                    .serverId(entry.getKey())
                    .metadata(discoveryMetadata)
                    .build();
            onlineClients.add(discovery);
        }

        if (CollectionUtils.isEmpty(onlineClients)) {
            return null;
        }

        // 路由选择合适的执行地址
        ExecutorRouter executorRouter = ServiceProvider.of(ExecutorRouter.class).getNewExtension(jobInfo.getJobExecuteRouter());
        if (null == executorRouter) {
            return null;
        }
        return executorRouter.route(triggerParam, onlineClients);
    }

    /**
     * 执行监控任务。
     *
     * @param jobLog       任务日志
     * @param address      执行地址列表
     * @param triggerParam 触发参数
     * @return 返回执行结果
     */
    public ReturnResult<String> run(MonitorJobLog jobLog, Set<Discovery> address, TriggerParam triggerParam) {
        // 通过 SyncServer 下发任务
        monitorSender.send(jobLog, address, triggerParam);
        return ReturnResult.success();
    }

    /**
     * 锁
     *
     * @param name 名称
     */
    public JobLock lock(String name) {
        return new RedisLock(redissonClient.getLock(name));
    }

    /**
     * 查询待调度的任务（带缓存）
     * <p>
     * 缓存策略：
     * 1. 使用本地缓存，过期时间3秒
     * 2. 缓存键由时间戳和数量组成
     * 3. 任务增删改时清除缓存
     * </p>
     *
     * @param l            触发时间阈值
     * @param preReadCount 预读取数量
     * @return 待调度任务列表
     */
    public List<MonitorJob> scheduleJobQuery(long l, int preReadCount) {
        // 构建缓存键：使用时间窗口（按秒取整）+ 数量
        long timeWindow = l / 1000;
        String cacheKey = timeWindow + "_" + preReadCount;
        long currentVersion = cacheVersion.get();
        
        // 尝试从缓存获取
        ScheduleJobCacheEntry cacheEntry = scheduleJobCache.get(cacheKey);
        if (cacheEntry != null && !cacheEntry.isExpired() && cacheEntry.getVersion() == currentVersion) {
            return cacheEntry.getData();
        }
        
        // 缓存未命中或已过期，从数据库查询
        List<MonitorJob> result = monitorJobService.page(
                new Page<MonitorJob>(1, preReadCount), 
                Wrappers.<MonitorJob>lambdaQuery()
                        .eq(MonitorJob::getJobTriggerStatus, 1)
                        .le(MonitorJob::getJobTriggerNextTime, l)
                        .orderByDesc(MonitorJob::getJobId)
        ).getRecords();
        
        // 更新缓存
        scheduleJobCache.put(cacheKey, new ScheduleJobCacheEntry(result, currentVersion));
        
        // 清理过期缓存（简单策略：缓存数量超过100时清理）
        if (scheduleJobCache.size() > 100) {
            cleanExpiredCache();
        }
        
        return result;
    }
    
    /**
     * 清除调度任务查询缓存
     * <p>
     * 在任务增删改时调用，使缓存立即失效
     * </p>
     */
    public void clearScheduleJobCache() {
        cacheVersion.incrementAndGet();
        scheduleJobCache.clear();
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        long currentVersion = cacheVersion.get();
        scheduleJobCache.entrySet().removeIf(entry -> 
                entry.getValue().isExpired() || entry.getValue().getVersion() != currentVersion);
    }
    
    /**
     * 调度任务缓存条目
     *
     * @author CH
     * @since 2024/03/08
     */
    private static class ScheduleJobCacheEntry {
        private final List<MonitorJob> data;
        private final long expireTime;
        private final long version;
        
        public ScheduleJobCacheEntry(List<MonitorJob> data, long version) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + CACHE_EXPIRE_MS;
            this.version = version;
        }
        
        public List<MonitorJob> getData() {
            return data;
        }
        
        public long getVersion() {
            return version;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    public void scheduleUpdate(MonitorJob jobInfo) {
        monitorJobService.updateById(jobInfo);
    }
}
