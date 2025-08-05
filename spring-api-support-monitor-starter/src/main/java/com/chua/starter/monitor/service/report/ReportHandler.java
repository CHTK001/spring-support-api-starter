package com.chua.starter.monitor.service.report;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.common.support.lang.code.ReturnResult;

import java.util.Map;

/**
 * 数据上报处理器接口
 * 定义统一的上报处理规范，不同的上报方式实现不同的处理逻辑
 * 
 * @author CH
 * @since 2024/12/25
 */
public interface ReportHandler {

    /**
     * 获取上报方式类型
     * 
     * @return 上报方式类型 (LOCAL, API, PROMETHEUS, NONE)
     */
    String getReportType();

    /**
     * 判断是否支持指定的服务器
     * 
     * @param server 服务器配置
     * @return 是否支持
     */
    boolean supports(MonitorSysGenServer server);

    /**
     * 处理数据上报
     * 
     * @param server 服务器配置
     * @param reportData 上报的数据（可选，某些方式不需要外部数据）
     * @return 处理结果
     */
    ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData);

    /**
     * 启动数据收集任务
     * 
     * @param server 服务器配置
     * @return 启动结果
     */
    ReturnResult<Boolean> startDataCollection(MonitorSysGenServer server);

    /**
     * 停止数据收集任务
     * 
     * @param serverId 服务器ID
     * @return 停止结果
     */
    ReturnResult<Boolean> stopDataCollection(Integer serverId);

    /**
     * 检查服务器配置是否有效
     * 
     * @param server 服务器配置
     * @return 检查结果
     */
    ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server);

    /**
     * 获取上报方式的描述信息
     * 
     * @return 描述信息
     */
    String getDescription();

    /**
     * 获取上报方式的优先级
     * 数值越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 是否需要定时任务
     * 
     * @return 是否需要定时任务
     */
    default boolean requiresScheduledTask() {
        return true;
    }

    /**
     * 获取默认监控间隔（秒）
     * 
     * @return 默认监控间隔
     */
    default int getDefaultMonitorInterval() {
        return 30;
    }
}
