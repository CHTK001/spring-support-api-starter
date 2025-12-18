package com.chua.starter.oauth.sso.support.parser.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.generation.JwtTokenGeneration;
import com.chua.starter.oauth.sso.support.generation.TokenGeneration;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.SecretKey;
import java.security.PublicKey;

/**
 * JWT 认证处理器
 * <p>
 * 处理 JWT Token 的验证和解析，支持：
 * <ul>
 *     <li>HS256/HS384/HS512 对称加密算法</li>
 *     <li>RS256/RS384/RS512 RSA 非对称加密算法</li>
 *     <li>ES256/ES384/ES512 ECDSA 非对称加密算法</li>
 * </ul>
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Spi("jwt")
public class JwtAuthorizationHandler implements AuthorizationHandler {

    /**
     * JWT Token 前缀
     */
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public ReturnResult<String> authenticate(OauthAuthInformation authInformation,
                                            String token,
                                            String cookie,
                                            String userCode) {
        log.info("使用 JWT 认证方式");

        AuthServerProperties authServerProperties = authInformation.getAuthServerProperties();
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();

        if (!jwtConfig.isEnabled()) {
            log.warn("JWT 认证未启用");
            return ReturnResult.error("JWT 认证未启用");
        }

        // 移除 Bearer 前缀
        String jwtToken = extractToken(token);
        if (StringUtils.isBlank(jwtToken)) {
            return ReturnResult.error("Token 不能为空");
        }

        try {
            // 如果启用了 Redis 存储，先检查 Token 是否在黑名单中
            if (jwtConfig.isStoreInRedis()) {
                if (isTokenBlacklisted(authInformation, jwtToken)) {
                    log.warn("JWT Token 已被撤销");
                    return ReturnResult.error("Token 已被撤销");
                }
            }

            // 解析并验证 JWT
            Claims claims = parseAndValidateJwt(jwtToken, jwtConfig);

            // 验证 Token 类型
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("JWT Token 类型错误: {}", tokenType);
                return ReturnResult.error("Token 类型错误");
            }

            // 构建用户信息
            UserResume userResume = buildUserResume(claims, jwtToken);

            String json = Json.toJson(userResume);
            log.info("JWT 认证成功: userId={}, username={}", 
                    userResume.getUserId(), userResume.getUsername());
            
            return ReturnResult.ok(authInformation.encodeHex(json));

        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期: {}", e.getMessage());
            return ReturnResult.error("Token 已过期");
        } catch (JwtException e) {
            log.warn("JWT Token 验证失败: {}", e.getMessage());
            return ReturnResult.error("Token 验证失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("JWT 认证异常", e);
            return ReturnResult.error("认证失败: " + e.getMessage());
        }
    }

    /**
     * 提取 Token（移除 Bearer 前缀）
     */
    private String extractToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }

    /**
     * 解析并验证 JWT
     */
    private Claims parseAndValidateJwt(String jwtToken, AuthServerProperties.JwtConfig jwtConfig) {
        String algorithm = jwtConfig.getAlgorithm().toUpperCase();

        // 获取 JwtTokenGeneration 来获取密钥
        JwtTokenGeneration jwtTokenGeneration = (JwtTokenGeneration) ServiceProvider.of(TokenGeneration.class)
                .getExtension("jwt");

        if (jwtTokenGeneration == null) {
            throw new IllegalStateException("JWT Token 生成器未初始化");
        }

        var parserBuilder = Jwts.parser();

        // 根据算法选择验证方式
        if (algorithm.startsWith("HS")) {
            // HMAC 对称加密
            SecretKey hmacKey = jwtTokenGeneration.getHmacKey();
            parserBuilder.verifyWith(hmacKey);
        } else if (algorithm.startsWith("RS") || algorithm.startsWith("ES")) {
            // RSA/ECDSA 非对称加密
            PublicKey publicKey = jwtTokenGeneration.getPublicKey();
            if (publicKey == null) {
                throw new IllegalStateException("RSA/ECDSA 公钥未配置");
            }
            parserBuilder.verifyWith(publicKey);
        } else {
            throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }

        // 验证签发者
        if (StringUtils.isNotBlank(jwtConfig.getIssuer())) {
            parserBuilder.requireIssuer(jwtConfig.getIssuer());
        }

        return parserBuilder.build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    /**
     * 构建用户信息
     */
    private UserResume buildUserResume(Claims claims, String token) {
        UserResume userResume = new UserResume();
        
        // 基本信息
        userResume.setUserId(claims.getSubject());
        userResume.setUsername(claims.get("username", String.class));

        // 可选信息
        if (claims.containsKey("nickName")) {
            userResume.setNickName(claims.get("nickName", String.class));
        }
        if (claims.containsKey("tenantId")) {
            userResume.setTenantId(claims.get("tenantId", String.class));
        }
        if (claims.containsKey("deptId")) {
            userResume.setDeptId(claims.get("deptId", String.class));
        }

        // 计算剩余过期时间
        if (claims.getExpiration() != null) {
            long expireTime = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            userResume.setExpireTime(Math.max(0, expireTime));
        }

        return userResume;
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    @SuppressWarnings("unchecked")
    private boolean isTokenBlacklisted(OauthAuthInformation authInformation, String token) {
        try {
            RedisTemplate<String, Object> redisTemplate = authInformation.getApplicationContext()
                    .getBean("stringRedisTemplate", RedisTemplate.class);
            
            String blacklistKey = "jwt:blacklist:" + token;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("检查 JWT 黑名单失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supports(String subProtocol, String token, String userCode) {
        // JWT 认证条件：
        // 1. subProtocol 为 "jwt"
        // 2. 或者 token 以 "Bearer " 开头
        // 3. 或者 token 是 JWT 格式（三段式，以 . 分隔）
        if ("jwt".equalsIgnoreCase(subProtocol)) {
            return true;
        }
        
        if (StringUtils.isNotBlank(token)) {
            // 检查 Bearer 前缀
            if (token.startsWith(BEARER_PREFIX)) {
                return true;
            }
            // 检查 JWT 格式（三段式）
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                return true;
            }
        }
        
        return false;
    }
}
