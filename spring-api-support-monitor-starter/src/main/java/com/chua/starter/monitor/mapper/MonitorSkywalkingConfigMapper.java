package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorSkywalkingConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * SkyWalking 配置 Mapper
 *
 * @author CH
 * @since 2024-12-18
 */
@Mapper
public interface MonitorSkywalkingConfigMapper extends BaseMapper<MonitorSkywalkingConfig> {
}
