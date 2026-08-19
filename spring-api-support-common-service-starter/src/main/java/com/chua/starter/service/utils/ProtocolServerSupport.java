package com.chua.starter.service.utils;

import com.chua.protocol.support.network.protocol.ServerSetting;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.core.utils.StringUtils;

/**
 * ProtocolServer 统一增强封装。
 */
public final class ProtocolServerSupport {

    /** 创建 ProtocolServerSupport 实例 */
    private ProtocolServerSupport() {
    }

    /** 创建 */
    public static ProtocolServer create(String type, ServerSetting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("ServerSetting 不能为空");
        }
        String protocol = StringUtils.isBlank(type) ? "http" : type;
        return ProtocolServer.create(protocol, setting);
    }

    /** 开始IfNecessary */
    public static ProtocolServer startIfNecessary(ProtocolServer server) throws Exception {
        if (server == null) {
            throw new IllegalArgumentException("ProtocolServer 不能为空");
        }
        if (!server.isRunning()) {
            server.start();
        }
        return server;
    }

    /** 停止Quietly */
    public static void stopQuietly(ProtocolServer server) {
        if (server == null) {
            return;
        }
        try {
            if (server.isRunning()) {
                server.stop();
            } else {
                server.close();
            }
        } catch (Exception ignored) {
        }
    }
}

