package com.chua.starter.geo.model;

import lombok.Data;

/**
* Esri Wayback 历史影像版本信息。
*
* @author CH
* @since 1.0.0
 */
@Data
public class WaybackVersion {
    /** 版本内部标识（itemtitle） */
    private String id;
    /** 显示日期或标题 */
    private String date;
    /** 完整标题 */
    private String title;
    /** 层标识符（layeridentifier） */
    private String layerId;
}
