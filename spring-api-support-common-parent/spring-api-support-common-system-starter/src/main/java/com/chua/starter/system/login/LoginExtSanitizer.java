package com.chua.starter.system.login;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 登录用户扩展信息脱敏。
 *
 * <p>{@code AbstractUserLoginService#loginInfo} 会把整个 {@code SysUser} 实体转成 Map
 * 塞进 {@code UserResume.ext}，这份 ext 会随登录/鉴权响应下发给客户端、写入 Redis、
 * 并出现在每次 {@code /verify} 的返回里。实体属性名是 {@code sysUserPassword}
 * 这类带前缀的写法，早期代码里 {@code ext.remove("password")} / {@code ext.remove("salt")}
 * 一个都命中不了，导致密码哈希、盐、TOTP 密钥、身份证号被一并下发。</p>
 *
 * <p>这里按「键名包含敏感词」统一剔除，避免字段改名后再次漏掉。只保留业务真正需要的键
 * （{@code sysUserId}、{@code sysDeptId}、{@code managedDeptTreeIds}、{@code authVersion} 等）。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public final class LoginExtSanitizer {

    /**
     * 键名中包含这些片段即视为敏感，整键剔除
     */
    private static final List<String> SENSITIVE_KEY_FRAGMENTS = List.of(
            "password", "passwd", "salt", "totp", "secret", "credential", "card"
    );

    /**
     * 工具类禁止实例化
     */
    private LoginExtSanitizer() {
    }

    /**
     * 就地剔除敏感键。
     *
     * @param ext 扩展信息，允许为 {@code null}
     * @return 同一个 Map 实例，便于链式使用
     */
    public static Map<String, Object> sanitize(Map<String, Object> ext) {
        if (ext == null || ext.isEmpty()) {
            return ext;
        }
        ext.keySet().removeIf(LoginExtSanitizer::isSensitiveKey);
        return ext;
    }

    /**
     * 判断键名是否敏感。
     *
     * @param key 键名，允许为 {@code null}
     * @return 敏感返回 {@code true}
     */
    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        for (String fragment : SENSITIVE_KEY_FRAGMENTS) {
            if (normalized.contains(fragment)) {
                return true;
            }
        }
        return false;
    }
}
