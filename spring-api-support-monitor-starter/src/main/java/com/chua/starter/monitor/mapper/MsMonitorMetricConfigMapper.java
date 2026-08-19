package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsMonitorMetricConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标解析规则 Mapper。
 *
 * @since 2026-08-11
 * @author CH
 */
@Mapper
public interface MsMonitorMetricConfigMapper extends BaseMapper<MsMonitorMetricConfig> {
}
