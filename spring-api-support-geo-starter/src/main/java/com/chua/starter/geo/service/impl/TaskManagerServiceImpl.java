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

    /** 任务存储：任务id -> 任务信息 */
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
        TaskInfo info = new TaskInfo();
        info.setId(taskId);
        info.setName(name);
        info.setSource(source);
        info.setSourceName(sourceName);
        info.setZoom(zoom);
        info.setFormat(format);
        info.setSavePath(savePath);
        info.setStatus(TaskStatus.PENDING);
        info.setTotal(total);
        info.setCreatedAt(LocalDateTime.now());
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
            info.setCompleted(completed);
            info.setFailedCount(failed);
            info.setSuccessCount(Math.max(0, completed - failed));
            info.setProgress(progress);
            info.setMessage(message);
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
            info.setStatus(status);
            info.setMessage(status.name());
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
        if (info != null && info.getStatus() != TaskStatus.COMPLETED
                && info.getStatus() != TaskStatus.FAILED
                && info.getStatus() != TaskStatus.CANCELLED) {
            info.setStatus(TaskStatus.CANCELLED);
            info.setMessage("用户取消");
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
