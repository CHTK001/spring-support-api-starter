package com.chua.starter.sso.service;

import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.service.UserInfoService;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 用户信息鉴权
 *
 * @author CH
 */
@Service
@LoginType("THIRD_PARTY")
public class ThirdPartyUserService extends UserService implements UserInfoService, Constant {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserResult checkLogin(String username, String password, String address, Object ext) {
        SysUser user = sysUserMapper.selectById(username);
        if (null == user) {
            return new UserResult().setMessage("账号/密码不正确");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }

        return loginInfo("", user, address);
    }

}
