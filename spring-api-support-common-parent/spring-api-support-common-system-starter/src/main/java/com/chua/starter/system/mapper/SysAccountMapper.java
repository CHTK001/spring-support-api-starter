package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统账号 Mapper
 *
 * @author CH
 * @since 2026/08/03
 */
@Mapper
public interface SysAccountMapper extends BaseMapper<SysAccount> {
}