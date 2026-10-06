package com.chua.starter.system.configuration;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingGroup;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OIDC 登录配置初始化
 *
 * <p>幂等补齐：新建 {@code oidc} 配置组与 5 个参数行（issuerUri / clientId / clientSecret /
 * redirectUri / scope），以及 {@code sso} 分组下的 {@code Oidc} 总开关。已存在的行一律不覆盖，
 * 仅在有新增时清缓存。异常只告警不阻断启动。</p>
 *
 * @since 2026/10/06
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OidcSettingInitializer implements ApplicationRunner {

    /**
     * OIDC 参数配置组编码
     */
    private static final String GROUP_CODE = "oidc";

    /**
     * SSO 总开关分组编码
     */
    private static final String SSO_GROUP = "sso";

    /**
     * SSO 分组下的 OIDC 开关名（与 OidcCallbackController.SSO_NAME 对齐）
     */
    private static final String SSO_NAME = "Oidc";

    private final SysSettingMapper sysSettingMapper;
    private final SysSettingGroupMapper sysSettingGroupMapper;
    private final Map<String, CacheManager> cacheManagers;

    @Override
    public void run(ApplicationArguments args) {
        try {
            doRun();
        } catch (Exception e) {
            log.warn("[OIDC 配置] 初始化失败，已跳过（OIDC 登录将不可用）: {}", e.getMessage());
        }
    }

    private void doRun() {
        boolean changed = false;
        changed |= ensureGroup();
        changed |= ensureSsoSwitch();
        changed |= ensureSetting(GROUP_CODE, "clientId", "", "OIDC Client ID", "String", 1);
        changed |= ensureSetting(GROUP_CODE, "clientSecret", "", "OIDC Client Secret", "Password", 2);
        changed |= ensureSetting(GROUP_CODE, "redirectUri", "", "OIDC 回调地址", "String", 3);
        changed |= ensureSetting(GROUP_CODE, "issuerUri", "", "OIDC Issuer 地址", "String", 4);
        changed |= ensureSetting(GROUP_CODE, "scope", "openid profile email", "OIDC 请求范围", "String", 5);
        if (changed) {
            clearCache();
        }
    }

    private boolean ensureGroup() {
        SysSettingGroup existed = sysSettingGroupMapper.selectOne(
                Wrappers.<SysSettingGroup>lambdaQuery()
                        .eq(SysSettingGroup::getSysSettingGroupCode, GROUP_CODE)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
        }
        SysSettingGroup group = new SysSettingGroup();
        group.setSysSettingGroupName("OIDC 登录");
        group.setSysSettingGroupCode(GROUP_CODE);
        group.setSysSettingGroupIcon("simple-icons:openid");
        group.setSysSettingGroupEnable(Boolean.TRUE);
        group.setSysSettingGroupRemark("OIDC 通用身份提供方配置");
        group.setSysSettingGroupSort(7);
        group.setSysSettingGroupUseProjectInterface(Boolean.FALSE);
        sysSettingGroupMapper.insert(group);
        log.info("[OIDC] 已自动初始化配置组: {}", GROUP_CODE);
        return true;
    }

    private boolean ensureSsoSwitch() {
        SysSetting existed = sysSettingMapper.selectOne(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, SSO_GROUP)
                        .eq(SysSetting::getSysSettingName, SSO_NAME)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
        }
        SysSetting setting = newSetting(SSO_GROUP, SSO_NAME, "false", "OIDC 登录开关", "Boolean", 4);
        sysSettingMapper.insert(setting);
        return true;
    }

    private boolean ensureSetting(String group, String name, String value, String remark, String valueType, int sort) {
        SysSetting existed = sysSettingMapper.selectOne(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, group)
                        .eq(SysSetting::getSysSettingName, name)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
        }
        sysSettingMapper.insert(newSetting(group, name, value, remark, valueType, sort));
        return true;
    }

    private static SysSetting newSetting(String group, String name, String value, String remark, String valueType, int sort) {
        SysSetting setting = new SysSetting();
        setting.setSysSettingName(name);
        setting.setSysSettingGroup(group);
        setting.setSysSettingValue(value);
        setting.setSysSettingRemark(remark);
        setting.setSysSettingValueType(valueType);
        setting.setSysSettingSort(sort);
        setting.setSysSettingStatus(1);
        setting.setSysSettingAdmin(1);
        setting.setSysSettingCollect(0);
        setting.setSysSettingInSystem(1);
        setting.setSysSettingAppInner(0);
        return setting;
    }

    private void clearCache() {
        cacheManagers.forEach((cacheName, manager) -> {
            Cache namedCache = manager.getCache(cacheName);
            if (namedCache != null) {
                namedCache.clear();
            }
            manager.getCacheNames().forEach(existingCacheName -> {
                Cache cache = manager.getCache(existingCacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        });
    }
}
