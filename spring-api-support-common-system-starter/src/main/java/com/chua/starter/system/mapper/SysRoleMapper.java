package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.mybatis.annotations.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 角色 Mapper接口
 *
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户ID查询角色
     * <p>登录时使用，不需要数据权限过滤</p>
     *
     * @param userIds 用户ID集合
     * @return 角色列表
     */
    @DataScope(enabled = false)
    List<SysRole> listByUserId(@Param("userIds") Set<Integer> userIds);

    /**
     * 鏍规嵁绉熸埛id查询绠＄悊鍛樿鑹?
     *
     * @param sysTenantId 绉熸埛id
     * @param sysRoleCode 角色编码
     * @return 角色
     */
    SysRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId, @Param("sysRoleCode") String sysRoleCode);

    /**
     * 鏍规嵁绉熸埛id查询绠＄悊鍛樿鑹"
     *
     * @param sysTenantId 绉熸埛id
     * @param sysTenantId 绉熸埛ID
     */
    void insertWithTenant(@Param("sysRole") SysRole sysRole, @Param("sysTenantId") Integer sysTenantId);
}