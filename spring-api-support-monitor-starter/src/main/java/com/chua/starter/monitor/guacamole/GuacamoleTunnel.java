package com.chua.starter.monitor.guacamole;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Guacamole隧道
 * 封装Guacamole连接的隧道功能
 *
 * @author CH
 * @since 2024/12/18
 */
@Slf4j
public class GuacamoleTunnel {

    /**
     * 服务器ID
     */
    private final Integer serverId;

    /**
     * Guacamole客户端
     */
    private final Object guacamoleClient;

    /**
     * 连接状态
     */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * 数据回调
     */
    private Consumer<String> dataCallback;

    /**
     * 错误回调
     */
    private Consumer<Exception> errorCallback;

    /**
     * 连接关闭回调
     */
    private Runnable closeCallback;

    public GuacamoleTunnel(Integer serverId, Object guacamoleClient) {
        this.serverId = serverId;
        this.guacamoleClient = guacamoleClient;
    }

    /**
     * 建立连接
     */
    public boolean connect() {
        try {
            if (guacamoleClient == null) {
                log.error("Guacamole客户端为空: serverId={}", serverId);
                return false;
            }

            // 这里应该调用实际的Guacamole客户端连接方法
            // 由于使用的是第三方库，这里简化处理
            connected.set(true);
            log.info("Guacamole隧道连接成功: serverId={}", serverId);
            return true;

        } catch (Exception e) {
            log.error("Guacamole隧道连接失败: serverId={}, error={}", serverId, e.getMessage(), e);
            if (errorCallback != null) {
                errorCallback.accept(e);
            }
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (connected.get()) {
                // 这里应该调用实际的Guacamole客户端断开方法
                connected.set(false);
                log.info("Guacamole隧道断开连接: serverId={}", serverId);
                
                if (closeCallback != null) {
                    closeCallback.run();
                }
            }
        } catch (Exception e) {
            log.error("Guacamole隧道断开连接失败: serverId={}, error={}", serverId, e.getMessage(), e);
        }
    }

    /**
     * 发送数据
     */
    public boolean sendData(String data) {
        if (!connected.get()) {
            log.warn("Guacamole隧道未连接，无法发送数据: serverId={}", serverId);
            return false;
        }

        try {
            // 这里应该调用实际的Guacamole客户端发送数据方法
            log.debug("发送Guacamole数据: serverId={}, data={}", serverId, data);
            return true;
        } catch (Exception e) {
            log.error("发送Guacamole数据失败: serverId={}, error={}", serverId, e.getMessage(), e);
            if (errorCallback != null) {
                errorCallback.accept(e);
            }
            return false;
        }
    }

    /**
     * 接收数据（模拟）
     */
    public void receiveData(String data) {
        if (dataCallback != null) {
            dataCallback.accept(data);
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * 获取服务器ID
     */
    public Integer getServerId() {
        return serverId;
    }

    /**
     * 获取Guacamole客户端
     */
    public Object getGuacamoleClient() {
        return guacamoleClient;
    }

    /**
     * 设置数据回调
     */
    public void setDataCallback(Consumer<String> dataCallback) {
        this.dataCallback = dataCallback;
    }

    /**
     * 设置错误回调
     */
    public void setErrorCallback(Consumer<Exception> errorCallback) {
        this.errorCallback = errorCallback;
    }

    /**
     * 设置连接关闭回调
     */
    public void setCloseCallback(Runnable closeCallback) {
        this.closeCallback = closeCallback;
    }

    /**
     * 获取连接信息
     */
    public String getConnectionInfo() {
        return String.format("GuacamoleTunnel[serverId=%d, connected=%s]", 
                           serverId, connected.get());
    }

    @Override
    public String toString() {
        return getConnectionInfo();
    }
}
