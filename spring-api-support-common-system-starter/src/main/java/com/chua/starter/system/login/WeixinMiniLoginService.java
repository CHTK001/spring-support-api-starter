package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.DigestUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 微信小程序登录服务
 * <p>处理微信小程序用户的登录验证与授权</p>
 *
 * @author CH
 * @since 2024/12/2
 */
@Slf4j
@Service
@LoginType("WECHAT")
@Tag(name = "微信小程序登录", description = "基于微信小程序OpenID的用户登录验证")
public class WeixinMiniLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "wechat";

    /**
     * OpenID映射缓存过期时间（分钟）
     */
    private static final long OPENID_CACHE_EXPIRE = 60L;

    /**
     * OpenID映射缓存键前缀
     */
    private static final String OPENID_CACHE_PREFIX = "miniapp:openid:";

    @Autowired
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + "WECHAT";
        return new String[]{
                DigestUtils.md5Hex(baseId) + "WECHAT",
                DigestUtils.md5Hex(baseId)
        };
    }

    @Override
    public UserResume checkLogin(String openId, String password, String address, Object ext) {
        log.debug("微信小程序登录请求: OpenID={}, IP={}", openId, address);

        String cacheKey = OPENID_CACHE_PREFIX + openId;
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // 尝试从缓存获取用户ID
        String cachedUserId = ops.get(cacheKey);
        Integer sysUserId = null;

        if (cachedUserId != null) {
            log.debug("OpenID缓存命中: {}", openId);
            sysUserId = Integer.valueOf(cachedUserId);
        } else {
            long startTime = System.currentTimeMillis();
            SysUserThirdParty thirdPartyUser = sysUserThirdPartyMapper.selectOne(
                    Wrappers.<SysUserThirdParty>lambdaQuery()
                            .eq(SysUserThirdParty::getSysUserThirdPartyUid, openId)
                            .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.WX_MINI_APP.name())
                            .last("LIMIT 1")
            );
            log.debug("小程序用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

            if (thirdPartyUser == null) {
                log.warn("微信小程序登录失败: 未找到关联用户 OpenID={}", openId);
                return new UserResume("账号不存在");
            }

            sysUserId = thirdPartyUser.getSysUserId();
            if (sysUserId != null) {
                ops.set(cacheKey, sysUserId.toString(), OPENID_CACHE_EXPIRE, TimeUnit.MINUTES);
            }
        }

        if (sysUserId == null) {
            log.warn("微信小程序登录失败: 关联记录中缺少用户ID OpenID={}", openId);
            return new UserResume("账号不存在");
        }

        SysUser user = sysUserMapper.selectById(sysUserId);
        if (null == user) {
            log.warn("微信小程序登录失败: 关联的系统用户不存在 ID={}", sysUserId);
            return new UserResume("账号不存在");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("微信小程序登录失败: 账户已禁用 ID={}", sysUserId);
            return new UserResume("账号已被禁用");
        }

        log.info("微信小程序登录成功: {} (ID:{}), OpenID={}", user.getSysUserUsername(), user.getSysUserId(), openId);
        return loginInfo(user.getSysUserUsername(), user, address);
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getUserId());
        if (null == user) {
            return new UserResume("账号不存在");
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            return new UserResume("账号已被禁用");
        }
        UserResume result = loginInfo(userResult.getUsername(), user, null);
        result.setLoginType(userResult.getLoginType());
        return result;
    }
}
