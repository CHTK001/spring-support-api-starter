package com.chua.starter.geo.controller;

import com.chua.starter.geo.config.GeoProperties;
import com.chua.starter.geo.model.AppSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 全局设置控制器，提供应用配置读写接口。
 *
 * @author CH
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1/geo/settings")
@Tag(name = "全局设置", description = "应用全局配置读写")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    /** Geo 配置属性 */
    private final GeoProperties geoProperties;

    /**
     * 获取全局设置。
     *
     * @return 当前配置快照
     */
    @GetMapping
    @Operation(summary = "获取全局设置")
    public AppSettings getSettings() {
        AppSettings settings = new AppSettings();
        settings.setTiandituToken(geoProperties.getTiandituToken());
        settings.setProxyEnabled(geoProperties.getProxy().isEnabled());
        settings.setProxyUrl(geoProperties.getProxy().getUrl());
        settings.setDefaultConcurrency(geoProperties.getTile().getMaxConcurrency());
        settings.setDefaultZoom(geoProperties.getTile().getDefaultZoom());
        settings.setDefaultFormat("geotiff");
        settings.setAllowInvalidCerts(geoProperties.isAllowInvalidCerts());
        settings.setTileCacheEnabled(geoProperties.getTile().isCacheEnabled());
        settings.setTileCacheMaxSizeMb(geoProperties.getTile().getCacheMaxSizeMb());
        return settings;
    }

    /**
     * 保存全局设置。
     *
     * @param settings 新配置
     * @return 操作结果
     */
    @PostMapping
    @Operation(summary = "保存全局设置")
    public Map<String, String> saveSettings(@RequestBody AppSettings settings) {
        if (settings != null) {
            if (settings.getTiandituToken() != null) {
                geoProperties.setTiandituToken(settings.getTiandituToken());
            }
            if (settings.getProxyEnabled() != null) {
                geoProperties.getProxy().setEnabled(settings.getProxyEnabled());
            }
            if (settings.getProxyUrl() != null) {
                geoProperties.getProxy().setUrl(settings.getProxyUrl());
            }
            if (settings.getDefaultConcurrency() > 0) {
                geoProperties.getTile().setMaxConcurrency(settings.getDefaultConcurrency());
            }
            if (settings.getDefaultZoom() > 0) {
                geoProperties.getTile().setDefaultZoom(settings.getDefaultZoom());
            }
            if (settings.getDefaultFormat() != null) {
                geoProperties.getExport().setCompression(settings.getDefaultFormat());
            }
        }
        log.info("全局设置已更新");
        return Map.of("message", "Settings saved");
    }
}
