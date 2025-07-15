package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.monitor.starter.entity.MonitorSysGenNodeSystem;
import com.chua.starter.monitor.starter.mapper.NodeSystemMapper;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;
import com.chua.starter.monitor.starter.service.NodeSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 节点系统服务实现类
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeSystemServiceImpl extends ServiceImpl<NodeSystemMapper, MonitorSysGenNodeSystem>
        implements NodeSystemService {

    private final NodeSystemMapper nodeSystemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncOnlineNodes(List<OnlineNodeInfo> onlineNodes) {
        log.info("开始同步在线节点数据，节点数量: {}", onlineNodes.size());

        try {
            for (OnlineNodeInfo nodeInfo : onlineNodes) {
                createOrUpdateNode(nodeInfo);
            }

            // 获取所有在线节点的ID
            List<String> onlineNodeIds = onlineNodes.stream()
                    .map(node -> generateNodeId(node.getIpAddress(), node.getPort())).collect(Collectors.toList());

            // 将不在在线列表中的节点标记为离线
            if (!onlineNodeIds.isEmpty()) {
                // 查询数据库中状态为在线但不在当前在线列表中的节点
                List<MonitorSysGenNodeSystem> allOnlineInDb = lambdaQuery()
                        .eq(MonitorSysGenNodeSystem::getNodeSystemStatus, "ONLINE").list();

                List<String> offlineNodeIds = allOnlineInDb.stream().map(MonitorSysGenNodeSystem::getNodeSystemId)
                        .filter(id -> !onlineNodeIds.contains(id)).collect(Collectors.toList());

                if (!offlineNodeIds.isEmpty()) {
                    batchUpdateOfflineStatus(offlineNodeIds);
                    log.info("标记 {} 个节点为离线状态", offlineNodeIds.size());
                }
            }

            log.info("节点数据同步完成");
        } catch (Exception e) {
            log.error("同步在线节点数据失败", e);
            throw e;
        }
    }

    @Override
    public MonitorSysGenNodeSystem getByIpAndPort(String ipAddress, Integer port) {
        return nodeSystemMapper.selectByIpAndPort(ipAddress, port);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenNodeSystem createOrUpdateNode(OnlineNodeInfo nodeInfo) {
        String nodeId = generateNodeId(nodeInfo.getIpAddress(), nodeInfo.getPort());

        MonitorSysGenNodeSystem existingNode = getById(nodeId);
        LocalDateTime now = LocalDateTime.now();

        if (existingNode == null) {
            // 创建新节点
            MonitorSysGenNodeSystem newNode = new MonitorSysGenNodeSystem();
            newNode.setNodeSystemId(nodeId);
            newNode.setNodeSystemName(nodeInfo.getNodeName());
            newNode.setNodeSystemApplicationName(nodeInfo.getApplicationName());
            newNode.setNodeSystemIpAddress(nodeInfo.getIpAddress());
            newNode.setNodeSystemPort(nodeInfo.getPort());
            newNode.setNodeSystemStatus(nodeInfo.getStatus().name());
            newNode.setNodeSystemType(nodeInfo.getNodeType());
            newNode.setNodeSystemVersion(nodeInfo.getVersion());
            newNode.setNodeSystemHealthy(nodeInfo.getHealthy());
            newNode.setNodeSystemFirstDiscoveredTime(now);
            newNode.setNodeSystemLastOnlineTime(now);
            newNode.setNodeSystemLastHeartbeatTime(now);
            newNode.setNodeSystemConnectionCount(1);
            newNode.setCreateTime(now);
            newNode.setUpdateTime(now);

            // 设置性能指标
            if (nodeInfo.getCpuUsage() != null) {
                newNode.setNodeSystemAvgCpuUsage(nodeInfo.getCpuUsage());
            }
            if (nodeInfo.getMemoryUsage() != null) {
                newNode.setNodeSystemAvgMemoryUsage(nodeInfo.getMemoryUsage());
            }
            if (nodeInfo.getDiskUsage() != null) {
                newNode.setNodeSystemAvgDiskUsage(nodeInfo.getDiskUsage());
            }
            if (nodeInfo.getNetworkLatency() != null) {
                newNode.setNodeSystemAvgNetworkLatency(nodeInfo.getNetworkLatency().doubleValue());
            }
            if (nodeInfo.getResponseTime() != null) {
                newNode.setNodeSystemAvgResponseTime(nodeInfo.getResponseTime().doubleValue());
            }

            save(newNode);
            log.info("创建新节点: {}", nodeId);
            return newNode;
        } else {
            // 更新现有节点
            existingNode.setNodeSystemName(nodeInfo.getNodeName());
            existingNode.setNodeSystemApplicationName(nodeInfo.getApplicationName());
            existingNode.setNodeSystemStatus(nodeInfo.getStatus().name());
            existingNode.setNodeSystemType(nodeInfo.getNodeType());
            existingNode.setNodeSystemVersion(nodeInfo.getVersion());
            existingNode.setNodeSystemHealthy(nodeInfo.getHealthy());
            existingNode.setNodeSystemLastHeartbeatTime(now);
            existingNode.setUpdateTime(now);

            // 如果节点重新上线，更新上线时间
            if ("ONLINE".equals(nodeInfo.getStatus().name())) {
                existingNode.setNodeSystemLastOnlineTime(now);
                // 增加连接次数
                existingNode.setNodeSystemConnectionCount((existingNode.getNodeSystemConnectionCount() != null
                        ? existingNode.getNodeSystemConnectionCount()
                        : 0) + 1);
            }

            // 更新性能指标（使用移动平均）
            updatePerformanceMetrics(existingNode, nodeInfo);

            updateById(existingNode);
            log.debug("更新节点: {}", nodeId);
            return existingNode;
        }
    }

    /**
     * 更新性能指标（使用移动平均算法）
     */
    private void updatePerformanceMetrics(MonitorSysGenNodeSystem existingNode, OnlineNodeInfo nodeInfo) {
        double alpha = 0.3; // 平滑因子，新值权重

        if (nodeInfo.getCpuUsage() != null) {
            double currentAvg = existingNode.getNodeSystemAvgCpuUsage() != null
                    ? existingNode.getNodeSystemAvgCpuUsage()
                    : 0.0;
            existingNode.setNodeSystemAvgCpuUsage(alpha * nodeInfo.getCpuUsage() + (1 - alpha) * currentAvg);
        }

        if (nodeInfo.getMemoryUsage() != null) {
            double currentAvg = existingNode.getNodeSystemAvgMemoryUsage() != null
                    ? existingNode.getNodeSystemAvgMemoryUsage()
                    : 0.0;
            existingNode.setNodeSystemAvgMemoryUsage(alpha * nodeInfo.getMemoryUsage() + (1 - alpha) * currentAvg);
        }

        if (nodeInfo.getDiskUsage() != null) {
            double currentAvg = existingNode.getNodeSystemAvgDiskUsage() != null
                    ? existingNode.getNodeSystemAvgDiskUsage()
                    : 0.0;
            existingNode.setNodeSystemAvgDiskUsage(alpha * nodeInfo.getDiskUsage() + (1 - alpha) * currentAvg);
        }

        if (nodeInfo.getNetworkLatency() != null) {
            double currentAvg = existingNode.getNodeSystemAvgNetworkLatency() != null
                    ? existingNode.getNodeSystemAvgNetworkLatency()
                    : 0.0;
            existingNode.setNodeSystemAvgNetworkLatency(
                    alpha * nodeInfo.getNetworkLatency().doubleValue() + (1 - alpha) * currentAvg);
        }

        if (nodeInfo.getResponseTime() != null) {
            double currentAvg = existingNode.getNodeSystemAvgResponseTime() != null
                    ? existingNode.getNodeSystemAvgResponseTime()
                    : 0.0;
            existingNode.setNodeSystemAvgResponseTime(
                    alpha * nodeInfo.getResponseTime().doubleValue() + (1 - alpha) * currentAvg);
        }
    }

    @Override
    public void updateNodeStatus(String nodeId, String status, Boolean healthy) {
        nodeSystemMapper.updateNodeStatus(nodeId, status, healthy, LocalDateTime.now());
    }

    @Override
    public void updateNodeMetrics(String nodeId, Double cpuUsage, Double memoryUsage, Double diskUsage,
            Double networkLatency, Double responseTime) {
        nodeSystemMapper.updateNodeMetrics(nodeId, cpuUsage, memoryUsage, diskUsage, networkLatency, responseTime);
    }

    @Override
    public void incrementConnectionCount(String nodeId) {
        nodeSystemMapper.incrementConnectionCount(nodeId);
    }

    @Override
    public void incrementErrorCount(String nodeId, String errorMessage) {
        nodeSystemMapper.incrementErrorCount(nodeId, LocalDateTime.now(), errorMessage);
    }

    @Override
    public Map<String, Object> getNodeStatistics() {
        return nodeSystemMapper.getNodeStatistics();
    }

    @Override
    public Map<String, Integer> getNodeCountByApplication() {
        List<Map<String, Object>> results = nodeSystemMapper.getNodeCountByApplication();
        return results.stream().collect(Collectors.toMap(map -> (String) map.get("applicationName"),
                map -> ((Number) map.get("nodeCount")).intValue()));
    }

    @Override
    public Map<String, Integer> getNodeCountByStatus() {
        List<Map<String, Object>> results = nodeSystemMapper.getNodeCountByStatus();
        return results.stream().collect(
                Collectors.toMap(map -> (String) map.get("status"), map -> ((Number) map.get("nodeCount")).intValue()));
    }

    @Override
    public Map<String, Integer> getNodeCountByType() {
        List<Map<String, Object>> results = nodeSystemMapper.getNodeCountByType();
        return results.stream().collect(Collectors.toMap(map -> (String) map.get("nodeType"),
                map -> ((Number) map.get("nodeCount")).intValue()));
    }

    @Override
    public List<MonitorSysGenNodeSystem> getRecentActiveNodes(int limit) {
        return nodeSystemMapper.getRecentActiveNodes(limit);
    }

    @Override
    public List<MonitorSysGenNodeSystem> getFastestNodes(int limit) {
        return nodeSystemMapper.getFastestNodes(limit);
    }

    @Override
    public List<MonitorSysGenNodeSystem> getHighestLoadNodes(int limit) {
        return nodeSystemMapper.getHighestLoadNodes(limit);
    }

    @Override
    public List<MonitorSysGenNodeSystem> searchNodes(String keyword, String status, String applicationName) {
        return nodeSystemMapper.searchNodes(keyword, status, applicationName);
    }

    @Override
    public void batchUpdateOfflineStatus(List<String> nodeIds) {
        if (!nodeIds.isEmpty()) {
            nodeSystemMapper.batchUpdateOfflineStatus(nodeIds);
        }
    }

    @Override
    public void updateOnlineDuration(String nodeId, Long duration) {
        nodeSystemMapper.updateOnlineDuration(nodeId, duration);
    }

    @Override
    public String generateNodeId(String ipAddress, Integer port) {
        String input = ipAddress + ":" + port;
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupOfflineNodes(int timeoutMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

        List<MonitorSysGenNodeSystem> timeoutNodes = lambdaQuery()
                .eq(MonitorSysGenNodeSystem::getNodeSystemStatus, "ONLINE")
                .lt(MonitorSysGenNodeSystem::getNodeSystemLastHeartbeatTime, cutoffTime).list();

        if (!timeoutNodes.isEmpty()) {
            List<String> timeoutNodeIds = timeoutNodes.stream().map(MonitorSysGenNodeSystem::getNodeSystemId)
                    .collect(Collectors.toList());

            batchUpdateOfflineStatus(timeoutNodeIds);
            log.info("清理 {} 个超时离线节点", timeoutNodeIds.size());
        }
    }

    @Override
    public Map<String, Object> getNodeHistoryStats(String nodeId, int days) {
        log.info("获取节点历史统计数据: nodeId={}, days={}", nodeId, days);

        Map<String, Object> stats = new HashMap<>();
        stats.put("nodeId", nodeId);
        stats.put("days", days);

        try {
            // 获取节点基本信息
            MonitorSysGenNodeSystem nodeSystem = getById(nodeId);
            if (nodeSystem == null) {
                stats.put("error", "节点不存在");
                return stats;
            }

            // 基本统计信息
            stats.put("nodeName", nodeSystem.getNodeSystemName());
            stats.put("applicationName", nodeSystem.getNodeSystemApplicationName());
            stats.put("ipAddress", nodeSystem.getNodeSystemIpAddress());
            stats.put("port", nodeSystem.getNodeSystemPort());
            stats.put("status", nodeSystem.getNodeSystemStatus());
            stats.put("healthy", nodeSystem.getNodeSystemHealthy());

            // 时间统计
            stats.put("firstDiscoveredTime", nodeSystem.getNodeSystemFirstDiscoveredTime());
            stats.put("lastOnlineTime", nodeSystem.getNodeSystemLastOnlineTime());
            stats.put("lastOfflineTime", nodeSystem.getNodeSystemLastOfflineTime());
            stats.put("lastHeartbeatTime", nodeSystem.getNodeSystemLastHeartbeatTime());
            stats.put("totalOnlineDuration", nodeSystem.getNodeSystemTotalOnlineDuration());

            // 性能统计
            stats.put("avgCpuUsage", nodeSystem.getNodeSystemAvgCpuUsage());
            stats.put("avgMemoryUsage", nodeSystem.getNodeSystemAvgMemoryUsage());
            stats.put("avgDiskUsage", nodeSystem.getNodeSystemAvgDiskUsage());
            stats.put("avgNetworkLatency", nodeSystem.getNodeSystemAvgNetworkLatency());
            stats.put("avgResponseTime", nodeSystem.getNodeSystemAvgResponseTime());

            // 连接和错误统计
            stats.put("connectionCount", nodeSystem.getNodeSystemConnectionCount());
            stats.put("errorCount", nodeSystem.getNodeSystemErrorCount());
            stats.put("lastErrorTime", nodeSystem.getNodeSystemLastErrorTime());
            stats.put("lastErrorMessage", nodeSystem.getNodeSystemLastErrorMessage());

            // 计算在线率（基于总在线时长和首次发现时间）
            if (nodeSystem.getNodeSystemFirstDiscoveredTime() != null
                    && nodeSystem.getNodeSystemTotalOnlineDuration() != null) {
                long totalDuration = java.time.Duration
                        .between(nodeSystem.getNodeSystemFirstDiscoveredTime(), LocalDateTime.now()).getSeconds();
                if (totalDuration > 0) {
                    double onlineRate = (double) nodeSystem.getNodeSystemTotalOnlineDuration() / totalDuration * 100;
                    stats.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);
                }
            }

            log.debug("节点历史统计数据获取成功: nodeId={}", nodeId);

        } catch (Exception e) {
            log.error("获取节点历史统计数据失败: nodeId={}", nodeId, e);
            stats.put("error", "获取历史统计数据失败: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getNodesHealthOverview() {
        log.info("获取节点健康状况概览");

        try {
            Map<String, Object> overview = getNodeStatistics();

            // 计算健康率
            Number totalNodes = (Number) overview.get("totalNodes");
            Number healthyNodes = (Number) overview.get("healthyNodes");
            Number onlineNodes = (Number) overview.get("onlineNodes");

            if (totalNodes != null && totalNodes.intValue() > 0) {
                double healthRate = (healthyNodes != null ? healthyNodes.doubleValue() : 0.0) / totalNodes.doubleValue()
                        * 100;
                double onlineRate = (onlineNodes != null ? onlineNodes.doubleValue() : 0.0) / totalNodes.doubleValue()
                        * 100;

                overview.put("healthRate", Math.round(healthRate * 100.0) / 100.0);
                overview.put("onlineRate", Math.round(onlineRate * 100.0) / 100.0);
            } else {
                overview.put("healthRate", 0.0);
                overview.put("onlineRate", 0.0);
            }

            // 添加状态描述和建议
            if (healthyNodes != null && totalNodes != null && totalNodes.intValue() > 0) {
                double healthRate = healthyNodes.doubleValue() / totalNodes.doubleValue() * 100;
                double onlineRate = (onlineNodes != null ? onlineNodes.doubleValue() : 0.0) / totalNodes.doubleValue()
                        * 100;

                if (healthRate >= 90 && onlineRate >= 95) {
                    overview.put("healthStatus", "excellent");
                    overview.put("healthDescription", "系统运行状态优秀");
                    overview.put("statusColor", "#67C23A"); // 绿色
                    overview.put("suggestion", "系统运行良好，继续保持");
                } else if (healthRate >= 80 && onlineRate >= 90) {
                    overview.put("healthStatus", "good");
                    overview.put("healthDescription", "系统运行状态良好");
                    overview.put("statusColor", "#409EFF"); // 蓝色
                    overview.put("suggestion", "系统运行正常，建议定期检查");
                } else if (healthRate >= 60 && onlineRate >= 70) {
                    overview.put("healthStatus", "warning");
                    overview.put("healthDescription", "系统运行状态一般，需要关注");
                    overview.put("statusColor", "#E6A23C"); // 橙色
                    overview.put("suggestion", "建议检查异常节点，优化系统配置");
                } else {
                    overview.put("healthStatus", "critical");
                    overview.put("healthDescription", "系统运行状态较差，需要立即处理");
                    overview.put("statusColor", "#F56C6C"); // 红色
                    overview.put("suggestion", "紧急处理离线和异常节点，检查网络连接");
                }
            } else {
                overview.put("healthStatus", "unknown");
                overview.put("healthDescription", "暂无节点数据");
                overview.put("statusColor", "#909399"); // 灰色
                overview.put("suggestion", "请检查节点发现服务是否正常");
            }

            // 添加时间戳
            overview.put("timestamp", LocalDateTime.now());
            overview.put("updateTime", System.currentTimeMillis());

            log.debug("节点健康状况概览获取成功");
            return overview;

        } catch (Exception e) {
            log.error("获取节点健康状况概览失败", e);
            Map<String, Object> errorOverview = new HashMap<>();
            errorOverview.put("error", "获取健康状况概览失败: " + e.getMessage());
            errorOverview.put("healthStatus", "error");
            errorOverview.put("healthDescription", "系统异常，无法获取健康状况");
            errorOverview.put("statusColor", "#F56C6C");
            errorOverview.put("timestamp", LocalDateTime.now());
            return errorOverview;
        }
    }
}
