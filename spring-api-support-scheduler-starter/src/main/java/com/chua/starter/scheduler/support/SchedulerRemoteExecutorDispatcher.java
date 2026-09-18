package com.chua.starter.scheduler.support;

import com.chua.common.support.lang.json.Json;
import com.chua.starter.job.support.store.JobEntity;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 调度平台远程执行器分发器。
 * <p>
 * 将到期的远程任务通过 REST 调用推送到配置的执行器服务，
 * 由执行器侧 job-starter 的 EXECUTOR 角色消费长链并执行。
 * </p>
 *
 * @since 4.0.0
 * @author CH
 */
@Slf4j
@Component
public class SchedulerRemoteExecutorDispatcher {

    /**
     * 默认分发路径
     */
    private static final String DEFAULT_DISPATCH_PATH = "/v1/job-executor/dispatch";

    /**
     * 调度平台属性
     */
    private final SchedulerJobPlatformProperties platformProperties;

    /**
     * 构造分发器。
     *
     * @param platformProperties 调度平台属性
     */
    public SchedulerRemoteExecutorDispatcher(SchedulerJobPlatformProperties platformProperties) {
        this.platformProperties = platformProperties;
    }

    /**
     * 是否持有指定命名空间的远程执行器。
     *
     * @param namespace 命名空间
     * @return 持有执行器返回 true
     */
    public boolean hasExecutors(String namespace) {
        if (!platformProperties.isRemoteDispatchEnabled()) {
            return false;
        }
        SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
        if (nsProps == null) {
            return false;
        }
        return nsProps.getExecutors() != null && !nsProps.getExecutors().isEmpty();
    }

    /**
     * 将任务推送到命名空间配置的远程执行器。
     *
     * @param namespace   命名空间
     * @param job         任务
     * @param param       执行参数
     * @param clusterMode 是否集群模式
     */
    public void dispatch(String namespace, JobEntity job, String param, boolean clusterMode) {
        if (!hasExecutors(namespace)) {
            return;
        }
        SchedulerJobPlatformProperties.Namespace nsProps = resolveNamespaceProperties(namespace);
        if (nsProps == null) {
            return;
        }
        String payload = buildPayload(job, param, clusterMode);
        for (SchedulerJobPlatformProperties.Executor executor : nsProps.getExecutors()) {
            dispatchToExecutor(namespace, executor, payload);
        }
    }

    /**
     * 将任务推送到命名空间配置的远程执行器（兼容旧签名）。
     *
     * @param namespace 命名空间
     * @param job       任务
     * @param param     执行参数
     */
    public void dispatch(String namespace, JobEntity job, String param) {
        dispatch(namespace, job, param, false);
    }

    /**
     * 推送到单个执行器。
     *
     * @param namespace 命名空间
     * @param executor  执行器
     * @param payload   负载
     */
    private void dispatchToExecutor(String namespace,
                                    SchedulerJobPlatformProperties.Executor executor,
                                    String payload) {
        if (executor == null || !hasUsableBaseUrl(executor.getBaseUrl())) {
            return;
        }
        try {
            String url = buildUrl(executor);
            RestTemplate restTemplate = buildRestTemplate(executor);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (executor.getAccessToken() != null && !executor.getAccessToken().isBlank()) {
                headers.setBearerAuth(executor.getAccessToken().trim());
            }
            restTemplate.postForObject(url, new HttpEntity<>(payload, headers), String.class);
            log.info("[Scheduler] 远程任务已下发: namespace={}, executor={}",
                    namespace, executor.getName());
        } catch (Exception e) {
            log.warn("[Scheduler] 远程任务下发失败: namespace={}, executor={}, error={}",
                    namespace, executor.getName(), e.getMessage());
        }
    }

    /**
     * 构建推送负载。
     *
     * @param job         任务
     * @param param       执行参数
     * @param clusterMode 是否集群模式
     * @return JSON 负载
     */
    private String buildPayload(JobEntity job, String param, boolean clusterMode) {
        String executeParam = param != null ? param : job.getSysJobExecuteParam();
        Map<String, Object> payload = Map.of(
                "jobId", job.getSysJobId() == null ? 0L : job.getSysJobId(),
                "jobNo", job.getSysJobNo() == null ? "" : job.getSysJobNo(),
                "handler", job.getSysJobExecuteBean() == null ? "" : job.getSysJobExecuteBean(),
                "param", executeParam == null ? "" : executeParam,
                "triggerTime", System.currentTimeMillis(),
                "clusterMode", clusterMode);
        return Json.toJson(payload);
    }

    /**
     * 构建执行器请求 URL。
     *
     * @param executor 执行器
     * @return URL
     */
    private String buildUrl(SchedulerJobPlatformProperties.Executor executor) {
        String baseUrl = trimToNull(executor.getBaseUrl());
        String dispatchPath = trimToNull(executor.getDispatchPath());
        if (dispatchPath == null) {
            dispatchPath = DEFAULT_DISPATCH_PATH;
        }
        if (!dispatchPath.startsWith("/")) {
            dispatchPath = "/" + dispatchPath;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + dispatchPath;
    }

    /**
     * 构建 REST 模板。
     *
     * @param executor 执行器
     * @return REST 模板
     */
    private RestTemplate buildRestTemplate(SchedulerJobPlatformProperties.Executor executor) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Math.min(Integer.MAX_VALUE, executor.getConnectTimeoutMs()));
        factory.setReadTimeout((int) Math.min(Integer.MAX_VALUE, executor.getReadTimeoutMs()));
        return new RestTemplate(factory);
    }

    /**
     * 解析命名空间配置。
     *
     * @param namespace 命名空间
     * @return 命名空间配置，未配置返回 空
     */
    private SchedulerJobPlatformProperties.Namespace resolveNamespaceProperties(String namespace) {
        if (namespace == null || platformProperties.getNamespaces() == null) {
            return null;
        }
        return platformProperties.getNamespaces().entrySet().stream()
                .filter(e -> namespace.equalsIgnoreCase(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /**
     * 判断 URL 是否可用。
     *
     * @param url URL
     * @return 可用返回 true
     */
    private boolean hasUsableBaseUrl(String url) {
        return url != null && !url.trim().isEmpty();
    }

    /**
     * 去除空白，空白文本转换为 空。
     *
     * @param value 文本值
     * @return 去除空白后的文本，空白返回 空
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
