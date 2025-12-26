package com.chua.starter.monitor.mapper.spider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSpiderData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 爬虫数据Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MonitorSpiderDataMapper extends BaseMapper<MonitorSpiderData> {

    /**
     * 统计任务的总数据量
     *
     * @param taskId 任务ID
     * @return 数据量
     */
    @Select("SELECT COUNT(*) FROM monitor_spider_data WHERE spider_task_id = #{taskId}")
    Long countByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计所有数据总量
     *
     * @return 总数据量
     */
    @Select("SELECT COUNT(*) FROM monitor_spider_data")
    Long countTotal();

    /**
     * 统计今日数据量
     *
     * @return 今日数据量
     */
    @Select("SELECT COUNT(*) FROM monitor_spider_data WHERE DATE(crawl_time) = CURDATE()")
    Long countToday();

    /**
     * 删除任务的所有数据
     *
     * @param taskId 任务ID
     * @return 删除数量
     */
    @Select("DELETE FROM monitor_spider_data WHERE spider_task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据哈希查找数据(去重)
     *
     * @param hash 数据哈希
     * @return 数据
     */
    @Select("SELECT * FROM monitor_spider_data WHERE spider_data_hash = #{hash} LIMIT 1")
    MonitorSpiderData selectByHash(@Param("hash") String hash);
}
