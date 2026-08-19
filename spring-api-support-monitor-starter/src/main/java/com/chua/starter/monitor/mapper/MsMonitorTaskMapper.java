package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsMonitorTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集任务 Mapper。
 *
 * @since 2026-08-11
 * @author CH
 */
@Mapper
public interface MsMonitorTaskMapper extends BaseMapper<MsMonitorTask> {
}
