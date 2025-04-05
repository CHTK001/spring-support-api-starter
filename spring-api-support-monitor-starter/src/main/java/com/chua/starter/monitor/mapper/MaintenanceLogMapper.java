package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MaintenanceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维护日志数据访问接口
 *
 * @author CH
 */
@Mapper
public interface MaintenanceLogMapper extends BaseMapper<MaintenanceLog> {
    
} 