// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.configuration;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.service.SysSettingGroup;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.service.mapper.SysSettingGroupMapper;
import com.chua.starter.service.mapper.SysSettingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户协议配置初始化
 *
 * @since 2026/08/03
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.user-agreement", name = "enable", havingValue = "true", matchIfMissing = true)
public class UserAgreementSettingInitializer implements ApplicationRunner {

    /**
     * 配置组编码
     */
    private static final String GROUP_CODE = "userAgreement";

    /**
     * SYS设置映射器
    */
    private final SysSettingMapper sysSettingMapper;
    /**
     * SYS设置分组映射器
    */
    private final SysSettingGroupMapper sysSettingGroupMapper;
    private final Map<String, CacheManager> cacheManagers; // 缓存管理器

    /**
     * 应用启动后执行用户协议配置初始化。
     *
     * <p>整段兜住异常：写协议配置属于「锦上添花」，失败只应让协议用前端静态兜底文案，
     * 不能让异常冒泡终止启动 —— 否则 {@code sys_setting} 结构稍有偏差就会整站起不来。</p>
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            doRun();
        } catch (Exception e) {
            log.warn("[用户协议配置] 初始化失败，已跳过（协议将使用前端静态兜底文案）: {}", e.getMessage());
        }
    }

    private void doRun() {
        boolean changed = false;
        changed |= ensureGroup();
        changed |= ensureSetting("title", "用户协议", "协议标题", "String", 10);
        changed |= ensureSetting("label", "System Console Agreement", "协议副标题", "String", 20);
        changed |= ensureSetting("headline", "平台访问与数据使用说明", "协议头部说明", "String", 30);
        changed |= ensureSetting("description", "账户中心、个人信息下拉与登录安全能力共用同一份协议定义，支持前端静态兜底与后端统一下发。", "协议摘要", "TextArea", 40);
        changed |= ensureSetting("version", "2026.04", "协议版本", "String", 50);
        changed |= ensureSetting("updatedAt", "2026-04-09", "协议更新时间", "String", 60);
        changed |= ensureSetting("section.scope", "本系统仅面向已授权账号使用，账号、消息、日志、菜单与配置数据均纳入平台审计范围。", "使用范围", "TextArea", 70);
        changed |= ensureSetting("section.account", "用户应妥善保管账号、密码与 OTP 动态口令，不得共享账号或绕过权限边界访问未授权数据。", "账号责任", "TextArea", 80);
        changed |= ensureSetting("section.data", "涉及个人信息、部门信息、登录日志与系统消息时，应在业务授权范围内查看、导出和处理。", "数据合规", "TextArea", 90);
        changed |= ensureSetting("section.feedback", "发现异常登录、越权访问、消息异常或配置错误时，应立即停止高风险操作并通过帮助与反馈入口上报。", "异常反馈", "TextArea", 100);
        if (changed) {
            clearCache();
        }
    }

    /**
     * 确保用户协议配置组存在。
     *
     * @return true 表示新建了配置组
     */
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
        group.setSysSettingGroupName("用户协议");
        group.setSysSettingGroupCode(GROUP_CODE);
        group.setSysSettingGroupIcon("ri:file-text-line");
        group.setSysSettingGroupEnable(Boolean.TRUE);
        group.setSysSettingGroupRemark("统一维护登录协议、账户中心协议与确认版本。");
        group.setSysSettingGroupSort(60);
        group.setSysSettingGroupUseProjectInterface(Boolean.FALSE);
        sysSettingGroupMapper.insert(group);
        log.info("[用户协议] 已自动初始化配置组: {}", GROUP_CODE);
        return true;
    }

    /**
     * 确保指定配置项存在。
     *
     * @param name      配置名
     * @param value     配置值
     * @param remark    配置备注
     * @param valueType 值类型
     * @param sort      排序
     * @return true 表示新建了配置项
     */
    private boolean ensureSetting(String name, String value, String remark, String valueType, int sort) {
        SysSetting existed = sysSettingMapper.selectOne(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, GROUP_CODE)
                        .eq(SysSetting::getSysSettingName, name)
                        .last("limit 1")
        );
        if (existed != null) {
            return false;
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
        setting.setSysSettingInSystem(0);
        setting.setSysSettingAppInner(0);
        sysSettingMapper.insert(setting);
        return true;
    }

    /**
     * 清空所有缓存管理器下的缓存。
     */
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
    }}