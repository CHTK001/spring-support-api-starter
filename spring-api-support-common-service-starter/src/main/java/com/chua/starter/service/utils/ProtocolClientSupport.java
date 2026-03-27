package com.chua.starter.service.utils;

import com.chua.common.support.network.protocol.ClientSetting;
import com.chua.common.support.network.protocol.client.BaseProtocolClient;
import com.chua.common.support.network.protocol.client.ProtocolClientFactory;

/**
 * ProtocolClientFactory 本地增强封装。
 */
public final class ProtocolClientSupport {

    private ProtocolClientSupport() {
    }

    public static <T extends BaseProtocolClient> T create(Object clientId, Class<T> type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    public static BaseProtocolClient create(Object clientId, String type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    public static void destroy(Object clientId, Class<? extends BaseProtocolClient> type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(factoryKey(clientId, type));
    }

    public static void destroy(Object clientId, String type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(String.valueOf(clientId) + type);
    }

    public static String factoryKey(Object clientId, Class<? extends BaseProtocolClient> type) {
        return String.valueOf(clientId) + type.getTypeName();
    }
}
