package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    MonitorSysGenServerMetrics selectLatestByServerId(@Param("serverId") Integer serverId);

    /**
     * 获取服务器指标历史数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据
     */
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
    int deleteExpiredMetrics(@Param("serverId") Integer serverId, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 批量删除过期指标数据
     *
     * @param expireTime 过期时间
     * @return 删除数量
     */
    int batchDeleteExpiredMetrics(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取服务器指标统计数据
     *
     * @param serverId 服务器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
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
    List<MonitorSysGenServerMetrics> getAnomalyData(@Param("serverId") Integer serverId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("cpuThreshold") Double cpuThreshold,
                                                    @Param("memoryThreshold") Double memoryThreshold,
                                                    @Param("diskThreshold") Double diskThreshold);

    /**
     * 优化指标数据表
     */
    void optimizeTable();
}
