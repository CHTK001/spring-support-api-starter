package com.chua.starter.geo.controller;

import com.chua.starter.geo.service.WaybackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Esri Wayback 历史影像控制器，提供历史版本查询与下载接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/wayback")
@Tag(name = "Esri Wayback历史影像", description = "Esri World Imagery 历史版本浏览")
@RequiredArgsConstructor
@Slf4j
public class WaybackController {

    /** Wayback 服务 */
    private final WaybackService waybackService;

    /**
    * 获取历史影像版本列表。
    *
    * @return 版本列表及总数
     */
    @GetMapping("/versions")
    @Operation(summary = "获取历史影像版本列表")
    public Map<String, Object> getVersions() {
        var versions = waybackService.getVersions();
        return Map.of("versions", versions, "count", versions.size());
    }

    /**
      * 创建 Wayback 下载任务（Web 版返回模拟 任务id）。
     *
     * @param body 请求参数
     * @return 任务信息
     */
    @PostMapping("/download")
    @Operation(summary = "创建Wayback下载任务")
    public ResponseEntity<Map<String, Object>> download(@RequestBody Map<String, Object> body) {
        String taskId = "wb_" + System.currentTimeMillis();
        log.info("[Wayback] 下载任务已创建: {}", taskId);
        return ResponseEntity.ok(Map.of("taskId", taskId, "tileCount", 0));
    }

    /**
     * 探测 Wayback 最大可用缩放级别（Web 版返回默认值 18）。
     *
     * @param body 请求参数（版本id, lat, lng）
     * @return 最大缩放级别
     */
    @PostMapping("/probe")
    @Operation(summary = "探测最大缩放级别")
    public ResponseEntity<Map<String, Object>> probe(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("maxZoom", 18));
    }

    /**
     * 扫描 Wayback 元数据（Web 版返回空结果）。
     *
     * @param body 扫描请求参数
     * @return 扫描结果
     */
    @PostMapping("/scan")
    @Operation(summary = "扫描Wayback元数据")
    public ResponseEntity<Map<String, Object>> scan(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("scanId", "scan_" + System.currentTimeMillis(), "status", "completed", "releases", 0));
    }
}
