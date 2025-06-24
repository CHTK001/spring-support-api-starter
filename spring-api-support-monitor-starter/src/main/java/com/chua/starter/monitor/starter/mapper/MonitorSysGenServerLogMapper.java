package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器日志Mapper接口
 *
 * @author CH
 * @since 2024/12/20
 */
@Mapper
public interface MonitorSysGenServerLogMapper extends BaseMapper<MonitorSysGenServerLog> {

    /**
     * 分页查询服务器日志
     *
     * @param page     分页参数
     * @param serverId 服务器ID
     * @param level    日志级别
     * @param source   日志来源
     * @param keyword  关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    IPage<MonitorSysGenServerLog> selectLogPage(Page<MonitorSysGenServerLog> page,
                                                @Param("serverId") Integer serverId,
                                                @Param("level") String level,
                                                @Param("source") String source,
                                                @Param("keyword") String keyword,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 根据服务器ID和时间范围查询日志
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    List<MonitorSysGenServerLog> selectLogsByServerAndTime(@Param("serverId") Integer serverId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 根据日志级别查询日志数量
     *
     * @param serverId  服务器ID
     * @param level     日志级别
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志数量
     */
    Long countLogsByLevel(@Param("serverId") Integer serverId,
                          @Param("level") String level,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 获取日志级别统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> getLogLevelStatistics(@Param("serverId") Integer serverId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取日志来源统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    List<Map<String, Object>> getLogSourceStatistics(@Param("serverId") Integer serverId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取最近的错误日志
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 错误日志列表
     */
    List<MonitorSysGenServerLog> getRecentErrorLogs(@Param("serverId") Integer serverId,
                                                     @Param("limit") Integer limit);

    /**
     * 获取实时日志
     *
     * @param serverId 服务器ID
     * @param lastTime 最后时间
     * @param limit    限制数量
     * @return 实时日志列表
     */
    List<MonitorSysGenServerLog> getRealTimeLogs(@Param("serverId") Integer serverId,
                                                 @Param("lastTime") LocalDateTime lastTime,
                                                 @Param("limit") Integer limit);

    /**
     * 删除过期日志
     *
     * @param serverId    服务器ID
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    int deleteExpiredLogs(@Param("serverId") Integer serverId,
                          @Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 批量删除过期日志
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    int batchDeleteExpiredLogs(@Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 获取日志大小统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志大小(字节)
     */
    Long getLogSizeStatistics(@Param("serverId") Integer serverId,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取日志时间分布统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间分布统计
     */
    List<Map<String, Object>> getLogTimeDistribution(@Param("serverId") Integer serverId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 搜索日志内容
     *
     * @param page      分页参数
     * @param serverId  服务器ID
     * @param keyword   关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 搜索结果
     */
    IPage<MonitorSysGenServerLog> searchLogContent(Page<MonitorSysGenServerLog> page,
                                                   @Param("serverId") Integer serverId,
                                                   @Param("keyword") String keyword,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 获取异常日志统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 异常统计
     */
    Map<String, Object> getExceptionStatistics(@Param("serverId") Integer serverId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
