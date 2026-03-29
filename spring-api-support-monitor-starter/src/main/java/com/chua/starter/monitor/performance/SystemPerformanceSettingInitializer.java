package com.chua.starter.monitor.performance;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingGroup;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

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
        for (SystemPerformanceSettings.SettingDefinition definition : SystemPerformanceSettings.definitions()) {
            created += ensureSetting(definition);
        }
        if (created > 0) {
            log.info("[系统性能监控] 启动时补齐 {} 个默认配置项", created);
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
            return 0;
        }

        SysSetting setting = new SysSetting();
        setting.setSysSettingName(definition.name());
        setting.setSysSettingValue(definition.defaultValue());
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
}
