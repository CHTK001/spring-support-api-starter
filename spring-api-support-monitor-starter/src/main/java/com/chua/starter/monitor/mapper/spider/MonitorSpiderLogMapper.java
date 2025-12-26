package com.chua.starter.monitor.mapper.spider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSpiderLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 爬虫日志Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MonitorSpiderLogMapper extends BaseMapper<MonitorSpiderLog> {

    /**
     * 统计任务的日志总量
     *
     * @param taskId 任务ID
     * @return 日志量
     */
    @Select("SELECT COUNT(*) FROM monitor_spider_log WHERE spider_task_id = #{taskId}")
    Long countByTaskId(@Param("taskId") Long taskId);

    /**
     * 删除任务的所有日志
     *
     * @param taskId 任务ID
     * @return 删除数量
     */
    @Delete("DELETE FROM monitor_spider_log WHERE spider_task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") Long taskId);

    /**
     * 清理过期日志（保留最近N天）
     *
     * @param days 保留天数
     * @return 删除数量
     */
    @Delete("DELETE FROM monitor_spider_log WHERE spider_log_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteOldLogs(@Param("days") int days);
}
