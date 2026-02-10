package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.monitor.pojo.node.NodeStatistics;
import com.chua.starter.monitor.pojo.node.OnlineNodeInfo;
import com.chua.starter.monitor.service.node.NodeSystemService;
import com.chua.starter.monitor.service.node.NodeManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NodeManagementServiceImpl implements NodeManagementService {

    /**
     * SyncServer 可选依赖，当未配置时为 null
     */
    @Autowired(required = false)
    private SyncServer syncServer;
    
    private final NodeSystemService nodeSystemService;

    // 缓存最后一次获取的节点列表，避免频繁调用发现服务
    private volatile List<OnlineNodeInfo> cachedNodeList = new ArrayList<>();
    private volatile long lastUpdateTime = 0;
    private static final long CACHE_DURATION = 30000; // 30秒缓存

    @Override
    public ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes() {
        return getAllOnlineNodes(false);
    }

    /**
     * 获取所有在线节点列表
     * 
     * @param forceRefresh 是否强制刷新缓存
     */
    private ReturnResult<List<OnlineNodeInfo>> getAllOnlineNodes(boolean forceRefresh) {
        try {
            long currentTime = System.currentTimeMillis();

            // 检查缓存是否有效
            if (!forceRefresh && (currentTime - lastUpdateTime) < CACHE_DURATION && !cachedNodeList.isEmpty()) {
                log.debug("使用缓存的节点列表，共 {} 个节点", cachedNodeList.size());
                return ReturnResult.ok(new ArrayList<>(cachedNodeList));
            }

            // 获取所有在线的监控节点
            if (syncServer == null) {
                log.info("SyncServer 未初始化");
                cachedNodeList = new ArrayList<>();
                lastUpdateTime = currentTime;
                return ReturnResult.ok(new ArrayList<>());
            }
            
            Map<String, ClientInfo> allClients = syncServer.getAllClients();
            if (allClients == null || allClients.isEmpty()) {
                log.info("未发现在线节点");
                cachedNodeList = new ArrayList<>();
                lastUpdateTime = currentTime;
                return ReturnResult.ok(new ArrayList<>());
            }

            // 转换为 OnlineNodeInfo 列表，过滤掉转换失败的节点
            // 注意：SyncServer.getAllClients() 已根据 IP + 端口去重
            List<OnlineNodeInfo> nodeList = allClients.values().stream()
                    .map(this::convertToOnlineNodeInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.debug("获取在线节点：SyncServer 返回 {} 个客户端，转换成功 {} 个节点", 
                    allClients.size(), nodeList.size());

            // 同步节点数据到数据库
            try {
                nodeSystemService.syncOnlineNodes(nodeList);
                log.debug("节点数据同步到数据库成功");
            } catch (Exception e) {
                log.error("同步节点数据到数据库失败", e);
                // 不影响主流程，继续返回在线节点列表
            }

            // 更新缓存
            cachedNodeList = new ArrayList<>(nodeList);
            lastUpdateTime = currentTime;

            log.info("获取在线节点列表成功，共 {} 个节点", nodeList.size());
            return ReturnResult.ok(nodeList);

        } catch (Exception e) {
            log.error("获取在线节点列表失败", e);
            // 如果有缓存数据，返回缓存数据
            if (!cachedNodeList.isEmpty()) {
                log.warn("获取节点列表失败，返回缓存数据");
                return ReturnResult.ok(new ArrayList<>(cachedNodeList));
            }
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

    @Override
    public OnlineNodeInfo getOnlineNode(String ipAddress, Integer port) {
        ReturnResult<List<OnlineNodeInfo>> allOnlineNodes = getAllOnlineNodes();
        if(!allOnlineNodes.isSuccess()) {
            return null;
        }
        for (OnlineNodeInfo node : allOnlineNodes.getData()) {
            if (node.getIpAddress().equals(ipAddress) && node.getPort().equals(port)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 将 ClientInfo 对象转换为 OnlineNodeInfo
     */
    private OnlineNodeInfo convertToOnlineNodeInfo(ClientInfo clientInfo) {
        if (clientInfo == null) {
            log.warn("ClientInfo对象为空，无法转换");
            return null;
        }

        try {
            OnlineNodeInfo nodeInfo = new OnlineNodeInfo();

            // 基本信息
            nodeInfo.setNodeId(generateNodeId(clientInfo));
            nodeInfo.setApplicationName(clientInfo.getClientApplicationName() != null ? clientInfo.getClientApplicationName() : "unknown");
            nodeInfo.setNodeName(clientInfo.getClientHostname() != null ? clientInfo.getClientHostname() : 
                    clientInfo.getClientIpAddress() + ":" + clientInfo.getClientPort());
            nodeInfo.setIpAddress(clientInfo.getClientIpAddress());
            nodeInfo.setPort(clientInfo.getClientPort());
            // 状态信息

            nodeInfo.setStatus(clientInfo.isClientOnline() ? OnlineNodeInfo.NodeStatus.ONLINE : OnlineNodeInfo.NodeStatus.OFFLINE);
            nodeInfo.setConnectTime(LocalDateTime.now());
            nodeInfo.setContextPath(clientInfo.getClientContextPath());
            nodeInfo.setActuatorPath(clientInfo.getClientActuatorPath());
            
            // 从心跳时间设置最后心跳时间
            if (clientInfo.getClientLastHeartbeatTime() > 0) {
                nodeInfo.setLastHeartbeatTime(java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(clientInfo.getClientLastHeartbeatTime()),
                        java.time.ZoneId.systemDefault()));
            } else {
                nodeInfo.setLastHeartbeatTime(LocalDateTime.now());
            }

            // 健康状态从元数据获取，默认为在线即健康
            nodeInfo.setHealthy(clientInfo.isClientOnline());

            // 版本和类型信息
            nodeInfo.setVersion(clientInfo.getClientJavaVersion() != null ? clientInfo.getClientJavaVersion() : "unknown");
            nodeInfo.setNodeType("monitor");

            // 性能信息（从元数据中获取，如果有的话）
            Map<String, Object> metadata = clientInfo.getClientMetadata();
            if (metadata != null) {
                Double cpuUsage = MapUtils.getDouble(metadata, "cpuUsage");
                if (cpuUsage != null && cpuUsage >= 0 && cpuUsage <= 100) {
                    nodeInfo.setCpuUsage(cpuUsage);
                }

                Double memoryUsage = MapUtils.getDouble(metadata, "memoryUsage");
                if (memoryUsage != null && memoryUsage >= 0 && memoryUsage <= 100) {
                    nodeInfo.setMemoryUsage(memoryUsage);
                }

                Double diskUsage = MapUtils.getDouble(metadata, "diskUsage");
                if (diskUsage != null && diskUsage >= 0 && diskUsage <= 100) {
                    nodeInfo.setDiskUsage(diskUsage);
                }

                Long responseTime = MapUtils.getLong(metadata, "responseTime");
                if (responseTime != null && responseTime >= 0) {
                    nodeInfo.setResponseTime(responseTime);
                }

                Long networkLatency = MapUtils.getLong(metadata, "networkLatency");
                if (networkLatency != null && networkLatency >= 0) {
                    nodeInfo.setNetworkLatency(networkLatency);
                }

                // 连接数
                Integer connectionCount = MapUtils.getInteger(metadata, "connectionCount", 1);
                nodeInfo.setConnectionCount(Math.max(connectionCount, 0));
            } else {
                nodeInfo.setConnectionCount(1);
            }

            // 元数据
            Map<String, Object> nodeMetadata = new HashMap<>();
            if (metadata != null) {
                nodeMetadata.putAll(metadata);
            }
            nodeMetadata.put("clientId", clientInfo.getClientId());
            nodeMetadata.put("osName", clientInfo.getClientOsName());
            // 从 clientMetadata 中获取详细信息
            Map<String, Object> clientMeta = clientInfo.getClientMetadata();
            if (clientMeta != null) {
                nodeMetadata.put("osVersion", clientMeta.get("osVersion"));
                nodeMetadata.put("cpuCores", clientMeta.get("cpuCores"));
                nodeMetadata.put("totalMemory", clientMeta.get("totalMemory"));
            }
            nodeInfo.setMetadata(nodeMetadata);

            return nodeInfo;

        } catch (Exception e) {
            log.error("转换ClientInfo对象失败: ip={}, port={}", clientInfo.getClientIpAddress(), clientInfo.getClientPort(), e);
            return null;
        }
    }

    /**
     * 生成节点ID（与NodeSystemService保持一致，使用MD5）
     */
    private String generateNodeId(ClientInfo clientInfo) {
        return nodeSystemService.generateNodeId(clientInfo.getClientIpAddress(), clientInfo.getClientPort());
    }

}
