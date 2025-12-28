package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSpiderLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫日志Mapper
 *
 * @author CH
 * @since 2024/12/28
 */
@Mapper
public interface MonitorSpiderLogMapper extends BaseMapper<MonitorSpiderLog> {
}
