package com.chua.starter.admin.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 日志统计数据
 */
@Data
@Schema(description = "日志统计数据")
public class LogStaticResult {
    /**
     * 成功的数据
     */
    @Schema(description = "成功的数据")
    private List<Integer> success;
    /**
     * 失败的数据
     */
    @Schema(description = "失败的数据")
    private List<Integer> failure;
}
