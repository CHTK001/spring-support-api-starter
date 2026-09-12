package com.chua.starter.geo.model;

import lombok.Data;

/**
* 自定义瓦片图源配置。
*
* @author CH
* @since 1.0.0
 */
@Data
public class CustomTileSource {
    /** 图源唯一标识 */
    private String id;
    /** 图源显示名称 */
    private String name;
    /** 瓦片 URL 模板 */
    private String url;
    /** 子域名列表（逗号分隔） */
    private String subdomains;
    /** 最大支持缩放级别 */
    private int maxZoom;
    /** URL 协议方案（http/https） */
    private String scheme;
}
