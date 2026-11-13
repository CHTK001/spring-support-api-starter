package com.chua.starter.system.support.socket;

import com.chua.socket.support.auth.SocketAuthFactory;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socket.support.session.SocketUser;
import com.chua.starter.oauth.client.support.algorithm.TokenCipher;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Socket.IO 握手令牌鉴权。
 *
 * <p>前端在 polling 握手里通过 {@code x-oauth-token} 头携带访问令牌；该头在
 * websocket-only 传输下浏览器不会发送，因此前端必须保留 polling 作为初始传输，
 * 或改用查询参数。这里同时接受请求头、Bearer 头和查询参数三种入口。</p>
 *
 * <p>本地登录签发的是 SM4 加密的用户快照，可直接解密校验；远程 OAuth 的令牌再走
 * {@link AuthClientExecute#getUserResult(String)} 校验。令牌无效、过期或解析不到用户
 * 一律拒绝，不做匿名降级。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
public class TokenSocketAuthFactory implements SocketAuthFactory {

    private static final String TOKEN_HEADER = "x-oauth-token";
    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String COOKIE_HEADER = "cookie";
    private static final String BEARER_PREFIX = "bearer ";
    private static final String TOKEN_QUERY = "token";
    private static final String ACCESS_TOKEN_QUERY = "access_token";
    private static final String OAUTH_TOKEN_COOKIE = "oauth-token";
    private static final String[] COOKIE_TOKEN_NAMES = {
            TOKEN_HEADER, TOKEN_QUERY, ACCESS_TOKEN_QUERY, OAUTH_TOKEN_COOKIE
    };
    private static final long MILLIS_THRESHOLD = 10_000_000_000L;

    @Override
    public boolean isAuthorized(Map<String, Object> handshakeData) {
        return resolveUser(handshakeData) != null;
    }

    @Override
    public SocketUser getUser(Map<String, Object> handshakeData) {
        UserResult result = resolveUser(handshakeData);
        if (result == null || isBlank(result.getUserId())) {
            return null;
        }
        SocketUser user = new SocketUser(result.getUserId(), result.getUsername());
        user.setTenantId(result.getTenantId());
        user.setExtra("roles", result.getRoles());
        user.setExtra("permissions", result.getPermission());
        return user;
    }

    /**
     * 解析并校验令牌。
     *
     * @param handshakeData 握手数据
     * @return 有效用户；无效返回 null
     */
    private UserResult resolveUser(Map<String, Object> handshakeData) {
        String token = extractToken(handshakeData);
        if (isBlank(token)) {
            return null;
        }
        UserResult local = TokenCipher.decryptToUser(token);
        if (isUsable(local)) {
            return local;
        }
        try {
            UserResult remote = AuthClientExecute.getInstance().getUserResult(token);
            return isUsable(remote) ? remote : null;
        } catch (Exception e) {
            // 远程认证服务不可用时也不能放行；前端会退回轮询
            return null;
        }
    }

    /**
     * 从请求头或查询参数提取令牌。
     *
     * @param handshakeData 握手数据
     * @return 令牌，缺失返回 null
     */
    private String extractToken(Map<String, Object> handshakeData) {
        Object headersValue = handshakeData.get(SocketSessionTemplate.HANDSHAKE_HEADERS);
        if (headersValue instanceof Map<?, ?> headers) {
            String token = headerValue(headers, TOKEN_HEADER);
            if (!isBlank(token)) {
                return token;
            }
            String authorization = headerValue(headers, AUTHORIZATION_HEADER);
            if (!isBlank(authorization) && authorization.toLowerCase(Locale.ROOT).startsWith(BEARER_PREFIX)) {
                return authorization.substring(BEARER_PREFIX.length()).trim();
            }
            String cookieToken = cookieToken(headerValue(headers, COOKIE_HEADER));
            if (!isBlank(cookieToken)) {
                return cookieToken;
            }
        }

        // 部分 Socket.IO 适配器把 Cookie 放在握手数据根节点，而不是 headers map。
        String handshakeCookie = cookieToken(handshakeData.get(COOKIE_HEADER));
        if (!isBlank(handshakeCookie)) {
            return handshakeCookie;
        }

        Object queryValue = handshakeData.get(SocketSessionTemplate.HANDSHAKE_QUERY);
        if (queryValue instanceof Map<?, ?> query) {
            String token = firstValue(query.get(TOKEN_HEADER));
            if (isBlank(token)) {
                token = firstValue(query.get(TOKEN_QUERY));
            }
            if (isBlank(token)) {
                token = firstValue(query.get(ACCESS_TOKEN_QUERY));
            }
            if (!isBlank(token)) {
                return token;
            }
        }
        return null;
    }

    // [P3C MJ-方法注释 豁免]
    private boolean isUsable(UserResult user) {
        if (user == null || isBlank(user.getUserId())) {
            return false;
        }
        Long expireTime = user.getExpireTime();
        if (expireTime == null || expireTime <= 0) {
            return true;
        }
        // [P3C A20 豁免]
        long expireMillis = expireTime < MILLIS_THRESHOLD ? expireTime * 1000L : expireTime;
        return expireMillis > System.currentTimeMillis();
    }

    /**
     * 按名称读取握手请求头（HTTP 头名称大小写不敏感）。
     *
     * @param headers 请求头
     * @param name    头名称
     * @return 头值；缺失返回 null
     */
    private String headerValue(Map<?, ?> headers, String name) {
        Object direct = headers.get(name);
        if (direct != null) {
            return stringValue(direct);
        }
        for (Map.Entry<?, ?> entry : headers.entrySet()) {
            if (entry.getKey() != null && name.equalsIgnoreCase(String.valueOf(entry.getKey()))) {
                return stringValue(entry.getValue());
            }
        }
        return null;
    }

    /**
     * 从 Cookie 头中提取令牌。
     *
     * <p>支持 {@code x-oauth-token}、{@code token}、{@code access_token} 与
     * {@code oauth-token} 四种 cookie 名；值按 URL 编码规则解码。</p>
     *
     * @param cookieHeader Cookie 头原始值
     * @return 令牌；缺失返回 null
     */
    private String cookieToken(Object cookieHeader) {
        String raw = stringValue(cookieHeader);
        if (isBlank(raw)) {
            return null;
        }
        for (String part : raw.split(";")) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String name = part.substring(0, separator).trim();
            if (!isCookieTokenName(name)) {
                continue;
            }
            String value = part.substring(separator + 1).trim();
            try {
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignored) {
                // 非法百分号编码时按原值处理，由后续令牌校验决定是否放行
            }
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断 cookie 名是否为受支持的令牌名。
     *
     * @param name cookie 名
     * @return 是否受支持
     */
    private boolean isCookieTokenName(String name) {
        for (String candidate : COOKIE_TOKEN_NAMES) {
            if (candidate.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String firstValue(Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                String text = stringValue(item);
                if (!isBlank(text)) {
                    return text;
                }
            }
            return null;
        }
        return stringValue(value);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    // [P3C MJ-方法注释 豁免]
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
