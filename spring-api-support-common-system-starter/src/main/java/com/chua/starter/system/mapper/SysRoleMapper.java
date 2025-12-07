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
     * 鏍规嵁鐢ㄦ埛id鏌ヨ瑙掕壊
     *
     * @param userIds 鐢ㄦ埛id
     * @return 瑙掕壊
     */
    List<SysRole> listByUserId(@Param("userIds") Set<Integer> userIds);

    /**
     * 鏍规嵁绉熸埛id鏌ヨ绠＄悊鍛樿鑹?
     *
     * @param sysTenantId 绉熸埛id
     * @param sysRoleCode 瑙掕壊缂栫爜
     * @return 瑙掕壊
     */
    SysRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId, @Param("sysRoleCode") String sysRoleCode);

    /**
     * 鏍规嵁绉熸埛id鏌ヨ绠＄悊鍛樿鑹"
     *
     * @param sysTenantId 绉熸埛id
     * @param sysTenantId 绉熸埛ID
     */
    void insertWithTenant(@Param("sysRole") SysRole sysRole, @Param("sysTenantId") Integer sysTenantId);
}