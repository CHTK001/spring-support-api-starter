package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.SoftServiceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 软件服务日志Mapper
 *
 * @author CH
 * @since 2025/6/5
 */
@Mapper
public interface SoftServiceLogMapper extends BaseMapper<SoftServiceLog> {
}