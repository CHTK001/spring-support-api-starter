package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 服务器配置Mapper接口
 *
 * @author CH
 * @since 2024/12/18
 */
@Mapper
public interface MonitorSysGenServerMapper extends BaseMapper<MonitorSysGenServer> {

    /**
     * 根据协议类型统计服务器数量
     *
     * @return 协议统计
     */
    @Select("SELECT monitor_sys_gen_server_protocol as protocol, COUNT(*) as count " +
            "FROM monitor_sys_gen_server " +
            "GROUP BY monitor_sys_gen_server_protocol")
    List<Map<String, Object>> countByProtocol();

    /**
     * 根据状态统计服务器数量
     *
     * @return 状态统计
     */
    @Select("SELECT monitor_sys_gen_server_status as status, COUNT(*) as count " +
            "FROM monitor_sys_gen_server " +
            "GROUP BY monitor_sys_gen_server_status")
    List<Map<String, Object>> countByStatus();

    /**
     * 根据标签统计服务器数量
     *
     * @return 标签统计
     */
    @Select("SELECT COALESCE(monitor_sys_gen_server_tags, '未分组') as tags, COUNT(*) as count " +
            "FROM monitor_sys_gen_server " +
            "GROUP BY monitor_sys_gen_server_tags")
    List<Map<String, Object>> countByTags();

    /**
     * 获取启用监控的服务器列表
     *
     * @return 服务器列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server " +
            "WHERE monitor_sys_gen_server_monitor_enabled = 1 " +
            "AND monitor_sys_gen_server_status = 1")
    List<MonitorSysGenServer> selectMonitoringEnabledServers();

    /**
     * 根据生成器ID获取服务器列表
     *
     * @param genId 生成器ID
     * @return 服务器列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server WHERE gen_id = #{genId}")
    List<MonitorSysGenServer> selectByGenId(@Param("genId") Integer genId);

    /**
     * 根据协议类型获取服务器列表
     *
     * @param protocol 协议类型
     * @return 服务器列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server WHERE monitor_sys_gen_server_protocol = #{protocol}")
    List<MonitorSysGenServer> selectByProtocol(@Param("protocol") String protocol);

    /**
     * 根据标签获取服务器列表
     *
     * @param tags 标签
     * @return 服务器列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server WHERE monitor_sys_gen_server_tags LIKE CONCAT('%', #{tags}, '%')")
    List<MonitorSysGenServer> selectByTags(@Param("tags") String tags);

    /**
     * 获取服务器概览统计
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_servers, " +
            "SUM(CASE WHEN monitor_sys_gen_server_status = 1 THEN 1 ELSE 0 END) as enabled_servers, " +
            "SUM(CASE WHEN monitor_sys_gen_server_monitor_enabled = 1 THEN 1 ELSE 0 END) as monitoring_enabled, " +
            "COUNT(DISTINCT monitor_sys_gen_server_protocol) as protocol_types, " +
            "COUNT(DISTINCT monitor_sys_gen_server_tags) as tag_groups " +
            "FROM monitor_sys_gen_server")
    Map<String, Object> getServerOverview();
}
