package com.chua.starter.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenPrometheusConfig;

import java.util.List;

/**
 * @author CH
 * @since 2025/6/6 16:50
 */
// Prometheus配置服务接口，继承自通用服务接口IService
public interface MonitorSysGenPrometheusConfigService extends IService<MonitorSysGenPrometheusConfig> {

    /**
     * 根据生成ID列出Prometheus配置
     *
     * @param genId                             生成ID，用于标识特定的Prometheus配置
     * @param monitorSysGenPrometheusConfigType
     * @return 返回Prometheus配置列表
     */
    List<MonitorSysGenPrometheusConfig> listPrometheusConfig(Integer genId, String monitorSysGenPrometheusConfigType);

    /**
     * 保存Prometheus配置
     *
     * @param monitorSysGenPrometheusConfig 要保存的Prometheus配置对象
     * @return 返回保存操作的结果
     */
    ReturnResult<Boolean> savePrometheusConfig(MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig);

    /**
     * 更新Prometheus配置
     *
     * @param monitorSysGenPrometheusConfig 要更新的Prometheus配置对象
     * @return 返回更新操作的结果
     */
    ReturnResult<Boolean> updatePrometheusConfig(MonitorSysGenPrometheusConfig monitorSysGenPrometheusConfig);

    /**
     * 删除Prometheus配置
     *
     * @param id 要删除的Prometheus配置的ID
     * @return 返回删除操作的结果
     */
    ReturnResult<Boolean> deletePrometheusConfig(Integer id);

    /**
     * 共享Prometheus配置
     *
     * @param id 要共享的Prometheus配置的ID
     * @return 返回共享操作的结果
     */
    ReturnResult<Boolean> sharePrometheusConfig(Integer id);

    /**
     * 列出所有共享的Prometheus配置
     *
     * @return 返回所有共享的Prometheus配置列表
     */
    ReturnResult<List<MonitorSysGenPrometheusConfig>> sharePrometheusConfig();
}
