package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerConnectionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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
}
