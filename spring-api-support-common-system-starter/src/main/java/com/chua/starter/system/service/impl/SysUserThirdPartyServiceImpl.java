package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.service.SysUserThirdPartyService;
import org.springframework.stereotype.Service;

/**
 * 用户第三方绑定服务实现类
 * <p>
 * 提供用户与第三方平台（如Gitee、微信小程序等）绑定信息的管理功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/12
 */
@Service
public class SysUserThirdPartyServiceImpl extends ServiceImpl<SysUserThirdPartyMapper, SysUserThirdParty>
        implements SysUserThirdPartyService {

}
