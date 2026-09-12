package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.service.SysRequestTraceRaw;
import org.apache.ibatis.annotations.Mapper;

/**
* 请求追踪原始记录映射器。
*
* @since 2026/03/25
* @author CH
 */
@Mapper
public interface SysRequestTraceRawMapper extends BaseMapper<SysRequestTraceRaw> {
}
