package com.chua.starter.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.monitor.entity.MonitorAcmeAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * ACME 账户 Mapper，提供 monitor_acme_account 表的基础增删改查能力。
 *
 * @since 2025-10-17
 * @author CH
 */
@Mapper
public interface MonitorAcmeAccountMapper extends BaseMapper<MonitorAcmeAccount> {
}
