package com.chua.starter.sso.third;

import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.sso.mapper.SysUserTripartiteBindMapper;
import com.chua.starter.sso.service.UserService;
import com.chua.starter.system.entity.SysUser;
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

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserResult checkLogin(String username, String password, String address, Object ext) {
        SysUser sysUser = sysUserTripartiteBindMapper.getSysUser(username, "gitee");
        return null == sysUser ? null : loginInfo(sysUser.getSysUserUsername(), sysUser, address);
    }

}
