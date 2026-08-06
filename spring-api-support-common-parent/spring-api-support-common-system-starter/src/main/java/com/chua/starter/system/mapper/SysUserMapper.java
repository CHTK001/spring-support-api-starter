package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.mybatis.annotations.DataScope;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.model.user.SysUserQuery;
import com.chua.starter.system.annotations.PrivilegedVisibility;
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
     * <p>不需要数据权限过滤</p>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @DataScope(enabled = false)
    Set<String> selectPermission(@Param("userId") Integer userId);

    /**
     * 查询登录授权画像
     * <p>聚合角色、权限、部门数据权限，减少登录/升级链路的数据库往返次数</p>
     *
     * @param userId 用户ID
     * @return 授权画像行
     */
    @DataScope(enabled = false)
    List<Map<String, Object>> selectAuthRows(@Param("userId") Integer userId);

    /**
     * 查询用户认证版本签名
     * <p>用于 VERSION 升级前快速判断用户态是否发生变化</p>
     *
     * @param userId 用户ID
     * @return 认证版本签名
     */
    @DataScope(enabled = false)
    String selectAuthVersion(@Param("userId") Integer userId);

    /**
     * 自定义查询用户（包含关联信息）
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param accessKey 用户名
     * @return 用户信息
     */
    @DataScope(enabled = false)
    SysUser selectCustomOne(@Param("accessKey") String accessKey);

    /**
     * 分页查询用户
     * <p>根据当前用户的数据权限过滤</p>
     *
     * @param page    分页对象
     * @param query   查询条件
     * @return 用户分页列表
     */
    @DataScope(value = DataFilterTypeEnum.SELF, useUserPermission = true, deptAlias = "su", userAlias = "su")
    @PrivilegedVisibility(userAlias = "su")
    IPage<SysUser> selectPageUser(Page<SysUser> page, @Param("query") SysUserQuery query);

    /**
     * 根据用户名查询用户
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户信息
     */
    @DataScope(enabled = false)
    SysUser selectOne(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 根据用户名和租户编码查询用户
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param username   用户名
     * @param tenantCode 租户编码
     * @return 用户信息
     */
    @DataScope(enabled = false)
    SysUser selectByTenantCode(@Param("username") String username, @Param("tenantCode") String tenantCode);

    /**
     * 根据租户查询用户
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param tenant 租户信息
     * @return 用户信息
     */
    @DataScope(enabled = false)
    SysUser selectOneByTenant(@Param("tenant") Map<String, Object> tenant);

    /**
     * 插入租户用户
     *
     * @param sysUser 用户信息
     * @param tenant  租户信息
     * @return 影响行数
     */
    int insertTenant(@Param("sysUser") SysUser sysUser, @Param("tenant") Map<String, Object> tenant);

    /**
     * 根据用户名查询最新的租户编码
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param username 用户名
     * @return 租户编码
     */
    @DataScope(enabled = false)
    String selectLatestTenantCodeByUsername(@Param("username") String username);
}
