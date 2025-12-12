package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 脚本执行历史Mapper
 * 
 * @author CH
 * @since 2024/12/19
 */
@Mapper
public interface MonitorSysGenScriptExecutionMapper extends BaseMapper<MonitorSysGenScriptExecution> {

        /**
         * 根据脚本ID查找执行历史
         *
         * @param scriptId 脚本ID
         * @param limit    限制数量
         * @return 执行历史列表
         */
        List<MonitorSysGenScriptExecution> findByScriptId(@Param("scriptId") Integer scriptId,
                        @Param("limit") Integer limit);

        /**
         * 根据执行状态查找执行历史
         *
         * @param status 执行状态
         * @return 执行历史列表
         */
        List<MonitorSysGenScriptExecution> findByStatus(@Param("status") String status);

        /**
         * 查找正在运行的执行记录
         *
         * @return 正在运行的执行记录列表
         */
        List<MonitorSysGenScriptExecution> findRunningExecutions();

        /**
         * 根据进程ID查找执行记录
         *
         * @param processId 进程ID
         * @return 执行记录
         */
        MonitorSysGenScriptExecution findByProcessId(@Param("processId") Long processId);

        /**
         * 查找指定时间范围内的执行记录
         *
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 执行记录列表
         */
        List<MonitorSysGenScriptExecution> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 获取脚本执行统计信息
         *
         * @return 执行统计信息
         */
        ExecutionStatistics getExecutionStatistics();

        /**
         * 获取指定脚本的执行统计
         *
         * @param scriptId 脚本ID
         * @return 脚本执行统计信息
         */
        ScriptExecutionStatistics getScriptExecutionStatistics(@Param("scriptId") Integer scriptId);

        /**
         * 获取每日执行统计
         *
         * @param startDate 开始日期
         * @return 每日执行统计列表
         */
        List<DailyExecutionStatistics> getDailyExecutionStatistics(@Param("startDate") LocalDateTime startDate);

        /**
         * 清理过期的执行记录
         *
         * @param expireTime 过期时间
         * @return 清理的记录数
         */
        int cleanExpiredExecutions(@Param("expireTime") LocalDateTime expireTime);

        /**
         * 根据脚本ID清空执行记录
         *
         * @param scriptId 脚本ID（可选，为null时清空所有）
         * @return 删除的记录数
         */
        int clearByScriptId(@Param("scriptId") Integer scriptId);

        /**
         * 执行统计信息
         */
        interface ExecutionStatistics {
                Long getTotalExecutions();

                Long getSuccessCount();

                Long getFailedCount();

                Long getTimeoutCount();

                Long getRunningCount();

                Double getAvgDuration();
        }

        /**
         * 脚本执行统计
         */
        interface ScriptExecutionStatistics {
                Long getTotalExecutions();

                Long getSuccessCount();

                Long getFailedCount();

                Long getTimeoutCount();

                Double getAvgDuration();

                LocalDateTime getLastExecuteTime();
        }

        /**
         * 每日执行统计
         */
        interface DailyExecutionStatistics {
                String getExecuteDate();

                Long getTotalCount();

                Long getSuccessCount();

                Long getFailedCount();
        }
}
