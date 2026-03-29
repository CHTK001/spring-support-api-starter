package com.chua.starter.monitor.performance;

/**
 * 系统性能监控服务。
 *
 * @author CH
 * @since 2026/3/28
 */
public interface SystemPerformanceService {

    /**
     * 获取当前性能监控配置。
     *
     * @return 配置快照
     */
    SystemPerformanceConfig getConfig();

    /**
     * 获取当前系统性能快照。
     *
     * @param broadcast 是否按配置广播
     * @return 性能快照
     */
    SystemPerformanceSnapshot getSnapshot(boolean broadcast);

    /**
     * 强制广播当前快照。
     *
     * @return 性能快照
     */
    SystemPerformanceSnapshot broadcastSnapshot();
}
