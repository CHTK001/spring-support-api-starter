package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSpiderConnection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫连接Mapper
 *
 * @author CH
 * @since 2024/12/28
 */
@Mapper
public interface MonitorSpiderConnectionMapper extends BaseMapper<MonitorSpiderConnection> {
}
