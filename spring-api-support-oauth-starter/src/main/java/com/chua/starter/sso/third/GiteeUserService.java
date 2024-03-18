package com.chua.starter.sso.third;

import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.sso.impl.SysUser;
import com.chua.starter.sso.mapper.SysUserTripartiteBindMapper;
import com.chua.starter.sso.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


/**
 * gitee用户服务
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/01/30
 */
@Service
@LoginType("gitee")
public class GiteeUserService extends UserService {

    @Resource
    private SysUserTripartiteBindMapper sysUserTripartiteBindMapper;
    @Override
    public UserResult checkLogin(String accessKey, String secretKey, String address, Object ext) {
        SysUser sysUser = sysUserTripartiteBindMapper.getSysUser(accessKey, "gitee");
        return null == sysUser ? null : loginInfo(sysUser.getUsername(), sysUser);
    }

}
