package com.chua.starter.system.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 登录扩展信息脱敏测试。
 *
 * <p>守护一次真实泄露：{@code SysUser} 转 Map 的键是 {@code sysUserPassword} /
 * {@code sysUserSalt} / {@code sysUserTotpCode}，按 {@code password}/{@code salt}
 * 删除不会命中，敏感值会随登录与 /verify 响应下发。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("LoginExtSanitizer: 凭据不随 ext 下发")
class LoginExtSanitizerTest {

    /**
     * 敏感键必须被剔除，业务键必须保留。
     */
    @Test
    @DisplayName("剔除密码/盐/TOTP/身份证，保留业务键")
    void shouldRemoveSensitiveKeysAndKeepBusinessKeys() {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("sysUserId", 1);
        ext.put("sysDeptId", 10);
        ext.put("sysUserUsername", "admin");
        ext.put("managedDeptTreeIds", List.of("1", "2"));
        ext.put("sysUserPassword", "HASH");
        ext.put("sysUserSalt", "SALT");
        ext.put("sysUserTotpCode", "TOTP_SECRET");
        ext.put("sysUserCard", "110101199001011234");
        ext.put("sysUserPasswordUpdateTime", "2026-01-01T00:00:00");

        LoginExtSanitizer.sanitize(ext);

        assertFalse(ext.containsKey("sysUserPassword"), "密码哈希不得下发");
        assertFalse(ext.containsKey("sysUserSalt"), "密码盐不得下发");
        assertFalse(ext.containsKey("sysUserTotpCode"), "TOTP 密钥不得下发");
        assertFalse(ext.containsKey("sysUserCard"), "身份证号不得下发");
        assertFalse(ext.containsKey("sysUserPasswordUpdateTime"));

        assertTrue(ext.containsKey("sysUserId"));
        assertTrue(ext.containsKey("sysDeptId"));
        assertTrue(ext.containsKey("sysUserUsername"));
        assertTrue(ext.containsKey("managedDeptTreeIds"));
    }

    /**
     * 大小写混写也要命中。
     */
    @Test
    @DisplayName("键名大小写不敏感")
    void shouldMatchIgnoreCase() {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("SysUserPassword", "HASH");
        ext.put("SYS_USER_SALT", "SALT");
        ext.put("usrTotpSecret", "SECRET");
        ext.put("keepMe", "ok");

        LoginExtSanitizer.sanitize(ext);

        assertTrue(ext.isEmpty() || ext.keySet().equals(java.util.Set.of("keepMe")), "敏感键应全部剔除");
        assertTrue(ext.containsKey("keepMe"));
    }

    /**
     * null / 空 Map 不应抛异常。
     */
    @Test
    @DisplayName("null 安全")
    void shouldBeNullSafe() {
        assertNull(LoginExtSanitizer.sanitize(null));
    }
}
