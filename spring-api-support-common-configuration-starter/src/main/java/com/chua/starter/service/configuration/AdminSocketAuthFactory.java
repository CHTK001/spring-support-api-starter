package com.chua.starter.service.configuration;

import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.socket.support.auth.SocketAuthFactory;
import com.chua.socket.support.session.SocketUser;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.corundumstudio.socketio.HandshakeData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 管理员套接字身份验证工厂
 *
 * @author CH
 */
@Configuration
@ConditionalOnClass(name = {"com.chua.starter.oauth.client.support.execute.AuthClientExecute"})
public class AdminSocketAuthFactory implements SocketAuthFactory {


    @Override
    public boolean isAuthorized(Map<String, Object> handshakeData) {
        String singleUrlParam = MapUtils.getString(handshakeData, "x-oauth-token");
        if(StringUtils.isEmpty(singleUrlParam)) {
            return false;
        }
        return null != AuthClientExecute.getInstance().getUserResult(singleUrlParam);
    }

    @Override
    public SocketUser getUser(Map<String, Object> handshakeData) {
        String singleUrlParam = MapUtils.getString(handshakeData, "x-oauth-token");
        if(StringUtils.isEmpty(singleUrlParam)) {
            return null;
        }
        UserResult userResult = AuthClientExecute.getInstance().getUserResult(singleUrlParam);
        SocketUser socketUser = BeanUtils.copyProperties(userResult, SocketUser.class);
        socketUser.setRoomId(MapUtils.getString(handshakeData, "roomId"));
        return socketUser;
    }
}
