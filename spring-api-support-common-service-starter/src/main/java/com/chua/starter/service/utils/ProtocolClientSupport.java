package com.chua.starter.service.utils;

import com.chua.protocol.support.network.protocol.ClientSetting;
import com.chua.protocol.support.network.protocol.client.BaseProtocolClient;
import com.chua.protocol.support.network.protocol.client.ProtocolClientFactory;

/**
 * ProtocolClientFactory 本地增强封装。
 */
public final class ProtocolClientSupport {

    private ProtocolClientSupport() {
    }

    /** 创建 */
    public static <T extends BaseProtocolClient> T create(Object clientId, Class<T> type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    /** 创建 */
    public static BaseProtocolClient create(Object clientId, String type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    /** 销毁 */
    public static void destroy(Object clientId, Class<? extends BaseProtocolClient> type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(factoryKey(clientId, type));
    }

    /** 销毁 */
    public static void destroy(Object clientId, String type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(String.valueOf(clientId) + type);
    }

    /** FactoryKey */
    public static String factoryKey(Object clientId, Class<? extends BaseProtocolClient> type) {
        return String.valueOf(clientId) + type.getTypeName();
    }
}

