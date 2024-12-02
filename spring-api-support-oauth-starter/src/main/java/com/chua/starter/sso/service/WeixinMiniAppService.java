package com.chua.starter.sso.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.service.UserInfoService;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import com.chua.tencent.support.miniapp.TencentMaHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Override
    public UserResult checkLogin(String username, String password, String address, Object ext) {
        String newAccessKey = "mini_app:" + LOGIN_ERROR + username;
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getSysUserUsername, username));
        if (null == user) {
            return new UserResult().setMessage("账号不存在");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }

        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);
        stringRedisTemplate.delete(newAccessKey);
        return loginInfo(username, user, address);
    }

}
