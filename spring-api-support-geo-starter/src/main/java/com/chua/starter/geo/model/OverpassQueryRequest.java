package com.chua.starter.geo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
* Overpass OSM 矢量下载请求体。
*
* @author CH
* @since 1.0.0
 */
@Data
public class OverpassQueryRequest {
    /** 地理边界 */
    @Valid
    @NotNull(message = "bounds不能为空")
    private Bounds bounds; // bounds

    /**
    * Overpass QL 查询语句。
    * 示例："[出:json];(节点['amenity'='restaurant'](bbox);way['amenity'='restaurant'](bbox);relation['amenity'='restaurant'](bbox););出 主体;"
     */
    @NotBlank(message = "query不能为空")
    private String query; // 查询

    /** 输出格式：json / xml / gpkg */
    @NotBlank(message = "format不能为空")
    private String format; // 格式化

    /** 代理地址 */
    private String proxy;

    /** 保存路径 */
    private String savePath;

    /** 任务名称 */
    private String taskName;
}
