package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorArthasSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Arthas Session Mapper
 */
@Mapper
public interface MonitorArthasSessionMapper extends BaseMapper<MonitorArthasSession> {

    /**
     * 根据sessionId查询session信息
     */
    @Select("SELECT * FROM monitor_arthas_session WHERE session_id = #{sessionId} AND status = 'RUNNING'")
    MonitorArthasSession selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 更新session状态
     */
    @Update("UPDATE monitor_arthas_session SET status = #{status}, update_time = #{updateTime} WHERE session_id = #{sessionId}")
    int updateStatusBySessionId(@Param("sessionId") String sessionId,
                                @Param("status") String status,
                                @Param("updateTime") LocalDateTime updateTime);

    /**
     * 查询过期的session
     */
    @Select("SELECT * FROM monitor_arthas_session WHERE expire_time < #{now} AND status = 'RUNNING'")
    List<MonitorArthasSession> selectExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * 批量更新过期session状态
     */
    @Update("UPDATE monitor_arthas_session SET status = 'EXPIRED', update_time = #{updateTime} WHERE expire_time < #{now} AND status = 'RUNNING'")
    int updateExpiredSessions(@Param("now") LocalDateTime now, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 删除已完成或过期的session记录（清理历史数据）
     */
    @Update("DELETE FROM monitor_arthas_session WHERE status IN ('COMPLETED', 'INTERRUPTED', 'EXPIRED') AND update_time < #{beforeTime}")
    int deleteOldSessions(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据节点ID查询运行中的session
     */
    @Select("SELECT * FROM monitor_arthas_session WHERE node_id = #{nodeId} AND status = 'RUNNING' ORDER BY create_time DESC")
    List<MonitorArthasSession> selectRunningSessionsByNodeId(@Param("nodeId") String nodeId);

    /**
     * 统计各状态的session数量
     */
    @Select("SELECT status, COUNT(*) as count FROM monitor_arthas_session GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus();
}
