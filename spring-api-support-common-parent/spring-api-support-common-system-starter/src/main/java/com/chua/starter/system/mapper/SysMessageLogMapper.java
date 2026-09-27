package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysMessageLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息推送日志 映射器接口
 *
 * @since 4.0.0.50
 * @author CH
 */
@Mapper
public interface SysMessageLogMapper extends BaseMapper<SysMessageLog> {
}
