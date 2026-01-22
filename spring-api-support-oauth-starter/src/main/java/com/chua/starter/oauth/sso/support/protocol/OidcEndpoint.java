package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.sso.support.generation.JwtTokenGeneration;
import com.chua.starter.oauth.sso.support.generation.TokenGeneration;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

/**
 * OpenID Connect 端点
 * <p>
 * 提供 OIDC 发现和 JWKS 端点：
 * <ul>
 *   <li>/.well-known/openid-configuration - OIDC 发现端点</li>
 *   <li>/.well-known/jwks.json - JSON Web Key Set 端点</li>
 * </ul>
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-28
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "OIDC 端点", description = "OpenID Connect 发现和密钥端点")
public class OidcEndpoint {

    private final AuthServerProperties authServerProperties;

    /**
     * OpenID Connect 发现端点
     * <p>
     * 返回 OIDC 服务器的元数据配置，符合 OpenID Connect Discovery 1.0 规范。
     * </p>
     */
    @GetMapping(value = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "OIDC 发现端点", description = "返回 OpenID Connect 服务器配置")
    public Map<String, Object> openidConfiguration(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();

        Map<String, Object> config = new LinkedHashMap<>();

        // 基本信息
        config.put("issuer", StringUtils.isNotBlank(jwtConfig.getIssuer()) ? jwtConfig.getIssuer() : baseUrl);
        
        // 端点
        config.put("authorization_endpoint", baseUrl + "/oauth2/authorize");
        config.put("token_endpoint", baseUrl + "/oauth2/token");
        config.put("userinfo_endpoint", baseUrl + "/oauth2/userinfo");
        config.put("revocation_endpoint", baseUrl + "/oauth2/revoke");
        config.put("introspection_endpoint", baseUrl + "/oauth2/introspect");
        
        // JWKS 端点
        if (jwtConfig.isJwksEnabled() || isAsymmetricAlgorithm(jwtConfig.getAlgorithm())) {
            config.put("jwks_uri", baseUrl + "/.well-known/jwks.json");
        }

        // 支持的响应类型
        config.put("response_types_supported", Arrays.asList("code", "token"));

        // 支持的授权类型
        config.put("grant_types_supported", Arrays.asList(
                "authorization_code",
                "client_credentials",
                "password",
                "refresh_token"
        ));

        // 支持的主题标识符类型
        config.put("subject_types_supported", Collections.singletonList("public"));

        // 支持的 ID Token 签名算法
        config.put("id_token_signing_alg_values_supported", 
                Collections.singletonList(jwtConfig.getAlgorithm()));

        // 支持的 scope
        config.put("scopes_supported", Arrays.asList(
                "openid", "profile", "email", "read", "write", "admin"
        ));

        // 支持的令牌端点认证方法
        config.put("token_endpoint_auth_methods_supported", Arrays.asList(
                "client_secret_basic",
                "client_secret_post"
        ));

        // 支持的 claim
        config.put("claims_supported", Arrays.asList(
                "sub", "iss", "aud", "exp", "iat", "name", "preferred_username", "email"
        ));

        // PKCE 支持
        config.put("code_challenge_methods_supported", Arrays.asList("S256", "plain"));

        // 其他配置
        config.put("response_modes_supported", Arrays.asList("query", "fragment"));
        config.put("request_parameter_supported", false);
        config.put("request_uri_parameter_supported", false);

        log.debug("返回 OIDC 发现配置: issuer={}", config.get("issuer"));
        return config;
    }

    /**
     * JSON Web Key Set 端点
     * <p>
     * 返回用于验证 JWT 签名的公钥集合。
     * 仅当使用非对称算法（RS256/ES256 等）时有效。
     * </p>
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "JWKS 端点", description = "返回 JSON Web Key Set")
    public Map<String, Object> jwks() {
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();
        Map<String, Object> jwks = new LinkedHashMap<>();
        List<Map<String, Object>> keys = new ArrayList<>();

        String algorithm = jwtConfig.getAlgorithm().toUpperCase();

        // 只有非对称算法才返回公钥
        if (isAsymmetricAlgorithm(algorithm)) {
            try {
                TokenGeneration tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension("jwt");
                if (tokenGeneration instanceof JwtTokenGeneration jwtTokenGeneration) {
                    PublicKey publicKey = jwtTokenGeneration.getPublicKey();
                    if (publicKey instanceof RSAPublicKey rsaPublicKey) {
                        keys.add(buildRsaJwk(rsaPublicKey, algorithm));
                    }
                    // TODO: 支持 ECDSA 公钥
                }
            } catch (Exception e) {
                log.warn("获取 JWT 公钥失败: {}", e.getMessage());
            }
        }

        jwks.put("keys", keys);
        log.debug("返回 JWKS: keyCount={}", keys.size());
        return jwks;
    }

    /**
     * 构建 RSA JWK
     */
    private Map<String, Object> buildRsaJwk(RSAPublicKey publicKey, String algorithm) {
        Map<String, Object> jwk = new LinkedHashMap<>();
        
        jwk.put("kty", "RSA");
        jwk.put("alg", algorithm);
        jwk.put("use", "sig");
        jwk.put("kid", generateKeyId(publicKey));
        
        // RSA 公钥参数
        jwk.put("n", base64UrlEncode(publicKey.getModulus()));
        jwk.put("e", base64UrlEncode(publicKey.getPublicExponent()));

        return jwk;
    }

    /**
     * 生成密钥 ID
     */
    private String generateKeyId(PublicKey publicKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getEncoded());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 8);
        } catch (Exception e) {
            return "default-key-id";
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
     * 判断是否是非对称算法
     */
    private boolean isAsymmetricAlgorithm(String algorithm) {
        String alg = algorithm.toUpperCase();
        return alg.startsWith("RS") || alg.startsWith("ES") || alg.startsWith("PS");
    }

    /**
     * 获取基础 URL
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}
