package com.chua.starter.oauth.sso.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.login.UserService;
import com.chua.starter.oauth.sso.mapper.SysUserMapper;
import com.chua.starter.oauth.sso.mapper.SysUserThirdPartyMapper;
import com.chua.starter.service.model.SysUser;
import com.chua.starter.service.model.SysUserThirdParty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 微信小程序登录服务
 * <p>处理微信小程序用户的登录验证与授权</p>
 *
 * <h2>功能特性</h2>
 * <ul>
 *   <li>支持通过微信小程序OpenID直接登录系统</li>
 *   <li>自动关联微信用户与系统内部用户账号</li>
 *   <li>继承标准用户服务的角色和权限加载逻辑</li>
 *   <li>OpenID与用户ID映射缓存，提高查询效率</li>
 * </ul>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li>微信小程序用户无感知登录</li>
 *   <li>小程序内部功能授权</li>
 *   <li>微信用户身份与系统用户身份的映射</li>
 *   <li>小程序多端同步登录状态</li>
 * </ul>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li>OpenID与用户ID映射关系缓存</li>
 *   <li>批量查询替代多次单条查询</li>
 *   <li>延迟加载用户详细信息</li>
 *   <li>精确的日志记录与性能监控</li>
 * </ul>
 *
 * @author CH
 * @since 2024/12/2
 */
@Slf4j
@Service
@LoginType("MINI_APP")
@Tag(name = "微信小程序登录", description = "基于微信小程序OpenID的用户登录验证")
public class WeixinMiniAppService extends UserService {

    /**
     * OpenID映射缓存过期时间（分钟）
     */
    private static final long OPENID_CACHE_EXPIRE = 60L;

    /**
     * OpenID映射缓存键前缀
     */
    private static final String OPENID_CACHE_PREFIX = "miniapp:openid:";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 微信小程序用户登录验证
     * <p>
     * 通过微信小程序的OpenID查询关联的系统用户。本方法不验证密码，
     * 而是通过小程序授权获取的OpenID查找对应的系统用户账号。
     * 查询结果会被缓存以提高后续访问性能。
     * </p>
     *
     * @param openId   微信小程序用户的OpenID
     * @param password 不使用，可为null
     * @param address  客户端IP地址
     * @param ext      扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String openId, String password, String address, Object ext) {
        log.debug("微信小程序登录请求: OpenID={}, IP={}", openId, address);

        // 构建缓存键
        String cacheKey = OPENID_CACHE_PREFIX + openId;
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 尝试从缓存获取用户ID
        String cachedUserId = ops.get(cacheKey);
        Integer sysUserId = null;

        if (cachedUserId != null) {
            // 缓存命中，直接使用缓存的用户ID
            log.debug("OpenID缓存命中: {}", openId);
            sysUserId = Integer.valueOf(cachedUserId);
        } else {
            // 缓存未命中，查询数据库
            long startTime = System.currentTimeMillis();

            // 使用更高效的查询方式 - 直接查询单个结果
            SysUserThirdParty thirdPartyUser = sysUserThirdPartyMapper.selectOne(
                    Wrappers.<SysUserThirdParty>lambdaQuery()
                            .eq(SysUserThirdParty::getSysUserThirdPartyUid, openId)
                            .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.WX_MINI_APP.name())
                            .last("LIMIT 1") // 限制只返回一条记录，提高查询效率
            );

            log.debug("小程序用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

            // 检查是否存在关联记录
            if (thirdPartyUser == null) {
                log.warn("微信小程序登录失败: 未找到关联用户 OpenID={}", openId);
                return new UserResume("账号不存在");
            }

            sysUserId = thirdPartyUser.getSysUserId();

            // 缓存OpenID与用户ID的映射关系
            if (sysUserId != null) {
                ops.set(cacheKey, sysUserId.toString(), OPENID_CACHE_EXPIRE, TimeUnit.MINUTES);
            }
        }

        if (sysUserId == null) {
            log.warn("微信小程序登录失败: 关联记录中缺少用户ID OpenID={}", openId);
            return new UserResume("账号不存在");
        }

        // 查询系统用户信息
        long startTime = System.currentTimeMillis();
        SysUser user = sysUserMapper.selectById(sysUserId);
        log.debug("系统用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

        if (null == user) {
            log.warn("微信小程序登录失败: 关联的系统用户不存在 ID={}", sysUserId);
            return new UserResume("账号不存在");
        }

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.warn("微信小程序登录失败: 账户已禁用 ID={}", sysUserId);
            return new UserResume("账号已被禁用");
        }

        // 更新用户登录信息
        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);

        log.info("微信小程序登录成功: {} (ID:{}), OpenID={}", user.getSysUserUsername(), user.getSysUserId(), openId);
        return loginInfo(user.getSysUserUsername(), user, address);
    }
}
