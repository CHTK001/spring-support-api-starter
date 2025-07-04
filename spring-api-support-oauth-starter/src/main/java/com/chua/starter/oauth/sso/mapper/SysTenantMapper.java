package com.chua.starter.oauth.sso.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.system.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}