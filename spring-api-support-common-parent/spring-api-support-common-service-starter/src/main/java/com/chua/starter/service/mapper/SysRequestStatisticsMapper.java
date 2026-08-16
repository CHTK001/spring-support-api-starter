package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysRequestStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请求统计 Mapper
 *
 * @author CH
 * @since 2026/08/03
 */
@Mapper
public interface SysRequestStatisticsMapper extends BaseMapper<SysRequestStatistics> {
}
