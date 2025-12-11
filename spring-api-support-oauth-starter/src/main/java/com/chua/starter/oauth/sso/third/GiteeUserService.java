package com.chua.starter.oauth.sso.third;

import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.mapper.SysUserTripartiteBindMapper;
import com.chua.starter.oauth.sso.login.UserService;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


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
    @Autowired
    private SysUserTripartiteBindMapper sysUserTripartiteBindMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserResume checkLogin(String username, String password, String address, Object ext) {
        SysUser sysUser = sysUserTripartiteBindMapper.getSysUser(username, "gitee");
        return null == sysUser ? null : loginInfo(sysUser.getSysUserUsername(), sysUser, address);
    }

}
