package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.sso.support.generation.JwtTokenGeneration;
import com.chua.starter.oauth.sso.support.generation.TokenGeneration;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECPoint;
import java.util.*;

/**
 * JWKS (JSON Web Key Set) 端点
 * <p>
 * 提供公钥分发服务，供客户端验证 JWT 签名。
 * 符合 RFC 7517 (JSON Web Key) 规范。
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-18
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7517">RFC 7517 - JSON Web Key (JWK)</a>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "JWKS 服务", description = "提供 JWT 公钥分发服务")
@ConditionalOnProperty(name = "plugin.oauth.server.jwt.jwks-enabled", havingValue = "true")
public class JwksController {

    private final AuthServerProperties authServerProperties;

    /**
     * 获取 JWKS (JSON Web Key Set)
     * <p>
     * 返回服务器的公钥集合，供客户端验证 JWT 签名。
     * </p>
     *
     * @return JWKS JSON 对象
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "获取 JWKS", description = "返回 JWT 公钥集合，用于验证 JWT 签名")
    public Map<String, Object> getJwks() {
        log.debug("JWKS 请求");

        Map<String, Object> jwks = new LinkedHashMap<>();
        List<Map<String, Object>> keys = new ArrayList<>();

        try {
            JwtTokenGeneration jwtTokenGeneration = (JwtTokenGeneration) ServiceProvider.of(TokenGeneration.class)
                    .getExtension("jwt");

            if (jwtTokenGeneration == null) {
                log.warn("JWT Token 生成器未初始化");
                jwks.put("keys", keys);
                return jwks;
            }

            String algorithm = jwtTokenGeneration.getAlgorithm().toUpperCase();

            // 只有非对称加密算法才需要 JWKS
            if (algorithm.startsWith("RS") || algorithm.startsWith("ES")) {
                PublicKey publicKey = jwtTokenGeneration.getPublicKey();
                if (publicKey != null) {
                    Map<String, Object> jwk = buildJwk(publicKey, algorithm);
                    if (jwk != null) {
                        keys.add(jwk);
                    }
                }
            } else {
                log.info("当前使用对称加密算法 {}，JWKS 返回空密钥集", algorithm);
            }

        } catch (Exception e) {
            log.error("构建 JWKS 失败", e);
        }

        jwks.put("keys", keys);
        return jwks;
    }

    /**
     * 构建 JWK (JSON Web Key)
     */
    private Map<String, Object> buildJwk(PublicKey publicKey, String algorithm) {
        Map<String, Object> jwk = new LinkedHashMap<>();

        try {
            if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                // RSA 公钥
                jwk.put("kty", "RSA");
                jwk.put("alg", algorithm);
                jwk.put("use", "sig");
                jwk.put("kid", generateKeyId(publicKey));
                jwk.put("n", base64UrlEncode(rsaPublicKey.getModulus()));
                jwk.put("e", base64UrlEncode(rsaPublicKey.getPublicExponent()));

            } else if (publicKey instanceof ECPublicKey ecPublicKey) {
                // ECDSA 公钥
                jwk.put("kty", "EC");
                jwk.put("alg", algorithm);
                jwk.put("use", "sig");
                jwk.put("kid", generateKeyId(publicKey));
                jwk.put("crv", getCurve(algorithm));

                ECPoint ecPoint = ecPublicKey.getW();
                jwk.put("x", base64UrlEncode(ecPoint.getAffineX()));
                jwk.put("y", base64UrlEncode(ecPoint.getAffineY()));

            } else {
                log.warn("不支持的公钥类型: {}", publicKey.getClass().getName());
                return null;
            }

            return jwk;

        } catch (Exception e) {
            log.error("构建 JWK 失败", e);
            return null;
        }
    }

    /**
     * 生成 Key ID
     */
    private String generateKeyId(PublicKey publicKey) {
        try {
            byte[] encoded = publicKey.getEncoded();
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(encoded);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(Arrays.copyOf(digest, 8));
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Base64 URL 编码 BigInteger
     */
    private String base64UrlEncode(BigInteger value) {
        byte[] bytes = value.toByteArray();
        // 移除前导零
        if (bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 获取椭圆曲线名称
     */
    private String getCurve(String algorithm) {
        return switch (algorithm) {
            case "ES256" -> "P-256";
            case "ES384" -> "P-384";
            case "ES512" -> "P-521";
            default -> "P-256";
        };
    }
}
