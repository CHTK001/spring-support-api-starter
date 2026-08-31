package com.chua.starter.geo.model;

import lombok.Data;
import java.util.Map;

/**
 * 3D Tiles 数据源配置，支持 Cesium Ion 资产和自定义 URL。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class Tiles3dSource {
    /** 数据源类型：cesium_ion 或 url */
    private String type;

    /** Cesium Ion 资产 ID（type=cesium_ion 时必填） */
    private Long assetId;

    /** Cesium Ion Access Token（type=cesium_ion 时必填） */
    private String accessToken;

    /** 直接 URL 模式的 tileset.json 地址（type=url 时必填） */
    private String tilesetQueryUrl;

    /** 额外请求头（如 Authorization） */
    private Map<String, String> headers;
}
