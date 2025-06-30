package com.chua.starter.monitor.starter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

import java.util.List;
import java.util.Map;

/**
 * 服务器配置设置服务接口
 *
 * @author CH
 * @since 2024/12/26
 */
public interface MonitorSysGenServerSettingService extends IService<MonitorSysGenServerSetting> {

    /**
     * 分页查询服务器配置设置
     *
     * @param page 分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    IPage<MonitorSysGenServerSetting> pageFor(Page<MonitorSysGenServerSetting> page, MonitorSysGenServerSetting entity);

    /**
     * 根据服务器ID获取配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置
     */
    ReturnResult<MonitorSysGenServerSetting> getByServerId(Integer serverId);

    /**
     * 根据服务器ID列表批量获取配置
     *
     * @param serverIds 服务器ID列表
     * @return 服务器配置列表
     */
    ReturnResult<List<MonitorSysGenServerSetting>> getByServerIds(List<Integer> serverIds);

    /**
     * 保存或更新服务器配置
     *
     * @param setting 服务器配置
     * @return 操作结果
     */
    ReturnResult<Boolean> saveOrUpdateSetting(MonitorSysGenServerSetting setting);

    /**
     * 批量保存或更新服务器配置
     *
     * @param settings 服务器配置列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchSaveOrUpdateSettings(List<MonitorSysGenServerSetting> settings);

    /**
     * 根据服务器ID删除配置
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteByServerId(Integer serverId);

    /**
     * 根据ID删除配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    ReturnResult<Boolean> deleteById(Integer id);

    /**
     * 批量删除配置
     *
     * @param ids 配置ID列表
     * @return 操作结果
     */
    ReturnResult<Boolean> batchDeleteByIds(List<Integer> ids);

    /**
     * 批量更新监控启用状态
     *
     * @param serverIds 服务器ID列表
     * @param enabled 是否启用
     * @return 操作结果
     */
    ReturnResult<Boolean> batchUpdateMonitorEnabled(List<Integer> serverIds, Boolean enabled);

    /**
     * 获取启用监控的服务器配置
     *
     * @return 启用监控的服务器配置列表
     */
    ReturnResult<List<MonitorSysGenServerSetting>> getMonitorEnabledSettings();

    /**
     * 获取启用告警的服务器配置
     *
     * @return 启用告警的服务器配置列表
     */
    ReturnResult<List<MonitorSysGenServerSetting>> getAlertEnabledSettings();

    /**
     * 获取启用上报的服务器配置
     *
     * @param reportMethod 上报方式
     * @return 启用上报的服务器配置列表
     */
    ReturnResult<List<MonitorSysGenServerSetting>> getReportEnabledSettings(String reportMethod);

    /**
     * 更新服务器配置状态
     *
     * @param serverId 服务器ID
     * @param status 状态
     * @return 操作结果
     */
    ReturnResult<Boolean> updateStatusByServerId(Integer serverId, Integer status);

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getSettingsStatistics();

    /**
     * 创建默认配置
     *
     * @param serverId 服务器ID
     * @return 默认配置
     */
    ReturnResult<MonitorSysGenServerSetting> createDefaultSetting(Integer serverId);

    /**
     * 复制配置到其他服务器
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerIds 目标服务器ID列表
     * @return 操作结果
     */
    ReturnResult<Boolean> copySettingToServers(Integer sourceServerId, List<Integer> targetServerIds);

    /**
     * 验证配置有效性
     *
     * @param setting 服务器配置
     * @return 验证结果
     */
    ReturnResult<Boolean> validateSetting(MonitorSysGenServerSetting setting);

    /**
     * 获取配置模板
     *
     * @param templateType 模板类型 (basic/advanced/prometheus)
     * @return 配置模板
     */
    ReturnResult<MonitorSysGenServerSetting> getSettingTemplate(String templateType);

    /**
     * 导出配置
     *
     * @param serverIds 服务器ID列表
     * @param format 导出格式 (json/yaml/xml)
     * @return 导出数据
     */
    ReturnResult<String> exportSettings(List<Integer> serverIds, String format);

    /**
     * 导入配置
     *
     * @param configData 配置数据
     * @param format 数据格式 (json/yaml/xml)
     * @param overwrite 是否覆盖现有配置
     * @return 导入结果
     */
    ReturnResult<Boolean> importSettings(String configData, String format, boolean overwrite);

    /**
     * 重置配置为默认值
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    ReturnResult<Boolean> resetToDefault(Integer serverId);

    /**
     * 获取配置变更历史
     *
     * @param serverId 服务器ID
     * @param limit 限制数量
     * @return 变更历史
     */
    ReturnResult<List<Map<String, Object>>> getSettingHistory(Integer serverId, Integer limit);

    /**
     * 测试告警配置
     *
     * @param serverId 服务器ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testAlertConfiguration(Integer serverId);

    /**
     * 测试上报配置
     *
     * @param serverId 服务器ID
     * @return 测试结果
     */
    ReturnResult<Boolean> testReportConfiguration(Integer serverId);

    /**
     * 获取推荐配置
     *
     * @param serverId 服务器ID
     * @return 推荐配置
     */
    ReturnResult<MonitorSysGenServerSetting> getRecommendedSetting(Integer serverId);

    /**
     * 应用配置模板到多个服务器
     *
     * @param templateType 模板类型
     * @param serverIds 服务器ID列表
     * @return 操作结果
     */
    ReturnResult<Boolean> applyTemplateToServers(String templateType, List<Integer> serverIds);

    /**
     * 获取服务器配置或创建默认配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置
     */
    ReturnResult<MonitorSysGenServerSetting> getOrCreateSetting(Integer serverId);
}
