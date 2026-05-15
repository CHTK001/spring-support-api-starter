package com.chua.starter.spider.test.execution;

import com.chua.starter.spider.support.entity.SpiderFlowNode;
import com.chua.starter.spider.support.execution.SpiderNodeExecutionContext;
import com.chua.starter.spider.support.execution.SpiderNodeExecutionResult;
import com.chua.starter.spider.support.execution.SpiderNodeExecutor;
import com.chua.starter.spider.test.runtime.BrowserAgentResult;
import com.chua.starter.spider.test.runtime.BrowserAgentRuntime;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * BROWSER_AGENT 节点执行器
 * <p>AI 自主浏览器操作节点，支持 navigate / click / fill / extract 等操作，
 * 遇到验证码/登录等情况可交人工处理。</p>
 *
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserAgentSpiderNodeExecutor implements SpiderNodeExecutor {

    private final BrowserAgentRuntime browserAgentRuntime;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String spiderNodeType) {
        return "BROWSER_AGENT".equals(spiderNodeType);
    }

    @Override
    public SpiderNodeExecutionResult plan(SpiderNodeExecutionContext context,
                                           SpiderFlowNode flowNode,
                                           boolean isAiEnabled) {
        Map<String, Object> config = readNodeConfig(flowNode);
        String goal = stringValue(config.get("goal"), "");
        int maxSteps = intValue(config.get("maxSteps"), 15);
        String fallbackStrategy = stringValue(config.get("fallbackStrategy"), "AUTO_THEN_HUMAN");

        log.info("BROWSER_AGENT 开始执行: goal={}, maxSteps={}, fallbackStrategy={}",
                truncate(goal, 80), maxSteps, fallbackStrategy);

        // 执行 AI Agent 循环
        BrowserAgentResult result = browserAgentRuntime.execute(
                goal, maxSteps, fallbackStrategy, config);

        // 根据结果类型构建 SpiderNodeExecutionResult
        if (result.isSuccess()) {
            return SpiderNodeExecutionResult.builder()
                    .spiderStepStatus("SUCCESS")
                    .spiderNeedManualIntervene(0)
                    .spiderMessage(formatSuccessMessage(result))
                    .build();
        }

        if (result.isManualRequired()) {
            List<Map<String, Object>> formFields = result.getManualFormFields();
            if (formFields == null) formFields = List.of();

            return SpiderNodeExecutionResult.builder()
                    .spiderStepStatus("WAIT_MANUAL")
                    .spiderNeedManualIntervene(1)
                    .spiderTaskType(stringValue(result.getManualTaskType(), "BROWSER_AGENT_MANUAL"))
                    .spiderMessage(result.getMessage())
                    .spiderTaskGuide(stringValue(result.getManualTaskGuide(), ""))
                    .spiderFormFields(formFields)
                    .build();
        }

        // FAILED
        return SpiderNodeExecutionResult.builder()
                .spiderStepStatus("FAILED")
                .spiderNeedManualIntervene(0)
                .spiderMessage(result.getMessage())
                .build();
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> readNodeConfig(SpiderFlowNode flowNode) {
        Object config = flowNode.getSpiderNodeConfig();
        if (config instanceof Map) {
            return (Map<String, Object>) config;
        }
        if (config instanceof String && !((String) config).isBlank()) {
            try {
                return objectMapper.readValue((String) config,
                        new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析 nodeConfig JSON 失败: {}", e.getMessage());
            }
        }
        return Map.of();
    }

    private static String stringValue(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        return value.toString();
    }

    private static int intValue(Object value, int defaultValue) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() <= maxLen ? str : str.substring(0, maxLen) + "...";
    }

    private String formatSuccessMessage(BrowserAgentResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getMessage());
        if (result.getExtractedData() != null && !result.getExtractedData().isEmpty()) {
            sb.append(" | 提取了 ").append(result.getExtractedData().size()).append(" 个字段");
        }
        if (result.getActionHistory() != null) {
            sb.append(" | 执行了 ").append(result.getActionHistory().size()).append(" 步操作");
        }
        return sb.toString();
    }
}
