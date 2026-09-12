package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysTenantService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
   * 租户服务关联 映射器 接口
 *
   * @版本 1.0.0
 * @since 2024/12/06
 * @author CH
 */
@Mapper
public interface SysTenantServiceMapper extends BaseMapper<SysTenantService> {

    /**
      * 根据租户标识获取菜单标识列表
     *
     * @param tenantId 租户标识
     * @return 菜单ID列表
     */
    List<Integer> getMenuByTenantId(@Param("tenantId") String tenantId);

    /**
      * 根据菜单标识查询关联该菜单(含该菜单对应服务)的所有有效租户标识
     *
     * @param menuId 菜单标识
     * @return 租户ID列表
     */
    List<String> findTenantIdsByMenuId(@Param("menuId") Integer menuId);

    /**
      * 按租户标识删除租户-服务绑定关系
     *
     * @param tenantId 租户标识
     * @return 删除行数
     */
    int deleteByTenantId(@Param("tenantId") String tenantId);
}
