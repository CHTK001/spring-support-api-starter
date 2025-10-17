package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorArthasSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Arthas Session Mapper
 *
 * 作者: CH
 * 创建时间: 2025-10-17
 * 版本: 1.0.0
 */
@Mapper
public interface MonitorArthasSessionMapper extends BaseMapper<MonitorArthasSession> {

    /**
     * 根据sessionId查询session信息
     * 入参: sessionId 会话ID
     * 出参: MonitorArthasSession 会话信息
     */
    MonitorArthasSession selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 更新session状态
     * 入参: sessionId 会话ID, status 状态, updateTime 更新时间
     * 出参: int 影响行数
     */
    int updateStatusBySessionId(@Param("sessionId") String sessionId,
                                @Param("status") String status,
                                @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询过期的session
     * 入参: now 当前时间
     * 出参: List<MonitorArthasSession>
     */
    List<MonitorArthasSession> selectExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * 批量更新过期session状态
     * 入参: now 当前时间, updateTime 更新时间
     * 出参: int 影响行数
     */
    int updateExpiredSessions(@Param("now") LocalDateTime now, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 删除已完成或过期的session记录（清理历史数据）
     * 入参: beforeTime 时间阈值
     * 出参: int 影响行数
     */
    int deleteOldSessions(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据节点ID查询运行中的session
     * 入参: nodeId 节点ID
     * 出参: List<MonitorArthasSession>
     */
    List<MonitorArthasSession> selectRunningSessionsByNodeId(@Param("nodeId") String nodeId);

    /**
     * 统计各状态的session数量
     * 出参: List<Map<String, Object>> 状态统计
     */
    List<java.util.Map<String, Object>> countByStatus();
}
