package com.chua.starter.geo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 3D 瓷砖 下载请求体。
 *
 * @param source 数据源配置
 * @param polygon 用户选区多边形 [[lng, lat], ...] WGS-84。 为 空 时表示下载全部内容。
 * @param savePath 保存路径
 * @param concurrency 并发数
 * @param proxy 代理地址
 *
 * @author CH
 * @since 1.0.0
 */
public record Tiles3dTaskRequest(
        @Valid @NotNull(message = "source不能为空") Tiles3dSource source,
        List<List<Double>> polygon,
        @NotBlank(message = "savePath不能为空") String savePath,
        Integer concurrency,
        String proxy
) {
    /**
     * 规范构造器：对用户选区多边形做防御性拷贝。
     *
     * <p>value class 前置条件——集合组件必须深不可变；多边形为 空 表示下载全部内容，
     * 故保留 空 语义并采用可空安全包装。</p>
     *
     * @param source      数据源配置
     * @param polygon     用户选区多边形
     * @param savePath    保存路径
     * @param concurrency 并发数
     * @param proxy       代理地址
     */
    public Tiles3dTaskRequest {
        polygon = polygon == null ? null : Collections.unmodifiableList(new ArrayList<>(polygon));
    }
}
