package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerConnectionStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT " +
            "monitor_sys_gen_server_id, " +
            "monitor_sys_gen_server_name, " +
            "monitor_sys_gen_server_host, " +
            "monitor_sys_gen_server_port, " +
            "monitor_sys_gen_server_protocol, " +
            "monitor_sys_gen_server_connection_status, " +
            "monitor_sys_gen_server_last_connect_time, " +
            "monitor_sys_gen_server_connection_error, " +
            "monitor_sys_gen_server_monitor_enabled, " +
            "monitor_sys_gen_server_status " +
            "FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_status = 1")
    List<MonitorSysGenServerConnectionStatus> getAllServerConnectionStatus();

    /**
     * 根据服务器ID获取连接状态
     *
     * @param serverId 服务器ID
     * @return 服务器连接状态
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_id, " +
            "monitor_sys_gen_server_name, " +
            "monitor_sys_gen_server_host, " +
            "monitor_sys_gen_server_port, " +
            "monitor_sys_gen_server_protocol, " +
            "monitor_sys_gen_server_connection_status, " +
            "monitor_sys_gen_server_last_connect_time, " +
            "monitor_sys_gen_server_connection_error, " +
            "monitor_sys_gen_server_monitor_enabled, " +
            "monitor_sys_gen_server_status " +
            "FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    MonitorSysGenServerConnectionStatus getServerConnectionStatus(@Param("serverId") Integer serverId);

    /**
     * 根据连接状态获取服务器列表
     *
     * @param connectionStatus 连接状态
     * @return 服务器连接状态列表
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_id, " +
            "monitor_sys_gen_server_name, " +
            "monitor_sys_gen_server_host, " +
            "monitor_sys_gen_server_port, " +
            "monitor_sys_gen_server_protocol, " +
            "monitor_sys_gen_server_connection_status, " +
            "monitor_sys_gen_server_last_connect_time, " +
            "monitor_sys_gen_server_connection_error, " +
            "monitor_sys_gen_server_monitor_enabled, " +
            "monitor_sys_gen_server_status " +
            "FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_connection_status = #{connectionStatus} " +
            "AND monitor_sys_gen_server_status = 1")
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
    @Update("UPDATE monitor_sys_gen_server SET " +
            "monitor_sys_gen_server_connection_status = #{connectionStatus}, " +
            "monitor_sys_gen_server_last_connect_time = #{lastConnectTime}, " +
            "monitor_sys_gen_server_connection_error = #{connectionError}, " +
            "monitor_sys_gen_server_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
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
    @Update("<script>" +
            "UPDATE monitor_sys_gen_server SET " +
            "monitor_sys_gen_server_connection_status = #{connectionStatus}, " +
            "monitor_sys_gen_server_last_connect_time = #{lastConnectTime}, " +
            "monitor_sys_gen_server_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id IN " +
            "<foreach collection='serverIds' item='serverId' open='(' separator=',' close=')'>" +
            "#{serverId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateServerConnectionStatus(@Param("serverIds") List<Integer> serverIds,
                                          @Param("connectionStatus") Integer connectionStatus,
                                          @Param("lastConnectTime") LocalDateTime lastConnectTime);

    /**
     * 获取连接状态统计
     *
     * @return 连接状态统计
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_connection_status, " +
            "COUNT(*) as count " +
            "FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_status = 1 " +
            "GROUP BY monitor_sys_gen_server_connection_status")
    List<java.util.Map<String, Object>> getConnectionStatusStatistics();

    /**
     * 获取长时间未连接的服务器
     *
     * @param minutes 分钟数
     * @return 服务器连接状态列表
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_id, " +
            "monitor_sys_gen_server_name, " +
            "monitor_sys_gen_server_host, " +
            "monitor_sys_gen_server_port, " +
            "monitor_sys_gen_server_protocol, " +
            "monitor_sys_gen_server_connection_status, " +
            "monitor_sys_gen_server_last_connect_time, " +
            "monitor_sys_gen_server_connection_error, " +
            "monitor_sys_gen_server_monitor_enabled, " +
            "monitor_sys_gen_server_status " +
            "FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_status = 1 " +
            "AND (monitor_sys_gen_server_last_connect_time IS NULL " +
            "OR monitor_sys_gen_server_last_connect_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE))")
    List<MonitorSysGenServerConnectionStatus> getLongTimeNoConnectServers(@Param("minutes") Integer minutes);
}
