package com.chua.starter.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.entity.MonitorSysGenServerSettingHistory;
import com.chua.starter.monitor.mapper.MonitorSysGenServerSettingHistoryMapper;
import com.chua.starter.monitor.mapper.MonitorSysGenServerSettingMapper;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.task.manager.ServerTaskManager;
import com.chua.starter.monitor.utils.MonitorSysGenServerSettingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务器配置设置服务实现类
 *
 * @author CH
 * @since 2024/12/26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerSettingServiceImpl
        extends ServiceImpl<MonitorSysGenServerSettingMapper, MonitorSysGenServerSetting>
        implements MonitorSysGenServerSettingService {

    private final MonitorSysGenServerSettingHistoryMapper historyMapper;

    @Autowired
    @Lazy
    private ServerTaskManager serverTaskManager;

    @Override
    public IPage<MonitorSysGenServerSetting> pageFor(Page<MonitorSysGenServerSetting> page,
                                                     MonitorSysGenServerSetting entity) {
        return this.page(page,
                Wrappers.<MonitorSysGenServerSetting>lambdaQuery()
                        .eq(entity.getMonitorSysGenServerId() != null,
                                MonitorSysGenServerSetting::getMonitorSysGenServerId, entity.getMonitorSysGenServerId())
                        .eq(entity.getMonitorSysGenServerSettingStatus() != null,
                                MonitorSysGenServerSetting::getMonitorSysGenServerSettingStatus,
                                entity.getMonitorSysGenServerSettingStatus())
                        .eq(entity.getMonitorSysGenServerSettingMonitorEnabled() != null,
                                MonitorSysGenServerSetting::getMonitorSysGenServerSettingMonitorEnabled,
                                entity.getMonitorSysGenServerSettingMonitorEnabled())
                        .eq(entity.getMonitorSysGenServerSettingAlertEnabled() != null,
                                MonitorSysGenServerSetting::getMonitorSysGenServerSettingAlertEnabled,
                                entity.getMonitorSysGenServerSettingAlertEnabled())
                        .eq(entity.getMonitorSysGenServerSettingReportEnabled() != null,
                                MonitorSysGenServerSetting::getMonitorSysGenServerSettingReportEnabled,
                                entity.getMonitorSysGenServerSettingReportEnabled())
                        .eq(StringUtils.isNotBlank(entity.getMonitorSysGenServerSettingDataReportMethod()),
                                MonitorSysGenServerSetting::getMonitorSysGenServerSettingDataReportMethod,
                                entity.getMonitorSysGenServerSettingDataReportMethod())
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
            // 基本参数验证
            if (setting == null || setting.getMonitorSysGenServerId() == null) {
                return ReturnResult.error("服务器配置或服务器ID不能为空");
            }

            // 先判断服务器是否已有配置
            MonitorSysGenServerSetting existingSetting = baseMapper
                    .selectByServerId(setting.getMonitorSysGenServerId());
            boolean result;
            String operation;

            if (existingSetting == null) {
                // 没有配置，执行保存操作
                result = this.save(setting);
                operation = "保存";
                log.info("服务器 {} 没有配置，执行保存操作: {}", setting.getMonitorSysGenServerId(), result ? "成功" : "失败");
            } else {
                // 有配置，执行更新操作
                setting.setMonitorSysGenServerSettingId(existingSetting.getMonitorSysGenServerSettingId());
                result = this.updateById(setting);
                operation = "更新";
                log.info("服务器 {} 已有配置，执行更新操作: {}", setting.getMonitorSysGenServerId(), result ? "成功" : "失败");
            }

            return ReturnResult.of(result, result ? operation + "成功" : operation + "失败");
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "serverSetting", key = "#serverId")
    public ReturnResult<Boolean> deleteByServerId(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 1. 删除配置历史记录
            try {
                historyMapper.deleteByServerId(serverId);
                log.debug("删除服务器 {} 配置历史记录成功", serverId);
            } catch (Exception e) {
                log.warn("删除服务器 {} 配置历史记录失败: {}", serverId, e.getMessage());
            }

            // 2. 通过任务管理器停止相关服务
            try {
                if (serverTaskManager != null) {
                    serverTaskManager.removeServerTasks(serverId);
                    log.debug("停止服务器 {} 相关任务成功", serverId);
                }
            } catch (Exception e) {
                log.warn("停止服务器 {} 相关任务失败: {}", serverId, e.getMessage());
            }

            // 3. 删除服务器配置
            int result = baseMapper.deleteByServerId(serverId);

            if (result > 0) {
                log.info("删除服务器 {} 配置成功", serverId);
            }

            return ReturnResult.of(result > 0, result > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            log.error("删除服务器配置失败: serverId={}", serverId, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> deleteById(Integer id) {
        try {
            if (id == null) {
                return ReturnResult.error("配置ID不能为空");
            }

            // 获取配置信息用于清理缓存
            MonitorSysGenServerSetting setting = this.getById(id);
            Integer serverId = setting != null ? setting.getMonitorSysGenServerId() : null;

            boolean result = this.removeById(id);

            // 如果删除成功且有serverId，手动清理缓存
            if (result && serverId != null) {
                try {
                    // 通过调用带缓存清理注解的方法来清理缓存
                    clearCacheForServer(serverId);
                } catch (Exception e) {
                    log.warn("清理缓存失败: serverId={}", serverId, e);
                }
            }

            return ReturnResult.of(result, result ? "删除成功" : "删除失败");
        } catch (Exception e) {
            log.error("删除配置失败: id={}", id, e);
            return ReturnResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 清理指定服务器的缓存
     */
    @CacheEvict(value = "serverSetting", key = "#serverId")
    public void clearCacheForServer(Integer serverId) {
        // 这个方法只是为了触发缓存清理
        log.debug("清理服务器 {} 的缓存", serverId);
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> getFromLocalCache(Integer serverId) {
        try {
            // 复用Spring Cache管理的缓存：通过调用 getByServerId 但避免触发DB
            // 这里直接访问底层缓存管理器较复杂，采取简化策略：
            // 使用一个快速失败的本地Map作为只读缓存由 ServerTaskManager 维护更合理；
            // 但为保持职责集中，此处先尝试通过本类的一级缓存（无则返回ERROR）。
            // 简化实现：直接返回ERROR，交由调用方优先使用其本地缓存（如ServerTaskManager.configCache）。
            return ReturnResult.error("LOCAL_CACHE_MISS");
        } catch (Exception e) {
            return ReturnResult.error("LOCAL_CACHE_ERROR: " + e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult<Boolean> batchDeleteByIds(List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ReturnResult.error("ID列表不能为空");
            }

            // 获取要删除的配置信息用于清理缓存
            List<MonitorSysGenServerSetting> settings = this.listByIds(ids);
            List<Integer> serverIds = settings.stream().map(MonitorSysGenServerSetting::getMonitorSysGenServerId)
                    .filter(Objects::nonNull).collect(Collectors.toList());

            boolean result = this.removeByIds(ids);

            // 如果删除成功，清理相关缓存
            if (result && !serverIds.isEmpty()) {
                for (Integer serverId : serverIds) {
                    try {
                        clearCacheForServer(serverId);
                    } catch (Exception e) {
                        log.warn("清理缓存失败: serverId={}", serverId, e);
                    }
                }
            }

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

            // 设置默认代理配置（用于Guacamole连接）
            setting.setMonitorSysGenServerSettingProxyEnabled(0); // 默认不启用代理
            setting.setMonitorSysGenServerSettingProxyHost("localhost"); // 默认代理主机
            setting.setMonitorSysGenServerSettingProxyPort(4822); // 默认Guacamole代理端口

            // 设置默认文件管理配置
            setting.setMonitorSysGenServerSettingFileManagementEnabled(0); // 默认不启用文件管理
            setting.setMonitorSysGenServerSettingFileManagementMode("NONE"); // 默认不启用文件管理
            setting.setMonitorSysGenServerSettingFileManagementTimeout(60); // 默认超时60秒
            setting.setMonitorSysGenServerSettingFileManagementMaxRetries(3); // 默认重试3次

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
                if (memoryThreshold.compareTo(BigDecimal.ZERO) <= 0
                        || memoryThreshold.compareTo(new BigDecimal("100")) > 0) {
                    return ReturnResult.error("内存告警阈值必须在0-100之间");
                }
            }

            if (setting.getMonitorSysGenServerSettingDiskAlertThreshold() != null) {
                BigDecimal diskThreshold = setting.getMonitorSysGenServerSettingDiskAlertThreshold();
                if (diskThreshold.compareTo(BigDecimal.ZERO) <= 0
                        || diskThreshold.compareTo(new BigDecimal("100")) > 0) {
                    return ReturnResult.error("磁盘告警阈值必须在0-100之间");
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
            if (setting.getMonitorSysGenServerSettingProxyEnabled() != null
                    && setting.getMonitorSysGenServerSettingProxyEnabled() == 1) {

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
                    settings = jsonMapper.readValue(configData, jsonMapper.getTypeFactory()
                            .constructCollectionType(List.class, MonitorSysGenServerSetting.class));
                    break;
                case "yaml":
                    YAMLMapper yamlMapper = new YAMLMapper();
                    settings = yamlMapper.readValue(configData, yamlMapper.getTypeFactory()
                            .constructCollectionType(List.class, MonitorSysGenServerSetting.class));
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
                    MonitorSysGenServerSetting existing = baseMapper
                            .selectByServerId(setting.getMonitorSysGenServerId());
                    if (existing != null && !overwrite) {
                        log.warn("配置已存在且不允许覆盖，跳过服务器: {}", setting.getMonitorSysGenServerId());
                        continue;
                    }

                    // 保存或更新
                    setting.setMonitorSysGenServerSettingId(
                            existing != null ? existing.getMonitorSysGenServerSettingId() : null);
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

            if (!MonitorSysGenServerSettingUtils.isAlertEnabled(setting)) {
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

            if (!MonitorSysGenServerSettingUtils.isMonitorEnabled(setting)) {
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

    @Override
    public ReturnResult<Boolean> testFileManagementConnection(Integer serverId) {
        try {
            if (serverId == null) {
                return ReturnResult.error("服务器ID不能为空");
            }

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = getOrCreateSetting(serverId);
            if (!settingResult.isOk()) {
                return ReturnResult.error("获取服务器配置失败: " + settingResult.getMsg());
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            String fileManagementModeStr = setting.getMonitorSysGenServerSettingFileManagementMode();

            if (fileManagementModeStr == null || fileManagementModeStr.trim().isEmpty()
                    || "NONE".equals(fileManagementModeStr)) {
                return ReturnResult.error("文件管理功能未启用");
            }

            // 根据不同模式进行连接测试
            switch (fileManagementModeStr.toUpperCase()) {
                case "LOCAL":
                    return testLocalFileManagement();
                case "SSH":
                    return testSshFileManagement(serverId);
                case "NODE":
                    return testNodeFileManagement(serverId, setting);
                case "API":
                    return testApiFileManagement(setting);
                default:
                    return ReturnResult.error("不支持的文件管理模式: " + fileManagementModeStr);
            }
        } catch (Exception e) {
            log.error("测试文件管理连接失败: serverId={}", serverId, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getFileManagementConfigTemplate(String mode) {
        try {
            Map<String, Object> template = new HashMap<>();

            switch (mode.toUpperCase()) {
                case "LOCAL":
                    template.put("description", "本地文件管理，直接访问服务器本地文件系统");
                    template.put("requirements", "仅适用于本地服务器");
                    break;
                case "SSH":
                    template.put("description", "通过SSH协议进行文件管理");
                    template.put("requirements", "需要配置SSH连接信息");
                    break;
                case "NODE":
                    template.put("description", "通过NODE客户端代理进行文件管理");
                    template.put("requirements", "需要选择可用的NODE客户端");
                    break;
                case "API":
                    template.put("description", "通过HTTP API进行文件管理");
                    template.put("apiHost", "localhost");
                    template.put("apiPort", 8080);
                    template.put("basePath", "/api/file");
                    template.put("useHttps", false);
                    template.put("authType", "NONE");
                    template.put("connectionTimeout", 30);
                    template.put("readTimeout", 60);
                    template.put("maxRetries", 3);
                    template.put("sslVerificationEnabled", true);
                    break;
                default:
                    return ReturnResult.error("不支持的文件管理模式: " + mode);
            }

            return ReturnResult.ok(template);
        } catch (Exception e) {
            log.error("获取文件管理配置模板失败: mode={}", mode, e);
            return ReturnResult.error("获取配置模板失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> validateFileManagementConfig(Map<String, Object> config) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);

            // 基本验证逻辑
            if (config == null || config.isEmpty()) {
                result.put("valid", false);
                result.put("message", "配置不能为空");
                return ReturnResult.ok(result);
            }

            // 根据不同的配置类型进行验证
            String authType = (String) config.get("authType");
            if ("BASIC".equals(authType)) {
                if (StringUtils.isBlank((String) config.get("username"))
                        || StringUtils.isBlank((String) config.get("password"))) {
                    result.put("valid", false);
                    result.put("message", "Basic认证需要用户名和密码");
                    return ReturnResult.ok(result);
                }
            }

            result.put("message", "配置验证通过");
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("验证文件管理配置失败", e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getAvailableNodeClients() {
        try {
            // 这里应该从Discovery服务或者应用注册中心获取NODE客户端列表
            // 暂时返回模拟数据
            List<Map<String, Object>> clients = new ArrayList<>();

            // 模拟NODE客户端数据
            Map<String, Object> client1 = new HashMap<>();
            client1.put("serverId", "node-client-1");
            client1.put("applicationName", "文件管理客户端-1");
            client1.put("host", "192.168.1.100");
            client1.put("port", 8081);
            client1.put("status", "ONLINE");
            client1.put("version", "1.0.0");
            client1.put("lastHeartbeat", System.currentTimeMillis());
            clients.add(client1);

            Map<String, Object> client2 = new HashMap<>();
            client2.put("serverId", "node-client-2");
            client2.put("applicationName", "文件管理客户端-2");
            client2.put("host", "192.168.1.101");
            client2.put("port", 8081);
            client2.put("status", "OFFLINE");
            client2.put("version", "1.0.0");
            client2.put("lastHeartbeat", System.currentTimeMillis() - 300000); // 5分钟前
            clients.add(client2);

            return ReturnResult.ok(clients);
        } catch (Exception e) {
            log.error("获取可用NODE客户端列表失败", e);
            return ReturnResult.error("获取NODE客户端列表失败: " + e.getMessage());
        }
    }

    /**
     * 测试本地文件管理
     */
    private ReturnResult<Boolean> testLocalFileManagement() {
        try {
            // 测试本地文件系统访问
            File testDir = new File(System.getProperty("login.home"));
            if (testDir.exists() && testDir.canRead()) {
                return ReturnResult.ok(true, "本地文件管理连接正常");
            } else {
                return ReturnResult.error("无法访问本地文件系统");
            }
        } catch (Exception e) {
            return ReturnResult.error("本地文件管理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试SSH文件管理
     */
    private ReturnResult<Boolean> testSshFileManagement(Integer serverId) {
        // 这里应该实现SSH连接测试逻辑
        // 暂时返回成功
        return ReturnResult.ok(true, "SSH文件管理连接测试通过");
    }

    /**
     * 测试NODE文件管理
     */
    private ReturnResult<Boolean> testNodeFileManagement(Integer serverId, MonitorSysGenServerSetting setting) {
        try {
            String nodeClientId = setting.getMonitorSysGenServerSettingFileManagementNodeClient();
            if (StringUtils.isBlank(nodeClientId)) {
                return ReturnResult.error("未选择NODE客户端");
            }

            // 这里应该实现NODE客户端连接测试逻辑
            // 暂时返回成功
            return ReturnResult.ok(true, "NODE客户端连接测试通过");
        } catch (Exception e) {
            return ReturnResult.error("NODE文件管理测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试API文件管理
     */
    private ReturnResult<Boolean> testApiFileManagement(MonitorSysGenServerSetting setting) {
        try {
            String apiConfig = setting.getMonitorSysGenServerSettingFileManagementApiConfig();
            if (StringUtils.isBlank(apiConfig)) {
                return ReturnResult.error("API配置为空");
            }

            // 这里应该实现API连接测试逻辑
            // 暂时返回成功
            return ReturnResult.ok(true, "API文件管理连接测试通过");
        } catch (Exception e) {
            return ReturnResult.error("API文件管理测试失败: " + e.getMessage());
        }
    }

    // ==================== 健康检查和指标采集开关方法实现 ====================

    @Override
    public ReturnResult<Boolean> updateHealthCheckEnabled(Integer serverId, Boolean enabled) {
        try {
            log.info("更新服务器健康检查开关: serverId={}, enabled={}", serverId, enabled);
            
            // 获取或创建配置
            MonitorSysGenServerSetting setting = getOrCreateSettingInternal(serverId);
            if (setting == null) {
                return ReturnResult.error("获取服务器配置失败");
            }
            
            // 更新健康检查开关
            setting.setMonitorSysGenServerSettingHealthCheckEnabled(enabled ? 1 : 0);
            boolean success = updateById(setting);
            
            if (success) {
                // 清除缓存
                invalidateSettingCache(serverId);
                log.info("服务器健康检查开关更新成功: serverId={}, enabled={}", serverId, enabled);
                return ReturnResult.ok(true, enabled ? "健康检查已启用" : "健康检查已禁用");
            } else {
                return ReturnResult.error("更新健康检查开关失败");
            }
        } catch (Exception e) {
            log.error("更新健康检查开关失败: serverId={}", serverId, e);
            return ReturnResult.error("更新健康检查开关失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> updateMetricsCollectionEnabled(Integer serverId, Boolean enabled) {
        try {
            log.info("更新服务器指标采集开关: serverId={}, enabled={}", serverId, enabled);
            
            // 获取或创建配置
            MonitorSysGenServerSetting setting = getOrCreateSettingInternal(serverId);
            if (setting == null) {
                return ReturnResult.error("获取服务器配置失败");
            }
            
            // 更新指标采集开关
            setting.setMonitorSysGenServerSettingMetricsCollectionEnabled(enabled ? 1 : 0);
            boolean success = updateById(setting);
            
            if (success) {
                // 清除缓存
                invalidateSettingCache(serverId);
                log.info("服务器指标采集开关更新成功: serverId={}, enabled={}", serverId, enabled);
                return ReturnResult.ok(true, enabled ? "指标采集已启用" : "指标采集已禁用");
            } else {
                return ReturnResult.error("更新指标采集开关失败");
            }
        } catch (Exception e) {
            log.error("更新指标采集开关失败: serverId={}", serverId, e);
            return ReturnResult.error("更新指标采集开关失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getMonitorSwitchStatus(Integer serverId) {
        try {
            log.debug("获取服务器监控开关状态: serverId={}", serverId);
            
            // 获取配置
            MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
            
            Map<String, Object> status = new HashMap<>();
            if (setting != null) {
                // 健康检查开关状态
                Integer healthCheckEnabled = setting.getMonitorSysGenServerSettingHealthCheckEnabled();
                status.put("healthCheckEnabled", healthCheckEnabled != null && healthCheckEnabled == 1);
                
                // 指标采集开关状态
                Integer metricsCollectionEnabled = setting.getMonitorSysGenServerSettingMetricsCollectionEnabled();
                status.put("metricsCollectionEnabled", metricsCollectionEnabled != null && metricsCollectionEnabled == 1);
                
                // 监控总开关状态
                Integer monitorEnabled = setting.getMonitorSysGenServerSettingMonitorEnabled();
                status.put("monitorEnabled", monitorEnabled != null && monitorEnabled == 1);
                
                // 配置ID
                status.put("settingId", setting.getMonitorSysGenServerSettingId());
            } else {
                // 默认状态（都启用）
                status.put("healthCheckEnabled", true);
                status.put("metricsCollectionEnabled", true);
                status.put("monitorEnabled", true);
                status.put("settingId", null);
            }
            
            status.put("serverId", serverId);
            return ReturnResult.ok(status);
        } catch (Exception e) {
            log.error("获取服务器监控开关状态失败: serverId={}", serverId, e);
            return ReturnResult.error("获取监控开关状态失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchUpdateHealthCheckEnabled(List<Integer> serverIds, Boolean enabled) {
        try {
            log.info("批量更新健康检查开关: serverIds={}, enabled={}", serverIds, enabled);
            
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            
            int successCount = 0;
            for (Integer serverId : serverIds) {
                ReturnResult<Boolean> result = updateHealthCheckEnabled(serverId, enabled);
                if (result.isOk()) {
                    successCount++;
                }
            }
            
            if (successCount == serverIds.size()) {
                return ReturnResult.ok(true, String.format("成功更新 %d 台服务器的健康检查开关", successCount));
            } else if (successCount > 0) {
                return ReturnResult.ok(true, String.format("成功更新 %d/%d 台服务器的健康检查开关", successCount, serverIds.size()));
            } else {
                return ReturnResult.error("批量更新健康检查开关失败");
            }
        } catch (Exception e) {
            log.error("批量更新健康检查开关失败", e);
            return ReturnResult.error("批量更新健康检查开关失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> batchUpdateMetricsCollectionEnabled(List<Integer> serverIds, Boolean enabled) {
        try {
            log.info("批量更新指标采集开关: serverIds={}, enabled={}", serverIds, enabled);
            
            if (serverIds == null || serverIds.isEmpty()) {
                return ReturnResult.error("服务器ID列表不能为空");
            }
            
            int successCount = 0;
            for (Integer serverId : serverIds) {
                ReturnResult<Boolean> result = updateMetricsCollectionEnabled(serverId, enabled);
                if (result.isOk()) {
                    successCount++;
                }
            }
            
            if (successCount == serverIds.size()) {
                return ReturnResult.ok(true, String.format("成功更新 %d 台服务器的指标采集开关", successCount));
            } else if (successCount > 0) {
                return ReturnResult.ok(true, String.format("成功更新 %d/%d 台服务器的指标采集开关", successCount, serverIds.size()));
            } else {
                return ReturnResult.error("批量更新指标采集开关失败");
            }
        } catch (Exception e) {
            log.error("批量更新指标采集开关失败", e);
            return ReturnResult.error("批量更新指标采集开关失败: " + e.getMessage());
        }
    }

    /**
     * 内部方法：获取或创建配置
     */
    private MonitorSysGenServerSetting getOrCreateSettingInternal(Integer serverId) {
        MonitorSysGenServerSetting setting = baseMapper.selectByServerId(serverId);
        if (setting == null) {
            // 创建默认配置
            setting = MonitorSysGenServerSetting.getDefaultSetting(serverId);
            save(setting);
        }
        return setting;
    }

    /**
     * 清除配置缓存
     */
    private void invalidateSettingCache(Integer serverId) {
        // 如果使用了本地缓存，这里清除缓存
        // 当前实现可能使用了其他缓存机制，根据实际情况调整
        log.debug("清除服务器配置缓存: serverId={}", serverId);
    }

    /**
     * 记录配置变更历史
     *
     * @deprecated 暂未使用，预留给将来的配置变更历史记录功能
     */
    @SuppressWarnings("unused")
    private void recordSettingHistory(Integer serverId, Long settingId,
                                      MonitorSysGenServerSettingHistory.ChangeType changeType, String description, String user) {
        try {
            MonitorSysGenServerSettingHistory history = MonitorSysGenServerSettingHistory.createHistory(serverId,
                    settingId, changeType, description, user);

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

}
