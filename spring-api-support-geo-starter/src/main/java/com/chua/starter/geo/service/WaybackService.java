package com.chua.starter.geo.service;

import com.chua.starter.geo.model.WaybackVersion;

import java.util.List;

/**
 * Esri Wayback 历史影像服务，查询 World Imagery 历史版本列表。
 *
 * @author CH
 * @since 1.0.0
 */
public interface WaybackService {

    /**
     * 获取所有历史影像版本列表。
     *
     * @return 版本列表
     */
    List<WaybackVersion> getVersions();
}
