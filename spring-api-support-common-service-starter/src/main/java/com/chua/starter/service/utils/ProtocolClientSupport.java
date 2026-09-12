package com.chua.starter.service.utils;

import com.chua.protocol.support.network.protocol.ClientSetting;
import com.chua.protocol.support.network.protocol.client.BaseProtocolClient;
import com.chua.protocol.support.network.protocol.client.ProtocolClientFactory;

/**
   * 协议客户端工厂 本地增强封装。
 * @author CH
 * @since 4.0.0
 */
public final class ProtocolClientSupport {

    /** 创建 协议客户端支持 实例 */
    private ProtocolClientSupport() {
    }

    /**
    * 创建
    *
    * @param clientId 客户端id
    * @param type 类型
    * @param setting setting
    * @return 创建的结果
     */
    public static <T extends BaseProtocolClient> T create(Object clientId, Class<T> type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    /**
     * 创建
     *
     * @param clientId 客户端id
     * @param type 类型
     * @param setting setting
     * @return 创建的结果
     */
    public static BaseProtocolClient create(Object clientId, String type, ClientSetting setting) {
        return ProtocolClientFactory.create(clientId, type, setting);
    }

    /**
     * 销毁
     *
     * @param clientId 客户端id
     * @param type 类型
     */
    public static void destroy(Object clientId, Class<? extends BaseProtocolClient> type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(factoryKey(clientId, type));
    }

    /**
     * 销毁
     *
     * @param clientId 客户端id
     * @param type 类型
     */
    public static void destroy(Object clientId, String type) {
        if (clientId == null || type == null) {
            return;
        }
        ProtocolClientFactory.uncreated(String.valueOf(clientId) + type);
    }

    /**
     * 工厂键
     *
     * @param clientId 客户端id
     * @param type 类型
     * @return 工厂键的结果
     */
    public static String factoryKey(Object clientId, Class<? extends BaseProtocolClient> type) {
        return String.valueOf(clientId) + type.getTypeName();
    }
}

