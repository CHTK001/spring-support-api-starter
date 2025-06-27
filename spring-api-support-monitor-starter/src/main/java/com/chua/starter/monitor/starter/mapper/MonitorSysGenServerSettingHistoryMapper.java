package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSettingHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器配置变更历史Mapper接口
 *
 * @author CH
 * @since 2024/12/27
 */
@Mapper
public interface MonitorSysGenServerSettingHistoryMapper extends BaseMapper<MonitorSysGenServerSettingHistory> {

    /**
     * 根据服务器ID查询历史记录
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 历史记录列表
     */
    List<MonitorSysGenServerSettingHistory> selectByServerId(@Param("serverId") Integer serverId, @Param("limit") Integer limit);

    /**
     * 根据服务器ID和时间范围查询历史记录
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 历史记录列表
     */
    List<MonitorSysGenServerSettingHistory> selectByServerIdAndTimeRange(
            @Param("serverId") Integer serverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据变更类型查询历史记录
     *
     * @param changeType 变更类型
     * @param limit      限制数量
     * @return 历史记录列表
     */
    List<MonitorSysGenServerSettingHistory> selectByChangeType(@Param("changeType") String changeType, @Param("limit") Integer limit);

    /**
     * 获取历史记录统计信息
     *
     * @param serverId 服务器ID（可选）
     * @return 统计信息
     */
    Map<String, Object> getHistoryStatistics(@Param("serverId") Integer serverId);

    /**
     * 删除指定时间之前的历史记录
     *
     * @param beforeTime 时间点
     * @return 删除数量
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据服务器ID删除历史记录
     *
     * @param serverId 服务器ID
     * @return 删除数量
     */
    int deleteByServerId(@Param("serverId") Integer serverId);
}
