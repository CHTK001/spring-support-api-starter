package com.chua.starter.tenant.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.tenant.entity.SysService;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务 Mapper 接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/07
 */
@Mapper
public interface SysServiceMapper extends BaseMapper<SysService> {
}
