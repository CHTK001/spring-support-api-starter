package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.DemSource;
import com.chua.starter.geo.service.DemDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DEM 高程模型下载控制器，提供 Terrarium 等 DEM 源的下载与导出接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/dem")
@Tag(name = "DEM高程模型", description = "数字高程模型批量下载与导出")
@RequiredArgsConstructor
@Slf4j
public class DemController {

    /** DEM 下载服务 */
    private final DemDownloadService demDownloadService;

    /**
     * 估算 DEM 瓦片数量。
     *
     * @param request 估算请求参数
     * @return 估算结果
     */
    @PostMapping("/estimate")
    @Operation(summary = "估算DEM瓦片数量")
    public ResponseEntity<Map<String, Object>> estimate(
            @RequestBody @Valid DemEstimateRequest request) {
        long count = demDownloadService.estimateTiles(request.getBounds(), request.getZoom());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tileCount", count);
        result.put("zoom", request.getZoom());
        result.put("source", request.getSource());
        return ResponseEntity.ok(result);
    }

    /**
     * 创建 DEM 下载任务。
     *
     * @param request DEM 下载请求
     * @return 包含 taskId 和 tileCount 的结果
     */
    @PostMapping("/download")
    @Operation(summary = "创建DEM下载任务")
    public ResponseEntity<Map<String, Object>> download(@RequestBody @Valid DemDownloadRequest request) {
        DemSource source = DemSource.fromString(request.getSource());
        if (source == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown DEM source: " + request.getSource()));
        }
        long count = demDownloadService.estimateTiles(request.getBounds(), request.getZoom());
        if (count == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "No tiles at this zoom level"));
        }
        int total = (int) Math.min(count, Integer.MAX_VALUE);
        String taskId = "dem_" + System.currentTimeMillis();
        log.info("[DEM] 创建任务: source={} zoom={} tiles={}", source.getDisplayName(), request.getZoom(), total);
        // TODO: 接入 TaskManager 做异步任务管理（当前简化处理直接返回）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("tileCount", total);
        result.put("source", source.getDisplayName());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有可用的 DEM 数据源。
     *
     * @return DEM 源列表
     */
    @GetMapping("/sources")
    @Operation(summary = "获取可用DEM数据源")
    public ResponseEntity<Map<String, Object>> getSources() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (DemSource source : DemSource.values()) {
            result.put(source.getId(), Map.of(
                    "id", source.getId(),
                    "name", source.getDisplayName(),
                    "maxZoom", source.getMaxZoom()));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * DEM 估算请求体。
     */
    public record DemEstimateRequest(
            /** 地理边界 */
            @NotNull Bounds bounds,
            /** 缩放级别 */
            @NotNull @Min(1) Integer zoom,
            /** DEM 数据源 ID */
            @NotNull String source
    ) {}

    /**
     * DEM 下载请求体。
     */
    public record DemDownloadRequest(
            /** 地理边界 */
            @NotNull Bounds bounds,
            /** 缩放级别 */
            @NotNull @Min(1) Integer zoom,
            /** DEM 数据源 ID */
            @NotNull String source,
            /** 保存路径 */
            String savePath,
            /** 代理地址 */
            String proxy,
            /** 是否生成侧面文件 */
            Boolean generateSidecars
    ) {}
}
