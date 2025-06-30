package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSettingBackup;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSettingHistory;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerSettingBackupMapper;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerSettingHistoryMapper;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerSettingMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器配置设置服务实现类
 *
 * @author CH
 * @since 2024/12/26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerSettingServiceImpl extends ServiceImpl<MonitorSysGenServerSettingMapper, MonitorSysGenServerSetting>
        implements MonitorSysGenServerSettingService {

    private final MonitorSysGenServerSettingHistoryMapper historyMapper;
    private final MonitorSysGenServerSettingBackupMapper backupMapper;

    @Override
    public IPage<MonitorSysGenServerSetting> pageFor(Page<MonitorSysGenServerSetting> page, MonitorSysGenServerSetting entity) {
        return this.page(page, Wrappers.<MonitorSysGenServerSetting>lambdaQuery()
                .eq(entity.getMonitorSysGenServerId() != null, 
                    MonitorSysGenServerSetting::getMonitorSysGenServerId, entity.getMonitorSysGenServerId())
                .eq(entity.getMonitorSysGenServerSettingStatus() != null, 
                    MonitorSysGenServerSetting::getMonitorSysGenServerSettingStatus, entity.getMonitorSysGenServerSettingStatus())
                .eq(entity.getMonitorSysGenServerSettingMonitorEnabled() != null, 
                    MonitorSysGenServerSetting::getMonitorSysGenServerSettingMonitorEnabled, entity.getMonitorSysGenServerSettingMonitorEnabled())
                .eq(entity.getMonitorSysGenServerSettingAlertEnabled() != null, 
                    MonitorSysGenServerSetting::getMonitorSysGenServerSettingAlertEnabled, entity.getMonitorSysGenServerSettingAlertEnabled())
                .eq(entity.getMonitorSysGenServerSettingReportEnabled() != null, 
                    MonitorSysGenServerSetting::getMonitorSysGenServerSettingReportEnabled, entity.getMonitorSysGenServerSettingReportEnabled())
                .eq(StringUtils.isNotBlank(entity.getMonitorSysGenServerSettingDataReportMethod()), 
                    MonitorSysGenServerSetting::getMonitorSysGenServerSettingDataReportMethod, entity.getMonitorSysGenServerSettingDataReportMethod())
                .orderByDesc(MonitorSysGenServerSetting::getUpdateTime));
    }

    @Override
    @Cacheable(value = "serverSetting", key = "#serverId", unless = "#result == null || !#result.isOk()")
    public ReturnResult<MonitorSysGenServerSetting> getByServerId(Integer serverId) {
        try {
            MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("根据服务器ID获取配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSetting>> getByServerIds(List<Integer> serverIds) {
        try {
            List<MonitorSysGenServerSetting> settings = baseMapper.selectByServerIds(serverIds);
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("批量获取服务器配置失败: serverIds={}", serverIds, e);
            return ReturnResult.error("批量获取配置失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "serverSetting", key = "#setting.monitorSysGenServerId")
    public ReturnResult<Boolean> saveOrUpdateSetting(MonitorSysGenServerSetting setting) {
        try {
            // 验证配置有效性
            ReturnResult<Boolean> validateResult = validateSetting(setting);
            if (!validateResult.isOk()) {
                return validateResult;
            }

            boolean result = this.saveOrUpdate(setting);
            return ReturnResult.of(result, result ? "保存成功" : "保存失败");
        } catch (Exception e) {
            log.error("保存服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchSaveOrUpdateSettings(List<MonitorSysGenServerSetting> settings) {
        try {
            // 验证所有配置的有效性
            for (MonitorSysGenServerSetting setting : settings) {
                ReturnResult<Boolean> validateResult = validateSetting(setting);
                if (!validateResult.isOk()) {
                    return ReturnResult.error("配置验证失败: " + validateResult.getMsg());
                }
            }

            int result = baseMapper.batchInsertOrUpdate(settings);
            return ReturnResult.of(result > 0, result > 0 ? "批量保存成功" : "批量保存失败");
        } catch (Exception e) {
            log.error("批量保存服务器配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("批量保存失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteByServerId(Integer serverId) {
        try {
            int result = baseMapper.deleteByServerId(serverId);
            return ReturnResult.of(result > 0, result > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            log.error("删除服务器配置失败: serverId={}", serverId, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> deleteById(Integer id) {
        try {
            boolean result = this.removeById(id);
            return ReturnResult.of(result, result ? "删除成功" : "删除失败");
        } catch (Exception e) {
            log.error("删除配置失败: id={}", id, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchDeleteByIds(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ReturnResult.error("ID列表不能为空");
            }
            boolean result = this.removeByIds(ids);
            return ReturnResult.of(result, result ? "批量删除成功" : "批量删除失败");
        } catch (Exception e) {
            log.error("批量删除配置失败: ids={}", ids, e);
            return ReturnResult.error("批量删除失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchUpdateMonitorEnabled(List<Integer> serverIds, Boolean enabled) {
        try {
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }

            int successCount = 0;
            for (Integer serverId : serverIds) {
                try {
                    ReturnResult<MonitorSysGenServerSetting> settingResult = getOrCreateSetting(serverId);
                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        setting.setMonitorSysGenServerSettingMonitorEnabled(enabled ? 1 : 0);
                        this.updateById(setting);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("更新服务器监控状态失败: serverId={}", serverId, e);
                }
            }

            boolean allSuccess = successCount == serverIds.size();
            String message = String.format("批量更新完成，成功: %d，失败: %d", successCount, serverIds.size() - successCount);
            return ReturnResult.of(allSuccess, message);

        } catch (Exception e) {
            log.error("批量更新监控启用状态失败: serverIds={}, enabled={}", serverIds, enabled, e);
            return ReturnResult.error("批量更新失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSetting>> getMonitorEnabledSettings() {
        try {
            List<MonitorSysGenServerSetting> settings = baseMapper.selectMonitorEnabledSettings();
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("获取启用监控的服务器配置失败", e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSetting>> getAlertEnabledSettings() {
        try {
            List<MonitorSysGenServerSetting> settings = baseMapper.selectAlertEnabledSettings();
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("获取启用告警的服务器配置失败", e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<MonitorSysGenServerSetting>> getReportEnabledSettings(String reportMethod) {
        try {
            List<MonitorSysGenServerSetting> settings = baseMapper.selectReportEnabledSettings(reportMethod);
            return ReturnResult.ok(settings);
        } catch (Exception e) {
            log.error("获取启用上报的服务器配置失败: reportMethod={}", reportMethod, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateStatusByServerId(Integer serverId, Integer status) {
        try {
            int result = baseMapper.updateStatusByServerId(serverId, status);
            return ReturnResult.of(result > 0, result > 0 ? "更新状态成功" : "更新状态失败");
        } catch (Exception e) {
            log.error("更新服务器配置状态失败: serverId={}, status={}", serverId, status, e);
            return ReturnResult.error("更新状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getSettingsStatistics() {
        try {
            List<Map<String, Object>> statistics = baseMapper.getSettingsStatistics();
            if (statistics.isEmpty()) {
                return ReturnResult.ok(Map.of());
            }
            return ReturnResult.ok(statistics.get(0));
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            return ReturnResult.error("获取统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> createDefaultSetting(Integer serverId) {
        try {
            MonitorSysGenServerSetting setting = new MonitorSysGenServerSetting();
            setting.setMonitorSysGenServerId(serverId);
            
            // 设置默认值
            setting.setMonitorSysGenServerSettingReportEnabled(1); // 默认启用上报
            setting.setMonitorSysGenServerSettingDataReportMethod("API"); // 默认API上报
            setting.setMonitorSysGenServerSettingMetricsRetentionDays(30); // 默认保留30天
            setting.setMonitorSysGenServerSettingMonitorInterval(60); // 默认60秒监控间隔
            setting.setMonitorSysGenServerSettingMonitorEnabled(1); // 默认启用监控
            
            // 设置默认告警阈值
            setting.setMonitorSysGenServerSettingCpuAlertThreshold(new BigDecimal("80.0")); // CPU 80%
            setting.setMonitorSysGenServerSettingMemoryAlertThreshold(new BigDecimal("85.0")); // 内存 85%
            setting.setMonitorSysGenServerSettingDiskAlertThreshold(new BigDecimal("90.0")); // 磁盘 90%
            setting.setMonitorSysGenServerSettingNetworkAlertThreshold(new BigDecimal("100.0")); // 网络 100Mbps
            setting.setMonitorSysGenServerSettingResponseTimeAlertThreshold(5000L); // 响应时间 5秒
            
            setting.setMonitorSysGenServerSettingAlertEnabled(1); // 默认启用告警
            setting.setMonitorSysGenServerSettingAlertNotificationMethod("EMAIL"); // 默认邮件通知
            setting.setMonitorSysGenServerSettingAlertSilenceDuration(30); // 默认30分钟静默
            setting.setMonitorSysGenServerSettingAutoRecoveryNotificationEnabled(1); // 默认启用恢复通知
            setting.setMonitorSysGenServerSettingDataCollectionFrequency(30); // 默认30秒收集频率
            setting.setMonitorSysGenServerSettingPerformanceSuggestionEnabled(1); // 默认启用性能建议
            setting.setMonitorSysGenServerSettingStatus(1); // 默认启用状态
            
            setting.setCreateTime(LocalDateTime.now());
            setting.setUpdateTime(LocalDateTime.now());
            
            return ReturnResult.ok(setting);
        } catch (Exception e) {
            log.error("创建默认配置失败: serverId={}", serverId, e);
            return ReturnResult.error("创建默认配置失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "serverSetting", key = "#serverId", unless = "#result == null || !#result.isOk()")
    public ReturnResult<MonitorSysGenServerSetting> getOrCreateSetting(Integer serverId) {
        try {
            // 先尝试获取现有配置
            MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
            if (setting != null) {
                return ReturnResult.ok(setting);
            }

            // 如果不存在，创建默认配置
            ReturnResult<MonitorSysGenServerSetting> defaultResult = createDefaultSetting(serverId);
            if (!defaultResult.isOk()) {
                return defaultResult;
            }

            MonitorSysGenServerSetting defaultSetting = defaultResult.getData();
            boolean saved = this.save(defaultSetting);
            if (saved) {
                return ReturnResult.ok(defaultSetting);
            } else {
                return ReturnResult.error("保存默认配置失败");
            }
        } catch (Exception e) {
            log.error("获取或创建服务器配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取或创建配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateSetting(MonitorSysGenServerSetting setting) {
        try {
            if (setting.getMonitorSysGenServerId() == null) {
                return ReturnResult.error("服务器ID不能为空");
            }
            
            // 验证告警阈值
            if (setting.getMonitorSysGenServerSettingCpuAlertThreshold() != null) {
                BigDecimal cpuThreshold = setting.getMonitorSysGenServerSettingCpuAlertThreshold();
                if (cpuThreshold.compareTo(BigDecimal.ZERO) <= 0 || cpuThreshold.compareTo(new BigDecimal("100")) > 0) {
                    return ReturnResult.error("CPU告警阈值必须在0-100之间");
                }
            }
            
            if (setting.getMonitorSysGenServerSettingMemoryAlertThreshold() != null) {
                BigDecimal memoryThreshold = setting.getMonitorSysGenServerSettingMemoryAlertThreshold();
                if (memoryThreshold.compareTo(BigDecimal.ZERO) <= 0 || memoryThreshold.compareTo(new BigDecimal("100")) > 0) {
                    return ReturnResult.error("内存告警阈值必须在0-100之间");
                }
            }
            
            if (setting.getMonitorSysGenServerSettingDiskAlertThreshold() != null) {
                BigDecimal diskThreshold = setting.getMonitorSysGenServerSettingDiskAlertThreshold();
                if (diskThreshold.compareTo(BigDecimal.ZERO) <= 0 || diskThreshold.compareTo(new BigDecimal("100")) > 0) {
                    return ReturnResult.error("磁盘告警阈值必须在0-100之间");
                }
            }
            
            // 验证监控间隔
            if (setting.getMonitorSysGenServerSettingMonitorInterval() != null) {
                Integer interval = setting.getMonitorSysGenServerSettingMonitorInterval();
                if (interval <= 0 || interval > 3600) {
                    return ReturnResult.error("监控间隔必须在1-3600秒之间");
                }
            }
            
            // 验证数据收集频率
            if (setting.getMonitorSysGenServerSettingDataCollectionFrequency() != null) {
                Integer frequency = setting.getMonitorSysGenServerSettingDataCollectionFrequency();
                if (frequency <= 0 || frequency > 3600) {
                    return ReturnResult.error("数据收集频率必须在1-3600秒之间");
                }
            }
            
            // 验证数据上报方式
            if (StringUtils.isNotBlank(setting.getMonitorSysGenServerSettingDataReportMethod())) {
                String method = setting.getMonitorSysGenServerSettingDataReportMethod();
                if (!List.of("NONE", "LOCAL", "API", "PROMETHEUS").contains(method)) {
                    return ReturnResult.error("无效的数据上报方式");
                }
            }

            // 验证代理配置（仅在启用代理时验证）
            if (setting.getMonitorSysGenServerSettingProxyEnabled() != null &&
                setting.getMonitorSysGenServerSettingProxyEnabled() == 1) {

                // 代理类型验证
                if (StringUtils.isBlank(setting.getMonitorSysGenServerSettingProxyType())) {
                    return ReturnResult.error("启用代理时必须选择代理类型");
                }

                String proxyType = setting.getMonitorSysGenServerSettingProxyType();
                if (!List.of("HTTP", "SOCKS4", "SOCKS5", "SSH_TUNNEL", "GUACAMOLE").contains(proxyType)) {
                    return ReturnResult.error("无效的代理类型");
                }

                // 代理地址验证
                if (StringUtils.isBlank(setting.getMonitorSysGenServerSettingProxyHost())) {
                    return ReturnResult.error("启用代理时必须输入代理服务器地址");
                }

                // 代理端口验证
                if (setting.getMonitorSysGenServerSettingProxyPort() == null) {
                    return ReturnResult.error("启用代理时必须输入代理端口");
                }

                Integer proxyPort = setting.getMonitorSysGenServerSettingProxyPort();
                if (proxyPort <= 0 || proxyPort > 65535) {
                    return ReturnResult.error("代理端口必须在1-65535之间");
                }
            }

            return ReturnResult.ok(true, "配置验证通过");
        } catch (Exception e) {
            log.error("验证配置失败", e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> copySettingToServers(Integer sourceServerId, List<Integer> targetServerIds) {
        try {
            if (sourceServerId == null) {
                return ReturnResult.error("源服务器ID不能为空");
            }
            if (targetServerIds == null || targetServerIds.isEmpty()) {
                return ReturnResult.error("目标服务器ID列表不能为空");
            }

            // 获取源配置
            MonitorSysGenServerSetting sourceSetting = baseMapper.selectByServerId(sourceServerId);
            if (sourceSetting == null) {
                return ReturnResult.error("源服务器配置不存在");
            }

            int successCount = 0;
            for (Integer targetServerId : targetServerIds) {
                if (targetServerId.equals(sourceServerId)) {
                    continue; // 跳过源服务器自身
                }

                try {
                    // 创建新配置
                    MonitorSysGenServerSetting newSetting = new MonitorSysGenServerSetting();
                    BeanUtils.copyProperties(sourceSetting, newSetting);
                    newSetting.setMonitorSysGenServerSettingId(null);
                    newSetting.setMonitorSysGenServerId(targetServerId);

                    // 删除现有配置
                    baseMapper.deleteByServerId(targetServerId);

                    // 保存新配置
                    this.save(newSetting);
                    successCount++;
                } catch (Exception e) {
                    log.warn("复制配置到服务器{}失败: {}", targetServerId, e.getMessage());
                }
            }

            log.info("配置复制完成: 源服务器={}, 成功复制到{}个目标服务器", sourceServerId, successCount);
            return ReturnResult.ok(true, String.format("成功复制到%d个服务器", successCount));
        } catch (Exception e) {
            log.error("复制配置失败", e);
            return ReturnResult.error("复制配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> getSettingTemplate(String templateType) {
        try {
            MonitorSysGenServerSetting template = new MonitorSysGenServerSetting();

            switch (templateType.toLowerCase()) {
                case "basic":
                    // 基础模板
                    template.setMonitorSysGenServerSettingMonitorEnabled(1);
                    template.setMonitorSysGenServerSettingDataReportMethod("API");
                    template.setMonitorSysGenServerSettingDataCollectionFrequency(60);
                    template.setMonitorSysGenServerSettingCpuAlertThreshold(new BigDecimal("80"));
                    template.setMonitorSysGenServerSettingMemoryAlertThreshold(new BigDecimal("85"));
                    template.setMonitorSysGenServerSettingDiskAlertThreshold(new BigDecimal("90"));
                    template.setMonitorSysGenServerSettingAlertEnabled(1);
                    template.setMonitorSysGenServerSettingAlertNotificationMethod("EMAIL");
                    break;

                case "advanced":
                    // 高级模板
                    template = MonitorSysGenServerSetting.getDefaultSetting(null);
                    template.setMonitorSysGenServerSettingProcessMonitorEnabled(1);
                    template.setMonitorSysGenServerSettingServiceMonitorEnabled(1);
                    template.setMonitorSysGenServerSettingPortMonitorEnabled(1);
                    template.setMonitorSysGenServerSettingLogMonitorEnabled(1);
                    break;

                case "prometheus":
                    // Prometheus模板
                    template.setMonitorSysGenServerSettingMonitorEnabled(1);
                    template.setMonitorSysGenServerSettingDataReportMethod("PROMETHEUS");
                    template.setMonitorSysGenServerSettingPrometheusHost("localhost");
                    template.setMonitorSysGenServerSettingPrometheusPort(9090);
                    template.setMonitorSysGenServerSettingAlertEnabled(0);
                    break;

                default:
                    return ReturnResult.error("不支持的模板类型: " + templateType);
            }

            return ReturnResult.ok(template);
        } catch (Exception e) {
            log.error("获取配置模板失败", e);
            return ReturnResult.error("获取配置模板失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> exportSettings(List<Integer> serverIds, String format) {
        try {
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }

            List<MonitorSysGenServerSetting> settings = new ArrayList<>();
            for (Integer serverId : serverIds) {
                MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
                if (setting != null) {
                    settings.add(setting);
                }
            }

            if (settings.isEmpty()) {
                return ReturnResult.error("没有找到任何配置数据");
            }

            String exportData;
            switch (format.toLowerCase()) {
                case "json":
                    ObjectMapper jsonMapper = new ObjectMapper();
                    exportData = jsonMapper.writeValueAsString(settings);
                    break;
                case "yaml":
                    YAMLMapper yamlMapper = new YAMLMapper();
                    exportData = yamlMapper.writeValueAsString(settings);
                    break;
                default:
                    return ReturnResult.error("不支持的导出格式: " + format);
            }

            log.info("导出配置成功: 服务器数量={}, 格式={}", settings.size(), format);
            return ReturnResult.ok(exportData);
        } catch (Exception e) {
            log.error("导出配置失败", e);
            return ReturnResult.error("导出配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> importSettings(String configData, String format, boolean overwrite) {
        try {
            if (StringUtils.isBlank(configData)) {
                return ReturnResult.error("配置数据不能为空");
            }

            List<MonitorSysGenServerSetting> settings;
            switch (format.toLowerCase()) {
                case "json":
                    ObjectMapper jsonMapper = new ObjectMapper();
                    settings = jsonMapper.readValue(configData,
                            jsonMapper.getTypeFactory().constructCollectionType(List.class, MonitorSysGenServerSetting.class));
                    break;
                case "yaml":
                    YAMLMapper yamlMapper = new YAMLMapper();
                    settings = yamlMapper.readValue(configData,
                            yamlMapper.getTypeFactory().constructCollectionType(List.class, MonitorSysGenServerSetting.class));
                    break;
                default:
                    return ReturnResult.error("不支持的导入格式: " + format);
            }

            if (settings == null || settings.isEmpty()) {
                return ReturnResult.error("配置数据为空");
            }

            int successCount = 0;
            for (MonitorSysGenServerSetting setting : settings) {
                try {
                    // 验证配置
                    ReturnResult<Boolean> validateResult = validateSetting(setting);
                    if (!validateResult.isOk()) {
                        log.warn("配置验证失败，跳过: {}", validateResult.getMsg());
                        continue;
                    }

                    // 检查是否已存在
                    MonitorSysGenServerSetting existing = baseMapper.selectByServerId(setting.getMonitorSysGenServerId());
                    if (existing != null && !overwrite) {
                        log.warn("配置已存在且不允许覆盖，跳过服务器: {}", setting.getMonitorSysGenServerId());
                        continue;
                    }

                    // 保存或更新
                    setting.setMonitorSysGenServerSettingId(existing != null ? existing.getMonitorSysGenServerSettingId() : null);
                    this.saveOrUpdate(setting);
                    successCount++;
                } catch (Exception e) {
                    log.warn("导入配置失败，服务器ID: {}, 错误: {}", setting.getMonitorSysGenServerId(), e.getMessage());
                }
            }

            log.info("导入配置完成: 总数={}, 成功={}", settings.size(), successCount);
            return ReturnResult.ok(true, String.format("成功导入%d个配置", successCount));
        } catch (Exception e) {
            log.error("导入配置失败", e);
            return ReturnResult.error("导入配置失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "serverSetting", key = "#serverId")
    public ReturnResult<Boolean> resetToDefault(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 删除现有配置
            baseMapper.deleteByServerId(serverId);

            // 创建默认配置
            ReturnResult<MonitorSysGenServerSetting> defaultResult = createDefaultSetting(serverId);
            if (!defaultResult.isOk()) {
                return ReturnResult.error(defaultResult.getMsg());
            }

            MonitorSysGenServerSetting defaultSetting = defaultResult.getData();
            boolean saved = this.save(defaultSetting);

            if (saved) {
                log.info("重置服务器配置为默认值成功: serverId={}", serverId);
                return ReturnResult.ok(true, "重置为默认配置成功");
            } else {
                return ReturnResult.error("保存默认配置失败");
            }
        } catch (Exception e) {
            log.error("重置配置失败", e);
            return ReturnResult.error("重置配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getSettingHistory(Integer serverId, Integer limit) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 查询历史记录
            List<MonitorSysGenServerSettingHistory> historyList = historyMapper.selectByServerId(serverId, limit);

            List<Map<String, Object>> result = new ArrayList<>();
            for (MonitorSysGenServerSettingHistory history : historyList) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", history.getMonitorSysGenServerSettingHistoryId());
                record.put("serverId", history.getMonitorSysGenServerId());
                record.put("settingId", history.getMonitorSysGenServerSettingId());
                record.put("changeType", history.getChangeType());
                record.put("changeDescription", history.getChangeDescription());
                record.put("changeUser", history.getChangeUser());
                record.put("changeTime", history.getChangeTime());
                record.put("changedFields", history.getChangedFields());
                record.put("oldValues", history.getOldValues());
                record.put("newValues", history.getNewValues());
                result.add(record);
            }

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取配置历史失败", e);
            return ReturnResult.error("获取配置历史失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testAlertConfiguration(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
            if (setting == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            if (!setting.isAlertEnabled()) {
                return ReturnResult.error("告警功能未启用");
            }

            String notificationMethod = setting.getMonitorSysGenServerSettingAlertNotificationMethod();
            String notificationAddress = setting.getMonitorSysGenServerSettingAlertNotificationAddress();

            if (StringUtils.isBlank(notificationMethod)) {
                return ReturnResult.error("告警方式未配置");
            }

            if (StringUtils.isBlank(notificationAddress)) {
                return ReturnResult.error("告警地址未配置");
            }

            // 这里应该实际发送测试告警
            // 由于没有具体的告警服务实现，这里只做基本验证
            log.info("测试告警配置: 服务器={}, 方式={}, 地址={}", serverId, notificationMethod, notificationAddress);

            return ReturnResult.ok(true, "告警配置测试通过");
        } catch (Exception e) {
            log.error("测试告警配置失败", e);
            return ReturnResult.error("测试告警配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testReportConfiguration(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
            if (setting == null) {
                return ReturnResult.error("服务器配置不存在");
            }

            if (!setting.isMonitorEnabled()) {
                return ReturnResult.error("监控功能未启用");
            }

            String reportMethod = setting.getMonitorSysGenServerSettingDataReportMethod();
            if (StringUtils.isBlank(reportMethod)) {
                return ReturnResult.error("数据上报方式未配置");
            }

            // 验证不同上报方式的配置
            switch (reportMethod) {
                case "PROMETHEUS":
                    String prometheusHost = setting.getMonitorSysGenServerSettingPrometheusHost();
                    Integer prometheusPort = setting.getMonitorSysGenServerSettingPrometheusPort();
                    if (StringUtils.isBlank(prometheusHost) || prometheusPort == null) {
                        return ReturnResult.error("Prometheus配置不完整");
                    }
                    break;
                case "API":
                    // API上报方式的验证逻辑
                    break;
                case "LOCAL":
                    // 本地上报方式的验证逻辑
                    break;
                default:
                    return ReturnResult.error("不支持的上报方式: " + reportMethod);
            }

            log.info("测试上报配置: 服务器={}, 方式={}", serverId, reportMethod);
            return ReturnResult.ok(true, "上报配置测试通过");
        } catch (Exception e) {
            log.error("测试上报配置失败", e);
            return ReturnResult.error("测试上报配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> getRecommendedSetting(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 创建推荐配置
            MonitorSysGenServerSetting recommended = MonitorSysGenServerSetting.getDefaultSetting(serverId);

            // 根据服务器类型调整推荐配置
            // 这里可以根据服务器的实际情况（CPU核数、内存大小等）来调整阈值
            // 由于没有服务器详细信息，使用默认推荐值

            recommended.setMonitorSysGenServerSettingCpuAlertThreshold(new BigDecimal("75"));
            recommended.setMonitorSysGenServerSettingMemoryAlertThreshold(new BigDecimal("80"));
            recommended.setMonitorSysGenServerSettingDiskAlertThreshold(new BigDecimal("85"));

            // 推荐启用基本监控
            recommended.setMonitorSysGenServerSettingMonitorEnabled(1);
            recommended.setMonitorSysGenServerSettingAlertEnabled(1);
            recommended.setMonitorSysGenServerSettingDataReportMethod("API");
            recommended.setMonitorSysGenServerSettingDataCollectionFrequency(60);

            return ReturnResult.ok(recommended);
        } catch (Exception e) {
            log.error("获取推荐配置失败", e);
            return ReturnResult.error("获取推荐配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> applyTemplateToServers(String templateType, List<Integer> serverIds) {
        try {
            if (StringUtils.isBlank(templateType)) {
                return ReturnResult.error("模板类型不能为空");
            }
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }

            // 获取模板配置
            ReturnResult<MonitorSysGenServerSetting> templateResult = getSettingTemplate(templateType);
            if (!templateResult.isOk()) {
                return ReturnResult.error("获取模板失败: " + templateResult.getMsg());
            }

            MonitorSysGenServerSetting template = templateResult.getData();
            int successCount = 0;

            for (Integer serverId : serverIds) {
                try {
                    // 创建新配置
                    MonitorSysGenServerSetting newSetting = new MonitorSysGenServerSetting();
                    BeanUtils.copyProperties(template, newSetting);
                    newSetting.setMonitorSysGenServerSettingId(null);
                    newSetting.setMonitorSysGenServerId(serverId);

                    // 删除现有配置
                    baseMapper.deleteByServerId(serverId);

                    // 保存新配置
                    this.save(newSetting);
                    successCount++;
                } catch (Exception e) {
                    log.warn("应用模板到服务器{}失败: {}", serverId, e.getMessage());
                }
            }

            log.info("模板应用完成: 模板类型={}, 成功应用到{}个服务器", templateType, successCount);
            return ReturnResult.ok(true, String.format("成功应用模板到%d个服务器", successCount));
        } catch (Exception e) {
            log.error("应用模板失败", e);
            return ReturnResult.error("应用模板失败: " + e.getMessage());
        }
    }

    /**
     * 记录配置变更历史
     */
    private void recordSettingHistory(Integer serverId, Long settingId,
                                      MonitorSysGenServerSettingHistory.ChangeType changeType,
                                      String description, String user) {
        try {
            MonitorSysGenServerSettingHistory history = MonitorSysGenServerSettingHistory.createHistory(
                    serverId, settingId, changeType, description, user);

            // 如果是更新操作，记录配置快照
            if (changeType == MonitorSysGenServerSettingHistory.ChangeType.UPDATE) {
                MonitorSysGenServerSetting currentSetting = baseMapper.selectByServerId(serverId);
                if (currentSetting != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    history.setSettingSnapshot(mapper.writeValueAsString(currentSetting));
                }
            }

            historyMapper.insert(history);
        } catch (Exception e) {
            log.warn("记录配置历史失败: {}", e.getMessage());
        }
    }

    /**
     * 创建配置备份
     */
    public ReturnResult<Long> createBackup(String backupName, String description,
                                           List<Integer> serverIds, String backupType, String user) {
        try {
            if (StringUtils.isBlank(backupName)) {
                return ReturnResult.error("备份名称不能为空");
            }

            // 检查备份名称是否已存在
            MonitorSysGenServerSettingBackup existing = backupMapper.selectByBackupName(backupName);
            if (existing != null) {
                return ReturnResult.error("备份名称已存在");
            }

            // 获取要备份的配置
            List<MonitorSysGenServerSetting> settings = new ArrayList<>();
            if (serverIds == null || serverIds.isEmpty()) {
                // 备份所有配置
                settings = this.list();
            } else {
                // 备份指定服务器的配置
                for (Integer serverId : serverIds) {
                    MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
                    if (setting != null) {
                        settings.add(setting);
                    }
                }
            }

            // 创建备份记录
            MonitorSysGenServerSettingBackup backup = new MonitorSysGenServerSettingBackup();
            backup.setBackupName(backupName);
            backup.setBackupDescription(description);
            backup.setBackupType(backupType);
            backup.setBackupScope(serverIds == null || serverIds.isEmpty() ? "ALL" : "SELECTED");
            backup.setBackupFormat("JSON");
            backup.setServerCount(settings.size());
            backup.setCreateUser(user);

            // 序列化配置数据
            ObjectMapper mapper = new ObjectMapper();
            String backupData = mapper.writeValueAsString(settings);
            backup.setBackupData(backupData);
            backup.setBackupSize((long) backupData.length());

            if (serverIds != null && !serverIds.isEmpty()) {
                backup.setServerIds(mapper.writeValueAsString(serverIds));
            }

            backup.setBackupStatus("SUCCESS");
            backupMapper.insert(backup);

            log.info("创建配置备份成功: 备份名称={}, 服务器数量={}", backupName, settings.size());
            return ReturnResult.ok(backup.getMonitorSysGenServerSettingBackupId(), "备份创建成功");
        } catch (Exception e) {
            log.error("创建配置备份失败", e);
            return ReturnResult.error("创建配置备份失败: " + e.getMessage());
        }
    }

    /**
     * 从备份恢复配置
     */
    public ReturnResult<Boolean> restoreFromBackup(Long backupId, boolean overwrite, String user) {
        try {
            if (backupId == null) {
                return ReturnResult.error("备份ID不能为空");
            }

            // 获取备份记录
            MonitorSysGenServerSettingBackup backup = backupMapper.selectById(backupId);
            if (backup == null) {
                return ReturnResult.error("备份记录不存在");
            }

            // 解析备份数据
            ObjectMapper mapper = new ObjectMapper();
            List<MonitorSysGenServerSetting> settings = mapper.readValue(backup.getBackupData(),
                    mapper.getTypeFactory().constructCollectionType(List.class, MonitorSysGenServerSetting.class));

            int successCount = 0;
            for (MonitorSysGenServerSetting setting : settings) {
                try {
                    Integer serverId = setting.getMonitorSysGenServerId();

                    // 检查是否已存在
                    MonitorSysGenServerSetting existing = baseMapper.selectByServerId(serverId);
                    if (existing != null && !overwrite) {
                        log.warn("配置已存在且不允许覆盖，跳过服务器: {}", serverId);
                        continue;
                    }

                    // 恢复配置
                    setting.setMonitorSysGenServerSettingId(existing != null ? existing.getMonitorSysGenServerSettingId() : null);
                    this.saveOrUpdate(setting);

                    // 记录历史
                    recordSettingHistory(serverId, setting.getMonitorSysGenServerSettingId().longValue(),
                            MonitorSysGenServerSettingHistory.ChangeType.UPDATE,
                            "从备份恢复: " + backup.getBackupName(), user);

                    successCount++;
                } catch (Exception e) {
                    log.warn("恢复配置失败，服务器ID: {}, 错误: {}", setting.getMonitorSysGenServerId(), e.getMessage());
                }
            }

            log.info("从备份恢复配置完成: 备份名称={}, 成功恢复{}个配置", backup.getBackupName(), successCount);
            return ReturnResult.ok(true, String.format("成功恢复%d个配置", successCount));
        } catch (Exception e) {
            log.error("从备份恢复配置失败", e);
            return ReturnResult.error("从备份恢复配置失败: " + e.getMessage());
        }
    }
}
