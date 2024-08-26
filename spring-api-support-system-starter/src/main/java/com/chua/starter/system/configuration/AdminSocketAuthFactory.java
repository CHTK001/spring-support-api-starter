package com.chua.starter.system.configuration;

import com.chua.common.support.utils.StringUtils;
import com.chua.socketio.support.auth.SocketAuthFactory;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.corundumstudio.socketio.HandshakeData;
import org.springframework.context.annotation.Configuration;

/**
 * 管理员套接字身份验证工厂
 *
 * @author CH
 */
@Configuration
public class AdminSocketAuthFactory implements SocketAuthFactory {

    @Override
    public boolean isAuthorized(HandshakeData data) {
        String singleUrlParam = data.getSingleUrlParam("x-oauth-token");
        if(StringUtils.isEmpty(singleUrlParam)) {
            return false;
        }
        return null != AuthClientExecute.getInstance().getUserResult(singleUrlParam);
    }
}
