package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsMonitorConfig;
import org.apache.ibatis.annotations.Mapper;

/**
* 全局配置 映射器。
*
* @since 2026-08-11
* @author CH
 */
@Mapper
public interface MsMonitorConfigMapper extends BaseMapper<MsMonitorConfig> {
}
