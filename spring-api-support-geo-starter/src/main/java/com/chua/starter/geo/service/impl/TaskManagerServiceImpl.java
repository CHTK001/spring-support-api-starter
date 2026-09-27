package com.chua.starter.geo.service.impl;

import com.chua.starter.geo.model.TaskInfo;
import com.chua.starter.geo.model.TaskStatus;
import com.chua.starter.geo.service.TaskManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 下载任务管理实现，基于内存存储任务状态。
 *
 * @author CH
 * @since 1.0.0
 */
@Service
@Slf4j
public class TaskManagerServiceImpl implements TaskManagerService {

    /**
     * 任务存储：任务id -> 任务信息
    */
    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    /**
     * 创建下载任务。
     *
     * @param name       任务名称
     * @param source     图源 标识
     * @param sourceName 图源显示名称
     * @param zoom       缩放级别
     * @param format     输出格式
     * @param savePath   保存路径
     * @param total      总瓦片数
     * @return 任务 标识
     */
    @Override
    public String createTask(String name, String source, String sourceName, int zoom,
                             String format, String savePath, int total) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        TaskInfo info = new TaskInfo(
                taskId,
                name,
                source,
                sourceName,
                zoom,
                format,
                savePath,
                TaskStatus.PENDING,
                0.0,
                0,
                total,
                0,
                0,
                0L,
                null,
                null,
                LocalDateTime.now()
        );
        tasks.put(taskId, info);
        log.info("[{}] 任务已创建, source={}, zoom={}, total={}", taskId, source, zoom, total);
        return taskId;
    }

    /**
     * 获取任务信息。
     *
     * @param taskId 任务 标识
     * @return 任务信息，不存在返回 空
     */
    @Override
    public TaskInfo getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 列出所有任务。
     *
     * @return 任务列表
     */
    @Override
    public List<TaskInfo> listTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 更新任务进度。
     *
     * @param taskId   任务 标识
     * @param completed 已完成数
     * @param failed   失败数
     * @param progress 进度百分比
     * @param message  状态消息
     */
    @Override
    public void updateProgress(String taskId, int completed, int failed, double progress, String message) {
        TaskInfo info = tasks.get(taskId);
        if (info != null) {
            tasks.put(taskId, new TaskInfo(
                    info.id(), info.name(), info.source(), info.sourceName(), info.zoom(), info.format(), info.savePath(),
                    info.status(), progress, completed, info.total(), failed, Math.max(0, completed - failed),
                    info.fileSize(), message, info.error(), info.createdAt()));
        }
    }

    /**
     * 设置任务状态。
     *
     * @param taskId 任务 标识
     * @param status 目标状态
     */
    @Override
    public void setStatus(String taskId, TaskStatus status) {
        TaskInfo info = tasks.get(taskId);
        if (info != null) {
            tasks.put(taskId, new TaskInfo(
                    info.id(), info.name(), info.source(), info.sourceName(), info.zoom(), info.format(), info.savePath(),
                    status, info.progress(), info.completed(), info.total(), info.failedCount(), info.successCount(),
                    info.fileSize(), status.name(), info.error(), info.createdAt()));
        }
    }

    /**
     * 取消任务。
     *
     * @param taskId 任务 标识
     */
    @Override
    public void cancelTask(String taskId) {
        TaskInfo info = tasks.get(taskId);
        if (info != null && info.status() != TaskStatus.COMPLETED
                && info.status() != TaskStatus.FAILED
                && info.status() != TaskStatus.CANCELLED) {
            tasks.put(taskId, new TaskInfo(
                    info.id(), info.name(), info.source(), info.sourceName(), info.zoom(), info.format(), info.savePath(),
                    TaskStatus.CANCELLED, info.progress(), info.completed(), info.total(), info.failedCount(), info.successCount(),
                    info.fileSize(), "用户取消", info.error(), info.createdAt()));
            log.info("[{}] 任务已取消", taskId);
        }
    }

    /**
     * 删除任务记录。
     *
     * @param taskId 任务 标识
     */
    @Override
    public void removeTask(String taskId) {
        tasks.remove(taskId);
        log.info("[{}] 任务记录已删除", taskId);
    }
}

