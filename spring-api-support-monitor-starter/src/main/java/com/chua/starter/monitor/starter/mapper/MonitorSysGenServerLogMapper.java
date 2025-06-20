package com.chua.starter.monitor.starter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

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
    @Select("<script>" +
            "SELECT * FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_log_status = 1 " +
            "<if test='serverId != null'>" +
            "AND monitor_sys_gen_server_id = #{serverId} " +
            "</if>" +
            "<if test='level != null and level != \"\"'>" +
            "AND monitor_sys_gen_server_log_level = #{level} " +
            "</if>" +
            "<if test='source != null and source != \"\"'>" +
            "AND monitor_sys_gen_server_log_source = #{source} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (monitor_sys_gen_server_log_content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR monitor_sys_gen_server_log_logger LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND monitor_sys_gen_server_log_create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND monitor_sys_gen_server_log_create_time <= #{endTime} " +
            "</if>" +
            "ORDER BY monitor_sys_gen_server_log_create_time DESC" +
            "</script>")
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
    @Select("SELECT * FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "ORDER BY monitor_sys_gen_server_log_create_time DESC")
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
    @Select("SELECT COUNT(*) FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_level = #{level} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1")
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
    @Select("SELECT " +
            "monitor_sys_gen_server_log_level as level, " +
            "COUNT(*) as count " +
            "FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "GROUP BY monitor_sys_gen_server_log_level")
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
    @Select("SELECT " +
            "monitor_sys_gen_server_log_source as source, " +
            "COUNT(*) as count " +
            "FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "GROUP BY monitor_sys_gen_server_log_source")
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
    @Select("SELECT * FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_level IN ('ERROR', 'FATAL') " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "ORDER BY monitor_sys_gen_server_log_create_time DESC " +
            "LIMIT #{limit}")
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
    @Select("SELECT * FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time > #{lastTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "ORDER BY monitor_sys_gen_server_log_create_time ASC " +
            "LIMIT #{limit}")
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
    @Delete("DELETE FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time < #{expiredTime}")
    int deleteExpiredLogs(@Param("serverId") Integer serverId,
                          @Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 批量删除过期日志
     *
     * @param expiredTime 过期时间
     * @return 删除数量
     */
    @Delete("DELETE FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_log_create_time < #{expiredTime}")
    int batchDeleteExpiredLogs(@Param("expiredTime") LocalDateTime expiredTime);

    /**
     * 获取日志大小统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志大小(字节)
     */
    @Select("SELECT " +
            "COALESCE(SUM(LENGTH(monitor_sys_gen_server_log_content)), 0) as totalSize " +
            "FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1")
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
    @Select("SELECT " +
            "DATE_FORMAT(monitor_sys_gen_server_log_create_time, '%Y-%m-%d %H:00:00') as hour, " +
            "COUNT(*) as count " +
            "FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "GROUP BY DATE_FORMAT(monitor_sys_gen_server_log_create_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour")
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
    @Select("SELECT * FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_content LIKE CONCAT('%', #{keyword}, '%') " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1 " +
            "ORDER BY monitor_sys_gen_server_log_create_time DESC")
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
    @Select("SELECT " +
            "COUNT(*) as exceptionCount, " +
            "COUNT(DISTINCT monitor_sys_gen_server_log_logger) as distinctLoggers " +
            "FROM monitor_sys_gen_server_logs " +
            "WHERE monitor_sys_gen_server_id = #{serverId} " +
            "AND monitor_sys_gen_server_log_exception_stack IS NOT NULL " +
            "AND monitor_sys_gen_server_log_exception_stack != '' " +
            "AND monitor_sys_gen_server_log_create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND monitor_sys_gen_server_log_status = 1")
    Map<String, Object> getExceptionStatistics(@Param("serverId") Integer serverId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
