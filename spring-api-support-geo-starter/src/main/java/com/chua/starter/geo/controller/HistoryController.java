package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.DownloadHistoryRecord;
import com.chua.starter.geo.service.DownloadHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 下载历史记录控制器，提供记录查询、删除、清空接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/history")
@Tag(name = "下载历史", description = "下载历史记录查询与管理")
@RequiredArgsConstructor
@Slf4j
public class HistoryController {

    /** 历史记录服务 */
    private final DownloadHistoryService historyService;

    /**
     * 获取所有历史记录。
     *
     * @return 历史记录列表
     */
    @GetMapping
    @Operation(summary = "获取所有历史记录")
    public ResponseEntity<List<DownloadHistoryRecord>> listAll() {
        return ResponseEntity.ok(historyService.listAll());
    }

    /**
     * 分页获取历史记录。
     *
     * @param page     页码（从 1 开始，默认 1）
     * @param pageSize 每页大小（默认 20，最大 100）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取历史记录")
    public ResponseEntity<Map<String, Object>> listPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        DownloadHistoryService.PageResult result = historyService.listPage(page, pageSize);
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("records", result.records());
        resp.put("total", result.total());
        resp.put("page", page);
        resp.put("pageSize", pageSize);
        return ResponseEntity.ok(resp);
    }

    /**
     * 添加一条下载记录。
     *
     * @param record 下载记录
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "添加下载记录")
    public ResponseEntity<Map<String, String>> add(@RequestBody DownloadHistoryRecord record) {
        if ("completed".equals(record.getStatus())) {
            historyService.save(record);
        } else {
            historyService.saveFailed(record);
        }
        return ResponseEntity.ok(Map.of("message", "Record added"));
    }

    /**
     * 删除指定历史记录。
     *
     * @param id 记录 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除历史记录")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        boolean removed = historyService.delete(id);
        return removed ? ResponseEntity.ok(Map.of("message", "Record deleted"))
                       : ResponseEntity.notFound().build();
    }

    /**
     * 构建影像金字塔（Web 版为 no-op）。
     *
     * @param id 记录 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/pyramid")
    @Operation(summary = "构建影像金字塔")
    public ResponseEntity<Map<String, String>> buildPyramid(@PathVariable String id) {
        log.info("[历史] 金字塔构建请求（Web版跳过）: {}", id);
        return ResponseEntity.ok(Map.of("message", "Pyramid build skipped (web mode)"));
    }

    /**
     * 清空所有历史记录。
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有历史记录")
    public ResponseEntity<Map<String, String>> clear() {
        historyService.clear();
        return ResponseEntity.ok(Map.of("message", "All history cleared"));
    }
}
