package com.chua.starter.monitor.mapper.spider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSpiderTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫任务Mapper接口
 *
 * @author CH
 */
@Mapper
public interface MonitorSpiderTaskMapper extends BaseMapper<MonitorSpiderTask> {
}
