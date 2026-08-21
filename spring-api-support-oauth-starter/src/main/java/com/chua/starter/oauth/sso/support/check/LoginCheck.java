package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.crypto.AesCodec;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.security.LoginAttemptService;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static com.chua.common.support.lang.code.ReturnCode.OK;
import static com.chua.starter.oauth.client.support.execute.AuthClientExecute.createUid;

/**
 * 登录检测
 * <p>
 * 支持登录失败锁定机制，防止暴力破解攻击。
 * </p>
 *
 * @author CH
 */
@Slf4j
public class LoginCheck {

    /** Auth服务器属性 */
    private final AuthServerProperties authServerProperties;

    /**
     * 应用上下文，用于从 Spring 容器加载 UserInfoService（实现为 @Service，依赖 Spring 注入）
     */
    private final ApplicationContext applicationContext;

    /**
     * 登录尝试服务（可选注入）
     */
    @Autowired(required = false)
    /** Loginattempt服务 */
    private LoginAttemptService loginAttemptService;

    /**
     * 创建 LoginCheck 实例
     * @param authServerProperties authServerProperties
     */
    public LoginCheck(AuthServerProperties authServerProperties) {
        this(authServerProperties, null);
    }

    /**
     * 创建 LoginCheck 实例
     * @param authServerProperties authServerProperties
     * @param applicationContext applicationContext
     */
    public LoginCheck(AuthServerProperties authServerProperties, ApplicationContext applicationContext) {
        this.authServerProperties = authServerProperties;
        this.applicationContext = applicationContext;
    }

    /**
     * 登录验证
     * <p>
     * 根据登录类型通过SPI选择合适的UserInfoService实现，验证用户凭证，并生成登录令牌。
     * 不同登录类型的参数含义可能不同，如用户名密码登录、令牌登录或第三方登录。
     * </p>
     *
     * @param address    客户端地址
     * @param principal  用户标识（用户名/手机号/邮箱/令牌等）
     * @param credential 用户凭证（密码/令牌/证书等）
     * @param authType   认证类型，与UserInfoService实现的@Extension注解值匹配
     * @param ext        扩展参数，传递给UserInfoService实现
     * @return 登录结果，包含令牌和用户信息
     */
    public ReturnResult<LoginResult> doLogin(String address, String principal, String credential, String authType, Object ext) {
        long startTime = System.currentTimeMillis();
        long totalNanosStart = System.nanoTime();
        log.debug("========== 登录检测开始 ==========");
        log.debug("用户标识: {}, 认证类型: {}, 客户端地址: {}", principal, authType, address);

        // 检查账户是否被锁定
        if (loginAttemptService != null && loginAttemptService.isAccountLocked(principal)) {
            long remainingSeconds = loginAttemptService.getLockRemainingSeconds(principal);
            String message = "账户已被锁定，请" + formatRemainingTime(remainingSeconds) + "后重试";
            log.warn("登录被拒绝: 账户已锁定, username={}, remainingSeconds={}", principal, remainingSeconds);
            return ReturnResult.error(message);
        }

        // authType 为空或 NONE 时，fallback 到默认的 WEB 登录
        String resolvedAuthType = (StringUtils.isBlank(authType) || "NONE".equalsIgnoreCase(authType))
                ? "WEB" : authType;

        // 使用 SPI 机制获取 UserInfoService 实现
        UserInfoService userInfoService = resolveUserInfoService(resolvedAuthType);
        if (userInfoService == null) {
            log.warn("未找到匹配的认证服务: authType={}", resolvedAuthType);
            return ReturnResult.error("不支持该登录/鉴权方式: " + resolvedAuthType);
        }
        // 用解析后的类型替换原始值，保证后续 loginType 记录正确
        authType = resolvedAuthType;
        
        log.debug("通过SPI获取到UserInfoService: {}", userInfoService.getClass().getSimpleName());
        
        UserResume userResume = null;
        long checkStartTime = System.currentTimeMillis();
        long checkStartNanos = System.nanoTime();
        long checkLoginMs = 0L;
        
        try {
            userResume = userInfoService.checkLogin(principal, credential, address, ext);
            checkLoginMs = System.nanoTime();
            log.debug("用户凭证验证完成, 耗时: {}ms, 结果: {}", 
                    (checkLoginMs - checkStartTime) / 1_000_000L,
                    userResume != null ? (Strings.isNullOrEmpty(userResume.getMessage()) ? "成功" : "失败:" + userResume.getMessage()) : "null");
        } catch (Exception e) {
            log.error("用户认证异常: {}, 类型: {}, 异常信息: {}", principal, authType, e.getMessage(), e);
            return ReturnResult.error("认证异常: " + e.getMessage());
        }
        
        if (null != userResume && Strings.isNullOrEmpty(userResume.getMessage())) {
            userResume.setLoginType(authType);
            userResume.setUid(createUid(principal, authType));
            // 优化 userCode 生成：简化结构，只保留必要信息
            // 格式：{uid}:{加密的userId}
            userResume.setUserCode(buildUserCode(userResume));
            log.debug("用户验证成功, userId: {}, uid: {}, userCode长度: {}", 
                    userResume.getUserId(), userResume.getUid(), 
                    userResume.getUserCode() != null ? userResume.getUserCode().length() : 0);
        }

        if (null == userResume) {
            // 记录登录失败
            recordLoginFailure(principal);
            log.warn("登录失败: 未找到匹配的认证服务或用户不存在, 用户: {}, 类型: {}, 耗时: {}ms", 
                    principal, authType, System.currentTimeMillis() - startTime);
            return buildLoginFailureResult(principal);
        }

        if (!Strings.isNullOrEmpty(userResume.getMessage())) {
            // 记录登录失败
            recordLoginFailure(principal);
            log.warn("登录失败: {}, 用户: {}, 类型: {}, 耗时: {}ms", 
                    userResume.getMessage(), principal, authType, System.currentTimeMillis() - startTime);
            return buildLoginFailureResult(principal, userResume.getMessage());
        }

        log.debug("开始创建Token, tokenManagement: {}", authServerProperties.getTokenManagement());
        TokenResolver tokenManagement = resolveTokenResolver(authServerProperties.getTokenManagement());
        UserResult userResult = new UserResult();
        BeanUtils.copyProperties(userResume, userResult);
        
        long tokenStartTime = System.currentTimeMillis();
        ReturnResult<LoginResult> token = tokenManagement.createToken(address, userResult, authType);
        long tokenEndNanos = System.nanoTime();
        log.debug("Token创建完成, 耗时: {}ms, 结果: {}", 
                System.currentTimeMillis() - tokenStartTime, token.isOk() ? "成功" : "失败:" + token.getMsg());
        
        String code = token.getCode();
        if (OK.getCode().equals(code)) {
            LoginResult loginResult = token.getData();
            loginResult.setUserResume(userResume);
            // 同步过期时间到返回的用户信息(createToken 在 userResult 上设置了 expireTime,
            // 前端/调用方通过 userResume.getExpireTime() 感知 token 有效期)
            if (userResume != null && userResume.getExpireTime() == null && userResult.getExpireTime() != null) {
                userResume.setExpireTime(userResult.getExpireTime());
            }
            // 登录成功，清除失败记录
            clearLoginFailure(principal);
            log.debug("登录成功: 用户={}, 类型={}, 总耗时={}ms", principal, authType, System.currentTimeMillis() - startTime);
            // 埋点:认证链分段耗时(纳秒→毫秒),用于定位 DB/Redis/JWT 各环节瓶颈
            long totalNs = System.nanoTime() - totalNanosStart;
            log.debug("[AUTH-PROFILE] user={} total={}ms checkLogin={}ms token={}ms other={}ms",
                    principal,
                    totalNs / 1_000_000L,
                    (checkLoginMs - checkStartNanos) / 1_000_000L,
                    (tokenEndNanos - checkLoginMs) / 1_000_000L,
                    (totalNs - (checkLoginMs - checkStartNanos) - (tokenEndNanos - checkLoginMs)) / 1_000_000L);
        }
        
        log.debug("========== 登录检测结束 ==========");
        return token;
    }

    /**
     * 解析 UserInfoService 实现
     * <p>优先从 Spring 容器获取（实现类为 @Service，依赖 Spring 依赖注入），
     * 按 @Spi/@LoginType 注解值匹配认证类型；未找到时回退 SPI 加载。</p>
     *
     * @param authType 认证类型
     * @return 用户信息服务实现
     */
    private UserInfoService resolveUserInfoService(String authType) {
        if (applicationContext != null) {
            for (UserInfoService service : applicationContext.getBeansOfType(UserInfoService.class).values()) {
                Spi spi = service.getClass().getAnnotation(Spi.class);
                if (spi != null) {
                    for (String value : spi.value()) {
                        if (authType.equalsIgnoreCase(value)) {
                            log.debug("从Spring容器获取UserInfoService: {}, authType={}", service.getClass().getSimpleName(), authType);
                            return service;
                        }
                    }
                }
            }
        }
        return ServiceProvider.of(UserInfoService.class).getExtension(authType);
    }

    /**
     * 解析 TokenResolver 实现
     * <p>优先从 Spring 容器获取（实现类为 @Service，依赖 Spring 依赖注入 RedisTemplate 等），
     * 按 @Spi 注解值匹配令牌管理类型；未找到时回退 SPI 加载。</p>
     *
     * @param tokenManagement 令牌管理类型
     * @return 令牌解析器实现
     */
    public TokenResolver resolveTokenResolver(String tokenManagement) {
        if (applicationContext != null) {
            for (TokenResolver resolver : applicationContext.getBeansOfType(TokenResolver.class).values()) {
                Spi spi = resolver.getClass().getAnnotation(Spi.class);
                if (spi != null) {
                    for (String value : spi.value()) {
                        if (tokenManagement.equalsIgnoreCase(value)) {
                            log.debug("从Spring容器获取TokenResolver: {}, tokenManagement={}", resolver.getClass().getSimpleName(), tokenManagement);
                            return resolver;
                        }
                    }
                }
            }
        }
        return ServiceProvider.of(TokenResolver.class).getExtension(tokenManagement);
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String username) {
        if (loginAttemptService != null && StringUtils.isNotBlank(username)) {
            loginAttemptService.recordFailedAttempt(username);
        }
    }

    /**
     * 清除登录失败记录
     */
    private void clearLoginFailure(String username) {
        if (loginAttemptService != null && StringUtils.isNotBlank(username)) {
            loginAttemptService.clearFailedAttempts(username);
        }
    }

    /**
     * 构建登录失败结果
     */
    private ReturnResult<LoginResult> buildLoginFailureResult(String username) {
        return buildLoginFailureResult(username, "用户名或密码错误");
    }

    /**
     * 构建登录失败结果（带剩余尝试次数提示）
     */
    private ReturnResult<LoginResult> buildLoginFailureResult(String username, String message) {
        if (loginAttemptService != null && StringUtils.isNotBlank(username)) {
            LoginAttemptService.LockInfo lockInfo = loginAttemptService.getLockInfo(username);
            if (lockInfo.locked()) {
                return ReturnResult.error("账户已被锁定，请" + lockInfo.formattedRemainingTime() + "后重试");
            }
            int remaining = lockInfo.remainingAttempts();
            if (remaining > 0 && remaining < lockInfo.maxAttempts()) {
                return ReturnResult.error(message + "，还剩" + remaining + "次尝试机会");
            }
        }
        return ReturnResult.error(message);
    }

    /**
     * 格式化剩余时间
     */
    private String formatRemainingTime(long seconds) {
        if (seconds <= 0) {
            return "稍后";
        }
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) {
            return minutes + "分" + secs + "秒";
        }
        return secs + "秒";
    }

    /**
     * 构建用户编码
     * <p>
     * 优化后的 userCode 格式：{uid}:{加密的userId}
     * </p>
     * <ul>
     *   <li>uid - 用户唯一标识（MD5）</li>
     *   <li>加密的userId - 使用 AES 加密的用户ID</li>
     * </ul>
     *
     * @param userResume 用户信息
     * @return 用户编码
     */
    private String buildUserCode(UserResume userResume) {
        if (userResume == null || userResume.getUid() == null || userResume.getUserId() == null) {
            return null;
        }
        
        String encryptedUserId = new AesCodec(authServerProperties.getServiceKey().getBytes())
                .encodeHex(userResume.getUserId());
        return userResume.getUid() + ":" + encryptedUserId;
    }

    /**
     * 第三方登录检查（JustAuth）
     * <p>
     * 将 JustAuth 的 AuthUser 对象转换为系统登录流程。
     * 用于处理 OAuth2 第三方平台（Gitee、Github、微信等）的登录。
     * </p>
     *
     * @param authUser JustAuth 的用户对象
     * @param request HTTP 请求对象
     * @return 登录结果，包含令牌和用户信息；如果登录失败返回 null
     */
    public LoginResult check(me.zhyd.oauth.model.AuthUser authUser, jakarta.servlet.http.HttpServletRequest request) {
        if (authUser == null) {
            log.warn("AuthUser 为空，无法进行登录检查");
            return null;
        }
        
        String address = com.chua.starter.common.support.utils.RequestUtils.getIpAddress(request);
        String platform = authUser.getSource();
        
        log.info("第三方登录检查: platform={}, uuid={}, username={}", 
                platform, authUser.getUuid(), authUser.getUsername());
        
        // 调用 doLogin 方法进行登录
        ReturnResult<LoginResult> result = doLogin(
                address,
                authUser.getUuid(),
                authUser.getUsername(),
                platform,
                authUser
        );
        
        if (result != null && result.isOk()) {
            return result.getData();
        }
        
        log.warn("第三方登录失败: platform={}, uuid={}, msg={}", 
                platform, authUser.getUuid(), result != null ? result.getMsg() : "result为null");
        return null;
    }

    /**
     * 获取用户信息
     * <p>通过 SPI 机制根据登录类型获取对应的 UserInfoService 实现</p>
     *
     * @param userResult 用户信息
     * @return 用户信息
     */
    public UserResume getUserInfo(UserResult userResult) {
        try {
            String loginType = userResult.getLoginType();
            if (StringUtils.isBlank(loginType)) {
                log.warn("登录类型为空，无法获取用户信息");
                return null;
            }
            
            // 使用 SPI 机制获取 UserInfoService 实现
            UserInfoService userInfoService = ServiceProvider.of(UserInfoService.class).getExtension(loginType);
            if (null == userInfoService) {
                log.warn("未找到匹配的 UserInfoService: loginType={}", loginType);
                return null;
            }

            return userInfoService.upgrade(userResult);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return null;
        }
    }

    /** 获取AuthVersion */
    public String getAuthVersion(UserResult userResult) {
        try {
            String loginType = userResult.getLoginType();
            if (StringUtils.isBlank(loginType)) {
                log.warn("登录类型为空，无法获取认证版本");
                return null;
            }

            UserInfoService userInfoService = ServiceProvider.of(UserInfoService.class).getExtension(loginType);
            if (null == userInfoService) {
                log.warn("未找到匹配的 UserInfoService: loginType={}", loginType);
                return null;
            }

            var authVersionMethod = userInfoService.getClass().getMethod("authVersion", UserResult.class);
            Object value = authVersionMethod.invoke(userInfoService, userResult);
            return value == null ? null : value.toString();
        } catch (NoSuchMethodException e) {
            log.debug("当前 UserInfoService 未提供 authVersion 能力: {}", userResult.getLoginType());
            return null;
        } catch (Exception e) {
            log.error("获取认证版本异常", e);
            return null;
        }
    }
}
