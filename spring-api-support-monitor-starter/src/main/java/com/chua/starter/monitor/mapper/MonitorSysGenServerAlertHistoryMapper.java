package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警历史 Mapper 接口
 *
 * @author CH
 * @since 2024/12/27
 */
@Mapper
public interface MonitorSysGenServerAlertHistoryMapper extends BaseMapper<MonitorSysGenServerAlertHistory> {

    /**
     * 批量恢复告警
     *
     * @param alertIds 告警ID列表
     * @return 更新的记录数
     */
    int batchRecoverAlerts(@Param("alertIds") List<Integer> alertIds);

    /**
     * 批量忽略告警
     *
     * @param alertIds 告警ID列表
     * @return 更新的记录数
     */
    int batchIgnoreAlerts(@Param("alertIds") List<Integer> alertIds);

    /**
     * 清理过期告警记录
     *
     * @param expireTime 过期时间
     * @return 清理的记录数
     */
    int cleanupExpiredAlerts(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取未发送通知的告警
     *
     * @param limit 限制数量
     * @return 未发送通知的告警列表
     */
    List<MonitorSysGenServerAlertHistory> getUnsentNotificationAlerts(@Param("limit") Integer limit);

    /**
     * 自动恢复过期的活跃告警
     *
     * @param expireTime 过期时间
     * @return 恢复的告警数量
     */
    int autoRecoverExpiredAlerts(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据指纹和时间范围查询告警
     *
     * @param fingerprint 告警指纹
     * @param startTime   开始时间
     * @return 告警列表
     */
    List<MonitorSysGenServerAlertHistory> getAlertsByFingerprintInTimeRange(
            @Param("fingerprint") String fingerprint,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 获取告警统计信息
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    List<Object> getAlertStatistics(@Param("serverId") Integer serverId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}
