package com.chua.starter.monitor.sender;

import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnCode;
import com.chua.report.client.starter.job.TriggerParam;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.sync.support.server.SyncServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static com.chua.common.support.core.utils.MathUtils.ZERO;

/**
 * 监控发送
 * <p>
 * 通过 SyncServer 下发任务
 * </p>
 *
 * @author CH
 * @since 2025/8/13 14:03
 */
@Slf4j
@SpringBootConfiguration
public class MonitorSender {

    private static final String TOPIC_JOB_DISPATCH = "monitor/job/dispatch";

    @Autowired(required = false)
    private SyncServer syncServer;

    /**
     * 发送任务
     *
     * @param jobLog    任务日志
     * @param addresses 目标地址
     * @param param     触发参数
     */
    public void send(MonitorJobLog jobLog, Set<Discovery> addresses, TriggerParam param) {
        if (syncServer == null) {
            jobLog.setJobLogTriggerMsg("SyncServer 未启用");
            jobLog.setJobLogTriggerCode("FAILURE");
            jobLog.setJobLogExecuteCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            jobLog.setJobLogCost(ZERO);
            return;
        }

        if (addresses == null || addresses.isEmpty()) {
            jobLog.setJobLogTriggerMsg("没有可用的执行器地址");
            jobLog.setJobLogTriggerCode("FAILURE");
            jobLog.setJobLogExecuteCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            jobLog.setJobLogCost(ZERO);
            return;
        }

        long startTime = System.currentTimeMillis();
        int sentCount = 0;
        try {
            Map<String, Object> data = buildJobData(param);

            // 遍历目标地址，向对应的客户端下发任务
            for (Discovery address : addresses) {
                String clientId = findClientIdByAddress(address);
                if (clientId != null) {
                    syncServer.send(clientId, TOPIC_JOB_DISPATCH, data);
                    sentCount++;
                    log.debug("[MonitorSender] 任务已发送到: clientId={}, address={}:{}", 
                            clientId, address.getHost(), address.getPort());
                } else {
                    log.warn("[MonitorSender] 未找到对应的客户端: address={}:{}", 
                            address.getHost(), address.getPort());
                }
            }

            if (sentCount > 0) {
                jobLog.setJobLogTriggerCode(ReturnCode.SUCCESS.getCode());
                jobLog.setJobLogTriggerMsg("任务已下发到 " + sentCount + " 个执行器");
                jobLog.setJobLogExecuteCode("PENDING");
                log.info("[MonitorSender] 任务已下发: jobId={}, sentCount={}", param.getJobId(), sentCount);
            } else {
                jobLog.setJobLogTriggerMsg("没有找到在线的执行器");
                jobLog.setJobLogTriggerCode("FAILURE");
                jobLog.setJobLogExecuteCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            }

        } catch (Exception e) {
            log.error("[MonitorSender] 任务下发失败: jobId={}", param.getJobId(), e);
            jobLog.setJobLogExecuteCode("FAILURE");
            jobLog.setJobLogTriggerCode(ReturnCode.SYSTEM_EXECUTION_ERROR.getCode());
            jobLog.setJobLogTriggerMsg(e.getMessage());
        }
        jobLog.setJobLogCost(new BigDecimal(System.currentTimeMillis() - startTime));
    }

    /**
     * 根据地址查找客户端ID
     */
    private String findClientIdByAddress(Discovery address) {
        String appName = address.getMetadata() != null ? 
                (String) address.getMetadata().get("applicationName") : null;
        return syncServer.findClientId(address.getHost(), address.getPort(), appName);
    }

    /**
     * 构建任务数据
     */
    private Map<String, Object> buildJobData(TriggerParam param) {
        return Map.ofEntries(
                Map.entry("jobId", param.getJobId()),
                Map.entry("profile", param.getProfile() != null ? param.getProfile() : ""),
                Map.entry("executorHandler", param.getExecutorHandler() != null ? param.getExecutorHandler() : ""),
                Map.entry("executorParams", param.getExecutorParams() != null ? param.getExecutorParams() : ""),
                Map.entry("executorBlockStrategy", param.getExecutorBlockStrategy() != null ? param.getExecutorBlockStrategy() : ""),
                Map.entry("executorTimeout", param.getExecutorTimeout()),
                Map.entry("logId", param.getLogId()),
                Map.entry("logDateTime", param.getLogDateTime()),
                Map.entry("glueType", param.getGlueType() != null ? param.getGlueType() : ""),
                Map.entry("glueSource", param.getGlueSource() != null ? param.getGlueSource() : ""),
                Map.entry("glueUpdatetime", param.getGlueUpdatetime()),
                Map.entry("broadcastIndex", param.getBroadcastIndex()),
                Map.entry("broadcastTotal", param.getBroadcastTotal())
        );
    }
}
