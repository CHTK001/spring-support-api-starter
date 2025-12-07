package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 用户 Mapper
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 用户权限
     *
     * @param userId 用户ID
     * @return 结果
     */
    Set<String> selectPermission(Integer userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户信息
     */
    SysUser selectOne(@Param("username") String username, @Param("tenantId") String tenantId);
}