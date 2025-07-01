package com.chua.starter.monitor.starter.websocket;

import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * VNC WebSocket处理器
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class VncWebSocketHandler extends RdpWebSocketHandler {

    public VncWebSocketHandler(MonitorSysGenServerService serverService, MonitorSysGenServerSettingService serverSettingService, SocketSessionTemplate socketSessionTemplate) {
        super(serverService, serverSettingService, socketSessionTemplate);
    }
}
