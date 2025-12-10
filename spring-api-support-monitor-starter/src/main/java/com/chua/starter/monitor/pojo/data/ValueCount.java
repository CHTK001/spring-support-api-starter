package com.chua.starter.monitor.pojo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 值-计数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueCount {
    @Schema(description = "值")
    private String value;
    @Schema(description = "计数")
    private long count;
}


