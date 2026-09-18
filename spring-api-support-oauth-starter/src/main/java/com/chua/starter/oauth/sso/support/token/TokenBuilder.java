package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.IdUtils;

/**
 * 令牌 生成工具
 * <p>
 * 统一生成 access令牌 和 refresh令牌，确保格式一致
 * </p>
 *
 * <h3>Token 格式：</h3>
 * <pre>
 * {prefix}{userIdHash}{uidHash}{randomHex}
 * - prefix = 2位，AT(AccessToken) 或 RT(RefreshToken)
 * - userIdHash = 32位，完整MD5(userId)，用于按用户统计在线数量
 * - uidHash = 32位，完整uid生成值，用于标识登录方式
 * - randomHex = 16位，随机值保证唯一性
 * 总长度：2 + 32 + 32 + 16 = 82位
 * </pre>
 *
 * @版本 1.0.0
 * @since 2025-12-07
 * @author CH
 */
public final class TokenBuilder {

    /**
     * access令牌 前缀
     */
    public static final String ACCESS_TOKEN_PREFIX = "AT";

    /**
     * refresh令牌 前缀
     */
    public static final String REFRESH_TOKEN_PREFIX = "RT";

    /**
     * 用户id哈希 在 令牌 中的起始位置（跳过前缀）
     */
    public static final int USER_ID_HASH_START = 2;

    /**
     * 用户id哈希 长度
     */
    public static final int USER_ID_HASH_LENGTH = 32;

    /**
     * uid哈希 在 令牌 中的起始位置
     */
    public static final int UID_HASH_START = USER_ID_HASH_START + USER_ID_HASH_LENGTH;

    /**
     * uid哈希 长度
     */
    public static final int UID_HASH_LENGTH = 32;

    /**
     * 随机hex 在 令牌 中的起始位置
     */
    public static final int RANDOM_START = UID_HASH_START + UID_HASH_LENGTH;

    /**
     * 随机hex 长度
     */
    public static final int RANDOM_LENGTH = 16;

    /**
     * 令牌 总长度
     */
    public static final int TOKEN_LENGTH = 2 + USER_ID_HASH_LENGTH + UID_HASH_LENGTH + RANDOM_LENGTH;

    /** 创建 令牌构建器 实例 */
    private TokenBuilder() {
        // 工具类禁止实例化
    }

    /**
     * 生成 access令牌
     *
     * @param userId  用户标识（数据库主键）
     * @param uidHash uid生成值（标识登录方式）
     * @return AccessToken
     */
    public static String generateAccessToken(String userId, String uidHash) {
        return generateToken(ACCESS_TOKEN_PREFIX, userId, uidHash);
    }

    /**
     * 生成 refresh令牌
     *
     * @param userId  用户标识（数据库主键）
     * @param uidHash uid生成值（标识登录方式）
     * @return RefreshToken
     */
    public static String generateRefreshToken(String userId, String uidHash) {
        return generateToken(REFRESH_TOKEN_PREFIX, userId, uidHash);
    }

    /**
     * 生成 令牌
     *
     * @param prefix  前缀（AT/RT）
     * @param userId  用户标识
     * @param uidHash uid生成值
     * @return Token
     */
    private static String generateToken(String prefix, String userId, String uidHash) {
        String userIdHash = DigestUtils.md5Hex(userId);
        String randomPart = DigestUtils.md5Hex(IdUtils.uuid()).substring(0, RANDOM_LENGTH);
        return prefix + userIdHash + uidHash + randomPart;
    }

    /**
     * 从 令牌 中提取 用户id哈希
     * <p>用于按用户统计在线数量</p>
     *
     * @param token 令牌
     * @return userIdHash（32位）
     */
    public static String extractUserIdHash(String token) {
        if (token == null || token.length() < USER_ID_HASH_START + USER_ID_HASH_LENGTH) {
            return null;
        }
        return token.substring(USER_ID_HASH_START, USER_ID_HASH_START + USER_ID_HASH_LENGTH);
    }

    /**
     * 从 令牌 中提取 uid哈希
     * <p>用于标识登录方式</p>
     *
     * @param token 令牌
     * @return uidHash（32位）
     */
    public static String extractUidHash(String token) {
        if (token == null || token.length() < UID_HASH_START + UID_HASH_LENGTH) {
            return null;
        }
        return token.substring(UID_HASH_START, UID_HASH_START + UID_HASH_LENGTH);
    }

    /**
     * 判断是否为 access令牌
     *
     * @param token 令牌
     * @return true-是AccessToken
     */
    public static boolean isAccessToken(String token) {
        return token != null && token.startsWith(ACCESS_TOKEN_PREFIX);
    }

    /**
     * 判断是否为 refresh令牌
     *
     * @param token 令牌
     * @return true-是RefreshToken
     */
    public static boolean isRefreshToken(String token) {
        return token != null && token.startsWith(REFRESH_TOKEN_PREFIX);
    }

    /**
     * 根据 用户id 生成查询模式
     * <p>用于查询该用户的所有 Token</p>
     *
     * @param userId 用户标识
     * @return 查询模式，如：AT{userIdHash}*
     */
    public static String buildAccessTokenPattern(String userId) {
        return ACCESS_TOKEN_PREFIX + DigestUtils.md5Hex(userId) + "*";
    }

    /**
     * 根据 用户id 生成 refresh令牌 查询模式
     *
     * @param userId 用户标识
     * @return 查询模式，如：RT{userIdHash}*
     */
    public static String buildRefreshTokenPattern(String userId) {
        return REFRESH_TOKEN_PREFIX + DigestUtils.md5Hex(userId) + "*";
    }

    /**
     * 根据 用户id哈希 生成查询模式（已有哈希值时使用）
     *
     * @param userIdHash 用户id哈希
     * @return 查询模式
     */
    public static String buildAccessTokenPatternByHash(String userIdHash) {
        return ACCESS_TOKEN_PREFIX + userIdHash + "*";
    }

    /**
     * 根据 用户id哈希 生成 refresh令牌 查询模式
     *
     * @param userIdHash 用户id哈希
     * @return 查询模式
     */
    public static String buildRefreshTokenPatternByHash(String userIdHash) {
        return REFRESH_TOKEN_PREFIX + userIdHash + "*";
    }
}
