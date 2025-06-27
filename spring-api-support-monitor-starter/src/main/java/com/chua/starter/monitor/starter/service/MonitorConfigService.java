package com.chua.starter.monitor.starter.service;

import java.util.Map;

/**
 * 监控配置服务接口
 * @author CH
 * @since 2024/12/27
 */
public interface MonitorConfigService {

    /**
     * 获取监控阈值配置
     * @return 阈值配置
     */
    Map<String, Object> getMonitorThresholds();

    /**
     * 保存监控阈值配置
     * @param thresholds 阈值配置
     */
    void saveMonitorThresholds(Map<String, Object> thresholds);

    /**
     * 获取全局监控配置
     * @return 全局配置
     */
    Map<String, Object> getGlobalMonitorConfig();

    /**
     * 保存全局监控配置
     * @param config 全局配置
     */
    void saveGlobalMonitorConfig(Map<String, Object> config);

    /**
     * 重置监控配置为默认值
     */
    void resetMonitorConfig();

    /**
     * 测试告警配置
     * @param testData 测试数据
     * @return 测试结果
     */
    boolean testAlertConfig(Map<String, Object> testData);
}
