package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.entity.MonitorSysGenScriptExecution;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 脚本执行任务管理器
 * 用于管理内存中的脚本执行任务
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@Component
public class ScriptExecutionTaskManager {

    /**
     * 任务ID生成器
     */
    private final AtomicLong taskIdGenerator = new AtomicLong(1);

    /**
     * 内存中的任务存储
     */
    private final Map<String, ExecutionTask> runningTasks = new ConcurrentHashMap<>();

    /**
     * 执行任务信息
     */
    @Data
    public static class ExecutionTask {
        /**
         * 任务ID
         */
        private String taskId;

        /**
         * 执行记录ID
         */
        private Long executionId;

        /**
         * 脚本ID
         */
        private Integer scriptId;

        /**
         * 任务状态
         */
        private TaskStatus status;

        /**
         * 进程对象
         */
        private Process process;

        /**
         * 异步任务Future
         */
        private Future<?> future;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 执行用户
         */
        private String executeUser;

        /**
         * 任务描述
         */
        private String description;

        /**
         * 扩展信息
         */
        private Map<String, Object> metadata;
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        CREATED("CREATED", "已创建"),
        RUNNING("RUNNING", "运行中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "执行失败"),
        CANCELLED("CANCELLED", "已取消"),
        TIMEOUT("TIMEOUT", "执行超时");

        private final String code;
        private final String desc;

        TaskStatus(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 生成任务ID
     */
    public String generateTaskId() {
        return "task_" + System.currentTimeMillis() + "_" + taskIdGenerator.getAndIncrement();
    }

    /**
     * 创建执行任务
     */
    public ExecutionTask createTask(MonitorSysGenScriptExecution execution, String executeUser, String description) {
        String taskId = generateTaskId();
        
        ExecutionTask task = new ExecutionTask();
        task.setTaskId(taskId);
        task.setExecutionId(execution.getMonitorSysGenScriptExecutionId());
        task.setScriptId(execution.getMonitorSysGenScriptId());
        task.setStatus(TaskStatus.CREATED);
        task.setCreateTime(LocalDateTime.now());
        task.setExecuteUser(executeUser);
        task.setDescription(description);
        task.setMetadata(new ConcurrentHashMap<>());

        // 存储到内存
        runningTasks.put(taskId, task);
        
        // 更新执行记录的任务ID
        execution.setMonitorSysGenScriptExecutionTaskId(taskId);

        log.info("创建执行任务: taskId={}, executionId={}, scriptId={}", 
            taskId, execution.getMonitorSysGenScriptExecutionId(), execution.getMonitorSysGenScriptId());

        return task;
    }

    /**
     * 启动任务
     */
    public void startTask(String taskId, Process process, Future<?> future) {
        ExecutionTask task = runningTasks.get(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.RUNNING);
            task.setStartTime(LocalDateTime.now());
            task.setProcess(process);
            task.setFuture(future);
            
            log.info("启动执行任务: taskId={}, processId={}", taskId, process != null ? process.pid() : "null");
        } else {
            log.warn("任务不存在，无法启动: taskId={}", taskId);
        }
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, TaskStatus status) {
        ExecutionTask task = runningTasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setEndTime(LocalDateTime.now());
            
            log.info("完成执行任务: taskId={}, status={}, duration={}ms", 
                taskId, status.getDesc(), 
                task.getStartTime() != null ? 
                    java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis() : 0);
        } else {
            log.warn("任务不存在，无法完成: taskId={}", taskId);
        }
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        ExecutionTask task = runningTasks.get(taskId);
        if (task == null) {
            log.warn("任务不存在，无法取消: taskId={}", taskId);
            return false;
        }

        try {
            // 取消异步任务
            if (task.getFuture() != null && !task.getFuture().isDone()) {
                task.getFuture().cancel(true);
            }

            // 终止进程
            if (task.getProcess() != null && task.getProcess().isAlive()) {
                task.getProcess().destroyForcibly();
            }

            // 更新任务状态
            task.setStatus(TaskStatus.CANCELLED);
            task.setEndTime(LocalDateTime.now());

            log.info("取消执行任务: taskId={}", taskId);
            return true;

        } catch (Exception e) {
            log.error("取消任务失败: taskId={}", taskId, e);
            return false;
        }
    }

    /**
     * 销毁任务（从内存中移除）
     */
    public void destroyTask(String taskId) {
        ExecutionTask task = runningTasks.remove(taskId);
        if (task != null) {
            log.info("销毁执行任务: taskId={}, status={}", taskId, task.getStatus().getDesc());
        }
    }

    /**
     * 获取任务
     */
    public ExecutionTask getTask(String taskId) {
        return runningTasks.get(taskId);
    }

    /**
     * 获取任务通过执行ID
     */
    public ExecutionTask getTaskByExecutionId(Long executionId) {
        return runningTasks.values().stream()
            .filter(task -> task.getExecutionId().equals(executionId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取所有运行中的任务
     */
    public Map<String, ExecutionTask> getRunningTasks() {
        return new ConcurrentHashMap<>(runningTasks);
    }

    /**
     * 获取任务数量
     */
    public int getTaskCount() {
        return runningTasks.size();
    }

    /**
     * 清理已完成的任务
     */
    public void cleanupCompletedTasks() {
        runningTasks.entrySet().removeIf(entry -> {
            ExecutionTask task = entry.getValue();
            boolean shouldRemove = task.getStatus() == TaskStatus.COMPLETED 
                || task.getStatus() == TaskStatus.FAILED 
                || task.getStatus() == TaskStatus.CANCELLED 
                || task.getStatus() == TaskStatus.TIMEOUT;
            
            if (shouldRemove) {
                log.debug("清理已完成任务: taskId={}, status={}", entry.getKey(), task.getStatus().getDesc());
            }
            
            return shouldRemove;
        });
    }

    /**
     * 更新任务元数据
     */
    public void updateTaskMetadata(String taskId, String key, Object value) {
        ExecutionTask task = runningTasks.get(taskId);
        if (task != null && task.getMetadata() != null) {
            task.getMetadata().put(key, value);
        }
    }
}
