package com.chua.starter.oauth.sso.support.core;

import com.chua.common.support.core.utils.StringUtils;
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
 * @author CH
 * @version 1.0.0
 * @since 2025-12-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2CoreService {

    private final SysAppKeyService sysAppKeyService;
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
     * 默认允许的所有 scope
     */
    private static final Set<String> DEFAULT_SCOPES = Set.of("read", "write", "admin", "openid", "profile", "email");

    /**
     * 默认允许的所有 grant_type
     */
    private static final Set<String> DEFAULT_GRANT_TYPES = Set.of(
            "client_credentials", "password", "authorization_code", "refresh_token"
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    // ==================== 授权码管理 ====================

    /**
     * 生成授权码
     *
     * @param clientId            客户端ID
     * @param redirectUri         回调地址
     * @param scope               请求的权限范围
     * @param state               状态码
     * @param codeChallenge       PKCE code_challenge
     * @param codeChallengeMethod PKCE 方法（S256 或 plain）
     * @param userId              用户ID
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
     * @param clientId     客户端ID
     * @param redirectUri  回调地址
     * @param codeVerifier PKCE code_verifier
     * @return 授权码数据，验证失败返回 null
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

        // 验证 client_id
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
     * @param codeVerifier  code_verifier
     * @param codeChallenge code_challenge
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
     * 计算 S256 code_challenge
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
     * @param clientId       客户端ID
     * @param requestedScope 请求的 scope
     * @return 验证结果
     */
    public ScopeValidationResult validateScope(String clientId, String requestedScope) {
        // 先尝试通过 AppId 查询
        SysAppId appId = sysAppIdService.getByAppIdValue(clientId);
        Set<String> allowedScopes;
        if (appId != null) {
            // 获取 AppId 允许的 scope
            allowedScopes = parseScopes(appId.getAppIdScope());
        } else {
            // 如果 AppId 不存在，尝试通过 AppKey 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return ScopeValidationResult.failure("客户端不存在");
            }
            // 获取 AppKey 允许的 scope
            allowedScopes = parseScopes(appKey.getSysAppKeyScope());
        }
        if (allowedScopes.isEmpty()) {
            allowedScopes = DEFAULT_SCOPES;
        }

        // 解析请求的 scope
        Set<String> requestedScopes = parseScopes(requestedScope);
        if (requestedScopes.isEmpty()) {
            // 未指定 scope，使用默认的 read
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
     * @param clientId  客户端ID
     * @param grantType 授权类型
     * @return 是否允许
     */
    public boolean isGrantTypeAllowed(String clientId, String grantType) {
        // 先尝试通过 AppId 查询
        SysAppId appId = sysAppIdService.getByAppIdValue(clientId);
        Set<String> allowedGrantTypes;
        if (appId != null) {
            // 获取 AppId 允许的 grant types
            allowedGrantTypes = parseScopes(appId.getAppIdGrantTypes());
        } else {
            // 如果 AppId 不存在，尝试通过 AppKey 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return false;
            }
            // 获取 AppKey 允许的 grant types
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
     * @param clientId    客户端ID
     * @param redirectUri 回调地址
     * @return 是否有效
     */
    public boolean isRedirectUriValid(String clientId, String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            return false;
        }

        // 先尝试通过 AppId 查询
        SysAppId appId = sysAppIdService.getByAppIdValue(clientId);
        String allowedUris;
        if (appId != null) {
            allowedUris = appId.getAppIdRedirectUris();
        } else {
            // 如果 AppId 不存在，尝试通过 AppKey 查询
            SysAppKey appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                return false;
            }
            allowedUris = appKey.getSysAppKeyRedirectUris();
        }
        if (StringUtils.isBlank(allowedUris)) {
            // 未配置回调地址限制，默认允许（兼容旧客户端）
            log.warn("客户端未配置回调地址限制: clientId={}", clientId);
            return true;
        }

        Set<String> uriSet = parseScopes(allowedUris);
        for (String uri : uriSet) {
            if (redirectUri.equals(uri) || redirectUri.startsWith(uri)) {
                return true;
            }
        }

        return false;
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成随机授权码
     */
    private String generateRandomCode() {
        byte[] bytes = new byte[AUTH_CODE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 解析 scope 字符串（逗号或空格分隔）
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
     * 遮蔽授权码（用于日志）
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
     */
    public record AuthorizationCodeData(
            String userId,
            String username,
            String scope,
            String clientId
    ) {}

    /**
     * Scope 验证结果
     */
    public record ScopeValidationResult(
            boolean valid,
            String errorMessage,
            Set<String> grantedScopes
    ) {
        public static ScopeValidationResult success(Set<String> scopes) {
            return new ScopeValidationResult(true, null, scopes);
        }

        public static ScopeValidationResult failure(String message) {
            return new ScopeValidationResult(false, message, Collections.emptySet());
        }
    }
}
