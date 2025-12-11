package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 Mapper
 *
 * @author CH
 * @since 1.0.0
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
