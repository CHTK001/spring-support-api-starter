package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Duration;

/**
 * prometheus查询
 *
 * @author CH
 * @since 2025/5/29 16:44
 */
@Data
@Schema(description = "prometheus查询")
public class PrometheusQueryRequest {

    /**
     * prometheus地址
     */
    @Schema(description = "prometheus地址")
    private String prometheusAddress;
    /**
     * promQL
     */
    @Schema(description = "promQL")
    private String promQL;

    /**
     * 时间范围
     */
    @Schema(description = "时间范围")
    private Duration range;
    /**
     * 时间间隔
     */
    @Schema(description = "时间间隔")
    private Duration step;
}
