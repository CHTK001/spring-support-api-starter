package com.chua.starter.system.support;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.api.feature.ApiFeatureManager;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.system.service.SysPolicyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略注解在线配置桥接器
 *
 * <p>把统一策略配置表里的 {@code type='FEATURE'} / {@code type='GRAY'} 行，
 * 同步到框架侧的 {@link ApiFeatureManager}，使 {@code @ApiFeature} / {@code @ApiGray}
 * 这类注解的开关与灰度参数可以在线配置而不必改代码重启。</p>
 *
 * <p><b>分层说明：</b>框架模块 {@code spring-support-common-starter} 不能反向依赖业务模块，
 * 因此桥接逻辑放在 {@code common-system}（它同时依赖框架与自身实体），框架侧保持零业务依赖。</p>
 *
 * <p>同步时机：应用就绪时全量对齐一次；运行期每次保存/切换 FEATURE 或 GRAY 行后调用
 * {@link #applyFeature(String, boolean)} 增量更新。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class PolicyAnnotationBridge implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 统一策略配置服务
     */
    private final SysPolicyConfigService sysPolicyConfigService;

    /**
     * 框架侧功能开关管理器（懒可选）
     *
     * <p>用 {@code ObjectProvider} 而非直接注入：{@code ApiFeatureManager} 由框架侧
     * {@code ApiFeatureConfiguration} 注册，而该配置类当前并未列入
     * {@code AutoConfiguration.imports}，在按包扫描不到它的宿主里不会存在。
     * 直接注入会让应用启动失败，功能开关本身是可选能力，不该成为硬依赖。</p>
     */
    private final ObjectProvider<ApiFeatureManager> apiFeatureManagerProvider;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            syncFeatures();
            log.info("策略注解在线配置已同步: feature={} 条, gray={} 条",
                    sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_FEATURE, null).size(),
                    sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_GRAY, null).size());
        } catch (Exception e) {
            // 同步失败不应阻断应用启动，注解默认值继续生效
            log.error("同步策略注解在线配置失败，功能开关回退为注解默认值", e);
        }
    }

    /**
     * 取框架侧功能开关管理器，不可用时返回 null。
     *
     * @return 功能开关管理器，未注册时为 null
     */
    private ApiFeatureManager manager() {
        return apiFeatureManagerProvider.getIfAvailable();
    }

    /**
     * 全量对齐 FEATURE 行到功能开关管理器。
     *
     * <p>只有注解已注册（{@code getFeatureInfo != null}）的功能才写回，
     * 避免库里残留的历史行把未知功能的默认开启状态带偏。</p>
     */
    public void syncFeatures() {
        ApiFeatureManager manager = manager();
        if (null == manager) {
            log.info("功能开关管理器未注册（ApiFeatureConfiguration 未生效），跳过 @ApiFeature 在线配置同步");
            return;
        }
        for (SysPolicyConfig row : sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_FEATURE, null)) {
            if (StringUtils.isBlank(row.getBizKey())) {
                continue;
            }
            applyFeature(row.getBizKey(), null != row.getStatus() && row.getStatus() == SysPolicyConfig.STATUS_ENABLED);
        }
    }

    /**
     * 增量更新单个功能开关。
     *
     * @param featureId 功能标识
     * @param enabled   是否启用
     * @return 是否成功（管理器缺失或功能未注册时返回 false）
     */
    public boolean applyFeature(String featureId, boolean enabled) {
        if (StringUtils.isBlank(featureId)) {
            return false;
        }
        ApiFeatureManager manager = manager();
        if (null == manager) {
            log.warn("功能开关管理器未注册，忽略在线配置: {}", featureId);
            return false;
        }
        if (null == manager.getFeatureInfo(featureId)) {
            log.warn("功能开关未在注解中注册，忽略在线配置: {}", featureId);
            return false;
        }
        return manager.setEnabled(featureId, enabled);
    }

    /**
     * 读取全部灰度配置（启用优先），供灰度评估器叠加注解默认值。
     *
     * @return 业务键 → 灰度行
     */
    public Map<String, SysPolicyConfig> listEnabledGray() {
        Map<String, SysPolicyConfig> result = new LinkedHashMap<>();
        for (SysPolicyConfig row : sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_GRAY, null)) {
            if (null == row.getStatus() || row.getStatus() == SysPolicyConfig.STATUS_ENABLED) {
                result.put(row.getBizKey(), row);
            }
        }
        return result;
    }

    /**
     * 读取启用中的功能开关配置。
     *
     * @return 功能标识 → 是否启用
     */
    public Map<String, Boolean> listFeatureStates() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (SysPolicyConfig row : sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_FEATURE, null)) {
            result.put(row.getBizKey(),
                    null != row.getStatus() && row.getStatus() == SysPolicyConfig.STATUS_ENABLED);
        }
        return result;
    }

    /**
     * 框架侧已注册的功能清单，供前端展示"可在线配置的功能点"。
     *
     * @return 功能标识列表
     */
    public List<String> listRegisteredFeatureIds() {
        List<String> ids = new ArrayList<>();
        ApiFeatureManager manager = manager();
        if (null == manager) {
            return ids;
        }
        manager.getAllFeatures().forEach(info -> ids.add(info.featureId()));
        return ids;
    }
}
