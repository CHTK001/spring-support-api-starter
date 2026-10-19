package com.chua.starter.geo.model;

import java.util.List;

/**
 * 3D 瓷砖 tileset 解析摘要，用于前端预览和下载前确认。
 *
 * @param version              tileset 规范版本
 * @param extent               覆盖范围 [west, south, east, north] 度，region 类型包围体
 * @param totalTiles           总节点数
 * @param contentTiles         有内容文件的节点数
 * @param maxDepth             最大树深度
 * @param levels               层级数（最大深度 + 1）
 * @param hasExternalTilesets  是否包含外部 tileset 引用
 * @author CH
 * @since 1.0.0
 */
public record TilesetSummary(
        String version,
        List<Double> extent,
        int totalTiles,
        int contentTiles,
        int maxDepth,
        int levels,
        boolean hasExternalTilesets) {
}
