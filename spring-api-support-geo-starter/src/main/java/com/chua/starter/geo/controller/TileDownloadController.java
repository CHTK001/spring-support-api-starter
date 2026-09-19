package com.chua.starter.geo.controller;

import com.chua.starter.geo.config.GeoTileSources;
import com.chua.starter.geo.model.*;
import com.chua.starter.geo.service.GeoTiffExporter;
import com.chua.starter.geo.service.TaskManagerService;
import com.chua.starter.geo.service.TileDownloaderService;
import com.chua.starter.geo.service.TileMergerService;
import com.chua.starter.geo.util.TileMathUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 瓦片下载控制器，提供图源查询、下载估算、任务创建与状态管理接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/tile")
@Tag(name = "瓦片下载", description = "影像瓦片批量下载与导出")
@RequiredArgsConstructor
@Slf4j
public class TileDownloadController {

    /**
     * 瓦片下载服务
    */
    private final TileDownloaderService downloaderService;

    /**
     * 瓦片拼接服务
    */
    private final TileMergerService mergerService;

    /**
     * 图像导出服务
    */
    private final GeoTiffExporter exporter;

    /**
     * 任务管理服务
    */
    private final TaskManagerService taskManager;

    /**
     * 图源配置服务
    */
    private final GeoTileSources tileSources;

    /**
     * 异步任务执行线程池（虚拟 Thread）
    */
    private ExecutorService executor;

    /**
     * 初始化。
     */
    @PostConstruct
    public void init() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * destroy。
     */
    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    /**
     * 估算指定区域的瓦片数量与大小。
     *
     * @param request 下载请求参数
     * @return 估算结果
     */
    @PostMapping("/estimate")
    @Operation(summary = "估算瓦片数量与大小")
    public ResponseEntity<TileEstimate> estimate(@RequestBody @Valid DownloadRequest request) {
        Objects.requireNonNull(request, "请求体不能为空");
        Objects.requireNonNull(request.getBounds(), "bounds不能为空");
        Objects.requireNonNull(request.getZoom(), "zoom不能为空");
        Objects.requireNonNull(request.getSource(), "source不能为空");
        Objects.requireNonNull(request.getFormat(), "format不能为空");

        TileSource source = tileSources.getByName(request.getSource(), request.getTiandituToken());
        if (source == null) {
            TileEstimate error = new TileEstimate();
            error.setTileCount(-1);
            error.setMessage("Unknown source: " + request.getSource());
            return ResponseEntity.badRequest().body(error);
        }
        long count = TileMathUtils.estimateTileCount(request.getBounds(), request.getZoom());
        TileEstimate estimate = new TileEstimate();
        estimate.setTileCount((int) Math.min(count, Integer.MAX_VALUE));
        estimate.setZoom(request.getZoom());
        estimate.setEstimatedOutputMb(count * 0.08);
        return ResponseEntity.ok(estimate);
    }

    /**
     * 创建瓦片下载任务并异步启动下载流程。
     *
     * @param request 下载请求参数
     * @return 包含 任务id 和 瓷砖数量 的结果
     */
    @PostMapping("/download")
    @Operation(summary = "创建瓦片下载任务")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody @Valid DownloadRequest request) {
        Objects.requireNonNull(request, "请求体不能为空");
        Objects.requireNonNull(request.getBounds(), "bounds不能为空");
        Objects.requireNonNull(request.getZoom(), "zoom不能为空");
        Objects.requireNonNull(request.getSource(), "source不能为空");
        Objects.requireNonNull(request.getFormat(), "format不能为空");

        TileSource source = tileSources.getByName(request.getSource(), request.getTiandituToken());
        if (source == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown source: " + request.getSource()));
        }
        long count = TileMathUtils.estimateTileCount(request.getBounds(), request.getZoom());
        int total = (int) Math.min(count, Integer.MAX_VALUE);
        String taskId = taskManager.createTask(
                request.getSourceName() != null ? request.getSourceName() : "download_task",
                request.getSource(), source.getName(), request.getZoom(),
                request.getFormat(),
                request.getSavePath() != null ? request.getSavePath() : "/tmp",
                total);
        executor.submit(() -> runDownload(taskId, request, source));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("tileCount", total);
        return ResponseEntity.ok(result);
    }

    /**
     * 异步执行完整的下载→拼接→导出流程。
     *
     * @param taskId  任务 标识
     * @param request 下载请求
     * @param source  图源配置
     */
    private void runDownload(String taskId, DownloadRequest request, TileSource source) {
        try {
            taskManager.setStatus(taskId, TaskStatus.DOWNLOADING);
            TileDownloaderService.DownloadResult result = downloaderService.downloadTiles(
                    source, request.getBounds(), request.getZoom(),
                    request.getProxy(), true);
            taskManager.updateProgress(taskId, result.completed(), result.failed(),
                    result.total() > 0 ? (double) result.completed() / result.total() * 100 : 0,
                    "瓦片下载完成: " + result.completed() + "/" + result.total());

            if (result.failed() == result.total() && result.total() > 0) {
                taskManager.setStatus(taskId, TaskStatus.FAILED);
                return;
            }

            taskManager.setStatus(taskId, TaskStatus.MERGING);
            BufferedImage image = mergerService.mergeTiles(result.tiles(), request.getBounds(), request.getZoom());

            taskManager.setStatus(taskId, TaskStatus.EXPORTING);
            String outputPath = request.getSavePath() != null ? request.getSavePath() : "/tmp";
            String fileName = request.getSourceName() != null ? request.getSourceName() : "output";
            String compression = request.getCompression() != null ? request.getCompression() : "lzw";
            boolean buildPyramid = Boolean.TRUE.equals(request.getBuildPyramid());
            boolean generateSidecars = Boolean.TRUE.equals(request.getGenerateSidecars());
            String finalPath = exporter.export(image,
                    OutputFormat.fromString(request.getFormat()),
                    outputPath + "/" + fileName,
                    compression, buildPyramid, generateSidecars);

            taskManager.updateProgress(taskId, result.completed(), result.failed(), 100.0, "导出完成: " + finalPath);
            taskManager.setStatus(taskId, TaskStatus.COMPLETED);
        } catch (Exception e) {
            taskManager.setStatus(taskId, TaskStatus.FAILED);
            log.error("[{}] 任务执行失败: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 获取所有可用图源列表。
     *
     * @param tiandituToken 天地图 令牌（可为 空）
     * @return 图源详情 映射
     * @param false false
     */
    @GetMapping("/sources")
    @Operation(summary = "获取可用图源列表")
    public ResponseEntity<Map<String, Object>> getSources(@RequestParam(required = false) String tiandituToken) {
        Map<String, TileSource> all = tileSources.getAllSources(tiandituToken);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, TileSource> entry : all.entrySet()) {
            TileSource s = entry.getValue();
            result.put(entry.getKey(), Map.of(
                    "id", s.getId(), "name", s.getName(),
                    "maxZoom", s.getMaxZoom(), "attribution", s.getAttribution()));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 查询单个任务状态。
     *
     * @param taskId 任务 标识
     * @return 任务信息
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询任务状态")
    public ResponseEntity<TaskInfo> getTask(@PathVariable String taskId) {
        TaskInfo info = taskManager.getTask(taskId);
        return info != null ? ResponseEntity.ok(info) : ResponseEntity.notFound().build();
    }

    /**
     * 列出所有任务。
     *
     * @return 任务列表
     */
    @GetMapping("/tasks")
    @Operation(summary = "列出所有任务")
    public ResponseEntity<List<TaskInfo>> listTasks() {
        return ResponseEntity.ok(taskManager.listTasks());
    }

    /**
     * 取消任务。
     *
     * @param taskId 任务 标识
     * @return 操作结果
     */
    @PostMapping("/task/{taskId}/cancel")
    @Operation(summary = "取消任务")
    public ResponseEntity<Map<String, String>> cancelTask(@PathVariable String taskId) {
        taskManager.cancelTask(taskId);
        return ResponseEntity.ok(Map.of("message", "Task cancelled"));
    }

    /**
     * 删除任务记录。
     *
     * @param taskId 任务 标识
     * @return 操作结果
     */
    @DeleteMapping("/task/{taskId}")
    @Operation(summary = "删除任务记录")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable String taskId) {
        taskManager.removeTask(taskId);
        return ResponseEntity.ok(Map.of("message", "Task removed"));
    }

    /**
     * 导出部分下载结果（Web 版为 no-op）。
     *
     * @param taskId 任务 标识
     * @return 操作结果
     */
    @PostMapping("/task/{taskId}/export")
    @Operation(summary = "导出部分下载结果")
    public ResponseEntity<Map<String, String>> exportPartial(@PathVariable String taskId) {
        log.info("[Tile] 部分导出请求（Web版跳过）: {}", taskId);
        return ResponseEntity.ok(Map.of("message", "Partial export skipped (web mode)"));
    }
}
