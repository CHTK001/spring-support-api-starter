package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 映射器 接口
 *
 * @since 2024/12/06
 * @author CH
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
