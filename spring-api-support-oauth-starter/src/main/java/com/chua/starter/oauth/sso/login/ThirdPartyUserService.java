package com.chua.starter.oauth.sso.login;

import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 第三方用户登录服务
 * <p>通过第三方系统的用户标识验证用户身份</p>
 *
 * <h2>功能特性</h2>
 * <ul>
 *   <li>支持通过第三方系统用户ID直接登录</li>
 *   <li>无需验证密码，基于第三方已验证的身份</li>
 *   <li>继承标准用户服务的角色和权限加载逻辑</li>
 *   <li>用户登录状态缓存，减少重复查询</li>
 * </ul>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li>第三方系统单点登录集成</li>
 *   <li>已完成第三方身份验证后的内部登录</li>
 *   <li>系统间用户信息共享和授权</li>
 *   <li>API网关和微服务间的身份传递</li>
 * </ul>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li>用户查询结果缓存，减少数据库访问</li>
 *   <li>登录状态追踪，避免频繁验证</li>
 *   <li>精确的日志记录，便于问题排查</li>
 * </ul>
 *
 * @author CH
 */
@Slf4j
@Service
@LoginType("THIRD_PARTY")
@Tag(name = "第三方登录服务", description = "基于第三方系统用户标识的登录验证")
public class ThirdPartyUserService extends UserService implements UserInfoService, Constant {

    /**
     * 用户缓存过期时间（分钟）
     */
    private static final long USER_CACHE_EXPIRE = 30L;

    /**
     * 用户缓存键前缀
     */
    private static final String USER_CACHE_PREFIX = "third_party:login:";
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 第三方账号登录验证
     * <p>
     * 根据第三方系统的用户ID查询对应的系统用户。本方法不验证密码，
     * 仅通过第三方系统提供的用户标识(principal)查找并验证用户状态。
     * 查询结果会被缓存以提高后续访问性能。
     * </p>
     *
     * @param principal  第三方系统的用户标识，通常是用户ID
     * @param credential 不使用，可为null
     * @param address    客户端地址
     * @param ext        扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        log.debug("第三方登录请求: 用户ID={}, IP={}", principal, address);

        // 构建缓存键
        String cacheKey = USER_CACHE_PREFIX + principal;
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 尝试从缓存获取用户ID
        String cachedUserId = ops.get(cacheKey);
        SysUser user = null;

        if (cachedUserId != null) {
            // 缓存命中，直接查询用户
            log.debug("第三方用户缓存命中: ID={}", principal);
            user = sysUserMapper.selectById(principal);
        } else {
            // 缓存未命中，查询数据库
            long startTime = System.currentTimeMillis();
            user = sysUserMapper.selectById(principal);
            log.debug("第三方用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

            // 如果用户存在且状态正常，缓存用户ID
            if (user != null && user.getSysUserStatus() == 1) {
                ops.set(cacheKey, principal, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
            }
        }
        
        if (null == user) {
            log.warn("第三方登录失败: 用户不存在 ID={}, IP={}", principal, address);
            return new UserResume("账号/密码不正确");

        }

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.warn("第三方登录失败: 账户已禁用 ID={}, IP={}", principal, address);
            return new UserResume("账号已被禁用");
        }

        // 更新用户最后登录IP
        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);

        log.info("第三方登录成功: {} (ID:{}), IP={}", user.getSysUserUsername(), user.getSysUserId(), address);

        // 创建登录信息，使用空字符串作为accessKey
        return loginInfo("", user, address);
    }
}
