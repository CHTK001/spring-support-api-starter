package com.chua.starter.tenant.listener;

/**
 * 监听器
 *
 * @author CH
 */
public interface MessageListener {

    /**
     * 监听数据
     *
     * @param message 数据
     */
    void listener(String message);
}
