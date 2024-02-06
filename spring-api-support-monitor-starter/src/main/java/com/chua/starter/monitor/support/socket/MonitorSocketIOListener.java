package com.chua.starter.monitor.support.socket;

import com.chua.socketio.support.SocketIOListener;
import com.chua.socketio.support.annotations.OnEvent;
import com.chua.socketio.support.session.SocketSession;
import org.springframework.context.annotation.Configuration;


/**
 * 显示器插座定位器
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/02/06
 */
@Configuration
public class MonitorSocketIOListener implements SocketIOListener {


    @OnEvent("terminal-request")
    public void onEvent(SocketSession session) {
        System.out.println();
    }
}
