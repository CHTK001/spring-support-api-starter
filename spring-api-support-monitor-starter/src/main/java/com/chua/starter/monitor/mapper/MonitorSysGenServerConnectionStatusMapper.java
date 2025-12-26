package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerConnectionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器连接状态Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenServerConnectionStatusMapper extends BaseMapper<MonitorSysGenServer> {

    /**
     * 获取所有服务器连接状态
     *
     * @return 服务器连接状态列表
     */
    List<MonitorSysGenServerConnectionStatus> getAllServerConnectionStatus();

    /**
     * 根据服务器ID获取连接状态
     *
     * @param serverId 服务器ID
     * @return 服务器连接状态
     */
    MonitorSysGenServerConnectionStatus getServerConnectionStatus(@Param("serverId") Integer serverId);

    /**
     * 根据连接状态获取服务器列表
     *
     * @param connectionStatus 连接状态
     * @return 服务器连接状态列表
     */
    List<MonitorSysGenServerConnectionStatus> getServersByConnectionStatus(@Param("connectionStatus") Integer connectionStatus);

    /**
     * 更新服务器连接状态
     *
     * @param serverId         服务器ID
     * @param connectionStatus 连接状态
     * @param lastConnectTime  最后连接时间
     * @param connectionError  连接错误信息
     * @return 更新行数
     */
    int updateServerConnectionStatus(@Param("serverId") Integer serverId,
                                     @Param("connectionStatus") Integer connectionStatus,
                                     @Param("lastConnectTime") LocalDateTime lastConnectTime,
                                     @Param("connectionError") String connectionError);

    /**
     * 批量更新服务器连接状态
     *
     * @param serverIds        服务器ID列表
     * @param connectionStatus 连接状态
     * @param lastConnectTime  最后连接时间
     * @return 更新行数
     */
    int batchUpdateServerConnectionStatus(@Param("serverIds") List<Integer> serverIds,
                                          @Param("connectionStatus") Integer connectionStatus,
                                          @Param("lastConnectTime") LocalDateTime lastConnectTime);

    /**
     * 获取连接状态统计
     *
     * @return 连接状态统计
     */
    List<java.util.Map<String, Object>> getConnectionStatusStatistics();

    /**
     * 获取长时间未连接的服务器
     *
     * @param minutes 分钟数
     * @return 服务器连接状态列表
     */
    List<MonitorSysGenServerConnectionStatus> getLongTimeNoConnectServers(@Param("minutes") Integer minutes);

    /**
     * 重置服务器连接状态
     *
     * @param serverId 服务器ID
     * @param resetTime 重置时间
     * @return 更新行数
     */
    int resetServerConnectionStatus(@Param("serverId") Integer serverId, @Param("resetTime") LocalDateTime resetTime);

    /**
     * 批量重置服务器连接状态
     *
     * @param serverIds 服务器ID列表
     * @param resetTime 重置时间
     * @return 更新行数
     */
    int batchResetServerConnectionStatus(@Param("serverIds") List<Integer> serverIds, @Param("resetTime") LocalDateTime resetTime);

    /**
     * 获取服务器连接趋势数据
     *
     * @param serverId 服务器ID
     * @param hours 小时数
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getServerConnectionTrend(@Param("serverId") Integer serverId, @Param("hours") Integer hours);

    /**
     * 获取连接失败原因统计
     *
     * @param days 天数
     * @return 失败原因统计列表
     */
    List<Map<String, Object>> getConnectionFailureReasons(@Param("days") Integer days);

    /**
     * 获取连接状态历史记录
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 限制数
     * @return 历史记录列表
     */
    List<Map<String, Object>> getServerConnectionStatusHistory(@Param("serverId") Integer serverId,
                                                               @Param("startTime") String startTime,
                                                               @Param("endTime") String endTime,
                                                               @Param("offset") Integer offset,
                                                               @Param("limit") Integer limit);

    /**
     * 获取连接状态历史记录总数
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 总数
     */
    Long getServerConnectionStatusHistoryCount(@Param("serverId") Integer serverId,
                                               @Param("startTime") String startTime,
                                               @Param("endTime") String endTime);
}
