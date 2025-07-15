package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.starter.monitor.starter.entity.MonitorSysGenNodeSystem;
import com.chua.starter.monitor.starter.pojo.OnlineNodeInfo;

import java.util.List;
import java.util.Map;

/**
 * 节点系统服务接口
 *
 * @author CH
 * @since 2024/12/30
 */
public interface NodeSystemService extends IService<MonitorSysGenNodeSystem> {

    /**
     * 同步在线节点数据到数据库
     * 
     * @param onlineNodes 在线节点列表
     */
    void syncOnlineNodes(List<OnlineNodeInfo> onlineNodes);

    /**
     * 根据IP和端口查询节点
     * 
     * @param ipAddress IP地址
     * @param port 端口
     * @return 节点信息
     */
    MonitorSysGenNodeSystem getByIpAndPort(String ipAddress, Integer port);

    /**
     * 创建或更新节点
     * 
     * @param nodeInfo 节点信息
     * @return 节点系统信息
     */
    MonitorSysGenNodeSystem createOrUpdateNode(OnlineNodeInfo nodeInfo);

    /**
     * 更新节点状态
     * 
     * @param nodeId 节点ID
     * @param status 状态
     * @param healthy 是否健康
     */
    void updateNodeStatus(String nodeId, String status, Boolean healthy);

    /**
     * 更新节点性能指标
     * 
     * @param nodeId 节点ID
     * @param cpuUsage CPU使用率
     * @param memoryUsage 内存使用率
     * @param diskUsage 磁盘使用率
     * @param networkLatency 网络延迟
     * @param responseTime 响应时间
     */
    void updateNodeMetrics(String nodeId, Double cpuUsage, Double memoryUsage, 
                          Double diskUsage, Double networkLatency, Double responseTime);

    /**
     * 增加节点连接次数
     * 
     * @param nodeId 节点ID
     */
    void incrementConnectionCount(String nodeId);

    /**
     * 增加节点错误次数
     * 
     * @param nodeId 节点ID
     * @param errorMessage 错误信息
     */
    void incrementErrorCount(String nodeId, String errorMessage);

    /**
     * 获取节点统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getNodeStatistics();

    /**
     * 按应用名称分组统计节点数量
     * 
     * @return 应用分组统计
     */
    Map<String, Integer> getNodeCountByApplication();

    /**
     * 按状态分组统计节点数量
     * 
     * @return 状态分组统计
     */
    Map<String, Integer> getNodeCountByStatus();

    /**
     * 按节点类型分组统计节点数量
     * 
     * @return 类型分组统计
     */
    Map<String, Integer> getNodeCountByType();

    /**
     * 获取最近活跃的节点
     * 
     * @param limit 限制数量
     * @return 最近活跃节点列表
     */
    List<MonitorSysGenNodeSystem> getRecentActiveNodes(int limit);

    /**
     * 获取响应最快的节点
     * 
     * @param limit 限制数量
     * @return 响应最快节点列表
     */
    List<MonitorSysGenNodeSystem> getFastestNodes(int limit);

    /**
     * 获取负载最高的节点
     * 
     * @param limit 限制数量
     * @return 负载最高节点列表
     */
    List<MonitorSysGenNodeSystem> getHighestLoadNodes(int limit);

    /**
     * 搜索节点
     * 
     * @param keyword 关键词
     * @param status 状态
     * @param applicationName 应用名称
     * @return 搜索结果
     */
    List<MonitorSysGenNodeSystem> searchNodes(String keyword, String status, String applicationName);

    /**
     * 批量更新节点状态为离线
     * 
     * @param nodeIds 节点ID列表
     */
    void batchUpdateOfflineStatus(List<String> nodeIds);

    /**
     * 更新节点在线时长
     * 
     * @param nodeId 节点ID
     * @param duration 时长（秒）
     */
    void updateOnlineDuration(String nodeId, Long duration);

    /**
     * 生成节点ID（MD5(IP+PORT)）
     * 
     * @param ipAddress IP地址
     * @param port 端口
     * @return 节点ID
     */
    String generateNodeId(String ipAddress, Integer port);

    /**
     * 清理离线节点（将长时间未心跳的节点标记为离线）
     * 
     * @param timeoutMinutes 超时时间（分钟）
     */
    void cleanupOfflineNodes(int timeoutMinutes);

    /**
     * 获取节点历史统计数据
     * 
     * @param nodeId 节点ID
     * @param days 天数
     * @return 历史统计数据
     */
    Map<String, Object> getNodeHistoryStats(String nodeId, int days);

    /**
     * 获取所有节点的健康状况概览
     * 
     * @return 健康状况概览
     */
    Map<String, Object> getNodesHealthOverview();
}
