package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * prometheus查询
 *
 * @author CH
 * @since 2025/5/29 16:44
 */
@Data
@Schema(description = "prometheus查询")
public class PrometheusGenQueryRequest {

    /**
     * prometheus地址
     */
    @Schema(description = "genId")
    private Integer monitorSysGenId;
    /**
     * promQL
     */
    @Schema(description = "promQL")
    private String promQL;

    /**
     * 开始时间（Unix 时间戳，秒级）
     */
    @Schema(description = "开始时间（Unix 时间戳，秒级）")
    private Long start;
    /**
     * 结束时间（Unix 时间戳，秒级）
     */
    @Schema(description = "结束时间（Unix 时间戳，秒级）")
    private Long end;
    /**
     * 步长（秒）
     */
    @Schema(description = "步长（秒）")
    private int step;
}
