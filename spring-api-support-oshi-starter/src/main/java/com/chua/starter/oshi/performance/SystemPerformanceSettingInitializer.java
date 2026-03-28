package com.chua.starter.oshi.performance;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingGroup;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Objects;

/**
 * 启动时补齐系统性能监控默认配置。
 *
 * @author CH
 * @since 2026/3/28
 */
@Slf4j
@RequiredArgsConstructor
public class SystemPerformanceSettingInitializer implements ApplicationRunner {

    private final SysSettingMapper sysSettingMapper;
    private final SysSettingGroupMapper sysSettingGroupMapper;

    @Override
    public void run(ApplicationArguments args) {
        ensureDefaultGroup();

        int created = 0;
        int updated = 0;
        for (SystemPerformanceSettings.SettingDefinition definition : SystemPerformanceSettings.definitions()) {
            int result = ensureSetting(definition);
            if (result > 0) {
                created++;
            } else if (result < 0) {
                updated++;
            }
        }
        if (created > 0) {
            log.info("[系统性能监控] 启动时补齐 {} 个默认配置项", created);
        }
        if (updated > 0) {
            log.info("[系统性能监控] 启动时同步 {} 个已有配置项元数据", updated);
        }
    }

    private void ensureDefaultGroup() {
        SysSettingGroup existing = sysSettingGroupMapper.selectOne(Wrappers.<SysSettingGroup>lambdaQuery()
                .eq(SysSettingGroup::getSysSettingGroupCode, SystemPerformanceSettings.SETTING_GROUP)
                .last("limit 1"));
        if (existing != null) {
            return;
        }

        SysSettingGroup group = new SysSettingGroup();
        group.setSysSettingGroupName("默认配置");
        group.setSysSettingGroupCode(SystemPerformanceSettings.SETTING_GROUP);
        group.setSysSettingGroupIcon("ep:setting");
        group.setSysSettingGroupEnable(Boolean.TRUE);
        group.setSysSettingGroupRemark("系统默认配置");
        group.setSysSettingGroupSort(1);
        group.setSysSettingGroupUseProjectInterface(Boolean.FALSE);
        sysSettingGroupMapper.insert(group);
        log.info("[系统性能监控] 自动创建 default 配置组");
    }

    private int ensureSetting(SystemPerformanceSettings.SettingDefinition definition) {
        SysSetting existing = sysSettingMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, SystemPerformanceSettings.SETTING_GROUP)
                .eq(SysSetting::getSysSettingName, definition.name())
                .last("limit 1"));
        if (existing != null) {
            return updateSetting(existing, definition) ? -1 : 0;
        }

        SysSetting setting = new SysSetting();
        setting.setSysSettingName(definition.name());
        setting.setSysSettingValue(resolveInitialValue(definition));
        setting.setSysSettingValueType(definition.valueType());
        setting.setSysSettingGroup(SystemPerformanceSettings.SETTING_GROUP);
        setting.setSysSettingStatus(1);
        setting.setSysSettingSort(definition.sort());
        setting.setSysSettingCollect(0);
        setting.setSysSettingInSystem(1);
        setting.setSysSettingAppInner(0);
        setting.setSysSettingAdmin(0);
        setting.setSysSettingVersion(1);
        setting.setSysSettingConfig(definition.config());
        setting.setSysSettingRemark(definition.remark());
        sysSettingMapper.insert(setting);
        return 1;
    }

    private boolean updateSetting(SysSetting existing, SystemPerformanceSettings.SettingDefinition definition) {
        boolean changed = false;
        if (StringUtils.isBlank(existing.getSysSettingValue())) {
            existing.setSysSettingValue(resolveInitialValue(definition));
            changed = true;
        }
        if (!Objects.equals(existing.getSysSettingValueType(), definition.valueType())) {
            existing.setSysSettingValueType(definition.valueType());
            changed = true;
        }
        if (!Objects.equals(existing.getSysSettingConfig(), definition.config())) {
            existing.setSysSettingConfig(definition.config());
            changed = true;
        }
        if (!Objects.equals(existing.getSysSettingRemark(), definition.remark())) {
            existing.setSysSettingRemark(definition.remark());
            changed = true;
        }
        if (!Objects.equals(existing.getSysSettingSort(), definition.sort())) {
            existing.setSysSettingSort(definition.sort());
            changed = true;
        }
        if (!changed) {
            return false;
        }
        sysSettingMapper.updateById(existing);
        return true;
    }

    private String resolveInitialValue(SystemPerformanceSettings.SettingDefinition definition) {
        String migratedValue = switch (definition.name()) {
            case SystemPerformanceSettings.SHOW_FPS_MONITOR -> findSettingValue(SystemPerformanceSettings.MONITOR_OPEN);
            case SystemPerformanceSettings.SHOW_CPU_MONITOR -> findSettingValue("PerformanceMonitorCpuOpen");
            case SystemPerformanceSettings.SHOW_MEMORY_MONITOR -> findSettingValue("PerformanceMonitorMemoryOpen");
            case SystemPerformanceSettings.SHOW_BANDWIDTH_MONITOR -> findSettingValue("PerformanceMonitorNetworkOpen");
            default -> null;
        };
        if (StringUtils.isNotBlank(migratedValue)) {
            return migratedValue;
        }
        return definition.defaultValue();
    }

    private String findSettingValue(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        SysSetting existing = sysSettingMapper.selectOne(Wrappers.<SysSetting>lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, SystemPerformanceSettings.SETTING_GROUP)
                .eq(SysSetting::getSysSettingName, name)
                .last("limit 1"));
        return existing == null ? null : existing.getSysSettingValue();
    }
}
