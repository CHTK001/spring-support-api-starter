package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadConfig;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadBatchResult;

import java.util.List;
import java.util.Map;

/**
 * 服务器文件上传配置管理服务接口
 *
 * @author CH
 * @since 2024/12/20
 */
public interface MonitorSysGenServerFileUploadConfigService {

    /**
     * 获取全局上传配置
     *
     * @return 全局配置
     */
    ReturnResult<MonitorSysGenServerFileUploadConfig> getGlobalUploadConfig();

    /**
     * 更新全局上传配置
     *
     * @param config 配置信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateGlobalUploadConfig(MonitorSysGenServerFileUploadConfig config);

    /**
     * 获取服务器上传配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置
     */
    ReturnResult<MonitorSysGenServerFileUploadConfig> getServerUploadConfig(Integer serverId);

    /**
     * 更新服务器上传配置
     *
     * @param serverId 服务器ID
     * @param config   配置信息
     * @return 更新结果
     */
    ReturnResult<Boolean> updateServerUploadConfig(Integer serverId, MonitorSysGenServerFileUploadConfig config);

    /**
     * 删除服务器上传配置
     *
     * @param serverId 服务器ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteServerUploadConfig(Integer serverId);

    /**
     * 获取有效的上传配置（服务器配置优先，否则使用全局配置）
     *
     * @param serverId 服务器ID
     * @return 有效配置
     */
    ReturnResult<MonitorSysGenServerFileUploadConfig> getEffectiveUploadConfig(Integer serverId);

    /**
     * 验证上传配置
     *
     * @param config 配置信息
     * @return 验证结果
     */
    ReturnResult<Map<String, Object>> validateUploadConfig(MonitorSysGenServerFileUploadConfig config);

    /**
     * 获取默认上传配置
     *
     * @return 默认配置
     */
    ReturnResult<MonitorSysGenServerFileUploadConfig> getDefaultUploadConfig();

    /**
     * 重置服务器配置为默认值
     *
     * @param serverId 服务器ID
     * @return 重置结果
     */
    ReturnResult<Boolean> resetServerConfigToDefault(Integer serverId);

    /**
     * 批量更新服务器配置
     *
     * @param serverConfigs 服务器配置列表
     * @return 更新结果
     */
    ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchUpdateServerConfigs(List<MonitorSysGenServerFileUploadConfig> serverConfigs);

    /**
     * 导出配置
     *
     * @param serverId 服务器ID（可选，为空则导出全局配置）
     * @return 导出结果
     */
    ReturnResult<String> exportConfig(Integer serverId);

    /**
     * 导入配置
     *
     * @param configData 配置数据
     * @param serverId   服务器ID（可选，为空则导入为全局配置）
     * @return 导入结果
     */
    ReturnResult<Boolean> importConfig(String configData, Integer serverId);

    /**
     * 获取配置历史
     *
     * @param serverId 服务器ID（可选）
     * @param limit    返回数量限制
     * @return 配置历史
     */
    ReturnResult<List<Map<String, Object>>> getConfigHistory(Integer serverId, Integer limit);

    /**
     * 回滚配置到指定版本
     *
     * @param serverId  服务器ID（可选）
     * @param versionId 版本ID
     * @return 回滚结果
     */
    ReturnResult<Boolean> rollbackConfig(Integer serverId, Long versionId);

    /**
     * 获取配置模板
     *
     * @return 配置模板列表
     */
    ReturnResult<List<Map<String, Object>>> getConfigTemplates();

    /**
     * 创建配置模板
     *
     * @param templateName 模板名称
     * @param config       配置信息
     * @return 创建结果
     */
    ReturnResult<Boolean> createConfigTemplate(String templateName, Map<String, Object> config);

    /**
     * 应用配置模板
     *
     * @param serverId   服务器ID
     * @param templateId 模板ID
     * @return 应用结果
     */
    ReturnResult<Boolean> applyConfigTemplate(Integer serverId, Long templateId);

    /**
     * 删除配置模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    ReturnResult<Boolean> deleteConfigTemplate(Long templateId);

    /**
     * 检查配置兼容性
     *
     * @param serverId 服务器ID
     * @param config   配置信息
     * @return 兼容性检查结果
     */
    ReturnResult<Map<String, Object>> checkConfigCompatibility(Integer serverId, Map<String, Object> config);

    /**
     * 获取配置建议
     *
     * @param serverId 服务器ID
     * @return 配置建议
     */
    ReturnResult<List<Map<String, Object>>> getConfigRecommendations(Integer serverId);

    /**
     * 优化配置
     *
     * @param serverId 服务器ID
     * @return 优化结果
     */
    ReturnResult<Map<String, Object>> optimizeConfig(Integer serverId);

    /**
     * 监控配置变更
     *
     * @return 配置变更监控结果
     */
    ReturnResult<List<Map<String, Object>>> monitorConfigChanges();

    /**
     * 同步配置到其他服务器
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerIds 目标服务器ID列表
     * @return 同步结果
     */
    ReturnResult<Map<String, Object>> syncConfigToServers(Integer sourceServerId, List<Integer> targetServerIds);

    /**
     * 备份配置
     *
     * @param serverId 服务器ID（可选）
     * @return 备份结果
     */
    ReturnResult<String> backupConfig(Integer serverId);

    /**
     * 恢复配置
     *
     * @param serverId   服务器ID（可选）
     * @param backupData 备份数据
     * @return 恢复结果
     */
    ReturnResult<Boolean> restoreConfig(Integer serverId, String backupData);

    /**
     * 获取配置差异
     *
     * @param serverId1 服务器1ID
     * @param serverId2 服务器2ID
     * @return 配置差异
     */
    ReturnResult<Map<String, Object>> getConfigDifference(Integer serverId1, Integer serverId2);

    /**
     * 合并配置
     *
     * @param baseConfig   基础配置
     * @param mergeConfig  合并配置
     * @param mergeStrategy 合并策略
     * @return 合并结果
     */
    ReturnResult<Map<String, Object>> mergeConfigs(Map<String, Object> baseConfig, 
                                                   Map<String, Object> mergeConfig, 
                                                   String mergeStrategy);

    /**
     * 测试配置
     *
     * @param serverId 服务器ID
     * @param config   配置信息
     * @return 测试结果
     */
    ReturnResult<Map<String, Object>> testConfig(Integer serverId, Map<String, Object> config);

    /**
     * 获取配置使用统计
     *
     * @return 使用统计
     */
    ReturnResult<Map<String, Object>> getConfigUsageStatistics();

    /**
     * 清理无效配置
     *
     * @return 清理结果
     */
    ReturnResult<Map<String, Object>> cleanupInvalidConfigs();
}
