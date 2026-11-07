// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.monitor.agent;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.spi.ServiceProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统指标数据源路由与自动探测。
 *
 * <p>职责：</p>
 * <ul>
 *   <li>通过 {@code ServiceProvider.of(SystemMetricProvider.class)} 收集 SPI 实现
 *       （{@code none} / {@code hotsport} / {@code skywalker}），并合并 Spring 容器
 *       中的同名 Bean，保证两种加载方式都可用；</li>
 *   <li>按 {@code system.metric.provider} 选择数据源：{@code auto} 按
 *       hotsport → skywalker → none 探测，首个可达者生效并缓存；显式模式可直接指定；</li>
 *   <li>转发失败时在自动模式下立即重探测并回退（最终 none），避免后端临时抖动打穿页面。</li>
 * </ul>
 *
 * <p>实例由 {@code SystemMetricAutoConfiguration} 显式声明为 Bean，这里不标
 * {@code @Component}，避免与 {@code @Bean} 同名冲突，也避免宿主整体扫描本包时重复注册。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class SystemMetricRegistry {

    /** 自动模式下稳定的候选顺序 */
    private static final List<String> AUTO_CANDIDATES = List.of("hotsport", "skywalker", "none");

    private final SystemMetricProperties properties;
    private final ApplicationContext applicationContext;

    /** spi + spring 实例缓存，key 为 provider.name() 小写 */
    private final Map<String, SystemMetricProvider> providers = new ConcurrentHashMap<>();

    /** 决策缓存（显式模式与自动模式共用同一 TTL，避免每次请求都去探测） */
    private volatile long lastDecideAt = 0;
    private volatile ProviderDecision cachedDecision =
            ProviderDecision.unavailable("none", "尚未初始化");

    /**
     * 获取当前数据源决策（带缓存）。
     *
     * @return 名称 / 可用性 / 理由
     */
    public ProviderDecision decide() {
        long cacheMs = Math.max(0, (long) properties.getCacheSeconds() * 1000);
        if (System.currentTimeMillis() - lastDecideAt < cacheMs) {
            return cachedDecision;
        }
        synchronized (this) {
            if (System.currentTimeMillis() - lastDecideAt < cacheMs) {
                return cachedDecision;
            }
            ProviderDecision next = decideNoCache();
            cachedDecision = next;
            lastDecideAt = System.currentTimeMillis();
            return next;
        }
    }

    /**
     * 处理一次接口调用；自动模式下失败会强制重探测并回退一次。
     *
     * @param request 接口请求
     * @return 响应对象
     */
    public SystemMetricResponse forward(SystemMetricRequest request) {
        ProviderDecision decision = decide();
        SystemMetricProvider provider = provider(decision.getName());
        if (provider == null) {
            return error(502, "系统指标数据源不可用: " + decision.getReason());
        }
        try {
            return provider.forward(request, properties);
        } catch (Exception e) {
            log.warn("[system-metric] 数据源 {} 转发失败: {}", decision.getName(), e.getMessage());
            if (isAutoMode()) {
                // 立即失效缓存并重探测，换一个可用数据源再试一次
                lastDecideAt = 0;
                ProviderDecision next = decide();
                if (!Objects.equals(next.getName(), decision.getName())
                        && !"none".equalsIgnoreCase(next.getName())) {
                    SystemMetricProvider fallback = provider(next.getName());
                    if (fallback != null) {
                        try {
                            return fallback.forward(request, properties);
                        } catch (Exception fallbackError) {
                            log.warn("[system-metric] 回退数据源 {} 也失败: {}",
                                    next.getName(), fallbackError.getMessage());
                        }
                    }
                }
            }
            return error(502, decision.getName() + " 数据源不可用: " + e.getMessage());
        }
    }

    /**
     * 数据源状态（含候选探测结果），供前端徽标展示。
     *
     * @return 状态对象
     */
    public ProviderInfo providerInfo() {
        ProviderDecision decision = decide();
        List<Candidate> candidates = new ArrayList<>();
        for (String name : AUTO_CANDIDATES) {
            SystemMetricProvider provider = provider(name);
            boolean available;
            long latency;
            String description;
            if (provider == null) {
                available = "none".equals(name);
                latency = 0;
                description = "未安装实现";
            } else {
                long start = System.nanoTime();
                available = probe(provider);
                latency = (System.nanoTime() - start) / 1_000_000L;
                description = provider.description();
            }
            candidates.add(new Candidate(name, available, latency, description));
        }

        ProviderInfo info = new ProviderInfo();
        info.setMode(decision.getName());
        info.setAvailable("none".equals(decision.getName()) ? false : decision.isAvailable());
        info.setReason(decision.getReason());
        info.setCandidates(candidates);
        return info;
    }

    // ---- 内部实现 ----

    /** isAutoMode 方法 */
    private boolean isAutoMode() {
        return "auto".equalsIgnoreCase(String.valueOf(properties.getProvider()));
    }

    /** 无缓存决策：显式模式按配置探测，自动模式按候选顺序探测 */
    private ProviderDecision decideNoCache() {
        String configured = String.valueOf(properties.getProvider()).trim().toLowerCase(Locale.ROOT);
        if ("none".equals(configured)) {
            return ProviderDecision.available("none", "未启用观测后端");
        }
        if (!"auto".equals(configured)) {
            SystemMetricProvider provider = provider(configured);
            if (provider == null) {
                return ProviderDecision.unavailable("none", "未找到数据源: " + configured);
            }
            if (probe(provider)) {
                return ProviderDecision.available(provider.name(),
                        "显式配置 " + configured + " 探测成功");
            }
            return ProviderDecision.unavailable("none", "数据源 " + configured + " 探测失败");
        }

        for (String name : AUTO_CANDIDATES) {
            SystemMetricProvider provider = provider(name);
            if (provider == null) {
                continue;
            }
            if (probe(provider)) {
                return ProviderDecision.available(name, "自动探测 " + name + " 成功");
            }
        }
        return ProviderDecision.unavailable("none", "未检测到可用的观测后端");
    }

    /** 懒加载并缓存数据源实例 */
    private SystemMetricProvider provider(String name) {
        String key = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (providers.isEmpty()) {
            initProviders();
        }
        return providers.get(key);
    }

    private synchronized void initProviders() {
        if (!providers.isEmpty()) {
            return;
        }
        // SPI：同包扫描 + META-INF/services + Spring 解析器
        try {
            ServiceProvider<SystemMetricProvider> spi =
                    ServiceProvider.of(SystemMetricProvider.class);
            for (SystemMetricProvider provider : spi.collect()) {
                providers.putIfAbsent(provider.name().toLowerCase(Locale.ROOT), provider);
            }
        } catch (Exception e) {
            log.warn("[system-metric] SPI 收集失败: {}", e.getMessage());
        }
        // Spring Bean 兜底（显式注册的实现无 SPI 也能被识别）
        try {
            applicationContext.getBeansOfType(SystemMetricProvider.class)
                    .values()
                    .forEach(p -> providers.putIfAbsent(p.name().toLowerCase(Locale.ROOT), p));
        } catch (Exception e) {
            log.warn("[system-metric] Spring 实现收集失败: {}", e.getMessage());
        }
        log.info("[system-metric] 可用数据源: {}", providers.keySet());
    }

    /** probe 方法 */
    private boolean probe(SystemMetricProvider provider) {
        try {
            return provider.probe(properties);
        } catch (Exception e) {
            log.debug("[system-metric] 探测异常 {}: {}", provider.name(), e.getMessage());
            return false;
        }
    }

    /** 标准错误响应 */
    private SystemMetricResponse error(int status, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status);
        body.put("msg", msg);
        body.put("data", null);
        return SystemMetricResponse.of(status, "application/json;charset=UTF-8",
                Json.toJson(body).getBytes(StandardCharsets.UTF_8));
    }

    /** 当前决策 */
    @Data
    public static class ProviderDecision {
        private String name;
        private boolean available;
        private String reason;

        public static ProviderDecision available(String name, String reason) {
            ProviderDecision decision = new ProviderDecision();
            decision.setName(name);
            decision.setAvailable(true);
            decision.setReason(reason);
            return decision;
        }

        public static ProviderDecision unavailable(String name, String reason) {
            ProviderDecision decision = new ProviderDecision();
            decision.setName(name);
            decision.setAvailable(false);
            decision.setReason(reason);
            return decision;
        }
    }

    /** /provider 返回的候选数据源 */
    @Data
    public static class Candidate {
        private String name;
        private boolean available;
        private long latencyMs;
        private String description;

        public Candidate(String name, boolean available, long latencyMs, String description) {
            this.name = name;
            this.available = available;
            this.latencyMs = latencyMs;
            this.description = description;
        }
    }

    /** /provider 返回数据 */
    @Data
    public static class ProviderInfo {
        private String mode;
        private boolean available;
        private String reason;
        private List<Candidate> candidates;
    }
}