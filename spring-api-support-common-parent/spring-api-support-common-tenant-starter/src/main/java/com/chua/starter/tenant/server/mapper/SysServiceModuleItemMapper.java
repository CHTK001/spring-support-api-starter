package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysServiceModuleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务模块关联 映射器 接口
 *
 * @版本 1.0.0
 * @since 2024/12/07
 * @author CH
 */
@Mapper
public interface SysServiceModuleItemMapper extends BaseMapper<SysServiceModuleItem> {

    /**
     * 按租户标识删除服务模块项记录
     *
     * @param tenantId 租户标识
     * @param tenantIdColumn 租户标识列名
     * @return 删除行数
     */
    int deleteByTenantId(@Param("tenantId") String tenantId, @Param("tenantIdColumn") String tenantIdColumn);
}
