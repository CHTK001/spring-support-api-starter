package com.chua.starter.spider.test.runtime;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * BROWSER_AGENT 节点执行结果
 */
@Data
public class BrowserAgentResult {
    /** SUCCESS / WAIT_MANUAL / FAILED */
    private String status;
    private String message;
    private Map<String, Object> extractedData;
    private List<Map<String, Object>> actionHistory;

    /** manual_required 时有效 */
    private String manualReason;
    private String manualTaskType;
    private String manualTaskGuide;
    private List<Map<String, Object>> manualFormFields;

    public static BrowserAgentResult success(String message, Map<String, Object> extractedData,
                                              List<Map<String, Object>> actionHistory) {
        BrowserAgentResult r = new BrowserAgentResult();
        r.status = "SUCCESS";
        r.message = message;
        r.extractedData = extractedData;
        r.actionHistory = actionHistory;
        return r;
    }

    public static BrowserAgentResult manualRequired(String reason, Map<String, Object> extractedData,
                                                     List<Map<String, Object>> actionHistory,
                                                     String taskType, String taskGuide,
                                                     List<Map<String, Object>> formFields) {
        BrowserAgentResult r = new BrowserAgentResult();
        r.status = "WAIT_MANUAL";
        r.message = reason;
        r.extractedData = extractedData;
        r.actionHistory = actionHistory;
        r.manualReason = reason;
        r.manualTaskType = taskType;
        r.manualTaskGuide = taskGuide;
        r.manualFormFields = formFields;
        return r;
    }

    public static BrowserAgentResult failed(String message) {
        return failed(message, null, null);
    }

    public static BrowserAgentResult failed(String message, Map<String, Object> extractedData,
                                             List<Map<String, Object>> actionHistory) {
        BrowserAgentResult r = new BrowserAgentResult();
        r.status = "FAILED";
        r.message = message;
        r.extractedData = extractedData;
        r.actionHistory = actionHistory;
        return r;
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean isManualRequired() {
        return "WAIT_MANUAL".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}
