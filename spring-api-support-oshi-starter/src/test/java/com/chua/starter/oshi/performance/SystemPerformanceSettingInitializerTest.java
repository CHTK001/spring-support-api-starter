package com.chua.starter.oshi.performance;

import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingGroup;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemPerformanceSettingInitializerTest {

    @Mock
    private SysSettingMapper sysSettingMapper;

    @Mock
    private SysSettingGroupMapper sysSettingGroupMapper;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private SystemPerformanceSettingInitializer initializer;

    @Test
    void shouldCreateFrontendCompatibilitySettingsUsingLegacyValues() {
        when(sysSettingGroupMapper.selectOne(any())).thenReturn(existingGroup());

        Map<String, SysSetting> existingSettings = new LinkedHashMap<>();
        existingSettings.put(SystemPerformanceSettings.MONITOR_OPEN, existingSetting(
                SystemPerformanceSettings.MONITOR_OPEN,
                "true"
        ));
        existingSettings.put("PerformanceMonitorCpuOpen", existingSetting(
                "PerformanceMonitorCpuOpen",
                "true"
        ));
        existingSettings.put("PerformanceMonitorMemoryOpen", existingSetting(
                "PerformanceMonitorMemoryOpen",
                "false"
        ));
        existingSettings.put("PerformanceMonitorNetworkOpen", existingSetting(
                "PerformanceMonitorNetworkOpen",
                "true"
        ));
        stubSelectOneSequence(existingSettings);

        initializer.run(applicationArguments);

        ArgumentCaptor<SysSetting> insertCaptor = ArgumentCaptor.forClass(SysSetting.class);
        verify(sysSettingMapper, atLeastOnce()).insert(insertCaptor.capture());

        Map<String, SysSetting> inserted = insertCaptor.getAllValues().stream()
                .collect(Collectors.toMap(
                        SysSetting::getSysSettingName,
                        item -> item,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        assertThat(inserted.get(SystemPerformanceSettings.SHOW_FPS_MONITOR).getSysSettingValue()).isEqualTo("true");
        assertThat(inserted.get(SystemPerformanceSettings.SHOW_CPU_MONITOR).getSysSettingValue()).isEqualTo("true");
        assertThat(inserted.get(SystemPerformanceSettings.SHOW_MEMORY_MONITOR).getSysSettingValue()).isEqualTo("false");
        assertThat(inserted.get(SystemPerformanceSettings.SHOW_BANDWIDTH_MONITOR).getSysSettingValue()).isEqualTo("true");
        assertThat(inserted.get(SystemPerformanceSettings.SHOW_BATTERY_MONITOR).getSysSettingValue()).isEqualTo("false");
    }

    @Test
    void shouldUpdateExistingFrontendSettingMetadataWithoutOverwritingValue() {
        when(sysSettingGroupMapper.selectOne(any())).thenReturn(existingGroup());

        SysSetting showCpuMonitor = new SysSetting();
        showCpuMonitor.setSysSettingName(SystemPerformanceSettings.SHOW_CPU_MONITOR);
        showCpuMonitor.setSysSettingGroup(SystemPerformanceSettings.SETTING_GROUP);
        showCpuMonitor.setSysSettingValue("true");
        showCpuMonitor.setSysSettingValueType("Boolean");
        showCpuMonitor.setSysSettingConfig("legacy");
        showCpuMonitor.setSysSettingRemark("legacy");
        showCpuMonitor.setSysSettingSort(1);

        Map<String, SysSetting> existingSettings = Map.of(
                SystemPerformanceSettings.SHOW_CPU_MONITOR,
                showCpuMonitor
        );
        stubSelectOneSequence(existingSettings);

        initializer.run(applicationArguments);

        ArgumentCaptor<SysSetting> updateCaptor = ArgumentCaptor.forClass(SysSetting.class);
        verify(sysSettingMapper).updateById(updateCaptor.capture());

        SysSetting updated = updateCaptor.getValue();
        assertThat(updated.getSysSettingName()).isEqualTo(SystemPerformanceSettings.SHOW_CPU_MONITOR);
        assertThat(updated.getSysSettingValue()).isEqualTo("true");
        assertThat(updated.getSysSettingValueType()).isEqualTo("BOOL");
        assertThat(updated.getSysSettingConfig()).isEqualTo("performance-monitor:frontend:cpu");
        assertThat(updated.getSysSettingRemark()).isEqualTo("前端CPU监控显示开关");
        assertThat(updated.getSysSettingSort()).isEqualTo(31);
    }

    private void stubSelectOneSequence(Map<String, SysSetting> existingSettings) {
        ArrayList<SysSetting> responses = new ArrayList<>();
        for (SystemPerformanceSettings.SettingDefinition definition : SystemPerformanceSettings.definitions()) {
            responses.add(existingSettings.get(definition.name()));
            String legacySourceName = legacySourceName(definition.name());
            if (legacySourceName != null && !existingSettings.containsKey(definition.name())) {
                responses.add(existingSettings.get(legacySourceName));
            }
        }
        when(sysSettingMapper.selectOne(any())).thenReturn(
                responses.getFirst(),
                responses.subList(1, responses.size()).toArray(SysSetting[]::new)
        );
    }

    private String legacySourceName(String name) {
        return switch (name) {
            case SystemPerformanceSettings.SHOW_FPS_MONITOR -> SystemPerformanceSettings.MONITOR_OPEN;
            case SystemPerformanceSettings.SHOW_CPU_MONITOR -> "PerformanceMonitorCpuOpen";
            case SystemPerformanceSettings.SHOW_MEMORY_MONITOR -> "PerformanceMonitorMemoryOpen";
            case SystemPerformanceSettings.SHOW_BANDWIDTH_MONITOR -> "PerformanceMonitorNetworkOpen";
            default -> null;
        };
    }

    private SysSetting existingSetting(String name, String value) {
        SystemPerformanceSettings.SettingDefinition definition = SystemPerformanceSettings.definition(name);
        SysSetting setting = new SysSetting();
        setting.setSysSettingName(name);
        setting.setSysSettingGroup(SystemPerformanceSettings.SETTING_GROUP);
        setting.setSysSettingValue(value);
        if (definition != null) {
            setting.setSysSettingValueType(definition.valueType());
            setting.setSysSettingConfig(definition.config());
            setting.setSysSettingRemark(definition.remark());
            setting.setSysSettingSort(definition.sort());
        }
        return setting;
    }

    private SysSettingGroup existingGroup() {
        SysSettingGroup group = new SysSettingGroup();
        group.setSysSettingGroupCode(SystemPerformanceSettings.SETTING_GROUP);
        group.setSysSettingGroupName("默认配置");
        return group;
    }

    private SysSetting existingSetting(String name, String value, String valueType, String config, String remark, Integer sort) {
        SysSetting setting = new SysSetting();
        setting.setSysSettingName(name);
        setting.setSysSettingGroup(SystemPerformanceSettings.SETTING_GROUP);
        setting.setSysSettingValue(value);
        setting.setSysSettingValueType(valueType);
        setting.setSysSettingConfig(config);
        setting.setSysSettingRemark(remark);
        setting.setSysSettingSort(sort);
        return setting;
    }

    @Test
    void shouldKeepExistingValueWhenMetadataNeedsAlignment() {
        when(sysSettingGroupMapper.selectOne(any())).thenReturn(existingGroup());

        Map<String, SysSetting> existingSettings = Map.of(
                SystemPerformanceSettings.SHOW_MEMORY_MONITOR,
                existingSetting(
                        SystemPerformanceSettings.SHOW_MEMORY_MONITOR,
                        "true",
                        "Boolean",
                        "legacy",
                        "legacy",
                        3
                )
        );
        stubSelectOneSequence(existingSettings);

        initializer.run(applicationArguments);

        ArgumentCaptor<SysSetting> updateCaptor = ArgumentCaptor.forClass(SysSetting.class);
        verify(sysSettingMapper).updateById(updateCaptor.capture());

        SysSetting updated = updateCaptor.getValue();
        assertThat(updated.getSysSettingName()).isEqualTo(SystemPerformanceSettings.SHOW_MEMORY_MONITOR);
        assertThat(updated.getSysSettingValue()).isEqualTo("true");
        assertThat(updated.getSysSettingValueType()).isEqualTo("BOOL");
    }
}
