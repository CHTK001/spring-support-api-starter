// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.annotations.Permission;
import com.chua.starter.common.support.annotations.SysLog;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.system.SysAccountBlackwhite;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.entity.system.SysRateLimitRule;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.hotreload.HotReloadConfigService;
import com.chua.starter.system.service.SysAccountBlackwhiteService;
import com.chua.starter.system.service.SysPolicyConfigService;
import com.chua.starter.system.service.SysRateLimitRuleService;
import com.chua.starter.system.support.ApiRouteExecutor;
import com.chua.starter.system.support.PolicyAnnotationBridge;
import com.chua.starter.system.support.PolicyHitCollector;
import com.chua.starter.system.support.RegisteredRouteIndex;
import com.chua.starter.system.support.SystemRateLimitManager;
import com.chua.starter.system.support.HeaderPolicyCacheProjector;
import com.chua.starter.system.support.HeaderRule;
import com.chua.starter.system.support.HeaderRulePresets;
import com.chua.starter.system.configuration.ApiRouteStartupInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略任务接口
 *
 * <p>限流规则、账号黑白名单、热更新配置的运行时管理。</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Tag(name = "策略任务接口")
@RestController
@RequestMapping("/v2/policy")
@RequiredArgsConstructor
@Slf4j
public class SysPolicyController {

    /**
     * 限流规则服务
     */
    private final SysRateLimitRuleService sysRateLimitRuleService;

    /**
     * 账号黑白名单服务
     */
    private final SysAccountBlackwhiteService sysAccountBlackwhiteService;

    /**
     * 系统设置服务
     */
    private final SysSettingService sysSettingService;

    /**
     * 热重载配置缓存服务
     */
    private final HotReloadConfigService hotReloadConfigService;

    /**
     * 全局限流管理器(common-system 内独立实现)
     */
    private final SystemRateLimitManager systemRateLimitManager;

    /**
     * 统一策略配置服务
     */
    private final SysPolicyConfigService sysPolicyConfigService;

    /**
     * 注解在线配置桥接器
     */
    private final PolicyAnnotationBridge policyAnnotationBridge;

    /**
     * 接口停用 / 转发执行器
     */
    private final ApiRouteExecutor apiRouteExecutor;

    /**
     * 路由索引 / 策略装载初始化器
     */
    private final ApiRouteStartupInitializer apiRouteStartupInitializer;

    /**
     * 消息头拦截规则投影器
     */
    private final HeaderPolicyCacheProjector headerPolicyCacheProjector;

    // ==================== 限流规则 ====================

    /**
     * 限流规则列表
     *
     * @param dimension 维度(可空)
     * @return 规则列表
     */
    @Operation(summary = "限流规则列表")
    @GetMapping("/ratelimit/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<SysRateLimitRule>> ratelimitList(@RequestParam(required = false) String dimension) {
        return ReturnResult.ok(sysRateLimitRuleService.listByDimension(dimension));
    }

    /**
     * 新增加限流规则
     *
     * @param entity 规则
     * @return 保存结果
     */
    @Operation(summary = "新增加限流规则")
    @PostMapping("/ratelimit/save")
    @SysLog
    @Permission(value = "sys:policy:ratelimit:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysRateLimitRule> ratelimitSave(@RequestBody SysRateLimitRule entity) {
        return sysRateLimitRuleService.saveRule(entity);
    }

    /**
     * 更新限流规则
     *
     * @param entity 规则
     * @return 更新结果
     */
    @Operation(summary = "更新限流规则")
    @PutMapping("/ratelimit/update")
    @SysLog
    @Permission(value = "sys:policy:ratelimit:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> ratelimitUpdate(@RequestBody SysRateLimitRule entity) {
        return sysRateLimitRuleService.updateRule(entity);
    }

    /**
     * 删除限流规则
     *
     * @param id 主键
     * @return 删除结果
     */
    @Operation(summary = "删除限流规则")
    @DeleteMapping("/ratelimit/delete")
    @SysLog
    @Permission(value = "sys:policy:ratelimit:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> ratelimitDelete(@RequestParam Long id) {
        return sysRateLimitRuleService.deleteRule(id);
    }

    /**
     * 全局限流状态
     *
     * @return 状态
     */
    @Operation(summary = "全局限流状态")
    @GetMapping("/ratelimit/global")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> ratelimitGlobal() {
        return ReturnResult.ok(systemRateLimitManager.getStatus());
    }

    /**
     * 应用全局限流配置
     *
     * @param body 配置 {enabled, qps, burstSize, dimension}
     * @return 结果
     */
    @Operation(summary = "应用全局限流配置")
    @PostMapping("/ratelimit/global/apply")
    @SysLog
    @Permission(value = "sys:policy:ratelimit:apply", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<String> ratelimitGlobalApply(@RequestBody Map<String, Object> body) {
        systemRateLimitManager.update(
                bool(body.get("enabled")),
                intVal(body.get("qps"), 100),
                intVal(body.get("burstSize"), 200),
                strVal(body.get("dimension"), "global"));
        return ReturnResult.ok("全局限流配置已应用");
    }

    // ==================== 账号黑白名单 ====================

    /**
     * 账号黑白名单分页
     *
     * @param query  分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @Operation(summary = "账号黑白名单分页")
    @GetMapping("/account-bw/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> accountBwPage(@ParameterObject Query<SysAccountBlackwhite> query,
                                                              @ParameterObject SysAccountBlackwhite entity) {
        com.baomidou.mybatisplus.core.metadata.IPage<SysAccountBlackwhite> page =
                sysAccountBlackwhiteService.page(query, entity);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNo", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("pages", page.getPages());
        return ReturnResult.ok(result);
    }

    /**
     * 新增账号黑白名单
     *
     * @param entity 实体
     * @return 新增结果
     */
    @Operation(summary = "新增账号黑白名单")
    @PostMapping("/account-bw/save")
    @SysLog
    @Permission(value = "sys:policy:account:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysAccountBlackwhite> accountBwSave(@RequestBody SysAccountBlackwhite entity) {
        return sysAccountBlackwhiteService.saveAccount(entity);
    }

    /**
     * 更新账号黑白名单
     *
     * @param entity 实体
     * @return 更新结果
     */
    @Operation(summary = "更新账号黑白名单")
    @PutMapping("/account-bw/update")
    @SysLog
    @Permission(value = "sys:policy:account:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> accountBwUpdate(@RequestBody SysAccountBlackwhite entity) {
        return sysAccountBlackwhiteService.updateAccount(entity);
    }

    /**
     * 删除账号黑白名单
     *
     * @param id 主键
     * @return 删除结果
     */
    @Operation(summary = "删除账号黑白名单")
    @DeleteMapping("/account-bw/delete")
    @SysLog
    @Permission(value = "sys:policy:account:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> accountBwDelete(@RequestParam Long id) {
        return sysAccountBlackwhiteService.deleteAccount(id);
    }

    // ==================== 热更新配置 ====================

    /**
     * 热更新配置列表
     *
     * @return 配置列表
     */
    @Operation(summary = "热更新配置列表")
    @GetMapping("/hotreload/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<SysSetting>> hotreloadList() {
        List<SysSetting> list = sysSettingService.list(
                Wrappers.<SysSetting>lambdaQuery()
                        .eq(SysSetting::getSysSettingGroup, SysSettingService.HOT_RELOAD_GROUP)
                        .orderByAsc(SysSetting::getSysSettingSort));
        return ReturnResult.ok(list);
    }

    /**
     * 保存热更新配置
     *
     * @param setting 配置
     * @return 保存结果
     */
    @Operation(summary = "保存热更新配置")
    @PostMapping("/hotreload/save")
    @SysLog
    @Permission(value = "sys:policy:hotreload:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> hotreloadSave(@RequestBody SysSetting setting) {
        return sysSettingService.saveHotReload(setting);
    }

    /**
     * 删除热更新配置。
     *
     * <p>其余 5 个策略页签都有删除入口，只有热更新配置此前只能建、只能改，
     * 配错了无法清理（2026-10-01 补齐）。服务层会限 {@code hot_reload} 分组
     * 并清掉该编码的值缓存，避免「删了仍按旧值生效」。</p>
     *
     * @param id 配置主键
     * @return 是否成功
     */
    @Operation(summary = "删除热更新配置")
    @DeleteMapping("/hotreload/delete")
    @SysLog
    @Permission(value = "sys:policy:hotreload:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> hotreloadDelete(@RequestParam Long id) {
        return sysSettingService.deleteHotReload(id);
    }

    /**
     * 主动热重载：清理全部热重载配置缓存，下一次业务访问按需回填。
     *
     * @return 重载结果
     */
    @Operation(summary = "热重载全部配置")
    @PostMapping("/hotreload/reload")
    @Permission(value = "sys:policy:hotreload:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> hotreloadReload() {
        hotReloadConfigService.evictAll();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reloaded", true);
        data.put("enabledCount", hotReloadConfigService.countEnabled());
        data.put("reloadTime", java.time.LocalDateTime.now().toString());
        return ReturnResult.ok(data);
    }

    // ==================== 统一策略配置（灰度/功能开关等） ====================

    /**
     * 按类型分页查询统一策略配置。
     *
     * @param type   策略类型(IP/RATE_LIMIT/ACCOUNT/GRAY/FEATURE)
     * @param query  分页参数
     * @param filter 过滤条件(bizKey/status)
     * @return 分页结果
     */
    @Operation(summary = "统一策略配置分页")
    @GetMapping("/config/page")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<com.baomidou.mybatisplus.core.metadata.IPage<SysPolicyConfig>> configPage(
            @RequestParam String type,
            @ParameterObject Query<SysPolicyConfig> query,
            @ParameterObject SysPolicyConfig filter) {
        return ReturnResult.ok(sysPolicyConfigService.pageByType(type, query, filter));
    }

    /**
     * 各类型配置条数统计。
     *
     * @return 类型 → 条数
     */
    @Operation(summary = "统一策略配置条数统计")
    @GetMapping("/config/count")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Long>> configCount() {
        return ReturnResult.ok(sysPolicyConfigService.countByType());
    }

    /**
     * 新增统一策略配置。
     *
     * @param config 配置
     * @return 保存后的行
     */
    @Operation(summary = "新增统一策略配置")
    @PostMapping("/config/save")
    @SysLog
    @Permission(value = "sys:policy:config:save", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<SysPolicyConfig> configSave(@RequestBody SysPolicyConfig config) {
        ReturnResult<SysPolicyConfig> result = sysPolicyConfigService.saveConfig(config);
        if (result.isSuccess()) {
            applyAnnotationBridge(config, result.getData());
        }
        return result;
    }

    /**
     * 更新统一策略配置。
     *
     * @param config 配置
     * @return 是否成功
     */
    @Operation(summary = "更新统一策略配置")
    @PutMapping("/config/update")
    @SysLog
    @Permission(value = "sys:policy:config:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> configUpdate(@RequestBody SysPolicyConfig config) {
        ReturnResult<Boolean> result = sysPolicyConfigService.updateConfig(config);
        if (result.isSuccess() && result.getData()) {
            applyAnnotationBridge(config, config);
        }
        return result;
    }

    /**
     * 删除统一策略配置。
     *
     * @param id 主键
     * @return 是否成功
     */
    @Operation(summary = "删除统一策略配置")
    @DeleteMapping("/config/delete")
    @SysLog
    @Permission(value = "sys:policy:config:delete", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> configDelete(@RequestParam Long id) {
        return sysPolicyConfigService.deleteConfig(id);
    }

    /**
     * 切换统一策略配置启用状态。
     *
     * @param id     主键
     * @param status 目标状态 1启用 0禁用
     * @return 是否成功
     */
    @Operation(summary = "切换统一策略配置启用状态")
    @PutMapping("/config/toggle")
    @SysLog
    @Permission(value = "sys:policy:config:update", role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Boolean> configToggle(@RequestParam Long id, @RequestParam Integer status) {
        ReturnResult<Boolean> result = sysPolicyConfigService.toggleStatus(id, status);
        if (result.isSuccess() && result.getData()) {
            sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_FEATURE, null).stream()
                    .filter(row -> row.getSysPolicyConfigId().equals(id))
                    .findFirst()
                    .ifPresent(row -> policyAnnotationBridge.applyFeature(row.getBizKey(),
                            status != null && status == SysPolicyConfig.STATUS_ENABLED));
        }
        return result;
    }

    // ==================== 注解在线配置 ====================

    /**
     * 功能开关在线配置列表（含框架侧已注册的功能点）。
     *
     * @return 功能标识 → 是否启用
     */
    @Operation(summary = "功能开关在线配置列表")
    @GetMapping("/feature/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Boolean>> featureList() {
        return ReturnResult.ok(policyAnnotationBridge.listFeatureStates());
    }

    /**
     * 框架侧已注册的功能点（可被在线配置的对象）。
     *
     * @return 功能标识列表
     */
    @Operation(summary = "可在线配置的功能点")
    @GetMapping("/feature/registered")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<String>> featureRegistered() {
        return ReturnResult.ok(policyAnnotationBridge.listRegisteredFeatureIds());
    }

    /**
     * 灰度配置列表（仅启用）。
     *
     * @return 业务键 → 灰度行
     */
    @Operation(summary = "灰度配置列表")
    @GetMapping("/gray/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, SysPolicyConfig>> grayList() {
        return ReturnResult.ok(policyAnnotationBridge.listEnabledGray());
    }

    // ==================== 接口停用 / 转发 ====================

    /**
     * 可作为转发目标的应用内已注册路由。
     *
     * <p>转发目标只能从这里选：目标路径由 Spring 已注册的 {@code @RequestMapping} 反查，
     * 配置面无法写入任意外部地址。</p>
     *
     * @param keyword 路径关键字过滤，可空
     * @return 已注册路由列表
     */
    @Operation(summary = "可转发的已注册路由")
    @GetMapping("/api-route/targets")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<RegisteredRouteIndex.RegisteredRoute>> apiRouteTargets(
            @RequestParam(required = false) String keyword) {
        List<RegisteredRouteIndex.RegisteredRoute> all = RegisteredRouteIndex.list();
        if (StringUtils.isBlank(keyword)) {
            return ReturnResult.ok(all);
        }
        return ReturnResult.ok(all.stream()
                .filter(r -> r.path().contains(keyword))
                .toList());
    }

    /**
     * 重新采集已注册路由并重载停用/转发策略。
     *
     * @return 路由条数与已装载策略条数
     */
    @Operation(summary = "重载接口停用/转发")
    @PostMapping("/api-route/refresh")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> apiRouteRefresh() {
        int routeCount = apiRouteStartupInitializer.refresh();
        int policyCount = 0;
        List<SysPolicyConfig> rows = sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_API_ROUTE, null);
        if (null != rows) {
            policyCount = rows.size();
        }
        return ReturnResult.ok(Map.of("routeCount", routeCount, "policyCount", policyCount));
    }

    // ==================== 消息头拦截 ====================

    /**
     * 消息头拦截规则列表（含已解析的规则体，便于前端直接展示与编辑）
     *
     * @return 规则行列表
     */
    @Operation(summary = "消息头拦截规则列表")
    @GetMapping("/header/list")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<List<Map<String, Object>>> headerList() {
        List<SysPolicyConfig> rows = sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_HEADER, null);
        List<Map<String, Object>> out = new java.util.ArrayList<>();
        if (null == rows) {
            return ReturnResult.ok(out);
        }
        for (SysPolicyConfig row : rows) {
            HeaderRule rule = HeaderRule.parse(row.getBizKey(), row.getConfig());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sysPolicyConfigId", row.getSysPolicyConfigId());
            item.put("headerName", row.getBizKey());
            item.put("status", row.getStatus());
            item.put("remark", row.getRemark());
            item.put("updateTime", row.getUpdateTime());
            item.put("parsed", Boolean.TRUE.equals(null != rule));
            if (null != rule) {
                item.put("matchType", rule.matchType());
                item.put("patterns", rule.patterns());
                item.put("action", rule.action());
                item.put("blockStatus", rule.status());
                item.put("message", rule.message());
            }
            out.add(item);
        }
        return ReturnResult.ok(out);
    }

    /**
     * 爬虫 / 自动化客户端的 UA 特征预设
     *
     * <p>「一键填入」用：管理员点一下就有内容，再自己删不要的，
     * 避免从零写正则写成过宽规则把正常流量也拦掉。</p>
     *
     * @return 分组名 → 特征列表，以及展平后的全量列表
     */
    @Operation(summary = "爬虫/自动化 UA 特征预设")
    @GetMapping("/header/presets")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> headerPresets() {
        return ReturnResult.ok(Map.of("groups", HeaderRulePresets.all(), "flat", HeaderRulePresets.flat()));
    }

    /**
     * 重载全部消息头拦截规则
     *
     * <p>正常情况下保存/启停/删除即生效（事务提交后投影），这里用于
     * 「改了库里的数据但缓存没跟上」这类手工改库后的补救。</p>
     *
     * @return 已装载的规则条数
     */
    @Operation(summary = "重载消息头拦截规则")
    @PostMapping("/header/refresh")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> headerRefresh() {
        List<SysPolicyConfig> rows = sysPolicyConfigService.listByType(SysPolicyConfig.TYPE_HEADER, null);
        headerPolicyCacheProjector.rebuild(rows);
        return ReturnResult.ok(Map.of("policyCount", null == rows ? 0 : rows.size()));
    }

    // ==================== 策略命中统计 ====================

    /**
     * 全部策略的命中次数。
     *
     * <p>供列表页一次性取回「命中次数」列，避免每行发一次请求。返回的键是
     * {@code 类型|策略键}（见 {@link PolicyHitCollector#compositeKey}），前端按
     * 自己那一行的类型与键去取即可。</p>
     *
     * <p>只统计策略<b>真的生效</b>的次数：黑名单命中=被拦、限流命中=被拒、
     * 接口停用/转发=规则触发。放行的行恒为 0，这是正确结果而非缺数据。</p>
     *
     * @return 键为 {@code type|key}、值为累计命中次数
     */
    @Operation(summary = "策略命中次数汇总")
    @GetMapping("/hit/counts")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Long>> hitCounts() {
        return ReturnResult.ok(PolicyHitCollector.getInstance().counts());
    }

    /**
     * 单条策略的命中明细。
     *
     * <p>含累计次数、首末命中时间、来源 IP 排行（谁在打）与分钟级趋势（什么时候打的），
     * 前端据此画 Top 榜与折线图。</p>
     *
     * @param type 策略类型，取 {@code PolicyHitCollector.TYPE_*}
     * @param key  策略键：名单项、{@code 维度:目标} 或接口路径
     * @return 命中明细；无数据时返回零值结构而非 null
     */
    @Operation(summary = "单条策略命中明细")
    @GetMapping("/hit/detail")
    @Permission(role = {AuthConstant.ADMIN, AuthConstant.SUPER_ADMIN})
    public ReturnResult<Map<String, Object>> hitDetail(@RequestParam String type, @RequestParam String key) {
        return ReturnResult.ok(PolicyHitCollector.getInstance().detail(type, key));
    }

    // ==================== 反射工具 ====================

    /**
     * 保存/更新后把注解类配置同步到运行时。
     *
     * @param submitted 提交内容
     * @param effective 实际生效的行（新增时为回填主键的行）
     */
    private void applyAnnotationBridge(SysPolicyConfig submitted, SysPolicyConfig effective) {
        if (null == effective || !SysPolicyConfig.TYPE_FEATURE.equals(effective.getType())) {
            return;
        }
        policyAnnotationBridge.applyFeature(effective.getBizKey(),
                null == effective.getStatus() || effective.getStatus() == SysPolicyConfig.STATUS_ENABLED);
    }


    /**
     * bool
     *
     * @param v 原值
     * @return boolean
     */
    private boolean bool(Object v) {
        if (v instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }

    /**
     * int
     *
     * @param v            原值
     * @param defaultValue 默认
     * @return int
     */
    private int intVal(Object v, int defaultValue) {
        try {
            if (v instanceof Number n) {
                return n.intValue();
            }
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * str
     *
     * @param v            原值
     * @param defaultValue 默认
     * @return string
     */
    private String strVal(Object v, String defaultValue) {
        if (v == null) {
            return defaultValue;
        }
        String s = String.valueOf(v);
        return s.isBlank() ? defaultValue : s;
    }
}