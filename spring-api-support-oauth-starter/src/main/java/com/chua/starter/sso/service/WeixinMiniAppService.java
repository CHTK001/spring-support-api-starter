package com.chua.starter.sso.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.service.UserInfoService;
import com.chua.starter.service.mapper.SysUserThirdPartyMapper;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserThirdParty;
import com.chua.tencent.support.miniapp.TencentMaHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 微信小程序
 * @author CH
 * @since 2024/12/2
 */
@Service
@LoginType("MINI_APP")
public class WeixinMiniAppService extends UserService{

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;
    @Override
    public UserResult checkLogin(String openId, String password, String address, Object ext) {

        List<SysUserThirdParty> sysUserThirdParties = sysUserThirdPartyMapper.selectList(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserThirdPartyUid, openId)
                .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.WX_MINI_APP.name())
        );
        if(CollectionUtils.isEmpty(sysUserThirdParties)) {
            return new UserResult().setMessage("账号不存在");
        }

        Integer sysUserId = com.chua.common.support.utils.CollectionUtils.findFirst(sysUserThirdParties).getSysUserId();
        if(null == sysUserId) {
            return new UserResult().setMessage("账号不存在");
        }

        SysUser user = sysUserMapper.selectById(sysUserId);
        if (null == user) {
            return new UserResult().setMessage("账号不存在");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }

        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);
        return loginInfo(user.getSysUserUsername(), user, address);
    }

}
