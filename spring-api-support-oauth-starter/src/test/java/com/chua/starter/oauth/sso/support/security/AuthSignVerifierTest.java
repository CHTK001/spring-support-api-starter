package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 请求签名校验收敛测试。
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("AuthSignVerifier: 签名开关与校验")
class AuthSignVerifierTest {

    /**
     * 关闭签名校验时直接放行。
     */
    @Test
    @DisplayName("signVerify=false: 放行")
    void shouldPassWhenDisabled() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setSignVerify(false);

        assertTrue(AuthSignVerifier.verify(properties, Map.of("a", "1"), "anything"));
    }

    /**
     * 开启时，正确签名通过。
     */
    @Test
    @DisplayName("signVerify=true: 正确签名通过")
    void shouldPassWithValidSign() {
        AuthServerProperties properties = new AuthServerProperties();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", "1");
        String sign = SignUtils.calculateMd5Sign(data);

        assertTrue(AuthSignVerifier.verify(properties, data, sign));
    }

    /**
     * 开启时，错误签名拒绝。
     */
    @Test
    @DisplayName("signVerify=true: 错误签名拒绝")
    void shouldRejectInvalidSign() {
        AuthServerProperties properties = new AuthServerProperties();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("a", "1");

        assertFalse(AuthSignVerifier.verify(properties, data, "deadbeef"));
    }

    /**
     * 配置为空时按「未启用校验」处理，避免 NPE。
     */
    @Test
    @DisplayName("properties=null: 放行")
    void shouldPassWhenPropertiesMissing() {
        assertTrue(AuthSignVerifier.verify(null, Map.of(), null));
    }
}
