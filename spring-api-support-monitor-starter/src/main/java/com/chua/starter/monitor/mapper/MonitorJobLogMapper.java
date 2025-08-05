package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorJobLog;
import com.chua.starter.monitor.job.pojo.JobStatistic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Administrator
 */
@Mapper
public interface MonitorJobLogMapper extends BaseMapper<MonitorJobLog> {
    /**
     * 时间
     *
     * @param entity 实体
     * @return {@link List}<{@link JobStatistic}>
     */
    List<JobStatistic> time(@Param("query") MonitorJobLog entity);


    /**
     * 删除指定类型的数字
     *
     * @param number 要删除的数量
     */
    void deleteNumber(@Param("number")Integer number);
}