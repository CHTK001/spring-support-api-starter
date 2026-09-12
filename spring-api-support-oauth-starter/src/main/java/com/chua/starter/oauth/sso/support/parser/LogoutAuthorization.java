package com.chua.starter.oauth.sso.support.parser;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
public final class LogoutAuthorization implements Authorization {
    /** 令牌 */
    private final String token;
    /** Cookie */
    private final String cookie;
    /** UID */
    private final String uid;
    /** Logout类型 */
    private final String logoutType;
    /** 响应 */
    private final HttpServletResponse response;
    /** 认证服务器属性 */
    private final AuthServerProperties authServerProperties;
    final ApplicationContext applicationContext; // application上下文
    final LoginCheck loginCheck; // login检查
    /** Authinformation */
    private final LogoutAuthInformation authInformation;

    /**
    * 创建 logout授权 实例
    * @param authInformation 认证信息
    * @param token 令牌
    * @param cookie Cookie
    * @param uid uid
    * @param logoutType logout类型
    * @param response 响应
    * @param authServerProperties 认证服务端属性
    * @param applicationContext application上下文
    * @param loginCheck login检查
     */
    public LogoutAuthorization(LogoutAuthInformation authInformation,
                               String token,
                               String cookie,
                               String uid,
                               String logoutType,
                               HttpServletResponse response,
                               AuthServerProperties authServerProperties,
                               ApplicationContext applicationContext,
                               LoginCheck loginCheck) {
        this.authInformation = authInformation;
        this.token = token;
        this.cookie = cookie;
        this.uid = uid;
        this.logoutType = logoutType;
        this.response = response;
        this.authServerProperties = authServerProperties;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
    }

    @Override
    /** 认证 */
    public ReturnResult<String> authentication() {
        long startTime = System.currentTimeMillis();
        String traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        log.info("[LOGOUT][traceId={}] 开始: uid={}, logoutType={}", traceId,
                uid != null && uid.length() > 8 ? uid.substring(0, 8) + "..." : uid, logoutType);

        String tokenManagement = authInformation.getAuthServerProperties().getTokenManagement();
        TokenResolver tokenResolver = loginCheck.resolveTokenResolver(tokenManagement);
        if (null == tokenResolver) {
            log.error("[LOGOUT][traceId={}] 失败: 认证服务器无法认证, tokenManagement={}", traceId, tokenManagement);
            return ReturnResult.error("认证服务器无法认证");
        }

        LogoutType type;
        try {
            type = LogoutType.valueOf(logoutType);
        } catch (Exception e) {
            log.warn("[LOGOUT][traceId={}] 非法 logoutType={}, 使用默认 LOGOUT", traceId, logoutType);
            type = LogoutType.LOGOUT;
        }

 // 如果 uid 为空但 令牌 不为空，尝试从 令牌 中提取 uid
        String actualUid = uid;
        if (actualUid == null && token != null && token.length() > 0) {
            try {
 // 尝试从 JWT 令牌 中提取 uid
                if (isJwtToken(token)) {
                    actualUid = extractUidFromJwt(token);
                    log.info("[LOGOUT][traceId={}] 从 JWT token 中提取 uid={}", traceId, actualUid);
                } else {
 // 对于普通 令牌，使用 令牌 本身作为 uid
                    actualUid = token;
                    log.info("[LOGOUT][traceId={}] 使用 token 作为 uid", traceId);
                }
            } catch (Exception e) {
                log.warn("[LOGOUT][traceId={}] 从 token 提取 uid 失败: {}", traceId, e.getMessage());
 // 如果提取失败，使用 令牌 本身
                actualUid = token;
            }
        }

        if (actualUid == null) {
            log.error("[LOGOUT][traceId={}] 失败: uid 和 token 均为空", traceId);
            return ReturnResult.error("登出失败: 缺少用户标识");
        }

        tokenResolver.logout(actualUid, type);

        long cost = System.currentTimeMillis() - startTime;
        log.info("[LOGOUT][traceId={}] 成功: uid={}, type={}, 耗时={}ms", traceId,
                actualUid != null && actualUid.length() > 8 ? actualUid.substring(0, 8) + "..." : actualUid, type, cost);

        return ReturnResult.ok(authInformation.encodeHex("SUCCESS"));
    }

    /**
    * 判断是否为 JWT 令牌（三段式，以 . 分隔）
    * @param token 令牌
    * @return 是否jwt令牌的结果
     */
    private boolean isJwtToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 移除 Bearer 前缀
        String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        String[] parts = actualToken.split("\\.");
        return parts.length == 3;
    }

    /**
      * 从 JWT 令牌 中提取 uid（sub 字段）
     * @param token 令牌
     * @return extractuid从jwt的结果
     */
    private String extractUidFromJwt(String token) {
        try {
            // 移除 Bearer 前缀
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
 // JWT 格式: 头部.payload.签名
            String[] parts = actualToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

 // 解码 payload（基础64URL）
            String payload = parts[1];
 // 基础64URL 解码
            byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(payload);
            String payloadJson = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);

            // 解析 JSON 获取 sub 字段
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> jsonMap = com.chua.common.support.lang.json.Json.fromJson(payloadJson, java.util.Map.class);
            Object subObj = jsonMap.get("sub");
            if (subObj == null) {
                throw new IllegalArgumentException("JWT payload missing 'sub' field");
            }
            String sub = String.valueOf(subObj);
            if (sub.isEmpty()) {
                throw new IllegalArgumentException("JWT payload 'sub' field is empty");
            }
            return sub;
        } catch (Exception e) {
            log.warn("从 JWT token 提取 uid 失败: {}", e.getMessage());
            throw new RuntimeException("Failed to extract uid from JWT token", e);
        }
    }

}
