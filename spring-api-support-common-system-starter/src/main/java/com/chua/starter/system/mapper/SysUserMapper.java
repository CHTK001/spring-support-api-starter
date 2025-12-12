package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.model.user.SysUserQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统用户 Mapper接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/12
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> selectPermission(@Param("userId") Integer userId);

    /**
     * 自定义查询用户（包含关联信息）
     *
     * @param accessKey 用户名
     * @return 用户信息
     */
    SysUser selectCustomOne(@Param("accessKey") String accessKey);

    /**
     * 分页查询用户
     *
     * @param page    分页对象
     * @param query   查询条件
     * @param isAdmin 是否管理员
     * @return 用户分页列表
     */
    IPage<SysUser> selectPageUser(Page<SysUser> page, @Param("query") SysUserQuery query, @Param("isAdmin") boolean isAdmin);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户信息
     */
    SysUser selectOne(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 根据用户名和租户编码查询用户
     *
     * @param username   用户名
     * @param tenantCode 租户编码
     * @return 用户信息
     */
    SysUser selectByTenantCode(@Param("username") String username, @Param("tenantCode") String tenantCode);

    /**
     * 根据租户查询用户
     *
     * @param tenant 租户信息
     * @return 用户信息
     */
    SysUser selectOneByTenant(@Param("tenant") Map<String, Object> tenant);

    /**
     * 插入租户用户
     *
     * @param sysUser 用户信息
     * @param tenant  租户信息
     * @return 影响行数
     */
    int insertTenant(@Param("sysUser") SysUser sysUser, @Param("tenant") Map<String, Object> tenant);
}
