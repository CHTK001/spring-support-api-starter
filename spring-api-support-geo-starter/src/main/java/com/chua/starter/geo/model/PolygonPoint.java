package com.chua.starter.geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多边形顶点坐标（WGS-84 经纬度）。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolygonPoint {
    /** 纬度 */
    private double lat;
    /** 经度 */
    private double lng;
}
