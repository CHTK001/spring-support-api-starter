package com.chua.starter.tenant.listener;

/**
 * 监听器
 *
 * @author CH
 */
public interface ServiceMessageListener {

    /**
     * 监听数据
     *
     * @param message 数据
     */
    void listen(String message);
}
