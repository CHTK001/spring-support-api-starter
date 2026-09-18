package com.chua.starter.geo.controller;

import com.chua.starter.geo.model.TilesetSummary;
import com.chua.starter.geo.model.Tiles3dSource;
import com.chua.starter.geo.model.Tiles3dTaskRequest;
import com.chua.starter.geo.service.Tiles3dService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 3D 瓷砖 下载控制器，提供 铯 Ion 和自定义 URL 的 3D 瓷砖 下载接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/3dtiles")
@Tag(name = "3D Tiles", description = "3D Tiles 资产下载与离线浏览")
@RequiredArgsConstructor
@Slf4j
public class Tiles3dController {

    /** 3D 瓷砖 下载服务 */
    private final Tiles3dService tiles3dService;

    /**
    * 分析 tileset 结构，返回摘要信息。
    *
    * @param request 数据源配置
    * @param proxy   代理地址（可为 空）
    * @return tileset 摘要
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析3D Tiles结构")
    public ResponseEntity<TilesetSummary> analyze(
            @RequestBody @Valid Map<String, Object> request,
            @RequestParam(required = false) String proxy) {
        Tiles3dSource source = buildSourceFromMap(request);
        try {
            TilesetSummary summary = tiles3dService.analyze(source, proxy);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("分析 3D Tiles 失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建 3D 瓷砖 下载任务。
     *
     * @param request 下载请求
     * @return 任务结果
     */
    @PostMapping("/download")
    @Operation(summary = "下载3D Tiles资产")
    public ResponseEntity<Map<String, Object>> download(
            @RequestBody @Valid Tiles3dTaskRequest request) {
        try {
            Tiles3dService.Download3dResult result = tiles3dService.download(
                    request.getSource(), request.getPolygon(),
                    request.getProxy(), request.getSavePath());
            Map<String, Object> resp = new java.util.LinkedHashMap<>();
            resp.put("savePath", result.savePath());
            resp.put("contentCount", result.contentCount());
            resp.put("totalTiles", result.totalTiles());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("3D Tiles 下载失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从 映射 构建 瓷砖3d源（供 分析 接口使用）。
     *
     * @param map 请求参数字典
     * @return Tiles3dSource
     */
    private Tiles3dSource buildSourceFromMap(Map<String, Object> map) {
        Tiles3dSource source = new Tiles3dSource();
        source.setType(map.containsKey("type") ? String.valueOf(map.get("type")) : "url");
        if (map.containsKey("assetId")) {
            source.setAssetId(Long.valueOf(String.valueOf(map.get("assetId"))));
        }
        if (map.containsKey("accessToken")) {
            source.setAccessToken(String.valueOf(map.get("accessToken")));
        }
        if (map.containsKey("tilesetQueryUrl")) {
            source.setTilesetQueryUrl(String.valueOf(map.get("tilesetQueryUrl")));
        }
        return source;
    }
}
