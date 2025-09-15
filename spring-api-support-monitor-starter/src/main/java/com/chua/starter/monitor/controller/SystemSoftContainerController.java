package com.chua.starter.monitor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.annotations.OnRouterEvent;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.SystemSoftContainer;
import com.chua.starter.monitor.pojo.request.BatchOperateRequest;
import com.chua.starter.monitor.pojo.request.CreateContainerRequest;
import com.chua.starter.monitor.pojo.request.UpdateContainerRequest;
import com.chua.starter.monitor.service.SystemSoftContainerService;
import com.chua.starter.monitor.service.SystemSoftContainerWebSocketService;
import com.chua.starter.mybatis.utils.ReturnPageResultUtils;
import com.github.dockerjava.api.model.Statistics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统软件容器管理控制器
 *
 * @author CH
 * @version 1.0
 * @since 2025/01/15
 */
@Api(tags = "系统软件容器管理")
@Tag(name = "系统软件容器管理", description = "系统软件容器管理相关接口")
@RestController
@RequestMapping("/v1/system/soft/container")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SystemSoftContainerController {

    private final SystemSoftContainerService systemSoftContainerService;

    @Autowired(required = false)
    private SystemSoftContainerWebSocketService webSocketService;

    /**
     * 分页查询容器列表
     *
     * @param current  当前页
     * @param size     每页大小
     * @param serverId 服务器ID
     * @param status   容器状态
     * @param keyword  关键词
     * @return 分页结果
     */
    @ApiOperation("分页查询容器列表")
    @Operation(summary = "分页查询容器列表", description = "根据条件分页查询容器列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", defaultValue = "1", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "容器状态", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "关键词", dataType = "string", paramType = "query")
    })
    @GetMapping("/page")
    @OnRouterEvent("分页查询容器列表")
    public ReturnPageResult<SystemSoftContainer> getContainerPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "容器状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {

        log.info("分页查询容器列表 - 当前页: {}, 每页大小: {}, 服务器ID: {}, 状态: {}, 关键词: {}",
                current, size, serverId, status, keyword);

        IPage<SystemSoftContainer> result = systemSoftContainerService.getContainerPage(current, size, serverId, status, keyword);
        return ReturnPageResultUtils.ok(result);
    }

    /**
     * 根据软件ID查询容器列表
     *
     * @param softId 软件ID
     * @return 容器列表
     */
    @ApiOperation("根据软件ID查询容器列表")
    @Operation(summary = "根据软件ID查询容器列表", description = "根据软件ID查询该软件的所有容器")
    @ApiImplicitParam(name = "softId", value = "软件ID", required = true, dataType = "int", paramType = "path")
    @GetMapping("/soft/{softId}")
    @OnRouterEvent("根据软件ID查询容器列表")
    public ReturnResult<List<SystemSoftContainer>> getContainersBySoftId(
            @Parameter(description = "软件ID") @PathVariable @NotNull Integer softId) {

        log.info("根据软件ID查询容器列表 - 软件ID: {}", softId);

        List<SystemSoftContainer> result = systemSoftContainerService.getContainersBySoftId(softId);
        return ReturnResult.ok(result);
    }

    /**
     * 根据服务器ID查询容器列表
     *
     * @param serverId 服务器ID
     * @return 容器列表
     */
    @ApiOperation("根据服务器ID查询容器列表")
    @Operation(summary = "根据服务器ID查询容器列表", description = "根据服务器ID查询该服务器的所有容器")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "path")
    @GetMapping("/server/{serverId}")
    @OnRouterEvent("根据服务器ID查询容器列表")
    public ReturnResult<List<SystemSoftContainer>> getContainersByServerId(
            @Parameter(description = "服务器ID") @PathVariable @NotNull Integer serverId) {

        log.info("根据服务器ID查询容器列表 - 服务器ID: {}", serverId);

        List<SystemSoftContainer> result = systemSoftContainerService.getContainersByServerId(serverId);
        return ReturnResult.ok(result);
    }

    /**
     * 根据ID查询容器详情
     *
     * @param id 容器ID
     * @return 容器详情
     */
    @ApiOperation("根据ID查询容器详情")
    @Operation(summary = "根据ID查询容器详情", description = "根据容器ID查询容器详细信息")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @GetMapping("/{id}")
    @OnRouterEvent("查询容器详情")
    public ReturnResult<SystemSoftContainer> getContainerById(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("查询容器详情 - 容器ID: {}", id);

        SystemSoftContainer result = systemSoftContainerService.getById(id);
        if (result == null) {
            return ReturnResult.error("容器不存在");
        }
        return ReturnResult.ok(result);
    }

    /**
     * 创建容器
     *
     * @param request 创建容器请求
     * @return 创建结果
     */
    @ApiOperation("创建容器")
    @Operation(summary = "创建容器", description = "根据软件镜像创建新容器")
    @PostMapping("/create")
    @OnRouterEvent("创建容器")
    public ReturnResult<Boolean> createContainer(@RequestBody @Valid CreateContainerRequest request) {

        log.info("创建容器 - 请求参数: {}", Json.toJson(request));

        boolean result = systemSoftContainerService.createContainer(
                request.getSoftId(),
                request.getServerId(),
                request.getContainerName(),
                request.getImageTag(),
                request.getPorts(),
                request.getEnvVars(),
                request.getVolumes(),
                request.getCommand(),
                request.getArgs()
        );

        if (result) {
            return ReturnResult.ok(true, "容器创建成功");
        } else {
            return ReturnResult.error("容器创建失败");
        }
    }

    /**
     * 启动容器
     *
     * @param id 容器ID
     * @return 启动结果
     */
    @ApiOperation("启动容器")
    @Operation(summary = "启动容器", description = "启动指定的容器")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @PostMapping("/{id}/start")
    @OnRouterEvent("启动容器")
    public ReturnResult<Boolean> startContainer(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("启动容器 - 容器ID: {}", id);

        boolean result = systemSoftContainerService.startContainer(id);

        if (result) {
            return ReturnResult.ok(true, "容器启动成功");
        } else {
            return ReturnResult.error("容器启动失败");
        }
    }

    /**
     * 停止容器
     *
     * @param id 容器ID
     * @return 停止结果
     */
    @ApiOperation("停止容器")
    @Operation(summary = "停止容器", description = "停止指定的容器")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @PostMapping("/{id}/stop")
    @OnRouterEvent("停止容器")
    public ReturnResult<Boolean> stopContainer(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("停止容器 - 容器ID: {}", id);

        boolean result = systemSoftContainerService.stopContainer(id);

        if (result) {
            return ReturnResult.ok(true, "容器停止成功");
        } else {
            return ReturnResult.error("容器停止失败");
        }
    }

    /**
     * 重启容器
     *
     * @param id 容器ID
     * @return 重启结果
     */
    @ApiOperation("重启容器")
    @Operation(summary = "重启容器", description = "重启指定的容器")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @PostMapping("/{id}/restart")
    @OnRouterEvent("重启容器")
    public ReturnResult<Boolean> restartContainer(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("重启容器 - 容器ID: {}", id);

        boolean result = systemSoftContainerService.restartContainer(id);

        if (result) {
            return ReturnResult.ok(true, "容器重启成功");
        } else {
            return ReturnResult.error("容器重启失败");
        }
    }

    /**
     * 删除容器
     *
     * @param id    容器ID
     * @param force 是否强制删除
     * @return 删除结果
     */
    @ApiOperation("删除容器")
    @Operation(summary = "删除容器", description = "删除指定的容器")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "force", value = "是否强制删除", defaultValue = "false", dataType = "boolean", paramType = "query")
    })
    @DeleteMapping("/{id}")
    @OnRouterEvent("删除容器")
    public ReturnResult<Boolean> removeContainer(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id,
            @Parameter(description = "是否强制删除") @RequestParam(defaultValue = "false") Boolean force) {

        log.info("删除容器 - 容器ID: {}, 强制删除: {}", id, force);

        boolean result = systemSoftContainerService.removeContainer(id, force);

        if (result) {
            return ReturnResult.ok(true, "容器删除成功");
        } else {
            return ReturnResult.error("容器删除失败");
        }
    }

    /**
     * 获取容器日志
     *
     * @param id    容器ID
     * @param lines 日志行数
     * @return 日志内容
     */
    @ApiOperation("获取容器日志")
    @Operation(summary = "获取容器日志", description = "获取指定容器的日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path"),
            @ApiImplicitParam(name = "lines", value = "日志行数", defaultValue = "100", dataType = "int", paramType = "query")
    })
    @GetMapping("/{id}/logs")
    @OnRouterEvent("获取容器日志")
    public ReturnResult<String> getContainerLogs(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id,
            @Parameter(description = "日志行数") @RequestParam(defaultValue = "100") Integer lines) {

        log.info("获取容器日志 - 容器ID: {}, 行数: {}", id, lines);

        String result = systemSoftContainerService.getContainerLogs(id, lines);
        return ReturnResult.ok(result);
    }

    /**
     * 获取容器统计信息
     *
     * @param id 容器ID
     * @return 统计信息
     */
    @ApiOperation("获取容器统计信息")
    @Operation(summary = "获取容器统计信息", description = "获取指定容器的资源使用统计信息")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @GetMapping("/{id}/stats")
    @OnRouterEvent("获取容器统计信息")
    public ReturnResult<Statistics> getContainerStats(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("获取容器统计信息 - 容器ID: {}", id);

        Statistics result = systemSoftContainerService.getContainerStats(id);
        return ReturnResult.ok(result);
    }

    /**
     * 同步容器状态
     *
     * @param serverId 服务器ID
     * @return 同步结果
     */
    @ApiOperation("同步容器状态")
    @Operation(summary = "同步容器状态", description = "从Docker API同步容器状态到数据库")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query")
    @PostMapping("/sync")
    @OnRouterEvent("同步容器状态")
    public ReturnResult<Integer> syncContainerStatus(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId) {

        log.info("同步容器状态 - 服务器ID: {}", serverId);

        int result = systemSoftContainerService.syncContainerStatus(serverId);
        return ReturnResult.ok(result, "同步完成，更新了 " + result + " 个容器状态");
    }

    /**
     * 同步单个容器状态
     *
     * @param id 容器ID
     * @return 同步结果
     */
    @ApiOperation("同步单个容器状态")
    @Operation(summary = "同步单个容器状态", description = "从Docker API同步指定容器的状态到数据库")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @PostMapping("/{id}/sync")
    @OnRouterEvent("同步单个容器状态")
    public ReturnResult<SystemSoftContainer> syncSingleContainerStatus(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id) {

        log.info("同步单个容器状态 - 容器ID: {}", id);

        SystemSoftContainer result = systemSoftContainerService.syncSingleContainerStatus(id);
        if (result != null) {
            return ReturnResult.ok(result, "容器状态同步成功");
        } else {
            return ReturnResult.error("容器状态同步失败，容器不存在或无法连接到Docker服务");
        }
    }

    /**
     * 批量操作容器
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @ApiOperation("批量操作容器")
    @Operation(summary = "批量操作容器", description = "批量启动、停止、重启或删除容器")
    @PostMapping("/batch")
    @OnRouterEvent("批量操作容器")
    public ReturnResult<Map<String, Object>> batchOperateContainers(@RequestBody @Valid BatchOperateRequest request) {

        log.info("批量操作容器 - 操作类型: {}, 容器数量: {}", request.getOperation(), request.getContainerIds().size());

        Map<String, Object> result = systemSoftContainerService.batchOperateContainers(request.getContainerIds(), request.getOperation());
        return ReturnResult.ok(result);
    }

    /**
     * 获取容器状态统计
     *
     * @param serverId 服务器ID
     * @return 状态统计
     */
    @ApiOperation("获取容器状态统计")
    @Operation(summary = "获取容器状态统计", description = "获取各状态容器的数量统计")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", dataType = "int", paramType = "query")
    @GetMapping("/stats")
    @OnRouterEvent("获取容器状态统计")
    public ReturnResult<Map<String, Long>> getContainerStatusStats(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId) {

        log.info("获取容器状态统计 - 服务器ID: {}", serverId);

        Map<String, Long> result = systemSoftContainerService.getContainerStatusStats(serverId);
        return ReturnResult.ok(result);
    }

    /**
     * 获取WebSocket主题信息
     *
     * @return WebSocket主题列表
     */
    @ApiOperation("获取WebSocket主题信息")
    @Operation(summary = "获取WebSocket主题信息", description = "获取WebSocket推送主题列表")
    @GetMapping("/websocket/topics")
    @OnRouterEvent("获取WebSocket主题信息")
    public ReturnResult<Map<String, String>> getWebSocketTopics() {
        if (webSocketService == null) {
            return ReturnResult.error("WebSocket服务未启用");
        }
        return ReturnResult.ok(webSocketService.getTopics());
    }

    /**
     * 获取WebSocket连接状态
     *
     * @return 连接状态信息
     */
    @ApiOperation("获取WebSocket连接状态")
    @Operation(summary = "获取WebSocket连接状态", description = "获取WebSocket连接状态信息")
    @GetMapping("/websocket/status")
    @OnRouterEvent("获取WebSocket连接状态")
    public ReturnResult<Map<String, Object>> getWebSocketStatus() {
        if (webSocketService == null) {
            return ReturnResult.error("WebSocket服务未启用");
        }
        return ReturnResult.ok(webSocketService.getConnectionStatus());
    }

    /**
     * 手动推送容器统计信息
     *
     * @param serverId 服务器ID
     * @return 推送结果
     */
    @ApiOperation("手动推送容器统计信息")
    @Operation(summary = "手动推送容器统计信息", description = "手动推送容器统计信息到WebSocket客户端")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "query")
    @PostMapping("/websocket/push-statistics")
    @OnRouterEvent("手动推送容器统计信息")
    public ReturnResult<String> pushContainerStatistics(
            @Parameter(description = "服务器ID") @RequestParam @NotNull Integer serverId) {
        if (webSocketService == null) {
            return ReturnResult.error("WebSocket服务未启用");
        }
        try {
            webSocketService.pushContainerStatistics(serverId);
            return ReturnResult.ok("推送成功");
        } catch (Exception e) {
            log.error("推送容器统计信息失败: serverId={}", serverId, e);
            return ReturnResult.error("推送失败: " + e.getMessage());
        }
    }

    /**
     * 手动推送批量容器状态
     *
     * @param serverId 服务器ID
     * @return 推送结果
     */
    @ApiOperation("手动推送批量容器状态")
    @Operation(summary = "手动推送批量容器状态", description = "手动推送批量容器状态到WebSocket客户端")
    @ApiImplicitParam(name = "serverId", value = "服务器ID", required = true, dataType = "int", paramType = "query")
    @PostMapping("/websocket/push-status")
    @OnRouterEvent("手动推送批量容器状态")
    public ReturnResult<String> pushBatchContainerStatus(
            @Parameter(description = "服务器ID") @RequestParam @NotNull Integer serverId) {
        if (webSocketService == null) {
            return ReturnResult.error("WebSocket服务未启用");
        }
        try {
            webSocketService.pushBatchContainerStatus(serverId);
            return ReturnResult.ok("推送成功");
        } catch (Exception e) {
            log.error("推送批量容器状态失败: serverId={}", serverId, e);
            return ReturnResult.error("推送失败: " + e.getMessage());
        }
    }

    /**
     * 获取运行中的容器列表
     *
     * @return 运行中的容器列表
     */
    @ApiOperation("获取运行中的容器列表")
    @Operation(summary = "获取运行中的容器列表", description = "获取所有运行中的容器")
    @GetMapping("/running")
    @OnRouterEvent("获取运行中的容器列表")
    public ReturnResult<List<SystemSoftContainer>> getRunningContainers() {

        log.info("获取运行中的容器列表");

        List<SystemSoftContainer> result = systemSoftContainerService.getRunningContainers();
        return ReturnResult.ok(result);
    }

    /**
     * 获取异常容器列表
     *
     * @return 异常容器列表
     */
    @ApiOperation("获取异常容器列表")
    @Operation(summary = "获取异常容器列表", description = "获取所有异常状态的容器")
    @GetMapping("/abnormal")
    @OnRouterEvent("获取异常容器列表")
    public ReturnResult<List<SystemSoftContainer>> getAbnormalContainers() {

        log.info("获取异常容器列表");

        List<SystemSoftContainer> result = systemSoftContainerService.getAbnormalContainers();
        return ReturnResult.ok(result);
    }

    /**
     * 更新容器
     *
     * @param id      容器ID
     * @param request 更新容器请求
     * @return 更新结果
     */
    @ApiOperation("更新容器")
    @Operation(summary = "更新容器", description = "更新容器配置信息")
    @ApiImplicitParam(name = "id", value = "容器ID", required = true, dataType = "int", paramType = "path")
    @PutMapping("/{id}")
    @OnRouterEvent("更新容器")
    public ReturnResult<Boolean> updateContainer(
            @Parameter(description = "容器ID") @PathVariable @NotNull Integer id,
            @RequestBody @Valid UpdateContainerRequest request) {

        log.info("更新容器 - 容器ID: {}, 请求参数: {}", id, request);

        return ReturnResult.of(systemSoftContainerService.updateContainer(id, request));
    }

}