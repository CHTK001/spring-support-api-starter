package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogConfig;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerLogConfigMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLogConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器日志配置管理服务实现类
 *
 * @author CH
 * @since 2024/12/20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerLogConfigServiceImpl extends ServiceImpl<MonitorSysGenServerLogConfigMapper, MonitorSysGenServerLogConfig> 
        implements MonitorSysGenServerLogConfigService {

    private final MonitorSysGenServerLogConfigMapper configMapper;

    @Override
    public ReturnResult<MonitorSysGenServerLogConfig> getConfigByServerId(Integer serverId) {
        try {
            MonitorSysGenServerLogConfig config = configMapper.selectByServerId(serverId);
            if (config == null) {
                // 如果没有配置，创建默认配置
                return createDefaultConfig(serverId);
            }
            return ReturnResult.success(config);
        } catch (Exception e) {
            log.error("根据服务器ID获取日志配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取日志配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerLogConfig> saveOrUpdateConfig(MonitorSysGenServerLogConfig config) {
        try {
            if (config == null) {
                return ReturnResult.illegal("配置不能为空");
            }

            // 设置默认值
            config.setDefaultConfig();
            
            // 验证配置
            ReturnResult<Map<String, Object>> validationResult = validateConfig(config);
            if (validationResult != null && validationResult.getData() != null) {
                Map<String, Object> validation = validationResult.getData();
                if (!(Boolean) validation.get("valid")) {
                    return ReturnResult.illegal("配置验证失败: " + validation.get("message"));
                }
            }

            LocalDateTime now = LocalDateTime.now();
            if (config.getMonitorSysGenServerLogConfigId() == null) {
                // 新增
                config.setMonitorSysGenServerLogCreateTime(now);
                config.setMonitorSysGenServerLogUpdateTime(now);
            } else {
                // 更新
                config.setMonitorSysGenServerLogUpdateTime(now);
            }

            boolean result = saveOrUpdate(config);
            if (result) {
                return ReturnResult.success(config);
            } else {
                return ReturnResult.error("保存配置失败");
            }
        } catch (Exception e) {
            log.error("保存或更新日志配置失败", e);
            return ReturnResult.error("保存配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getEnabledConfigs() {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectEnabledConfigs();
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("获取启用的日志配置失败", e);
            return ReturnResult.error("获取启用配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getRealTimeEnabledConfigs() {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectRealTimeEnabledConfigs();
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("获取启用实时日志的配置失败", e);
            return ReturnResult.error("获取实时日志配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByRetentionDays(Integer retentionDays) {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectByRetentionDays(retentionDays);
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("根据保留天数获取配置失败: retentionDays={}", retentionDays, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsForCleanup() {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectConfigsForCleanup();
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("获取需要清理的配置失败", e);
            return ReturnResult.error("获取清理配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateConfigStatus(Integer serverId, Integer status) {
        try {
            int updated = configMapper.updateConfigStatus(serverId, status);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("更新配置状态失败: serverId={}, status={}", serverId, status, e);
            return ReturnResult.error("更新配置状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateLogEnabled(Integer serverId, Integer enabled) {
        try {
            int updated = configMapper.updateLogEnabled(serverId, enabled);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("更新日志启用状态失败: serverId={}, enabled={}", serverId, enabled, e);
            return ReturnResult.error("更新日志启用状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> updateRealTimeEnabled(Integer serverId, Integer enabled) {
        try {
            int updated = configMapper.updateRealTimeEnabled(serverId, enabled);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("更新实时日志启用状态失败: serverId={}, enabled={}", serverId, enabled, e);
            return ReturnResult.error("更新实时日志启用状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> batchUpdateConfigStatus(List<Integer> serverIds, Integer status) {
        try {
            int updated = configMapper.batchUpdateConfigStatus(serverIds, status);
            return ReturnResult.success(updated > 0);
        } catch (Exception e) {
            log.error("批量更新配置状态失败: serverIds={}, status={}", serverIds, status, e);
            return ReturnResult.error("批量更新配置状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getConfigStatistics() {
        try {
            Map<String, Object> statistics = configMapper.getConfigStatistics();
            return ReturnResult.success(statistics);
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            return ReturnResult.error("获取配置统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByCollectionInterval(Integer interval) {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectByCollectionInterval(interval);
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("根据收集间隔获取配置失败: interval={}", interval, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getExpiredConfigs(Integer days) {
        try {
            List<MonitorSysGenServerLogConfig> configs = configMapper.selectExpiredConfigs(days);
            return ReturnResult.success(configs);
        } catch (Exception e) {
            log.error("获取过期配置失败: days={}", days, e);
            return ReturnResult.error("获取过期配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> hasConfigByServerId(Integer serverId) {
        try {
            boolean exists = configMapper.existsByServerId(serverId);
            return ReturnResult.success(exists);
        } catch (Exception e) {
            log.error("检查服务器配置是否存在失败: serverId={}", serverId, e);
            return ReturnResult.error("检查配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerLogConfig> getDefaultTemplate() {
        try {
            MonitorSysGenServerLogConfig template = configMapper.selectDefaultTemplate();
            if (template == null) {
                // 创建默认模板
                template = new MonitorSysGenServerLogConfig();
                template.setDefaultConfig();
            }
            return ReturnResult.success(template);
        } catch (Exception e) {
            log.error("获取默认配置模板失败", e);
            return ReturnResult.error("获取默认模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerLogConfig> createDefaultConfig(Integer serverId) {
        try {
            MonitorSysGenServerLogConfig config = new MonitorSysGenServerLogConfig();
            config.setMonitorSysGenServerId(serverId);
            config.setDefaultConfig();
            
            LocalDateTime now = LocalDateTime.now();
            config.setMonitorSysGenServerLogCreateTime(now);
            config.setMonitorSysGenServerLogUpdateTime(now);

            boolean result = save(config);
            if (result) {
                return ReturnResult.success(config);
            } else {
                return ReturnResult.error("创建默认配置失败");
            }
        } catch (Exception e) {
            log.error("为服务器创建默认配置失败: serverId={}", serverId, e);
            return ReturnResult.error("创建默认配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> copyConfigToServers(Integer sourceServerId, List<Integer> targetServerIds) {
        try {
            MonitorSysGenServerLogConfig sourceConfig = configMapper.selectByServerId(sourceServerId);
            if (sourceConfig == null) {
                return ReturnResult.illegal("源服务器配置不存在");
            }

            LocalDateTime now = LocalDateTime.now();
            for (Integer targetServerId : targetServerIds) {
                MonitorSysGenServerLogConfig targetConfig = new MonitorSysGenServerLogConfig();
                // 复制配置属性
                targetConfig.setMonitorSysGenServerId(targetServerId);
                targetConfig.setMonitorSysGenServerLogRetentionDays(sourceConfig.getMonitorSysGenServerLogRetentionDays());
                targetConfig.setMonitorSysGenServerLogEnabled(sourceConfig.getMonitorSysGenServerLogEnabled());
                targetConfig.setMonitorSysGenServerLogLevelFilter(sourceConfig.getMonitorSysGenServerLogLevelFilter());
                targetConfig.setMonitorSysGenServerLogSourceFilter(sourceConfig.getMonitorSysGenServerLogSourceFilter());
                targetConfig.setMonitorSysGenServerLogMaxSizeMb(sourceConfig.getMonitorSysGenServerLogMaxSizeMb());
                targetConfig.setMonitorSysGenServerLogMaxFiles(sourceConfig.getMonitorSysGenServerLogMaxFiles());
                targetConfig.setMonitorSysGenServerLogCollectionInterval(sourceConfig.getMonitorSysGenServerLogCollectionInterval());
                targetConfig.setMonitorSysGenServerLogBatchSize(sourceConfig.getMonitorSysGenServerLogBatchSize());
                targetConfig.setMonitorSysGenServerLogCompressionEnabled(sourceConfig.getMonitorSysGenServerLogCompressionEnabled());
                targetConfig.setMonitorSysGenServerLogRealTimeEnabled(sourceConfig.getMonitorSysGenServerLogRealTimeEnabled());
                targetConfig.setMonitorSysGenServerLogFilePatterns(sourceConfig.getMonitorSysGenServerLogFilePatterns());
                targetConfig.setMonitorSysGenServerLogExcludePatterns(sourceConfig.getMonitorSysGenServerLogExcludePatterns());
                targetConfig.setMonitorSysGenServerLogCustomParsers(sourceConfig.getMonitorSysGenServerLogCustomParsers());
                targetConfig.setMonitorSysGenServerLogAlertRules(sourceConfig.getMonitorSysGenServerLogAlertRules());
                targetConfig.setMonitorSysGenServerLogConfigStatus(sourceConfig.getMonitorSysGenServerLogConfigStatus());
                targetConfig.setMonitorSysGenServerLogCreateTime(now);
                targetConfig.setMonitorSysGenServerLogUpdateTime(now);

                saveOrUpdate(targetConfig);
            }

            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("复制配置到其他服务器失败: sourceServerId={}, targetServerIds={}", sourceServerId, targetServerIds, e);
            return ReturnResult.error("复制配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> validateConfig(MonitorSysGenServerLogConfig config) {
        try {
            Map<String, Object> result = new HashMap<>();
            StringBuilder message = new StringBuilder();
            boolean valid = true;

            if (config.getMonitorSysGenServerId() == null) {
                valid = false;
                message.append("服务器ID不能为空; ");
            }

            if (config.getMonitorSysGenServerLogRetentionDays() == null || config.getMonitorSysGenServerLogRetentionDays() <= 0) {
                valid = false;
                message.append("日志保留天数必须大于0; ");
            }

            if (config.getMonitorSysGenServerLogCollectionInterval() == null || config.getMonitorSysGenServerLogCollectionInterval() <= 0) {
                valid = false;
                message.append("日志收集间隔必须大于0; ");
            }

            if (config.getMonitorSysGenServerLogBatchSize() == null || config.getMonitorSysGenServerLogBatchSize() <= 0) {
                valid = false;
                message.append("批量处理大小必须大于0; ");
            }

            result.put("valid", valid);
            result.put("message", message.toString());
            return ReturnResult.success(result);
        } catch (Exception e) {
            log.error("验证配置失败", e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<MonitorSysGenServerLogConfig> resetConfigToDefault(Integer serverId) {
        try {
            MonitorSysGenServerLogConfig config = configMapper.selectByServerId(serverId);
            if (config == null) {
                return createDefaultConfig(serverId);
            }

            config.setDefaultConfig();
            config.setMonitorSysGenServerLogUpdateTime(LocalDateTime.now());

            boolean result = updateById(config);
            if (result) {
                return ReturnResult.success(config);
            } else {
                return ReturnResult.error("重置配置失败");
            }
        } catch (Exception e) {
            log.error("重置配置为默认值失败: serverId={}", serverId, e);
            return ReturnResult.error("重置配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportConfigs(List<Integer> serverIds, String format) {
        try {
            // TODO: 实现配置导出功能
            log.info("导出配置: serverIds={}, format={}", serverIds, format);
            return ReturnResult.success("配置导出功能正在开发中");
        } catch (Exception e) {
            log.error("导出配置失败", e);
            return ReturnResult.error("导出配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<List<MonitorSysGenServerLogConfig>> importConfigs(String configData, String format) {
        try {
            // TODO: 实现配置导入功能
            log.info("导入配置: format={}", format);
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("导入配置失败", e);
            return ReturnResult.error("导入配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getConfigChangeHistory(Integer serverId) {
        try {
            // TODO: 实现配置变更历史功能
            log.info("获取配置变更历史: serverId={}", serverId);
            return ReturnResult.success(List.of());
        } catch (Exception e) {
            log.error("获取配置变更历史失败: serverId={}", serverId, e);
            return ReturnResult.error("获取配置变更历史失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ReturnResult<Boolean> applyConfigTemplate(Long templateId, List<Integer> serverIds) {
        try {
            // TODO: 实现应用配置模板功能
            log.info("应用配置模板: templateId={}, serverIds={}", templateId, serverIds);
            return ReturnResult.success(true);
        } catch (Exception e) {
            log.error("应用配置模板失败: templateId={}", templateId, e);
            return ReturnResult.error("应用配置模板失败: " + e.getMessage());
        }
    }
}
