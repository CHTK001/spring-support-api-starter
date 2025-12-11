package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.IdUtils;

/**
 * Token 生成工具
 * <p>
 * 统一生成 AccessToken 和 RefreshToken，确保格式一致
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
 * @author CH
 * @version 1.0.0
 * @since 2025-12-07
 */
public final class TokenBuilder {

    /**
     * AccessToken 前缀
     */
    public static final String ACCESS_TOKEN_PREFIX = "AT";

    /**
     * RefreshToken 前缀
     */
    public static final String REFRESH_TOKEN_PREFIX = "RT";

    /**
     * userIdHash 在 Token 中的起始位置（跳过前缀）
     */
    public static final int USER_ID_HASH_START = 2;

    /**
     * userIdHash 长度
     */
    public static final int USER_ID_HASH_LENGTH = 32;

    /**
     * uidHash 在 Token 中的起始位置
     */
    public static final int UID_HASH_START = USER_ID_HASH_START + USER_ID_HASH_LENGTH;

    /**
     * uidHash 长度
     */
    public static final int UID_HASH_LENGTH = 32;

    /**
     * randomHex 在 Token 中的起始位置
     */
    public static final int RANDOM_START = UID_HASH_START + UID_HASH_LENGTH;

    /**
     * randomHex 长度
     */
    public static final int RANDOM_LENGTH = 16;

    /**
     * Token 总长度
     */
    public static final int TOKEN_LENGTH = 2 + USER_ID_HASH_LENGTH + UID_HASH_LENGTH + RANDOM_LENGTH;

    private TokenBuilder() {
        // 工具类禁止实例化
    }

    /**
     * 生成 AccessToken
     *
     * @param userId  用户ID（数据库主键）
     * @param uidHash uid生成值（标识登录方式）
     * @return AccessToken
     */
    public static String generateAccessToken(String userId, String uidHash) {
        return generateToken(ACCESS_TOKEN_PREFIX, userId, uidHash);
    }

    /**
     * 生成 RefreshToken
     *
     * @param userId  用户ID（数据库主键）
     * @param uidHash uid生成值（标识登录方式）
     * @return RefreshToken
     */
    public static String generateRefreshToken(String userId, String uidHash) {
        return generateToken(REFRESH_TOKEN_PREFIX, userId, uidHash);
    }

    /**
     * 生成 Token
     *
     * @param prefix  前缀（AT/RT）
     * @param userId  用户ID
     * @param uidHash uid生成值
     * @return Token
     */
    private static String generateToken(String prefix, String userId, String uidHash) {
        String userIdHash = DigestUtils.md5Hex(userId);
        String randomPart = DigestUtils.md5Hex(IdUtils.createTid()).substring(0, RANDOM_LENGTH);
        return prefix + userIdHash + uidHash + randomPart;
    }

    /**
     * 从 Token 中提取 userIdHash
     * <p>用于按用户统计在线数量</p>
     *
     * @param token Token
     * @return userIdHash（32位）
     */
    public static String extractUserIdHash(String token) {
        if (token == null || token.length() < USER_ID_HASH_START + USER_ID_HASH_LENGTH) {
            return null;
        }
        return token.substring(USER_ID_HASH_START, USER_ID_HASH_START + USER_ID_HASH_LENGTH);
    }

    /**
     * 从 Token 中提取 uidHash
     * <p>用于标识登录方式</p>
     *
     * @param token Token
     * @return uidHash（32位）
     */
    public static String extractUidHash(String token) {
        if (token == null || token.length() < UID_HASH_START + UID_HASH_LENGTH) {
            return null;
        }
        return token.substring(UID_HASH_START, UID_HASH_START + UID_HASH_LENGTH);
    }

    /**
     * 判断是否为 AccessToken
     *
     * @param token Token
     * @return true-是AccessToken
     */
    public static boolean isAccessToken(String token) {
        return token != null && token.startsWith(ACCESS_TOKEN_PREFIX);
    }

    /**
     * 判断是否为 RefreshToken
     *
     * @param token Token
     * @return true-是RefreshToken
     */
    public static boolean isRefreshToken(String token) {
        return token != null && token.startsWith(REFRESH_TOKEN_PREFIX);
    }

    /**
     * 根据 userId 生成查询模式
     * <p>用于查询该用户的所有 Token</p>
     *
     * @param userId 用户ID
     * @return 查询模式，如：AT{userIdHash}*
     */
    public static String buildAccessTokenPattern(String userId) {
        return ACCESS_TOKEN_PREFIX + DigestUtils.md5Hex(userId) + "*";
    }

    /**
     * 根据 userId 生成 RefreshToken 查询模式
     *
     * @param userId 用户ID
     * @return 查询模式，如：RT{userIdHash}*
     */
    public static String buildRefreshTokenPattern(String userId) {
        return REFRESH_TOKEN_PREFIX + DigestUtils.md5Hex(userId) + "*";
    }

    /**
     * 根据 userIdHash 生成查询模式（已有hash值时使用）
     *
     * @param userIdHash userIdHash
     * @return 查询模式
     */
    public static String buildAccessTokenPatternByHash(String userIdHash) {
        return ACCESS_TOKEN_PREFIX + userIdHash + "*";
    }

    /**
     * 根据 userIdHash 生成 RefreshToken 查询模式
     *
     * @param userIdHash userIdHash
     * @return 查询模式
     */
    public static String buildRefreshTokenPatternByHash(String userIdHash) {
        return REFRESH_TOKEN_PREFIX + userIdHash + "*";
    }
}
