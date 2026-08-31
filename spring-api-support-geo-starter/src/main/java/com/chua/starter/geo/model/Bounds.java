package com.chua.starter.geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理边界框（Web Mercator 坐标系）。
 * 表示一个矩形区域，通常由地图当前可视范围决定。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bounds {
    /** 北边界（纬度最大值） */
    private double north;
    /** 南边界（纬度最小值） */
    private double south;
    /** 东边界（经度最大值） */
    private double east;
    /** 西边界（经度最小值） */
    private double west;
}
