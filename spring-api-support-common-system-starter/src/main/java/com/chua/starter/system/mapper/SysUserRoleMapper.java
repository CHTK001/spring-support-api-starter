package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author CH
 * @since 2024/8/7
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    /**
     * 淇濆瓨鐢ㄦ埛瑙掕壊
     * @param sysUserId 鐢ㄦ埛ID
     * @param roleCode 瑙掕壊缂栫爜
     */
    void saveUserRole(@Param("sysUserId") Integer sysUserId, @Param("roleCode") String roleCode);

    /**
     * 鏌ヨ鐢ㄦ埛瑙掕壊
     *
     * @param sysTenantId 绉熸埛ID
     * @param sysUserId   鐢ㄦ埛ID
     * @param sysRoleId   瑙掕壊ID
     * @return SysUserRole
     */
    SysUserRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId,
                                  @Param("sysUserId") Integer sysUserId,
                                  @Param("sysRoleId") Integer sysRoleId);

    /**
     * 淇濆瓨鐢ㄦ埛瑙掕壊
     *
     * @param sysUserRole 鐢ㄦ埛瑙掕壊
     * @param sysTenantId 绉熸埛ID
     */
    void insertWithTenant(@Param("sysUserRole") SysUserRole sysUserRole, @Param("sysTenantId") Integer sysTenantId);
}