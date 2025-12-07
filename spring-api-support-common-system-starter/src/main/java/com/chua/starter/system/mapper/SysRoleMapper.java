package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 鏍规嵁用户id查询角色
     *
     * @param userIds 用户id
     * @return 角色
     */
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