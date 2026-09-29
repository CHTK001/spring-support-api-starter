package com.chua.starter.video.live.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播目录（分组 + 频道）。
 *
 * <p>既是内置 JSON（{@code video/live-channels.json}）的反序列化目标，也是
 * 配置数据库（{@code sys_setting}）行数据的组装结果。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
public class LiveCatalog {

    /**
     * 分组列表。
     */
    private List<LiveGroup> groups = new ArrayList<>();

    /**
     * 频道列表。
     */
    private List<LiveChannel> channels = new ArrayList<>();

    /**
     * 是否为空（无频道）。
     *
     * @return 无频道返回 true
     */
    public boolean isEmpty() {
        return channels == null || channels.isEmpty();
    }
}
