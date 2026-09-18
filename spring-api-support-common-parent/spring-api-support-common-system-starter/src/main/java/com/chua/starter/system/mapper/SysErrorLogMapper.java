package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysErrorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 前端异常上报日志 映射器
 *
 * @since 2026/8/8
 * @author CH
 */
@Mapper
public interface SysErrorLogMapper extends BaseMapper<SysErrorLog> {
}
