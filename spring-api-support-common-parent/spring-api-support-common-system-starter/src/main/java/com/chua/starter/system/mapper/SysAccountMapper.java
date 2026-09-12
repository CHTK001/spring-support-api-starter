package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysAccount;
import org.apache.ibatis.annotations.Mapper;

/**
   * 系统账号 映射器
 *
 * @since 2026/08/03
 * @author CH
 */
@Mapper
public interface SysAccountMapper extends BaseMapper<SysAccount> {
}