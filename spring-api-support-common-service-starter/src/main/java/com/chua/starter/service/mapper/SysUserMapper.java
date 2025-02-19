package com.chua.starter.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.common.support.annotations.DataScope;
import com.chua.starter.service.query.SysUserQuery;
import com.chua.starter.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 用戶權限
     * @param userId 用户ID
     * @return 结果
     */
    Set<String> selectPermission(Integer userId);

    /**
     * 查詢賬號信息
     * @param accessKey 賬號
     * @return 用户信息
     */
    SysUser selectCustomOne(String accessKey);
    /**
     * 查询系统用户分页数据
     * 此方法用于根据不同的筛选条件分页查询系统用户信息
     *
     * @param page 分页对象，包含分页参数如当前页码和每页记录数
     * @param isAdmin 是否仅查询管理员用户，true为仅查询管理员，false或不传查询所有用户
     * @param sysUserQuery 查询条件对象，用于指定更详细的查询条件
     * @return 返回分页查询结果，包含查询到的用户记录和分页信息
     */
    @DataScope
    Page<SysUser> selectPageUser(
            @Param("page") Page<SysUser> page,
            @Param("isAdmin") boolean isAdmin,
            @Param("query") SysUserQuery sysUserQuery);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @param tenantId 租户ID
     * @return 用户信息
     */
    SysUser selectOne(@Param("username") String username, @Param("tenantId") String tenantId);
}
