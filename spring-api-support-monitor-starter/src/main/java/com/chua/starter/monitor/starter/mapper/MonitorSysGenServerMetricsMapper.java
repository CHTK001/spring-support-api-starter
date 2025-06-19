package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器指标数据Mapper接口
 *
 * @author CH
 * @since 2024/12/18
 */
@Mapper
public interface MonitorSysGenServerMetricsMapper extends BaseMapper<MonitorSysGenServerMetrics> {

    /**
     * 获取服务器最新指标数据
     *
     * @param serverId 服务器ID
     * @return 最新指标数据
     */
    @Select("SELECT * FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "ORDER BY monitor_sys_gen_server_metrics_collect_time DESC " +
            "LIMIT 1")
    MonitorSysGenServerMetrics selectLatestByServerId(@Param("serverId") Integer serverId);

    /**
     * 获取服务器指标历史数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据
     */
    @Select("SELECT * FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY monitor_sys_gen_server_metrics_collect_time ASC")
    List<MonitorSysGenServerMetrics> selectHistoryByServerId(@Param("serverId") Integer serverId,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期指标数据
     *
     * @param serverId 服务器ID
     * @param expireTime 过期时间
     * @return 删除数量
     */
    @Delete("DELETE FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time < #{expireTime}")
    int deleteExpiredMetrics(@Param("serverId") Integer serverId, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 批量删除过期指标数据
     *
     * @param expireTime 过期时间
     * @return 删除数量
     */
    @Delete("DELETE FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_metrics_collect_time < #{expireTime}")
    int batchDeleteExpiredMetrics(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取服务器指标统计数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @Select("SELECT " +
            "AVG(monitor_sys_gen_server_metrics_cpu_usage) as avg_cpu_usage, " +
            "MAX(monitor_sys_gen_server_metrics_cpu_usage) as max_cpu_usage, " +
            "MIN(monitor_sys_gen_server_metrics_cpu_usage) as min_cpu_usage, " +
            "AVG(monitor_sys_gen_server_metrics_memory_usage) as avg_memory_usage, " +
            "MAX(monitor_sys_gen_server_metrics_memory_usage) as max_memory_usage, " +
            "MIN(monitor_sys_gen_server_metrics_memory_usage) as min_memory_usage, " +
            "AVG(monitor_sys_gen_server_metrics_disk_usage) as avg_disk_usage, " +
            "MAX(monitor_sys_gen_server_metrics_disk_usage) as max_disk_usage, " +
            "MIN(monitor_sys_gen_server_metrics_disk_usage) as min_disk_usage, " +
            "COUNT(*) as data_points " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getMetricsStatistics(@Param("serverId") Integer serverId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 获取CPU使用率图表数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔(分钟)
     * @return 图表数据
     */
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') as time_point, " +
            "AVG(monitor_sys_gen_server_metrics_cpu_usage) as cpu_usage " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY time_point ASC")
    List<Map<String, Object>> getCpuUsageChartData(@Param("serverId") Integer serverId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("interval") Integer interval);

    /**
     * 获取内存使用率图表数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔(分钟)
     * @return 图表数据
     */
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') as time_point, " +
            "AVG(monitor_sys_gen_server_metrics_memory_usage) as memory_usage " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY time_point ASC")
    List<Map<String, Object>> getMemoryUsageChartData(@Param("serverId") Integer serverId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("interval") Integer interval);

    /**
     * 获取磁盘使用率图表数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔(分钟)
     * @return 图表数据
     */
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') as time_point, " +
            "AVG(monitor_sys_gen_server_metrics_disk_usage) as disk_usage " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY time_point ASC")
    List<Map<String, Object>> getDiskUsageChartData(@Param("serverId") Integer serverId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("interval") Integer interval);

    /**
     * 获取网络流量图表数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔(分钟)
     * @return 图表数据
     */
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') as time_point, " +
            "AVG(monitor_sys_gen_server_metrics_network_in) as network_in, " +
            "AVG(monitor_sys_gen_server_metrics_network_out) as network_out " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY time_point ASC")
    List<Map<String, Object>> getNetworkChartData(@Param("serverId") Integer serverId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("interval") Integer interval);

    /**
     * 获取多服务器对比数据
     *
     * @param serverIds 服务器ID列表
     * @param metricType 指标类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 对比数据
     */
    @Select("<script>" +
            "SELECT " +
            "monitor_sys_gen_server_id as server_id, " +
            "DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') as time_point, " +
            "<choose>" +
            "<when test='metricType == \"cpu\"'>" +
            "AVG(monitor_sys_gen_server_metrics_cpu_usage) as metric_value " +
            "</when>" +
            "<when test='metricType == \"memory\"'>" +
            "AVG(monitor_sys_gen_server_metrics_memory_usage) as metric_value " +
            "</when>" +
            "<when test='metricType == \"disk\"'>" +
            "AVG(monitor_sys_gen_server_metrics_disk_usage) as metric_value " +
            "</when>" +
            "<otherwise>" +
            "AVG(monitor_sys_gen_server_metrics_cpu_usage) as metric_value " +
            "</otherwise>" +
            "</choose>" +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id IN " +
            "<foreach item='serverId' collection='serverIds' open='(' separator=',' close=')'>" +
            "#{serverId}" +
            "</foreach>" +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY monitor_sys_gen_server_id, DATE_FORMAT(monitor_sys_gen_server_metrics_collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY time_point ASC, server_id ASC" +
            "</script>")
    List<Map<String, Object>> getCompareChartData(@Param("serverIds") List<Integer> serverIds,
                                                  @Param("metricType") String metricType,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取服务器健康状态数据
     *
     * @param serverId 服务器ID
     * @return 健康状态数据
     */
    @Select("SELECT " +
            "monitor_sys_gen_server_metrics_cpu_usage as cpu_usage, " +
            "monitor_sys_gen_server_metrics_memory_usage as memory_usage, " +
            "monitor_sys_gen_server_metrics_disk_usage as disk_usage, " +
            "monitor_sys_gen_server_metrics_status as status, " +
            "monitor_sys_gen_server_metrics_response_time as response_time, " +
            "monitor_sys_gen_server_metrics_collect_time as collect_time " +
            "FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "ORDER BY monitor_sys_gen_server_metrics_collect_time DESC " +
            "LIMIT 1")
    Map<String, Object> getServerHealthData(@Param("serverId") Integer serverId);

    /**
     * 获取异常检测数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param cpuThreshold CPU阈值
     * @param memoryThreshold 内存阈值
     * @param diskThreshold 磁盘阈值
     * @return 异常数据
     */
    @Select("SELECT * FROM monitor_sys_gen_server_metrics " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_metrics_collect_time BETWEEN #{startTime} AND #{endTime} " +
            "AND (monitor_sys_gen_server_metrics_cpu_usage > #{cpuThreshold} " +
            "OR monitor_sys_gen_server_metrics_memory_usage > #{memoryThreshold} " +
            "OR monitor_sys_gen_server_metrics_disk_usage > #{diskThreshold}) " +
            "ORDER BY monitor_sys_gen_server_metrics_collect_time DESC")
    List<MonitorSysGenServerMetrics> getAnomalyData(@Param("serverId") Integer serverId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("cpuThreshold") Double cpuThreshold,
                                                    @Param("memoryThreshold") Double memoryThreshold,
                                                    @Param("diskThreshold") Double diskThreshold);
}
