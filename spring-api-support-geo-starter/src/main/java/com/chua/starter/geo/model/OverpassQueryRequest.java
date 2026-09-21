package com.chua.starter.geo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Overpass OSM 矢量下载请求体。
 *
 * @param bounds 地理边界
 * @param query Overpass QL 查询语句。 示例："[出:json];(节点['amenity'='restaurant'](bbox);way['amenity'='restaurant'](bbox);relation['amenity'='restaurant'](bbox););出 主体;"
 * @param format 输出格式：json / xml / gpkg
 * @param proxy 代理地址
 * @param savePath 保存路径
 * @param taskName 任务名称
 *
 * @author CH
 * @since 1.0.0
 */
public record OverpassQueryRequest(
        @Valid @NotNull(message = "bounds不能为空") Bounds bounds,
        @NotBlank(message = "query不能为空") String query,
        @NotBlank(message = "format不能为空") String format,
        String proxy,
        String savePath,
        String taskName
) {
}
