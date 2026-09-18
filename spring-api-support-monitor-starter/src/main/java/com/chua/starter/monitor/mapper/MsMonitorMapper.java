package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsMonitor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 监控资产 映射器。
 *
 * @since 2026-08-11
 * @author CH
 */
@Mapper
public interface MsMonitorMapper extends BaseMapper<MsMonitor> {
}
