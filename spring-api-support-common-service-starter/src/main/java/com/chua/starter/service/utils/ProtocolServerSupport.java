package com.chua.starter.service.utils;

import com.chua.protocol.support.network.protocol.ServerSetting;
import com.chua.protocol.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.core.utils.StringUtils;

/**
   * 协议服务端 统一增强封装。
 * @author CH
 * @since 4.0.0
 */
public final class ProtocolServerSupport {

    /** 创建 协议服务端支持 实例 */
    private ProtocolServerSupport() {
    }

    /**
     * 创建
     *
     * @param type 类型
     * @param setting setting
     * @return 创建的结果
     */
    public static ProtocolServer create(String type, ServerSetting setting) {
        if (setting == null) {
            throw new IllegalArgumentException("ServerSetting 不能为空");
        }
        String protocol = StringUtils.isBlank(type) ? "http" : type;
        return ProtocolServer.create(protocol, setting);
    }

    /**
     * 开始ifnecessary
     *
     * @param server 服务端
     * @return 启动ifnecessary的结果
     */
    public static ProtocolServer startIfNecessary(ProtocolServer server) throws Exception {
        if (server == null) {
            throw new IllegalArgumentException("ProtocolServer 不能为空");
        }
        if (!server.isRunning()) {
            server.start();
        }
        return server;
    }

    /**
     * 停止Quietly
     *
     * @param server 服务端
     */
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

