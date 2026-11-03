// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.geo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 助手控制器，提供聊天流式接口。
 * Web 版使用外部 LLM API（需配置）或返回模拟响应。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/assistant")
@Tag(name = "AI助手", description = "地理数据下载助手")
@Slf4j
public class AssistantController {

    /**
     * JSON 序列化器，用于转义 SSE 事件中的字符串
    */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 虚拟线程执行器
    */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 获取助手状态。
     * @return 状态的结果
     */
    @GetMapping("/status")
    @Operation(summary = "获取助手状态")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "mode", "web",
            "configured", false,
            "model", "none",
            "knowledge", Map.of("version", "", "articleCount", 0)
        ));
    }

    /**
     * 流式聊天（SSE）。
     * 请求体包含 消息 数组和可选的 diagnostic上下文。
     * 返回 SSE 流，每行格式：数据: {"类型":"delta","内容":"..."}
     * @param body 主体
     * @return 对话的结果
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天")
    public SseEmitter chat(@RequestBody Map<String, Object> body) {
        SseEmitter emitter = new SseEmitter(60_000L);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");
        String userMsg = messages != null && !messages.isEmpty()
                ? messages.get(messages.size() - 1).get("content")
                : "";
        log.info("[Assistant] chat: {}", userMsg != null ? userMsg.substring(0, Math.min(100, userMsg.length())) : "");

        executor.submit(() -> {
            try {
                // 模拟流式响应（Web版返回提示信息）
                String response = "Web 版 AI 助手暂未连接 LLM 服务。请在桌面版中使用，或配置后端 LLM API。";
                String[] chunks = response.split("");
                for (int i = 0; i < chunks.length; i++) {
                    emitter.send("data: " + "{\"type\":\"delta\",\"content\":\"" + escapeJson(chunks[i]) + "\"}\n\n");
                    Thread.sleep(20);
                }
                emitter.send("data: {\"type\":\"done\"}\n\n");
                emitter.complete();
            } catch (Exception e) {
                log.error("[Assistant] chat error", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 取消聊天（Web 版无操作）。
     * @param body 主体
     * @return cancel的结果
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消聊天")
    public ResponseEntity<Map<String, String>> cancel(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    /**
     * 使用 Jackson 转义 JSON 字符串。
     *
     * @param s 待转义字符串，可为 空
     * @return 转义后的 JSON 字符串（不含引号）
     */
    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        try {
 // 写入值as字符串 输出带引号的 JSON 字符串，去掉首尾引号
            String escaped = objectMapper.writeValueAsString(s);
            return escaped.substring(1, escaped.length() - 1);
        } catch (Exception e) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }
    }
}
