package com.chua.starter.geo.service;

import com.chua.starter.geo.model.OverpassQueryRequest;

/**
 * Overpass OSM 矢量数据查询服务。
 *
 * @author CH
 * @since 1.0.0
 */
public interface OverpassService {

    /**
     * 执行 Overpass API 查询并返回结果统计。
     *
     * @param request 查询请求
     * @return 查询结果统计（元素数量等）
     */
    OverpassQueryResult query(OverpassQueryRequest request);

    /**
     * 查询结果封装。
     *
     * @param elementCount   查询到的元素数量
     * @param estimateSizeMb 预估输出大小（MB）
     */
    record OverpassQueryResult(int elementCount, double estimateSizeMb) {}
}
