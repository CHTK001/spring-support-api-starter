package com.chua.starter.monitor.socketio;

import com.chua.common.support.json.Json;
import com.chua.common.support.utils.MapUtils;
import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import com.chua.socketio.support.session.SocketSessionTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringBootConfiguration;

import java.util.List;
import java.util.Map;

/**
 * @author CH
 * @since 2024/12/13
 */
@SpringBootConfiguration
@RequiredArgsConstructor
public class WebrtcSocketIOListener implements SocketIOListener {

    private final SocketSessionTemplate socketSessionTemplate;

    @OnEvent("message")
    public void message(SocketSession socketSession, Map<String, Object> item) {
        String target = MapUtils.getString(item, "target");
        List<SocketSession> session = socketSessionTemplate.getOnlineSession("webrtc",  MapUtils.getString(item, "roomId"));
        for (SocketSession socketSession1 : session) {
            socketSession1.send("message", Json.toJson(item));
        }
    }
}
