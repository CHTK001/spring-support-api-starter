package com.chua.starter.tenant.server.support;

import com.chua.starter.tenant.entity.SysTenant;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * 租户专用数据库账号的命名规则。
 *
 * <p>平台为每个「引用现成实例」的租户建一个 {@code t_<租户编码>} 账号，只授权到
 * 该租户自己的库。账号名要能安全拼进 DDL（标识符白名单只允许字母数字下划线），
 * 所以统一在这里归一化，避免创建路径与补账号任务各写一套、生成出不一致的账号。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
public final class TenantDbAccountNaming {

    /**
     * 账号名前缀：{@code t_} 表示平台生成，删除租户库时据此判断能否回收账号
     */
    public static final String PREFIX = "t_";

    /**
     * MySQL 账号名上限
     */
    private static final int MAX_LENGTH = 32;

    private TenantDbAccountNaming() {
    }

    /**
     * 生成租户专用账号名。
     *
     * <p>规则：取租户编码的小写形式，非字母数字下划线的字符替换成下划线；
     * 首字符必须是字母，否则加 {@code t_} 前缀；总长不超过 32 位。</p>
     *
     * @param tenant 租户记录
     * @return 可安全拼进 DDL 的账号名
     */
    public static String generate(SysTenant tenant) {
        String base = tenant == null || isBlank(tenant.getSysTenantCode())
                ? "tenant" : tenant.getSysTenantCode().toLowerCase(Locale.ROOT);
        String normalized = base.replaceAll("[^a-z0-9_]", "_");
        if (normalized.isEmpty() || !Character.isLetter(normalized.charAt(0))) {
            normalized = PREFIX + normalized;
        }
        String name = normalized.startsWith(PREFIX) ? normalized : PREFIX + normalized;
        return name.length() > MAX_LENGTH ? name.substring(0, MAX_LENGTH) : name;
    }

    /**
     * 是否平台生成的账号（删除租户时只回收这类账号）。
     *
     * @param username 账号名
     * @return 以 {@code t_} 开头返回 true
     */
    public static boolean isGenerated(String username) {
        return username != null && username.startsWith(PREFIX);
    }

    /**
     * 是否空白。
     *
     * @param value 待判断值
     * @return 空白返回 true
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
