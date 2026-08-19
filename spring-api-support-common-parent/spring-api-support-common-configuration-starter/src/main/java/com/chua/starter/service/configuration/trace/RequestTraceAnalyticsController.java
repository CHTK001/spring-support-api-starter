package com.chua.starter.service.configuration.trace;

import com.chua.common.support.lang.code.ReturnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 请求追踪分析控制器。
 *
 * @since 2026/03/25
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/request-trace/analytics")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "org.springframework.web.bind.annotation.RestController",
        "com.chua.common.support.lang.code.ReturnResult"
})
public class RequestTraceAnalyticsController {

    /** 请求跟踪analytics服务 */
    private final RequestTraceAnalyticsService requestTraceAnalyticsService;
    /** 请求跟踪策略配置服务 */
    private final RequestTraceStrategyConfigService requestTraceStrategyConfigService;

    @GetMapping
    public ReturnResult<RequestTraceAnalyticsSnapshot> snapshot(
            @RequestParam(value = "minutes", required = false) Integer minutes) {
        try {
            return ReturnResult.ok(requestTraceAnalyticsService.snapshot(minutes));
        } catch (Exception ex) {
            log.error("获取请求追踪分析快照失败: minutes={}", minutes, ex);
            return ReturnResult.error("获取请求追踪分析快照失败: " + ex.getMessage());
        }
    }

    @GetMapping("/config")
    public ReturnResult<RequestTraceStrategyConfig> currentConfig() {
        try {
            return ReturnResult.ok(requestTraceStrategyConfigService.currentConfig());
        } catch (Exception ex) {
            log.error("获取请求追踪配置失败", ex);
            return ReturnResult.error("获取请求追踪配置失败: " + ex.getMessage());
        }
    }

    @PutMapping("/config")
    public ReturnResult<RequestTraceStrategyConfig> updateConfig(@RequestBody RequestTraceStrategyConfig config) {
        try {
            return ReturnResult.ok(requestTraceStrategyConfigService.update(config));
        } catch (Exception ex) {
            log.error("更新请求追踪配置失败", ex);
            return ReturnResult.error("更新请求追踪配置失败: " + ex.getMessage());
        }
    }

    @PostMapping("/config/refresh")
    public ReturnResult<RequestTraceStrategyConfig> refreshConfig() {
        try {
            return ReturnResult.ok(requestTraceStrategyConfigService.refresh());
        } catch (Exception ex) {
            log.error("刷新请求追踪配置失败", ex);
            return ReturnResult.error("刷新请求追踪配置失败: " + ex.getMessage());
        }
    }

    @GetMapping("/options")
    public ReturnResult<Map<String, Object>> options() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("configStorageType", requestTraceStrategyConfigService.configStorageType());
            data.put("configStorageTypes", requestTraceStrategyConfigService.configStorageTypes());
            data.put("rawStorageTypes", requestTraceStrategyConfigService.rawStorageTypes());
            data.put("behaviorStorageTypes", requestTraceStrategyConfigService.behaviorStorageTypes());
            data.put("signals", Arrays.stream(RequestTraceSignal.values())
                    .map(signal -> {
                        Map<String, String> item = new LinkedHashMap<>();
                        item.put("code", signal.getCode());
                        item.put("description", signal.getDescription());
                        return item;
                    })
                    .collect(Collectors.toList()));
            return ReturnResult.ok(data);
        } catch (Exception ex) {
            log.error("获取请求追踪选项失败", ex);
            return ReturnResult.error("获取请求追踪选项失败: " + ex.getMessage());
        }
    }
}
