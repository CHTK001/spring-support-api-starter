// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
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
 * Third-party login setting initializer.
 *
 * <p>Idempotent fill for 12 platforms: wechat / wechat_mp / wechat_enterprise /
 * qq / alipay / dingtalk / weibo / baidu / microsoft / taobao / douyin / feishu.
 * Each platform gets one config group, one sso switch and 3 params
 * (clientId / clientSecret / redirectUri). Existing rows are never overwritten,
 * cache is cleared only when something new is inserted. Failures only warn.</p>
 *
 * @since 2026/10/06
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPartySettingInitializer implements ApplicationRunner {

    /**
     * SSO switch group code.
     */
    private static final String SSO_GROUP = "sso";

    /**
     * Login callback base url, {loginType} is replaced per platform.
     */
    private static final String CALLBACK_TPL = "http://127.0.0.1:18170/system/api/v2/third/login/{loginType}/callback";

    /**
     * Platform definitions: loginType, sso switch name, sso sort, group sort, icon, label.
     */
    private static final Platform[] PLATFORMS = {
            new Platform("wechat", "Wechat", 5, 10, "simple-icons:wechat", "Wechat"),
            new Platform("wechat_mp", "Wechat_mp", 6, 11, "simple-icons:wechat", "Wechat_mp"),
            new Platform("wechat_enterprise", "Wechat_enterprise", 7, 12, "simple-icons:wechat", "Wechat_enterprise"),
            new Platform("qq", "Qq", 8, 13, "simple-icons:qq", "Qq"),
            new Platform("alipay", "Alipay", 9, 14, "simple-icons:alipay", "Alipay"),
            new Platform("dingtalk", "Dingtalk", 10, 15, "simple-icons:dingtalk", "Dingtalk"),
            new Platform("weibo", "Weibo", 11, 16, "simple-icons:weibo", "Weibo"),
            new Platform("baidu", "Baidu", 12, 17, "simple-icons:baidu", "Baidu"),
            new Platform("microsoft", "Microsoft", 13, 18, "simple-icons:microsoft", "Microsoft"),
            new Platform("taobao", "Taobao", 14, 19, "simple-icons:taobao", "Taobao"),
            new Platform("douyin", "Douyin", 15, 20, "simple-icons:douyin", "Douyin"),
            new Platform("feishu", "Feishu", 16, 21, "simple-icons:feishu", "Feishu"),
    };

    private final SysSettingMapper sysSettingMapper;
    private final SysSettingGroupMapper sysSettingGroupMapper;
    private final Map<String, CacheManager> cacheManagers;

    @Override
    public void run(ApplicationArguments args) {
        try {
            doRun();
        } catch (Exception e) {
            log.warn("[ThirdParty] init skipped: {}", e.getMessage());
        }
    }

    private void doRun() {
        boolean changed = false;
        for (Platform p : PLATFORMS) {
            try {
                changed |= ensureGroup(p);
                changed |= ensureSsoSwitch(p);
                changed |= ensureSetting(p.loginType, "clientId", "", p.label + " Client ID", "String", 1);
                changed |= ensureSetting(p.loginType, "clientSecret", "", p.label + " Client Secret", "Password", 2);
                changed |= ensureSetting(p.loginType, "redirectUri",
                        CALLBACK_TPL.replace("{loginType}", p.loginType),
                        p.label + " callback url", "String", 3);
            } catch (Exception e) {
                log.warn("[ThirdParty] platform {} init skipped: {}", p.loginType, e.getMessage());
            }
        }
        if (changed) {
            clearCache();
        }
    }

    /** ensureGroup 方法 */
    private boolean ensureGroup(Platform p) {
        SysSettingGroup existed = sysSettingGroupMapper.selectOne(
                Wrappers.<SysSettingGroup>lambdaQuery()
                        .eq(SysSettingGroup::getSysSettingGroupCode, p.loginType)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
        }
        SysSettingGroup group = new SysSettingGroup();
        group.setSysSettingGroupName(p.label);
        group.setSysSettingGroupCode(p.loginType);
        group.setSysSettingGroupIcon(p.icon);
        group.setSysSettingGroupEnable(Boolean.TRUE);
        group.setSysSettingGroupRemark(p.label + " OAuth config");
        group.setSysSettingGroupSort(p.groupSort);
        group.setSysSettingGroupUseProjectInterface(Boolean.FALSE);
        sysSettingGroupMapper.insert(group);
        log.info("[ThirdParty] group inited: {}", p.loginType);
        return true;
    }

    /** ensureSsoSwitch 方法 */
    private boolean ensureSsoSwitch(Platform p) {
        SysSetting existed = sysSettingMapper.selectOne(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, SSO_GROUP)
                        .eq(SysSetting::getSysSettingName, p.ssoName)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
        }
        sysSettingMapper.insert(newSetting(SSO_GROUP, p.ssoName, "false", p.label + " login switch", "Boolean", p.ssoSort));
        return true;
    }

    /** ensureSetting 方法 */
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

    /** newSetting 方法 */
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

    /** clearCache 方法 */
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

    private static final class Platform {
        final String loginType;
        final String ssoName;
        final int ssoSort;
        final int groupSort;
        final String icon;
        final String label;

        Platform(String loginType, String ssoName, int ssoSort, int groupSort, String icon, String label) {
            this.loginType = loginType;
            this.ssoName = ssoName;
            this.ssoSort = ssoSort;
            this.groupSort = groupSort;
            this.icon = icon;
            this.label = label;
        }
    }
}