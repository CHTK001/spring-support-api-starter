package com.chua.starter.monitor.service.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.starter.monitor.entity.MonitorSysGenConfig;
import com.chua.starter.monitor.pojo.ConfigManagementDTO;

import java.util.List;
import java.util.Map;

/**
 * 配置管理服务接口
 * 
 * @author CH
 * @since 2024/12/16
 */
public interface MonitorSysGenConfigService {

    /**
     * 分页查询配置列表
     */
    IPage<MonitorSysGenConfig> getConfigPage(ConfigManagementDTO.ConfigQueryDTO queryDTO);

    /**
     * 根据ID获取配置详情
     */
    MonitorSysGenConfig getConfigById(Integer configId);

    /**
     * 创建配置
     */
    MonitorSysGenConfig createConfig(MonitorSysGenConfig config);

    /**
     * 更新配置
     */
    MonitorSysGenConfig updateConfig(MonitorSysGenConfig config);

    /**
     * 删除配置
     */
    boolean deleteConfig(Integer configId);

    /**
     * 批量删除配置
     */
    boolean batchDeleteConfigs(List<Integer> configIds);

    /**
     * 启用/禁用配置
     */
    boolean updateConfigStatus(Integer configId, Integer status);

    /**
     * 获取所有环境列表
     */
    List<String> getAllEnvs();

    /**
     * 获取所有应用列表
     */
    List<String> getAllApps();

    /**
     * 获取配置统计信息
     */
    Map<String, Object> getConfigStatistics();

    /**
     * 根据环境获取配置列表
     */
    List<MonitorSysGenConfig> getConfigsByEnv(String env);

    /**
     * 根据应用获取配置列表
     */
    List<MonitorSysGenConfig> getConfigsByApp(String app);

    /**
     * 检查配置键是否可用（同一环境下唯一）
     */
    boolean isConfigKeyAvailable(String configKey, String env, Integer excludeId);

    /**
     * 下发配置到指定服务器
     */
    ConfigManagementDTO.ConfigPushResult pushConfigToNodes(ConfigManagementDTO.ConfigPushRequest request);

    /**
     * 获取配置下发历史
     */
    IPage<ConfigManagementDTO.ConfigPushHistory> getPushHistory(ConfigManagementDTO.PushHistoryQueryDTO queryDTO);

    /**
     * 从历史记录还原推送
     */
    ConfigManagementDTO.ConfigPushResult repushFromHistory(Long historyId);

    /**
     * 批量从历史记录还原推送
     */
    ConfigManagementDTO.ConfigPushResult batchRepushFromHistory(List<Long> historyIds);

    /**
     * 删除推送历史
     */
    boolean deletePushHistory(Long historyId);

    /**
     * 批量删除推送历史
     */
    boolean batchDeletePushHistory(List<Long> historyIds);

    /**
     * 导出配置
     */
    List<MonitorSysGenConfig> exportConfigs(List<Integer> configIds);

    /**
     * 导入配置
     */
    int importConfigs(List<MonitorSysGenConfig> configs);
}
