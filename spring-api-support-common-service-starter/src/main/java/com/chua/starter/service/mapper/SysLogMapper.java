package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志映射接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}