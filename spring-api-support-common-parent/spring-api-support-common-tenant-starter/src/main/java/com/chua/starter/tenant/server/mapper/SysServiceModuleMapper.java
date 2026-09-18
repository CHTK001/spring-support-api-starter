package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysServiceModule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 服务模块 映射器 接口
 *
 * @版本 1.0.0
 * @since 2024/12/07
 * @author CH
 */
@Mapper
public interface SysServiceModuleMapper extends BaseMapper<SysServiceModule> {

    /**
     * 按租户标识删除服务模块记录
     *
     * @param tenantId 租户标识
     * @param tenantIdColumn 租户标识列名
     * @return 删除行数
     */
    int deleteByTenantId(@Param("tenantId") String tenantId, @Param("tenantIdColumn") String tenantIdColumn);
}
