package com.chua.starter.video.live.model;

import lombok.Data;

/**
 * 直播分组。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class LiveGroup {

    /**
     * 分组标识。
     */
    private String id;

    /**
     * 分组名称。
     */
    private String label;

    /**
     * 是否央视分组。
     */
    private boolean cctv;

    /**
     * 排序值。
     */
    private int sort;

    /**
     * 分组内频道数量。
     */
    private int count;
}
