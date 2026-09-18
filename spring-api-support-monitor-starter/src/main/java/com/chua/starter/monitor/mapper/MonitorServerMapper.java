package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 被监控服务器主机 映射器。
 *
 * @since 2026-07-28
 * @author CH
 */
@Mapper
public interface MonitorServerMapper extends BaseMapper<MonitorServer> {
}
