package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.discovery.support.service.DiscoveryService;
import com.chua.starter.monitor.starter.pojo.NodeStatistics;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.service.OnlineNodeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线节点管理服务实现
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineNodeManagementServiceImpl implements OnlineNodeManagementService {

    private final DiscoveryService discoveryService;

    @Override
    public ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes() {
        try {
            // 获取所有在线的监控节点
            Set<Discovery> discoveryAll = discoveryService.getDiscoveryAll("monitor");

            if (discoveryAll == null || discoveryAll.isEmpty()) {
                log.info("未发现在线节点");
                return ReturnResult.ok(new ArrayList<>());
            }

            // 转换为 OnlineNodeInfo 列表
            List<OnlineNodeInfo> nodeList = discoveryAll.stream().map(this::convertToOnlineNodeInfo)
                    .collect(Collectors.toList());

            log.info("获取在线节点列表成功，共 {} 个节点", nodeList.size());
            return ReturnResult.ok(nodeList);

        } catch (Exception e) {
            log.error("获取在线节点列表失败", e);
            return ReturnResult.error("获取在线节点列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<OnlineNodeInfo>>> getOnlineNodesByApplication() {
        try {
            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isSuccess()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            List<OnlineNodeInfo> allNodes = allNodesResult.getData();

            // 按应用名称分组
            Map<String, List<OnlineNodeInfo>> groupedNodes = allNodes.stream()
                    .collect(Collectors.groupingBy(OnlineNodeInfo::getApplicationName));

            log.info("按应用分组获取节点成功，共 {} 个应用", groupedNodes.size());
            return ReturnResult.ok(groupedNodes);

        } catch (Exception e) {
            log.error("按应用分组获取节点失败", e);
            return ReturnResult.error("按应用分组获取节点失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<NodeStatistics> getNodeStatistics() {
        try {
            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isSuccess()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            List<OnlineNodeInfo> allNodes = allNodesResult.getData();

            // 计算统计信息
            NodeStatistics.NodeStatisticsBuilder builder = NodeStatistics.builder();

            // 基础统计
            builder.totalNodes(allNodes.size());
            builder.onlineNodes((int) allNodes.stream()
                    .filter(node -> OnlineNodeInfo.NodeStatus.ONLINE.equals(node.getStatus())).count());
            builder.offlineNodes((int) allNodes.stream()
                    .filter(node -> OnlineNodeInfo.NodeStatus.OFFLINE.equals(node.getStatus())).count());
            builder.healthyNodes(
                    (int) allNodes.stream().filter(node -> Boolean.TRUE.equals(node.getHealthy())).count());
            builder.errorNodes((int) allNodes.stream()
                    .filter(node -> OnlineNodeInfo.NodeStatus.ERROR.equals(node.getStatus())).count());
            builder.maintenanceNodes((int) allNodes.stream()
                    .filter(node -> OnlineNodeInfo.NodeStatus.MAINTENANCE.equals(node.getStatus())).count());

            // 连接数统计
            int totalConnections = allNodes.stream()
                    .mapToInt(node -> node.getConnectionCount() != null ? node.getConnectionCount() : 0).sum();
            builder.totalConnections(totalConnections);

            // 性能统计
            OptionalDouble avgResponseTime = allNodes.stream().filter(node -> node.getResponseTime() != null)
                    .mapToLong(OnlineNodeInfo::getResponseTime).average();
            builder.averageResponseTime(avgResponseTime.isPresent() ? avgResponseTime.getAsDouble() : 0.0);

            OptionalDouble avgCpuUsage = allNodes.stream().filter(node -> node.getCpuUsage() != null)
                    .mapToDouble(OnlineNodeInfo::getCpuUsage).average();
            builder.averageCpuUsage(avgCpuUsage.isPresent() ? avgCpuUsage.getAsDouble() : 0.0);

            OptionalDouble avgMemoryUsage = allNodes.stream().filter(node -> node.getMemoryUsage() != null)
                    .mapToDouble(OnlineNodeInfo::getMemoryUsage).average();
            builder.averageMemoryUsage(avgMemoryUsage.isPresent() ? avgMemoryUsage.getAsDouble() : 0.0);

            OptionalDouble avgDiskUsage = allNodes.stream().filter(node -> node.getDiskUsage() != null)
                    .mapToDouble(OnlineNodeInfo::getDiskUsage).average();
            builder.averageDiskUsage(avgDiskUsage.isPresent() ? avgDiskUsage.getAsDouble() : 0.0);

            // 分组统计
            Map<String, Integer> nodesByStatus = allNodes.stream()
                    .collect(Collectors.groupingBy(node -> node.getStatus().name(),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
            builder.nodesByStatus(nodesByStatus);

            Map<String, Integer> nodesByApplication = allNodes.stream()
                    .collect(Collectors.groupingBy(OnlineNodeInfo::getApplicationName,
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
            builder.nodesByApplication(nodesByApplication);

            Map<String, Integer> nodesByType = allNodes.stream().filter(node -> node.getNodeType() != null)
                    .collect(Collectors.groupingBy(OnlineNodeInfo::getNodeType,
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
            builder.nodesByType(nodesByType);

            // 最近活跃节点（按连接时间排序，取前5个）
            List<OnlineNodeInfo> recentActiveNodes = allNodes.stream().filter(node -> node.getConnectTime() != null)
                    .sorted((a, b) -> b.getConnectTime().compareTo(a.getConnectTime())).limit(5)
                    .collect(Collectors.toList());
            builder.recentActiveNodes(recentActiveNodes);

            // 响应时间最快的节点（取前5个）
            List<OnlineNodeInfo> fastestNodes = allNodes.stream().filter(node -> node.getResponseTime() != null)
                    .sorted(Comparator.comparing(OnlineNodeInfo::getResponseTime)).limit(5)
                    .collect(Collectors.toList());
            builder.fastestNodes(fastestNodes);

            // 负载最高的节点（按CPU使用率排序，取前5个）
            List<OnlineNodeInfo> highestLoadNodes = allNodes.stream().filter(node -> node.getCpuUsage() != null)
                    .sorted((a, b) -> Double.compare(b.getCpuUsage(), a.getCpuUsage())).limit(5)
                    .collect(Collectors.toList());
            builder.highestLoadNodes(highestLoadNodes);

            NodeStatistics statistics = builder.build();

            log.info("获取节点统计信息成功: 总数={}, 在线={}, 离线={}, 健康={}", statistics.getTotalNodes(), statistics.getOnlineNodes(),
                    statistics.getOfflineNodes(), statistics.getHealthyNodes());

            return ReturnResult.ok(statistics);

        } catch (Exception e) {
            log.error("获取节点统计信息失败", e);
            return ReturnResult.error("获取节点统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<OnlineNodeInfo> getNodeDetails(String nodeId) {
        try {
            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isSuccess()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            List<OnlineNodeInfo> allNodes = allNodesResult.getData();

            // 查找指定节点
            Optional<OnlineNodeInfo> nodeOptional = allNodes.stream().filter(node -> nodeId.equals(node.getNodeId()))
                    .findFirst();

            if (nodeOptional.isPresent()) {
                log.info("获取节点详情成功: nodeId={}", nodeId);
                return ReturnResult.ok(nodeOptional.get());
            } else {
                log.warn("节点不存在: nodeId={}", nodeId);
                return ReturnResult.error("节点不存在");
            }

        } catch (Exception e) {
            log.error("获取节点详情失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点详情失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<OnlineNodeInfo>> searchNodes(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllOnlineNodes();
            }

            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isSuccess()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            List<OnlineNodeInfo> allNodes = allNodesResult.getData();
            String lowerKeyword = keyword.toLowerCase();

            // 搜索匹配的节点
            List<OnlineNodeInfo> matchedNodes = allNodes.stream().filter(
                            node -> (node.getNodeName() != null && node.getNodeName().toLowerCase().contains(lowerKeyword))
                                    || (node.getApplicationName() != null
                                    && node.getApplicationName().toLowerCase().contains(lowerKeyword))
                                    || (node.getIpAddress() != null
                                    && node.getIpAddress().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());

            log.info("搜索节点成功: keyword={}, 匹配数量={}", keyword, matchedNodes.size());
            return ReturnResult.ok(matchedNodes);

        } catch (Exception e) {
            log.error("搜索节点失败: keyword={}", keyword, e);
            return ReturnResult.error("搜索节点失败: " + e.getMessage());
        }
    }

    /**
     * 将 Discovery 对象转换为 OnlineNodeInfo
     */
    private OnlineNodeInfo convertToOnlineNodeInfo(Discovery discovery) {
        OnlineNodeInfo nodeInfo = new OnlineNodeInfo();

        // 基本信息
        nodeInfo.setNodeId(generateNodeId(discovery));
        nodeInfo.setApplicationName(MapUtils.getString(discovery.getMetadata(), "applicationName", "unknown"));
        nodeInfo.setNodeName(MapUtils.getString(discovery.getMetadata(), "nodeName"));
        nodeInfo.setIpAddress(discovery.getHost());
        nodeInfo.setPort(discovery.getPort());

        // 状态信息
        nodeInfo.setStatus(OnlineNodeInfo.NodeStatus.ONLINE); // Discovery中的都是在线的
        nodeInfo.setConnectTime(LocalDateTime.now());
        nodeInfo.setLastHeartbeatTime(LocalDateTime.now());
        nodeInfo.setHealthy(true);

        // 版本和类型信息
        nodeInfo.setVersion(MapUtils.getString(discovery.getMetadata(), "version"));
        nodeInfo.setNodeType(MapUtils.getString(discovery.getMetadata(), "nodeType", "monitor"));

        // 性能信息（从元数据中获取，如果有的话）
        nodeInfo.setCpuUsage(MapUtils.getDouble(discovery.getMetadata(), "cpuUsage"));
        nodeInfo.setMemoryUsage(MapUtils.getDouble(discovery.getMetadata(), "memoryUsage"));
        nodeInfo.setDiskUsage(MapUtils.getDouble(discovery.getMetadata(), "diskUsage"));
        nodeInfo.setResponseTime(MapUtils.getLong(discovery.getMetadata(), "responseTime"));
        nodeInfo.setNetworkLatency(MapUtils.getLong(discovery.getMetadata(), "networkLatency"));

        // 连接数
        nodeInfo.setConnectionCount(MapUtils.getInteger(discovery.getMetadata(), "connectionCount", 1));

        // 元数据 - 转换类型
        Map<String, Object> metadata = new HashMap<>();
        if (discovery.getMetadata() != null) {
            metadata.putAll(discovery.getMetadata());
        }
        nodeInfo.setMetadata(metadata);

        return nodeInfo;
    }

    /**
     * 生成节点ID
     */
    private String generateNodeId(Discovery discovery) {
        return discovery.getHost() + ":" + discovery.getPort();
    }
}
