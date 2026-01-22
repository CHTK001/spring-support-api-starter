package com.chua.starter.oauth.sso.support.generation;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 令牌生成器
 * <p>
 * 支持多种签名算法：HS256/HS384/HS512、RS256/RS384/RS512、ES256/ES384/ES512
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Extension("jwt")
public class JwtTokenGeneration implements TokenGeneration {

    @AutoInject
    private AuthServerProperties authServerProperties;

    private volatile SecretKey hmacKey;
    private volatile PrivateKey privateKey;
    private volatile PublicKey publicKey;
    private volatile boolean initialized = false;

    @Override
    public String generation(String key) {
        // 简单生成模式，返回 JWT ID
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 JWT Access Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param claims   自定义声明
     * @return JWT Token
     */
    public String generateAccessToken(String userId, String username, Map<String, Object> claims) {
        ensureInitialized();
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + jwtConfig.getExpireSeconds() * 1000);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("username", username)
                .claim("type", "access")
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration);

        // 添加受众
        if (StringUtils.isNotBlank(jwtConfig.getAudience())) {
            builder.audience().add(jwtConfig.getAudience());
        }

        // 添加自定义声明
        if (claims != null && !claims.isEmpty()) {
            claims.forEach(builder::claim);
        }

        // 根据算法签名
        return signToken(builder, jwtConfig.getAlgorithm());
    }

    /**
     * 生成 JWT Refresh Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(String userId, String username) {
        ensureInitialized();
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + jwtConfig.getRefreshExpireSeconds() * 1000);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim("username", username)
                .claim("type", "refresh")
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiration);

        return signToken(builder, jwtConfig.getAlgorithm());
    }

    /**
     * 根据算法签名 Token
     */
    private String signToken(io.jsonwebtoken.JwtBuilder builder, String algorithm) {
        String alg = algorithm.toUpperCase();

        if (alg.startsWith("HS")) {
            // HMAC 对称加密
            return builder.signWith(hmacKey, getHmacAlgorithm(alg)).compact();
        } else if (alg.startsWith("RS") || alg.startsWith("ES")) {
            // RSA/ECDSA 非对称加密
            if (privateKey == null) {
                throw new IllegalStateException("使用 " + alg + " 算法需要配置私钥");
            }
            return builder.signWith(privateKey, getAsymmetricAlgorithm(alg)).compact();
        } else {
            throw new IllegalArgumentException("不支持的签名算法: " + algorithm);
        }
    }

    /**
     * 获取 HMAC 算法
     */
    private io.jsonwebtoken.security.SecureDigestAlgorithm<SecretKey, SecretKey> getHmacAlgorithm(String alg) {
        return switch (alg) {
            case "HS256" -> Jwts.SIG.HS256;
            case "HS384" -> Jwts.SIG.HS384;
            case "HS512" -> Jwts.SIG.HS512;
            default -> Jwts.SIG.HS256;
        };
    }

    /**
     * 获取非对称算法
     */
    private io.jsonwebtoken.security.SignatureAlgorithm getAsymmetricAlgorithm(String alg) {
        return switch (alg) {
            case "RS256" -> Jwts.SIG.RS256;
            case "RS384" -> Jwts.SIG.RS384;
            case "RS512" -> Jwts.SIG.RS512;
            case "ES256" -> Jwts.SIG.ES256;
            case "ES384" -> Jwts.SIG.ES384;
            case "ES512" -> Jwts.SIG.ES512;
            default -> throw new IllegalArgumentException("不支持的非对称算法: " + alg);
        };
    }

    /**
     * 获取公钥（用于 JWKS 端点）
     */
    public PublicKey getPublicKey() {
        ensureInitialized();
        return publicKey;
    }

    /**
     * 获取 HMAC 密钥（用于验证）
     */
    public SecretKey getHmacKey() {
        ensureInitialized();
        return hmacKey;
    }

    /**
     * 获取签名算法
     */
    public String getAlgorithm() {
        return authServerProperties.getJwt().getAlgorithm();
    }

    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initialize();
                    initialized = true;
                }
            }
        }
    }

    /**
     * 初始化密钥
     */
    private void initialize() {
        AuthServerProperties.JwtConfig jwtConfig = authServerProperties.getJwt();
        String algorithm = jwtConfig.getAlgorithm().toUpperCase();

        try {
            if (algorithm.startsWith("HS")) {
                // 初始化 HMAC 密钥
                String secret = jwtConfig.getSecret();
                if (StringUtils.isBlank(secret) || secret.length() < 32) {
                    log.warn("【JWT】密钥长度不足 32 字节，建议使用更长的密钥");
                }
                hmacKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                log.info("【JWT】HMAC 密钥初始化完成，算法: {}", algorithm);

            } else if (algorithm.startsWith("RS")) {
                // 初始化 RSA 密钥对
                loadRsaKeys(jwtConfig);
                log.info("【JWT】RSA 密钥对初始化完成，算法: {}", algorithm);

            } else if (algorithm.startsWith("ES")) {
                // 初始化 ECDSA 密钥对
                loadEcdsaKeys(jwtConfig);
                log.info("【JWT】ECDSA 密钥对初始化完成，算法: {}", algorithm);
            }
        } catch (Exception e) {
            log.error("【JWT】密钥初始化失败", e);
            throw new RuntimeException("JWT 密钥初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载 RSA 密钥对
     */
    private void loadRsaKeys(AuthServerProperties.JwtConfig jwtConfig) throws Exception {
        String publicKeyPath = jwtConfig.getPublicKeyPath();
        String privateKeyPath = jwtConfig.getPrivateKeyPath();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        // 加载私钥
        if (StringUtils.isNotBlank(privateKeyPath)) {
            byte[] privateKeyBytes = loadKeyFile(privateKeyPath);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }

        // 加载公钥
        if (StringUtils.isNotBlank(publicKeyPath)) {
            byte[] publicKeyBytes = loadKeyFile(publicKeyPath);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        }
    }

    /**
     * 加载 ECDSA 密钥对
     */
    private void loadEcdsaKeys(AuthServerProperties.JwtConfig jwtConfig) throws Exception {
        String publicKeyPath = jwtConfig.getPublicKeyPath();
        String privateKeyPath = jwtConfig.getPrivateKeyPath();

        KeyFactory keyFactory = KeyFactory.getInstance("EC");

        // 加载私钥
        if (StringUtils.isNotBlank(privateKeyPath)) {
            byte[] privateKeyBytes = loadKeyFile(privateKeyPath);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }

        // 加载公钥
        if (StringUtils.isNotBlank(publicKeyPath)) {
            byte[] publicKeyBytes = loadKeyFile(publicKeyPath);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        }
    }

    /**
     * 加载密钥文件
     */
    private byte[] loadKeyFile(String path) throws Exception {
        InputStream inputStream;

        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IllegalArgumentException("找不到类路径资源: " + resourcePath);
            }
        } else {
            inputStream = Files.newInputStream(Paths.get(path));
        }

        try (inputStream) {
            String keyContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            // 移除 PEM 头尾和换行
            keyContent = keyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replace("-----BEGIN EC PRIVATE KEY-----", "")
                    .replace("-----END EC PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            return Base64.getDecoder().decode(keyContent);
        }
    }
}
