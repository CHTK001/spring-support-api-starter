package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysPolicyConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统一策略配置 映射器接口
 *
 * <p>统一策略配置表 {@code sys_firewall_config} 是 IP 黑白名单、限流规则、账号黑白名单、
 * 灰度规则与功能开关的唯一存储，SQL 全部走 MyBatis-Plus Wrapper，不写注解 SQL。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Mapper
public interface SysPolicyConfigMapper extends BaseMapper<SysPolicyConfig> {
}
