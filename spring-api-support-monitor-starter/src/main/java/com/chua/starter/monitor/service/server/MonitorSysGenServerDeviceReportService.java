package com.chua.starter.monitor.service.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.DeviceMetrics;

import java.util.Map;

/**
 * 服务器设备数据上报服务接口
 * 
 * @author CH
 * @since 2024/12/19
 */
public interface MonitorSysGenServerDeviceReportService {

    /**
     * 处理设备指标数据上报
     * 根据IP地址匹配服务器，如果存在相同IP的服务器则关联数据
     *
     * @param metrics 设备指标数据
     * @return 处理结果
     */
    ReturnResult<String> handleDeviceMetrics(DeviceMetrics metrics);

    /**
     * 处理设备指标数据上报（Map格式）
     * 根据IP地址匹配服务器，如果存在相同IP的服务器则关联数据
     *
     * @param metricsData 设备指标数据
     * @return 处理结果
     */
    ReturnResult<String> handleDeviceMetrics(Map<String, Object> metricsData);

    /**
     * 获取设备上报统计信息
     * 
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getDeviceReportStatistics();

    /**
     * 获取指定IP的设备上报历史
     * 
     * @param deviceIp 设备IP地址
     * @param limit 限制数量
     * @return 上报历史
     */
    ReturnResult<Map<String, Object>> getDeviceReportHistory(String deviceIp, Integer limit);

    /**
     * 清理过期的设备上报数据
     * 
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ReturnResult<String> cleanupExpiredDeviceReports(Integer retentionDays);

    /**
     * 获取活跃设备列表
     * 
     * @param hours 活跃时间范围（小时）
     * @return 活跃设备列表
     */
    ReturnResult<Map<String, Object>> getActiveDevices(Integer hours);

    /**
     * 获取设备上报频率统计
     * 
     * @return 频率统计
     */
    ReturnResult<Map<String, Object>> getDeviceReportFrequencyStats();

    /**
     * 处理批量设备指标数据上报
     * 
     * @param batchMetrics 批量设备指标数据
     * @return 处理结果
     */
    ReturnResult<String> handleBatchDeviceMetrics(Map<String, Object> batchMetrics);

    /**
     * 获取设备上报错误统计
     * 
     * @return 错误统计
     */
    ReturnResult<Map<String, Object>> getDeviceReportErrorStats();

    /**
     * 重置设备上报统计
     * 
     * @param deviceIp 设备IP地址，为空则重置所有
     * @return 重置结果
     */
    ReturnResult<String> resetDeviceReportStats(String deviceIp);

    /**
     * 获取设备上报配置
     * 
     * @param deviceIp 设备IP地址
     * @return 配置信息
     */
    ReturnResult<Map<String, Object>> getDeviceReportConfig(String deviceIp);

    /**
     * 更新设备上报配置
     * 
     * @param deviceIp 设备IP地址
     * @param config 配置信息
     * @return 更新结果
     */
    ReturnResult<String> updateDeviceReportConfig(String deviceIp, Map<String, Object> config);
}
