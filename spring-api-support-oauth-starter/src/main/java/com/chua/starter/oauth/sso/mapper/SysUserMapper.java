package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.service.model.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 用戶權限
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