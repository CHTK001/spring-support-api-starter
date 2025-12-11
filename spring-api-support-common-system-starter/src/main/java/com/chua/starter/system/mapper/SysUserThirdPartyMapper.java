package com.chua.starter.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chua.starter.entity.system.SysUserThirdParty;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户第三方绑定信息 Mapper
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Mapper
public interface SysUserThirdPartyMapper extends BaseMapper<SysUserThirdParty> {
}
