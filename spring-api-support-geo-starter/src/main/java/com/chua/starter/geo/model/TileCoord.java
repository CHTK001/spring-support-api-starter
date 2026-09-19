package com.chua.starter.geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 瓦片坐标，表示一个瓦片的唯一位置。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TileCoord {
    /**
     * X 轴瓦片索引
    */
    private int x;
    /**
     * Y 轴瓦片索引
    */
    private int y;
    /**
     * 缩放级别
    */
    private int z;
}
