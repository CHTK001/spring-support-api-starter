package com.chua.starter.geo.model;

import lombok.Data;
import java.util.List;

/**
 * 瓦片图源配置，描述一个可用的地图瓦片服务。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class TileSource {
    /** 图源唯一标识，如 "google_satellite" */
    private String id;
    /** 图源显示名称 */
    private String name;
    /** 瓦片 URL 模板，支持 {x}/{y}/{z}/{s} 占位符 */
    private String url;
    /** 子域名列表，用于负载均衡 */
    private List<String> subdomains;
    /** 最大支持缩放级别 */
    private int maxZoom;
    /** 版权署名信息 */
    private String attribution;
}
