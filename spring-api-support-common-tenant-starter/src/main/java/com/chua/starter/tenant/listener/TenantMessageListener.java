package com.chua.starter.tenant.listener;

/**
 * 监听器
 *
 * @author CH
 */
public interface TenantMessageListener {

    /**
     * 监听数据
     *
     * @param message 数据
     */
    void listen(String message);
}
