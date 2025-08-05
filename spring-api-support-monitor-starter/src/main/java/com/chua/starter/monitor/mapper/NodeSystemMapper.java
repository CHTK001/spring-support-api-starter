package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.NodeSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 节点系统表 Mapper 接口
 *
 * @author CH
 * @since 2024/12/30
 */
@Mapper
public interface NodeSystemMapper extends BaseMapper<NodeSystem> {

    /**
     * 根据IP和端口查询节点
     */
    NodeSystem selectByIpAndPort(@Param("ipAddress") String ipAddress, @Param("port") Integer port);

    /**
     * 更新节点在线状态
     */
    int updateNodeStatus(@Param("nodeId") String nodeId,
                         @Param("status") String status,
                         @Param("healthy") Boolean healthy,
                         @Param("heartbeatTime") LocalDateTime heartbeatTime);

    /**
     * 更新节点性能指标
     */
    int updateNodeMetrics(@Param("nodeId") String nodeId,
                          @Param("cpuUsage") Double cpuUsage,
                          @Param("memoryUsage") Double memoryUsage,
                          @Param("diskUsage") Double diskUsage,
                          @Param("networkLatency") Double networkLatency,
                          @Param("responseTime") Double responseTime);

    /**
     * 增加连接次数
     */
    int incrementConnectionCount(@Param("nodeId") String nodeId);

    /**
     * 增加错误次数
     */
    int incrementErrorCount(@Param("nodeId") String nodeId,
                           @Param("errorTime") LocalDateTime errorTime,
                           @Param("errorMessage") String errorMessage);

    /**
     * 获取节点统计信息
     */
    Map<String, Object> getNodeStatistics();

    /**
     * 按应用名称分组统计节点数量
     */
    List<Map<String, Object>> getNodeCountByApplication();

    /**
     * 按状态分组统计节点数量
     */
    List<Map<String, Object>> getNodeCountByStatus();

    /**
     * 按节点类型分组统计节点数量
     */
    List<Map<String, Object>> getNodeCountByType();

    /**
     * 获取最近活跃的节点（按最后心跳时间排序）
     */
    List<NodeSystem> getRecentActiveNodes(@Param("limit") int limit);

    /**
     * 获取响应最快的节点
     */
    List<NodeSystem> getFastestNodes(@Param("limit") int limit);

    /**
     * 获取负载最高的节点
     */
    List<NodeSystem> getHighestLoadNodes(@Param("limit") int limit);

    /**
     * 搜索节点
     */
    List<NodeSystem> searchNodes(@Param("keyword") String keyword,
                                 @Param("status") String status,
                                 @Param("applicationName") String applicationName);

    /**
     * 批量更新节点状态为离线
     */
    int batchUpdateOfflineStatus(@Param("nodeIds") List<String> nodeIds);

    /**
     * 更新在线时长统计
     */
    int updateOnlineDuration(@Param("nodeId") String nodeId, @Param("duration") Long duration);
}
