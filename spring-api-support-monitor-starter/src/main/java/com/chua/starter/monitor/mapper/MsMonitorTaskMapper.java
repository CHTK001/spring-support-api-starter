package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MsMonitorTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集任务 Mapper。
 *
 * @author CH
 * @since 2026-08-11
 */
@Mapper
public interface MsMonitorTaskMapper extends BaseMapper<MsMonitorTask> {
}
