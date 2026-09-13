package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysIpBlackwhite;
import org.apache.ibatis.annotations.Mapper;

/**
* IP黑白名单 映射器接口
*
* @since 4.0.0.43
* @author CH
 */
@Mapper
public interface SysIpBlackwhiteMapper extends BaseMapper<SysIpBlackwhite> {
}
