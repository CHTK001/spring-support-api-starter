package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 日期查询
 *
 * @param startDate 开始日期
 * @param endDate 结束日期
 *
 * @since 2024/8/20
 * @author CH
 */
@Schema(description = "日期查询")
public record DateQuery(
        @Schema(description = "开始日期") String startDate,
        @Schema(description = "结束日期") String endDate
) {
}