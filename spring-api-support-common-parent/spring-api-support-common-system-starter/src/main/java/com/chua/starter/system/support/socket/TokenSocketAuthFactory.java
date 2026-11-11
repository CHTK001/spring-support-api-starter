package com.chua.starter.system.support.socket;

import com.chua.socket.support.auth.SocketAuthFactory;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.socket.support.session.SocketUser;
import com.chua.starter.oauth.client.support.algorithm.TokenCipher;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;

import java.util.List;
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
    private static final String BEARER_PREFIX = "bearer ";
    private static final String TOKEN_QUERY = "token";
    private static final String ACCESS_TOKEN_QUERY = "access_token";
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
            String token = stringValue(headers.get(TOKEN_HEADER));
            if (!isBlank(token)) {
                return token;
            }
            String authorization = stringValue(headers.get(AUTHORIZATION_HEADER));
            if (!isBlank(authorization) && authorization.toLowerCase().startsWith(BEARER_PREFIX)) {
                return authorization.substring(BEARER_PREFIX.length()).trim();
            }
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

    private boolean isUsable(UserResult user) {
        if (user == null || isBlank(user.getUserId())) {
            return false;
        }
        Long expireTime = user.getExpireTime();
        if (expireTime == null || expireTime <= 0) {
            return true;
        }
        long expireMillis = expireTime < MILLIS_THRESHOLD ? expireTime * 1000L : expireTime;
        return expireMillis > System.currentTimeMillis();
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
