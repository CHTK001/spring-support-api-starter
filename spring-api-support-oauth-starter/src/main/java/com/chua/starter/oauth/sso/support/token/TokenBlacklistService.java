package com.chua.starter.oauth.sso.support.token;

/**
 * Token 黑名单服务
 * <p>用于管理已失效的 Token（登出、撤销、踢出等场景）</p>
 *
 * @since 2025/03/18
 * @author CH
 */
public interface TokenBlacklistService {

    /**
     * 黑名单原因枚举
     */
    enum BlacklistReason {
        /** 用户主动登出 */
        LOGOUT,
        /** Token 被撤销（如 refresh 后旧 token 失效） */
        REVOKED,
        /** 用户被踢下线 */
        KICKED,
        /** 用户被全局禁用 */
        USER_DISABLED
    }

    /**
     * 将 Token 加入黑名单
     *
     * @param token  Token 值
     * @param reason 加入黑名单的原因
     */
    void addToBlacklist(String token, BlacklistReason reason);

    /**
     * 将 Token 加入黑名单（带过期时间）
     *
     * @param token      Token 值
     * @param reason     加入黑名单的原因
     * @param expireTime Token 原本的过期时间（秒），黑名单记录会在此时间后自动删除
     * @param userId     用户ID（可选，用于日志）
     * @param username   用户名（可选，用于日志）
     */
    void addToBlacklist(String token, BlacklistReason reason, long expireTime, String userId, String username);

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token Token 值
     * @return true 表示在黑名单中（已失效），false 表示不在黑名单中
     */
    boolean isBlacklisted(String token);

    /**
     * 将用户加入全局黑名单（禁用用户）
     * <p>该用户的所有 Token 都将失效</p>
     *
     * @param userId 用户ID
     * @param reason 原因
     */
    void addUserToBlacklist(String userId, String reason);

    /**
     * 从全局黑名单中移除用户（启用用户）
     *
     * @param userId 用户ID
     */
    void removeUserFromBlacklist(String userId);

    /**
     * 检查用户是否在全局黑名单中
     *
     * @param userId 用户ID
     * @return true 表示用户被禁用，false 表示用户正常
     */
    boolean isUserBlacklisted(String userId);

    /**
     * 从黑名单中移除 Token（一般不需要手动调用，Redis 会自动过期）
     *
     * @param token Token 值
     */
    void removeFromBlacklist(String token);

    /**
     * 清空所有黑名单记录（慎用）
     */
    void clearAll();
}
