package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.Bounds;
import com.chua.starter.geo.model.DemSource;
import com.chua.starter.geo.model.TaskStatus;
import com.chua.starter.geo.service.DemDownloadService;
import com.chua.starter.geo.service.TaskManagerService;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /**
     * DEM 下载服务
    */
    private final DemDownloadService demDownloadService;

    /**
     * DEM 下载任务服务
     */
    private final TaskManagerService taskManagerService;

    /**
     * DEM 任务执行器
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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
        long count = demDownloadService.estimateTiles(request.bounds(), request.zoom());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tileCount", count);
        result.put("zoom", request.zoom());
        result.put("source", request.source());
        return ResponseEntity.ok(result);
    }

    /**
     * 创建 DEM 下载任务。
     *
     * @param request DEM 下载请求
     * @return 包含 任务id 和 瓷砖数量 的结果
     */
    @PostMapping("/download")
    @Operation(summary = "创建DEM下载任务")
    public ResponseEntity<Map<String, Object>> download(@RequestBody @Valid DemDownloadRequest request) {
        DemSource source = DemSource.fromString(request.source());
        if (source == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown DEM source: " + request.source()));
        }
        long count = demDownloadService.estimateTiles(request.bounds(), request.zoom());
        if (count == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "No tiles at this zoom level"));
        }
        int total = (int) Math.min(count, Integer.MAX_VALUE);
        String savePath = request.savePath();
        if (savePath == null || savePath.isBlank()) {
            savePath = System.getProperty("java.io.tmpdir");
        }
        final String outputDirectory = savePath;
        String taskId = taskManagerService.createTask(
                "dem_download",
                source.getId(),
                source.getDisplayName(),
                request.zoom(),
                "tiff",
                outputDirectory,
                total);
        executor.submit(() -> runDownload(taskId, request, source, outputDirectory));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        result.put("tileCount", total);
        result.put("source", source.getDisplayName());
        return ResponseEntity.accepted().body(result);
    }

    /**
     * 执行 DEM 下载、拼接和 GeoTIFF 导出，并维护任务状态。
     *
     * @param taskId 任务标识
     * @param request 下载请求
     * @param source DEM 数据源
     * @param savePath 输出目录
     */
    private void runDownload(String taskId, DemDownloadRequest request, DemSource source, String savePath) {
        try {
            taskManagerService.setStatus(taskId, TaskStatus.DOWNLOADING);
            String outputPath = demDownloadService.downloadAndExport(
                    source,
                    request.bounds(),
                    request.zoom(),
                    request.proxy(),
                    savePath,
                    Boolean.TRUE.equals(request.generateSidecars()));
            taskManagerService.updateProgress(taskId, 1, 0, 100.0, "导出完成: " + outputPath);
            taskManagerService.setStatus(taskId, TaskStatus.COMPLETED);
        } catch (Exception e) {
            taskManagerService.setStatus(taskId, TaskStatus.FAILED);
            log.error("[DEM] 任务执行失败: taskId={}", taskId, e);
        }
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
     *
     * <p>{@code POST /v1/geo/dem/estimate} 的 JSON 请求体，只做瓦片数量试算，
     * 不落盘、不落任务。</p>
     *
     * @param bounds  下载区域的地理边界框（WGS84 经纬度，Web Mercator 投影），
     *                含北/南/东/西四个边界值；不允许为 {@code null}（{@code @NotNull}），
     *                非法地理范围会导致估算出的瓦片数为 0
     * @param zoom    瓦片缩放级别（Web 墨卡托金字塔层级）；不允许为 {@code null}，
     *                下界为 1（{@code @Min(1)}），上界由数据源决定，
     *                当前唯一数据源 {@code DemSource.TERRARIUM} 最大为 15
     * @param source  DEM 数据源标识，交给 {@code DemSource.fromString(String)} 解析，
     *                取值为 {@code terrarium}（对应显示名 {@code AWS Terrain (Terrarium)}），
     *                忽略大小写；不允许为 {@code null}（{@code @NotNull}）。
     *                估算接口不校验该值，非法值仍返回 200，只是回显原串
     */
    public record DemEstimateRequest(
            /**
             * 地理边界
            */
            @NotNull Bounds bounds,
            /**
             * 缩放级别
            */
            @NotNull @Min(1) Integer zoom,
            /**
             * DEM 数据源 标识
            */
            @NotNull String source
    ) {}

    /**
     * DEM 下载请求体。
     *
     * <p>{@code POST /v1/geo/dem/download} 的 JSON 请求体。接口先按
     * {@code bounds} 与 {@code zoom} 试算瓦片数，为 0 直接返回 400；
     * 否则登记异步任务并立即返回 202，任务体在虚拟线程中执行下载、拼接与 GeoTIFF 导出。</p>
     *
     * @param bounds            下载区域的地理边界框（WGS84 经纬度，Web Mercator 投影），
     *                          含北/南/东/西四个边界值；不允许为 {@code null}（{@code @NotNull}），
     *                          落在该范围外的瓦片数为 0，会被下载接口判为 400
     * @param zoom              瓦片缩放级别（Web 墨卡托金字塔层级）；不允许为 {@code null}，
     *                          下界为 1（{@code @Min(1)}），上界受数据源限制
     *                          （{@code DemSource.TERRARIUM} 最大 15）
     * @param source            DEM 数据源标识，经 {@code DemSource.fromString(String)} 解析为枚举，
     *                          当前支持 {@code terrarium}（忽略大小写，匹配 id 或显示名）；
     *                          不允许为 {@code null}（{@code @NotNull}），
     *                          解析失败返回 400 {@code Unknown DEM source: ...}
     * @param savePath          导出文件的输出目录；允许为 {@code null} 或空白，
     *                          此时回落到系统属性 {@code java.io.tmpdir}；
     *                          目录不存在时由 {@code exportElevationTiff} 自动 {@code mkdirs} 创建
     * @param proxy             下载 DEM 瓦片时使用的 HTTP 代理地址，构造
     *                          {@code java.net.Proxy} 前会先经 {@code new URI(proxy)} 解析，
     *                          因此必须带协议前缀且为合法 URI（如 {@code http://127.0.0.1:7890}）；
     *                          允许为 {@code null} 或空串（表示直连）；注意天地图（tianditu）
     *                          域名的请求会主动跳过代理
     * @param generateSidecars  是否在导出 {@code .tif} 之外附带生成投影附属文件
     *                          （当前实现写的是 Web Mercator / EPSG:3857 的 {@code .prj}）；
     *                          允许为 {@code null}，以 {@code Boolean.TRUE.equals(...)} 判定，
     *                          即只有显式传 {@code true} 才生成，传 {@code null} 或 {@code false} 均不生成
     */
    public record DemDownloadRequest(
            /**
             * 地理边界
            */
            @NotNull Bounds bounds,
            /**
             * 缩放级别
            */
            @NotNull @Min(1) Integer zoom,
            /**
             * DEM 数据源 标识
            */
            @NotNull String source,
            /**
             * 保存路径
            */
            String savePath,
            /**
             * 代理地址
            */
            String proxy,
            /**
             * 是否生成侧面文件
            */
            Boolean generateSidecars
    ) {}
}
