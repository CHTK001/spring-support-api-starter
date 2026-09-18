package com.chua.starter.geo.model;

import lombok.Data;
import java.util.List;

/**
 * 3D 瓷砖 tileset 解析摘要，用于前端预览和下载前确认。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class TilesetSummary {
    /** tileset 规范版本 */
    private String version;
    /** 覆盖范围 [west, south, east, north] 度，region 类型包围体 */
    private List<Double> extent;
    /** 总节点数 */
    private int totalTiles;
    /** 有内容文件的节点数 */
    private int contentTiles;
    /** 最大树深度 */
    private int maxDepth;
    /** 层级数（最大深度 + 1） */
    private int levels;
    /** 是否包含外部 tileset 引用 */
    private boolean hasExternalTilesets;
}
