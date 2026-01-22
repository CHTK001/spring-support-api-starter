package com.chua.starter.monitor.service.sync;

import com.chua.common.support.text.json.Json;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.constant.MonitorSocketTopics;
import com.chua.starter.monitor.entity.MonitorSyncTask;
import com.chua.starter.monitor.entity.MonitorSyncTaskLog;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 同步任务 WebSocket 推送服务
 * 用于推送同步任务的实时日志、状态变更、执行进度等
 *
 * @author CH
 * @since 2024/12/19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SyncTaskWebSocketService {

    private final SocketSessionTemplate socketSessionTemplate;

    /**
     * WebSocket Topic 常量
     */
    public static final String TOPIC_SYNC_TASK_STATUS = MonitorSocketTopics.Monitor.Sync.STATUS;
    public static final String TOPIC_SYNC_TASK_LOG = MonitorSocketTopics.Monitor.Sync.LOG;
    public static final String TOPIC_SYNC_TASK_PROGRESS = MonitorSocketTopics.Monitor.Sync.PROGRESS;
    public static final String TOPIC_SYNC_TASK_METRICS = MonitorSocketTopics.Monitor.Sync.METRICS;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 推送任务状态变更
     *
     * @param taskId     任务ID
     * @param taskName   任务名称
     * @param status     新状态 (RUNNING/STOPPED/ERROR)
     * @param message    状态消息
     */
    public void pushTaskStatus(Long taskId, String taskName, String status, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("taskName", taskName);
            data.put("status", status);
            data.put("message", message);
            data.put("time", LocalDateTime.now().format(TIME_FORMATTER));

            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType("sync_task_status")
                    .dataId(String.valueOf(taskId))
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SYNC_TASK_STATUS, Json.toJson(wsMessage));
            log.debug("推送任务状态变更: taskId={}, status={}", taskId, status);

        } catch (Exception e) {
            log.error("推送任务状态失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送实时执行日志
     *
     * @param taskId    任务ID
     * @param logId     执行日志ID
     * @param level     日志级别 (DEBUG/INFO/WARN/ERROR)
     * @param nodeKey   相关节点Key (可为null)
     * @param message   日志消息
     */
    public void pushTaskLog(Long taskId, Long logId, String level, String nodeKey, String message) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("logId", logId);
            data.put("level", level);
            data.put("nodeKey", nodeKey);
            data.put("message", message);
            data.put("time", LocalDateTime.now().format(TIME_FORMATTER));

            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType("sync_task_log")
                    .dataId(String.valueOf(taskId))
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SYNC_TASK_LOG, Json.toJson(wsMessage));
            log.trace("推送任务日志: taskId={}, level={}, message={}", taskId, level, message);

        } catch (Exception e) {
            log.error("推送任务日志失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送执行进度
     *
     * @param taskId         任务ID
     * @param logId          执行日志ID
     * @param readCount      读取数量
     * @param writeCount     写入数量
     * @param successCount   成功数量
     * @param failCount      失败数量
     * @param progress       进度百分比 (0-100)
     */
    public void pushTaskProgress(Long taskId, Long logId, long readCount, long writeCount,
                                  long successCount, long failCount, int progress) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("logId", logId);
            data.put("readCount", readCount);
            data.put("writeCount", writeCount);
            data.put("successCount", successCount);
            data.put("failCount", failCount);
            data.put("progress", progress);
            data.put("time", LocalDateTime.now().format(TIME_FORMATTER));

            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType("sync_task_progress")
                    .dataId(String.valueOf(taskId))
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SYNC_TASK_PROGRESS, Json.toJson(wsMessage));
            log.debug("推送任务进度: taskId={}, progress={}%", taskId, progress);

        } catch (Exception e) {
            log.error("推送任务进度失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送执行指标
     *
     * @param taskId          任务ID
     * @param logId           执行日志ID
     * @param throughput      吞吐量 (条/秒)
     * @param avgProcessTime  平均处理时间 (毫秒)
     * @param cpuUsage        CPU使用率
     * @param memoryUsage     内存使用率
     */
    public void pushTaskMetrics(Long taskId, Long logId, double throughput, double avgProcessTime,
                                 double cpuUsage, double memoryUsage) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("logId", logId);
            data.put("throughput", throughput);
            data.put("avgProcessTime", avgProcessTime);
            data.put("cpuUsage", cpuUsage);
            data.put("memoryUsage", memoryUsage);
            data.put("time", LocalDateTime.now().format(TIME_FORMATTER));

            ServerWebSocketMessage wsMessage = ServerWebSocketMessage.builder()
                    .messageType("sync_task_metrics")
                    .dataId(String.valueOf(taskId))
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(TOPIC_SYNC_TASK_METRICS, Json.toJson(wsMessage));
            log.debug("推送任务指标: taskId={}, throughput={}/s", taskId, throughput);

        } catch (Exception e) {
            log.error("推送任务指标失败: taskId={}", taskId, e);
        }
    }

    /**
     * 推送任务执行开始
     *
     * @param task    任务实体
     * @param taskLog 执行日志
     */
    public void pushTaskStarted(MonitorSyncTask task, MonitorSyncTaskLog taskLog) {
        pushTaskStatus(task.getSyncTaskId(), task.getSyncTaskName(), "RUNNING", "任务开始执行");
        pushTaskLog(task.getSyncTaskId(), taskLog.getSyncLogId(), "INFO", null,
                "任务开始执行, 触发类型: " + taskLog.getSyncLogTriggerType());
    }

    /**
     * 推送任务执行完成
     *
     * @param task    任务实体
     * @param taskLog 执行日志
     */
    public void pushTaskCompleted(MonitorSyncTask task, MonitorSyncTaskLog taskLog) {
        String status = "SUCCESS".equals(taskLog.getSyncLogStatus()) ? "STOPPED" : "ERROR";
        String message = "SUCCESS".equals(taskLog.getSyncLogStatus())
                ? String.format("任务执行成功, 读取%d条, 写入%d条, 耗时%dms",
                    taskLog.getSyncLogReadCount(), taskLog.getSyncLogWriteCount(), taskLog.getSyncLogCost())
                : "任务执行失败: " + taskLog.getSyncLogMessage();

        pushTaskStatus(task.getSyncTaskId(), task.getSyncTaskName(), status, message);
        pushTaskLog(task.getSyncTaskId(), taskLog.getSyncLogId(),
                "SUCCESS".equals(taskLog.getSyncLogStatus()) ? "INFO" : "ERROR", null, message);

        // 推送最终进度
        pushTaskProgress(task.getSyncTaskId(), taskLog.getSyncLogId(),
                taskLog.getSyncLogReadCount() != null ? taskLog.getSyncLogReadCount() : 0,
                taskLog.getSyncLogWriteCount() != null ? taskLog.getSyncLogWriteCount() : 0,
                taskLog.getSyncLogSuccessCount() != null ? taskLog.getSyncLogSuccessCount() : 0,
                taskLog.getSyncLogFailCount() != null ? taskLog.getSyncLogFailCount() : 0,
                100);
    }

    /**
     * 推送节点处理日志
     *
     * @param taskId   任务ID
     * @param logId    执行日志ID
     * @param nodeKey  节点Key
     * @param nodeName 节点名称
     * @param count    处理数量
     */
    public void pushNodeProcessed(Long taskId, Long logId, String nodeKey, String nodeName, long count) {
        pushTaskLog(taskId, logId, "DEBUG", nodeKey,
                String.format("节点[%s]处理完成, 数量: %d", nodeName, count));
    }

    /**
     * 推送错误日志
     *
     * @param taskId    任务ID
     * @param logId     执行日志ID
     * @param nodeKey   相关节点Key (可为null)
     * @param error     错误消息
     * @param exception 异常对象 (可为null)
     */
    public void pushError(Long taskId, Long logId, String nodeKey, String error, Exception exception) {
        String message = error;
        if (exception != null) {
            message += ": " + exception.getMessage();
        }
        pushTaskLog(taskId, logId, "ERROR", nodeKey, message);
    }

    /**
     * 获取WebSocket Topic列表
     *
     * @return Topic映射
     */
    public Map<String, String> getTopics() {
        Map<String, String> topics = new HashMap<>();
        topics.put("status", TOPIC_SYNC_TASK_STATUS);
        topics.put("log", TOPIC_SYNC_TASK_LOG);
        topics.put("progress", TOPIC_SYNC_TASK_PROGRESS);
        topics.put("metrics", TOPIC_SYNC_TASK_METRICS);
        return topics;
    }
}
