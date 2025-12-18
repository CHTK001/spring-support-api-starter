package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chua.starter.monitor.entity.MonitorSysGenConfig;
import com.chua.starter.monitor.entity.MonitorSysGenConfigPushHistory;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.mapper.MonitorSysGenConfigMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenConfigPushHistoryMapper;
import com.chua.starter.monitor.pojo.ConfigManagementDTO;
import com.chua.starter.monitor.service.config.MonitorSysGenConfigService;
import com.chua.starter.monitor.service.server.MonitorSysGenServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 配置管理服务实现
 *
 * @author CH
 * @since 2024/12/16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorSysGenConfigServiceImpl implements MonitorSysGenConfigService {

    private final MonitorSysGenConfigMapper configMapper;
    private final MonitorSysGenConfigPushHistoryMapper pushHistoryMapper;
    private final MonitorSysGenServerService serverService;

    @Override
    public IPage<MonitorSysGenConfig> getConfigPage(ConfigManagementDTO.ConfigQueryDTO queryDTO) {
        // 构建查询条件 - 链式处理
        LambdaQueryWrapper<MonitorSysGenConfig> wrapper = new LambdaQueryWrapper<MonitorSysGenConfig>()
                .and(StringUtils.hasText(queryDTO.getKeyword()), w -> w
                        .like(MonitorSysGenConfig::getMonitorSysGenConfigKey, queryDTO.getKeyword())
                        .or()
                        .like(MonitorSysGenConfig::getMonitorSysGenConfigValue, queryDTO.getKeyword())
                        .or()
                        .like(MonitorSysGenConfig::getMonitorSysGenConfigDescription, queryDTO.getKeyword()))
                .eq(StringUtils.hasText(queryDTO.getEnv()), MonitorSysGenConfig::getMonitorSysGenConfigEnv, queryDTO.getEnv())
                .eq(StringUtils.hasText(queryDTO.getApp()), MonitorSysGenConfig::getMonitorSysGenConfigApp, queryDTO.getApp())
                .eq(queryDTO.getStatus() != null, MonitorSysGenConfig::getMonitorSysGenConfigStatus, queryDTO.getStatus())
                .orderByDesc(MonitorSysGenConfig::getUpdateTime);

        // 分页查询
        return configMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
    }

    @Override
    public MonitorSysGenConfig getConfigById(Integer configId) {
        if (configId == null) {
            log.warn("配置ID不能为空");
            return null;
        }

        MonitorSysGenConfig config = configMapper.selectById(configId);
        if (config == null) {
            log.warn("配置不存在: configId={}", configId);
            return null;
        }

        log.info("获取配置详情成功: configId={}, configKey={}", configId, config.getMonitorSysGenConfigKey());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenConfig createConfig(MonitorSysGenConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置信息不能为空");
        }

        // 验证配置键唯一性（同一环境下）
        if (!isConfigKeyAvailable(config.getMonitorSysGenConfigKey(), config.getMonitorSysGenConfigEnv(), null)) {
            throw new IllegalArgumentException("配置键在该环境下已存在: " + config.getMonitorSysGenConfigKey());
        }

        // 设置默认值
        if (config.getMonitorSysGenConfigStatus() == null) {
            config.setMonitorSysGenConfigStatus(1); // 默认启用
        }

        // 保存配置
        int result = configMapper.insert(config);
        if (result > 0) {
            log.info("创建配置成功: configId={}, configKey={}", config.getMonitorSysGenConfigId(),
                    config.getMonitorSysGenConfigKey());
            return config;
        } else {
            throw new RuntimeException("创建配置失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorSysGenConfig updateConfig(MonitorSysGenConfig config) {
        if (config == null || config.getMonitorSysGenConfigId() == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }

        // 检查配置是否存在
        MonitorSysGenConfig existingConfig = configMapper.selectById(config.getMonitorSysGenConfigId());
        if (existingConfig == null) {
            throw new IllegalArgumentException("配置不存在: " + config.getMonitorSysGenConfigId());
        }

        // 验证配置键唯一性（同一环境下，排除当前配置）
        if (StringUtils.hasText(config.getMonitorSysGenConfigKey())
                && !isConfigKeyAvailable(config.getMonitorSysGenConfigKey(), 
                        config.getMonitorSysGenConfigEnv() != null ? config.getMonitorSysGenConfigEnv() : existingConfig.getMonitorSysGenConfigEnv(),
                        config.getMonitorSysGenConfigId())) {
            throw new IllegalArgumentException("配置键在该环境下已存在: " + config.getMonitorSysGenConfigKey());
        }

        // 更新配置
        int result = configMapper.updateById(config);
        if (result > 0) {
            log.info("更新配置成功: configId={}, configKey={}", config.getMonitorSysGenConfigId(),
                    config.getMonitorSysGenConfigKey());
            return configMapper.selectById(config.getMonitorSysGenConfigId());
        } else {
            throw new RuntimeException("更新配置失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Integer configId) {
        if (configId == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }

        // 检查配置是否存在
        MonitorSysGenConfig config = configMapper.selectById(configId);
        if (config == null) {
            log.warn("配置不存在: configId={}", configId);
            return false;
        }

        // 删除配置
        int result = configMapper.deleteById(configId);
        if (result > 0) {
            log.info("删除配置成功: configId={}, configKey={}", configId, config.getMonitorSysGenConfigKey());
            return true;
        } else {
            throw new RuntimeException("删除配置失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteConfigs(List<Integer> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            throw new IllegalArgumentException("配置ID列表不能为空");
        }

        int result = configMapper.deleteBatchIds(configIds);
        if (result > 0) {
            log.info("批量删除配置成功: count={}", result);
            return true;
        } else {
            log.warn("批量删除配置失败: configIds={}", configIds);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigStatus(Integer configId, Integer status) {
        if (configId == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }

        MonitorSysGenConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new IllegalArgumentException("配置不存在: " + configId);
        }

        config.setMonitorSysGenConfigStatus(status);
        int result = configMapper.updateById(config);
        if (result > 0) {
            log.info("更新配置状态成功: configId={}, status={}", configId, status);
            return true;
        } else {
            throw new RuntimeException("更新配置状态失败");
        }
    }

    @Override
    public List<String> getAllEnvs() {
        return configMapper.findAllEnvs();
    }

    @Override
    public List<String> getAllApps() {
        return configMapper.findAllApps();
    }

    @Override
    public Map<String, Object> getConfigStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 基本统计
        MonitorSysGenConfigMapper.ConfigStatistics configStats = configMapper.getConfigStatistics();
        if (configStats != null) {
            statistics.put("total", configStats.getTotal() != null ? configStats.getTotal() : 0);
            statistics.put("enabled", configStats.getEnabled() != null ? configStats.getEnabled() : 0);
            statistics.put("disabled", configStats.getDisabled() != null ? configStats.getDisabled() : 0);
        } else {
            statistics.put("total", 0);
            statistics.put("enabled", 0);
            statistics.put("disabled", 0);
        }

        // 环境统计
        List<MonitorSysGenConfigMapper.EnvConfigCount> envStats = configMapper.getEnvConfigStatistics();
        Map<String, Long> envStatsMap = new HashMap<>();
        if (envStats != null) {
            for (MonitorSysGenConfigMapper.EnvConfigCount envCount : envStats) {
                envStatsMap.put(envCount.getEnv(), envCount.getCount());
            }
        }
        statistics.put("envStats", envStatsMap);

        return statistics;
    }

    @Override
    public List<MonitorSysGenConfig> getConfigsByEnv(String env) {
        if (!StringUtils.hasText(env)) {
            return Collections.emptyList();
        }
        return configMapper.findByEnv(env);
    }

    @Override
    public List<MonitorSysGenConfig> getConfigsByApp(String app) {
        if (!StringUtils.hasText(app)) {
            return Collections.emptyList();
        }
        return configMapper.findByApp(app);
    }

    @Override
    public boolean isConfigKeyAvailable(String configKey, String env, Integer excludeId) {
        if (!StringUtils.hasText(configKey)) {
            return false;
        }

        // 链式处理
        LambdaQueryWrapper<MonitorSysGenConfig> wrapper = new LambdaQueryWrapper<MonitorSysGenConfig>()
                .eq(MonitorSysGenConfig::getMonitorSysGenConfigKey, configKey)
                .eq(StringUtils.hasText(env), MonitorSysGenConfig::getMonitorSysGenConfigEnv, env)
                .and(!StringUtils.hasText(env), w -> w
                        .isNull(MonitorSysGenConfig::getMonitorSysGenConfigEnv)
                        .or()
                        .eq(MonitorSysGenConfig::getMonitorSysGenConfigEnv, ""))
                .ne(excludeId != null, MonitorSysGenConfig::getMonitorSysGenConfigId, excludeId);

        return configMapper.selectCount(wrapper) == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigManagementDTO.ConfigPushResult pushConfigToNodes(ConfigManagementDTO.ConfigPushRequest request) {
        List<Integer> configIds = request.getConfigIds();
        List<Integer> serverIds = request.getServerIds();

        if (configIds == null || configIds.isEmpty()) {
            throw new IllegalArgumentException("配置ID列表不能为空");
        }
        if (serverIds == null || serverIds.isEmpty()) {
            throw new IllegalArgumentException("服务器ID列表不能为空");
        }

        // 获取配置列表
        List<MonitorSysGenConfig> configs = configMapper.selectBatchIds(configIds);
        if (configs.isEmpty()) {
            throw new IllegalArgumentException("未找到指定的配置");
        }

        // 获取服务器列表
        List<MonitorSysGenServer> servers = serverService.listByIds(serverIds);
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("未找到指定的服务器");
        }

        List<ConfigManagementDTO.NodePushResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        // 向每个服务器推送每个配置
        for (MonitorSysGenServer server : servers) {
            for (MonitorSysGenConfig config : configs) {
                if (!config.isEnabled()) {
                    continue;
                }

                ConfigManagementDTO.NodePushResult nodeResult = ConfigManagementDTO.NodePushResult.builder()
                        .serverId(server.getMonitorSysGenServerId())
                        .serverName(server.getMonitorSysGenServerName())
                        .build();

                String errorMessage = null;
                boolean pushResult = false;

                try {
                    // 通过 SyncServer 下发配置
                    pushResult = pushConfigToServer(server, config.getMonitorSysGenConfigKey(), config.getMonitorSysGenConfigValue());
                    nodeResult.setSuccess(pushResult);
                    if (pushResult) {
                        successCount++;
                        log.info("配置下发成功: configKey={}, serverId={}", config.getMonitorSysGenConfigKey(), server.getMonitorSysGenServerId());
                    } else {
                        failedCount++;
                        errorMessage = "配置下发失败";
                        nodeResult.setErrorMessage(errorMessage);
                        log.warn("配置下发失败: configKey={}, serverId={}", config.getMonitorSysGenConfigKey(), server.getMonitorSysGenServerId());
                    }
                } catch (Exception e) {
                    failedCount++;
                    nodeResult.setSuccess(false);
                    errorMessage = e.getMessage();
                    nodeResult.setErrorMessage(errorMessage);
                    log.error("配置下发异常: configKey={}, serverId={}", config.getMonitorSysGenConfigKey(), server.getMonitorSysGenServerId(), e);
                }

                // 保存推送历史
                savePushHistory(config, server, pushResult, errorMessage);
                results.add(nodeResult);
            }
        }

        return ConfigManagementDTO.ConfigPushResult.builder()
                .total(configs.size() * servers.size())
                .success(successCount)
                .failed(failedCount)
                .results(results)
                .build();
    }

    /**
     * 保存推送历史
     */
    private void savePushHistory(MonitorSysGenConfig config, MonitorSysGenServer server, boolean success, String errorMessage) {
        MonitorSysGenConfigPushHistory history = new MonitorSysGenConfigPushHistory();
        history.setMonitorSysGenConfigId(config.getMonitorSysGenConfigId());
        history.setMonitorSysGenConfigKey(config.getMonitorSysGenConfigKey());
        history.setMonitorSysGenConfigValue(config.getMonitorSysGenConfigValue());
        history.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
        history.setMonitorSysGenServerName(server.getMonitorSysGenServerName());
        history.setPushSuccess(success ? 1 : 0);
        history.setErrorMessage(errorMessage);
        history.setPushTime(LocalDateTime.now());
        // TODO: 获取当前用户
        history.setOperator("system");
        pushHistoryMapper.insert(history);
    }

    /**
     * 向单个服务器推送单个配置
     */
    private boolean pushConfigToServer(MonitorSysGenServer server, String configKey, String configValue) {
        // TODO: 实现通过 SyncServer 下发配置的逻辑
        // 1. 建立与服务器的连接
        // 2. 发送配置数据
        // 3. 等待确认
        log.info("向服务器推送配置: serverId={}, configKey={}", server.getMonitorSysGenServerId(), configKey);
        return true;
    }

    @Override
    public IPage<ConfigManagementDTO.ConfigPushHistory> getPushHistory(ConfigManagementDTO.PushHistoryQueryDTO queryDTO) {
        // 链式查询
        LambdaQueryWrapper<MonitorSysGenConfigPushHistory> wrapper = new LambdaQueryWrapper<MonitorSysGenConfigPushHistory>()
                .eq(queryDTO.getConfigId() != null, MonitorSysGenConfigPushHistory::getMonitorSysGenConfigId, queryDTO.getConfigId())
                .eq(queryDTO.getServerId() != null, MonitorSysGenConfigPushHistory::getMonitorSysGenServerId, queryDTO.getServerId())
                .eq(queryDTO.getPushSuccess() != null, MonitorSysGenConfigPushHistory::getPushSuccess, queryDTO.getPushSuccess())
                .like(StringUtils.hasText(queryDTO.getKeyword()), MonitorSysGenConfigPushHistory::getMonitorSysGenConfigKey, queryDTO.getKeyword())
                .orderByDesc(MonitorSysGenConfigPushHistory::getPushTime);

        Page<MonitorSysGenConfigPushHistory> entityPage = pushHistoryMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        // 转换为 DTO
        Page<ConfigManagementDTO.ConfigPushHistory> dtoPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        dtoPage.setTotal(entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(this::convertToHistoryDTO)
                .toList());
        return dtoPage;
    }

    /**
     * 将实体转换为 DTO
     */
    private ConfigManagementDTO.ConfigPushHistory convertToHistoryDTO(MonitorSysGenConfigPushHistory entity) {
        return ConfigManagementDTO.ConfigPushHistory.builder()
                .id(entity.getId())
                .configId(entity.getMonitorSysGenConfigId())
                .configKey(entity.getMonitorSysGenConfigKey())
                .configValue(entity.getMonitorSysGenConfigValue())
                .serverId(entity.getMonitorSysGenServerId())
                .serverName(entity.getMonitorSysGenServerName())
                .success(entity.isSuccess())
                .errorMessage(entity.getErrorMessage())
                .pushTime(entity.getPushTime())
                .operator(entity.getOperator())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigManagementDTO.ConfigPushResult repushFromHistory(Long historyId) {
        if (historyId == null) {
            throw new IllegalArgumentException("历史ID不能为空");
        }

        MonitorSysGenConfigPushHistory history = pushHistoryMapper.selectById(historyId);
        if (history == null) {
            throw new IllegalArgumentException("历史记录不存在: " + historyId);
        }

        // 获取服务器
        MonitorSysGenServer server = serverService.getById(history.getMonitorSysGenServerId());
        if (server == null) {
            throw new IllegalArgumentException("服务器不存在: " + history.getMonitorSysGenServerId());
        }

        List<ConfigManagementDTO.NodePushResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        ConfigManagementDTO.NodePushResult nodeResult = ConfigManagementDTO.NodePushResult.builder()
                .serverId(server.getMonitorSysGenServerId())
                .serverName(server.getMonitorSysGenServerName())
                .build();

        String errorMessage = null;
        boolean pushResult = false;

        try {
            // 使用历史记录中的配置值进行推送
            pushResult = pushConfigToServer(server, history.getMonitorSysGenConfigKey(), history.getMonitorSysGenConfigValue());
            nodeResult.setSuccess(pushResult);
            if (pushResult) {
                successCount++;
                log.info("从历史还原推送成功: historyId={}, configKey={}", historyId, history.getMonitorSysGenConfigKey());
            } else {
                failedCount++;
                errorMessage = "从历史还原推送失败";
                nodeResult.setErrorMessage(errorMessage);
            }
        } catch (Exception e) {
            failedCount++;
            nodeResult.setSuccess(false);
            errorMessage = e.getMessage();
            nodeResult.setErrorMessage(errorMessage);
            log.error("从历史还原推送异常: historyId={}", historyId, e);
        }

        // 保存新的推送历史
        MonitorSysGenConfigPushHistory newHistory = new MonitorSysGenConfigPushHistory();
        newHistory.setMonitorSysGenConfigId(history.getMonitorSysGenConfigId());
        newHistory.setMonitorSysGenConfigKey(history.getMonitorSysGenConfigKey());
        newHistory.setMonitorSysGenConfigValue(history.getMonitorSysGenConfigValue());
        newHistory.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
        newHistory.setMonitorSysGenServerName(server.getMonitorSysGenServerName());
        newHistory.setPushSuccess(pushResult ? 1 : 0);
        newHistory.setErrorMessage(errorMessage);
        newHistory.setPushTime(LocalDateTime.now());
        newHistory.setOperator("system");
        pushHistoryMapper.insert(newHistory);

        results.add(nodeResult);

        return ConfigManagementDTO.ConfigPushResult.builder()
                .total(1)
                .success(successCount)
                .failed(failedCount)
                .results(results)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigManagementDTO.ConfigPushResult batchRepushFromHistory(List<Long> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            throw new IllegalArgumentException("历史ID列表不能为空");
        }

        List<ConfigManagementDTO.NodePushResult> allResults = new ArrayList<>();
        int totalSuccess = 0;
        int totalFailed = 0;

        for (Long historyId : historyIds) {
            try {
                ConfigManagementDTO.ConfigPushResult result = repushFromHistory(historyId);
                totalSuccess += result.getSuccess();
                totalFailed += result.getFailed();
                allResults.addAll(result.getResults());
            } catch (Exception e) {
                totalFailed++;
                log.error("批量还原推送失败: historyId={}", historyId, e);
            }
        }

        return ConfigManagementDTO.ConfigPushResult.builder()
                .total(historyIds.size())
                .success(totalSuccess)
                .failed(totalFailed)
                .results(allResults)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePushHistory(Long historyId) {
        if (historyId == null) {
            throw new IllegalArgumentException("历史ID不能为空");
        }
        return pushHistoryMapper.deleteById(historyId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeletePushHistory(List<Long> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            throw new IllegalArgumentException("历史ID列表不能为空");
        }
        return pushHistoryMapper.deleteBatchIds(historyIds) > 0;
    }

    @Override
    public List<MonitorSysGenConfig> exportConfigs(List<Integer> configIds) {
        if (configIds == null || configIds.isEmpty()) {
            return Collections.emptyList();
        }
        return configMapper.selectBatchIds(configIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importConfigs(List<MonitorSysGenConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (MonitorSysGenConfig config : configs) {
            // 清除 ID，作为新配置导入
            config.setMonitorSysGenConfigId(null);
            
            // 检查配置键是否已存在
            if (!isConfigKeyAvailable(config.getMonitorSysGenConfigKey(), config.getMonitorSysGenConfigEnv(), null)) {
                log.warn("导入配置跳过，配置键已存在: key={}, env={}", 
                        config.getMonitorSysGenConfigKey(), config.getMonitorSysGenConfigEnv());
                continue;
            }

            // 设置默认状态
            if (config.getMonitorSysGenConfigStatus() == null) {
                config.setMonitorSysGenConfigStatus(1);
            }

            configMapper.insert(config);
            count++;
        }

        log.info("导入配置完成: total={}, imported={}", configs.size(), count);
        return count;
    }
}
