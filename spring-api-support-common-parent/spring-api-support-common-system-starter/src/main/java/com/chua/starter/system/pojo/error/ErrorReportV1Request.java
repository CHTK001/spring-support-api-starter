package com.chua.starter.system.pojo.error;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 前端异常上报请求
 *
 * @param records 异常记录列表
 * @param time 客户端上报时间(毫秒时间戳)
 *
 * @since 2026/8/8
 * @author CH
 */
@Schema(description = "前端异常上报请求")
public record ErrorReportV1Request(
        @Schema(description = "异常记录列表") @NotEmpty(message = "异常记录列表不能为空") @Valid List<ErrorReportV1Item> records,
        @Schema(description = "客户端上报时间(毫秒时间戳)") @NotNull(message = "上报时间不能为空") Long time
) {
}
