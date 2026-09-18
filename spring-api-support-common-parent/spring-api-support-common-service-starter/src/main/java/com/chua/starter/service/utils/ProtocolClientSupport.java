package com.chua.starter.service.utils;

import com.chua.common.support.network.protocol.ClientSetting;
import com.chua.ssh.support.client.SshClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议客户端工厂 本地增强封装。
 * 使用 ssh客户端.构建器() 创建实例，不依赖已删除的 协议-启动 模块。
 * @author CH
 * @since 4.0.0
 */
public final class ProtocolClientSupport {

    /** 缓存已创建的客户端，用于 销毁 */
    private static final Map<String, Object> CLIENTS = new ConcurrentHashMap<>();

    /**
    * 协议客户端支持。
     */
    private ProtocolClientSupport() {
    }

    /**
     * 创建 SSH 客户端
     *
     * @param clientId 客户端标识
     * @param type     客户端类型（必须是 ssh客户端.类）
     * @param setting  连接配置
     * @param <T>      客户端类型
     * @return 客户端实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Object clientId, Class<T> type, ClientSetting setting) {
        if (SshClient.class.isAssignableFrom(type)) {
            SshClient client = SshClient.builder()
                    .host(setting.getHost())
                    .port(setting.getPort())
                    .username(setting.getUsername())
                    .password(setting.getPassword())
                    .connectTimeout(30)
                    .build()
                    .connect();
            CLIENTS.put(String.valueOf(clientId), client);
            return (T) client;
        }
        throw new IllegalArgumentException("Unsupported client type: " + type);
    }

    /**
     * 创建客户端（通过类型名称）
     * @param clientId 客户端id
     * @param type 类型
     * @param setting setting
     * @return 创建的结果
     */
    public static SshClient create(Object clientId, String type, ClientSetting setting) {
        return create(clientId, SshClient.class, setting);
    }

    /**
     * 销毁客户端
     * @param clientId 客户端id
     * @param type 类型
     */
    public static void destroy(Object clientId, Class<?> type) {
        if (clientId == null || type == null) {
            return;
        }
        String key = String.valueOf(clientId);
        Object client = CLIENTS.remove(key);
        if (client instanceof AutoCloseable ac) {
            try {
                ac.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 销毁客户端
     * @param clientId 客户端id
     * @param type 类型
     */
    public static void destroy(Object clientId, String type) {
        if (clientId == null || type == null) {
            return;
        }
        String key = String.valueOf(clientId);
        Object client = CLIENTS.remove(key);
        if (client instanceof AutoCloseable ac) {
            try {
                ac.close();
            } catch (Exception ignored) {
            }
        }
    }
}
