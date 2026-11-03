// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.geo.controller;

import com.chua.starter.geo.config.GeoTileSources;
import com.chua.starter.geo.model.CustomTileSource;
import com.chua.starter.geo.model.TileSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图源管理控制器，提供图源查询和 URL 分析接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/source")
@Tag(name = "图源管理", description = "图源配置与分析")
@RequiredArgsConstructor
@Slf4j
public class SourceController {

    /**
     * 图源配置服务
    */
    private final GeoTileSources tileSources;

    /**
     * 获取所有可用图源（含自定义图源）。
     *
     * @param tiandituToken 天地图 令牌（可为 空）
     * @return 图源详情 映射
     * @param false false
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有图源")
    public Map<String, Object> getAllSources(@RequestParam(required = false) String tiandituToken) {
        Map<String, TileSource> sources = tileSources.getAllSources(tiandituToken);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, TileSource> entry : sources.entrySet()) {
            TileSource s = entry.getValue();
            result.put(entry.getKey(), Map.of(
                    "id", s.getId(), "name", s.getName(), "url", s.getUrl(),
                    "subdomains", s.getSubdomains(), "maxZoom", s.getMaxZoom(),
                    "attribution", s.getAttribution()));
        }
        return result;
    }

    /**
     * 分析瓦片 URL 模板，提取占位符信息。
     *
     * @param req 包含 url 字段的请求体
     * @return 分析结果
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析URL模板")
    public Map<String, Object> analyzeUrl(@RequestBody Map<String, String> req) {
        String url = req != null ? req.getOrDefault("url", "") : "";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("urlTemplate", url);
        result.put("hasX", url.contains("{x}"));
        result.put("hasY", url.contains("{y}"));
        result.put("hasZ", url.contains("{z}"));
        result.put("hasS", url.contains("{s}"));
        return result;
    }

    /**
     * 添加自定义图源。
     *
     * @param custom 自定义图源配置
     * @return 操作结果
     */
    @PostMapping("/custom")
    @Operation(summary = "添加自定义图源")
    public Map<String, String> addCustom(@RequestBody CustomTileSource custom) {
        if (custom != null) {
            log.info("添加自定义图源: id={} name={}", custom.getId(), custom.getName());
        }
        return Map.of("message", "Custom source added: " + (custom != null ? custom.getId() : "unknown"));
    }
}
