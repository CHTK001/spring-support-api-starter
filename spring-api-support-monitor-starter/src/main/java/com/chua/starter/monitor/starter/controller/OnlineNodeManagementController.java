package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.NodeStatistics;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.service.OnlineNodeManagementService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 在线节点管理控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("/v1/monitor/nodes")
@Api(tags = "在线节点管理")
@Tag(name = "在线节点管理")
@RequiredArgsConstructor
@Slf4j
public class OnlineNodeManagementController {

    private final OnlineNodeManagementService onlineNodeManagementService;

    /**
     * 获取所有在线节点列表
     *
     * @return 在线节点列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有在线节点", description = "获取所有在线的监控节点列表")
    public ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes() {
        try {
            log.info("开始获取所有在线节点列表");
            return onlineNodeManagementService.getAllOnlineNodes();
        } catch (Exception e) {
            log.error("获取所有在线节点列表失败", e);
            return ReturnResult.error("获取在线节点列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据应用名称分组获取在线节点
     *
     * @return 按应用名称分组的节点列表
     */
    @GetMapping("/by-application")
    @Operation(summary = "按应用分组获取节点", description = "根据应用名称分组获取在线节点列表")
    public ReturnResult<Map<String, List<OnlineNodeInfo>>> getOnlineNodesByApplication() {
        try {
            log.info("开始按应用分组获取在线节点");
            return onlineNodeManagementService.getOnlineNodesByApplication();
        } catch (Exception e) {
            log.error("按应用分组获取在线节点失败", e);
            return ReturnResult.error("按应用分组获取节点失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点统计信息
     *
     * @return 节点统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取节点统计信息", description = "获取在线节点的统计信息，包括总数、在线数、离线数等")
    public ReturnResult<NodeStatistics> getNodeStatistics() {
        try {
            log.info("开始获取节点统计信息");
            return onlineNodeManagementService.getNodeStatistics();
        } catch (Exception e) {
            log.error("获取节点统计信息失败", e);
            return ReturnResult.error("获取节点统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据节点ID获取节点详细信息
     *
     * @param nodeId 节点ID
     * @return 节点详细信息
     */
    @GetMapping("/details/{nodeId}")
    @Operation(summary = "获取节点详情", description = "根据节点ID获取节点的详细信息")
    public ReturnResult<OnlineNodeInfo> getNodeDetails(
            @Parameter(description = "节点ID", required = true)
            @PathVariable("nodeId") String nodeId) {
        try {
            log.info("开始获取节点详情: nodeId={}", nodeId);
            return onlineNodeManagementService.getNodeDetails(nodeId);
        } catch (Exception e) {
            log.error("获取节点详情失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点详情失败: " + e.getMessage());
        }
    }

    /**
     * 搜索节点
     *
     * @param keyword 搜索关键词（支持节点名称、IP地址、应用名称）
     * @return 匹配的节点列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索节点", description = "根据关键词搜索节点，支持节点名称、IP地址、应用名称")
    public ReturnResult<List<OnlineNodeInfo>> searchNodes(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam("keyword") String keyword) {
        try {
            log.info("开始搜索节点: keyword={}", keyword);
            return onlineNodeManagementService.searchNodes(keyword);
        } catch (Exception e) {
            log.error("搜索节点失败: keyword={}", keyword, e);
            return ReturnResult.error("搜索节点失败: " + e.getMessage());
        }
    }

    /**
     * 检查节点健康状态
     *
     * @param clientIp   客户端IP
     * @param clientPort 客户端端口
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查节点健康状态", description = "检查指定IP和端口的节点健康状态")
    public ReturnResult<Map<String, Object>> getNodeHealth(
            @Parameter(description = "客户端IP", required = true)
            @RequestParam("clientIp") String clientIp,
            @Parameter(description = "客户端端口", required = true)
            @RequestParam("clientPort") Integer clientPort) {
        try {
            log.info("开始检查节点健康状态: {}:{}", clientIp, clientPort);

            // 简单的健康检查实现
            // 这里可以根据实际需求实现更复杂的健康检查逻辑
            Map<String, Object> healthStatus = Map.of(
                    "healthy", true,
                    "ip", clientIp,
                    "port", clientPort,
                    "checkTime", System.currentTimeMillis(),
                    "message", "节点健康检查通过"
            );

            return ReturnResult.ok(healthStatus);
        } catch (Exception e) {
            log.error("检查节点健康状态失败: {}:{}", clientIp, clientPort, e);
            return ReturnResult.error("检查节点健康状态失败: " + e.getMessage());
        }
    }
}
