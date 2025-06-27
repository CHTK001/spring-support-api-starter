package com.chua.starter.monitor.starter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.mapper.MonitorSysGenServerSettingMapper;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            
            return ReturnResult.ok(true, "配置验证通过");
        } catch (Exception e) {
            log.error("验证配置失败", e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    // 其他方法的实现将在下一个编辑中添加
    @Override
    public ReturnResult<Boolean> copySettingToServers(Integer sourceServerId, List<Integer> targetServerIds) {
        // TODO: 实现配置复制逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> getSettingTemplate(String templateType) {
        // TODO: 实现配置模板逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<String> exportSettings(List<Integer> serverIds, String format) {
        // TODO: 实现配置导出逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<Boolean> importSettings(String configData, String format, boolean overwrite) {
        // TODO: 实现配置导入逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<Boolean> resetToDefault(Integer serverId) {
        // TODO: 实现重置为默认配置逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<List<Map<String, Object>>> getSettingHistory(Integer serverId, Integer limit) {
        // TODO: 实现配置历史逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<Boolean> testAlertConfiguration(Integer serverId) {
        // TODO: 实现告警配置测试逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<Boolean> testReportConfiguration(Integer serverId) {
        // TODO: 实现上报配置测试逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<MonitorSysGenServerSetting> getRecommendedSetting(Integer serverId) {
        // TODO: 实现推荐配置逻辑
        return ReturnResult.error("功能暂未实现");
    }

    @Override
    public ReturnResult<Boolean> applyTemplateToServers(String templateType, List<Integer> serverIds) {
        // TODO: 实现模板应用逻辑
        return ReturnResult.error("功能暂未实现");
    }
}
