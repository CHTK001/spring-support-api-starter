package com.chua.starter.oauth.sso.support.configuration;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OAuth服务端配置校验器
 * <p>
 * 在应用启动时校验关键配置项，确保生产环境安全。
 * 校验失败时抛出异常阻止应用启动。
 * </p>
 *
 * @since 2024/12/07
 * @author CH
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthServerConfigValidator {

    /**
     * 认证服务器属性
    */
    private final AuthServerProperties authServerProperties;
    /**
     * 环境
    */
    private final Environment environment;

    /**
     * 危险的默认密钥列表（不允许在生产环境使用）
     */
    private static final List<String> DANGEROUS_KEYS = List.of(
            "D518E462DF7B36828FA68CCD69FC6140",
            "1234567890oOlIi0", // 认证服务端属性默认值
            "your-256-bit-secret-key-here-32b",  // JWT默认密钥
            "123456",
            "password",
            "secret",
            "default",
            "test",
            "demo"
    );

    /**
     * 密钥最小长度
     */
    private static final int MIN_KEY_LENGTH = 16;

    /**
     * HMAC 系 JWT 密钥最小字节数
     *
     * <p>HS256 的名义输出是 256 位，密钥短于该长度只会降低强度而不会报错，
     * 所以这里直接按 32 字节卡死。</p>
     */
    private static final int MIN_JWT_SECRET_BYTES = 32;

    /**
     * 启动时执行配置校验
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("========== OAuth服务端配置校验开始 ==========");
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // 获取当前环境
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProduction = isProductionEnvironment(activeProfiles);
        
        log.info("【配置校验】当前环境: {}", String.join(",", activeProfiles));

        // 1. 校验服务密钥
        validateServiceKey(errors, warnings, isProduction);
        // 1b. 校验 JWT 签名密钥（此前完全没有校验，是最致命的一处硬编码）
        validateJwtSecret(errors, warnings, isProduction);
        
 // 2. 校验令牌过期时间
        validateTokenExpire(errors, warnings);
        
        // 3. 校验协议配置
        validateProtocols(errors, warnings);
        
        // 4. 校验加密配置
        validateEncryption(errors, warnings);

        // 输出警告
        if (!warnings.isEmpty()) {
            log.warn("========== 配置警告 ==========");
            warnings.forEach(w -> log.warn("⚠️  {}", w));
        }

        // 输出错误并阻止启动
        if (!errors.isEmpty()) {
            log.error("========== 配置错误 ==========");
            errors.forEach(e -> log.error("❌ {}", e));
            throw new IllegalStateException(
                    "【安全警告】OAuth服务端配置校验失败，应用启动被阻止！\n错误列表:\n- " 
                    + String.join("\n- ", errors));
        }

        log.info("========== OAuth服务端配置校验通过 ✅ ==========");
    }

    /**
     * 校验服务密钥
     * @param errors 错误
     * @param warnings 警告
     * @param isProduction 是否production
     */
    private void validateServiceKey(List<String> errors, List<String> warnings, boolean isProduction) {
        String serviceKey = authServerProperties.getServiceKey();
        
        if (StringUtils.isBlank(serviceKey)) {
            errors.add("plugin.oauth.server.service-key 必须配置！请设置一个安全的随机密钥。");
            return;
        }

        // 检查是否使用危险的默认密钥
        // <b>修复</b>：原实现仅在 prod/profile 名命中时才算错误，其它 profile 只打一条 warning。
        // 但「用了出厂密钥」这件事与跑在哪个 profile 无关 —— 仓库里的默认值对所有人公开，
        // 攻击者不需要知道你的 profile 就能用它签令牌。故一律视为错误。
        if (DANGEROUS_KEYS.stream().anyMatch(k -> k.equalsIgnoreCase(serviceKey))) {
            errors.add("plugin.oauth.server.service-key 使用了代码中已知的出厂默认值！"
                    + "该值随源码公开，任何人都可伪造令牌。请立即更换为随机串（openssl rand -base64 48）。");
        }

        // 检查密钥长度
        if (serviceKey.length() < MIN_KEY_LENGTH) {
            if (isProduction) {
                errors.add("plugin.oauth.server.service-key 长度必须至少 " + MIN_KEY_LENGTH + " 位！当前长度: " + serviceKey.length());
            } else {
                warnings.add("plugin.oauth.server.service-key 长度建议至少 " + MIN_KEY_LENGTH + " 位。当前长度: " + serviceKey.length());
            }
        }

        log.info("【配置校验】服务密钥: 已配置, 长度: {}", serviceKey.length());
    }

    /**
     * 校验 JWT 签名密钥
     *
     * <p><b>修复一个致命缺陷</b>：本方法此前<b>完全不存在</b>。也就是说
     * {@code plugin.oauth.server.jwt.secret} 一直是代码里的出厂字面量
     * {@code your-256-bit-secret-key-here-32b}，且校验器只检查了 serviceKey。
     * 后果：HS256 是对称算法，签名密钥公开 = 任何人都能在本地离线伪造出
     * 任意用户、任意权限的有效 JWT，认证体系直接失效，且无需接触服务器。</p>
     *
     * <p>HMAC 系算法（HS256/384/512）必须提供独立密钥，且：</p>
     * <ul>
     *   <li>为空 → 错误（无密钥即无法安全签发）；</li>
     *   <li>命中出厂默认 / 弱口令 → 一律错误（与 profile 无关）；</li>
     *   <li>长度不足 32 字节 → 错误（HS256 的强度下限）。</li>
     * </ul>
     * <p>RSA/ECDSA 系算法改用公私钥文件，不读本项，跳过。</p>
     *
     * @param errors       错误收集
     * @param warnings     警告收集
     * @param isProduction 是否生产环境（仅影响警告措辞）
     */
    private void validateJwtSecret(List<String> errors, List<String> warnings, boolean isProduction) {
        AuthServerProperties.JwtConfig jwt = authServerProperties.getJwt();
        if (jwt == null || !jwt.isEnabled()) {
            log.info("【配置校验】JWT 未启用, 跳过密钥校验");
            return;
        }
        String algorithm = StringUtils.defaultString(jwt.getAlgorithm(), "HS256").toUpperCase();
        if (!algorithm.startsWith("HS")) {
            log.info("【配置校验】JWT 算法为 {} (非 HMAC), 使用公私钥文件, 跳过 secret 校验", algorithm);
            return;
        }
        String secret = jwt.getSecret();
        if (StringUtils.isBlank(secret)) {
            errors.add("plugin.oauth.server.jwt.secret 必须配置！当前算法 " + algorithm
                    + " 为对称加密, 密钥缺失将无法安全签发令牌。请设置随机串(建议 openssl rand -base64 48)。");
            return;
        }
        if (DANGEROUS_KEYS.stream().anyMatch(k -> k.equalsIgnoreCase(secret))) {
            errors.add("plugin.oauth.server.jwt.secret 使用了代码中已知的出厂默认值！"
                    + "HS256 是对称算法, 该值随源码公开 = 任何人都能离线伪造有效 JWT。请立即更换。");
            return;
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_JWT_SECRET_BYTES) {
            errors.add("plugin.oauth.server.jwt.secret 至少需要 " + MIN_JWT_SECRET_BYTES
                    + " 字节 (当前 " + secret.getBytes(StandardCharsets.UTF_8).length + " 字节)。");
            return;
        }
        if (secret.indexOf(' ') >= 0) {
            warnings.add("plugin.oauth.server.jwt.secret 含空格, 请确认这是有意为之而非复制粘贴引入。");
        }
        log.info("【配置校验】JWT 算法: {}, 密钥长度: {} 字节", algorithm,
                secret.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * 校验令牌过期时间
     * @param errors 错误
     * @param warnings 警告
     */
    private void validateTokenExpire(List<String> errors, List<String> warnings) {
        long expire = authServerProperties.getExpire();
        long refreshExpire = authServerProperties.getRefreshExpire();
        
        if (expire <= 0) {
            errors.add("plugin.oauth.server.expire 必须大于0！当前值: " + expire);
        } else if (expire > 86400 * 30) {
            warnings.add("plugin.oauth.server.expire 设置过长（超过30天），建议缩短Token有效期。当前值: " + expire + "秒");
        }

        if (refreshExpire <= expire) {
            warnings.add("plugin.oauth.server.refresh-expire 应大于 expire。refresh-expire=" + refreshExpire + ", expire=" + expire);
        }

        log.info("【配置校验】Token过期时间: {}秒, 刷新Token过期时间: {}秒", expire, refreshExpire);
    }

    /**
     * 校验协议配置
     * @param errors 错误
     * @param warnings 警告
     */
    private void validateProtocols(List<String> errors, List<String> warnings) {
        // 读「实际启用的协议」而不是 config.protocols：后者只被已删除的 *OauthProtocol
        // 读取（其依赖的 Protocol SPI 全仓零实现），不再影响任何监听行为。
        // 若真没配任何协议，应当在 protocol-server 那一层报错，而不是报一个不生效的字段。
        List<String> protocols = authServerProperties.resolveEnabledProtocols();

        if (protocols == null || protocols.isEmpty()) {
            errors.add("plugin.oauth.protocol-server.protocols 必须至少启用一个协议！");
            return;
        }

        log.info("【配置校验】已启用协议: {}", protocols);
    }

    /**
     * 校验加密配置
     * @param errors 错误
     * @param warnings 警告
     */
    private void validateEncryption(List<String> errors, List<String> warnings) {
        String encryption = authServerProperties.getEncryption();
        
        if (StringUtils.isBlank(encryption)) {
            warnings.add("plugin.oauth.server.encryption 未配置加密算法，将使用默认值");
        } else {
            log.info("【配置校验】加密算法: {}", encryption);
        }
    }

    /**
     * 判断是否为生产环境
     * @param activeProfiles 活跃配置文件
     * @return 是否production环境的结果
     */
    private boolean isProductionEnvironment(String[] activeProfiles) {
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile) || 
                "production".equalsIgnoreCase(profile) ||
                "prd".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}
