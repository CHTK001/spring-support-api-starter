package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SystemServerSettingIPRateLimit;
import org.apache.ibatis.annotations.Mapper;

/**
 * IP 限流规则 Mapper
 */
@Mapper
public interface SystemServerSettingIPRateLimitMapper extends BaseMapper<SystemServerSettingIPRateLimit> {
}


