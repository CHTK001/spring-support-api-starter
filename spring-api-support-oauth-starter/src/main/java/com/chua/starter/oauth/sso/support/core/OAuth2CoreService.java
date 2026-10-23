package com.chua.starter.oauth.sso.support.core;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.entity.system.SysAppId;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.system.service.SysAppIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

/**
 * OAuth2 核心服务
 * <p>
 * 提供 OAuth2 协议所需的核心功能：
 * <ul>
 *   <li>授权码（Authorization Code）的生成、存储和验证</li>
 *   <li>PKCE（Proof Key for Code Exchange）支持</li>
 *   <li>Scope 权限验证</li>
 *   <li>Grant Type 授权类型验证</li>
 *   <li>Redirect URI 回调地址验证</li>
 * </ul>
 * </p>
 *
 * @since 2025-12-28
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2CoreService {

    /**
     * SYSAPP密钥服务
    */
    private final SysAppKeyService sysAppKeyService;
    /**
     * SYSAPPID服务
    */
    private final SysAppIdService sysAppIdService;

    /**
     * Redis 键前缀 - 授权码
     */
    private static final String AUTH_CODE_PREFIX = "oauth:code:";

    /**
     * 授权码有效期（秒）
     */
    private static final int AUTH_CODE_EXPIRE_SECONDS = 300;

    /**
     * 授权码长度
     */
    private static final int AUTH_CODE_LENGTH = 32;

    /**
     * PKCE S256 方法
     */
    private static final String PKCE_METHOD_S256 = "S256";

    /**
     * PKCE plain 方法
     */
    private static final String PKCE_METHOD_PLAIN = "plain";

    /**
     * 默认允许的 scope
     *
     * <p><b>不含 admin/write</b>：客户端未显式配置 scope 时，默认授予最小读权限。
     * 早期默认集合包含 {@code admin}，等于「没配置 = 默认给管理权限」。</p>
     */
    private static final Set<String> DEFAULT_SCOPES = Set.of("read", "openid", "profile", "email");

    /**
     * 默认允许的 grant_type
     *
     * <p><b>不含 password</b>：资源所有者密码模式已不推荐，且会把用户口令暴露给客户端。
     * 需要兼容老客户端时应在具体 appId/appKey 上显式配置。</p>
     */
    private static final Set<String> DEFAULT_GRANT_TYPES = Set.of(
            "client_credentials", "authorization_code", "refresh_token"
    );

    /**
     * 字符串Redis模板
    */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * Securerandom
    */
    private final SecureRandom secureRandom = new SecureRandom();

    // ==================== 授权码管理 ====================

    /**
     * 生成授权码
     *
     * @param clientId            客户端标识
     * @param redirectUri         回调地址
     * @param scope               请求的权限范围
     * @param state               状态码
     * @param codeChallenge       PKCE 编码_challenge
     * @param codeChallengeMethod PKCE 方法（S256 或 plain）
     * @param userId              用户标识
     * @param username            用户名
     * @return 授权码
     */
    public String generateAuthorizationCode(String clientId, String redirectUri, String scope,
                                            String state, String codeChallenge, String codeChallengeMethod,
                                            String userId, String username) {
        // 生成随机授权码
        String code = generateRandomCode();

        // 构建授权码数据
        Map<String, String> codeData = new HashMap<>();
        codeData.put("client_id", clientId);
        codeData.put("redirect_uri", redirectUri);
        codeData.put("scope", scope != null ? scope : "");
        codeData.put("state", state != null ? state : "");
        codeData.put("user_id", userId);
        codeData.put("username", username);
        codeData.put("created_at", String.valueOf(System.currentTimeMillis()));

        // 存储 PKCE 信息
        if (StringUtils.isNotBlank(codeChallenge)) {
            codeData.put("code_challenge", codeChallenge);
            codeData.put("code_challenge_method", 
                    StringUtils.isNotBlank(codeChallengeMethod) ? codeChallengeMethod : PKCE_METHOD_PLAIN);
        }

        // 存储到 Redis
        String redisKey = AUTH_CODE_PREFIX + code;
        stringRedisTemplate.opsForHash().putAll(redisKey, codeData);
        stringRedisTemplate.expire(redisKey, Duration.ofSeconds(AUTH_CODE_EXPIRE_SECONDS));

        log.debug("生成授权码: clientId={}, userId={}, hasCodeChallenge={}", 
                clientId, userId, StringUtils.isNotBlank(codeChallenge));
        return code;
    }

    /**
     * 验证并消费授权码
     *
     * @param code         授权码
     * @param clientId     客户端标识
     * @param redirectUri  回调地址
     * @param codeVerifier PKCE 编码_verifier
     * @return 授权码数据，验证失败返回 空
     */
    public AuthorizationCodeData validateAndConsumeCode(String code, String clientId, 
                                                        String redirectUri, String codeVerifier) {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        String redisKey = AUTH_CODE_PREFIX + code;
        Map<Object, Object> codeData = stringRedisTemplate.opsForHash().entries(redisKey);

        if (codeData.isEmpty()) {
            log.warn("授权码不存在或已过期: code={}", maskCode(code));
            return null;
        }

        // 删除授权码（一次性使用）
        stringRedisTemplate.delete(redisKey);

 // 验证 客户端_标识
        String storedClientId = (String) codeData.get("client_id");
        if (!clientId.equals(storedClientId)) {
            log.warn("授权码客户端ID不匹配: expected={}, actual={}", storedClientId, clientId);
            return null;
        }

        // 验证 redirect_uri
        String storedRedirectUri = (String) codeData.get("redirect_uri");
        if (!redirectUri.equals(storedRedirectUri)) {
            log.warn("授权码回调地址不匹配");
            return null;
        }

        // 验证 PKCE
        String storedCodeChallenge = (String) codeData.get("code_challenge");
        if (StringUtils.isNotBlank(storedCodeChallenge)) {
            if (StringUtils.isBlank(codeVerifier)) {
                log.warn("缺少 code_verifier");
                return null;
            }

            String method = (String) codeData.get("code_challenge_method");
            if (!verifyPkce(codeVerifier, storedCodeChallenge, method)) {
                log.warn("PKCE 验证失败");
                return null;
            }
        }

        // 构建返回数据
        return new AuthorizationCodeData(
                (String) codeData.get("user_id"),
                (String) codeData.get("username"),
                (String) codeData.get("scope"),
                clientId
        );
    }

    // ==================== PKCE 验证 ====================

    /**
     * 验证 PKCE
     *
     * @param codeVerifier  编码_verifier
     * @param codeChallenge 编码_challenge
     * @param method        验证方法（S256 或 plain）
     * @return 是否验证通过
     */
    public boolean verifyPkce(String codeVerifier, String codeChallenge, String method) {
        if (StringUtils.isBlank(codeVerifier) || StringUtils.isBlank(codeChallenge)) {
            return false;
        }

        if (PKCE_METHOD_S256.equalsIgnoreCase(method)) {
            // S256: BASE64URL(SHA256(code_verifier)) == code_challenge
            String computed = computeS256Challenge(codeVerifier);
            return codeChallenge.equals(computed);
        } else if (PKCE_METHOD_PLAIN.equalsIgnoreCase(method) || StringUtils.isBlank(method)) {
            // plain: code_verifier == code_challenge
            return codeVerifier.equals(codeChallenge);
        }

        log.warn("不支持的 PKCE 方法: {}", method);
        return false;
    }

    /**
     * 计算 S256 编码_challenge
     * @param codeVerifier 编码verifier
     * @return computeS256Challenge的结果
     */
    private String computeS256Challenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    // ==================== Scope 验证 ====================

    /**
     * 验证客户端请求的 scope
     *
     * @param clientId       客户端标识
     * @param requestedScope 请求的 scope
     * @return 验证结果
     */
    public ScopeValidationResult validateScope(String clientId, String requestedScope) {
 // 先尝试通过 appid 查询
        SysAppId appId = getAppIdSafely(clientId);
        Set<String> allowedScopes;
        if (appId != null) {
 // 获取 appid 允许的 scope
            allowedScopes = parseScopes(appId.getAppIdScope());
        } else {
 // 如果 appid 不存在，尝试通过 app键 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return ScopeValidationResult.failure("客户端不存在");
            }
 // 获取 app键 允许的 scope
            allowedScopes = parseScopes(appKey.getSysAppKeyScope());
        }
        if (allowedScopes.isEmpty()) {
            allowedScopes = DEFAULT_SCOPES;
        }

        // 解析请求的 scope
        Set<String> requestedScopes = parseScopes(requestedScope);
        if (requestedScopes.isEmpty()) {
 // 未指定 scope，使用默认的 读取
            return ScopeValidationResult.success(Set.of("read"));
        }

        // 检查是否有不允许的 scope
        Set<String> invalidScopes = new HashSet<>();
        for (String scope : requestedScopes) {
            if (!allowedScopes.contains(scope)) {
                invalidScopes.add(scope);
            }
        }

        if (!invalidScopes.isEmpty()) {
            return ScopeValidationResult.failure("不允许的 scope: " + String.join(", ", invalidScopes));
        }

        return ScopeValidationResult.success(requestedScopes);
    }

    /**
     * 验证客户端是否允许指定的授权类型
     *
     * @param clientId  客户端标识
     * @param grantType 授权类型
     * @return 是否允许
     */
    public boolean isGrantTypeAllowed(String clientId, String grantType) {
 // 先尝试通过 appid 查询
        SysAppId appId = getAppIdSafely(clientId);
        Set<String> allowedGrantTypes;
        if (appId != null) {
 // 获取 appid 允许的 grant 类型
            allowedGrantTypes = parseScopes(appId.getAppIdGrantTypes());
        } else {
 // 如果 appid 不存在，尝试通过 app键 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return false;
            }
 // 获取 app键 允许的 grant 类型
            allowedGrantTypes = parseScopes(appKey.getSysAppKeyGrantTypes());
        }
        if (allowedGrantTypes.isEmpty()) {
            allowedGrantTypes = DEFAULT_GRANT_TYPES;
        }

        return allowedGrantTypes.contains(grantType.toLowerCase());
    }

    /**
     * 验证回调地址
     *
     * @param clientId    客户端标识
     * @param redirectUri 回调地址
     * @return 是否有效
     */
    public boolean isRedirectUriValid(String clientId, String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            return false;
        }

 // 先尝试通过 appid 查询
        SysAppId appId = getAppIdSafely(clientId);
        String allowedUris;
        if (appId != null) {
            allowedUris = appId.getAppIdRedirectUris();
        } else {
 // 如果 appid 不存在，尝试通过 app键 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return false;
            }
            allowedUris = appKey.getSysAppKeyRedirectUris();
        }
        if (StringUtils.isBlank(allowedUris)) {
            // fail-closed：没配置回调地址就等于「不知道允许回哪」，不能默认放行，
            // 否则任何 client 都能把授权码带回自己的域名
            log.warn("客户端未配置回调地址限制，拒绝授权码回调: clientId={}", clientId);
            return false;
        }

        Set<String> uriSet = parseUriSet(allowedUris);
        for (String uri : uriSet) {
            // 精确匹配优先：早期用 startsWith，https://app/cb 会放行 https://app/cb.evil.com
            if (redirectUri.equals(uri)) {
                return true;
            }
            // 仅支持显式通配：以 /* 结尾表示允许该路径前缀下的地址
            if (uri.endsWith("/*")) {
                String prefix = uri.substring(0, uri.length() - 1);
                String remainder = redirectUri.startsWith(prefix) ? redirectUri.substring(prefix.length()) : null;
                if (remainder != null && !remainder.contains("..")) {
                    return true;
                }
            }
        }

        return false;
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成随机授权码
     * @return generate随机编码的结果
     */
    private String generateRandomCode() {
        byte[] bytes = new byte[AUTH_CODE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 解析地址列表（逗号或空白分隔）。
     *
     * <p>与 {@link #parseScopes(String)} 分开：scope 统一小写没问题，
     * 但回调地址的 path 大小写敏感，不能一起 lowerCase。</p>
     *
     * @param uriStr 原始配置
     * @return 地址集合（保持原始大小写）
     */
    private Set<String> parseUriSet(String uriStr) {
        if (StringUtils.isBlank(uriStr)) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String uri : uriStr.split("[,\\s]+")) {
            String trimmed = uri.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 解析 scope 字符串（逗号或空格分隔）
     * @param scopeStr scopeStr
     * @return 解析scopes的结果
     */
    private Set<String> parseScopes(String scopeStr) {
        if (StringUtils.isBlank(scopeStr)) {
            return Collections.emptySet();
        }
        String[] scopes = scopeStr.split("[,\\s]+");
        Set<String> result = new HashSet<>();
        for (String scope : scopes) {
            String trimmed = scope.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 安全查询 appid。
     * <p>部分部署仅启用 AppKey，数据库中不存在 app_id 表时应回退到 AppKey 流程，而不是直接抛 500。</p>
     * @param clientId 客户端id
     * @return 获取appidsafely的结果
     */
    private SysAppId getAppIdSafely(String clientId) {
        try {
            return sysAppIdService.getByAppIdValue(clientId);
        } catch (Exception e) {
            log.warn("查询AppId失败，回退到AppKey流程: clientId={}, error={}", clientId, e.getMessage());
            return null;
        }
    }

    /**
     * 遮蔽授权码（用于日志）
     * @param code 编码
     * @return mask编码的结果
     */
    private String maskCode(String code) {
        if (code == null || code.length() <= 8) {
            return "****";
        }
        return code.substring(0, 4) + "****" + code.substring(code.length() - 4);
    }

    // ==================== 内部类 ====================

    /**
     * 授权码数据
     *
     * <p>授权码消费成功后回传给上层的业务身份信息，来源是
     * {@link #generateAuthorizationCode(String, String, String, String, String, String, String, String)}
     * 写入 Redis 哈希的那份快照。本记录不参与任何协议校验，
     * {@code clientId} 已在 {@link #validateAndConsumeCode} 中比对通过。</p>
     *
     * @param userId   授权码对应的用户 ID，取自 Redis 字段 {@code user_id}；未登录态发码时为 null
     * @param username 授权码对应的用户名，取自 Redis 字段 {@code username}；未登录态发码时为 null
     * @param scope    发码时申请的权限范围原文，取自 Redis 字段 {@code scope}，多个 scope 以空格分隔；发码未指定 scope 时为空串，不会为 null
     * @param clientId 客户端标识，取自调用方入参（已与 Redis 中存的值比对一致）；非空
     */
    public record AuthorizationCodeData(
            String userId,
            String username,
            String scope,
            String clientId
    ) {}

    /**
     * Scope 验证结果
     *
     * <p>由 {@link #validateScope(String, String)} 返回的三元组：是否通过、失败原因、实际授予的 scope 集合。
     * 二者互斥——通过时 {@code errorMessage} 为 null 并携带授予集合，失败时携带错误文案且授予集合为空集。
     * 本记录不可变，授予集合在规范构造器中已做防御性拷贝。</p>
     *
     * @param valid         是否校验通过；由 {@link #success(Set)} 置 {@code true}、{@link #failure(String)} 置 {@code false}
     * @param errorMessage  失败原因文案（如「客户端不存在」「不允许的 scope: xxx」）；校验通过时为 null
     * @param grantedScopes 实际授予的 scope 集合，统一小写；不允许为 null（null 会让规范构造器立即抛异常），校验失败时为不可变空集
     */
    public record ScopeValidationResult(
            boolean valid,
            String errorMessage,
            Set<String> grantedScopes
    ) {
        /**
         * 规范构造器：对已授权 scope 集合做防御性拷贝。
         *
         * <p>value class 前置条件——集合组件必须深不可变；scope 解析过程已剔除 空 值，
         * 故可使用 {@code Set.copyOf}。</p>
         *
         * @param valid         是否校验通过
         * @param errorMessage  失败原因，通过时为 空
         * @param grantedScopes 已授权 scope 集合
         */
        public ScopeValidationResult {
            grantedScopes = Set.copyOf(Objects.requireNonNull(grantedScopes, "grantedScopes 不能为 null"));
        }

        /**
         * 成功
         *
         * @param scopes scopes
         * @return 成功的结果
         */
        public static ScopeValidationResult success(Set<String> scopes) {
            return new ScopeValidationResult(true, null, scopes);
        }

        /**
         * 失败
         *
         * @param message 消息
         * @return 失败的结果
         */
        public static ScopeValidationResult failure(String message) {
            return new ScopeValidationResult(false, message, Collections.emptySet());
        }
    }
}
