package com.chua.starter.service.impl;

import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.service.SysSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("热更新配置校验测试")
class HotReloadSettingValidationTest {

    @Test
    @DisplayName("保存前强制分组和默认状态并规范类型")
    void shouldNormalizeHotReloadSetting() {
        SysSetting setting = new SysSetting();
        setting.setSysSettingName("  runtime.enabled  ");
        setting.setSysSettingCode("  runtime.enabled  ");
        setting.setSysSettingValue("true");
        setting.setSysSettingValueType("BOOLEAN");
        setting.setSysSettingGroup("other");

        assertNull(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting));
        assertEquals(SysSettingService.HOT_RELOAD_GROUP, setting.getSysSettingGroup());
        assertEquals(1, setting.getSysSettingStatus());
        assertEquals("runtime.enabled", setting.getSysSettingName());
        assertEquals("runtime.enabled", setting.getSysSettingCode());
        assertEquals("boolean", setting.getSysSettingValueType());
    }

    @Test
    @DisplayName("拒绝空名称空编码和不支持类型")
    void shouldRejectInvalidMetadata() {
        SysSetting setting = new SysSetting();
        setting.setSysSettingCode("runtime.enabled");
        setting.setSysSettingValue("1");
        setting.setSysSettingValueType("number");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("名称"));

        setting.setSysSettingName("runtime.enabled");
        setting.setSysSettingCode(" ");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("编码"));

        setting.setSysSettingCode("runtime.enabled");
        setting.setSysSettingValueType("text");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("类型"));
    }

    @Test
    @DisplayName("按值类型校验运行时值")
    void shouldValidateTypedValues() {
        SysSetting setting = new SysSetting();
        setting.setSysSettingName("runtime.number");
        setting.setSysSettingCode("runtime.number");
        setting.setSysSettingValueType("number");
        setting.setSysSettingValue("not-a-number");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("number"));

        setting.setSysSettingValueType("boolean");
        setting.setSysSettingValue("yes");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("boolean"));

        setting.setSysSettingValueType("json");
        setting.setSysSettingValue("{broken");
        assertTrue(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting).contains("json"));

        setting.setSysSettingValueType("string");
        setting.setSysSettingValue(null);
        assertNull(SysSettingServiceImpl.normalizeAndValidateHotReloadSetting(setting));
    }
}
