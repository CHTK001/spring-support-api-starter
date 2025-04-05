package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维护任务Mapper
 *
 * @author CH
 */
@Mapper
public interface MaintenanceTaskMapper extends BaseMapper<MaintenanceTask> {
}