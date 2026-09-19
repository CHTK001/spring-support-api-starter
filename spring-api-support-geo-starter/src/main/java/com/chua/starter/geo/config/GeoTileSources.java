package com.chua.starter.geo.config;

import com.chua.starter.geo.model.TileSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 瓦片图源配置服务，管理内置图源及天地图图源。
 *
 * @author CH
 * @since 1.0.0
 */
@Component
public class GeoTileSources {

    /**
     * 天地图域名前缀常量
    */
    private static final String TIANDITU_HOST = "tianditu";

    /**
     * 图源缓存，键 为图源 标识，值 为图源配置
    */
    private final Map<String, TileSource> builtInSources = new LinkedHashMap<>();

    /**
     * 构造方法，初始化内置图源列表。
     */
    public GeoTileSources() {
        initBuiltInSources();
    }

    /**
     * 初始化内置图源列表。
     */
    private void initBuiltInSources() {
        builtInSources.put("google_satellite", make("google_satellite", "Google 卫星",
                "https://mt{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}", List.of("0", "1", "2", "3"), 20, "© Google"));
        builtInSources.put("google_map", make("google_map", "Google 地图",
                "https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}", List.of("0", "1", "2", "3"), 20, "© Google"));
        builtInSources.put("google_hybrid", make("google_hybrid", "Google 混合",
                "https://mt{s}.google.com/vt/lyrs=y&x={x}&y={y}&z={z}", List.of("0", "1", "2", "3"), 20, "© Google"));
        builtInSources.put("osm", make("osm", "OpenStreetMap",
                "https://tile.openstreetmap.org/{z}/{x}/{y}.png", List.of(), 19, "© OpenStreetMap contributors"));
        builtInSources.put("arcgis_satellite", make("arcgis_satellite", "ArcGIS 卫星",
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", List.of(), 19, "© Esri"));
        builtInSources.put("arcgis_world", make("arcgis_world", "ArcGIS 世界",
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}", List.of(), 19, "© Esri"));
        builtInSources.put("carto_light", make("carto_light", "Carto Light",
                "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png", List.of("a", "b", "c", "d"), 19, "© CARTO"));
        builtInSources.put("carto_dark", make("carto_dark", "Carto Dark",
                "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png", List.of("a", "b", "c", "d"), 19, "© CARTO"));
        builtInSources.put("gaode_satellite", make("gaode_satellite", "高德卫星",
                "https://webst{s}.is.autonavi.com/app/tile?style=satellite&x={x}&y={y}&z={z}",
                List.of("0", "1", "2", "3"), 18, "© 高德"));
        builtInSources.put("gaode_map", make("gaode_map", "高德地图",
                "https://webst{s}.is.autonavi.com/app/tile?style=normal&x={x}&y={y}&z={z}",
                List.of("0", "1", "2", "3"), 18, "© 高德"));
        builtInSources.put("esri_world", make("esri_world", "Esri World Imagery",
                "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
                List.of(), 19, "© Esri"));
        builtInSources.put("open_topo", make("open_topo", "OpenTopoMap",
                "https://tile.opentopomap.org/{z}/{x}/{y}.png", List.of(), 17, "© OpenTopoMap"));
    }

    /**
     * 获取所有可用图源（含天地图，需传入 令牌）。
     *
     * @param tiandituToken 天地图 令牌（可为 空，使用 geo属性 中配置的默认值）
     * @return 图源 映射
     */
    public Map<String, TileSource> getAllSources(String tiandituToken) {
        Map<String, TileSource> result = new LinkedHashMap<>(builtInSources);
        String token = tiandituToken != null ? tiandituToken : "";
        if (!token.isEmpty()) {
            result.put("tianditu_img", makeTianditu(token, "img", "天地图影像", 18));
            result.put("tianditu_cia", makeTianditu(token, "cia", "天地图影像标注", 18));
            result.put("tianditu_vec", makeTianditu(token, "vec", "天地图矢量", 18));
            result.put("tianditu_cva", makeTianditu(token, "cva", "天地图矢量标注", 18));
        }
        return result;
    }

    /**
     * 根据 标识 获取单个图源。
     *
     * @param id             图源标识
     * @param tiandituToken  天地图 令牌（可为 空）
     * @return 图源配置，不存在返回 空
     */
    public TileSource getByName(String id, String tiandituToken) {
        return getAllSources(tiandituToken).get(id);
    }

    /**
     * 创建图源配置对象。
     *
     * @param id           图源唯一标识
     * @param name         图源显示名称
     * @param url          瓦片 URL 模板
     * @param subdomains   子域名列表
     * @param maxZoom      最大缩放级别
     * @param attribution  版权署名
     * @return 图源配置对象
     */
    private TileSource make(String id, String name, String url, List<String> subdomains, int maxZoom, String attribution) {
        TileSource s = new TileSource();
        s.setId(id);
        s.setName(name);
        s.setUrl(url);
        s.setSubdomains(subdomains);
        s.setMaxZoom(maxZoom);
        s.setAttribution(attribution);
        return s;
    }

    /**
     * 创建天地图图源配置。
     *
     * @param token   天地图访问 令牌
     * @param layer   图层名称（img/cia/vec/cva）
     * @param name    显示名称
     * @param maxZoom 最大缩放级别
     * @return 天地图图源配置
     */
    private TileSource makeTianditu(String token, String layer, String name, int maxZoom) {
        String firstChar = layer.substring(0, 1).toLowerCase();
        String url = "https://t" + firstChar + TIANDITU_HOST + ".gov.cn/" + layer + "_wmts?"
                + "service=wmts&request=GetTile&version=1.0.0&layer=" + layer
                + "&styles=default&tileMatrixSet=w&format=tiles&tk=" + token
                + "&TileMatrix={z}&TileRow={y}&TileCol={x}";
        return make("tianditu_" + layer, name, url, List.of(), maxZoom, "© 天地图");
    }
}
