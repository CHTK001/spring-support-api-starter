package com.chua.starter.monitor.socket.listener.webrtc;

import com.chua.common.support.json.Json;
import com.chua.common.support.utils.MapUtils;
import com.chua.socket.support.SocketListener;
import com.chua.socket.support.annotations.OnEvent;
import com.chua.socket.support.session.SocketSession;
import com.chua.socket.support.session.SocketSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;

/**
 * @author CH
 * @since 2024/12/13
 */
@SpringBootConfiguration
public class WebrtcSocketIOListener implements SocketListener {

    @Autowired
    @Lazy
    private SocketSessionTemplate socketSessionTemplate;

    @OnEvent("message")
    public void message(SocketSession socketSession, Map<String, Object> item) {
        String target = MapUtils.getString(item, "target");
        List<SocketSession> session = socketSessionTemplate.getOnlineSession("webrtc",  MapUtils.getString(item, "roomId"));
        for (SocketSession socketSession1 : session) {
            socketSession1.send("message", Json.toJson(item));
        }
    }
}

