package com.chua.starter.geo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 3D Tiles 下载请求体。
 *
 * @author CH
 * @since 1.0.0
 */
@Data
public class Tiles3dTaskRequest {
    /** 数据源配置 */
    @Valid
    @NotNull(message = "source不能为空")
    private Tiles3dSource source;

    /**
     * 用户选区多边形 [[lng, lat], ...] WGS-84。
     * 为 null 时表示下载全部内容。
     */
    private List<List<Double>> polygon;

    /** 保存路径 */
    @NotBlank(message = "savePath不能为空")
    private String savePath;

    /** 并发数 */
    private Integer concurrency;

    /** 代理地址 */
    private String proxy;
}
