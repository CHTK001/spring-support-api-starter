package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogConfig;

import java.util.List;
import java.util.Map;

/**
 * 服务器日志配置管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerLogConfigService extends IService<MonitorSysGenServerLogConfig> {

    /**
     * 根据服务器ID获取日志配置
     *
     * @param serverId 服务器ID
     * @return 日志配置
     */
    ReturnResult<MonitorSysGenServerLogConfig> getConfigByServerId(Integer serverId);

    /**
     * 保存或更新日志配置
     *
     * @param config 日志配置
     * @return 保存结果
     */
    ReturnResult<MonitorSysGenServerLogConfig> saveOrUpdateConfig(MonitorSysGenServerLogConfig config);

    /**
     * 获取所有启用的日志配置
     *
     * @return 启用的日志配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getEnabledConfigs();

    /**
     * 获取启用实时日志的配置
     *
     * @return 启用实时日志的配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getRealTimeEnabledConfigs();

    /**
     * 根据保留天数获取配置
     *
     * @param retentionDays 保留天数
     * @return 配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByRetentionDays(Integer retentionDays);

    /**
     * 获取需要清理的配置
     *
     * @return 需要清理的配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsForCleanup();

    /**
     * 更新配置状态
     *
     * @param serverId 服务器ID
     * @param status   状态
     * @return 更新结果
     */
    ReturnResult<Boolean> updateConfigStatus(Integer serverId, Integer status);

    /**
     * 更新日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新结果
     */
    ReturnResult<Boolean> updateLogEnabled(Integer serverId, Integer enabled);

    /**
     * 更新实时日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新结果
     */
    ReturnResult<Boolean> updateRealTimeEnabled(Integer serverId, Integer enabled);

    /**
     * 批量更新配置状态
     *
     * @param serverIds 服务器ID列表
     * @param status    状态
     * @return 更新结果
     */
    ReturnResult<Boolean> batchUpdateConfigStatus(List<Integer> serverIds, Integer status);

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getConfigStatistics();

    /**
     * 根据收集间隔获取配置
     *
     * @param interval 收集间隔
     * @return 配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByCollectionInterval(Integer interval);

    /**
     * 获取过期配置（长时间未更新）
     *
     * @param days 天数
     * @return 过期配置列表
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> getExpiredConfigs(Integer days);

    /**
     * 检查服务器是否有日志配置
     *
     * @param serverId 服务器ID
     * @return 是否存在配置
     */
    ReturnResult<Boolean> hasConfigByServerId(Integer serverId);

    /**
     * 获取默认配置模板
     *
     * @return 默认配置
     */
    ReturnResult<MonitorSysGenServerLogConfig> getDefaultTemplate();

    /**
     * 为服务器创建默认配置
     *
     * @param serverId 服务器ID
     * @return 创建结果
     */
    ReturnResult<MonitorSysGenServerLogConfig> createDefaultConfig(Integer serverId);

    /**
     * 复制配置到其他服务器
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerIds 目标服务器ID列表
     * @return 复制结果
     */
    ReturnResult<Boolean> copyConfigToServers(Integer sourceServerId, List<Integer> targetServerIds);

    /**
     * 验证配置有效性
     *
     * @param config 配置
     * @return 验证结果
     */
    ReturnResult<Map<String, Object>> validateConfig(MonitorSysGenServerLogConfig config);

    /**
     * 重置配置为默认值
     *
     * @param serverId 服务器ID
     * @return 重置结果
     */
    ReturnResult<MonitorSysGenServerLogConfig> resetConfigToDefault(Integer serverId);

    /**
     * 导出配置
     *
     * @param serverIds 服务器ID列表
     * @param format    导出格式
     * @return 导出结果
     */
    ReturnResult<String> exportConfigs(List<Integer> serverIds, String format);

    /**
     * 导入配置
     *
     * @param configData 配置数据
     * @param format     数据格式
     * @return 导入结果
     */
    ReturnResult<List<MonitorSysGenServerLogConfig>> importConfigs(String configData, String format);

    /**
     * 获取配置变更历史
     *
     * @param serverId 服务器ID
     * @return 变更历史
     */
    ReturnResult<List<Map<String, Object>>> getConfigChangeHistory(Integer serverId);

    /**
     * 应用配置模板
     *
     * @param templateId  模板ID
     * @param serverIds   服务器ID列表
     * @return 应用结果
     */
    ReturnResult<Boolean> applyConfigTemplate(Long templateId, List<Integer> serverIds);
}
