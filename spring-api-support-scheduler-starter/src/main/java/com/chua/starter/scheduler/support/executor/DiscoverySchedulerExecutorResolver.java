package com.chua.starter.scheduler.support.executor;

import com.chua.common.support.network.discovery.Discovery;
import com.chua.common.support.reflection.ReflectUtils;
import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.spi.annotations.SpiCondition;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
* Discovery 执行器解析器。
* <p>
* 这里不直接依赖 discovery-starter 类型签名，避免缺少该模块时影响调度中心启动。
* </p>
* @author CH
* @since 4.0.0
* @param beanFactory Bean工厂
* @return discovery服务的结果
 */
@Slf4j
@Spi({"DISCOVERY", "discovery"})
@SpiCondition("com.chua.starter.discovery.support.service.DiscoveryService")
public class DiscoverySchedulerExecutorResolver implements SchedulerExecutorResolver {

    private static final String DISCOVERY_SERVICE_CLASS = "com.chua.starter.discovery.support.service.DiscoveryService"; // discovery服务类
    private static final long DEFAULT_CONNECT_TIMEOUT_MS = 3000L; // 默认连接超时ms
    private static final long DEFAULT_READ_TIMEOUT_MS = 10000L; // 默认读取超时ms
    private static final long DISCOVERY_CACHE_TTL_SECONDS = 30L; // discovery缓存ttlseconds

    private final Cache<String, List<SchedulerJobPlatformProperties.Executor>> discoveryCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(DISCOVERY_CACHE_TTL_SECONDS, TimeUnit.SECONDS)
                    .build();
/**
* resolve。
* @param context 上下文
* @return resolve的结果
 */

    @Override
    public List<SchedulerJobPlatformProperties.Executor> resolve(SchedulerExecutorResolveContext context) {
        if (context == null || context.namespaceProperties() == null) {
            return List.of();
        }

        SchedulerJobPlatformProperties.Discovery discoveryProperties = context.namespaceProperties().getDiscovery();
        if (discoveryProperties != null && !discoveryProperties.isEnabled()) {
            return List.of();
        }

        Object discoveryService = discoveryService(context.beanFactory());
        if (discoveryService == null) {
            return List.of();
        }

        String discoveryNamespace = resolveDiscoveryNamespace(context.namespace(), discoveryProperties);
        if (!StringUtils.hasText(discoveryNamespace)) {
            return List.of();
        }

        String cacheKey = context.namespace() + "|" + discoveryNamespace;
        try {
            return discoveryCache.get(cacheKey, () -> {
                Set<Discovery> discoveries = loadDiscoveries(discoveryService, discoveryProperties, discoveryNamespace);
                if (discoveries == null || discoveries.isEmpty()) {
                    return List.of();
                }
                return discoveries.stream()
                        .map(discovery -> toExecutor(discovery, discoveryProperties))
                        .filter(this::hasUsableBaseUrl)
                        .toList();
            });
        } catch (Exception e) {
            log.warn("[DiscoveryResolver] 从缓存获取发现服务实例失败，降级为实时查询: namespace={}, error={}",
                    context.namespace(), e.getMessage());
            Set<Discovery> discoveries = loadDiscoveries(discoveryService, discoveryProperties, discoveryNamespace);
            if (discoveries == null || discoveries.isEmpty()) {
                return List.of();
            }
            return discoveries.stream()
                    .map(discovery -> toExecutor(discovery, discoveryProperties))
                    .filter(this::hasUsableBaseUrl)
                    .toList();
        }
    }

    /**
     * discovery服务。
     *
     * @param beanFactory bean工厂，不允许为 null
     * @return 对象 对象
     */
    private Object discoveryService(ListableBeanFactory beanFactory) {
        if (beanFactory == null) {
            return null;
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = DiscoverySchedulerExecutorResolver.class.getClassLoader();
        }
        if (!org.springframework.util.ClassUtils.isPresent(DISCOVERY_SERVICE_CLASS, classLoader)) {
            return null;
        }

        try {
            Class<?> discoveryServiceType = org.springframework.util.ClassUtils.forName(DISCOVERY_SERVICE_CLASS, classLoader);
            Map<String, ?> beans = beanFactory.getBeansOfType(discoveryServiceType);
            if (beans.isEmpty()) {
                return null;
            }
            return beans.values().iterator().next();
        } catch (Exception e) {
            return null;
        }
    }

    /**
    * 通过反射加载 Discovery 实例列表。
    * 使用反射是因为 discovery服务 来自外部模块，此处仅调用公共方法，无标准替代方案。
    * @param value 值
    * @return 修剪转为空的结果
     */
    @SuppressWarnings({"unchecked", "java:S1186"})
    private Set<Discovery> loadDiscoveries(Object discoveryService,
                                           SchedulerJobPlatformProperties.Discovery discoveryProperties,
                                           String discoveryNamespace) {
        try {
            String discoveryProtocol = discoveryProperties == null ? null : trimToNull(discoveryProperties.getProtocol());
 // 服务实例来自外部模块，方法调用统一经 ReflectUtils（getDiscoveryAll 返回 Set<Discovery>）
            Object result;
            if (StringUtils.hasText(discoveryProtocol)) {
                result = ReflectUtils.invoke(discoveryService, "getDiscoveryAll", Set.class,
                        new Class<?>[]{String.class, String.class}, discoveryProtocol, discoveryNamespace);
            } else {
                result = ReflectUtils.invoke(discoveryService, "getDiscoveryAll", Set.class,
                        new Class<?>[]{String.class}, discoveryNamespace);
            }
            if (result == null) {
                return Set.of();
            }
            return (Set<Discovery>) result;
        } catch (Exception e) {
            return Set.of();
        }
    }

    private SchedulerJobPlatformProperties.Executor toExecutor(Discovery discovery,
                                                               SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        SchedulerJobPlatformProperties.Executor executor = new SchedulerJobPlatformProperties.Executor();
        executor.setName(resolveDiscoveryExecutorName(discovery));
        executor.setBaseUrl(buildDiscoveryBaseUrl(discovery, discoveryProperties));
        executor.setAccessToken(resolveDiscoveryAccessToken(discovery, discoveryProperties));
        executor.setDispatchPath(resolveDiscoveryDispatchPath(discovery, discoveryProperties));
        executor.setConnectTimeoutMs(discoveryProperties == null ? DEFAULT_CONNECT_TIMEOUT_MS : discoveryProperties.getConnectTimeoutMs());
        executor.setReadTimeoutMs(discoveryProperties == null ? DEFAULT_READ_TIMEOUT_MS : discoveryProperties.getReadTimeoutMs());
        return executor;
    /**
    * resolvediscovery执行器名称。
    * @param discovery discovery
    * @return resolvediscovery执行器名称的结果
     */
    }

    /**
     * 解析DiscoveryExecutor名称。
     *
     * @param discovery 方法入参 discovery
     * @return 结果字符串
     */
    private String resolveDiscoveryExecutorName(Discovery discovery) {
        if (discovery == null) {
            return null;
        }
        String serverId = trimToNull(discovery.getServerId());
        if (StringUtils.hasText(serverId)) {
            return serverId;
        }
        return trimToNull(discovery.getId());
    }

    private String buildDiscoveryBaseUrl(Discovery discovery,
                                         SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        if (discovery == null || !StringUtils.hasText(discovery.getHost()) || discovery.getPort() <= 0) {
            return null;
        }

        String protocol = StringUtils.hasText(discovery.getProtocol()) ? discovery.getProtocol().trim() : "http";
        String basePath = resolveDiscoveryBasePath(discovery, discoveryProperties);
        return protocol + "://" + discovery.getHost() + ":" + discovery.getPort() + normalizePath(basePath);
    }

    private String resolveDiscoveryBasePath(Discovery discovery,
                                            SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        String configuredBasePath = discoveryProperties == null ? null : trimToNull(discoveryProperties.getBasePath());
        if (StringUtils.hasText(configuredBasePath)) {
            return configuredBasePath;
        }
        if (discovery != null && discovery.getMetadata() != null) {
            Object contextPath = discovery.getMetadata().get("contextPath");
            if (contextPath != null && StringUtils.hasText(contextPath.toString())) {
                return contextPath.toString().trim();
            }
        }
        return "";
    }

    private String resolveDiscoveryAccessToken(Discovery discovery,
                                               SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        if (discoveryProperties != null && StringUtils.hasText(discoveryProperties.getAccessToken())) {
            return discoveryProperties.getAccessToken().trim();
        }
        if (discovery != null && discovery.getMetadata() != null) {
            Object accessToken = discovery.getMetadata().get("accessToken");
            if (accessToken != null && StringUtils.hasText(accessToken.toString())) {
                return accessToken.toString().trim();
            }
        }
        return null;
    }

    private String resolveDiscoveryDispatchPath(Discovery discovery,
                                                SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        if (discoveryProperties != null && StringUtils.hasText(discoveryProperties.getDispatchPath())) {
            return normalizePath(discoveryProperties.getDispatchPath());
        }
        if (discovery != null && discovery.getMetadata() != null) {
            Object dispatchPath = discovery.getMetadata().get("dispatchPath");
            if (dispatchPath != null && StringUtils.hasText(dispatchPath.toString())) {
                return normalizePath(dispatchPath.toString());
            }
        }
        return "/v1/job-executor/dispatch";
    }

    private String resolveDiscoveryNamespace(String namespace,
                                             SchedulerJobPlatformProperties.Discovery discoveryProperties) {
        String configuredNamespace = discoveryProperties == null ? null : trimToNull(discoveryProperties.getNamespace());
        if (StringUtils.hasText(configuredNamespace)) {
            return configuredNamespace;
        }
        return StringUtils.hasText(namespace) ? "/job-executor/" + namespace.trim() : null;
    /**
    * 是否包含usablebaseurl。
    * @param executor 执行器
    * @return 是否包含usablebaseurl的结果
     */
    }

    /**
     * 是否含有UsableBaseURL。
     *
     * @param executor 方法入参 executor
     * @return 是否成功（true 表示成功）
     */
    private boolean hasUsableBaseUrl(SchedulerJobPlatformProperties.Executor executor) {
        return executor != null
                && executor.getBaseUrl() != null
                && !executor.getBaseUrl().trim().isEmpty();
    /**
    * normalize路径。
    * @param path 路径
    * @return normalize路径的结果
     */
    }

    /**
     * normalize路径。
     *
     * @param path 路径，不允许为 null
     * @return 结果字符串
     */
    private String normalizePath(String path) {
        String value = trimToNull(path);
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    /**
     * trim转为Null。
     *
     * @param value 值，不允许为 null
     * @return 结果字符串
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
