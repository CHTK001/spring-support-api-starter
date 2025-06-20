package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenFileUploadTask;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenFileUploadTaskMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenFileUploadTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传任务管理服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenFileUploadTaskServiceImpl extends ServiceImpl<MonitorSysGenFileUploadTaskMapper, MonitorSysGenFileUploadTask> 
        implements MonitorSysGenFileUploadTaskService {

    private final MonitorSysGenFileUploadTaskMapper taskMapper;

    @Override
    public ReturnResult<IPage<MonitorSysGenFileUploadTask>> getTaskPage(Page<MonitorSysGenFileUploadTask> page,
                                                                        String taskName,
                                                                        String status,
                                                                        String mode,
                                                                        String type,
                                                                        String createUser,
                                                                        LocalDateTime startTime,
                                                                        LocalDateTime endTime) {
        try {
            IPage<MonitorSysGenFileUploadTask> result = taskMapper.selectTaskPage(page, taskName, status, mode, type, createUser, startTime, endTime);
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("分页查询文件上传任务失败", e);
            return ReturnResult.error("查询任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenFileUploadTask> createUploadTask(MonitorSysGenFileUploadTask task) {
        try {
            // 验证任务参数
            ReturnResult<Map<String, Object>> validationResult = validateTask(task);
            if (validationResult != null && validationResult.getData() != null) {
                Map<String, Object> validation = validationResult.getData();
                if (!(Boolean) validation.get("valid")) {
                    return ReturnResult.illegal("任务参数验证失败: " + validation.get("message"));
                }
            }

            // 设置默认值
            LocalDateTime now = LocalDateTime.now();
            if (task.getMonitorSysGenFileUploadCreateTime() == null) {
                task.setMonitorSysGenFileUploadCreateTime(now);
            }
            if (task.getMonitorSysGenFileUploadUpdateTime() == null) {
                task.setMonitorSysGenFileUploadUpdateTime(now);
            }
            if (task.getMonitorSysGenFileUploadStatus() == null) {
                task.setMonitorSysGenFileUploadStatus(MonitorSysGenFileUploadTask.TaskStatus.PENDING.getCode());
            }
            if (task.getMonitorSysGenFileUploadTaskStatus() == null) {
                task.setMonitorSysGenFileUploadTaskStatus(1);
            }
            if (task.getMonitorSysGenFileUploadPriority() == null) {
                task.setMonitorSysGenFileUploadPriority(5);
            }
            if (task.getMonitorSysGenFileUploadRetryCount() == null) {
                task.setMonitorSysGenFileUploadRetryCount(0);
            }

            boolean result = save(task);
            if (result) {
                return ReturnResult.success(task);
            } else {
                return ReturnResult.error("创建任务失败");
            }
        } catch (Exception e) {
            log.error("创建文件上传任务失败", e);
            return ReturnResult.error("创建任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<List<MonitorSysGenFileUploadTask>> createBatchUploadTasks(List<MonitorSysGenFileUploadTask> tasks) {
        try {
            if (tasks == null || tasks.isEmpty()) {
                return ReturnResult.illegal("任务列表不能为空");
            }

            LocalDateTime now = LocalDateTime.now();
            for (MonitorSysGenFileUploadTask task : tasks) {
                if (task.getMonitorSysGenFileUploadCreateTime() == null) {
                    task.setMonitorSysGenFileUploadCreateTime(now);
                }
                if (task.getMonitorSysGenFileUploadUpdateTime() == null) {
                    task.setMonitorSysGenFileUploadUpdateTime(now);
                }
                if (task.getMonitorSysGenFileUploadStatus() == null) {
                    task.setMonitorSysGenFileUploadStatus(MonitorSysGenFileUploadTask.TaskStatus.PENDING.getCode());
                }
                if (task.getMonitorSysGenFileUploadTaskStatus() == null) {
                    task.setMonitorSysGenFileUploadTaskStatus(1);
                }
            }

            boolean result = saveBatch(tasks);
            if (result) {
                return ReturnResult.success(tasks);
            } else {
                return ReturnResult.error("批量创建任务失败");
            }
        } catch (Exception e) {
            log.error("批量创建文件上传任务失败", e);
            return ReturnResult.error("批量创建任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> startUploadTask(Long taskId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = taskMapper.updateTaskStatus(taskId, MonitorSysGenFileUploadTask.TaskStatus.PROCESSING.getCode(), now, null);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("启动上传任务失败: taskId={}", taskId, e);
            return ReturnResult.error("启动任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> pauseUploadTask(Long taskId) {
        try {
            // TODO: 实现暂停逻辑
            log.info("暂停上传任务: taskId={}", taskId);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("暂停上传任务失败: taskId={}", taskId, e);
            return ReturnResult.error("暂停任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> cancelUploadTask(Long taskId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = taskMapper.updateTaskStatus(taskId, MonitorSysGenFileUploadTask.TaskStatus.CANCELLED.getCode(), null, now);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("取消上传任务失败: taskId={}", taskId, e);
            return ReturnResult.error("取消任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> retryUploadTask(Long taskId) {
        try {
            MonitorSysGenFileUploadTask task = getById(taskId);
            if (task == null) {
                return ReturnResult.illegal("任务不存在");
            }

            if (!task.canRetry()) {
                return ReturnResult.illegal("任务不能重试");
            }

            // 增加重试次数
            taskMapper.incrementRetryCount(taskId);
            
            // 重置任务状态
            LocalDateTime now = LocalDateTime.now();
            taskMapper.updateTaskStatus(taskId, MonitorSysGenFileUploadTask.TaskStatus.PENDING.getCode(), null, null);

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("重试上传任务失败: taskId={}", taskId, e);
            return ReturnResult.error("重试任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Map<String, Object>> batchOperateTasks(List<Long> taskIds, String operation) {
        try {
            Map<String, Object> result = new HashMap<>();
            int successCount = 0;
            int failCount = 0;

            for (Long taskId : taskIds) {
                try {
                    boolean success = false;
                    switch (operation.toLowerCase()) {
                        case "start":
                            success = startUploadTask(taskId).getData();
                            break;
                        case "pause":
                            success = pauseUploadTask(taskId).getData();
                            break;
                        case "cancel":
                            success = cancelUploadTask(taskId).getData();
                            break;
                        case "retry":
                            success = retryUploadTask(taskId).getData();
                            break;
                        default:
                            log.warn("不支持的操作类型: {}", operation);
                            break;
                    }
                    
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("批量操作任务失败: taskId={}, operation={}", taskId, operation, e);
                    failCount++;
                }
            }

            result.put("totalCount", taskIds.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("operation", operation);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("批量操作任务失败: operation={}", operation, e);
            return ReturnResult.error("批量操作失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByStatus(String status) {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.selectTasksByStatus(status);
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("根据状态查询任务失败: status={}", status, e);
            return ReturnResult.error("查询任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getPendingScheduledTasks() {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.selectPendingScheduledTasks(LocalDateTime.now());
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("查询待处理的定时任务失败", e);
            return ReturnResult.error("查询定时任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getRetryableTasks() {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.selectRetryableTasks();
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("查询可重试的失败任务失败", e);
            return ReturnResult.error("查询可重试任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTimeoutTasks(Integer timeoutMinutes) {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.selectTimeoutTasks(timeoutMinutes);
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("查询超时任务失败: timeoutMinutes={}", timeoutMinutes, e);
            return ReturnResult.error("查询超时任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateTaskStatus(Long taskId, String status, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            int updated = taskMapper.updateTaskStatus(taskId, status, startTime, endTime);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("更新任务状态失败: taskId={}, status={}", taskId, status, e);
            return ReturnResult.error("更新任务状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> incrementRetryCount(Long taskId) {
        try {
            int updated = taskMapper.incrementRetryCount(taskId);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("增加重试次数失败: taskId={}", taskId, e);
            return ReturnResult.error("增加重试次数失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTaskStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<Map<String, Object>> statistics = taskMapper.getTaskStatistics(startTime, endTime);
            
            Map<String, Object> result = new HashMap<>();
            int totalTasks = 0;
            long totalSize = 0;
            Map<String, Integer> statusCounts = new HashMap<>();

            for (Map<String, Object> stat : statistics) {
                String status = (String) stat.get("status");
                Integer count = ((Number) stat.get("count")).intValue();
                Long size = stat.get("totalSize") != null ? ((Number) stat.get("totalSize")).longValue() : 0L;
                
                statusCounts.put(status, count);
                totalTasks += count;
                totalSize += size;
            }

            result.put("totalTasks", totalTasks);
            result.put("totalSize", totalSize);
            result.put("statusCounts", statusCounts);
            result.put("startTime", startTime);
            result.put("endTime", endTime);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getUserTaskStatistics(String createUser, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = taskMapper.getUserTaskStatistics(createUser, startTime, endTime);
            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("根据用户查询任务统计失败: createUser={}", createUser, e);
            return ReturnResult.error("获取用户统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getPopularFileTypes(Integer limit) {
        try {
            List<Map<String, Object>> fileTypes = taskMapper.getPopularFileTypes(limit);
            return ReturnResult.success(fileTypes);
        } catch (Exception e) {
            log.error("查询热门文件类型失败: limit={}", limit, e);
            return ReturnResult.error("查询热门文件类型失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getLargeFileTasks(Integer minSizeMB, Integer limit) {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.getLargeFileTasks(minSizeMB, limit);
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("查询大文件任务失败: minSizeMB={}, limit={}", minSizeMB, limit, e);
            return ReturnResult.error("查询大文件任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getRecentTasks(Integer limit) {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.getRecentTasks(limit);
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("查询最近任务失败: limit={}", limit, e);
            return ReturnResult.error("查询最近任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenFileUploadTask>> getTasksByFileMd5(String fileMd5) {
        try {
            List<MonitorSysGenFileUploadTask> tasks = taskMapper.selectTasksByFileMd5(fileMd5);
            return ReturnResult.success(tasks);
        } catch (Exception e) {
            log.error("根据文件MD5查询任务失败: fileMd5={}", fileMd5, e);
            return ReturnResult.error("查询任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Long> getQueueTaskCount() {
        try {
            Long count = taskMapper.getQueueTaskCount();
            return ReturnResult.success(count);
        } catch (Exception e) {
            log.error("查询队列任务数量失败", e);
            return ReturnResult.error("查询队列任务数量失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTodayTaskStatistics() {
        try {
            Map<String, Object> statistics = taskMapper.getTodayTaskStatistics();
            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("查询今日任务统计失败", e);
            return ReturnResult.error("查询今日统计失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Integer> deleteExpiredTasks(LocalDateTime expiredTime) {
        try {
            int deletedCount = taskMapper.deleteExpiredTasks(expiredTime);
            log.info("删除过期任务 {} 个", deletedCount);
            return ReturnResult.success(deletedCount);
        } catch (Exception e) {
            log.error("删除过期任务失败", e);
            return ReturnResult.error("删除过期任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> validateTask(MonitorSysGenFileUploadTask task) {
        try {
            Map<String, Object> result = new HashMap<>();
            StringBuilder message = new StringBuilder();
            boolean valid = true;

            if (task.getMonitorSysGenFileUploadTaskName() == null || task.getMonitorSysGenFileUploadTaskName().trim().isEmpty()) {
                valid = false;
                message.append("任务名称不能为空; ");
            }

            if (task.getMonitorSysGenFileUploadFileName() == null || task.getMonitorSysGenFileUploadFileName().trim().isEmpty()) {
                valid = false;
                message.append("文件名称不能为空; ");
            }

            if (task.getMonitorSysGenFileUploadFilePath() == null || task.getMonitorSysGenFileUploadFilePath().trim().isEmpty()) {
                valid = false;
                message.append("文件路径不能为空; ");
            }

            result.put("valid", valid);
            result.put("message", message.toString());
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("验证任务参数失败", e);
            return ReturnResult.error("验证任务参数失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenFileUploadTask> duplicateTask(Long taskId) {
        try {
            MonitorSysGenFileUploadTask originalTask = getById(taskId);
            if (originalTask == null) {
                return ReturnResult.illegal("原任务不存在");
            }

            MonitorSysGenFileUploadTask newTask = new MonitorSysGenFileUploadTask();
            // 复制任务属性
            newTask.setMonitorSysGenFileUploadTaskName(originalTask.getMonitorSysGenFileUploadTaskName() + "_副本");
            newTask.setMonitorSysGenFileUploadFileName(originalTask.getMonitorSysGenFileUploadFileName());
            newTask.setMonitorSysGenFileUploadFilePath(originalTask.getMonitorSysGenFileUploadFilePath());
            newTask.setMonitorSysGenFileUploadFileSize(originalTask.getMonitorSysGenFileUploadFileSize());
            newTask.setMonitorSysGenFileUploadFileMd5(originalTask.getMonitorSysGenFileUploadFileMd5());
            newTask.setMonitorSysGenFileUploadTargetServerIds(originalTask.getMonitorSysGenFileUploadTargetServerIds());
            newTask.setMonitorSysGenFileUploadTargetPaths(originalTask.getMonitorSysGenFileUploadTargetPaths());
            newTask.setMonitorSysGenFileUploadMode(originalTask.getMonitorSysGenFileUploadMode());
            newTask.setMonitorSysGenFileUploadType(originalTask.getMonitorSysGenFileUploadType());
            newTask.setMonitorSysGenFileUploadStatus(MonitorSysGenFileUploadTask.TaskStatus.PENDING.getCode());
            newTask.setMonitorSysGenFileUploadPriority(originalTask.getMonitorSysGenFileUploadPriority());
            newTask.setMonitorSysGenFileUploadMaxRetry(originalTask.getMonitorSysGenFileUploadMaxRetry());
            newTask.setMonitorSysGenFileUploadTimeoutSeconds(originalTask.getMonitorSysGenFileUploadTimeoutSeconds());
            newTask.setMonitorSysGenFileUploadOverwriteEnabled(originalTask.getMonitorSysGenFileUploadOverwriteEnabled());
            newTask.setMonitorSysGenFileUploadBackupEnabled(originalTask.getMonitorSysGenFileUploadBackupEnabled());
            newTask.setMonitorSysGenFileUploadCompressEnabled(originalTask.getMonitorSysGenFileUploadCompressEnabled());
            newTask.setMonitorSysGenFileUploadVerifyEnabled(originalTask.getMonitorSysGenFileUploadVerifyEnabled());
            newTask.setMonitorSysGenFileUploadDescription(originalTask.getMonitorSysGenFileUploadDescription());
            newTask.setMonitorSysGenFileUploadTags(originalTask.getMonitorSysGenFileUploadTags());
            newTask.setMonitorSysGenFileUploadCreateUser(originalTask.getMonitorSysGenFileUploadCreateUser());

            LocalDateTime now = LocalDateTime.now();
            newTask.setMonitorSysGenFileUploadCreateTime(now);
            newTask.setMonitorSysGenFileUploadUpdateTime(now);
            newTask.setMonitorSysGenFileUploadRetryCount(0);
            newTask.setMonitorSysGenFileUploadTaskStatus(1);

            return createUploadTask(newTask);
        } catch (Exception e) {
            log.error("复制任务失败: taskId={}", taskId, e);
            return ReturnResult.error("复制任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportTasks(List<Long> taskIds, String format) {
        try {
            // TODO: 实现任务导出功能
            log.info("导出任务: taskIds={}, format={}", taskIds, format);
            return ReturnResult.success("导出功能正在开发中");
        } catch (Exception e) {
            log.error("导出任务失败", e);
            return ReturnResult.error("导出任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<List<MonitorSysGenFileUploadTask>> importTasks(String taskData, String format) {
        try {
            // TODO: 实现任务导入功能
            log.info("导入任务: format={}", format);
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("导入任务失败", e);
            return ReturnResult.error("导入任务失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTaskExecutionReport(Long taskId) {
        try {
            MonitorSysGenFileUploadTask task = getById(taskId);
            if (task == null) {
                return ReturnResult.illegal("任务不存在");
            }

            Map<String, Object> report = new HashMap<>();
            report.put("taskId", taskId);
            report.put("taskName", task.getMonitorSysGenFileUploadTaskName());
            report.put("status", task.getMonitorSysGenFileUploadStatus());
            report.put("createTime", task.getMonitorSysGenFileUploadCreateTime());
            report.put("startTime", task.getMonitorSysGenFileUploadStartTime());
            report.put("endTime", task.getMonitorSysGenFileUploadEndTime());
            report.put("retryCount", task.getMonitorSysGenFileUploadRetryCount());

            // TODO: 添加更多报告内容

            return ReturnResult.success(report);
        } catch (Exception e) {
            log.error("获取任务执行报告失败: taskId={}", taskId, e);
            return ReturnResult.error("获取执行报告失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Map<String, Object>> cleanupTaskData(Integer cleanupDays) {
        try {
            LocalDateTime expiredTime = LocalDateTime.now().minusDays(cleanupDays);
            int deletedCount = deleteExpiredTasks(expiredTime).getData();

            Map<String, Object> result = new HashMap<>();
            result.put("cleanupDays", cleanupDays);
            result.put("expiredTime", expiredTime);
            result.put("deletedCount", deletedCount);

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("清理任务数据失败: cleanupDays={}", cleanupDays, e);
            return ReturnResult.error("清理任务数据失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getTaskQueueStatus() {
        try {
            Map<String, Object> status = new HashMap<>();

            Long queueCount = getQueueTaskCount().getData();
            List<MonitorSysGenFileUploadTask> pendingTasks = getTasksByStatus(MonitorSysGenFileUploadTask.TaskStatus.PENDING.getCode()).getData();
            List<MonitorSysGenFileUploadTask> processingTasks = getTasksByStatus(MonitorSysGenFileUploadTask.TaskStatus.PROCESSING.getCode()).getData();

            status.put("queueCount", queueCount);
            status.put("pendingCount", pendingTasks != null ? pendingTasks.size() : 0);
            status.put("processingCount", processingTasks != null ? processingTasks.size() : 0);
            status.put("checkTime", LocalDateTime.now());

            return ReturnResult.success(status);
        } catch (Exception e) {
            log.error("获取任务队列状态失败", e);
            return ReturnResult.error("获取队列状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Map<String, Object>> optimizeTaskQueue() {
        try {
            Map<String, Object> result = new HashMap<>();

            // 清理超时任务
            List<MonitorSysGenFileUploadTask> timeoutTasks = getTimeoutTasks(30).getData(); // 30分钟超时
            int timeoutCount = 0;
            if (timeoutTasks != null) {
                for (MonitorSysGenFileUploadTask task : timeoutTasks) {
                    updateTaskStatus(task.getMonitorSysGenFileUploadTaskId(),
                                   MonitorSysGenFileUploadTask.TaskStatus.FAILED.getCode(),
                                   null, LocalDateTime.now());
                    timeoutCount++;
                }
            }

            result.put("timeoutTasksProcessed", timeoutCount);
            result.put("optimizeTime", LocalDateTime.now());

            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("优化任务队列失败", e);
            return ReturnResult.error("优化队列失败: " + e.getMessage());
        }
    }
}
