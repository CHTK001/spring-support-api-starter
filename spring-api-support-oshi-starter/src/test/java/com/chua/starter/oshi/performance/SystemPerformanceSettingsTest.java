package com.chua.starter.oshi.performance;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SystemPerformanceSettingsTest {

    @Test
    void shouldExposeFrontendCompatibleSettings() {
        Map<String, SystemPerformanceSettings.SettingDefinition> definitions = SystemPerformanceSettings.definitions().stream()
                .collect(Collectors.toMap(
                        SystemPerformanceSettings.SettingDefinition::name,
                        Function.identity()
                ));

        assertThat(definitions).containsKeys(
                SystemPerformanceSettings.SHOW_FPS_MONITOR,
                SystemPerformanceSettings.SHOW_CPU_MONITOR,
                SystemPerformanceSettings.SHOW_MEMORY_MONITOR,
                SystemPerformanceSettings.SHOW_BANDWIDTH_MONITOR,
                SystemPerformanceSettings.SHOW_BATTERY_MONITOR,
                SystemPerformanceSettings.SHOW_BLUETOOTH_MONITOR,
                SystemPerformanceSettings.SHOW_SCREEN_MONITOR,
                SystemPerformanceSettings.MONITOR_POSITION,
                SystemPerformanceSettings.MONITOR_MODE,
                SystemPerformanceSettings.MONITOR_LAYOUT,
                SystemPerformanceSettings.MONITOR_DIRECTION
        );
        assertThat(definitions.get(SystemPerformanceSettings.MONITOR_POSITION).defaultValue()).isEqualTo("top-left");
        assertThat(definitions.get(SystemPerformanceSettings.MONITOR_MODE).defaultValue()).isEqualTo("minimal");
        assertThat(definitions.get(SystemPerformanceSettings.MONITOR_LAYOUT).defaultValue()).isEqualTo("merged");
        assertThat(definitions.get(SystemPerformanceSettings.MONITOR_DIRECTION).defaultValue()).isEqualTo("auto");
    }

    @Test
    void shouldUseCanonicalValueTypes() {
        assertThat(SystemPerformanceSettings.definitions())
                .extracting(SystemPerformanceSettings.SettingDefinition::valueType)
                .containsOnly("BOOL", "NUMBER", "TEXT");
    }
}
