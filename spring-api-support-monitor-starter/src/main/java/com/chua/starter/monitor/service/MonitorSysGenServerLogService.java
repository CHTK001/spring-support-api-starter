package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 服务器日志管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerLogService extends IService<MonitorSysGenServerLog> {

    /**
     * 分页查询服务器日志
     *
     * @param page      分页参数
     * @param serverId  服务器ID
     * @param level     日志级别
     * @param source    日志来源
     * @param keyword   关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    ReturnResult<IPage<MonitorSysGenServerLog>> getLogPage(Page<MonitorSysGenServerLog> page, Integer serverId,
            String level, String source, String keyword, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据服务器ID和时间范围查询日志
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志列表
     */
    ReturnResult<List<MonitorSysGenServerLog>> getLogsByServerAndTime(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取最近的错误日志
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 错误日志列表
     */
    ReturnResult<List<MonitorSysGenServerLog>> getRecentErrorLogs(Integer serverId, Integer limit);

    /**
     * 获取实时日志
     *
     * @param serverId 服务器ID
     * @param lastTime 最后时间
     * @param limit    限制数量
     * @return 实时日志列表
     */
    ReturnResult<List<MonitorSysGenServerLog>> getRealTimeLogs(Integer serverId, LocalDateTime lastTime, Integer limit);

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
    ReturnResult<IPage<MonitorSysGenServerLog>> searchLogContent(Page<MonitorSysGenServerLog> page, Integer serverId,
            String keyword, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量保存日志
     *
     * @param logs 日志列表
     * @return 保存结果
     */
    ReturnResult<Boolean> batchSaveLogs(List<MonitorSysGenServerLog> logs);

    /**
     * 删除过期日志
     *
     * @param serverId    服务器ID
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    ReturnResult<Integer> deleteExpiredLogs(Integer serverId, LocalDateTime expiredTime);

    /**
     * 批量删除过期日志
     *
     * @param expiredTime 过期时间
     * @return 删除结果
     */
    ReturnResult<Integer> batchDeleteExpiredLogs(LocalDateTime expiredTime);

    /**
     * 获取日志级别统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    ReturnResult<Map<String, Object>> getLogLevelStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取日志来源统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    ReturnResult<Map<String, Object>> getLogSourceStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取日志时间分布统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间分布统计
     */
    ReturnResult<List<Map<String, Object>>> getLogTimeDistribution(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取异常日志统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 异常统计
     */
    ReturnResult<Map<String, Object>> getExceptionStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 获取日志大小统计
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志大小统计
     */
    ReturnResult<Map<String, Object>> getLogSizeStatistics(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 生成日志报告
     *
     * @param serverId  服务器ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日志报告
     */
    ReturnResult<Map<String, Object>> generateLogReport(Integer serverId, LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 导出日志
     *
     * @param serverId  服务器ID
     * @param level     日志级别
     * @param source    日志来源
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param format    导出格式
     * @return 导出结果
     */
    ReturnResult<String> exportLogs(Integer serverId, String level, String source, LocalDateTime startTime,
            LocalDateTime endTime, String format);

    /**
     * 清理日志数据
     *
     * @param serverId 服务器ID
     * @return 清理结果
     */
    ReturnResult<Map<String, Object>> cleanupLogs(Integer serverId);

    /**
     * 获取日志收集状态
     *
     * @param serverId 服务器ID
     * @return 收集状态
     */
    ReturnResult<Map<String, Object>> getLogCollectionStatus(Integer serverId);

    /**
     * 启动日志收集
     *
     * @param serverId 服务器ID
     * @return 启动结果
     */
    ReturnResult<Boolean> startLogCollection(Integer serverId);

    /**
     * 停止日志收集
     *
     * @param serverId 服务器ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopLogCollection(Integer serverId);

    /**
     * 重启日志收集
     *
     * @param serverId 服务器ID
     * @return 重启结果
     */
    ReturnResult<Boolean> restartLogCollection(Integer serverId);

    /**
     * 获取服务器日志统计信息（简化版本）
     *
     * @param serverId 服务器ID
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getServerLogStatistics(Integer serverId);

    /**
     * 清理服务器日志
     *
     * @param serverId   服务器ID
     * @param beforeDate 清理此日期之前的日志
     * @return 清理的记录数
     */
    ReturnResult<Integer> cleanupServerLogs(Integer serverId, LocalDateTime beforeDate);
}
