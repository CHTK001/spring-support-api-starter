package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.pojo.NodeStatistics;
import com.chua.starter.monitor.starter.service.OnlineNodeManagementService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
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
@Api(tags = "在线节点管理")
@Tag(name = "在线节点管理", description = "在线节点管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/monitor/nodes")
@RequiredArgsConstructor
public class OnlineNodeController {

    private final OnlineNodeManagementService nodeManagementService;
    private final NodeWebSocketService nodeWebSocketService;

    /**
     * 获取所有在线节点列表
     */
    @ApiOperation("获取所有在线节点列表")
    @Operation(summary = "获取所有在线节点列表", description = "获取当前所有在线的监控节点信息")
    @GetMapping("/online")
    public ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes() {
        try {
            return nodeManagementService.getAllOnlineNodes();
        } catch (Exception e) {
            log.error("获取在线节点列表失败", e);
            return ReturnResult.error("获取在线节点列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据应用名称分组获取在线节点
     */
    @ApiOperation("根据应用名称分组获取在线节点")
    @Operation(summary = "根据应用名称分组获取在线节点", description = "按应用名称分组返回在线节点，用于卡片叠放展示")
    @GetMapping("/online/grouped")
    public ReturnResult<Map<String, List<OnlineNodeInfo>>> getOnlineNodesByApplication() {
        try {
            return nodeManagementService.getOnlineNodesByApplication();
        } catch (Exception e) {
            log.error("按应用分组获取节点失败", e);
            return ReturnResult.error("按应用分组获取节点失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点统计信息
     */
    @ApiOperation("获取节点统计信息")
    @Operation(summary = "获取节点统计信息", description = "获取节点的统计概览信息，包括总数、在线数、健康状态等")
    @GetMapping("/statistics")
    public ReturnResult<NodeStatistics> getNodeStatistics() {
        try {
            return nodeManagementService.getNodeStatistics();
        } catch (Exception e) {
            log.error("获取节点统计信息失败", e);
            return ReturnResult.error("获取节点统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据节点ID获取节点详细信息
     */
    @ApiOperation("获取节点详细信息")
    @Operation(summary = "获取节点详细信息", description = "根据节点ID获取单个节点的详细信息")
    @GetMapping("/{nodeId}")
    public ReturnResult<OnlineNodeInfo> getNodeDetails(
            @ApiParam(value = "节点ID", required = true) @PathVariable String nodeId) {
        try {
            return nodeManagementService.getNodeDetails(nodeId);
        } catch (Exception e) {
            log.error("获取节点详细信息失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点详细信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索节点
     */
    @ApiOperation("搜索节点")
    @Operation(summary = "搜索节点", description = "根据关键词搜索节点，支持节点名称、IP地址、应用名称")
    @GetMapping("/search")
    public ReturnResult<List<OnlineNodeInfo>> searchNodes(
            @ApiParam(value = "搜索关键词", required = true) @RequestParam String keyword) {
        try {
            return nodeManagementService.searchNodes(keyword);
        } catch (Exception e) {
            log.error("搜索节点失败: keyword={}", keyword, e);
            return ReturnResult.error("搜索节点失败: " + e.getMessage());
        }
    }

    /**
     * 刷新节点数据
     */
    @ApiOperation("刷新节点数据")
    @Operation(summary = "刷新节点数据", description = "手动刷新节点数据，重新从发现服务获取最新信息")
    @PostMapping("/refresh")
    public ReturnResult<List<OnlineNodeInfo>> refreshNodes() {
        try {
            // 直接重新获取最新数据
            return nodeManagementService.getAllOnlineNodes();
        } catch (Exception e) {
            log.error("刷新节点数据失败", e);
            return ReturnResult.error("刷新节点数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点健康状态概览
     */
    @ApiOperation("获取节点健康状态概览")
    @Operation(summary = "获取节点健康状态概览", description = "获取所有节点的健康状态概览信息")
    @GetMapping("/health/overview")
    public ReturnResult<Map<String, Object>> getHealthOverview() {
        try {
            ReturnResult<NodeStatistics> statisticsResult = nodeManagementService.getNodeStatistics();
            if (!statisticsResult.isOk()) {
                return ReturnResult.error(statisticsResult.getMsg());
            }

            NodeStatistics statistics = statisticsResult.getData();
            Map<String, Object> overview = Map.of("totalNodes", statistics.getTotalNodes(), "onlineNodes",
                    statistics.getOnlineNodes(), "healthyNodes", statistics.getHealthyNodes(), "onlineRate",
                    statistics.getOnlineRate(), "healthyRate", statistics.getHealthyRate(), "statusDescription",
                    statistics.getStatusDescription(), "isOverallHealthy", statistics.isOverallHealthy());

            return ReturnResult.ok(overview);
        } catch (Exception e) {
            log.error("获取节点健康状态概览失败", e);
            return ReturnResult.error("获取节点健康状态概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取应用名称列表
     */
    @ApiOperation("获取应用名称列表")
    @Operation(summary = "获取应用名称列表", description = "获取所有在线节点的应用名称列表，用于筛选")
    @GetMapping("/applications")
    public ReturnResult<List<String>> getApplicationNames() {
        try {
            ReturnResult<Map<String, List<OnlineNodeInfo>>> groupedResult = nodeManagementService
                    .getOnlineNodesByApplication();
            if (!groupedResult.isOk()) {
                return ReturnResult.error(groupedResult.getMsg());
            }

            List<String> applicationNames = groupedResult.getData().keySet().stream().sorted()
                    .collect(java.util.stream.Collectors.toList());

            return ReturnResult.ok(applicationNames);
        } catch (Exception e) {
            log.error("获取应用名称列表失败", e);
            return ReturnResult.error("获取应用名称列表失败: " + e.getMessage());
        }
    }
}
