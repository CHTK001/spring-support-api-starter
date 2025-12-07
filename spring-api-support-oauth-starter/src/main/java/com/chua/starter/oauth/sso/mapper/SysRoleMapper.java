package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}