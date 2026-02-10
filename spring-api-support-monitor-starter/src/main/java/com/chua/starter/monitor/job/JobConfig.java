package com.chua.starter.monitor.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.CollectionUtils;
import com.chua.report.client.starter.job.TriggerParam;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.entity.MonitorJob;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.job.lock.JobLock;
import com.chua.starter.monitor.job.lock.RedisLock;
import com.chua.starter.monitor.job.route.ExecutorRouter;
import com.chua.starter.monitor.properties.ReportJobProperties;
import com.chua.starter.monitor.sender.MonitorSender;
import com.chua.starter.monitor.service.app.MonitorAppService;
import com.chua.starter.monitor.service.job.MonitorJobLogService;
import com.chua.starter.monitor.service.job.MonitorJobService;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * 作业配置
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/03/08
 */
public class JobConfig {

    private static final JobConfig INSTANCE = new JobConfig();
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

    public List<MonitorJob> scheduleJobQuery(long l, int preReadCount) {
        return monitorJobService.page(new Page<MonitorJob>(1, preReadCount), Wrappers.<MonitorJob>lambdaQuery().eq(MonitorJob::getJobTriggerStatus, 1)
                .le(MonitorJob::getJobTriggerNextTime, l)
                .orderByDesc(MonitorJob::getJobId)
        ).getRecords();
    }

    public void scheduleUpdate(MonitorJob jobInfo) {
        monitorJobService.updateById(jobInfo);
    }
}
