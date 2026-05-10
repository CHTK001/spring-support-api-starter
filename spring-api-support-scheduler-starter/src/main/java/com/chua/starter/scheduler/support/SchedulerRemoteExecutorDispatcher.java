package com.chua.starter.scheduler.support;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.job.support.JobNumberGenerator;
import com.chua.starter.job.support.entity.SysJob;
import com.chua.starter.job.support.remote.RemoteJobTriggerRequest;
import com.chua.starter.job.support.remote.RemoteJobTriggerResponse;
import com.chua.starter.job.support.scheduler.JobDispatchModeEnum;
import com.chua.starter.job.support.scheduler.TriggerTypeEnum;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.chua.starter.scheduler.support.executor.SchedulerExecutorResolveContext;
import com.chua.starter.scheduler.support.executor.SchedulerExecutorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 调度中心远程执行器分发器。
 * <p>
 * 当某个命名空间配置了执行器地址后，平台会通过这个组件把任务推送到远程业务服务，
 * 由业务服务里的 {@code job-starter} 接收并执行。当前实现提供：
 * </p>
 * <ul>
 *     <li>按命名空间维护执行器列表</li>
 *     <li>简单轮询 + 失败转移</li>
 *     <li>执行器访问令牌透传</li>
 *     <li>请求/响应与平台返回结构校验</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerRemoteExecutorDispatcher {

    private static final String ACCESS_TOKEN_HEADER = "X-Job-Access-Token";
    private static final String EXECUTE_CODE_PADDING = "PADDING";
    private static final String EXECUTE_CODE_SUCCESS = "SUCCESS";
    private static final String EXECUTE_CODE_FAILURE = "FAILURE";
    private static final ParameterizedTypeReference<ReturnResult<RemoteJobTriggerResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final SchedulerJobPlatformProperties platformProperties;
    private final ObjectProvider<RestTemplateBuilder> restTemplateBuilderProvider;
    private final ListableBeanFactory beanFactory;
    private final SchedulerNamespaceTableResolver namespaceTableResolver;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Map<String, AtomicInteger> namespaceCursor = new ConcurrentHashMap<>();

    /**
     * 当前命名空间是否配置了远程执行器。
     */
    public boolean hasExecutors(String namespace) {
        return !executors(namespace).isEmpty();
    }

    /**
     * 将任务推送到命名空间下的某个远程执行器。
     */
    public void dispatch(String namespace, SysJob job, String overrideParam) {
        dispatch(namespace, job, overrideParam, false);
    }

    /**
     * 将任务推送到命名空间下的某个远程执行器。
     *
     * @param clusterMode 集群模式下跳过日志入库
     */
    public void dispatch(String namespace, SysJob job, String overrideParam, boolean clusterMode) {
        List<SchedulerJobPlatformProperties.Executor> executors = resolveExecutors(namespace, job);
        if (executors.isEmpty()) {
            throw new IllegalStateException("未找到可用远程执行器: namespace=" + namespace + ", jobNo=" + trimToNull(job.getJobNo()));
        }
        validateJob(job);

        RemoteJobTriggerRequest request = createRequest(job, overrideParam);
        List<String> failures = new ArrayList<>();
        for (SchedulerJobPlatformProperties.Executor executor : orderedExecutors(namespace, executors)) {
            try {
                RemoteJobTriggerResponse response = invoke(executor, request);
                if (!clusterMode) {
                    persistRemoteExecutionLog(namespace, job, request, response, executor);
                }
                if (EXECUTE_CODE_SUCCESS.equalsIgnoreCase(response.getExecuteCode())) {
                    log.info("[Scheduler] 远程任务下发并执行成功: namespace={}, jobNo={}, jobId={}, handler={}, executor={}",
                            namespace, job.getJobNo(), job.getJobId(), job.getJobExecuteBean(), executorLabel(executor));
                } else if (EXECUTE_CODE_FAILURE.equalsIgnoreCase(response.getExecuteCode())) {
                    log.warn("[Scheduler] 远程任务执行失败: namespace={}, jobNo={}, jobId={}, handler={}, executor={}, message={}",
                            namespace, job.getJobNo(), job.getJobId(), job.getJobExecuteBean(), executorLabel(executor), response.getExecuteMessage());
                } else {
                    log.info("[Scheduler] 远程任务下发成功: namespace={}, jobNo={}, jobId={}, handler={}, executor={}, executeCode={}",
                            namespace, job.getJobNo(), job.getJobId(), job.getJobExecuteBean(), executorLabel(executor), response.getExecuteCode());
                }
                return;
            } catch (Exception e) {
                String reason = executorLabel(executor) + ": " + e.getMessage();
                failures.add(reason);
                log.warn("[Scheduler] 远程任务下发失败: namespace={}, jobNo={}, jobId={}, handler={}, executor={}, error={}",
                        namespace, job.getJobNo(), job.getJobId(), job.getJobExecuteBean(), executorLabel(executor), e.getMessage());
            }
        }

        if (!clusterMode) {
            persistDispatchFailureLog(namespace, job, request, failures);
        }
        throw new IllegalStateException("远程任务下发失败: namespace=" + namespace
                + ", jobNo=" + job.getJobNo()
                + ", jobId=" + job.getJobId()
                + ", failures=" + String.join(" | ", failures));
    }

    private RemoteJobTriggerResponse invoke(SchedulerJobPlatformProperties.Executor executor,
                                            RemoteJobTriggerRequest request) {
        RestTemplate restTemplate = buildRestTemplate(executor);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(executor.getAccessToken())) {
            headers.set(ACCESS_TOKEN_HEADER, executor.getAccessToken().trim());
        }

        URI uri = URI.create(normalizeBaseUrl(executor.getBaseUrl()) + normalizePath(executor.getDispatchPath()));
        HttpEntity<RemoteJobTriggerRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ReturnResult<RemoteJobTriggerResponse>> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                RESPONSE_TYPE
        );
        ReturnResult<RemoteJobTriggerResponse> body = responseEntity.getBody();
        if (body == null) {
            throw new IllegalStateException("执行器返回空响应");
        }
        if (body.isFailure()) {
            throw new IllegalStateException(messageOrDefault(body.getMsg(), "执行器返回失败"));
        }
        RemoteJobTriggerResponse data = body.getData();
        if (data == null) {
            throw new IllegalStateException("执行器返回结果为空");
        }
        if (!data.isAccepted()) {
            throw new IllegalStateException(messageOrDefault(data.getMessage(), "任务未进入远程执行队列"));
        }
        return data;
    }

    private void persistRemoteExecutionLog(String namespace,
                                           SysJob job,
                                           RemoteJobTriggerRequest request,
                                           RemoteJobTriggerResponse response,
                                           SchedulerJobPlatformProperties.Executor executor) {
        if (response == null || response.isLogHandledByExecutor()) {
            return;
        }

        String executeCode = normalizeExecuteCode(response.getExecuteCode());
        String message = messageOrDefault(response.getExecuteMessage(), response.getMessage());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getJobId());
        parameters.addValue("jobNo", trimToNull(job.getJobNo()));
        parameters.addValue("jobLogNo", trimToNull(response.getLogNo()) == null ? trimToNull(request.getLogNo()) : trimToNull(response.getLogNo()));
        parameters.addValue("jobLogApp", "scheduler");
        parameters.addValue("jobLogTriggerBean", job.getJobExecuteBean());
        parameters.addValue("jobLogTriggerType", normalizeTriggerType(request.getTriggerType()));
        parameters.addValue("jobLogTriggerTime", new Timestamp(resolveTriggerTime(request)));
        parameters.addValue("jobLogTriggerDate", resolveTriggerDate(request));
        parameters.addValue("jobLogTriggerCode", EXECUTE_CODE_FAILURE.equals(executeCode) ? EXECUTE_CODE_FAILURE : EXECUTE_CODE_SUCCESS);
        parameters.addValue("jobLogExecuteCode", executeCode);
        parameters.addValue("jobLogCost", response.getCostMs() == null ? BigDecimal.ZERO : BigDecimal.valueOf(Math.max(0L, response.getCostMs())));
        parameters.addValue("jobLogTriggerMsg", message);
        parameters.addValue("jobLogTriggerParam", trimToNull(request.getExecutorParams()));
        parameters.addValue("jobLogProfile", namespace);
        parameters.addValue("jobLogTriggerAddress", normalizeBaseUrl(executor.getBaseUrl()));
        parameters.addValue("jobLogFilePath", null);

        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        namedParameterJdbcTemplate.update(
                "INSERT INTO " + tables.logTable() + " ("
                        + "job_id, job_no, job_log_no, job_log_app, job_log_trigger_bean, job_log_trigger_type, job_log_trigger_time, "
                        + "job_log_trigger_date, job_log_trigger_code, job_log_execute_code, job_log_cost, "
                        + "job_log_trigger_msg, job_log_trigger_param, job_log_profile, job_log_trigger_address, job_log_file_path"
                        + ") VALUES ("
                        + ":jobId, :jobNo, :jobLogNo, :jobLogApp, :jobLogTriggerBean, :jobLogTriggerType, :jobLogTriggerTime, "
                        + ":jobLogTriggerDate, :jobLogTriggerCode, :jobLogExecuteCode, :jobLogCost, "
                        + ":jobLogTriggerMsg, :jobLogTriggerParam, :jobLogProfile, :jobLogTriggerAddress, :jobLogFilePath"
                        + ")",
                parameters
        );
    }

    private void persistDispatchFailureLog(String namespace,
                                           SysJob job,
                                           RemoteJobTriggerRequest request,
                                           List<String> failures) {
        SchedulerNamespaceTableResolver.ResolvedNamespaceTables tables = namespaceTableResolver.resolve(namespace);
        String message = "远程任务下发失败: " + String.join(" | ", failures);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobId", job.getJobId());
        parameters.addValue("jobNo", trimToNull(job.getJobNo()));
        parameters.addValue("jobLogNo", trimToNull(request.getLogNo()));
        parameters.addValue("jobLogApp", "scheduler");
        parameters.addValue("jobLogTriggerBean", job.getJobExecuteBean());
        parameters.addValue("jobLogTriggerType", normalizeTriggerType(request.getTriggerType()));
        parameters.addValue("jobLogTriggerTime", new Timestamp(resolveTriggerTime(request)));
        parameters.addValue("jobLogTriggerDate", resolveTriggerDate(request));
        parameters.addValue("jobLogTriggerCode", EXECUTE_CODE_FAILURE);
        parameters.addValue("jobLogExecuteCode", EXECUTE_CODE_FAILURE);
        parameters.addValue("jobLogCost", BigDecimal.ZERO);
        parameters.addValue("jobLogTriggerMsg", message);
        parameters.addValue("jobLogTriggerParam", trimToNull(request.getExecutorParams()));
        parameters.addValue("jobLogProfile", namespace);
        parameters.addValue("jobLogTriggerAddress", null);
        parameters.addValue("jobLogFilePath", null);
        namedParameterJdbcTemplate.update(
                "INSERT INTO " + tables.logTable() + " ("
                        + "job_id, job_no, job_log_no, job_log_app, job_log_trigger_bean, job_log_trigger_type, job_log_trigger_time, "
                        + "job_log_trigger_date, job_log_trigger_code, job_log_execute_code, job_log_cost, "
                        + "job_log_trigger_msg, job_log_trigger_param, job_log_profile, job_log_trigger_address, job_log_file_path"
                        + ") VALUES ("
                        + ":jobId, :jobNo, :jobLogNo, :jobLogApp, :jobLogTriggerBean, :jobLogTriggerType, :jobLogTriggerTime, "
                        + ":jobLogTriggerDate, :jobLogTriggerCode, :jobLogExecuteCode, :jobLogCost, "
                        + ":jobLogTriggerMsg, :jobLogTriggerParam, :jobLogProfile, :jobLogTriggerAddress, :jobLogFilePath"
                        + ")",
                parameters
        );
    }

    private RestTemplate buildRestTemplate(SchedulerJobPlatformProperties.Executor executor) {
        RestTemplateBuilder builder = restTemplateBuilderProvider.getIfAvailable(RestTemplateBuilder::new);
        return builder
                .setConnectTimeout(Duration.ofMillis(Math.max(1L, executor.getConnectTimeoutMs())))
                .setReadTimeout(Duration.ofMillis(Math.max(1L, executor.getReadTimeoutMs())))
                .build();
    }

    private RemoteJobTriggerRequest createRequest(SysJob job, String overrideParam) {
        RemoteJobTriggerRequest request = new RemoteJobTriggerRequest();
        request.setJobId(job.getJobId());
        request.setJobNo(trimToNull(job.getJobNo()));
        request.setJobName(job.getJobName());
        request.setExecutorHandler(job.getJobExecuteBean());
        request.setExecutorParams(StringUtils.hasText(overrideParam) ? overrideParam.trim() : trimToNull(job.getJobExecuteParam()));
        request.setExecutorTimeout(job.getJobExecuteTimeout());
        request.setTriggerType(TriggerTypeEnum.API.name());
        request.setLogDateTime(System.currentTimeMillis());
        request.setLogId(System.currentTimeMillis() + (job.getJobId() == null ? 0 : job.getJobId()));
        request.setLogNo(JobNumberGenerator.nextJobLogNo());
        request.setBroadcastIndex(0);
        request.setBroadcastTotal(1);
        request.setDispatchMode(JobDispatchModeEnum.match(job.getJobDispatchMode()).name());
        request.setRemoteExecutorAddress(trimToNull(job.getJobRemoteExecutorAddress()));
        return request;
    }

    private void validateJob(SysJob job) {
        if (job == null) {
            throw new IllegalArgumentException("远程下发的任务不能为空");
        }
        if (!StringUtils.hasText(job.getJobNo())) {
            throw new IllegalArgumentException("远程下发的任务编号不能为空");
        }
        if (!StringUtils.hasText(job.getJobExecuteBean())) {
            throw new IllegalArgumentException("远程下发要求执行 Bean 名称不能为空");
        }
        if (StringUtils.hasText(job.getJobGlueType()) && !"BEAN".equalsIgnoreCase(job.getJobGlueType())) {
            throw new IllegalArgumentException("远程下发暂只支持 BEAN 类型任务: " + job.getJobGlueType());
        }
    }

    public List<SchedulerJobPlatformProperties.Executor> resolveExecutors(String namespace, SysJob job) {
        SchedulerJobPlatformProperties.Namespace namespaceProperties = namespaceProperties(trimToNull(namespace));
        List<SchedulerJobPlatformProperties.Executor> jobOverrides = buildOverrideExecutors(job, namespaceProperties);
        if (!jobOverrides.isEmpty()) {
            return jobOverrides;
        }
        return executors(namespace, namespaceProperties);
    }

    private List<SchedulerJobPlatformProperties.Executor> buildOverrideExecutors(SysJob job,
                                                                                 SchedulerJobPlatformProperties.Namespace namespaceProperties) {
        String addressValue = job == null ? null : trimToNull(job.getJobRemoteExecutorAddress());
        if (!StringUtils.hasText(addressValue)) {
            return List.of();
        }

        SchedulerJobPlatformProperties.Executor template = overrideTemplate(namespaceProperties);
        List<SchedulerJobPlatformProperties.Executor> overrides = new ArrayList<>();
        for (String address : addressValue.split("[,;\\r\\n]+")) {
            String normalizedAddress = trimToNull(address);
            if (!StringUtils.hasText(normalizedAddress)) {
                continue;
            }
            SchedulerJobPlatformProperties.Executor executor = new SchedulerJobPlatformProperties.Executor();
            executor.setName(normalizedAddress);
            executor.setBaseUrl(normalizedAddress);
            if (template != null) {
                executor.setAccessToken(template.getAccessToken());
                executor.setDispatchPath(template.getDispatchPath());
                executor.setConnectTimeoutMs(template.getConnectTimeoutMs());
                executor.setReadTimeoutMs(template.getReadTimeoutMs());
            }
            overrides.add(executor);
        }
        return overrides;
    }

    private List<SchedulerJobPlatformProperties.Executor> executors(String namespace) {
        return executors(namespace, namespaceProperties(trimToNull(namespace)));
    }

    private List<SchedulerJobPlatformProperties.Executor> executors(String namespace,
                                                                    SchedulerJobPlatformProperties.Namespace namespaceProperties) {
        String normalizedNamespace = trimToNull(namespace);
        if (!StringUtils.hasText(normalizedNamespace) || platformProperties.getNamespaces() == null) {
            return List.of();
        }
        if (namespaceProperties == null) {
            return List.of();
        }

        SchedulerExecutorResolver resolver = executorResolver(namespaceProperties.getExecutorMode());
        if (resolver == null) {
            log.warn("[Scheduler] 未找到执行器来源 SPI: namespace={}, executorMode={}",
                    normalizedNamespace, trimToNull(namespaceProperties.getExecutorMode()));
            return List.of();
        }

        return resolver.resolve(new SchedulerExecutorResolveContext(normalizedNamespace, namespaceProperties, beanFactory));
    }

    private SchedulerJobPlatformProperties.Namespace namespaceProperties(String namespace) {
        if (!StringUtils.hasText(namespace) || platformProperties.getNamespaces() == null) {
            return null;
        }
        return platformProperties.getNamespaces().entrySet().stream()
                .filter(entry -> namespace.equalsIgnoreCase(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private SchedulerJobPlatformProperties.Executor overrideTemplate(SchedulerJobPlatformProperties.Namespace namespaceProperties) {
        if (namespaceProperties == null) {
            return null;
        }

        String executorMode = trimToNull(namespaceProperties.getExecutorMode());
        if ("DISCOVERY".equalsIgnoreCase(executorMode)) {
            SchedulerJobPlatformProperties.Discovery discoveryProperties = namespaceProperties.getDiscovery();
            SchedulerJobPlatformProperties.Executor executor = new SchedulerJobPlatformProperties.Executor();
            if (discoveryProperties != null) {
                executor.setAccessToken(trimToNull(discoveryProperties.getAccessToken()));
                executor.setDispatchPath(normalizePath(discoveryProperties.getDispatchPath()));
                executor.setConnectTimeoutMs(discoveryProperties.getConnectTimeoutMs());
                executor.setReadTimeoutMs(discoveryProperties.getReadTimeoutMs());
            }
            return executor;
        }

        return namespaceProperties.getExecutors().isEmpty() ? null : namespaceProperties.getExecutors().get(0);
    }

    private SchedulerExecutorResolver executorResolver(String executorMode) {
        String mode = StringUtils.hasText(executorMode) ? executorMode.trim() : "DISCOVERY";
        return ServiceProvider.of(SchedulerExecutorResolver.class).getNewExtension(mode);
    }

    private List<SchedulerJobPlatformProperties.Executor> orderedExecutors(String namespace,
                                                                           List<SchedulerJobPlatformProperties.Executor> executors) {
        if (executors.size() <= 1) {
            return executors;
        }

        int start = Math.floorMod(namespaceCursor
                .computeIfAbsent(namespace.toLowerCase(), key -> new AtomicInteger())
                .getAndIncrement(), executors.size());
        List<SchedulerJobPlatformProperties.Executor> ordered = new ArrayList<>(executors.size());
        for (int i = 0; i < executors.size(); i++) {
            ordered.add(executors.get((start + i) % executors.size()));
        }
        return ordered;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = trimToNull(baseUrl);
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("执行器地址不能为空");
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizePath(String path) {
        String normalized = trimToNull(path);
        if (!StringUtils.hasText(normalized)) {
            return "/v1/job-executor/dispatch";
        }
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private String executorLabel(SchedulerJobPlatformProperties.Executor executor) {
        String name = trimToNull(executor.getName());
        return StringUtils.hasText(name) ? name : normalizeBaseUrl(executor.getBaseUrl());
    }

    private String messageOrDefault(String message, String defaultMessage) {
        return StringUtils.hasText(message) ? message.trim() : defaultMessage;
    }

    private long resolveTriggerTime(RemoteJobTriggerRequest request) {
        return request.getLogDateTime() == null ? System.currentTimeMillis() : request.getLogDateTime();
    }

    private LocalDate resolveTriggerDate(RemoteJobTriggerRequest request) {
        return Instant.ofEpochMilli(resolveTriggerTime(request))
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String normalizeTriggerType(String triggerType) {
        String normalized = trimToNull(triggerType);
        return normalized == null ? TriggerTypeEnum.API.getName() : normalized;
    }

    private String normalizeExecuteCode(String executeCode) {
        String normalized = trimToNull(executeCode);
        if (!StringUtils.hasText(normalized)) {
            return EXECUTE_CODE_SUCCESS;
        }
        String upper = normalized.toUpperCase();
        if (EXECUTE_CODE_SUCCESS.equals(upper) || EXECUTE_CODE_FAILURE.equals(upper) || EXECUTE_CODE_PADDING.equals(upper)) {
            return upper;
        }
        return EXECUTE_CODE_FAILURE;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
