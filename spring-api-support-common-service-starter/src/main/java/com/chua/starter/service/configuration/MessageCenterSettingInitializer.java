package com.chua.starter.service.configuration;

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

import java.util.Objects;
import java.util.Map;

/**
 * 消息中心配置初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCenterSettingInitializer implements ApplicationRunner {

    public static final String GROUP_CODE = "messageCenter";
    public static final String PREVIEW_LIMIT_NAME = "previewLimit";
    public static final String PREVIEW_LIMIT_CONFIG = "{\"min\":1,\"max\":20,\"step\":1}";

    private final SysSettingMapper sysSettingMapper;
    private final SysSettingGroupMapper sysSettingGroupMapper;
    private final Map<String, CacheManager> cacheManagers;

    @Override
    public void run(ApplicationArguments args) {
        boolean changed = false;
        changed |= ensureGroup();
        changed |= ensureSetting(
                PREVIEW_LIMIT_NAME,
                "5",
                "消息预览条数（未读不足时自动补最近已读，最大20条）",
                "Number",
                10,
                PREVIEW_LIMIT_CONFIG
        );
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
        group.setSysSettingGroupName("消息中心");
        group.setSysSettingGroupCode(GROUP_CODE);
        group.setSysSettingGroupIcon("ri:message-3-line");
        group.setSysSettingGroupEnable(Boolean.TRUE);
        group.setSysSettingGroupRemark("控制登录后和顶部消息中心的预览条数。");
        group.setSysSettingGroupSort(55);
        group.setSysSettingGroupUseProjectInterface(Boolean.FALSE);
        sysSettingGroupMapper.insert(group);
        log.info("[消息中心] 已自动初始化配置组: {}", GROUP_CODE);
        return true;
    }

    private boolean ensureSetting(
            String name,
            String value,
            String remark,
            String valueType,
            int sort,
            String config
    ) {
        SysSetting existed = sysSettingMapper.selectOne(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, GROUP_CODE)
                        .eq(SysSetting::getSysSettingName, name)
                        .last("limit 1")
        );
        if (existed != null) {
            boolean changed = false;
            if (!remark.equals(existed.getSysSettingRemark())) {
                existed.setSysSettingRemark(remark);
                changed = true;
            }
            if (!valueType.equals(existed.getSysSettingValueType())) {
                existed.setSysSettingValueType(valueType);
                changed = true;
            }
            if (!Objects.equals(sort, existed.getSysSettingSort())) {
                existed.setSysSettingSort(sort);
                changed = true;
            }
            if (config != null && !config.equals(existed.getSysSettingConfig())) {
                existed.setSysSettingConfig(config);
                changed = true;
            }
            if (changed) {
                sysSettingMapper.updateById(existed);
            }
            return changed;
        }

        SysSetting setting = new SysSetting();
        setting.setSysSettingName(name);
        setting.setSysSettingGroup(GROUP_CODE);
        setting.setSysSettingValue(value);
        setting.setSysSettingRemark(remark);
        setting.setSysSettingValueType(valueType);
        setting.setSysSettingSort(sort);
        setting.setSysSettingStatus(1);
        setting.setSysSettingAdmin(1);
        setting.setSysSettingCollect(0);
        setting.setSysSettingInSystem(1);
        setting.setSysSettingAppInner(0);
        setting.setSysSettingConfig(config);
        sysSettingMapper.insert(setting);
        return true;
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
