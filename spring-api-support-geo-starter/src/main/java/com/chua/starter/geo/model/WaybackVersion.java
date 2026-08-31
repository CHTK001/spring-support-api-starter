package com.chua.starter.geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Esri Wayback 历史影像版本信息。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaybackVersion {
    /** 版本唯一标识 */
    private String id;
    /** 影像捕获时间 */
    private LocalDateTime timestamp;
    /** 影像日期字符串（YYYY-MM-DD） */
    private String dateStr;
    /** 版本描述 */
    private String description;
    /** 该版本的瓦片图源配置 */
    private TileSource tileSource;
}
