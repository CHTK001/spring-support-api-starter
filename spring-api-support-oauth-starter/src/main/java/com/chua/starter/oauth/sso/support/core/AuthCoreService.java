package com.chua.starter.oauth.sso.support.core;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证核心服务。
 *
 * <p>把「登录 / 升级 / 登出」这三类会签发或销毁凭据的操作收敛到一处，
 * 让不同传输入口（Spring MVC 的 {@code AuthProtocolController}、独立协议服务器的
 * {@code OauthProtocolHandler}）只负责解析与编码，不再各自拼装认证逻辑。</p>
 *
 * <p>此前这些逻辑分散在 {@code LoginAuthorization} / {@code UpgradeAuthorization} /
 * {@code LogoutAuthorization} 与 {@code OauthProtocolHandler} 中，同一个语义改一处漏一处
 * （例如升级类型的大小写处理、登出时 uid/token 的选择）。收敛后策略只在这里生效。</p>
 *
 * <p>说明：{@code /verify} 暂未收敛——MVC 侧要经过 Bearer/JWT/AppKey 等
 * {@code AuthorizationHandler} 分支，与协议服务器只做 token 解析的语义不同，
 * 需要单独设计契约后再合并。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCoreService {

    /**
     * 认证服务端属性
     */
    private final AuthServerProperties authServerProperties;

    /**
     * 登录检查
     */
    private final LoginCheck loginCheck;

    /**
     * 登录。
     *
     * @param address    可信客户端地址，可为空
     * @param principal  用户标识
     * @param credential 凭证
     * @param authType   认证类型
     * @param ext        扩展参数
     * @return 登录结果
     */
    public ReturnResult<LoginResult> login(String address, String principal, String credential,
                                           String authType, Object ext) {
        return loginCheck.doLogin(address, principal, credential, authType, ext);
    }

    /**
     * 令牌校验（标准令牌 / Bearer 共用）。
     *
     * <p>MVC 侧原先在 {@code DefaultAuthorizationHandler} 与
     * {@code BearerAuthorizationHandler} 里各写了一遍「取解析器 + resolve」，
     * 协议侧又写了一遍；收敛后只有这里一份。JWT / AppKey / AppId 等附加校验
     * 仍由各自的 {@code AuthorizationHandler} 负责。</p>
     *
     * @param cookie 会话 Cookie，可为空
     * @param token  访问令牌，可为空
     * @return 用户信息
     */
    public ReturnResult<UserResume> verify(String cookie, String token) {
        TokenResolver resolver = tokenResolver();
        if (resolver == null) {
            return ReturnResult.error("认证服务器无法认证");
        }
        return resolver.resolve(cookie, token);
    }

    /**
     * 令牌升级。
     *
     * <p>类型字符串大小写不敏感：早期协议入口用 {@code getUpgradeType} 精确匹配
     * {@code name()}，小写 {@code refresh} 会被静默降级成 TIMESTAMP。这里统一按
     * 忽略大小写处理，未知/空值仍回落 TIMESTAMP，与历史行为兼容。</p>
     *
     * @param upgradeType  升级类型（refresh/version/timestamp）
     * @param token        当前令牌
     * @param refreshToken 刷新令牌
     * @return 升级后的用户信息
     */
    public ReturnResult<UserResume> upgrade(String upgradeType, String token, String refreshToken) {
        TokenResolver resolver = tokenResolver();
        if (resolver == null) {
            return ReturnResult.error("认证服务器无法认证");
        }
        String type = upgradeType == null ? "" : upgradeType.trim();
        if ("REFRESH".equalsIgnoreCase(type)) {
            return resolver.upgradeForRefresh(token, refreshToken);
        }
        if ("VERSION".equalsIgnoreCase(type)) {
            return resolver.upgradeForVersion(token, refreshToken);
        }
        return resolver.upgradeForTimestamp(token, refreshToken);
    }

    /**
     * 登出 / 踢下线 / 禁用等会话操作。
     *
     * <p>语义与 {@link TokenResolver#logout(String, LogoutType)} 一致：
     * 有 uid 时按 uid + 类型处理（DISABLE/ENABLE/KICK 等），只有令牌时按单令牌登出。</p>
     *
     * @param uid   用户标识或令牌（随类型而定），可为空
     * @param type  操作类型，为空按 LOGOUT 处理
     * @param token 当前令牌，可为空
     * @return 处理结果
     */
    public ReturnResult<String> logout(String uid, LogoutType type, String token) {
        TokenResolver resolver = tokenResolver();
        if (resolver == null) {
            return ReturnResult.error("认证服务器无法认证");
        }
        if (StringUtils.isNotBlank(uid)) {
            resolver.logout(uid, type == null ? LogoutType.LOGOUT : type);
        } else if (StringUtils.isNotBlank(token)) {
            resolver.logout(token);
        }
        return ReturnResult.ok("登出成功");
    }

    /**
     * 解析当前配置对应的令牌解析器。
     *
     * @return 令牌解析器，未配置时返回 {@code null}
     */
    public TokenResolver tokenResolver() {
        String tokenManagement = authServerProperties.getTokenManagement();
        TokenResolver resolver = loginCheck.resolveTokenResolver(tokenManagement);
        if (resolver == null) {
            log.error("[AuthCore] 令牌解析器不可用: tokenManagement={}", tokenManagement);
        }
        return resolver;
    }
}
