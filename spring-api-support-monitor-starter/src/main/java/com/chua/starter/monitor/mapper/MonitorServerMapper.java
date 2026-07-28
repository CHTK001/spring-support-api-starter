package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 被监控服务器主机 Mapper。
 *
 * @author CH
 * @since 2026-07-28
 */
@Mapper
public interface MonitorServerMapper extends BaseMapper<MonitorServer> {
}
