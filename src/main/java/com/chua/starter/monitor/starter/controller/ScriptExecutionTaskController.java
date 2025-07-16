package com.chua.starter.monitor.starter.controller;

import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.result.ReturnResult;
import com.chua.starter.monitor.starter.service.ScriptExecutionTaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 脚本执行任务管理控制器
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/script/task")
@RequiredArgsConstructor
@Tag(name = "脚本执行任务管理", description = "管理内存中的脚本执行任务")
public class ScriptExecutionTaskController {

    private final ScriptExecutionTaskManager taskManager;

    /**
     * 获取所有运行中的任务
     */
    @GetMapping("/running")
    @Operation(summary = "获取运行中的任务", description = "获取所有正在运行的脚本执行任务")
    public ReturnResult<Map<String, ScriptExecutionTaskManager.ExecutionTask>> getRunningTasks() {
        try {
            Map<String, ScriptExecutionTaskManager.ExecutionTask> runningTasks = taskManager.getRunningTasks();
            return Result.ok(runningTasks);
        } catch (Exception e) {
            log.error("获取运行中的任务失败", e);
            return Result.error("获取运行中的任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据任务ID获取任务详细信息")
    public ReturnResult<ScriptExecutionTaskManager.ExecutionTask> getTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            ScriptExecutionTaskManager.ExecutionTask task = taskManager.getTask(taskId);
            if (task == null) {
                return Result.error("任务不存在: " + taskId);
            }
            return Result.ok(task);
        } catch (Exception e) {
            log.error("获取任务详情失败: taskId={}", taskId, e);
            return Result.error("获取任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消指定的脚本执行任务")
    public ReturnResult<Boolean> cancelTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        try {
            boolean success = taskManager.cancelTask(taskId);
            if (success) {
                return Result.ok(true);
            } else {
                return Result.error("取消任务失败，任务可能不存在或已完成");
            }
        } catch (Exception e) {
            log.error("取消任务失败: taskId={}", taskId, e);
            return Result.error("取消任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取任务统计", description = "获取任务的统计信息")
    public ReturnResult<Map<String, Object>> getTaskStatistics() {
        try {
            Map<String, ScriptExecutionTaskManager.ExecutionTask> runningTasks = taskManager.getRunningTasks();
            
            long totalTasks = runningTasks.size();
            long runningCount = runningTasks.values().stream()
                .filter(task -> task.getStatus() == ScriptExecutionTaskManager.TaskStatus.RUNNING)
                .count();
            long completedCount = runningTasks.values().stream()
                .filter(task -> task.getStatus() == ScriptExecutionTaskManager.TaskStatus.COMPLETED)
                .count();
            long failedCount = runningTasks.values().stream()
                .filter(task -> task.getStatus() == ScriptExecutionTaskManager.TaskStatus.FAILED)
                .count();
            long cancelledCount = runningTasks.values().stream()
                .filter(task -> task.getStatus() == ScriptExecutionTaskManager.TaskStatus.CANCELLED)
                .count();

            Map<String, Object> statistics = Map.of(
                "totalTasks", totalTasks,
                "runningCount", runningCount,
                "completedCount", completedCount,
                "failedCount", failedCount,
                "cancelledCount", cancelledCount
            );

            return Result.ok(statistics);
        } catch (Exception e) {
            log.error("获取任务统计失败", e);
            return Result.error("获取任务统计失败: " + e.getMessage());
        }
    }

    /**
     * 清理已完成的任务
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理已完成任务", description = "清理内存中已完成的任务")
    public ReturnResult<String> cleanupCompletedTasks() {
        try {
            int beforeCount = taskManager.getTaskCount();
            taskManager.cleanupCompletedTasks();
            int afterCount = taskManager.getTaskCount();
            int cleanedCount = beforeCount - afterCount;
            
            String message = String.format("清理完成，共清理 %d 个已完成任务", cleanedCount);
            return Result.ok(message);
        } catch (Exception e) {
            log.error("清理已完成任务失败", e);
            return Result.error("清理已完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 根据执行ID获取任务
     */
    @GetMapping("/execution/{executionId}")
    @Operation(summary = "根据执行ID获取任务", description = "根据脚本执行记录ID获取对应的任务")
    public ReturnResult<ScriptExecutionTaskManager.ExecutionTask> getTaskByExecutionId(
            @Parameter(description = "执行记录ID") @PathVariable Long executionId) {
        try {
            ScriptExecutionTaskManager.ExecutionTask task = taskManager.getTaskByExecutionId(executionId);
            if (task == null) {
                return Result.error("未找到对应的任务: executionId=" + executionId);
            }
            return Result.ok(task);
        } catch (Exception e) {
            log.error("根据执行ID获取任务失败: executionId={}", executionId, e);
            return Result.error("获取任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务元数据
     */
    @PostMapping("/{taskId}/metadata")
    @Operation(summary = "更新任务元数据", description = "更新任务的元数据信息")
    public ReturnResult<String> updateTaskMetadata(
            @Parameter(description = "任务ID") @PathVariable String taskId,
            @RequestBody Map<String, Object> metadata) {
        try {
            ScriptExecutionTaskManager.ExecutionTask task = taskManager.getTask(taskId);
            if (task == null) {
                return Result.error("任务不存在: " + taskId);
            }

            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                taskManager.updateTaskMetadata(taskId, entry.getKey(), entry.getValue());
            }

            return Result.ok("元数据更新成功");
        } catch (Exception e) {
            log.error("更新任务元数据失败: taskId={}", taskId, e);
            return Result.error("更新任务元数据失败: " + e.getMessage());
        }
    }
}
