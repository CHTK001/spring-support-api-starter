package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.service.ServerTaskManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 服务器任务管理控制器
 * 提供任务管理相关的API接口
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server-task")
@RequiredArgsConstructor
@Api(tags = "服务器任务管理")
public class ServerTaskManagementController {

    private final ServerTaskManagementService serverTaskManagementService;

    /**
     * 为服务器创建任务
     */
    @PostMapping("/create/{serverId}")
    @ApiOperation("为服务器创建任务")
    public ReturnResult<Boolean> createServerTasks(
            @ApiParam(value = "服务器ID", required = true) @PathVariable Integer serverId) {
        return serverTaskManagementService.createServerTasks(serverId);
    }

    /**
     * 更新服务器任务配置
     */
    @PutMapping("/update/{serverId}")
    @ApiOperation("更新服务器任务配置")
    public ReturnResult<Boolean> updateServerTasks(
            @ApiParam(value = "服务器ID", required = true) @PathVariable Integer serverId) {
        return serverTaskManagementService.updateServerTasks(serverId);
    }

    /**
     * 移除服务器任务
     */
    @DeleteMapping("/remove/{serverId}")
    @ApiOperation("移除服务器任务")
    public ReturnResult<Boolean> removeServerTasks(
            @ApiParam(value = "服务器ID", required = true) @PathVariable Integer serverId) {
        return serverTaskManagementService.removeServerTasks(serverId);
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取任务统计信息")
    public ReturnResult<Map<String, Object>> getTaskStatistics() {
        return serverTaskManagementService.getTaskStatistics();
    }

    /**
     * 重新初始化所有服务器任务
     */
    @PostMapping("/reinitialize")
    @ApiOperation("重新初始化所有服务器任务")
    public ReturnResult<Boolean> reinitializeAllTasks() {
        return serverTaskManagementService.reinitializeAllTasks();
    }

    /**
     * 检查任务健康状态
     */
    @GetMapping("/health")
    @ApiOperation("检查任务健康状态")
    public ReturnResult<Map<String, Object>> checkTaskHealth() {
        return serverTaskManagementService.checkTaskHealth();
    }
}
