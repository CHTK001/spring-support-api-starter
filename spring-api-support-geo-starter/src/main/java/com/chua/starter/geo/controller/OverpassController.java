package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.OverpassQueryRequest;
import com.chua.starter.geo.service.OverpassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Overpass OSM 矢量数据查询控制器。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/osm")
@Tag(name = "OSM矢量数据", description = "Overpass API 查询与下载")
@RequiredArgsConstructor
@Slf4j
public class OverpassController {

    /**
     * Overpass 查询服务
    */
    private final OverpassService overpassService;

    /**
     * 执行 Overpass 查询并返回结果统计。
     *
     * @param request 查询请求
     * @return 查询结果统计
     */
    @PostMapping("/query")
    @Operation(summary = "执行Overpass查询")
    public ResponseEntity<Map<String, Object>> query(@RequestBody @Valid OverpassQueryRequest request) {
        OverpassService.OverpassQueryResult result = overpassService.query(request);
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("elementCount", result.elementCount());
        resp.put("estimateSizeMb", String.format("%.2f", result.estimateSizeMb()));
        resp.put("format", request.getFormat());
        return ResponseEntity.ok(resp);
    }

    /**
     * 创建 OSM 矢量下载任务（简化版：仅查询统计，实际导出待后续实现）。
     *
     * @param request 查询请求
     * @return 任务信息
     */
    @PostMapping("/download")
    @Operation(summary = "创建OSM下载任务")
    public ResponseEntity<Map<String, Object>> download(@RequestBody @Valid OverpassQueryRequest request) {
        OverpassService.OverpassQueryResult result = overpassService.query(request);
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("taskId", "osm_" + System.currentTimeMillis());
        resp.put("elementCount", result.elementCount());
        resp.put("estimateSizeMb", String.format("%.2f", result.estimateSizeMb()));
        resp.put("message", "OSM 数据查询完成，导出功能待后续实现");
        return ResponseEntity.ok(resp);
    }
}
