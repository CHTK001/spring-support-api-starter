package com.chua.starter.oauth.sso.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.oauth.sso.support.entity.SysOAuthTraceLog;
import org.apache.ibatis.annotations.Mapper;

/**
* OAuth追踪日志 映射器
*
* @since 2025/01/01
* @author CH
 */
@Mapper
public interface SysOAuthTraceLogMapper extends BaseMapper<SysOAuthTraceLog> {
}
