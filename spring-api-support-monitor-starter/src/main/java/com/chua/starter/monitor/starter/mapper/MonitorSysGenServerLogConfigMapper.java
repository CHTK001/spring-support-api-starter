package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 服务器日志配置Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenServerLogConfigMapper extends BaseMapper<MonitorSysGenServerLogConfig> {

    /**
     * 根据服务器ID获取日志配置
     *
     * @param serverId 服务器ID
     * @return 日志配置
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    MonitorSysGenServerLogConfig selectByServerId(@Param("serverId") Integer serverId);

    /**
     * 获取所有启用的日志配置
     *
     * @return 启用的日志配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_enabled = 1 " +
            "AND monitor_sys_gen_server_log_config_status = 1")
    List<MonitorSysGenServerLogConfig> selectEnabledConfigs();

    /**
     * 获取启用实时日志的配置
     *
     * @return 启用实时日志的配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_enabled = 1 " +
            "AND monitor_sys_gen_server_log_real_time_enabled = 1 " +
            "AND monitor_sys_gen_server_log_config_status = 1")
    List<MonitorSysGenServerLogConfig> selectRealTimeEnabledConfigs();

    /**
     * 根据保留天数获取配置
     *
     * @param retentionDays 保留天数
     * @return 配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_retention_days = #{retentionDays} " +
            "AND monitor_sys_gen_server_log_enabled = 1 " +
            "AND monitor_sys_gen_server_log_config_status = 1")
    List<MonitorSysGenServerLogConfig> selectByRetentionDays(@Param("retentionDays") Integer retentionDays);

    /**
     * 获取需要清理的配置
     *
     * @return 需要清理的配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_enabled = 1 " +
            "AND monitor_sys_gen_server_log_config_status = 1 " +
            "AND monitor_sys_gen_server_log_retention_days > 0")
    List<MonitorSysGenServerLogConfig> selectConfigsForCleanup();

    /**
     * 更新配置状态
     *
     * @param serverId 服务器ID
     * @param status   状态
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_server_log_config " +
            "SET monitor_sys_gen_server_log_config_status = #{status}, " +
            "monitor_sys_gen_server_log_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    int updateConfigStatus(@Param("serverId") Integer serverId, @Param("status") Integer status);

    /**
     * 更新日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_server_log_config " +
            "SET monitor_sys_gen_server_log_enabled = #{enabled}, " +
            "monitor_sys_gen_server_log_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    int updateLogEnabled(@Param("serverId") Integer serverId, @Param("enabled") Integer enabled);

    /**
     * 更新实时日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新行数
     */
    @Update("UPDATE monitor_sys_gen_server_log_config " +
            "SET monitor_sys_gen_server_log_real_time_enabled = #{enabled}, " +
            "monitor_sys_gen_server_log_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    int updateRealTimeEnabled(@Param("serverId") Integer serverId, @Param("enabled") Integer enabled);

    /**
     * 批量更新配置状态
     *
     * @param serverIds 服务器ID列表
     * @param status    状态
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE monitor_sys_gen_server_log_config " +
            "SET monitor_sys_gen_server_log_config_status = #{status}, " +
            "monitor_sys_gen_server_log_update_time = NOW() " +
            "WHERE monitor_sys_gen_server_id IN " +
            "<foreach collection='serverIds' item='serverId' open='(' separator=',' close=')'>" +
            "#{serverId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateConfigStatus(@Param("serverIds") List<Integer> serverIds, @Param("status") Integer status);

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalConfigs, " +
            "SUM(CASE WHEN monitor_sys_gen_server_log_enabled = 1 THEN 1 ELSE 0 END) as enabledConfigs, " +
            "SUM(CASE WHEN monitor_sys_gen_server_log_real_time_enabled = 1 THEN 1 ELSE 0 END) as realTimeConfigs, " +
            "AVG(monitor_sys_gen_server_log_retention_days) as avgRetentionDays, " +
            "AVG(monitor_sys_gen_server_log_collection_interval) as avgCollectionInterval " +
            "FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_config_status = 1")
    java.util.Map<String, Object> getConfigStatistics();

    /**
     * 根据收集间隔获取配置
     *
     * @param interval 收集间隔
     * @return 配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_collection_interval = #{interval} " +
            "AND monitor_sys_gen_server_log_enabled = 1 " +
            "AND monitor_sys_gen_server_log_config_status = 1")
    List<MonitorSysGenServerLogConfig> selectByCollectionInterval(@Param("interval") Integer interval);

    /**
     * 获取过期配置（长时间未更新）
     *
     * @param days 天数
     * @return 过期配置列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_update_time < DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND monitor_sys_gen_server_log_config_status = 1")
    List<MonitorSysGenServerLogConfig> selectExpiredConfigs(@Param("days") Integer days);

    /**
     * 检查服务器是否有日志配置
     *
     * @param serverId 服务器ID
     * @return 是否存在配置
     */
    @Select("SELECT COUNT(*) > 0 FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_id = #{serverId}")
    boolean existsByServerId(@Param("serverId") Integer serverId);

    /**
     * 获取默认配置模板
     *
     * @return 默认配置
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_config " +
            "WHERE monitor_sys_gen_server_log_config_status = 1 " +
            "ORDER BY monitor_sys_gen_server_log_create_time ASC " +
            "LIMIT 1")
    MonitorSysGenServerLogConfig selectDefaultTemplate();
}
