package com.chua.starter.monitor.websocket.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * VNC WebSocket处理器
 * <p>
 * 继承自 RdpWebSocketHandler，通过 Guacamole 协议处理 VNC 远程桌面连接。
 * VNC 和 RDP 的主要区别在于协议参数配置，此类可根据需要覆盖特定方法。
 * </p>
 *
 * @author CH
 * @since 2024/12/18
 */
@Component
@Slf4j
public class VncWebSocketHandler extends RdpWebSocketHandler {
    
    /**
     * VNC 协议默认端口
     */
    private static final int DEFAULT_VNC_PORT = 5900;
    
    // VNC 使用与 RDP 相同的 Guacamole 隧道机制
    // 协议差异由 Guacamole 配置中的 protocol 参数处理
    // 如需 VNC 特定功能，可在此类中覆盖相应方法
}
