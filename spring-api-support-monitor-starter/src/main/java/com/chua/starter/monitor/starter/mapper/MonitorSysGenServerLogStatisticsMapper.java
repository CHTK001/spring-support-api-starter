package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    int aggregateHourlyToDaily(@Param("serverId") Integer serverId, @Param("date") LocalDate date);

    /**
     * 删除过期统计数据
     *
     * @param expiredDate 过期日期
     * @return 删除行数
     */
    int deleteExpiredStatistics(@Param("expiredDate") LocalDate expiredDate);

    /**
     * 获取服务器日志统计概览
     *
     * @param serverId  服务器ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计概览
     */
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
    List<Map<String, Object>> getLogVolumeTrend(@Param("serverId") Integer serverId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
