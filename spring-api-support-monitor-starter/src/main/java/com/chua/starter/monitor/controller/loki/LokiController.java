package com.chua.starter.monitor.controller.loki;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.service.loki.LokiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Loki 日志管理 API
 */
@RestController
@RequestMapping("/v2/loki")
@RequiredArgsConstructor
@Tag(name = "Loki 日志管理")
public class LokiController {

    private final LokiService lokiService;

    // ─── 连接管理 ──────────────────────────────────────────────────────────

    @Operation(summary = "打开连接")
    @PostMapping("/open")
    public ReturnResult<String> open(@RequestBody Map<String, Object> body) {
        String host = (String) body.get("host");
        Object portObj = body.get("port");
        int port = portObj instanceof Number ? ((Number) portObj).intValue() : Integer.parseInt(String.valueOf(portObj));
        String username = (String) body.getOrDefault("username", "");
        String password = (String) body.getOrDefault("password", "");
        String id = lokiService.open(host, port, username, password);
        lokiService.ready(id);
        return ReturnResult.ok(id);
    }

    @Operation(summary = "关闭连接")
    @DeleteMapping("/{connectionId}")
    public ReturnResult<Void> close(@PathVariable String connectionId) {
        lokiService.close(connectionId);
        return ReturnResult.ok();
    }

    @Operation(summary = "健康检查")
    @GetMapping("/{connectionId}/ready")
    public ReturnResult<Boolean> ready(@PathVariable String connectionId) {
        return ReturnResult.ok(lokiService.ready(connectionId));
    }

    // ─── 标签查询 ──────────────────────────────────────────────────────────

    @Operation(summary = "获取所有标签")
    @GetMapping("/{connectionId}/labels")
    public ReturnResult<List<String>> labels(@PathVariable String connectionId) {
        return ReturnResult.ok(lokiService.labels(connectionId));
    }

    @Operation(summary = "获取标签值")
    @GetMapping("/{connectionId}/labels/{label}/values")
    public ReturnResult<List<String>> labelValues(@PathVariable String connectionId, @PathVariable String label) {
        return ReturnResult.ok(lokiService.labelValues(connectionId, label));
    }

    // ─── 日志查询 ──────────────────────────────────────────────────────────

    @Operation(summary = "即时查询")
    @GetMapping("/{connectionId}/query")
    public ReturnResult<Map<String, Object>> query(
            @PathVariable String connectionId,
            @RequestParam String logql,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long time,
            @RequestParam(required = false) String direction) {
        Map<String, Object> result = lokiService.query(connectionId, logql, limit, time, direction);
        return ReturnResult.ok(result);
    }

    @Operation(summary = "范围查询")
    @GetMapping("/{connectionId}/query_range")
    public ReturnResult<Map<String, Object>> queryRange(
            @PathVariable String connectionId,
            @RequestParam String logql,
            @RequestParam long start,
            @RequestParam long end,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String step,
            @RequestParam(required = false) String direction) {
        Map<String, Object> result = lokiService.queryRange(connectionId, logql, start, end, limit, step, direction);
        return ReturnResult.ok(result);
    }

    // ─── 流与统计 ──────────────────────────────────────────────────────────

    @Operation(summary = "流查询")
    @GetMapping("/{connectionId}/series")
    public ReturnResult<List<Map<String, String>>> series(
            @PathVariable String connectionId,
            @RequestParam String match,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end) {
        return ReturnResult.ok(List.of());
    }

    @Operation(summary = "索引统计")
    @GetMapping("/{connectionId}/stats")
    public ReturnResult<Map<String, Object>> stats(
            @PathVariable String connectionId,
            @RequestParam String query,
            @RequestParam long start,
            @RequestParam long end) {
        return ReturnResult.ok(Map.of("status", "success"));
    }
}
