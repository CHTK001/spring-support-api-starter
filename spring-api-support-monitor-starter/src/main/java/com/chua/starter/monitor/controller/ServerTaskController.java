package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.task.manager.ServerTaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 服务器任务管理控制器
 * 提供任务管理相关的API接口
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("v1/server/task")
@Tag(name = "服务器任务管理")
@RequiredArgsConstructor
public class ServerTaskController {

    private final ServerTaskManager serverTaskManager;

    /**
     * 获取所有任务概览
     *
     * @return 任务概览信息
     */
    @GetMapping("overview")
    @Operation(summary = "获取所有任务概览")
    public ReturnResult<Map<String, Object>> getTasksOverview() {
        try {
            Map<String, Object> overview = serverTaskManager.getAllTasksOverview();
            return ReturnResult.ok(overview);
        } catch (Exception e) {
            return ReturnResult.error("获取任务概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定服务器的任务状态
     *
     * @param serverId 服务器ID
     * @return 任务状态信息
     */
    @GetMapping("status/{serverId}")
    @Operation(summary = "获取指定服务器的任务状态")
    public ReturnResult<Map<String, Object>> getServerTaskStatus(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            Map<String, Object> status = serverTaskManager.getServerTaskStatus(serverId);
            return ReturnResult.ok(status);
        } catch (Exception e) {
            return ReturnResult.error("获取服务器任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 手动更新服务器任务配置
     *
     * @param serverId 服务器ID
     * @return 更新结果
     */
    @PostMapping("update/{serverId}")
    @Operation(summary = "手动更新服务器任务配置")
    public ReturnResult<String> updateServerTasks(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            serverTaskManager.updateServerTasks(serverId);
            return ReturnResult.ok("任务配置更新成功");
        } catch (Exception e) {
            return ReturnResult.error("更新任务配置失败: " + e.getMessage());
        }
    }

    /**
     * 智能更新服务器任务配置
     * 只有当配置真正发生变化时才重新创建任务
     *
     * @param serverId 服务器ID
     * @return 更新结果
     */
    @PostMapping("smart-update/{serverId}")
    @Operation(summary = "智能更新服务器任务配置")
    public ReturnResult<String> smartUpdateServerTasks(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            serverTaskManager.smartUpdateServerTasks(serverId);
            return ReturnResult.ok("智能任务配置更新完成");
        } catch (Exception e) {
            return ReturnResult.error("智能更新任务配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建服务器任务
     *
     * @param serverId 服务器ID
     * @return 创建结果
     */
    @PostMapping("create/{serverId}")
    @Operation(summary = "创建服务器任务")
    public ReturnResult<String> createServerTasks(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            serverTaskManager.createServerTasks(serverId);
            return ReturnResult.ok("服务器任务创建成功");
        } catch (Exception e) {
            return ReturnResult.error("创建服务器任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除服务器任务
     *
     * @param serverId 服务器ID
     * @return 删除结果
     */
    @DeleteMapping("remove/{serverId}")
    @Operation(summary = "删除服务器任务")
    public ReturnResult<String> removeServerTasks(
            @Parameter(name = "服务器ID") @PathVariable Integer serverId) {
        try {
            serverTaskManager.removeServerTasks(serverId);
            return ReturnResult.ok("服务器任务删除成功");
        } catch (Exception e) {
            return ReturnResult.error("删除服务器任务失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新所有服务器任务
     *
     * @return 更新结果
     */
    @PostMapping("update-all")
    @Operation(summary = "批量更新所有服务器任务")
    public ReturnResult<String> updateAllServerTasks() {
        try {
            serverTaskManager.initializeServerTasks();
            return ReturnResult.ok("所有服务器任务更新成功");
        } catch (Exception e) {
            return ReturnResult.error("批量更新任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    @GetMapping("statistics")
    @Operation(summary = "获取任务统计信息")
    public ReturnResult<Map<String, Object>> getTaskStatistics() {
        try {
            Map<String, Object> stats = serverTaskManager.getTaskStatistics();
            return ReturnResult.ok(stats);
        } catch (Exception e) {
            return ReturnResult.error("获取任务统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务管理器健康状态
     *
     * @return 健康状态
     */
    @GetMapping("health")
    @Operation(summary = "检查任务管理器健康状态")
    public ReturnResult<Map<String, Object>> checkTaskManagerHealth() {
        try {
            Map<String, Object> health = new java.util.HashMap<>();
            
            // 获取基本统计信息
            Map<String, Object> stats = serverTaskManager.getTaskStatistics();
            health.put("statistics", stats);
            
            // 获取概览信息
            Map<String, Object> overview = serverTaskManager.getAllTasksOverview();
            health.put("overview", overview);
            
            // 判断健康状态
            int totalServers = (Integer) overview.get("totalServers");
            int activeServers = (Integer) overview.get("activeServers");
            
            String status;
            if (totalServers == 0) {
                status = "no_servers";
            } else if (activeServers == totalServers) {
                status = "healthy";
            } else if (activeServers > totalServers / 2) {
                status = "warning";
            } else {
                status = "critical";
            }
            
            health.put("status", status);
            health.put("message", getHealthMessage(status, totalServers, activeServers));
            
            return ReturnResult.ok(health);
        } catch (Exception e) {
            return ReturnResult.error("检查任务管理器健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康状态描述信息
     */
    private String getHealthMessage(String status, int totalServers, int activeServers) {
        switch (status) {
            case "no_servers":
                return "当前没有配置任何服务器";
            case "healthy":
                return String.format("所有 %d 个服务器的任务都在正常运行", totalServers);
            case "warning":
                return String.format("%d 个服务器中有 %d 个任务正常运行，部分服务器可能存在问题", 
                        totalServers, activeServers);
            case "critical":
                return String.format("%d 个服务器中只有 %d 个任务正常运行，需要检查服务器配置", 
                        totalServers, activeServers);
            default:
                return "未知状态";
        }
    }

}
