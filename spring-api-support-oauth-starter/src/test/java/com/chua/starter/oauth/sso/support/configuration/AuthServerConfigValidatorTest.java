package com.chua.starter.oauth.sso.support.configuration;

import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AuthServerConfigValidator} 回归测试
 *
 * <p>守护 P0-4：此前 {@code jwt.secret} 完全没有校验，出厂字面量
 * {@code your-256-bit-secret-key-here-32b} 直接生效；且 serviceKey 命中危险默认值时
 * 只在 prod profile 下才算错误，dev profile 仅仅打一条 warning。
 * HS256 是对称算法，密钥公开 = 任何人可离线伪造有效 JWT。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("AuthServerConfigValidator: 密钥 fail-fast")
class AuthServerConfigValidatorTest {

    /**
     * 服务端配置
     */
    private AuthServerProperties properties;

    /**
     * 环境
     */
    private MockEnvironment environment;

    /**
     * 一条足够长的随机密钥
     */
    private static final String GOOD_SECRET = "9f2b7c1d4e6a8f0b3d5c7e9a1b3d5f70c9e2a4b6d8f0c2e4a6b8d0f2c4e6a8b0d";

    @BeforeEach
    void setUp() {
        properties = new AuthServerProperties();
        // 一个整体可通过的基线：只有密钥相关的断言会被单独覆盖
        properties.setServiceKey(GOOD_SECRET);
        properties.getJwt().setSecret(GOOD_SECRET);
        properties.getJwt().setAlgorithm("HS256");
        environment = new MockEnvironment();
    }

    /**
     * 执行校验
     */
    private void validate() {
        new AuthServerConfigValidator(properties, environment).validateConfiguration();
    }

    @Test
    @DisplayName("出厂 JWT 密钥必须被拒绝（dev profile 下同样拒绝）")
    void shippedJwtSecretIsRejectedInEveryProfile() {
        properties.getJwt().setSecret("your-256-bit-secret-key-here-32b");
        environment.setActiveProfiles("dev");

        IllegalStateException e = assertThrows(IllegalStateException.class, this::validate,
                "出厂 JWT 密钥在任何 profile 下都必须阻止启动");
        assertTrue(e.getMessage().contains("jwt.secret"), "报错需指明配置项: " + e.getMessage());
    }

    @Test
    @DisplayName("JWT 密钥为空必须被拒绝")
    void blankJwtSecretIsRejected() {
        properties.getJwt().setSecret(null);
        assertThrows(IllegalStateException.class, this::validate);
    }

    @Test
    @DisplayName("JWT 密钥短于 32 字节必须被拒绝")
    void shortJwtSecretIsRejected() {
        properties.getJwt().setSecret("too-short-secret");
        assertThrows(IllegalStateException.class, this::validate);
    }

    @Test
    @DisplayName("弱口令 JWT 密钥必须被拒绝")
    void weakJwtSecretIsRejected() {
        properties.getJwt().setSecret("password");
        assertThrows(IllegalStateException.class, this::validate);
    }

    @Test
    @DisplayName("出厂 serviceKey 在 dev profile 下也必须被拒绝")
    void shippedServiceKeyIsRejectedInDevProfile() {
        properties.setServiceKey("1234567890oOlIi0");
        environment.setActiveProfiles("dev");

        assertThrows(IllegalStateException.class, this::validate,
                "使用出厂主密钥与 profile 无关，必须一律拒绝");
    }

    @Test
    @DisplayName("serviceKey 为空必须被拒绝")
    void blankServiceKeyIsRejected() {
        properties.setServiceKey(null);
        assertThrows(IllegalStateException.class, this::validate);
    }

    @Test
    @DisplayName("合法随机密钥可通过校验")
    void validRandomSecretsPass() {
        assertDoesNotThrow(this::validate);
    }

    @Test
    @DisplayName("非 HMAC 算法改用公私钥文件，不要求 secret")
    void asymmetricAlgorithmSkipsSecretCheck() {
        properties.getJwt().setAlgorithm("RS256");
        properties.getJwt().setSecret(null);
        // RS256 分支跳过 secret 校验；其余项仍需满足，此处基线已满足
        assertDoesNotThrow(this::validate);
    }

    @Test
    @DisplayName("JWT 关闭时不校验其密钥")
    void disabledJwtSkipsSecretCheck() {
        properties.getJwt().setEnabled(false);
        properties.getJwt().setSecret(null);
        assertDoesNotThrow(this::validate);
    }
}