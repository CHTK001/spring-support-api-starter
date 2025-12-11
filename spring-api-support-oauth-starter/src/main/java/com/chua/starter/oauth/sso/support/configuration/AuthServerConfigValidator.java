package com.chua.starter.oauth.sso.support.configuration;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth服务端配置校验器
 * <p>
 * 在应用启动时校验关键配置项，确保生产环境安全。
 * 校验失败时抛出异常阻止应用启动。
 * </p>
 *
 * @author CH
 * @since 2024/12/07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthServerConfigValidator {

    private final AuthServerProperties authServerProperties;
    private final Environment environment;

    /**
     * 危险的默认密钥列表（不允许在生产环境使用）
     */
    private static final List<String> DANGEROUS_KEYS = List.of(
            "D518E462DF7B36828FA68CCD69FC6140",
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
        
        // 2. 校验Token过期时间
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
     */
    private void validateServiceKey(List<String> errors, List<String> warnings, boolean isProduction) {
        String serviceKey = authServerProperties.getServiceKey();
        
        if (StringUtils.isBlank(serviceKey)) {
            errors.add("plugin.oauth.server.service-key 必须配置！请设置一个安全的随机密钥。");
            return;
        }

        // 检查是否使用危险的默认密钥
        if (DANGEROUS_KEYS.stream().anyMatch(k -> k.equalsIgnoreCase(serviceKey))) {
            if (isProduction) {
                errors.add("plugin.oauth.server.service-key 使用了不安全的默认值！生产环境禁止使用默认密钥。");
            } else {
                warnings.add("plugin.oauth.server.service-key 使用了不安全的默认值，请在生产环境前修改。");
            }
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
     * 校验Token过期时间
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
     */
    private void validateProtocols(List<String> errors, List<String> warnings) {
        List<String> protocols = authServerProperties.getProtocols();
        
        if (protocols == null || protocols.isEmpty()) {
            errors.add("plugin.oauth.server.protocols 必须至少配置一个协议！");
            return;
        }

        log.info("【配置校验】已启用协议: {}", protocols);

        // 检查gRPC TLS配置
        if (protocols.contains("grpc") && !authServerProperties.isGrpcEnableTls()) {
            warnings.add("gRPC协议未启用TLS加密，生产环境建议开启 plugin.oauth.server.grpc-enable-tls=true");
        }
    }

    /**
     * 校验加密配置
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
