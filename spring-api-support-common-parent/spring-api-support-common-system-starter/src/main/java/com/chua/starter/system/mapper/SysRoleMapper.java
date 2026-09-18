package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.mybatis.annotations.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* 角色 映射器接口
*
* @since 2024/8/7
* @author CH
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
    * 根据用户标识查询角色
    * <p>登录时使用，不需要数据权限过滤</p>
    *
    * @param userIds 用户标识集合
    * @return 角色列表
     */
    @DataScope(enabled = false)
    List<SysRole> listByUserId(@Param("userIds") Set<Integer> userIds);

    /**
    * 鏍规嵁绉熸埛标识查询绠＄悊鍛樿鑹?
    *
    * @param sysTenantId 绉熸埛标识
    * @param sysRoleCode 角色编码
    * @return 角色
     */
    SysRole selectOneByTenant(@Param("sysTenantId") Integer sysTenantId, @Param("sysRoleCode") String sysRoleCode);

    /**
    * 鏍规嵁绉熸埛标识查询绠＄悊鍛樿鑹"
    *
    * @param sysTenantId 绉熸埛标识
    * @param sysTenantId 绉熸埛标识
    * @param sysRole 方法入参 sysRole
     */
    void insertWithTenant(@Param("sysRole") SysRole sysRole, @Param("sysTenantId") Integer sysTenantId);
}