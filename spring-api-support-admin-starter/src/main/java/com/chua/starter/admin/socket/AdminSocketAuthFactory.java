package com.chua.starter.admin.socket;

import com.chua.socketio.support.auth.SocketAuthFactory;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.corundumstudio.socketio.HandshakeData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * 管理员套接字身份验证工厂
 *
 * @author CH
 */
@Configuration
public class AdminSocketAuthFactory implements SocketAuthFactory {

    private static final Cache<String, Boolean> CACHE = CacheBuilder
            .newBuilder().expireAfterWrite(Duration.of(3, ChronoUnit.MINUTES))
            .build();
    @Override
    public boolean isAuthorized(HandshakeData data) {
        String singleUrlParam = data.getSingleUrlParam("x-oauth-token");
        Boolean ifPresent = CACHE.getIfPresent(singleUrlParam);
        if(null != ifPresent) {
            return ifPresent;
        }
        boolean b = null != AuthClientExecute.getInstance().getUserResult(singleUrlParam);
        CACHE.put(singleUrlParam, b);
        return b;
    }
}
