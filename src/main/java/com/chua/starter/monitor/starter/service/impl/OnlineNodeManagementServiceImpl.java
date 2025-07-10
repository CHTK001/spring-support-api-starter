package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.discovery.support.service.DiscoveryService;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.pojo.NodeStatistics;
import com.chua.starter.monitor.starter.service.OnlineNodeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
            // 从Discovery服务获取所有在线的监控节点
            Set<Discovery> discoveries = discoveryService.getDiscoveryAll("monitor");
            if (discoveries == null || discoveries.isEmpty()) {
                return ReturnResult.ok(new ArrayList<>());
            }

            List<OnlineNodeInfo> nodes = discoveries.stream()
                    .map(this::convertToOnlineNodeInfo)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(OnlineNodeInfo::getApplicationName)
                            .thenComparing(OnlineNodeInfo::getIpAddress))
                    .collect(Collectors.toList());

            return ReturnResult.ok(nodes);
        } catch (Exception e) {
            log.error("获取在线节点列表失败", e);
            return ReturnResult.error("获取在线节点列表失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, List<OnlineNodeInfo>>> getOnlineNodesByApplication() {
        try {
            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isOk()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            Map<String, List<OnlineNodeInfo>> groupedNodes = allNodesResult.getData().stream()
                    .collect(Collectors.groupingBy(
                            node -> StringUtils.defaultString(node.getApplicationName(), "未知应用"),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

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
            if (!allNodesResult.isOk()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            List<OnlineNodeInfo> nodes = allNodesResult.getData();
            
            // 计算统计信息
            int totalNodes = nodes.size();
            int onlineNodes = (int) nodes.stream().filter(OnlineNodeInfo::isOnline).count();
            int healthyNodes = (int) nodes.stream().filter(OnlineNodeInfo::isHealthy).count();
            int totalConnections = nodes.stream().mapToInt(node -> 
                    node.getConnectionCount() != null ? node.getConnectionCount() : 0).sum();

            // 按状态分组
            Map<String, Integer> nodesByStatus = nodes.stream()
                    .collect(Collectors.groupingBy(
                            node -> node.getStatus() != null ? node.getStatus().name() : "UNKNOWN",
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                    ));

            // 按应用分组
            Map<String, Integer> nodesByApplication = nodes.stream()
                    .collect(Collectors.groupingBy(
                            node -> StringUtils.defaultString(node.getApplicationName(), "未知应用"),
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                    ));

            // 计算平均值
            double avgResponseTime = nodes.stream()
                    .filter(node -> node.getResponseTime() != null)
                    .mapToLong(OnlineNodeInfo::getResponseTime)
                    .average().orElse(0.0);

            double avgCpuUsage = nodes.stream()
                    .filter(node -> node.getCpuUsage() != null)
                    .mapToDouble(OnlineNodeInfo::getCpuUsage)
                    .average().orElse(0.0);

            double avgMemoryUsage = nodes.stream()
                    .filter(node -> node.getMemoryUsage() != null)
                    .mapToDouble(OnlineNodeInfo::getMemoryUsage)
                    .average().orElse(0.0);

            NodeStatistics statistics = NodeStatistics.builder()
                    .totalNodes(totalNodes)
                    .onlineNodes(onlineNodes)
                    .offlineNodes(totalNodes - onlineNodes)
                    .healthyNodes(healthyNodes)
                    .totalConnections(totalConnections)
                    .averageResponseTime(avgResponseTime)
                    .averageCpuUsage(avgCpuUsage)
                    .averageMemoryUsage(avgMemoryUsage)
                    .nodesByStatus(nodesByStatus)
                    .nodesByApplication(nodesByApplication)
                    .build();

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
            if (!allNodesResult.isOk()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            Optional<OnlineNodeInfo> nodeOpt = allNodesResult.getData().stream()
                    .filter(node -> nodeId.equals(node.getNodeId()))
                    .findFirst();

            if (nodeOpt.isPresent()) {
                return ReturnResult.ok(nodeOpt.get());
            } else {
                return ReturnResult.error("节点不存在: " + nodeId);
            }
        } catch (Exception e) {
            log.error("获取节点详细信息失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<OnlineNodeInfo>> searchNodes(String keyword) {
        try {
            if (StringUtils.isBlank(keyword)) {
                return getAllOnlineNodes();
            }

            ReturnResult<List<OnlineNodeInfo>> allNodesResult = getAllOnlineNodes();
            if (!allNodesResult.isOk()) {
                return ReturnResult.error(allNodesResult.getMsg());
            }

            String lowerKeyword = keyword.toLowerCase();
            List<OnlineNodeInfo> filteredNodes = allNodesResult.getData().stream()
                    .filter(node -> matchesKeyword(node, lowerKeyword))
                    .collect(Collectors.toList());

            return ReturnResult.ok(filteredNodes);
        } catch (Exception e) {
            log.error("搜索节点失败: keyword={}", keyword, e);
            return ReturnResult.error("搜索节点失败: " + e.getMessage());
        }
    }

    /**
     * 将Discovery对象转换为OnlineNodeInfo
     */
    private OnlineNodeInfo convertToOnlineNodeInfo(Discovery discovery) {
        try {
            Map<String, String> metadata = discovery.getMetadata();
            String applicationName = MapUtils.getString(metadata, "applicationName");
            
            return OnlineNodeInfo.builder()
                    .nodeId(generateNodeId(discovery))
                    .nodeName(applicationName)
                    .applicationName(applicationName)
                    .ipAddress(discovery.getHost())
                    .port(discovery.getPort())
                    .status(OnlineNodeInfo.NodeStatus.ONLINE)
                    .connectTime(LocalDateTime.now()) // 这里可以从metadata中获取实际连接时间
                    .lastHeartbeatTime(LocalDateTime.now())
                    .connectionCount(1) // 默认连接数
                    .version(MapUtils.getString(metadata, "version", "未知"))
                    .nodeType(MapUtils.getString(metadata, "nodeType", "monitor"))
                    .healthy(true)
                    .responseTime(0L) // 可以通过ping测试获取
                    .metadata(new HashMap<>(metadata))
                    .build();
        } catch (Exception e) {
            log.warn("转换Discovery对象失败: {}", discovery, e);
            return null;
        }
    }

    /**
     * 生成节点ID
     */
    private String generateNodeId(Discovery discovery) {
        return discovery.getHost() + ":" + discovery.getPort();
    }

    /**
     * 检查节点是否匹配关键词
     */
    private boolean matchesKeyword(OnlineNodeInfo node, String keyword) {
        return (node.getNodeName() != null && node.getNodeName().toLowerCase().contains(keyword)) ||
               (node.getApplicationName() != null && node.getApplicationName().toLowerCase().contains(keyword)) ||
               (node.getIpAddress() != null && node.getIpAddress().toLowerCase().contains(keyword)) ||
               (node.getNodeId() != null && node.getNodeId().toLowerCase().contains(keyword));
    }
}
