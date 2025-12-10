package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.NodeControlRequest;
import com.chua.starter.monitor.pojo.NodeMetricsDTO;
import com.chua.starter.monitor.service.NodeControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节点控制接口
 * <p>
 * 提供节点的日志管理、配置管理、监控数据、重启管理等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/12/08
 */
@RestController
@RequestMapping("v1/node/control")
@Tag(name = "节点控制接口")
@RequiredArgsConstructor
public class NodeControlController {

    private final NodeControlService nodeControlService;

    // ==================== 日志管理 ====================

    /**
     * 获取节点日志文件列表
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 日志文件列表
     */
    @GetMapping("/log/files")
    @Operation(summary = "获取节点日志文件列表", description = "通过actuator获取节点的日志文件列表")
    public ReturnResult<List<String>> getLogFilesForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.getLogFilesForNode(ipAddress, port);
    }

    /**
     * 获取节点日志内容
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param logFile   日志文件名（可选）
     * @param lines     行数（可选）
     * @return 日志内容
     */
    @GetMapping("/log/content")
    @Operation(summary = "获取节点日志内容", description = "通过actuator获取节点的日志内容")
    public ReturnResult<String> getLogContentForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "日志文件名") @RequestParam(required = false) String logFile,
            @Parameter(description = "行数") @RequestParam(required = false, defaultValue = "100") Integer lines) {
        return nodeControlService.getLogContentForNode(ipAddress, port, logFile, lines);
    }

    /**
     * 设置节点日志级别
     *
     * @param request 控制请求
     * @return 操作结果
     */
    @PostMapping("/log/level")
    @Operation(summary = "设置节点日志级别", description = "通过SyncServer通知客户端设置日志级别")
    public ReturnResult<Boolean> setLogLevelForNode(@RequestBody NodeControlRequest request) {
        return nodeControlService.setLogLevelForNode(request);
    }

    // ==================== 配置管理 ====================

    /**
     * 获取节点环境配置
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 环境配置
     */
    @GetMapping("/config/env")
    @Operation(summary = "获取节点环境配置", description = "通过actuator获取节点的环境配置信息")
    public ReturnResult<Map<String, Object>> getEnvironmentForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.getEnvironmentForNode(ipAddress, port);
    }

    /**
     * 获取节点配置属性
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param prefix    配置前缀（可选）
     * @return 配置属性
     */
    @GetMapping("/config/props")
    @Operation(summary = "获取节点配置属性", description = "通过actuator获取节点的配置属性信息")
    public ReturnResult<Map<String, Object>> getConfigPropsForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "配置前缀") @RequestParam(required = false) String prefix) {
        return nodeControlService.getConfigPropsForNode(ipAddress, port, prefix);
    }

    /**
     * 获取节点系统信息
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 系统信息
     */
    @GetMapping("/config/system")
    @Operation(summary = "获取节点系统信息", description = "通过actuator获取节点的系统信息（健康、应用信息、指标等）")
    public ReturnResult<Map<String, Object>> getSystemInfoForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.getSystemInfoForNode(ipAddress, port);
    }

    // ==================== 监控数据 ====================

    /**
     * 获取节点监控指标
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 监控指标
     */
    @GetMapping("/metrics")
    @Operation(summary = "获取节点监控指标", description = "从Redis缓存获取节点的实时监控指标")
    public ReturnResult<NodeMetricsDTO> getNodeMetricsForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.getNodeMetricsForNode(ipAddress, port);
    }

    /**
     * 获取节点监控历史数据
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param minutes   最近多少分钟
     * @return 历史监控数据
     */
    @GetMapping("/metrics/history")
    @Operation(summary = "获取节点监控历史数据", description = "从Redis缓存获取节点的历史监控数据")
    public ReturnResult<List<NodeMetricsDTO>> getNodeMetricsHistoryForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port,
            @Parameter(description = "最近多少分钟") @RequestParam(required = false, defaultValue = "30") Integer minutes) {
        return nodeControlService.getNodeMetricsHistoryForNode(ipAddress, port, minutes);
    }

    // ==================== 重启管理 ====================

    /**
     * 重启节点
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 操作结果
     */
    @PostMapping("/restart")
    @Operation(summary = "重启节点", description = "通过SyncServer通知客户端重启")
    public ReturnResult<Boolean> restartNodeForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.restartNodeForNode(ipAddress, port);
    }

    /**
     * 关闭节点
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 操作结果
     */
    @PostMapping("/shutdown")
    @Operation(summary = "关闭节点", description = "通过SyncServer通知客户端优雅关闭")
    public ReturnResult<Boolean> shutdownNodeForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return nodeControlService.shutdownNodeForNode(ipAddress, port);
    }

    // ==================== 通用接口 ====================

    /**
     * 发送控制命令
     *
     * @param request 控制请求
     * @return 操作结果
     */
    @PostMapping("/command")
    @Operation(summary = "发送控制命令", description = "向节点发送自定义控制命令")
    public ReturnResult<Object> sendControlCommandForNode(@RequestBody NodeControlRequest request) {
        return nodeControlService.sendControlCommandForNode(request);
    }

    /**
     * 检查节点在线状态
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 是否在线
     */
    @GetMapping("/online")
    @Operation(summary = "检查节点在线状态", description = "检查指定节点是否在线")
    public ReturnResult<Boolean> isNodeOnlineForNode(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return ReturnResult.success(nodeControlService.isNodeOnlineForNode(ipAddress, port));
    }
}
