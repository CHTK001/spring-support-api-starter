package com.chua.starter.oauth.sso.service;

import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.service.UserInfoService;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 第三方用户登录服务
 * <p>通过第三方系统的用户标识验证用户身份</p>
 *
 * <h2>功能特性</h2>
 * <ul>
 *   <li>支持通过第三方系统用户ID直接登录</li>
 *   <li>无需验证密码，基于第三方已验证的身份</li>
 *   <li>继承标准用户服务的角色和权限加载逻辑</li>
 * </ul>
 *
 * <h2>使用场景</h2>
 * <ul>
 *   <li>第三方系统单点登录集成</li>
 *   <li>已完成第三方身份验证后的内部登录</li>
 *   <li>系统间用户信息共享和授权</li>
 * </ul>
 *
 * @author CH
 */
@Slf4j
@Service
@LoginType("THIRD_PARTY")
@Tag(name = "第三方登录服务", description = "基于第三方系统用户标识的登录验证")
public class ThirdPartyUserService extends UserService implements UserInfoService, Constant {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 第三方账号登录验证
     * <p>
     * 根据第三方系统的用户ID查询对应的系统用户。本方法不验证密码，
     * 仅通过第三方系统提供的用户标识(principal)查找并验证用户状态。
     * </p>
     *
     * @param principal  第三方系统的用户标识，通常是用户ID
     * @param credential 不使用，可为null
     * @param address    客户端地址
     * @param ext        扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResult checkLogin(String principal, String credential, String address, Object ext) {
        log.debug("第三方登录请求: 用户ID={}, IP={}", principal, address);

        // 根据第三方用户ID查询系统用户
        long startTime = System.currentTimeMillis();
        SysUser user = sysUserMapper.selectById(principal);
        log.debug("第三方用户查询耗时: {}ms", System.currentTimeMillis() - startTime);
        
        if (null == user) {
            log.warn("第三方登录失败: 用户不存在 ID={}, IP={}", principal, address);
            return new UserResult().setMessage("账号/密码不正确");
        }

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.warn("第三方登录失败: 账户已禁用 ID={}, IP={}", principal, address);
            return new UserResult().setMessage("账号已被禁用");
        }

        log.info("第三方登录成功: {} (ID:{}), IP={}", user.getSysUserUsername(), user.getSysUserId(), address);

        // 创建登录信息，使用空字符串作为accessKey
        return loginInfo("", user, address);
    }

}
