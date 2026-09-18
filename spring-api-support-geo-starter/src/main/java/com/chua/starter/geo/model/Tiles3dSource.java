package com.chua.starter.geo.model;

import lombok.Data;
import java.util.Map;

/**
 * 3D 瓷砖 数据源配置，支持 铯 Ion 资产和自定义 URL。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class Tiles3dSource {
    /** 数据源类型：铯_ion 或 url */
    private String type;

    /** 铯 Ion 资产 标识（类型=铯_ion 时必填） */
    private Long assetId;

    /** 铯 Ion Access 令牌（类型=铯_ion 时必填） */
    private String accessToken;

    /** 直接 URL 模式的 tileset.json 地址（类型=url 时必填） */
    private String tilesetQueryUrl;

    /** 额外请求头（如 授权） */
    private Map<String, String> headers;
}
