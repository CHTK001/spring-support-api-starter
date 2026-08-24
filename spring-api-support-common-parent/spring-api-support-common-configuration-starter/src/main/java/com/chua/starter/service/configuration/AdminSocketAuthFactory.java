package com.chua.starter.service.configuration;

import com.chua.common.support.utils.BeanUtils;
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
 * @since 2025/7/1
 * @author CH
 */
@Configuration
@ConditionalOnClass(name = {"com.chua.starter.oauth.client.support.execute.AuthClientExecute", "com.chua.socket.support.auth.SocketAuthFactory"})
public class AdminSocketAuthFactory implements SocketAuthFactory {


    /**
     * 校验握手数据是否携带有效的 OAuth Token。
     *
     * @param handshakeData 握手数据
     * @return true 表示已授权
     */
    @Override
    public boolean isAuthorized(Map<String, Object> handshakeData) {
        String singleUrlParam = MapUtils.getString(handshakeData, "x-oauth-token");
        if (StringUtils.isEmpty(singleUrlParam)) {
            return false;
        }
        return null != AuthClientExecute.getInstance().getUserResult(singleUrlParam);
    }

    /**
     * 根据握手数据构建套接字用户。
     *
     * @param handshakeData 握手数据
     * @return 套接字用户，未携带 Token 时为 null
     */
    @Override
    public SocketUser getUser(Map<String, Object> handshakeData) {
        String singleUrlParam = MapUtils.getString(handshakeData, "x-oauth-token");
        if (StringUtils.isEmpty(singleUrlParam)) {
            return null;
        }
        UserResult userResult = AuthClientExecute.getInstance().getUserResult(singleUrlParam);
        SocketUser socketUser = BeanUtils.copyProperties(userResult, SocketUser.class);
        // TODO[@L56]: SocketUser 可能没有 setRoomId 方法，需要检查 SocketUser 类的实际结构
        // socketUser.setRoomId(MapUtils.getString(handshakeData, "roomId"));
        return socketUser;
    }
}
