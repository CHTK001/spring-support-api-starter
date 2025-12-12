package com.chua.starter.system.login;

import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.mapper.SysUserTripartiteBindMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Gitee 第三方登录服务
 * <p>处理 Gitee 用户的登录验证与授权</p>
 *
 * @author CH
 * @since 2024/01/30
 */
@Slf4j
@Service
@LoginType("gitee")
public class GiteeLoginService extends UserLoginService {

    @Autowired
    private SysUserTripartiteBindMapper sysUserTripartiteBindMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Gitee 用户登录验证
     *
     * @param username Gitee 用户唯一标识
     * @param password 不使用，可为null
     * @param address  客户端IP地址
     * @param ext      扩展参数，可为null
     * @return 用户信息，若验证失败则返回null
     */
    @Override
    public UserResume checkLogin(String username, String password, String address, Object ext) {
        log.debug("Gitee登录请求: username={}, IP={}", username, address);
        
        SysUser sysUser = sysUserTripartiteBindMapper.getSysUser(username, "gitee");
        if (null == sysUser) {
            log.warn("Gitee登录失败: 未找到关联用户 username={}", username);
            return null;
        }
        
        log.info("Gitee登录成功: {} (ID:{})", sysUser.getSysUserUsername(), sysUser.getSysUserId());
        return loginInfo(sysUser.getSysUserUsername(), sysUser, address);
    }
}
