package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 服务器日志统计Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenServerLogStatisticsMapper extends BaseMapper<MonitorSysGenServerLogStatistics> {

    /**
     * 根据服务器ID和日期获取统计
     *
     * @param serverId 服务器ID
     * @param date     日期
     * @return 统计列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date = #{date} " +
            "ORDER BY monitor_sys_gen_server_log_stat_hour")
    List<MonitorSysGenServerLogStatistics> selectByServerAndDate(@Param("serverId") Integer serverId,
                                                                 @Param("date") LocalDate date);

    /**
     * 根据服务器ID和日期范围获取统计
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY monitor_sys_gen_server_log_stat_date, monitor_sys_gen_server_log_stat_hour")
    List<MonitorSysGenServerLogStatistics> selectByServerAndDateRange(@Param("serverId") Integer serverId,
                                                                      @Param("startDate") LocalDate startDate,
                                                                      @Param("endDate") LocalDate endDate);

    /**
     * 获取日级统计
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日级统计列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NULL " +
            "ORDER BY monitor_sys_gen_server_log_stat_date")
    List<MonitorSysGenServerLogStatistics> selectDailyStatistics(@Param("serverId") Integer serverId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);

    /**
     * 获取小时级统计
     *
     * @param serverId 服务器ID
     * @param date     日期
     * @return 小时级统计列表
     */
    @Select("SELECT * FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date = #{date} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NOT NULL " +
            "ORDER BY monitor_sys_gen_server_log_stat_hour")
    List<MonitorSysGenServerLogStatistics> selectHourlyStatistics(@Param("serverId") Integer serverId,
                                                                  @Param("date") LocalDate date);

    /**
     * 插入或更新统计数据
     *
     * @param serverId    服务器ID
     * @param date        日期
     * @param hour        小时
     * @param totalCount  总数量
     * @param debugCount  DEBUG数量
     * @param infoCount   INFO数量
     * @param warnCount   WARN数量
     * @param errorCount  ERROR数量
     * @param fatalCount  FATAL数量
     * @param sizeBytes   大小
     * @return 影响行数
     */
    @Insert("INSERT INTO monitor_sys_gen_server_log_statistics (" +
            "monitor_sys_gen_server_id, monitor_sys_gen_server_log_stat_date, monitor_sys_gen_server_log_stat_hour, " +
            "monitor_sys_gen_server_log_total_count, monitor_sys_gen_server_log_debug_count, " +
            "monitor_sys_gen_server_log_info_count, monitor_sys_gen_server_log_warn_count, " +
            "monitor_sys_gen_server_log_error_count, monitor_sys_gen_server_log_fatal_count, " +
            "monitor_sys_gen_server_log_size_bytes, monitor_sys_gen_server_log_stat_create_time" +
            ") VALUES (" +
            "#{serverId}, #{date}, #{hour}, #{totalCount}, #{debugCount}, #{infoCount}, " +
            "#{warnCount}, #{errorCount}, #{fatalCount}, #{sizeBytes}, NOW()" +
            ") ON DUPLICATE KEY UPDATE " +
            "monitor_sys_gen_server_log_total_count = monitor_sys_gen_server_log_total_count + #{totalCount}, " +
            "monitor_sys_gen_server_log_debug_count = monitor_sys_gen_server_log_debug_count + #{debugCount}, " +
            "monitor_sys_gen_server_log_info_count = monitor_sys_gen_server_log_info_count + #{infoCount}, " +
            "monitor_sys_gen_server_log_warn_count = monitor_sys_gen_server_log_warn_count + #{warnCount}, " +
            "monitor_sys_gen_server_log_error_count = monitor_sys_gen_server_log_error_count + #{errorCount}, " +
            "monitor_sys_gen_server_log_fatal_count = monitor_sys_gen_server_log_fatal_count + #{fatalCount}, " +
            "monitor_sys_gen_server_log_size_bytes = monitor_sys_gen_server_log_size_bytes + #{sizeBytes}, " +
            "monitor_sys_gen_server_log_stat_update_time = NOW()")
    int insertOrUpdateStatistics(@Param("serverId") Integer serverId,
                                 @Param("date") LocalDate date,
                                 @Param("hour") Integer hour,
                                 @Param("totalCount") Long totalCount,
                                 @Param("debugCount") Long debugCount,
                                 @Param("infoCount") Long infoCount,
                                 @Param("warnCount") Long warnCount,
                                 @Param("errorCount") Long errorCount,
                                 @Param("fatalCount") Long fatalCount,
                                 @Param("sizeBytes") Long sizeBytes);

    /**
     * 聚合小时统计到日统计
     *
     * @param serverId 服务器ID
     * @param date     日期
     * @return 影响行数
     */
    @Insert("INSERT INTO monitor_sys_gen_server_log_statistics (" +
            "monitor_sys_gen_server_id, monitor_sys_gen_server_log_stat_date, " +
            "monitor_sys_gen_server_log_total_count, monitor_sys_gen_server_log_debug_count, " +
            "monitor_sys_gen_server_log_info_count, monitor_sys_gen_server_log_warn_count, " +
            "monitor_sys_gen_server_log_error_count, monitor_sys_gen_server_log_fatal_count, " +
            "monitor_sys_gen_server_log_size_bytes, monitor_sys_gen_server_log_stat_create_time" +
            ") " +
            "SELECT " +
            "monitor_sys_gen_server_id, monitor_sys_gen_server_log_stat_date, " +
            "SUM(monitor_sys_gen_server_log_total_count), SUM(monitor_sys_gen_server_log_debug_count), " +
            "SUM(monitor_sys_gen_server_log_info_count), SUM(monitor_sys_gen_server_log_warn_count), " +
            "SUM(monitor_sys_gen_server_log_error_count), SUM(monitor_sys_gen_server_log_fatal_count), " +
            "SUM(monitor_sys_gen_server_log_size_bytes), NOW() " +
            "FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date = #{date} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NOT NULL " +
            "GROUP BY monitor_sys_gen_server_id, monitor_sys_gen_server_log_stat_date " +
            "ON DUPLICATE KEY UPDATE " +
            "monitor_sys_gen_server_log_total_count = VALUES(monitor_sys_gen_server_log_total_count), " +
            "monitor_sys_gen_server_log_debug_count = VALUES(monitor_sys_gen_server_log_debug_count), " +
            "monitor_sys_gen_server_log_info_count = VALUES(monitor_sys_gen_server_log_info_count), " +
            "monitor_sys_gen_server_log_warn_count = VALUES(monitor_sys_gen_server_log_warn_count), " +
            "monitor_sys_gen_server_log_error_count = VALUES(monitor_sys_gen_server_log_error_count), " +
            "monitor_sys_gen_server_log_fatal_count = VALUES(monitor_sys_gen_server_log_fatal_count), " +
            "monitor_sys_gen_server_log_size_bytes = VALUES(monitor_sys_gen_server_log_size_bytes), " +
            "monitor_sys_gen_server_log_stat_update_time = NOW()")
    int aggregateHourlyToDaily(@Param("serverId") Integer serverId, @Param("date") LocalDate date);

    /**
     * 删除过期统计数据
     *
     * @param expiredDate 过期日期
     * @return 删除行数
     */
    @Delete("DELETE FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_log_stat_date < #{expiredDate}")
    int deleteExpiredStatistics(@Param("expiredDate") LocalDate expiredDate);

    /**
     * 获取服务器日志统计概览
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计概览
     */
    @Select("SELECT " +
            "SUM(monitor_sys_gen_server_log_total_count) as totalLogs, " +
            "SUM(monitor_sys_gen_server_log_error_count + monitor_sys_gen_server_log_fatal_count) as errorLogs, " +
            "SUM(monitor_sys_gen_server_log_warn_count) as warnLogs, " +
            "SUM(monitor_sys_gen_server_log_size_bytes) as totalSize, " +
            "AVG(monitor_sys_gen_server_log_total_count) as avgDailyLogs " +
            "FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NULL")
    Map<String, Object> getLogOverview(@Param("serverId") Integer serverId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 获取所有服务器的统计概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计概览列表
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_id as serverId, " +
            "SUM(monitor_sys_gen_server_log_total_count) as totalLogs, " +
            "SUM(monitor_sys_gen_server_log_error_count + monitor_sys_gen_server_log_fatal_count) as errorLogs, " +
            "SUM(monitor_sys_gen_server_log_warn_count) as warnLogs, " +
            "SUM(monitor_sys_gen_server_log_size_bytes) as totalSize " +
            "FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NULL " +
            "GROUP BY monitor_sys_gen_server_id")
    List<Map<String, Object>> getAllServersOverview(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 获取错误日志趋势
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 错误日志趋势
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_log_stat_date as date, " +
            "SUM(monitor_sys_gen_server_log_error_count + monitor_sys_gen_server_log_fatal_count) as errorCount " +
            "FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NULL " +
            "GROUP BY monitor_sys_gen_server_log_stat_date " +
            "ORDER BY monitor_sys_gen_server_log_stat_date")
    List<Map<String, Object>> getErrorLogTrend(@Param("serverId") Integer serverId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 获取日志量趋势
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日志量趋势
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_log_stat_date as date, " +
            "SUM(monitor_sys_gen_server_log_total_count) as totalCount " +
            "FROM monitor_sys_gen_server_log_statistics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_stat_date BETWEEN #{startDate} AND #{endDate} " +
            "AND monitor_sys_gen_server_log_stat_hour IS NULL " +
            "GROUP BY monitor_sys_gen_server_log_stat_date " +
            "ORDER BY monitor_sys_gen_server_log_stat_date")
    List<Map<String, Object>> getLogVolumeTrend(@Param("serverId") Integer serverId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
