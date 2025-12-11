package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户日志 Mapper
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysUserLogMapper extends BaseMapper<SysUserLog> {
}