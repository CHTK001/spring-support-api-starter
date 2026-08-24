package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysServiceModuleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务模块关联 Mapper 接口
 *
 * @version 1.0.0
 * @since 2024/12/07
 * @author CH
 */
@Mapper
public interface SysServiceModuleItemMapper extends BaseMapper<SysServiceModuleItem> {

    /**
     * 按租户ID删除服务模块项记录
     *
     * @param tenantId 租户ID
     * @param tenantIdColumn 租户ID列名
     * @return 删除行数
     */
    int deleteByTenantId(@Param("tenantId") String tenantId, @Param("tenantIdColumn") String tenantIdColumn);
}
